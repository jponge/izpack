package org.izpack.mojo;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;

import java.io.File;
import java.util.Collections;

/**
 * Test of izpack mojo
 *
 * @author Anthonin Bonnefoy
 */
public class IzPackMojoTest extends AbstractMojoTestCase {
    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {

        // required for mojo lookups to work
        super.setUp();
    }

    /**
     * @throws Exception
     */
    public void testMojoGoal() throws Exception {
        File testPom = new File(getBasedir(),
                "target/test-classes/basic-pom.xml");

//        IzPackMojo mojo = (IzPackMojo) lookupMojo("izpack",testPom);
//        assertThat(mojo, IsNull.<Object>notNullValue());
//        initIzpackMojo(mojo);
//        mojo.execute();
//        assertNotNull(mojo);
    }

    private void initIzpackMojo(IzPackMojo mojo) throws IllegalAccessException {
        File installFile = new File("target/test-classes/helloAndFinish.xml");
        File customPanel = new File("target/test-classes/panels");
        setVariableValueToObject(mojo, "installerFile", installFile);

        IzPackMavenProjectStub project = new IzPackMavenProjectStub();
        setVariableValueToObject(mojo, "project", project);

        setVariableValueToObject(mojo, "customPanelDirectory", customPanel);
        setVariableValueToObject(mojo, "classpathElements", Collections.<Object>emptyList());
        setVariableValueToObject(mojo, "izpackBasedir", new File("target/test-classes/"));
        setVariableValueToObject(mojo, "descriptor", installFile);
    }


}
