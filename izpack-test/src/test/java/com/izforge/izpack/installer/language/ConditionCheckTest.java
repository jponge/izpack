package com.izforge.izpack.installer.language;

import static org.hamcrest.MatcherAssert.assertThat;

import org.hamcrest.core.Is;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.installer.InstallerRequirementDisplay;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.compiler.container.TestInstallationContainer;
import com.izforge.izpack.core.resource.ResourceManager;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.junit.PicoRunner;

/**
 * Test installerRequirements
 *
 * @author Anthonin Bonnefoy
 */
@RunWith(PicoRunner.class)
@Container(TestInstallationContainer.class)
public class ConditionCheckTest
{

    private ConditionCheck conditionCheck;
    private ResourceManager resourceManager;

    public ConditionCheckTest(AutomatedInstallData installData, ResourceManager resourceManager,
                              RulesEngine rules)
    {
        this.conditionCheck = new ConditionCheck(installData, resourceManager, rules);
        this.resourceManager = resourceManager;
    }

    @Test
    @InstallFile("samples/checkRequirement.xml")
    public void testCheckInstallerRequirements() throws Exception
    {
        InstallerRequirementDisplay requirementDisplay = Mockito.mock(InstallerRequirementDisplay.class);
        assertThat(conditionCheck.checkInstallerRequirements(requirementDisplay), Is.is(false));
        Mockito.verify(requirementDisplay).showMissingRequirementMessage("42");
    }
}
