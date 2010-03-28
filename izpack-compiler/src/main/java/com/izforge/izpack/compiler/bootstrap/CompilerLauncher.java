package com.izforge.izpack.compiler.bootstrap;

import com.izforge.izpack.compiler.CompilerConfig;
import com.izforge.izpack.compiler.container.CompilerContainer;

import java.util.Date;

/**
 * CompilerLauncher class initizaling bindings and launching the compiler
 *
 * @author Anthonin Bonnefoy
 */
public class CompilerLauncher
{

    /**
     * The main method if the compiler is invoked by a command-line call.
     *
     * @param args The arguments passed on the command-line.
     */
    public static void main(String[] args)
    {
        // exit code 1 means: error
        int exitCode = 1;
        try
        {
            CompilerContainer compilerContainer = new CompilerContainer();
            compilerContainer.initBindings();
            compilerContainer.processCompileDataFromArgs(args);


//            CompilerConfig compiler = new CompilerConfig(filename, base, kind, output,compr_format, compr_level, listener, null);
            CompilerConfig compiler = compilerContainer.getComponent(CompilerConfig.class);
            compiler.executeCompiler();

            // Waits
            while (compiler.isAlive())
            {
                Thread.sleep(100);
            }

            if (compiler.wasSuccessful())
            {
                exitCode = 0;
            }

            System.out.println("Build time: " + new Date());
        }
        catch (Exception err)
        {
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


}
