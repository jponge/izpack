/*
 * Copyright  2000-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.izforge.izpack.util.file.types;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.file.FileUtils;
import com.izforge.izpack.util.file.PathTokenizer;

import java.io.File;
import java.util.Vector;


/**
 * This object represents a path as used by CLASSPATH or PATH
 * environment variable.
 * <p/>
 * <code>
 * &lt;sometask&gt;<br>
 * &nbsp;&nbsp;&lt;somepath&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;pathelement location="/path/to/file.jar" /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;pathelement path="/path/to/file2.jar:/path/to/class2;/path/to/class3" /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;pathelement location="/path/to/file3.jar" /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;pathelement location="/path/to/file4.jar" /&gt;<br>
 * &nbsp;&nbsp;&lt;/somepath&gt;<br>
 * &lt;/sometask&gt;<br>
 * </code>
 * <p/>
 * The object implemention <code>sometask</code> must provide a method called
 * <code>createSomepath</code> which returns an instance of <code>Path</code>.
 * Nested path definitions are handled by the Path object and must be labeled
 * <code>pathelement</code>.<p>
 * <p/>
 * The path element takes a parameter <code>path</code> which will be parsed
 * and split into single elements. It will usually be used
 * to define a path from an environment variable.
 */

public class Path extends DataType implements Cloneable
{

    private static FileUtils fileUtils = FileUtils.newFileUtils();

    private Vector elements;

//    /** The system classspath as a Path object */
//    public static Path systemClasspath =
//        new Path(/*null, */System.getProperty("java.class.path"));
//
//
//    /**
//     * The system bootclasspath as a Path object.
//     *
//     * @since Ant 1.6.2
//     */
//    public static Path systemBootClasspath =
//        new Path(/*null, */System.getProperty("sun.boot.class.path"));


    /**
     * Helper class, holds the nested <code>&lt;pathelement&gt;</code> values.
     */
    public class PathElement
    {
        private String[] parts;

        public void setLocation(File loc)
        {
            parts = new String[]{translateFile(loc.getAbsolutePath())};
        }

        public void setPath(AutomatedInstallData idata, String path)
        {
            parts = Path.translatePath(idata, path);
        }

        public String[] getParts()
        {
            return parts;
        }
    }

    /**
     * Invoked by IntrospectionHelper for <code>setXXX(Path p)</code>
     * attribute setters.
     *
     * @param project the <CODE>Project</CODE> for this path.
     * @param path    the <CODE>String</CODE> path definition.
     */
    public Path(AutomatedInstallData idata, String path) throws Exception
    {
//        this(p);
        createPathElement().setPath(idata, path);
    }

    /**
     * Construct an empty <CODE>Path</CODE>.
     *
     * @param project the <CODE>Project</CODE> for this path.
     */
    public Path()
    {
//        setProject(project);
        elements = new Vector();
    }

    /**
     * Adds a element definition to the path.
     *
     * @param location the location of the element to add (must not be
     *                 <code>null</code> nor empty.
     */
    public void setLocation(File location) throws Exception
    {
        createPathElement().setLocation(location);
    }


    /**
     * Parses a path definition and creates single PathElements.
     *
     * @param path the <CODE>String</CODE> path definition.
     */
    public void setPath(AutomatedInstallData idata, String path) throws Exception
    {
        createPathElement().setPath(idata, path);
    }

    /**
     * Creates the nested <code>&lt;pathelement&gt;</code> element.
     */
    public PathElement createPathElement() throws Exception
    {
        PathElement pe = new PathElement();
        elements.addElement(pe);
        return pe;
    }

    /**
     * Adds a nested <code>&lt;fileset&gt;</code> element.
     */
    public void addFileset(FileSet fs) throws Exception
    {
        elements.addElement(fs);
        setChecked(false);
    }

    /**
     * Adds a nested <code>&lt;filelist&gt;</code> element.
     */
    public void addFilelist(FileList fl) throws Exception
    {
        elements.addElement(fl);
        setChecked(false);
    }

    /**
     * Adds a nested <code>&lt;dirset&gt;</code> element.
     */
    public void addDirset(DirSet dset) throws Exception
    {
        elements.addElement(dset);
        setChecked(false);
    }

    /**
     * Adds a nested path
     *
     * @since Ant 1.6
     */
    public void add(Path path) throws Exception
    {
        elements.addElement(path);
        setChecked(false);

    }

    /**
     * Creates a nested <code>&lt;path&gt;</code> element.
     */
    public Path createPath() throws Exception
    {
        Path p = new Path();
        elements.addElement(p);
        setChecked(false);
        return p;
    }

