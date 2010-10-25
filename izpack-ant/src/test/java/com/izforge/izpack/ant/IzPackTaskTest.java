package com.izforge.izpack.ant;

import com.izforge.izpack.matcher.ZipMatcher;
import org.apache.tools.ant.Project;
import org.hamcrest.collection.IsCollectionContaining;
import org.hamcrest.core.Is;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Anthonin Bonnefoy
 */
public class IzPackTaskTest
{

    @Test
    @Ignore
    public void testExecuteAntAction() throws IllegalAccessException, InterruptedException
    {

        IzPackTask task = new IzPackTask();
        initIzpackTask(task);
        task.execute();

        Thread.sleep(30000);
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
        File installFile = new File(getClass().getClassLoader().getResource("helloAndFinish.xml").getFile());
        task.setInput(installFile.getAbsolutePath());
        task.setBasedir(getClass().getClassLoader().getResource("").getFile());
        task.setOutput("target/izpackResult.jar");
        task.setCompression("default");
        task.setCompressionLevel(-1);
        task.setProject(new Project());
    }

}
