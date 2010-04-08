package com.izforge.izpack.compiler.container.provider;

import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.data.binding.Help;
import com.izforge.izpack.api.data.binding.IzpackProjectInstaller;
import com.izforge.izpack.api.data.binding.Listener;
import com.izforge.izpack.api.data.binding.OsModel;
import com.thoughtworks.xstream.XStream;
import org.picocontainer.injectors.Provider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
            "packs", "packaging", "conditions", "installerrequirements", "locale", "resources", "panels", "help", "validator"
            , "actions", "native", "dynamicvariables");

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
        izpackProjectInstaller = (IzpackProjectInstaller) xStream.fromXML(inputStream);
        izpackProjectInstaller.fillWithDefault();
        return izpackProjectInstaller;
    }

    private void configurePanels(XStream xStream)
    {
        xStream.alias("panel", Panel.class);
        for (Map.Entry<String, String> attributeEntry : PANEL_ATTRIBUTE.entrySet())
        {
            xStream.aliasAttribute(Panel.class, attributeEntry.getValue(), attributeEntry.getKey());
        }
        // Implicit collection for os list in panel
        xStream.addImplicitCollection(Panel.class, "osConstraints", "os", OsModel.class);
        xStream.addImplicitCollection(Panel.class, "helps", "help", Help.class);
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
