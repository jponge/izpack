package com.izforge.izpack.compiler.helper.impl;

import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.compiler.CompilerConfig;
import com.izforge.izpack.compiler.CompilerException;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.helper.AssertionHelper;
import com.izforge.izpack.compiler.helper.IXmlCompilerHelper;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Anthonin Bonnefoy
 */
public class XmlCompilerHelper implements IXmlCompilerHelper {

    private CompilerData compilerData;

    public XmlCompilerHelper(CompilerData compilerData) {
        this.compilerData = compilerData;
    }

    /**
     * Call getContent on an element, producing a meaningful error message if not present, or empty.
     * It is an error for 'element' to be null.
     *
     * @param element        The element to get content of
     * @param compilerConfig
     */
    public String requireContent(IXMLElement element, CompilerConfig compilerConfig) throws CompilerException {
        String content = element.getContent();
        if (content == null || content.length() == 0) {
            AssertionHelper.parseError(element, "<" + element.getName() + "> requires content", compilerData.getInstallFile());
        }
        return content;
    }

    /**
     * Call getContent on an element, producing a meaningful error message if not present, or empty,
     * or a valid URL. It is an error for 'element' to be null.
     *
     * @param element        The element to get content of
     * @param compilerConfig
     */
    public URL requireURLContent(IXMLElement element, CompilerConfig compilerConfig) throws CompilerException {
        URL url = null;
        try {
            url = new URL(requireContent(element, compilerConfig));
        }
        catch (MalformedURLException x) {
            AssertionHelper.parseError(element, "<" + element.getName() + "> requires valid URL", x, compilerData.getInstallFile());
        }
        return url;
    }

    /**
     * Call getFirstChildNamed on the parent, producing a meaningful error message on failure. It is
     * an error for 'parent' to be null.
     *
     * @param parent         The element to search for a child
     * @param name           Name of the child element to get
     * @param compilerConfig
     */
    public IXMLElement requireChildNamed(IXMLElement parent, String name, CompilerConfig compilerConfig) throws CompilerException {
        IXMLElement child = parent.getFirstChildNamed(name);
        if (child == null) {
            AssertionHelper.parseError(parent, "<" + parent.getName() + "> requires child <" + name + ">", compilerData.getInstallFile());
        }
        return child;
    }
}
