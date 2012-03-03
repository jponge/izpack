package com.izforge.izpack.installer.requirement;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.InstallerRequirement;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.installer.RequirementChecker;
import com.izforge.izpack.api.rules.Condition;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.util.Debug;

/**
 * Evaluates each {@link InstallerRequirement} returned by {@link AutomatedInstallData#getInstallerrequirements()}
 * to determine if installation should proceed.
 *
 * @author Tim Anderson
 */
public class InstallerRequirementChecker implements RequirementChecker
{
    /**
     * The installation data.
     */
    private final AutomatedInstallData installData;

    /**
     * The rules engine.
     */
    private final RulesEngine rules;

    /**
     * The prompt.
     */
    private final Prompt prompt;

    /**
     * Constructs a <tt>InstallerRequirementChecker</tt>.
     *
     * @param installData the installation data.
     * @param rules       the rules engine
     * @param prompt      the prompt
     */
    public InstallerRequirementChecker(AutomatedInstallData installData, RulesEngine rules, Prompt prompt)
    {
        this.installData = installData;
        this.rules = rules;
        this.prompt = prompt;
    }

    /**
     * Determines if installation requirements are met.
     *
     * @return <tt>true</tt> if requirements are met, otherwise <tt>false</tt>
     */
    @Override
    public boolean check()
    {
        boolean fail = false;

        for (InstallerRequirement requirement : installData.getInstallerrequirements())
        {
            String id = requirement.getCondition();
            Condition condition = rules.getCondition(id);
            if (condition == null)
            {
                fail = true;
                Debug.error(id + " is not a valid condition.");
                break;
            }
            if (!condition.isTrue())
            {
                fail = true;
                String message = requirement.getMessage();
                if ((message != null) && (message.length() > 0))
                {
                    String localizedMessage = installData.getLangpack().getString(message);
                    prompt.message(Prompt.Type.ERROR, localizedMessage);
                }
                break;
            }
        }
        return !fail;
    }

}
