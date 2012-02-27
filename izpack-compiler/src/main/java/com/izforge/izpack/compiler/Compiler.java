/*
 * $Id$
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2001 Johannes Lehtinen
 * Copyright 2002 Paul Wilkinson
 * Copyright 2004 Gaganis Giorgos
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.compiler;

import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.PackColor;
import com.izforge.izpack.api.data.binding.OsModel;
import com.izforge.izpack.api.data.binding.Stage;
import com.izforge.izpack.api.exception.CompilerException;
import com.izforge.izpack.api.exception.MergeException;
import com.izforge.izpack.compiler.helper.CompilerHelper;
import com.izforge.izpack.compiler.packager.IPackager;
import com.izforge.izpack.data.CustomData;
import com.izforge.izpack.data.PackInfo;
import com.izforge.izpack.merge.resolve.ClassPathCrawler;
import com.izforge.izpack.util.ClassUtils;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.FileUtil;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The IzPack compiler class. This is now a java bean style class that can be
 * configured using the object representations of the install.xml
 * configuration. The install.xml configuration is now handled by the
 * CompilerConfig class.
 *
 * @author Julien Ponge
 * @author Tino Schwarze
 * @author Chadwick McHenry
 * @see CompilerConfig
 */
public class Compiler extends Thread
{
    /**
     * Collects and packs files into installation jars, as told.
     */
    private IPackager packager;

    /**
     * Error code, set to false if compilation succeeded.
     */
    private boolean compileFailed = true;

    /**
     * Compiler helper.
     */
    private final CompilerHelper compilerHelper;

    /**
     * Classpath crawler.
     */
    private final ClassPathCrawler classPathCrawler;

    /**
     * The paths of resources included in user-specified jars.
     */
    private Set<String> resourcePaths = new HashSet<String>();

    /**
     * The resources by their corresponding jar URL.
     */
    private Map<URL, List<String>> resourcesByJar = new HashMap<URL, List<String>>();

    /**
     * Constructs a <tt>Compiler</tt>.
     *
     * @param compilerHelper   the compiler helper
     * @param packager         the packager
     * @param classPathCrawler the class path crawler
     */
    public Compiler(CompilerHelper compilerHelper, IPackager packager, ClassPathCrawler classPathCrawler)
    {
        this.compilerHelper = compilerHelper;
        this.packager = packager;
        this.classPathCrawler = classPathCrawler;
    }

