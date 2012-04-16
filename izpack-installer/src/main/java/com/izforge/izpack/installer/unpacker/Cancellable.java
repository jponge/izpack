package com.izforge.izpack.installer.unpacker;

/**
 * Represents an operation that may be cancelled.
 *
 * @author Tim Anderson
 */
public interface Cancellable
{

    /**
     * Determines if the operation has been cancelled.
     *
     * @return <tt>true</tt> if the operation has been cancelled
     */
    boolean isCancelled();
}