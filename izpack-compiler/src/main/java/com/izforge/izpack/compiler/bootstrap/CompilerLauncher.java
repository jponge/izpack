package com.izforge.izpack.compiler.bootstrap;

import com.izforge.izpack.compiler.Compiler;
import com.izforge.izpack.compiler.CompilerConfig;
import com.izforge.izpack.compiler.container.CompilerContainer;
import com.izforge.izpack.compiler.listener.CmdlinePackagerListener;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Date;

/**
 * CompilerLauncher class initizaling bindings and launching the compiler
 *
 * @author Anthonin Bonnefoy
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

        CompilerContainer compilerContainer = new CompilerContainer();
        compilerContainer.initBindings();
        compilerContainer.processCompileDataFromArgs(args);

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
            // Calls the compiler
            CmdlinePackagerListener listener = new CmdlinePackagerListener();

//            CompilerConfig compiler = new CompilerConfig(filename, base, kind, output,compr_format, compr_level, listener, null);
            CompilerConfig compiler = compilerContainer.getComponent(CompilerConfig.class);
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

}
