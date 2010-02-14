package org.izpack.mojo;

import com.izforge.izpack.matcher.ZipMatcher;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.hamcrest.collection.IsCollectionContaining;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test of new IzPack mojo
 *
 * @author Anthonin Bonnefoy
 */
public class IzPackNewMojoTest extends AbstractMojoTestCase {

    @Test
    public void testExecute() throws Exception {
        File testPom = new File(getBasedir(), "target/test-classes/basic-pom.xml");
        if (!testPom.exists()) {
            testPom = new File(getBasedir(), "izpack-maven-plugin/target/test-classes/basic-pom.xml");
        }
        IzPackNewMojo mojo = (IzPackNewMojo) lookupMojo("compile", testPom);
        assertThat(mojo, IsNull.notNullValue());
        initIzpackMojo(mojo);

        mojo.execute();

        File outputResult = new File("target/izpackResult.jar");
        assertThat(outputResult.exists(), Is.is(true));
        assertThat(outputResult,
                ZipMatcher.isZipMatching(IsCollectionContaining.hasItems(
                        "com/izforge/izpack/panels/hello/HelloPanel.class",
                        "com/izforge/izpack/core/container/AbstractContainer.class",
                        "com/izforge/izpack/panels/simplefinish/SimpleFinishPanel.class")));
    }

    private void initIzpackMojo(IzPackNewMojo mojo) throws IllegalAccessException {
        File installFile = new File("target/test-classes/helloAndFinish.xml");
        setVariableValueToObject(mojo, "comprFormat", "default");
        setVariableValueToObject(mojo, "installFile", installFile.getAbsolutePath());
        setVariableValueToObject(mojo, "kind", "standard");
        setVariableValueToObject(mojo, "baseDir", new File("target/test-classes/").getAbsolutePath());
        setVariableValueToObject(mojo, "output", "target/izpackResult.jar");
        setVariableValueToObject(mojo, "comprLevel", -1);
    }

}