package com.izforge.izpack.compiler.helper;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.exception.CompilerException;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;


public class XmlCompilerHelperTest
{
    private XmlCompilerHelper helper = new XmlCompilerHelper(new AssertionHelper("fake.xml"));

    @Test
    public void testRequireURLContent() throws CompilerException
    {
        IXMLElement webDir = new XMLElementImpl("webdir");

        webDir.setContent("http://some.url/without-spaces");
        assertThat(helper.requireURLContent(webDir).toString(), is("http://some.url/without-spaces"));

        webDir.setContent("http://some.url/with spaces inside");
        assertThat(helper.requireURLContent(webDir).toString(), is("http://some.url/with%20spaces%20inside"));
    }
}
