package com.izforge.izpack.api.event;

/**
 * This interface is used to notify the user of some progress.
 * <p/>
 * For example, the installation progress and compilation progress are communicated to the user
 * using this interface. The interface supports a two-stage progress indication: The whole action is
 * divided into steps (for example, packs when installing) and sub-steps (for example, files of a pack).
 */
public interface ProgressListener
{
    /**
     * Invoked when an action starts.
     *
     * @param name      the name of the action
     * @param stepCount the number of steps the action consists of
     */
    void startAction(String name, int stepCount);

    /**
     * Invoked when an action finishes.
     */
    void stopAction();

    /**
     * Invoked when an action step starts.
     *
     * @param stepName     the name of the step
     * @param stepNo       the step number
     * @param subStepCount the number of sub-steps the step consists of
     */
    void nextStep(String stepName, int stepNo, int subStepCount);

    /**
     * Sets the number of sub-steps.
     * <p/>
     * This may be used if the number of sub-steps changes during an action.
     *
     * @param count the number of sub-steps
     */
    void setSubStepNo(int count);

    /**
     * Notify of progress.
     *
     * @param subStepNo the sub-step which will be performed next
     * @param message   an additional message describing the sub-step
     */
    void progress(int subStepNo, String message);
}