    /**
     * Append the contents of the other Path instance to this.
     */
    public void append(Path other) throws Exception
    {
        if (other == null)
        {
            return;
        }
        String[] l = other.list();
        for (int i = 0; i < l.length; i++)
        {
            if (elements.indexOf(l[i]) == -1)
            {
                elements.addElement(l[i]);
            }
        }
    }

    /**
     * Adds the components on the given path which exist to this
     * Path. Components that don't exist, aren't added.
     *
     * @param source - source path whose components are examined for existence
     */
    public void addExisting(Path source) throws Exception
    {
        addExisting(source, false);
    }

    /**
     * Same as addExisting, but support classpath behavior if tryUserDir
     * is true. Classpaths are relative to user dir, not the project base.
     * That used to break jspc test
     *
     * @param source
     * @param tryUserDir
     */
    public void addExisting(Path source, boolean tryUserDir) throws Exception
    {
        String[] list = source.list();
        File userDir = (tryUserDir) ? new File(System.getProperty("user.dir"))
                : null;

        for (int i = 0; i < list.length; i++)
        {
            File f = null;
//            if (getProject() != null) {
//                f = getProject().resolveFile(list[i]);
//            } else {
            f = new File(list[i]);
//            }
            // probably not the best choice, but it solves the problem of
            // relative paths in CLASSPATH
            if (tryUserDir && !f.exists())
            {
                f = new File(userDir, list[i]);
            }
            if (f.exists())
            {
                setLocation(f);
            }
            else
            {
                Debug.log("dropping " + f + " from path as it doesn't exist"/*,
                    Project.MSG_VERBOSE*/);
            }
        }
    }

    /**
     * Returns all path elements defined by this and nested path objects.
     *
     * @return list of path elements.
     */
    public String[] list() throws Exception
    {
//        if (!isChecked()) {
//            // make sure we don't have a circular reference here
//            Stack stk = new Stack();
//            stk.push(this);
//            dieOnCircularReference(stk, getProject());
//        }

        Vector result = new Vector(2 * elements.size());
        for (int i = 0; i < elements.size(); i++)
        {
            Object o = elements.elementAt(i);
//            if (o instanceof Reference) {
//                Reference r = (Reference) o;
//                o = r.getReferencedObject(getProject());
//                // we only support references to paths right now
//                if (!(o instanceof Path)) {
//                    String msg = r.getRefId() + " doesn\'t denote a path " + o;
//                    throw new Exception(msg);
//                }
//            }

            if (o instanceof String)
            {
                // obtained via append
                addUnlessPresent(result, (String) o);
            }
            else if (o instanceof PathElement)
            {
                String[] parts = ((PathElement) o).getParts();
                if (parts == null)
                {
                    throw new Exception("You must either set location or"
                            + " path on <pathelement>");
                }
                for (int j = 0; j < parts.length; j++)
                {
                    addUnlessPresent(result, parts[j]);
                }
            }
            else if (o instanceof Path)
            {
                Path p = (Path) o;
//                if (p.getProject() == null) {
//                    p.setProject(getProject());
//                }
                String[] parts = p.list();
                for (int j = 0; j < parts.length; j++)
                {
                    addUnlessPresent(result, parts[j]);
                }
            }
            else if (o instanceof DirSet)
            {
                DirSet dset = (DirSet) o;
                addUnlessPresent(result, dset.getDir(/*getProject()*/),
                        dset.getDirectoryScanner(/*getProject()*/).getIncludedDirectories());
            }
            else if (o instanceof FileSet)
            {
                FileSet fs = (FileSet) o;
                addUnlessPresent(result, fs.getDir(/*getProject()*/),
                        fs.getDirectoryScanner(/*getProject()*/).getIncludedFiles());
            }
            else if (o instanceof FileList)
            {
                FileList fl = (FileList) o;
                addUnlessPresent(result,
                        fl.getDir(/*getProject()*/), fl.getFiles(/*getProject()*/));
            }
        }
        String[] res = new String[result.size()];
        result.copyInto(res);
        return res;
    }


