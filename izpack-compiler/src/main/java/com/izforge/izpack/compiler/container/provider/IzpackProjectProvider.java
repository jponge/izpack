package com.izforge.izpack.compiler.container.provider;

import com.izforge.izpack.api.data.binding.IzpackProjectInstaller;
import com.izforge.izpack.api.data.binding.Listener;
import com.izforge.izpack.api.data.binding.OsModel;
import com.thoughtworks.xstream.XStream;
import org.picocontainer.injectors.Provider;

import java.util.Arrays;
import java.util.List;

/**
 * Provider for izpack project
 *
 * @author Anthonin Bonnefoy
 */
public class IzpackProjectProvider implements Provider
{

    public static List<String> TAG_TO_IGNORE = Arrays.asList("info", "guiprefs", "variables",
            "packs", "packaging", "conditions", "installerrequirements", "locale", "resources", "panels", "help", "validator"
            , "actions", "native");

    public static List<String> LISTENER_ATTRIBUTE = Arrays.asList("classname", "stage");
    public static List<String> OS_ATTRIBUTE = Arrays.asList("arch", "jre", "family", "name", "version");


    public IzpackProjectInstaller provide(String installFile)
    {
        IzpackProjectInstaller izpackProjectInstaller;

        XStream xStream = new XStream();


        xStream.alias("installation", IzpackProjectInstaller.class);

        configureListener(xStream);

        for (String tag : TAG_TO_IGNORE)
        {
            xStream.omitField(IzpackProjectInstaller.class, tag);
        }

        izpackProjectInstaller = (IzpackProjectInstaller) xStream.fromXML(ClassLoader.getSystemResourceAsStream(installFile));

        return izpackProjectInstaller;
    }

    private void configureListener(XStream xStream)
    {
        xStream.alias("listener", Listener.class);
        for (String listenerAttribute : LISTENER_ATTRIBUTE)
        {
            xStream.aliasAttribute(Listener.class, listenerAttribute, listenerAttribute);
        }
        for (String osAttribute : OS_ATTRIBUTE)
        {
            xStream.aliasAttribute(OsModel.class, osAttribute, osAttribute);
        }
    }
}
