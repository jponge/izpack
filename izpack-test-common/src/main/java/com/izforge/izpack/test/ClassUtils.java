package com.izforge.izpack.test;

import sun.misc.URLClassPath;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

/**
 * Utils methods for tests purpose
 *
 * @author Anthonin Bonnefoy
 */
public class ClassUtils
{
    public static void unloadLastJar()
            throws NoSuchFieldException, IllegalAccessException
    {
        URLClassLoader systemClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Field ucpField = URLClassLoader.class.getDeclaredField("ucp");
        ucpField.setAccessible(true);
        URLClassPath ucp = (URLClassPath) ucpField.get(systemClassLoader);
        Field pathField = URLClassPath.class.getDeclaredField("path");
        pathField.setAccessible(true);
        ArrayList<URL> path = (ArrayList) pathField.get(ucp);
        path.remove(path.size() - 1);

        Field loaderField = URLClassPath.class.getDeclaredField("loaders");
        loaderField.setAccessible(true);
        ArrayList loaders = (ArrayList) loaderField.get(ucp);
        loaders.remove(loaders.size() - 1);
    }

    public static void loadJarInSystemClassLoader(File out) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, MalformedURLException
    {
        URLClassLoader systemClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Method declaredMethod = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
        declaredMethod.setAccessible(true);
        declaredMethod.invoke(systemClassLoader, out.toURI().toURL());
    }
}