    /**
     * The run() method.
     */
    public void run()
    {
        try
        {
            createInstaller(); // Execute the compiler - may send info to
            // System.out
        }
        catch (CompilerException ce)
        {
            System.out.println(ce.getMessage() + "\n");
        }
        catch (Exception e)
        {
            if (Debug.stackTracing())
            {
                e.printStackTrace();
            }
            else
            {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    /**
     * Compiles the installation.
     *
     * @throws Exception Description of the Exception
     */
    public void createInstaller() throws Exception
    {
        // We ask the packager to create the installer
        packager.createInstaller();
        this.compileFailed = false;
    }

    /**
     * Returns whether the installation was successful or not.
     *
     * @return whether the installation was successful or not
     */
    public boolean wasSuccessful()
    {
        return !this.compileFailed;
    }

    /**
     * Verifies dependencies between packs.
     *
     * @throws CompilerException if there are circular dependencies between packs, or a dependency doesn't exist
     */
    public void checkDependencies() throws CompilerException
    {
        checkDependencies(packager.getPacksList());
    }

    /**
     * Verifies that no two preselected packs have the same excludeGroup.
     *
     * @throws CompilerException if two preselected packs have the same excludeGroup
     */
    public void checkExcludes() throws CompilerException
    {
        checkExcludes(packager.getPacksList());
    }

    /**
     * Verifies that no two preselected packs have the same excludeGroup.
     *
     * @param packs list of packs which should be checked
     * @throws CompilerException if two preselected packs have the same excludeGroup
     */
    public void checkExcludes(List<PackInfo> packs) throws CompilerException
    {
        for (int q = 0; q < packs.size(); q++)
        {
            PackInfo packinfo1 = packs.get(q);
            Pack pack1 = packinfo1.getPack();
            for (int w = 0; w < q; w++)
            {

                PackInfo packinfo2 = packs.get(w);
                Pack pack2 = packinfo2.getPack();
                if (pack1.excludeGroup != null && pack2.excludeGroup != null)
                {
                    if (pack1.excludeGroup.equals(pack2.excludeGroup))
                    {
                        if (pack1.preselected && pack2.preselected)
                        {
                            error("Packs " + pack1.name + " and " + pack2.name +
                                    " belong to the same excludeGroup " + pack1.excludeGroup +
                                    " and are both preselected. This is not allowed.");
                        }
                    }
                }
            }
        }
    }

    /**
     * Verifies dependencies between packs.
     *
     * @param packs the packs to check
     * @throws CompilerException if there are circular dependencies between packs, or a dependency doesn't exist
     */
    public void checkDependencies(List<PackInfo> packs) throws CompilerException
    {
        // Because we use package names in the configuration file we associate
        // the names with the objects
        Map<String, PackInfo> names = new HashMap<String, PackInfo>();
        for (PackInfo pack : packs)
        {
            names.put(pack.getPack().name, pack);
        }
        int result = dfs(packs, names);
        // @todo More informative messages to include the source of the error
        if (result == -2)
        {
            error("Circular dependency detected");
        }
        else if (result == -1)
        {
            error("A dependency doesn't exist");
        }
    }

    /**
     * We use the dfs graph search algorithm to check whether the graph is acyclic as described in:
     * Thomas H. Cormen, Charles Leiserson, Ronald Rivest and Clifford Stein. Introduction to
     * algorithms 2nd Edition 540-549,MIT Press, 2001
     *
     * @param packs The graph
     * @param names The name map
     * @return -2 if back edges exist, else 0
     */
    private int dfs(List<PackInfo> packs, Map<String, PackInfo> names)
    {
        Map<Edge, PackColor> edges = new HashMap<Edge, PackColor>();
        for (PackInfo pack : packs)
        {
            if (pack.colour == PackColor.WHITE)
            {
                if (dfsVisit(pack, names, edges) != 0)
                {
                    return -1;
                }
            }

        }
        return checkBackEdges(edges);
    }

    /**
     * This function checks for the existence of back edges.
     *
     * @param edges map to be checked
     * @return -2 if back edges exist, else 0
     */
    private int checkBackEdges(Map<Edge, PackColor> edges)
    {
        Set<Edge> keys = edges.keySet();
        for (final Edge key : keys)
        {
            PackColor color = edges.get(key);
            if (color == PackColor.GREY)
            {
                return -2;
            }
        }
        return 0;

    }

    /**
     * This class is used for the classification of the edges
     */
    private class Edge
    {

        PackInfo u;

        PackInfo v;

        Edge(PackInfo u, PackInfo v)
        {
            this.u = u;
            this.v = v;
        }
    }

    private int dfsVisit(PackInfo u, Map<String, PackInfo> names, Map<Edge, PackColor> edges)
    {
        u.colour = PackColor.GREY;
        List<String> deps = u.getDependencies();
        if (deps != null)
        {
            for (String name : deps)
            {
                PackInfo v = names.get(name);
                if (v == null)
                {
                    System.out.println("Failed to find dependency: " + name);
                    return -1;
                }
                Edge edge = new Edge(u, v);
                if (edges.get(edge) == null)
                {
                    edges.put(edge, v.colour);
                }

                if (v.colour == PackColor.WHITE)
                {

                    final int result = dfsVisit(v, names, edges);
                    if (result != 0)
                    {
                        return result;
                    }
                }
            }
        }
        u.colour = PackColor.BLACK;
        return 0;
    }

    /**
     * Raises an exception.
     *
     * @param message a brief error message
     * @throws CompilerException when invoked
     */
    private void error(String message) throws CompilerException
    {
        compileFailed = true;
        throw new CompilerException(message);
    }

    /**
     * Adds a jar.
     *
     * @param url         the JAR url
     * @param uninstaller if <tt>true</tt>, include jar in the uninstaller
     * @throws IOException if the jar cannot be read
     */
    public void addJar(URL url, boolean uninstaller) throws IOException
    {
        List<String> paths = compilerHelper.getContainedFilePaths(url);
        resourcePaths.addAll(paths);
        resourcesByJar.put(url, paths);
        if (uninstaller)
        {
            CustomData data = new CustomData(null, paths, null, CustomData.UNINSTALLER_JAR);
            packager.addCustomJar(data, url);
        }
        else
        {
            packager.addJarContent(url);
        }

        // TODO - not clear why this is required. Would be better if jars were loaded via URLClassLoader
        ClassUtils.loadJarInSystemClassLoader(FileUtil.convertUrlToFile(url));
    }

    /**
     * Adds a listener to be invoked during installation or uninstallation.
     *
     * @param className   the listener class name
     * @param stage       the stage when the listener is invoked
     * @param constraints the list of constraints. May be <tt>null</tt>
     * @throws MergeException if the class cannot be found
     */
    public void addCustomListener(String className, Stage stage, List<OsModel> constraints)
    {
        checkClassName(className);
        int type = (stage == Stage.install) ? CustomData.INSTALLER_LISTENER : CustomData.UNINSTALLER_LISTENER;
        CustomData data = new CustomData(className, null, constraints, type);
        packager.addCustomJar(data, null);
    }

    /**
     * Verifies that the named class exists in the class path.
     *
     * @param className the class name
     * @throws IllegalArgumentException if the class name is not fully qualified
     * @throws MergeException           if the class cannot be found
     */
    private void checkClassName(String className)
    {
        String existing = findClass(className, (URL) null);
        if (!className.equals(existing))
        {
            throw new IllegalArgumentException("Expected fully qualified class name for class: " + existing
                    + ", but got: " + className);
        }
    }

    /**
     * Returns the fully qualified name for a class, verifying that it exists in the class path.
     * <br/>
     * For historical reasons, unqualified class names may be specified. The <tt>url</tt> argument may be used
     * to specify the URL of the jar containing the class, to avoid picking up classes from other jars with the same
     * name.
     *
     * @param className the class name. May be qualified or unqualified
     * @param url       the URL of the jar to examine first. May be <tt>null</tt>
     * @return the fully qualified class name
     * @throws MergeException if the class cannot be found
     */
    public String findClass(String className, URL url)
    {
        String result = null;
        if (url != null)
        {
            List<String> resources = resourcesByJar.get(url);
            if (resources != null)
            {
                result = findClass(className, resources);
            }
        }
        if (result == null)
        {
            result = findClass(className, resourcePaths);
        }
        if (result == null)
        {
            // not present in user-specified jars. Try and find it in the class path
            Class aClass = classPathCrawler.findClass(className);
            result = aClass.getName();
        }
        return result;
    }

    /**
     * Finds a class in a list of resources.
     *
     * @param className the class name. May be unqualified
     * @param resources the resources
     * @return the fully qualified class name, or <tt>null</tt> if it is not found
     */
    private String findClass(String className, Collection<String> resources)
    {
        String result = null;
        boolean qualified = className.indexOf('.') != -1;
        String resource = className.replace('.', '/') + ".class"; // resource path of the class
        int length = resource.length();
        if (resources.contains(resource))
        {
            // exact match
            result = resource;
        }
        else if (!qualified)
        {
            // looking for an unqualified class name
            for (String path : resources)
            {
                int index = path.lastIndexOf(resource);
                if (index > 0 && path.length() == index + length && path.charAt(index - 1) == '/')
                {
                    result = path;
                }
            }
        }
        if (result != null)
        {
            // strip off .class suffix
            result = result.substring(0, result.length() - 6).replace('/', '.');
        }
        return result;
    }
}
