package com.izforge.izpack.compiler.bootstrap;

import com.izforge.izpack.compiler.Compiler;
import com.izforge.izpack.compiler.CompilerConfig;
import com.izforge.izpack.compiler.PackagerListener;
import com.izforge.izpack.compiler.data.CompilerData;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Date;

/**
 * CompilerLauncher class initizaling bindings and launching the compiler
 */
public class CompilerLauncher {

    private final static String IZ_TEST_FILE = "ShellLink.dll";

    private final static String IZ_TEST_SUBDIR = "bin" + File.separator + "native" + File.separator
            + "izpack";

    /**
     * The main method if the compiler is invoked by a command-line call.
     *
     * @param args The arguments passed on the command-line.
     */
    public static void main(String[] args) {
        // Outputs some informations
        System.out.println("");
        System.out.println(".::  IzPack - Version " + CompilerData.IZPACK_VERSION + " ::.");
        System.out.println("");
        System.out.println("< compiler specifications version: " + CompilerConfig.VERSION + " >");
        System.out.println("");
        System.out.println("- Copyright (c) 2001-2008 Julien Ponge");
        System.out.println("- Visit http://izpack.org/ for the latest releases");
        System.out
                .println("- Released under the terms of the Apache Software License version 2.0.");
        System.out.println("");

        // exit code 1 means: error
        int exitCode = 1;
        String home = ".";

        // We get the IzPack home directory
        String izHome = System.getProperty("izpack.home");
        if (izHome != null) {
            home = izHome;
        } else {
            izHome = System.getenv("IZPACK_HOME");
            if (izHome != null) {
                home = izHome;
            }
        }

        // We analyse the command line parameters
        try {
            // Our arguments
            String filename;
            String base = ".";
            String kind = "standard";
            String output;
            String compr_format = "default";
            int compr_level = -1;

            // First check
            int nArgs = args.length;
            if (nArgs < 1) {
                throw new Exception("no arguments given");
            }

            // The users wants to know the command line parameters
            if ("-?".equalsIgnoreCase(args[0])) {
                System.out.println("-> Command line parameters are : (xml file) [args]");
                System.out.println("   (xml file): the xml file describing the installation");
                System.out
                        .println("   -h (IzPack home) : the root path of IzPack. This will be needed");
                System.out
                        .println("               if the compiler is not called in the root directory  of IzPack.");
                System.out
                        .println("               Do not forget quotations if there are blanks in the path.");
                System.out
                        .println("   -b (base) : indicates the base path that the compiler will use for filenames");
                System.out
                        .println("               of sources. Default is the current path. Attend to -h.");
                System.out.println("   -k (kind) : indicates the kind of installer to generate");
                System.out.println("               default is standard");
                System.out.println("   -o (out)  : indicates the output file name");
                System.out.println("               default is the xml file name\n");
                System.out
                        .println("   -c (compression)  : indicates the compression format to be used for packs");
                System.out.println("               default is the internal deflate compression\n");
                System.out
                        .println("   -l (compression-level)  : indicates the level for the used compression format");
                System.out.println("                if supported. Only integer are valid\n");

                System.out
                        .println("   When using vm option -DSTACKTRACE=true there is all kind of debug info ");
                System.out.println("");
                exitCode = 0;
            } else {
                // We can parse the other parameters & try to compile the
                // installation

                // We get the input file name and we initialize the output file
                // name
                filename = args[0];
                // default jar files names are based on input file name
                output = filename.substring(0, filename.length() - 3) + "jar";

                // We parse the other ones
                int pos = 1;
                while (pos < nArgs) {
                    if ((args[pos].startsWith("-")) && (args[pos].length() == 2)) {
                        switch (args[pos].toLowerCase().charAt(1)) {
                            case 'b':
                                if ((pos + 1) < nArgs) {
                                    pos++;
                                    base = args[pos];
                                } else {
                                    throw new Exception("base argument missing");
                                }
                                break;
                            case 'k':
                                if ((pos + 1) < nArgs) {
                                    pos++;
                                    kind = args[pos];
                                } else {
                                    throw new Exception("kind argument missing");
                                }
                                break;
                            case 'o':
                                if ((pos + 1) < nArgs) {
                                    pos++;
                                    output = args[pos];
                                } else {
                                    throw new Exception("output argument missing");
                                }
                                break;
                            case 'c':
                                if ((pos + 1) < nArgs) {
                                    pos++;
                                    compr_format = args[pos];
                                } else {
                                    throw new Exception("compression format argument missing");
                                }
                                break;
                            case 'l':
                                if ((pos + 1) < nArgs) {
                                    pos++;
                                    compr_level = Integer.parseInt(args[pos]);
                                } else {
                                    throw new Exception("compression level argument missing");
                                }
                                break;
                            case 'h':
                                if ((pos + 1) < nArgs) {
                                    pos++;
                                    home = args[pos];
                                } else {
                                    throw new Exception("IzPack home path argument missing");
                                }
                                break;
                            default:
                                throw new Exception("unknown argument");
                        }
                        pos++;
                    } else {
                        throw new Exception("bad argument");
                    }
                }

                home = resolveIzPackHome(home);
                // Outputs what we are going to do
                System.out.println("-> Processing  : " + filename);
                System.out.println("-> Output      : " + output);
                System.out.println("-> Base path   : " + base);
                System.out.println("-> Kind        : " + kind);
                System.out.println("-> Compression : " + compr_format);
                System.out.println("-> Compr. level: " + compr_level);
                System.out.println("-> IzPack home : " + home);
                System.out.println("");

                CompilerData.setIzpackHome(home);

                // Calls the compiler
                CmdlinePackagerListener listener = new CmdlinePackagerListener();
                CompilerConfig compiler = new CompilerConfig(filename, base, kind, output,
                        compr_format, compr_level, listener, null);
                compiler.executeCompiler();

                // Waits
                while (compiler.isAlive()) {
                    Thread.sleep(100);
                }

                if (compiler.wasSuccessful()) {
                    exitCode = 0;
                }

                System.out.println("Build time: " + new Date());
            }
        }
        catch (Exception err) {
            // Something bad has happened
            System.err.println("-> Fatal error :");
            System.err.println("   " + err.getMessage());
            err.printStackTrace();
            System.err.println("");
            System.err.println("(tip : use -? to get the commmand line parameters)");
        }

        // Closes the JVM
        System.exit(exitCode);
    }


