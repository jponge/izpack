package com.izforge.izpack.merge;

import com.izforge.izpack.api.exception.MergeException;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * Helper methods for class
 *
 * @author Anthonin Bonnefoy
 */
public class ClassResolver
{
    public static final List<String> packageBegin = Arrays.asList("com/", "org/", "net/");

    /**
     * Search for a standard package begin like com/ org/ net/
     *
     * @param file File to process
     * @return Full className
     */
    public static String processFileToClassName(File file)
    {
        String absolutePath = file.getAbsolutePath();
        for (String packageString : packageBegin)
        {
            if (!absolutePath.contains(packageString))
            {
                continue;
            }
            return absolutePath.substring(absolutePath.lastIndexOf(packageString)).replaceAll("\\.class", "").replaceAll("/", ".");
        }
        throw new MergeException("No standard package begin found in " + file.getPath());
    }

    public static String processURLToClassName(URL url)
    {
        return processFileToClassName(new File(url.getFile()));
    }

    public static boolean isFullClassName(String className)
    {
        return className.contains(".");
    }
}
