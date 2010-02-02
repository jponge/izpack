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
import com.izforge.izpack.api.exception.CompilerException;
import com.izforge.izpack.api.substitutor.SubstitutionType;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.data.PropertyManager;
import com.izforge.izpack.compiler.helper.AssertionHelper;
import com.izforge.izpack.compiler.helper.CompilerHelper;
import com.izforge.izpack.compiler.packager.IPackager;
import com.izforge.izpack.data.CustomData;
import com.izforge.izpack.data.PackInfo;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.OsConstraint;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;

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
public class Compiler extends Thread {
    /**
     * Collects and packs files into installation jars, as told.
     */
    private IPackager packager;

    /**
     * Error code, set to true if compilation succeeded.
     */
    private boolean compileFailed = true;

    private CompilerHelper compilerHelper;
    /**
     * Replaces the properties in the install.xml file prior to compiling
     */
    private VariableSubstitutor propertySubstitutor;

    public PropertyManager propertyManager;

    /**
     * The constructor.
     *
     * @throws CompilerException
     */
    public Compiler(VariableSubstitutor variableSubstitutor, PropertyManager propertyManager, CompilerHelper compilerHelper, IPackager packager) throws CompilerException {
        this.propertyManager = propertyManager;
        this.propertySubstitutor = variableSubstitutor;
        this.compilerHelper = compilerHelper;
        this.packager = packager;
        // add izpack built in property
    }

