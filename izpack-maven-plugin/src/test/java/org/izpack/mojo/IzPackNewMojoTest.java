package org.izpack.mojo;

import com.izforge.izpack.matcher.ZipMatcher;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.hamcrest.collection.IsCollectionContaining;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test of new IzPack mojo
 *
 * @author Anthonin Bonnefoy
 */
public class IzPackNewMojoTest extends AbstractMojoTestCase
{

    @Test
    public void testExecute() throws Exception
    {
        File testPom = new File(
                Thread.currentThread().getContextClassLoader().getResource("basic-pom.xml").toURI()
        );
        IzPackNewMojo mojo = (IzPackNewMojo) lookupMojo("izpack", testPom);
        assertThat(mojo, IsNull.notNullValue());
        initIzpackMojo(mojo);

        mojo.execute();

        File outputResult = new File("target/izpackResult.jar");
        assertThat(outputResult.exists(), Is.is(true));
        assertThat(outputResult, ZipMatcher.isZipMatching(IsCollectionContaining.hasItems(
                "com/izforge/izpack/core/container/AbstractContainer.class",
                "com/izforge/izpack/uninstaller/Destroyer.class",
                "com/izforge/izpack/panels/checkedhello/CheckedHelloPanel.class",
                "META-INF/Test.png"
        )));

        ZipFile zipFile = new ZipFile(outputResult);
        ZipArchiveEntry entry = zipFile.getEntry("META-INF/MANIFEST.MF");
        InputStream content = zipFile.getInputStream(entry);
        try
        {
            List<String> list = IOUtils.readLines(content);
            assertThat(list, IsCollectionContaining.hasItem("SplashScreen-Image: META-INF/Test.png"));
        }
        finally
        {
            content.close();
        }

    }

    private void initIzpackMojo(IzPackNewMojo mojo) throws IllegalAccessException
    {
        File installFile = new File("target/test-classes/helloAndFinish.xml");
        setVariableValueToObject(mojo, "comprFormat", "default");
        setVariableValueToObject(mojo, "installFile", installFile.getAbsolutePath());
        setVariableValueToObject(mojo, "kind", "standard");
        setVariableValueToObject(mojo, "baseDir", new File("target/test-classes/").getAbsolutePath());
        setVariableValueToObject(mojo, "output", "target/izpackResult.jar");
        setVariableValueToObject(mojo, "comprLevel", -1);
    }
}