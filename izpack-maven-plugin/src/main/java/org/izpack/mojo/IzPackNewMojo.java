package org.izpack.mojo;

import com.izforge.izpack.compiler.CompilerConfig;
import com.izforge.izpack.compiler.container.CompilerContainer;
import com.izforge.izpack.compiler.data.CompilerData;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Mojo for izpack
 *
 * @author Anthonin Bonnefoy
 * @goal compile
 * @phase package
 * @requiresDependencyResolution test
 */
public class IzPackNewMojo extends AbstractMojo {

    /**
     * Format compression. Choices are bzip2, default
     *
     * @parameter default-value="default"
     */
    private String comprFormat;

    /**
     * Kind of installation. Choices are standard (default) or web
     *
     * @parameter default-value="standard"
     */
    private String kind;

    /**
     * Location of the IzPack installation file
     *
     * @parameter default-value="${basedir}/src/main/izpack/install.xml"
     */
    private String installFile;

    /**
     * Base directory of compilation process
     *
     * @parameter default-value="${project.build.directory}/staging"
     */
    private String baseDir;

    /**
     * The output compilation file name
     *
     * @parameter default-value="${project.build.directory}/${project.build.finalName)-izpack.jar"
     */
    private String output;

    /**
     * Compression level of the installation. Desactivated by default (-1)
     *
     * @parameter default-value="-1"
     */
    private int comprLevel;


    public void execute() throws MojoExecutionException, MojoFailureException {
        CompilerData compilerData = initCompilerData();
        CompilerContainer compilerContainer = new CompilerContainer();
        compilerContainer.initBindings();
        compilerContainer.addComponent(CompilerData.class, compilerData);

        CompilerConfig compilerConfig = compilerContainer.getComponent(CompilerConfig.class);
        try {
            compilerConfig.executeCompiler();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private CompilerData initCompilerData() {
        return new CompilerData(comprFormat, kind, installFile, null, baseDir, output, comprLevel);
    }
}
