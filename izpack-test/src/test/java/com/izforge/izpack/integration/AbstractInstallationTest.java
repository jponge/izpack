package com.izforge.izpack.integration;

import java.io.File;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import com.izforge.izpack.api.data.AutomatedInstallData;


/**
 * Base class for installation integration test cases.
 *
 * @author Tim Anderson
 */
public class AbstractInstallationTest
{
    /**
     * Temporary folder to perform installations to.
     */
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    /**
     * The installation data.
     */
    private AutomatedInstallData installData;


    /**
     * Constructs an <tt>AbstractInstallationTest</tt>.
     *
     * @param installData the installation data
     */
    public AbstractInstallationTest(AutomatedInstallData installData)
    {
        this.installData = installData;
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Before
    public void setUp() throws Exception
    {
        // write to temporary folder so the test doesn't need to be run with elevated permissions
        File installPath = new File(temporaryFolder.getRoot(), "izpackTest");
        installData.setInstallPath(installPath.getAbsolutePath());
    }

    /**
     * Returns the install path.
     *
     * @return the install path
     */
    protected String getInstallPath()
    {
        return installData.getInstallPath();
    }

    /**
     * Returns the install data.
     *
     * @return the install data
     */
    protected AutomatedInstallData getInstallData()
    {
        return installData;
    }
}
