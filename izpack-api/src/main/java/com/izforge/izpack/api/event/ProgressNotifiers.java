package com.izforge.izpack.api.event;


/**
 * Container for {@link InstallListener listeners} that may notify an {@link ProgressListener}.
 *
 * @author Tim Anderson
 */
public interface ProgressNotifiers
{

    /**
     * Adds a listener that may notify a {@link ProgressListener}.
     *
     * @param listener the listener
     */
    void addNotifier(InstallListener listener);

    /**
     * Retursn the index of the specified listener.
     *
     * @param listener the listener
     * @return the index of the listener or {@code -1} if it is not registered
     */
    int indexOf(InstallListener listener);

    /**
     * Determines if listeners should notify an {@link ProgressListener}.
     *
     * @param notify if {@code true}, notify the {@link ProgressListener}
     */
    void setNotifyProgress(boolean notify);

    /**
     * Determines if listeners should notify an {@link ProgressListener}.
     *
     * @return {@code true} if the {@link ProgressListener} should be notified
     */
    boolean notifyProgress();

    /**
     * Returns the count of registered listeners that may perform notification.
     *
     * @return the count of registered listeners
     */
    int getNotifiers();
}