package com.izforge.izpack.installer.requirement;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.core.handler.ConsolePrompt;
import com.izforge.izpack.installer.data.InstallData;
import com.izforge.izpack.test.util.TestConsole;

/**
 * Tests the {@link JavaVersionChecker} class.
 *
 * @author Tim Anderson
 */
public class JavaVersionCheckerTest
{
    /**
     * The installation data.
     */
    private InstallData installData;

    /**
     * Constructs a <tt>JavaVersionCheckerTest</tt>.
     */
    public JavaVersionCheckerTest()
    {
        installData = new InstallData(null);
        Info info = new Info();
        installData.setInfo(info);
    }

    /**
     * Tests the {@link JavaVersionChecker}.
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
