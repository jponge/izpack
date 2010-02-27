package com.izforge.izpack.compiler.container.provider;

import com.izforge.izpack.compiler.helper.AssertionHelper;
import com.izforge.izpack.compiler.helper.XmlCompilerHelper;
import org.picocontainer.injectors.Provider;

/**
 * Provide xmlCompilerHelper
 *
 * @author Anthonin Bonnefoy
 */
public class XmlCompilerHelperProvider implements Provider
{

    public XmlCompilerHelper provide(String installFile, AssertionHelper assertionHelper)
    {
        return new XmlCompilerHelper(installFile, assertionHelper);
    }
}
