package com.izforge.izpack.api.event;


/**
 * A {@link ProgressListener} that may be restarted.
 *
 * @author Klaus Bartz
 */
public interface RestartableProgressListener extends ProgressListener
{

    /**
     * Invoked when an action restarts.
     *
     * @param name       the name of the action
     * @param overallMsg message to be used in the overall label
     * @param tip        message to be used in the tip label
     * @param steps      the number of steps the action consists of
     */
    void restartAction(String name, String overallMsg, String tip, int steps);

    /**
     * Invoked to notify progress.
     * <p/>
     * This increments the current step.
     *
     * @param message a message describing the step
     */
    void progress(String message);

}
