/*
 * Copyright (C) 2011 Linkeos.
 *
 * This file is part of BloatIt.
 *
 * BloatIt is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * BloatIt is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with BloatIt. If not, see <http://www.gnu.org/licenses/>.
 */
package com.bloatit.framework.xcgiserver.fcgi;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;

import com.bloatit.framework.xcgiserver.XcgiParser;

/**
 * <p>
 * Parse a fastcgi input stream to extract http params and post stream.
 * </p>
 * <p>
 * The fastcgi protocol specification can be found here: {@link http
 * ://www.fastcgi.com/devkit/doc/fcgi-spec.html}
 * </p>
 * <p>
 * The fastcgi protocol use 2 stream, 1 input stream and 1 output stream,
 * composed by records. The input stream contains the request and the
 * outputstream contains the response.
 * </p>
 * <p>
 * In the input stream, 2 virtuals stream are composed by 2 type of record: the
 * params records and the stdin record.
 * </p>
 *
 */
public class FCGIParser implements XcgiParser {

    /**
     * <p>
     * The response is encapsuled in FCGI_STDOUT record. The size of the content
     * of a record is coded with 2 bytes so the size can not exceed 65536 bytes.
     * The {@link FCGIOutputStream} charged to generete the output records is
     * not protected so a buffedOutputStream is use to cut the response in small
     * blocks.
     * </p>
     * The output record size mustn't be more than 65000 (max size of a record).
     * <p>
     * If the output record size is too small, the overhead weigth due to FCGI
     * record's header will be heavy.
     * </p>
     * <p>
     * If the output record size is too big, the global latency of the response
     * will increase.
     * </p>
     */
    private static final int DEFAULT_OUTPUT_RECORD_SIZE = 8192;

    /**
     * The FCGI version is always 1.
     */
    final static byte FCGI_VERSION_1 = 1;

    final static byte FCGI_BEGIN_REQUEST = 1;
    final static byte FCGI_ABORT_REQUEST = 2;
    final static byte FCGI_END_REQUEST = 3;
    final static byte FCGI_PARAMS = 4;
    final static byte FCGI_STDIN = 5;
    final static byte FCGI_STDOUT = 6;
    final static byte FCGI_STDERR = 7;
    final static byte FCGI_DATA = 8;
    final static byte FCGI_GET_VALUES = 9;
    final static byte FCGI_GET_VALUES_RESULT = 10;
    final static byte FCGI_UNKNOWN_TYPE = 11;
    final static byte FCGI_MAXTYPE = FCGI_UNKNOWN_TYPE;

    final static byte FCGI_KEEP_CONN = 1;

    final static byte FCGI_RESPONDER = 1;
    final static byte FCGI_AUTHORIZER = 2;
    final static byte FCGI_FILTER = 3;

    DataInputStream dataInput;
    boolean paramStreamOpen = true;
    boolean postStreamOpen = true;

    private final Map<String, String> env;

    private final OutputStream writeStream;
    private final FCGIPostStream postStream;

    public FCGIParser(final InputStream input, OutputStream bos) throws IOException {
        writeStream = new BufferedOutputStream(new FCGIOutputStream(this, new BufferedOutputStream(bos, 8192)), DEFAULT_OUTPUT_RECORD_SIZE);
        postStream = new FCGIPostStream(this);

        dataInput = new DataInputStream(input);
        env = new HashMap<String, String>();

    }

    public static class FCGIException extends IOException {

        private static final long serialVersionUID = 1L;

        public FCGIException(final String message) {
            super(message);
        }
    }

    @Override
    public OutputStream getResponseStream() {
        return writeStream;
    }

    @Override
    public Map<String, String> getEnv() throws IOException {

        while (paramStreamOpen) {
            parseRecord();
        }

        return env;
    }

    public void fetchPostRecord() throws IOException {

        // TODO: comment that
        while (postStreamOpen && !(parseRecord() == FCGI_STDIN))
            ;
    }

    public void fetchAll() throws IOException {
        while (paramStreamOpen && postStreamOpen) {
            parseRecord();
        }
    }

