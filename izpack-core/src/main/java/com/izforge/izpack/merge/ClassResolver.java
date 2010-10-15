package com.izforge.izpack.merge;

import com.izforge.izpack.api.exception.MergeException;
import com.izforge.izpack.merge.resolve.ResolveUtils;
import com.izforge.izpack.util.FileUtil;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

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
        String absolutePath = ResolveUtils.convertPathToPosixPath(file.getAbsolutePath());
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

    public static String processFileToClassName(File file, Package aPackage)
    {
        String absolutePath = ResolveUtils.convertPathToPosixPath(file.getAbsolutePath());
        String packagePath = convertPackageToPath(aPackage.getName());
        if (ClassResolver.isFilePathContainingPackage(file.getAbsolutePath(), aPackage))
        {
            return absolutePath.substring(absolutePath.lastIndexOf(packagePath)).replaceAll("\\.class", "").replaceAll("/", ".");
        }
        throw new MergeException("The file " + file + " does not contains the package " + aPackage);
    }

    public static String processURLToClassName(URL url)
    {
        return processFileToClassName(FileUtil.convertUrlToFile(url));
    }

    public static boolean isFullClassName(String className)
    {
        return className.contains(".");
    }

    public static String getLastPackagePart(String packageName)
    {
        String[] packages = packageName.split("\\.");
        return packages[packages.length - 1];
    }

    public static String convertPackageToPath(String packageName)
    {
        return packageName.replaceAll("\\.", "/");
    }

    public static boolean isFilePathContainingPackage(String filePath, Package aPackage)
    {
        return filePath.contains(aPackage.getName().replaceAll("\\.", "/"));
    }

    public static boolean isFilePathInsidePackageSet(String filePath, Set<Package> packageSet)
    {
        for (Package aPackage : packageSet)
        {
            if (filePath.contains(aPackage.getName().replaceAll("\\.", "/")))
            {
                return true;
            }
        }
        return false;
    }

    public static boolean isUrlContainingPackage(URL url, String aPackage)
    {
        return FileUtil.convertUrlToFilePath(url).contains(aPackage.replaceAll("\\.", "/"));
    }
}
