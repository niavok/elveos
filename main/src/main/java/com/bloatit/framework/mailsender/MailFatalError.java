package com.bloatit.framework.mailsender;

import com.bloatit.framework.exceptions.FatalErrorException;

public class MailFatalError extends FatalErrorException {
    private static final long serialVersionUID = -1660347313919720091L;

    public MailFatalError(final String string, final Throwable cause) {
        super(string, cause);
    }

    public MailFatalError(final Throwable cause) {
        super(cause);
    }

    public MailFatalError(final String message) {
        super(message);
    }

}