    private byte parseRecord() throws IOException {
        byte version = dataInput.readByte();
        byte type = dataInput.readByte();
        int requestId = dataInput.readUnsignedShort();
        int contentLength = dataInput.readUnsignedShort();
        int paddingLength = dataInput.readUnsignedByte();
        // Reserved byte
        dataInput.skip(1);

        if (version != FCGI_VERSION_1) {
            throw new FCGIException("Bad FCGI version code. Found '" + version + "' but '" + FCGI_VERSION_1 + "' excepted.");
        }

        if (requestId != 1) {
            throw new FCGIException("Bad request ID. Found '" + requestId + "' but '" + 1 + "' excepted.");
        }

        switch (type) {
        case FCGI_BEGIN_REQUEST:
            parseBeginRequestRecord(contentLength);
            break;
        case FCGI_ABORT_REQUEST:
            throw new NotImplementedException("TODO type: " + type);
            // break;
        case FCGI_PARAMS:
            parseParamsRecord(contentLength);
            break;
        case FCGI_STDIN:
            parseStdinRecord(contentLength);
            break;
        case FCGI_DATA:
            throw new NotImplementedException("TODO type: " + type);
            // break;
        case FCGI_GET_VALUES:
            throw new NotImplementedException("TODO type: " + type);
            // break;
        default:
            throw new FCGIException("Invalid type code for a record: " + type);
        }

        // Skip padding
        dataInput.skipBytes(paddingLength);
        return type;
    }

    private void parseBeginRequestRecord(int contentLenght) throws IOException {
        if (contentLenght != 8) {
            throw new FCGIException("Bad lenght for begin request record. Found '" + contentLenght + "' but '8' excepted.");
        }
        short role = dataInput.readShort();
        byte flags = dataInput.readByte();
        // Skip 5 reserved bytes
        dataInput.skipBytes(5);

        if ((flags & FCGI_KEEP_CONN) != 0) {
            throw new NotImplementedException("Keep Connection mode is not implemented");
        }

        if (role != FCGI_RESPONDER) {
            throw new NotImplementedException("Only FCGI responder role is implemented");
        }

    }

    private void parseParamsRecord(int contentLength) throws IOException {
        if (contentLength == 0) {
            // End of param stream
            paramStreamOpen = false;
            return;
        }

        int remainingLength = contentLength;

        while (remainingLength > 0) {
            int usedLength = parseNameValuePair();
            if (usedLength > remainingLength) {
                throw new FCGIException("Bad format un params record");
            }
            remainingLength -= usedLength;
        }
    }

    private int parseNameValuePair() throws IOException {
        int usedLength = 0;
        long nameLength = 0;
        int firstNameLengthByte = dataInput.readUnsignedByte();
        usedLength++;

        if ((firstNameLengthByte & 0x80) != 0) {// 10000000
            int[] lengthArray = new int[4];
            lengthArray[0] = firstNameLengthByte;
            lengthArray[1] = dataInput.readUnsignedByte();
            lengthArray[2] = dataInput.readUnsignedByte();
            lengthArray[3] = dataInput.readUnsignedByte();
            usedLength += 3;

            nameLength = unsignedIntToLong(lengthArray);
        } else {
            nameLength = firstNameLengthByte;
        }

        long valueLength = 0;

        int firstValueLengthByte = dataInput.readUnsignedByte();
        usedLength++;

        if ((firstValueLengthByte >> 7) == 1) { // 10000000
            int[] lengthArray = new int[4];
            lengthArray[0] = firstValueLengthByte;
            lengthArray[1] = dataInput.readUnsignedByte();
            lengthArray[2] = dataInput.readUnsignedByte();
            lengthArray[3] = dataInput.readUnsignedByte();
            usedLength += 3;

            valueLength = unsignedIntToLong(lengthArray);
        } else {
            valueLength = firstValueLengthByte;
        }

        byte[] nameArray = new byte[(int) nameLength];
        dataInput.read(nameArray);
        usedLength += nameLength;
        String name = new String(nameArray);

        byte[] valueArray = new byte[(int) valueLength];
        dataInput.read(valueArray);
        usedLength += valueLength;
        String value = new String(valueArray);

        env.put(name, value);

        return usedLength;
    }

    private void parseStdinRecord(int contentLength) throws IOException {
        if (contentLength == 0) {
            // End of stdin stream
            postStreamOpen = false;
            return;
        }

        byte[] data = new byte[contentLength];

        int readLength = 0;

        while (readLength < contentLength) {
            int size = dataInput.read(data, readLength, contentLength - readLength);
            if (size == -1) {
                throw new EOFException();
            }
            readLength += size;
        }
        postStream.pushData(data);
    }

    /**
     * Converts a 4 byte array of unsigned bytes to an long
     *
     * @param b
     *            an array of 4 unsigned bytes
     * @return a long representing the unsigned int
     */
    public static final long unsignedIntToLong(int[] b) {
        long l = 0;

        l += b[0];
        l += b[1] << 8;
        l += b[2] << 16;
        l += (b[3] & 0x7F) << 24;

        return l;
    }

    @Override
    public InputStream getPostStream() {
        return postStream;
    }

    public boolean isPostStreamOpen() {
        return postStreamOpen;
    }
}
