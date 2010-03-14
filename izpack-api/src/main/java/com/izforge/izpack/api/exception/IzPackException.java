package com.izforge.izpack.api.exception;

/**
 * Izpack specific exception
 *
 * @author Anthonin Bonnefoy
 */
public class IzPackException extends RuntimeException
{
    public IzPackException(String message)
    {
        super(message);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public IzPackException(Throwable cause)
    {
        super(cause);
    }

    public IzPackException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
