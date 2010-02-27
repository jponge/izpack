package com.izforge.izpack.compiler.helper;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.exception.CompilerException;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Helper methods for compiler
 *
 * @author Anthonin Bonnefoy
 */
public class XmlCompilerHelper
{

    private final String installFile;
    private AssertionHelper assertionHelper;

    public XmlCompilerHelper(String installFile, AssertionHelper assertionHelper)
    {
        this.installFile = installFile;
        this.assertionHelper = assertionHelper;
    }

    /**
     * Call getContent on an element, producing a meaningful error message if not present, or empty.
     * It is an error for 'element' to be null.
     *
     * @param element The element to get content of
     */
    public String requireContent(IXMLElement element) throws CompilerException
    {
        String content = element.getContent();
        if (content == null || content.length() == 0)
        {
            assertionHelper.parseError(element, "<" + element.getName() + "> requires content");
        }
        return content;
    }

    /**
     * Call getContent on an element, producing a meaningful error message if not present, or empty,
     * or a valid URL. It is an error for 'element' to be null.
     *
     * @param element The element to get content of
     */
    public URL requireURLContent(IXMLElement element) throws CompilerException
    {
        URL url = null;
        try
        {
            url = new URL(requireContent(element));
        }
        catch (MalformedURLException x)
        {
            assertionHelper.parseError(element, "<" + element.getName() + "> requires valid URL", x);
        }
        return url;
    }

    /**
     * Call getFirstChildNamed on the parent, producing a meaningful error message on failure. It is
     * an error for 'parent' to be null.
     *
     * @param parent The element to search for a child
     * @param name   Name of the child element to get
     */
    public IXMLElement requireChildNamed(IXMLElement parent, String name) throws CompilerException
    {
        IXMLElement child = parent.getFirstChildNamed(name);
        if (child == null)
        {
            assertionHelper.parseError(parent, "<" + parent.getName() + "> requires child <" + name + ">");
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
    public int requireIntAttribute(IXMLElement element, String attribute, String installFile)
            throws CompilerException
    {
        String value = element.getAttribute(attribute);
        if (value == null || value.length() == 0)
        {
            assertionHelper.parseError(element, "<" + element.getName() + "> requires attribute '" + attribute
                    + "'");
        }
        try
        {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException x)
        {
            assertionHelper.parseError(element, "'" + attribute + "' must be an integer");
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
            throws CompilerException
    {
        String value = requireAttribute(element, attribute, installFile);
        if ("yes".equalsIgnoreCase(value))
        {
            return true;
        }
        if ("no".equalsIgnoreCase(value))
        {
            return false;
        }

        assertionHelper.parseError(element, "<" + element.getName() + "> invalid attribute '" + attribute
                + "': Expected (yes|no)");

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
    public boolean validateYesNoAttribute(IXMLElement element, String attribute,
                                          boolean defaultValue, String installFile)
    {
        if (element == null)
        {
            return defaultValue;
        }

        String value = element.getAttribute(attribute, (defaultValue ? "yes" : "no"));
        if ("yes".equalsIgnoreCase(value))
        {
            return true;
        }
        if ("no".equalsIgnoreCase(value))
        {
            return false;
        }

        // TODO: should this be an error if it's present but "none of the
        // above"?
        assertionHelper.parseWarn(element, "<" + element.getName() + "> invalid attribute '" + attribute
                + "': Expected (yes|no) if present");

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
    public String requireAttribute(IXMLElement element, String attribute, String installFile)
            throws CompilerException
    {
        String value = element.getAttribute(attribute);
        if (value == null)
        {
            assertionHelper.parseError(element, "<" + element.getName() + "> requires attribute '" + attribute
                    + "'");
        }
        return value;
    }
}
