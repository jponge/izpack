package com.izforge.izpack.uninstaller.event;

import com.izforge.izpack.api.event.ProgressListener;


/**
 * Destroyer progress listener.
 *
 * @author Tim Anderson
 */
public abstract class DestroyerListener implements ProgressListener
{

    /**
     * The next step starts.
     *
     * @param stepName     the name of the step which starts now.
     * @param stepNo       the number of the step.
     * @param noOfSubSteps the number of sub-steps this step consists of
     */
    @Override
    public void nextStep(String stepName, int stepNo, int noOfSubSteps)
    {
        // not used
    }

    /**
     * Set the number of substeps.
     * <p/>
     * This may be used if the number of substeps changes during an action.
     *
     * @param subSteps The number of substeps.
     */
    @Override
    public void setSubStepNo(int subSteps)
    {
        // not used
    }
}
