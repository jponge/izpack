package com.izforge.izpack.api.exception;

/**
 * Container exception.
 *
 * @author Tim Anderson
 */
public class ContainerException extends IzPackException
{

    /**
     * Constructs a <tt>ContainerException</tt>.
     *
     * @param message the error message
     */
    public ContainerException(String message)
    {
        super(message);
    }

    /**
     * Constructs a <tt>ContainerException</tt>.
     *
     * @param cause the cause
     */
    public ContainerException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Constructs a <tt>ContainerException</tt>.
     *
     * @param message the error message
     * @param cause   the cause
     */
    public ContainerException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
