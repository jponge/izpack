package com.izforge.izpack.compiler;

import com.izforge.izpack.compiler.container.CompilerContainer;
import com.izforge.izpack.compiler.data.CompilerData;
import org.hamcrest.core.Is;
import org.testng.annotations.BeforeMethod;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Anthonin Bonnefoy
 */
public abstract class AbstractCompilationTest
{
    private File baseDir = new File(getClass().getClassLoader().getResource("samples").getFile());
    private File installerFile = new File(getClass().getClassLoader().getResource(getInstallFileName()).getFile());

    abstract String getInstallFileName();


    protected File out = new File(baseDir, "out.jar");
    protected CompilerContainer compilerContainer;
    private CompilerData data;

    @BeforeMethod
    public void cleanFiles()
    {
        assertThat(baseDir.exists(), Is.is(true));
        out.delete();
        data = new CompilerData(installerFile.getAbsolutePath(), baseDir.getAbsolutePath(), out.getAbsolutePath());
        compilerContainer = new CompilerContainer();
        compilerContainer.initBindings();
        compilerContainer.addConfig("installFile", installerFile.getAbsolutePath());
        compilerContainer.addComponent(CompilerData.class, data);
    }
}
