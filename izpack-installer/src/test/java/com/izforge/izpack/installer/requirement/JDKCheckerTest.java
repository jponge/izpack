package com.izforge.izpack.installer.requirement;

import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.installer.console.ConsolePrompt;
import com.izforge.izpack.installer.data.InstallData;
import com.izforge.izpack.test.io.TestConsole;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link com.izforge.izpack.installer.requirement.JavaVersionChecker} class.
 *
 * @author Tim Anderson
 */
public class JDKCheckerTest
{
    /**
     * The installation data.
     */
    private InstallData installData;

    /**
     * Constructs a <tt>JavaVersionCheckerTest</tt>.
     */
    public JDKCheckerTest()
    {
        installData = new InstallData(null, null);
        Info info = new Info();
        installData.setInfo(info);
    }

    /**
     * Tests the {@link com.izforge.izpack.installer.requirement.JavaVersionChecker}.
     */
    @Test
    public void testJavaVersion()
    {
        TestConsole console = new TestConsole();
        ConsolePrompt prompt = new ConsolePrompt(console);
        JavaVersionChecker checker = new JavaVersionChecker(installData, prompt);

        installData.getInfo().setJavaVersion(null);
        assertTrue(checker.check());

        String currentVersion = System.getProperty("java.version");
        installData.getInfo().setJavaVersion("9" + currentVersion);
        assertFalse(checker.check());

        installData.getInfo().setJavaVersion(currentVersion);
        assertTrue(checker.check());
    }
}
