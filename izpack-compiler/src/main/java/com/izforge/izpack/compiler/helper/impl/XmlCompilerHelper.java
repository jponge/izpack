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

    /**
     * Get a required attribute of an element, ensuring it is an integer. A meaningful error message
     * is generated as a CompilerException if not present or parseable as an int. It is an error for
     * 'element' or 'attribute' to be null.
     *
     * @param element     The element to get the attribute value of
     * @param attribute   The name of the attribute to get
     * @param installFile
     */
    public static int requireIntAttribute(IXMLElement element, String attribute, String installFile)
            throws CompilerException {
        String value = element.getAttribute(attribute);
        if (value == null || value.length() == 0) {
            AssertionHelper.parseError(element, "<" + element.getName() + "> requires attribute '" + attribute
                    + "'", installFile);
        }
        try {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException x) {
            AssertionHelper.parseError(element, "'" + attribute + "' must be an integer", installFile);
        }
        return 0; // never happens
    }

    /**
     * Call getAttribute on an element, producing a meaningful error message if not present, or one
     * of "yes" or "no". It is an error for 'element' or 'attribute' to be null.
     *
     * @param element     The element to get the attribute value of
     * @param attribute   The name of the attribute to get
     * @param installFile
     */
    public boolean requireYesNoAttribute(IXMLElement element, String attribute, String installFile)
            throws CompilerException {
        String value = requireAttribute(element, attribute, installFile);
        if ("yes".equalsIgnoreCase(value)) {
            return true;
        }
        if ("no".equalsIgnoreCase(value)) {
            return false;
        }

        AssertionHelper.parseError(element, "<" + element.getName() + "> invalid attribute '" + attribute
                + "': Expected (yes|no)", installFile);

        return false; // never happens
    }

    /**
     * Call getAttribute on an element, producing a meaningful warning if not "yes" or "no". If the
     * 'element' or 'attribute' are null, the default value is returned.
     *
     * @param element      The element to get the attribute value of
     * @param attribute    The name of the attribute to get
     * @param defaultValue Value returned if attribute not present or invalid
     * @param installFile
     */
    public static boolean validateYesNoAttribute(IXMLElement element, String attribute,
                                                 boolean defaultValue, String installFile) {
        if (element == null) {
            return defaultValue;
        }

        String value = element.getAttribute(attribute, (defaultValue ? "yes" : "no"));
        if ("yes".equalsIgnoreCase(value)) {
            return true;
        }
        if ("no".equalsIgnoreCase(value)) {
            return false;
        }

        // TODO: should this be an error if it's present but "none of the
        // above"?
        AssertionHelper.parseWarn(element, "<" + element.getName() + "> invalid attribute '" + attribute
                + "': Expected (yes|no) if present", installFile);

        return defaultValue;
    }

    /**
     * Call getAttribute on an element, producing a meaningful error message if not present, or
     * empty. It is an error for 'element' or 'attribute' to be null.
     *
     * @param element     The element to get the attribute value of
     * @param attribute   The name of the attribute to get
     * @param installFile
     */
    public static String requireAttribute(IXMLElement element, String attribute, String installFile)
            throws CompilerException {
        String value = element.getAttribute(attribute);
        if (value == null) {
            AssertionHelper.parseError(element, "<" + element.getName() + "> requires attribute '" + attribute
                    + "'", installFile);
        }
        return value;
    }
}