    /**
     * Returns a textual representation of the path, which can be used as
     * CLASSPATH or PATH environment variable definition.
     *
     * @return a textual representation of the path.
     */
    public String toString()
    {
        try
        {
            final String[] list = list();

            // empty path return empty string
            if (list.length == 0)
            {
                return "";
            }

            // path containing one or more elements
            final StringBuffer result = new StringBuffer(list[0].toString());
            for (int i = 1; i < list.length; i++)
            {
                result.append(File.pathSeparatorChar);
                result.append(list[i]);
            }

            return result.toString();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Splits a PATH (with : or ; as separators) into its parts.
     */
    public static String[] translatePath(AutomatedInstallData idata, String source)
    {
        final Vector result = new Vector();
        if (source == null)
        {
            return new String[0];
        }

        PathTokenizer tok = new PathTokenizer(source);
        StringBuffer element = new StringBuffer();
        while (tok.hasMoreTokens())
        {
            String pathElement = tok.nextToken();
            try
            {
                element.append(resolveFile(idata, pathElement));
            }
            catch (Exception e)
            {
                Debug.log("Dropping path element " + pathElement
                        + " as it is not valid relative to the project"/*,
                    Project.MSG_VERBOSE*/);
            }
            for (int i = 0; i < element.length(); i++)
            {
                translateFileSep(element, i);
            }
            result.addElement(element.toString());
            element = new StringBuffer();
        }
        String[] res = new String[result.size()];
        result.copyInto(res);
        return res;
    }

    /**
     * Returns its argument with all file separator characters
     * replaced so that they match the local OS conventions.
     */
    public static String translateFile(String source)
    {
        if (source == null)
        {
            return "";
        }

        final StringBuffer result = new StringBuffer(source);
        for (int i = 0; i < result.length(); i++)
        {
            translateFileSep(result, i);
        }

        return result.toString();
    }

    /**
     * Translates all occurrences of / or \ to correct separator of the
     * current platform and returns whether it had to do any
     * replacements.
     */
    protected static boolean translateFileSep(StringBuffer buffer, int pos)
    {
        if (buffer.charAt(pos) == '/' || buffer.charAt(pos) == '\\')
        {
            buffer.setCharAt(pos, File.separatorChar);
            return true;
        }
        return false;
    }

    /**
     * How many parts does this Path instance consist of.
     */
    public int size() throws Exception
    {
        return list().length;
    }

    /**
     * Return a Path that holds the same elements as this instance.
     */
    public Object clone()
    {
        try
        {
            Path p = (Path) super.clone();
            p.elements = (Vector) elements.clone();
            return p;
        }
        catch (CloneNotSupportedException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Resolve a filename with Project's help - if we know one that is.
     * <p/>
     * <p>Assume the filename is absolute if project is null.</p>
     */
    private static String resolveFile(AutomatedInstallData idata, String relativeName)
            throws Exception
    {
        File f = fileUtils.resolveFile(new File(idata.getInstallPath()), relativeName);
        return f.getAbsolutePath();
    }

    /**
     * Adds a String to the Vector if it isn't already included.
     */
    private static void addUnlessPresent(Vector v, String s)
    {
        if (v.indexOf(s) == -1)
        {
            v.addElement(s);
        }
    }

    /**
     * Adds absolute path names of listed files in the given directory
     * to the Vector if they are not already included.
     */
    private static void addUnlessPresent(Vector v, File dir, String[] s)
    {
        for (int j = 0; j < s.length; j++)
        {
            File d = new File(dir, s[j]);
            String absolutePath = d.getAbsolutePath();
            addUnlessPresent(v, translateFile(absolutePath));
        }
    }

//    /**
//     * Concatenates the system class path in the order specified by
//     * the ${build.sysclasspath} property - using &quot;last&quot; as
//     * default value.
//     */
//    public Path concatSystemClasspath() {
//        return concatSystemClasspath("last");
//    }

//    /**
//     * Concatenates the system class path in the order specified by
//     * the ${build.sysclasspath} property - using the supplied value
//     * if ${build.sysclasspath} has not been set.
//     */
//    public Path concatSystemClasspath(String defValue) {
//
//        Path result = new Path(/*getProject()*/);
//
//        String order = defValue;
//        if (getProject() != null) {
//            String o = getProject().getProperty("build.sysclasspath");
//            if (o != null) {
//                order = o;
//            }
//        }
//
//        if (order.equals("only")) {
//            // only: the developer knows what (s)he is doing
//            result.addExisting(Path.systemClasspath, true);
//
//        } else if (order.equals("first")) {
//            // first: developer could use a little help
//            result.addExisting(Path.systemClasspath, true);
//            result.addExisting(this);
//
//        } else if (order.equals("ignore")) {
//            // ignore: don't trust anyone
//            result.addExisting(this);
//
//        } else {
//            // last: don't trust the developer
//            if (!order.equals("last")) {
//                log("invalid value for build.sysclasspath: " + order,
//                    Project.MSG_WARN);
//            }
//
//            result.addExisting(this);
//            result.addExisting(Path.systemClasspath, true);
//        }
//
//
//        return result;
//
//    }

//    /**
//     * Add the Java Runtime classes to this Path instance.
//     */
//    public void addJavaRuntime() {
//        if ("Kaffe".equals(System.getProperty("java.vm.name"))) {
//            // newer versions of Kaffe (1.1.1+) won't have this,
//            // but this will be sorted by FileSet anyway.
//            File kaffeShare = new File(System.getProperty("java.home")
//                                       + File.separator + "share"
//                                       + File.separator + "kaffe");
//            if (kaffeShare.isDirectory()) {
//                FileSet kaffeJarFiles = new FileSet();
//                kaffeJarFiles.setDir(kaffeShare);
//                kaffeJarFiles.setIncludes("*.jar");
//                addFileset(kaffeJarFiles);
//            }
//        } else if ("GNU libgcj".equals(System.getProperty("java.vm.name"))) {
//            addExisting(systemBootClasspath);
//        }
//
//        if (System.getProperty("java.vendor").toLowerCase(Locale.US).indexOf("microsoft") >= 0) {
//            // Pull in *.zip from packages directory
//            FileSet msZipFiles = new FileSet();
//            msZipFiles.setDir(new File(System.getProperty("java.home")
//                + File.separator + "Packages"));
//            msZipFiles.setIncludes("*.ZIP");
//            addFileset(msZipFiles);
//        } else if (JavaEnvUtils.isJavaVersion(JavaEnvUtils.JAVA_1_1)) {
//            addExisting(new Path(null,
//                                 System.getProperty("java.home")
//                                 + File.separator + "lib"
//                                 + File.separator
//                                 + "classes.zip"));
//        } else {
//            // JDK > 1.1 seems to set java.home to the JRE directory.
//            addExisting(new Path(null,
//                                 System.getProperty("java.home")
//                                 + File.separator + "lib"
//                                 + File.separator + "rt.jar"));
//            // Just keep the old version as well and let addExisting
//            // sort it out.
//            addExisting(new Path(null,
//                                 System.getProperty("java.home")
//                                 + File.separator + "jre"
//                                 + File.separator + "lib"
//                                 + File.separator + "rt.jar"));
//
//            // Sun's and Apple's 1.4 have JCE and JSSE in separate jars.
//            String[] secJars = {"jce", "jsse"};
//            for (int i = 0; i < secJars.length; i++) {
//                addExisting(new Path(null,
//                                     System.getProperty("java.home")
//                                     + File.separator + "lib"
//                                     + File.separator + secJars[i] + ".jar"));
//                addExisting(new Path(null,
//                                     System.getProperty("java.home")
//                                     + File.separator + ".."
//                                     + File.separator + "Classes"
//                                     + File.separator + secJars[i] + ".jar"));
//            }
//
//            // IBM's 1.4 has rt.jar split into 4 smaller jars and a combined
//            // JCE/JSSE in security.jar.
//            String[] ibmJars
//                = {"core", "graphics", "security", "server", "xml"};
//            for (int i = 0; i < ibmJars.length; i++) {
//                addExisting(new Path(null,
//                                     System.getProperty("java.home")
//                                     + File.separator + "lib"
//                                     + File.separator + ibmJars[i] + ".jar"));
//            }
//
//            // Added for MacOS X
//            addExisting(new Path(null,
//                                 System.getProperty("java.home")
//                                 + File.separator + ".."
//                                 + File.separator + "Classes"
//                                 + File.separator + "classes.jar"));
//            addExisting(new Path(null,
//                                 System.getProperty("java.home")
//                                 + File.separator + ".."
//                                 + File.separator + "Classes"
//                                 + File.separator + "ui.jar"));
//        }
//    }
//
//    /**
//     * Emulation of extdirs feature in java >= 1.2.
//     * This method adds all files in the given
//     * directories (but not in sub-directories!) to the classpath,
//     * so that you don't have to specify them all one by one.
//     * @param extdirs - Path to append files to
//     */
//    public void addExtdirs(Path extdirs) {
//        if (extdirs == null) {
//            String extProp = System.getProperty("java.ext.dirs");
//            if (extProp != null) {
//                extdirs = new Path(getProject(), extProp);
//            } else {
//                return;
//            }
//        }
//
//        String[] dirs = extdirs.list();
//        for (int i = 0; i < dirs.length; i++) {
//            File dir = getProject().resolveFile(dirs[i]);
//            if (dir.exists() && dir.isDirectory()) {
//                FileSet fs = new FileSet();
//                fs.setDir(dir);
//                fs.setIncludes("*");
//                addFileset(fs);
//            }
//        }
//    }
}
