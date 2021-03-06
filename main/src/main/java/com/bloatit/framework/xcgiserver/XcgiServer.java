/*
 * Copyright (C) 2011 Linkeos.
 *
 * This file is part of BloatIt.
 *
 * BloatIt is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * BloatIt is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with BloatIt. If not, see <http://www.gnu.org/licenses/>.
 */
package com.bloatit.framework.xcgiserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.ByteChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bloatit.common.Log;
import com.bloatit.framework.FrameworkConfiguration;
import com.bloatit.framework.exceptions.highlevel.BadProgrammerException;
import com.bloatit.framework.utils.parameters.Parameters;
import com.bloatit.framework.webprocessor.context.SessionManager;
import com.bloatit.framework.xcgiserver.RequestKey.Source;
import com.bloatit.framework.xcgiserver.fcgi.FCGIParser;

public final class XcgiServer {

    private static final int SCGI_PORT = FrameworkConfiguration.getXcgiListenport();
    private static final int NB_THREADS = FrameworkConfiguration.getXcgiThreadsNumber();

    private final List<XcgiThread> threads = new ArrayList<XcgiThread>(NB_THREADS);
    private final List<XcgiProcessor> processors = new ArrayList<XcgiProcessor>();

    private final Guard guard = new Guard();

    private static class Guard {
        private final Map<String, Integer> lastAccessedIp = new HashMap<String, Integer>();
        private long lasteTimestamp = new Date().getTime();

        public void access(final String ip, final long timestamp) {
            if (timestamp > (lasteTimestamp + FrameworkConfiguration.getXcgiBlockerElapse())) {
                lasteTimestamp = timestamp;
                lastAccessedIp.clear();
                lastAccessedIp.put(ip, 1);
            } else {
                Integer occur = lastAccessedIp.get(ip);
                if (occur == null) {
                    lastAccessedIp.put(ip, 1);
                } else {
                    lastAccessedIp.put(ip, ++occur);
                    if (occur > FrameworkConfiguration.getXcgiBlockerNbRequest()) {
                        Log.fakesshguard().error("Invalid user inexu from " + ip);
                    }
                }
            }
        }
    }

    public XcgiServer() {
        // Nothing ?
    }

    public void addProcessor(final XcgiProcessor processor) {
        this.processors.add(processor);
    }

    private List<XcgiProcessor> getProcessors() {
        return processors;
    }

    public void initialize() throws IOException {
        SessionManager.loadSessions();
        Log.framework().info("Init: Start BloatIt serveur");

        Log.framework().info("-> initializing all processors");
        for (final XcgiProcessor processor : processors) {
            Log.framework().info("--> initialization processor: " + processor.getClass().getSimpleName());
            if (!processor.initialize()) {
                throw new BadProgrammerException("Initialization of processor failed");
            }
        }

        Log.framework().info("-> launching " + NB_THREADS + " threads on ports " + SCGI_PORT + " ...");
        for (int i = SCGI_PORT; i < SCGI_PORT + NB_THREADS; ++i) {
            threads.add(new XcgiThread(i));
        }
    }

    public void start() {
        for (final XcgiThread thread : threads) {
            thread.start();
        }

        for (final XcgiThread thread : threads) {
            try {
                thread.join();
            } catch (final InterruptedException e) {
                Log.framework().fatal("", e);
            }
        }
    }

    private final class XcgiThread extends Thread {
        private static final int NB_MAX_SOCKET_ERROR = 12;
        private SocketChannel socket;
        private final ServerSocketChannel provider;

        private final Timer timer;

        private XcgiThread(final int port) throws IOException {
            super();
            provider = ServerSocketChannel.open();
            provider.socket().bind(new InetSocketAddress(InetAddress.getByName(FrameworkConfiguration.getXcgiListenAddress()), port));
            timer = new Timer();
        }

        @Override
        public void run() {
            int nbError = 0;
            while (true) {
                try {
                    generateAndSendReponse();
                } catch (final IOException e) {
                    nbError++;
                    if (nbError > NB_MAX_SOCKET_ERROR) {
                        throw new BadProgrammerException("Too much errors on this socket.", e);
                    }
                    Log.framework().fatal("Socket error on port: " + provider.socket().getLocalPort(), e);
                }
            }
        }

