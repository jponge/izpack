package com.izforge.izpack.compiler.container;

import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.test.InstallFile;
import org.junit.rules.TemporaryFolder;
import org.picocontainer.MutablePicoContainer;

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

    public void fillContainer(MutablePicoContainer pico) throws Exception
    {
        String installFileName = ((InstallFile) klass.getAnnotation(InstallFile.class)).value();

        File baseDir = new TemporaryFolder().newFolder("baseDirTemp");
        File installerFile = new File(getClass().getClassLoader().getResource(installFileName).getFile());

        File out = new File(baseDir, "out.jar");
        out.delete();
        CompilerData data = new CompilerData(installerFile.getAbsolutePath(), baseDir.getAbsolutePath(), out.getAbsolutePath());
        pico.addConfig("installFile", installerFile.getAbsolutePath());
        pico.addComponent(CompilerData.class, data);
        pico.addComponent(File.class, out);
    }

}
