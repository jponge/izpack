package com.izforge.izpack.compiler.container.provider;

import com.izforge.izpack.compiler.cli.CliAnalyzer;
import com.izforge.izpack.compiler.container.CompilerContainer;
import com.izforge.izpack.compiler.data.CompilerData;
import org.apache.commons.cli.ParseException;
import org.picocontainer.injectors.Provider;

/**
 * Provide CompileData coming from CliAnalyzer
 *
 * @author Anthonin Bonnefoy
 */
public class CompilerDataProvider implements Provider
{
    private String[] args;

    public CompilerDataProvider(String[] args)
    {
        this.args = args;
    }

    public CompilerData provide(CliAnalyzer cliAnalyzer, CompilerContainer compilerContainer) throws ParseException
    {
        CompilerData compilerData = cliAnalyzer.printAndParseArgs(args);
        compilerContainer.addConfig("installFile", compilerData.getInstallFile());
        // REFACTOR : find a way to test with a fake home
        // compilerData.resolveIzpackHome();
        return compilerData;
    }

}
