package com.izforge.izpack.uninstaller.event;

import com.izforge.izpack.api.handler.AbstractUIProgressHandler;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.core.handler.PromptUIHandler;


/**
 * Destroyer handler.
 *
 * @author Tim Anderson
 */
public abstract class DestroyerHandler extends PromptUIHandler implements AbstractUIProgressHandler
{

    /**
     * Constructs a {@code DestroyerHandler}.
     *
     * @param prompt the prompt
     */
    public DestroyerHandler(Prompt prompt)
    {
        super(prompt);
    }

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
