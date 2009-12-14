package com.izforge.izpack.compiler.cli;

import com.izforge.izpack.compiler.data.CompilerData;
import org.apache.commons.cli.*;

import java.util.List;


/**
 * Parse and analyze cli
 */
public class CliAnalyzer {

    /**
     * Get options for the command line parser
     *
     * @return Options
     */
    private Options getOptions() {
        Options options = new Options();
        options.addOption("h", true, "IzPack home : the root path of IzPack. This will be needed if the compiler " +
                "is not called in the root directory of IzPack." +
                "Do not forget quotations if there are blanks in the path.");
        options.addOption("b", true, "base : indicates the base path that the compiler will use for filenames."
                + " of sources. Default is the current path. Attend to -h.");
        options.addOption("k", true, "kind : indicates the kind of installer to generate, default is standard");
        options.addOption("o", true, "out  : indicates the output file name default is the xml file name\n");
        options.addOption("c", true, "compression : indicates the compression format to be used for packs " +
                "default is the internal deflate compression\n");
        options.addOption("l", true, "compression-level)  : indicates the level for the used compression format"
                + " if supported. Only integer are valid\n");
        return options;
    }

    /**
     * Process args and print information
     *
     * @param args Command line arguments
     * @return Compile data with informations
     */
    public CompilerData processArgs(String[] args) {
        printHelp();
        CompilerData result = parseArgs(args);
        printTail(result);
        return result;
    }

    /**
     * Print the result of parse analysis
     *
     * @param result Compile data created from arguments
     */
    private void printTail(CompilerData result) {
        // Outputs what we are going to do
        System.out.println("-> Processing  : " + result.getFilename());
        System.out.println("-> Output      : " + result.getOutput());
        System.out.println("-> Base path   : " + result.getBasedir());
        System.out.println("-> Kind        : " + result.getKind());
        System.out.println("-> Compression : " + result.getCompr_format());
        System.out.println("-> Compr. level: " + result.getCompr_level());
        System.out.println("-> IzPack home : " + CompilerData.IZPACK_HOME);
        System.out.println("");
    }


    public CompilerData parseArgs(String[] args) {

        CommandLineParser parser = new PosixParser();
        CompilerData compilerData = new CompilerData();
        try {
            CommandLine commandLine = parser.parse(getOptions(), args);
            return analyzeCommandLine(commandLine);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return compilerData;
    }

    /**
     * Print help
     */
    private void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        String cmdLineUsage = "IzPack -> Command line parameters are : (xml file) [args]";
        String header = "(xml file): the xml file describing the installation";
        String footer = "When using vm option -DSTACKTRACE=true there is all kind of debug info ";
        formatter.printHelp(cmdLineUsage, header, getOptions(), footer);
    }

    private CompilerData analyzeCommandLine(CommandLine commandLine) {
        validateCommandLine(commandLine);
        CompilerData compilerData = new CompilerData();

        if (commandLine.hasOption("?")) {
            printHelp();
            throw new RuntimeException("Helped requested, compiler stop");
        }
        List argList = commandLine.getArgList();
        if (argList.size() == 1) {
            compilerData.setFilename((String) argList.get(0));
        }


        return compilerData;
    }

    private void validateCommandLine(CommandLine commandLine) {
        if (commandLine.getArgList().size() < 1) {
            throw new RuntimeException("no arguments given");
        }
    }

//    public void orig() {
//        String filename;
//        String base = ".";
//        String kind = "standard";
//        String output;
//        String compr_format = "default";
//        int compr_level = -1;

    // We can parse the other parameters & try to compile the
    // installation

    // We get the input file name and we initialize the output file
    // name
//        filename = args[0];
//        // default jar files names are based on input file name
//        output = filename.substring(0, filename.length() - 3) + "jar";
//
//        // We parse the other ones
//        int pos = 1;
//        while (pos < nArgs) {
//            if ((args[pos].startsWith("-")) && (args[pos].length() == 2)) {
//                switch (args[pos].toLowerCase().charAt(1)) {
//                    case 'b':
//                        if ((pos + 1) < nArgs) {
//                            pos++;
//                            base = args[pos];
//                        } else {
//                            throw new Exception("base argument missing");
//                        }
//                        break;
//                    case 'k':
//                        if ((pos + 1) < nArgs) {
//                            pos++;
//                            kind = args[pos];
//                        } else {
//                            throw new Exception("kind argument missing");
//                        }
//                        break;
//                    case 'o':
//                        if ((pos + 1) < nArgs) {
//                            pos++;
//                            output = args[pos];
//                        } else {
//                            throw new Exception("output argument missing");
//                        }
//                        break;
//                    case 'c':
//                        if ((pos + 1) < nArgs) {
//                            pos++;
//                            compr_format = args[pos];
//                        } else {
//                            throw new Exception("compression format argument missing");
//                        }
//                        break;
//                    case 'l':
//                        if ((pos + 1) < nArgs) {
//                            pos++;
//                            compr_level = Integer.parseInt(args[pos]);
//                        } else {
//                            throw new Exception("compression level argument missing");
//                        }
//                        break;
//                    case 'h':
//                        if ((pos + 1) < nArgs) {
//                            pos++;
//                            home = args[pos];
//                        } else {
//                            throw new Exception("IzPack home path argument missing");
//                        }
//                        break;
//                    default:
//                        throw new Exception("unknown argument");
//                }
//                pos++;
//            } else {
//                throw new Exception("bad argument");
//            }
//        }
//
//        home = resolveIzPackHome(home);
//
//
//        CompilerData.setIzpackHome(home);
}
