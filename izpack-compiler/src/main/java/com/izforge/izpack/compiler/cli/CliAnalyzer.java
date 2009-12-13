package com.izforge.izpack.compiler.cli;

import org.apache.commons.cli.Options;


/**
 * Parse and analyze cli
 */
public class CliAnalyzer {

    public Options getOptions() {
        Options options = new Options();
        options.addOption("a", "all", false, "do not hide entries starting with .");
        options.addOption("A", "almost-all", false, "do not list implied . and ..");
        return options;
    }
}