        private void kill() {
            if (socket != null && socket.isOpen()) {
                try {
                    socket.close();
                } catch (final IOException e) {
                    Log.framework().fatal("Cannot close the socket.", e);
                }
            }
        }

        private void generateAndSendReponse() throws IOException {
            // Wait for connection
            Log.framework().trace("Waiting connection");

            // Wait for a connection.
            socket = provider.accept();
            Log.framework().trace("Received a connection");
            timer.start();

            // Parse the header and the post data.
            final XcgiParser parser = getXCGIParser(socket);

            final Map<String, String> env = parser.getHeaders();
            final HttpHeader header = new HttpHeader(env);
            final HttpPost post = new HttpPost(parser.getPostStream(), header.getContentLength(), header.getContentType());

            Log.framework().debug(env);

            // LOGGING REQUESTS
            // make sure bash injection is not possible
            final StringBuilder request = new StringBuilder();
            request.append("Access:Request: ");
            request.append("REQUEST_URI='");
            request.append(header.getRequestUri().replaceAll("'", ""));
            request.append("'; REQUEST_METHOD='");
            request.append(header.getRequestMethod().replaceAll("'", ""));
            request.append("'; USER_AGENT='");
            request.append(header.getHttpUserAgent().replaceAll("'", ""));
            request.append("'; ACCEPT_LANGUAGES='");
            request.append(header.getHttpAcceptLanguage());
            request.append("'; HTTP_REFERER='");
            request.append(header.getHttpReferer().replaceAll("'", ""));
            request.append("'; REMOTE_ADDR='");
            request.append(header.getRemoteAddr().replaceAll("'", ""));
            request.append("'; SERVER_PROTOCOL='");
            request.append(header.getServerProtocol());
            request.append("'; SERVER_ADDR='");
            request.append(header.getServerAddr());
            request.append('"');
            Log.framework().info(request.toString());

            if (FrameworkConfiguration.getXcgiBlockerEnable()) {
                guard.access(header.getServerAddr(), new Date().getTime());
            }

            try {
                for (final XcgiProcessor processor : getProcessors()) {
                    if (processor.process(createKey(header, header.getGetParameters()), header, post, new HttpResponse(parser.getResponseStream(),
                                                                                                                       header))) {
                        // Check temporary autentification
                        break;
                    }
                }
            } catch (final IOException e) {
                Log.framework().fatal("SCGIServer: IOException on the socket output", e);
            } catch (final RuntimeException e) {
                Log.framework().fatal("SCGIServer: Unknown RuntimeException", e);
            } catch (final Exception e) {
                Log.framework().fatal("SCGIServer: Unknown Exception", e);
            } finally {
                Log.framework().trace("Closing connection");
                parser.getResponseStream().close();
            }

            Log.framework().debug("Page generated in " + timer.elapsed() + " ms");
        }

        private RequestKey createKey(final HttpHeader header, final Parameters parameters) {
            final String ipAddress = header.getRemoteAddr();
            try {
                if (parameters.containsKey("access_token")) {
                    // Works for POST and GET
                    return new RequestKey(parameters.look("access_token").getSimpleValue(), ipAddress, Source.TOKEN);
                }
                if (!HttpHeader.AUTHORIZATION_UNKNOWN.equals(header.getHttpAuthorizationType())) {
                    if ("Bearer".equals(header.getHttpAuthorizationType())) {
                        return new RequestKey(header.getHttpAuthorizationData(), ipAddress, Source.TOKEN);
                    }
                }
                if (header.getHttpCookie().containsKey("session_key")) {
                    return new RequestKey(header.getHttpCookie().get("session_key"), ipAddress, Source.COOKIE);
                }
                return new RequestKey(ipAddress);
            } catch (final WrongSessionKeyFormatException e) {
                return new RequestKey(ipAddress);
            }
        }

        private XcgiParser getXCGIParser(final ByteChannel channel) throws IOException {
            // You can also use scgi parser
            return new FCGIParser(channel);
        }
    }

    public void stop() {
        for (final XcgiThread thread : threads) {
            if (thread.isAlive()) {
                thread.kill();
            }
        }
    }
}
