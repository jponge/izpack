package com.izforge.izpack.api.exception;


/**
 * Unchecked wrapper around <tt>ClassNotFoundException</tt>.
 *
 * @author Tim Anderson
 */
public class IzPackClassNotFoundException extends IzPackException
{

    /**
     * Constructs a <tt>IzPackClassNotFoundException</tt>.
     *
     * @param className the class name
     * @param exception the exception
     */
    public IzPackClassNotFoundException(String className, ClassNotFoundException exception)
    {
        super("Class '" + className + "' not found", exception);
    }

}
