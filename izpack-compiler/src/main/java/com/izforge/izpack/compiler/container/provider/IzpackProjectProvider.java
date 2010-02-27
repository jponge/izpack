package com.izforge.izpack.compiler.container.provider;

import com.izforge.izpack.api.data.binding.IzpackProjectInstaller;
import com.izforge.izpack.api.data.binding.Listener;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.enums.EnumConverter;
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

    public IzpackProjectInstaller provide(String installFile)
    {
        IzpackProjectInstaller izpackProjectInstaller;

        XStream xStream = new XStream();

        xStream.alias("installation", IzpackProjectInstaller.class);
        xStream.alias("listener", Listener.class);

        xStream.aliasAttribute(Listener.class, "classname", "classname");
        xStream.aliasAttribute(Listener.class, "stage", "stage");
        xStream.registerConverter(new EnumConverter());

        for (String tag : TAG_TO_IGNORE)
        {
            xStream.omitField(IzpackProjectInstaller.class, tag);
        }

        izpackProjectInstaller = (IzpackProjectInstaller) xStream.fromXML(ClassLoader.getSystemResourceAsStream(installFile));

        return izpackProjectInstaller;
    }
}
