package com.izforge.izpack.api.exception;


/**
 * An exception thrown when resource retrieval is interrupted.
 *
 * @author Tim Anderson
 */
public class ResourceInterruptedException extends ResourceException
{
    /**
     * Constructs a {@code ResourceInterruptedException}.
     *
     * @param message the the error message
     */
    public ResourceInterruptedException(String message)
    {
        super(message);
    }

    /**
     * Constructs a {@code ResourceInterruptedException}.
     *
     * @param message the the error message
     * @param cause   the cause
     */
    public ResourceInterruptedException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
