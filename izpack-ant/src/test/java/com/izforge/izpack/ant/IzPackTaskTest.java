package com.izforge.izpack.ant;

import com.izforge.izpack.matcher.ZipMatcher;
import org.hamcrest.collection.IsCollectionContaining;
import org.hamcrest.core.Is;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Anthonin Bonnefoy
 */
public class IzPackTaskTest
{

    @Test
    public void testExecuteAntAction() throws IllegalAccessException
    {

        IzPackTask task = new IzPackTask();
        initIzpackTask(task);
        task.execute();

        File outputResult = new File("target/izpackResult.jar");
        assertThat(outputResult.exists(), Is.is(true));
        assertThat(outputResult, ZipMatcher.isZipMatching(IsCollectionContaining.hasItem(
                "com/izforge/izpack/panels/checkedhello/CheckedHelloPanel.class"
        )));
        assertThat(outputResult, ZipMatcher.isZipMatching(IsCollectionContaining.hasItem(
                "com/izforge/izpack/core/container/AbstractContainer.class"
        )));
        assertThat(outputResult, ZipMatcher.isZipMatching(IsCollectionContaining.hasItem(
                "com/izforge/izpack/uninstaller/Destroyer.class"
        )));

    }

    private void initIzpackTask(IzPackTask task) throws IllegalAccessException
    {
        File installFile = new File("target/test-classes/helloAndFinish.xml");
        task.setInput(installFile.getAbsolutePath());
        task.setBasedir(new File("target/test-classes/").getAbsolutePath());
        task.setOutput("target/izpackResult.jar");
        task.setCompression("default");
        task.setCompressionLevel(-1);
    }

}