    /**
     * The run() method.
     */
    public void run() {
        try {
            createInstaller(); // Execute the compiler - may send info to
            // System.out
        }
        catch (CompilerException ce) {
            System.out.println(ce.getMessage() + "\n");
        }
        catch (Exception e) {
            if (Debug.stackTracing()) {
                e.printStackTrace();
            } else {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    /**
     * Compiles the installation.
     *
     * @throws Exception Description of the Exception
     */
    public void createInstaller() throws Exception {
        // We ask the packager to create the installer
        packager.createInstaller();
        this.compileFailed = false;
    }

    /**
     * Returns whether the installation was successful or not.
     *
     * @return whether the installation was successful or not
     */
    public boolean wasSuccessful() {
        return !this.compileFailed;
    }

    /**
     * Checks whether the dependencies stated in the configuration file are correct. Specifically it
     * checks that no pack point to a non existent pack and also that there are no circular
     * dependencies in the packs.
     *
     * @throws CompilerException
     */
    public void checkDependencies() throws CompilerException {
        checkDependencies(packager.getPacksList());
    }

    /**
     * Checks whether the excluded packs exist. (simply calles the other function)
     *
     * @throws CompilerException
     */
    public void checkExcludes() throws CompilerException {
        checkExcludes(packager.getPacksList());
    }

    /**
     * This checks if there are more than one preselected packs per excludeGroup.
     *
     * @param packs list of packs which should be checked
     * @throws CompilerException
     */
    public void checkExcludes(List<PackInfo> packs) throws CompilerException {
        for (int q = 0; q < packs.size(); q++) {
            PackInfo packinfo1 = packs.get(q);
            Pack pack1 = packinfo1.getPack();
            for (int w = 0; w < q; w++) {

                PackInfo packinfo2 = packs.get(w);
                Pack pack2 = packinfo2.getPack();
                if (pack1.excludeGroup != null && pack2.excludeGroup != null) {
                    if (pack1.excludeGroup.equals(pack2.excludeGroup)) {
                        if (pack1.preselected && pack2.preselected) {
                            parseError("Packs " + pack1.name + " and " + pack2.name +
                                    " belong to the same excludeGroup " + pack1.excludeGroup +
                                    " and are both preselected. This is not allowed.");
                        }
                    }
                }
            }

        }
    }

    /**
     * Checks whether the dependencies among the given Packs. Specifically it
     * checks that no pack point to a non existent pack and also that there are no circular
     * dependencies in the packs.
     *
     * @param packs - List<Pack> representing the packs in the installation
     * @throws CompilerException
     */
    public void checkDependencies(List<PackInfo> packs) throws CompilerException {
        // Because we use package names in the configuration file we assosiate
        // the names with the objects
        Map<String, PackInfo> names = new HashMap<String, PackInfo>();
        for (PackInfo pack : packs) {
            names.put(pack.getPack().name, pack);
        }
        int result = dfs(packs, names);
        // @todo More informative messages to include the source of the error
        if (result == -2) {
            parseError("Circular dependency detected");
        } else if (result == -1) {
            parseError("A dependency doesn't exist");
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
    private int dfs(List<PackInfo> packs, Map<String, PackInfo> names) {
        Map<Edge, PackColor> edges = new HashMap<Edge, PackColor>();
        for (PackInfo pack : packs) {
            if (pack.colour == PackColor.WHITE) {
                if (dfsVisit(pack, names, edges) != 0) {
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
    private int checkBackEdges(Map<Edge, PackColor> edges) {
        Set<Edge> keys = edges.keySet();
        for (final Edge key : keys) {
            PackColor color = edges.get(key);
            if (color == PackColor.GREY) {
                return -2;
            }
        }
        return 0;

    }

    /**
     * This class is used for the classification of the edges
     */
    private class Edge {

        PackInfo u;

        PackInfo v;

        Edge(PackInfo u, PackInfo v) {
            this.u = u;
            this.v = v;
        }
    }

    private int dfsVisit(PackInfo u, Map<String, PackInfo> names, Map<Edge, PackColor> edges) {
        u.colour = PackColor.GREY;
        List<String> deps = u.getDependencies();
        if (deps != null) {
            for (String name : deps) {
                PackInfo v = names.get(name);
                if (v == null) {
                    System.out.println("Failed to find dependency: " + name);
                    return -1;
                }
                Edge edge = new Edge(u, v);
                if (edges.get(edge) == null) {
                    edges.put(edge, v.colour);
                }

                if (v.colour == PackColor.WHITE) {

                    final int result = dfsVisit(v, names, edges);
                    if (result != 0) {
                        return result;
                    }
                }
            }
        }
        u.colour = PackColor.BLACK;
        return 0;
    }

    public URL findIzPackResource(String path, String desc) throws CompilerException {
        return findIzPackResource(path, desc, false);
    }

    /**
     * Look for an IzPack resource either in the compiler jar, or within IZPACK_HOME. The path must
     * not be absolute. The path must use '/' as the fileSeparator (it's used to access the jar
     * file). If the resource is not found, take appropriate action base on ignoreWhenNotFound flag.
     *
     * @param path               the relative path (using '/' as separator) to the resource.
     * @param desc               the description of the resource used to report errors
     * @param ignoreWhenNotFound when false, throws a CompilerException indicate
     *                           fault in the parent element when resource not found.
     * @return a URL to the resource.
     * @throws CompilerException
     */
    public URL findIzPackResource(String path, String desc, boolean ignoreWhenNotFound)
            throws CompilerException {
        URL url = getClass().getResource("/" + path);
        if (url == null) {
            File resource = new File(path);
            if (!resource.isAbsolute()) {
                resource = new File(CompilerData.IZPACK_HOME, path);
            }

            if (!resource.exists()) {
                if (ignoreWhenNotFound) {
                    AssertionHelper.parseWarn(desc + " not found: " + resource);
                } else {
                    parseError(desc + " not found: " + resource); // fatal
                }
            } else {
                try {
                    url = resource.toURI().toURL();
                }
                catch (MalformedURLException how) {
                    parseError(desc + "(" + resource + ")", how);
                }
            }
        }

        return url;
    }

    /**
     * Create parse error with consistent messages. Includes file name. For use When parent is
     * unknown.
     *
     * @param message Brief message explaining error
     * @throws CompilerException
     */
    public void parseError(String message) throws CompilerException {
        this.compileFailed = true;
        throw new CompilerException(message);
    }

    /**
     * Create parse error with consistent messages. Includes file name. For use When parent is
     * unknown.
     *
     * @param message Brief message explaining error
     * @param how     throwable which was catched
     * @throws CompilerException
     */
    public void parseError(String message, Throwable how) throws CompilerException {
        this.compileFailed = true;
        throw new CompilerException(message, how);
    }

    // -------------------------------------------------------------------------
    // ------------- Listener stuff ------------------------- START ------------

    /**
     * This method parses install.xml for defined listeners and put them in the right position. If
     * posible, the listeners will be validated. Listener declaration is a fragmention in
     * install.xml like : &lt;listeners&gt; &lt;listener compiler="PermissionCompilerListener"
     * installer="PermissionInstallerListener"/1gt; &lt;/listeners&gt;
     *
     * @param type        The listener type.
     * @param className   The class name.
     * @param jarPath     The jar path.
     * @param constraints The list of constraints.
     * @throws Exception Thrown in case an error occurs.
     */
    public void addCustomListener(int type, String className, String jarPath, List<OsConstraint> constraints) throws Exception {
        jarPath = propertySubstitutor.substitute(jarPath, SubstitutionType.TYPE_AT);
        String fullClassName = className;
        List<String> filePaths = null;

        URL url = findIzPackResource(jarPath, "CustomAction jar file", true);

        if (url != null) {
            fullClassName = getFullClassName(url, className);
            if (fullClassName == null) {
                throw new CompilerException("CustomListener class '" + className + "' not found in '"
                        + url + "'. The class and listener name must match");
            }
            filePaths = compilerHelper.getContainedFilePaths(url);
        }

        CustomData ca = new CustomData(fullClassName, filePaths, constraints, type);
        packager.addCustomJar(ca, url);
    }

    /**
     * Returns the qualified class name for the given class. This method expects as the url param a
     * jar file which contains the given class. It scans the zip entries of the jar file.
     *
     * @param url       url of the jar file which contains the class
     * @param className short name of the class for which the full name should be resolved
     * @return full qualified class name
     * @throws Exception
     */
    private String getFullClassName(URL url, String className) throws Exception {
        JarInputStream jis = new JarInputStream(url.openStream());
        ZipEntry zentry;
        while ((zentry = jis.getNextEntry()) != null) {
            String name = zentry.getName();
            int lastPos = name.lastIndexOf(".class");
            if (lastPos < 0) {
                continue; // No class file.
            }
            name = name.replace('/', '.');
            int pos;
            if (className != null) {
                pos = name.indexOf(className);
                if (pos >= 0 && name.length() == pos + className.length() + 6) // "Main" class
                // found
                {
                    jis.close();
                    return (name.substring(0, lastPos));
                }
            }
        }
        jis.close();
        return (null);
    }

    // -------------------------------------------------------------------------
    // ------------- Listener stuff ------------------------- END ------------

}
