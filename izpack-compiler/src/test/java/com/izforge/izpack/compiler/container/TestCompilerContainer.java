package com.izforge.izpack.compiler.container;

import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.test.BaseDir;
import com.izforge.izpack.test.InstallFile;

import java.io.File;

/**
 * Container for compilation test
 *
 * @author Anthonin Bonnefoy
 */
public class TestCompilerContainer extends AbstractContainer
{
    private Class klass;

    public TestCompilerContainer(Class klass)
    {
        this.klass = klass;
    }

    public void initBindings() throws Exception
    {
        CompilerContainer compilerContainer = new CompilerContainer();
        compilerContainer.initBindings();
        pico = compilerContainer.getContainer();

        String installFileName = ((InstallFile) klass.getAnnotation(InstallFile.class)).value();
        String baseDirName = ((BaseDir) klass.getAnnotation(BaseDir.class)).value();

        File baseDir = new File(getClass().getClassLoader().getResource(baseDirName).getFile());
        File installerFile = new File(getClass().getClassLoader().getResource(installFileName).getFile());

        File out = new File(baseDir, "out.jar");
        out.delete();
        CompilerData data = new CompilerData(installerFile.getAbsolutePath(), baseDir.getAbsolutePath(), out.getAbsolutePath());
        pico.addConfig("installFile", installerFile.getAbsolutePath());
        pico.addComponent(CompilerData.class, data);
        pico.addComponent(File.class, out);
    }
}
