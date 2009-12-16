package com.izforge.izpack.compiler.provider;

import com.izforge.izpack.compiler.cli.CliAnalyzer;
import com.izforge.izpack.compiler.data.CompilerData;
import org.picocontainer.injectors.Provider;

/**
 * Provide CompileData coming from CliAnalyzer
 *
 * @author Anthonin Bonnefoy
 */
public class CompilerDataProvider implements Provider {
    private String[] args;

    public CompilerDataProvider(String[] args) {
        this.args = args;
    }

    public CompilerData provide(CliAnalyzer cliAnalyzer) {
        return cliAnalyzer.printAndParseArgs(args);
    }

}
