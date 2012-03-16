package com.izforge.izpack.ant;

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

import org.apache.tools.ant.Project;
import org.hamcrest.collection.IsCollectionContaining;
import org.hamcrest.core.Is;
import org.junit.Ignore;
import org.junit.Test;

import com.izforge.izpack.matcher.ZipMatcher;

/**
 * @author Anthonin Bonnefoy
 */
public class IzPackTaskTest
{

    @Test
    @Ignore
    public void testExecuteAntAction() throws IllegalAccessException, InterruptedException, IOException
    {

        IzPackTask task = new IzPackTask();
        initIzpackTask(task);
        task.execute();

        Thread.sleep(30000);
        File file = new File("target/izpackResult.jar");
        ZipFile zipFile = new ZipFile(file);
        assertThat(file.exists(), Is.is(true));
        assertThat(zipFile, ZipMatcher.isZipMatching(IsCollectionContaining.hasItems(
                "com/izforge/izpack/panels/checkedhello/CheckedHelloPanel.class",
                "com/izforge/izpack/core/container/AbstractContainer.class",
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
