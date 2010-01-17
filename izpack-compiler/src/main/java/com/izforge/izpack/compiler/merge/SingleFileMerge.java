package com.izforge.izpack.compiler.merge;

import com.izforge.izpack.compiler.packager.PackagerHelper;
import org.apache.tools.zip.ZipOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Single file merge
 *
 * @author Anthonin Bonnefoy
 */
public class SingleFileMerge implements Mergeable {

    private File fileToCopy;

    public SingleFileMerge(File fileToCopy) {
        this.fileToCopy = fileToCopy;
    }

    public void merge(ZipOutputStream outputStream) {
        try {
            FileInputStream inputStream = new FileInputStream(fileToCopy);
            PackagerHelper.copyStreamToJar(inputStream, outputStream, fileToCopy.getName(), fileToCopy.lastModified());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
