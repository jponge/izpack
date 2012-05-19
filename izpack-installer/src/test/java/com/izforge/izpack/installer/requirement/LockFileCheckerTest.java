package com.izforge.izpack.installer.requirement;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.core.handler.ConsolePrompt;
import com.izforge.izpack.installer.data.InstallData;
import com.izforge.izpack.test.util.TestConsole;
import com.izforge.izpack.util.FileUtil;

/**
 * Tests the {@link LockFileChecker} class.
 *
 * @author Tim Anderson
 */
public class LockFileCheckerTest
{
    /**
     * The installation data.
     */
    private InstallData installData;

    /**
     * Constructs a <tt>LockFileCheckerTest</tt>.
     */
    public LockFileCheckerTest()
    {
        installData = new InstallData(null);
        Info info = new Info();
        installData.setInfo(info);
    }

    /**
     * Tests the {@link LockFileChecker}.
     */
    @Test
    public void testLockFile()
    {
        String appName = "TestApp" + System.currentTimeMillis();
        installData.getInfo().setAppName(appName);
        TestConsole console = new TestConsole();
        ConsolePrompt prompt = new ConsolePrompt(console);
        LockFileChecker checker = new LockFileChecker(installData, prompt);

        // no lock file yet.
        assertTrue(checker.check());

        // lock file should now exist. Enter n to cancel
        console.addScript("LockFileExists-enter-N", "n");
        assertFalse(checker.check());

        // rerun the check, this time selecting Y to continue
        console.addScript("LockFileExists-enter-Y", "y");
        assertTrue(checker.check());

        // now delete the lock file and verify the check returns true
        File file = FileUtil.getLockFile(appName);
        assertTrue(file.delete());
        assertTrue(checker.check());
    }
}
