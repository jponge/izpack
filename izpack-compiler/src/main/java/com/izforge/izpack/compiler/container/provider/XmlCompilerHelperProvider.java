package com.izforge.izpack.compiler.container.provider;

import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.helper.XmlCompilerHelper;
import org.picocontainer.injectors.Provider;

/**
 * Provide xmlCompilerHelper
 *
 * @author Anthonin Bonnefoy
 */
public class XmlCompilerHelperProvider implements Provider
{

    public XmlCompilerHelper provide(CompilerData compilerData)
    {
        return new XmlCompilerHelper(compilerData.getInstallFile());
    }
}
