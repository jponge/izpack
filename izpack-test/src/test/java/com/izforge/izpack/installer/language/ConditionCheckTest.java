package com.izforge.izpack.installer.language;

import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.api.installer.InstallerRequirementDisplay;
import com.izforge.izpack.compiler.container.TestInstallationContainer;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.junit.PicoRunner;
import org.hamcrest.core.Is;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import static org.hamcrest.MatcherAssert.assertThat;

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

    public ConditionCheckTest(ConditionCheck conditionCheck, ResourceManager resourceManager)
    {
        this.conditionCheck = conditionCheck;
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
