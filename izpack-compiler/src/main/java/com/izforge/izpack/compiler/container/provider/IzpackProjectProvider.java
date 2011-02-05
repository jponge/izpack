package com.izforge.izpack.compiler.container.provider;

import com.izforge.izpack.api.adaptator.IXMLParser;
import com.izforge.izpack.api.adaptator.XMLException;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.data.binding.*;
import com.thoughtworks.xstream.XStream;
import org.picocontainer.injectors.Provider;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.*;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provider for izpack project
 *
 * @author Anthonin Bonnefoy
 */
public class IzpackProjectProvider implements Provider
{

    public static List<String> TAG_TO_IGNORE = Arrays.asList("info", "guiprefs", "variables",
            "packs", "packaging", "conditions", "installerrequirements", "locale", "resources",
            "panels", "help", "validator", "actions", "natives", "dynamicvariables", "jar");

    public static Map<String, String> PANEL_ATTRIBUTE = new HashMap<String, String>();

    static
    {
        // Xml <-> Java field
        PANEL_ATTRIBUTE.put("classname", "className");
        PANEL_ATTRIBUTE.put("id", "panelid");
        PANEL_ATTRIBUTE.put("condition", "condition");
    }

    public static List<String> LISTENER_ATTRIBUTE = Arrays.asList("classname", "stage", "jar");
    public static List<String> OS_ATTRIBUTE = Arrays.asList("arch", "jre", "family", "name", "version");
    public static List<String> HELP_ATTRIBUTE = Arrays.asList("iso3", "src");


    public IzpackProjectInstaller provide(String installFile) throws IOException
    {
        IzpackProjectInstaller izpackProjectInstaller;
        XStream xStream = new XStream();
        xStream.alias("installation", IzpackProjectInstaller.class);
        configureListener(xStream);
        configurePanels(xStream);
        for (String tag : TAG_TO_IGNORE)
        {
            xStream.omitField(IzpackProjectInstaller.class, tag);
        }

        URL resource = ClassLoader.getSystemResource(installFile);
        InputStream inputStream;
        if (resource != null)
        {
            inputStream = resource.openStream();
        }
        else
        {
            inputStream = new FileInputStream(new File(installFile));
        }

        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);

        try
        {
            InputSource inputSource = new InputSource(inputStream);

            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            saxParserFactory.setNamespaceAware(true);
            saxParserFactory.setXIncludeAware(true);
            XMLReader xmlReader = saxParserFactory.newSAXParser().getXMLReader();
            XMLFilterImpl filter = new XMLFilterImpl(xmlReader);

            SAXSource source = new SAXSource(inputSource);
            source.setXMLReader(filter);
            URL xslResourceUrl = IXMLParser.class.getResource(IXMLParser.XSL_FILE_NAME);
            if (xslResourceUrl == null)
            {
                throw new XMLException("Can't find IzPack internal file \"" + IXMLParser.XSL_FILE_NAME + "\"");
            }
            Source xsltSource = new StreamSource(xslResourceUrl.openStream());
            Transformer xformer;
            xformer = TransformerFactory.newInstance().newTransformer(xsltSource);
            xformer.transform(source, result);

            System.out.println(result.getWriter());

        }
        catch (TransformerConfigurationException e)
        {
            throw new IOException(e);
        }
        catch (SAXException e)
        {
            throw new IOException(e);
        }
        catch (ParserConfigurationException e)
        {
            throw new IOException(e);
        }
        catch (TransformerException e)
        {
            throw new IOException(e);
        }

        izpackProjectInstaller = (IzpackProjectInstaller) xStream.fromXML(writer.toString());
        izpackProjectInstaller.fillWithDefault();
        return izpackProjectInstaller;
    }

    private void configurePanels(XStream xStream)
    {
        xStream.alias("panel", Panel.class);
        xStream.alias("action", Action.class);
        for (Map.Entry<String, String> attributeEntry : PANEL_ATTRIBUTE.entrySet())
        {
            xStream.aliasAttribute(Panel.class, attributeEntry.getValue(), attributeEntry.getKey());
        }
        // Implicit collection for os list in panel
        xStream.addImplicitCollection(Panel.class, "osConstraints", "os", OsModel.class);
        xStream.addImplicitCollection(Panel.class, "helps", "help", Help.class);
        for (String helpAttribute : HELP_ATTRIBUTE)
        {
            xStream.aliasAttribute(Help.class, helpAttribute, helpAttribute);
        }
        for (String osAttribute : OS_ATTRIBUTE)
        {
            xStream.aliasAttribute(OsModel.class, osAttribute, osAttribute);
        }
    }

    private void configureListener(XStream xStream)
    {
        xStream.alias("listener", Listener.class);
        for (String panelAttribute : LISTENER_ATTRIBUTE)
        {
            xStream.aliasAttribute(Listener.class, panelAttribute, panelAttribute);
        }
        // Implicit collection for os list in listener
        xStream.addImplicitCollection(Listener.class, "os", OsModel.class);
        for (String osAttribute : OS_ATTRIBUTE)
        {
            xStream.aliasAttribute(OsModel.class, osAttribute, osAttribute);
        }
    }


}