    private static String resolveIzPackHome(String home) {
        File test = new File(home, IZ_TEST_SUBDIR + File.separator + IZ_TEST_FILE);
        if (test.exists()) {
            return (home);
        }
        // Try to resolve the path using compiler.jar which also should be under
        // IZPACK_HOME.
        String self = Compiler.class.getName();
        self = self.replace('.', '/');
        self = "/" + self + ".class";
        URL url = Compiler.class.getResource(self);
        String np = url.getFile();
        int start = np.indexOf(self);
        np = np.substring(0, start);
        if (np.endsWith("!")) { // Where shut IZPACK_HOME at the standalone-compiler be??
            // No idea.
            if (np.endsWith("standalone-compiler.jar!")
                    || np.endsWith("standalone-compiler-4.0.0.jar!")
                    || np.matches("standalone-compiler-[\\d\\.]+.jar!")) {
                return (".");
            }
            np = np.substring(0, np.length() - 1);
        }
        File root;
        if (URI.create(np).isAbsolute()) {
            root = new File(URI.create(np));
        } else {
            root = new File(np);
        }
        while (true) {
            if (root == null) {
                throw new IllegalArgumentException(
                        "No valid IzPack home directory found");
            }
            test = new File(root, IZ_TEST_SUBDIR + File.separator + IZ_TEST_FILE);
            if (test.exists()) {
                return (root.getAbsolutePath());
            }
            root = root.getParentFile();
        }
    }

    /**
     * Used to handle the packager messages in the command-line mode.
     *
     * @author julien created October 26, 2002
     */
    static class CmdlinePackagerListener implements PackagerListener {

        /**
         * Print a message to the console at default priority (MSG_INFO).
         *
         * @param info The information.
         */
        public void packagerMsg(String info) {
            packagerMsg(info, MSG_INFO);
        }

        /**
         * Print a message to the console at the specified priority.
         *
         * @param info     The information.
         * @param priority priority to be used for the message prefix
         */
        public void packagerMsg(String info, int priority) {
            final String prefix;
            switch (priority) {
                case MSG_DEBUG:
                    prefix = "[ DEBUG ] ";
                    break;
                case MSG_ERR:
                    prefix = "[ ERROR ] ";
                    break;
                case MSG_WARN:
                    prefix = "[ WARNING ] ";
                    break;
                case MSG_INFO:
                case MSG_VERBOSE:
                default: // don't die, but don't prepend anything
                    prefix = "";
            }

            System.out.println(prefix + info);
        }

        /**
         * Called when the packager starts.
         */
        public void packagerStart() {
            System.out.println("[ Begin ]");
            System.out.println();
        }

        /**
         * Called when the packager stops.
         */
        public void packagerStop() {
            System.out.println();
            System.out.println("[ End ]");
        }
    }

}
