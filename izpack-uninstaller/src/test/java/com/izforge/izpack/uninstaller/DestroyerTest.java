package com.izforge.izpack.uninstaller;

import com.izforge.izpack.api.container.BindeableContainer;
import com.izforge.izpack.api.handler.AbstractUIProgressHandler;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.matchers.IsCollectionContaining;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import java.io.*;
import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test destroyer
 *
 * @author Anthonin Bonnefoy
 */
public class DestroyerTest
{
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private File logInstall;

    @Test
    public void testGetInstallFiles() throws Exception
    {
        Destroyer destroyer = new Destroyer("", false, Mockito.mock(AbstractUIProgressHandler.class),
                Mockito.mock(BindeableContainer.class));
        ArrayList<File> files = createTestFiles();
        ArrayList<File> filesList = destroyer.readBufferForFileList(
                new BufferedReader(
                        new InputStreamReader(new FileInputStream(logInstall))
                ));

        assertThat(filesList, IsCollectionContaining.<File>hasItems(
                files.get(1), files.get(2)));
    }

    private ArrayList<File> createTestFiles() throws IOException
    {
        File temporaryFolder = this.temporaryFolder.newFolder("tempTestFolder");
        logInstall = new File(temporaryFolder, "install.log");
        logInstall.createNewFile();
        ArrayList<File> listFileToDelete = new ArrayList<File>();
        listFileToDelete.add(temporaryFolder);
        String[] strings = {"one", "two"};
        for (String string : strings)
        {
            File file = new File(temporaryFolder, string);
            file.createNewFile();
            listFileToDelete.add(file.getAbsoluteFile());
        }
        FileUtils.writeLines(logInstall, listFileToDelete);
        return listFileToDelete;
    }
}
