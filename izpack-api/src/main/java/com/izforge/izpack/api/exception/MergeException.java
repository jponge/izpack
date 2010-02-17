package com.izforge.izpack.api.exception;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Anthonin Bonnefoy
 */
public class MergeException extends RuntimeException {
    public MergeException(String message) {
        super(message);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public MergeException(String message, Throwable cause) {
        super(message, cause);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public MergeException(Throwable cause) {
        super(cause);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
