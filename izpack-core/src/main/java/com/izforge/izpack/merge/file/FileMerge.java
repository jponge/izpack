package com.izforge.izpack.merge.file;

import com.izforge.izpack.api.exception.MergeException;
import com.izforge.izpack.merge.AbstractMerge;
import com.izforge.izpack.util.IoHelper;
import org.apache.tools.zip.ZipOutputStream;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * File merge. Can be a single file or a directory.
 *
 * @author Anthonin Bonnefoy
 */
public class FileMerge extends AbstractMerge
{

    private File fileToCopy;

    private String destination;

    public FileMerge(URL url, Map<OutputStream, List<String>> mergeContent)
    {
        this(url, "", mergeContent);
    }

    public FileMerge(URL url, String destination, Map<OutputStream, List<String>> mergeContent)
    {
        this.mergeContent = mergeContent;
        try
        {
            this.fileToCopy = new File(url.toURI());
        }
        catch (URISyntaxException e)
        {
            throw new MergeException(e);
        }
        this.destination = destination;
    }

    public File find(FileFilter fileFilter)
    {
        return findRecursivelyForFile(fileFilter, fileToCopy);
    }

    public List<File> recursivelyListFiles(FileFilter fileFilter)
    {
        List<File> result = new ArrayList<File>();
        findRecursivelyForFiles(fileFilter, fileToCopy, result);
        return result;
    }

    /**
     * Recursively search a file matching the fileFilter
     *
     * @param fileFilter  Filter accepting directory and file matching a classname pattern
     * @param currentFile Current directory
     * @return the first found file or null
     */
    private File findRecursivelyForFile(FileFilter fileFilter, File currentFile)
    {
        if (currentFile.isDirectory())
        {
            for (File files : currentFile.listFiles(fileFilter))
            {
                File file = findRecursivelyForFile(fileFilter, files);
                if (file != null)
                {
                    return file;
                }
            }
        }
        else
        {
            return currentFile;
        }
        return null;
    }

    /**
     * Recursively search a all files matching the fileFilter
     *
     * @param fileFilter  Filter accepting directory and file matching a classname pattern
     * @param currentFile Current directory
     * @return the first found file or null
     */
    private void findRecursivelyForFiles(FileFilter fileFilter, File currentFile, List<File> result)
    {
        if (currentFile.isDirectory())
        {
            for (File files : currentFile.listFiles(fileFilter))
            {
                result.add(currentFile);
                findRecursivelyForFiles(fileFilter, files, result);
            }
        }
        else
        {
            result.add(currentFile);
        }
    }

    public void merge(ZipOutputStream outputStream)
    {
        List<String> mergeList = getMergeList(outputStream);
        try
        {
            if (mergeList.contains(fileToCopy.getAbsolutePath()))
            {
                return;
            }
            mergeList.add(fileToCopy.getAbsolutePath());
            copyFileToJar(fileToCopy, outputStream);
        }
        catch (IOException e)
        {
            throw new MergeException(e);
        }
    }

    public void merge(java.util.zip.ZipOutputStream outputStream)
    {
        List<String> mergeList = getMergeList(outputStream);
        try
        {
            if (mergeList.contains(fileToCopy.getAbsolutePath()))
            {
                return;
            }
            mergeList.add(fileToCopy.getAbsolutePath());
            copyFileToJar(fileToCopy, outputStream);
        }
        catch (IOException e)
        {
            throw new MergeException(e);
        }
    }

    private void copyFileToJar(File fileToCopy, java.util.zip.ZipOutputStream outputStream) throws IOException
    {
        if (fileToCopy.isDirectory())
        {
            for (File file : fileToCopy.listFiles())
            {
                copyFileToJar(file, outputStream);
            }
        }
        else
        {
            String entryName = resolveName(fileToCopy, this.destination);
            FileInputStream inputStream = new FileInputStream(fileToCopy);
            IoHelper.copyStreamToJar(inputStream, outputStream, entryName, fileToCopy.lastModified());
        }
    }

    private void copyFileToJar(File fileToCopy, ZipOutputStream outputStream) throws IOException
    {
        if (fileToCopy.isDirectory())
        {
            for (File file : fileToCopy.listFiles())
            {
                copyFileToJar(file, outputStream);
            }
        }
        else
        {
            String entryName = resolveName(fileToCopy, this.destination);
            FileInputStream inputStream = new FileInputStream(fileToCopy);
            IoHelper.copyStreamToJar(inputStream, outputStream, entryName, fileToCopy.lastModified());
        }
    }

    private String resolveName(File fileToCopy, String destination)
    {
        if (isFile(destination))
        {
            return destination;
        }
        String path = this.fileToCopy.getAbsolutePath();
        if (destination.equals(""))
        {
            path = this.fileToCopy.getParentFile().getAbsolutePath();
        }
        path = path + '/';
        StringBuilder builder = new StringBuilder();
        builder.append(destination);
        builder.append(fileToCopy.getAbsolutePath().replaceAll(path, ""));
        return builder.toString().replaceAll("//", "/");
    }

    private boolean isFile(String destination)
    {
        if (destination.length() == 0)
        {
            return false;
        }
        if (!destination.contains("/"))
        {
            return true;
        }
        return !destination.endsWith("/");
    }

    @Override
    public String toString()
    {
        return "FileMerge{" +
                "fileToCopy=" + fileToCopy +
                ", destination='" + destination + '\'' +
                '}';
    }
}
