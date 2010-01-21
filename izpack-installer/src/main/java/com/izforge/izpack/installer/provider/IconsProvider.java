package com.izforge.izpack.installer.provider;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.IXMLParser;
import com.izforge.izpack.api.adaptator.impl.XMLParser;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.gui.IconsDatabase;
import com.izforge.izpack.installer.base.InstallerFrame;
import com.izforge.izpack.util.Debug;
import org.picocontainer.injectors.Provider;

import javax.swing.*;
import java.io.InputStream;
import java.net.URL;
import java.util.Vector;

/**
 * Provide icons database
 */
public class IconsProvider implements Provider {

    /**
     * Resource name for custom icons
     */
    private static final String CUSTOM_ICONS_RESOURCEFILE = "customicons.xml";

    public IconsDatabase provide(ResourceManager resourceManager) throws Exception {
        IconsDatabase icons = new IconsDatabase();
        loadIcons(icons);
        loadCustomIcons(icons, resourceManager);
        return icons;
    }

    /**
     * Loads the icons.
     *
     * @param iconsDatabase
     * @throws Exception Description of the Exception
     */
    private void loadIcons(IconsDatabase iconsDatabase) throws Exception {
        // Initialisations
        URL url;
        ImageIcon img;
        IXMLElement icon;
        InputStream inXML = InstallerFrame.class
                .getResourceAsStream("/com/izforge/izpack/installer/icons.xml");

        // Initialises the parser
        IXMLParser parser = new XMLParser();

        // We get the data
        IXMLElement data = parser.parse(inXML);

        // We load the icons
        Vector<IXMLElement> children = data.getChildrenNamed("icon");
        int size = children.size();
        for (int i = 0; i < size; i++) {
            icon = children.get(i);
            url = InstallerFrame.class.getResource(icon.getAttribute("res"));
            img = new ImageIcon(url);
            iconsDatabase.put(icon.getAttribute("id"), img);
        }

        // We load the Swing-specific icons
        children = data.getChildrenNamed("sysicon");
        size = children.size();
        for (int i = 0; i < size; i++) {
            icon = children.get(i);
            url = InstallerFrame.class.getResource(icon.getAttribute("res"));
            img = new ImageIcon(url);
            UIManager.put(icon.getAttribute("id"), img);
        }
    }

    /**
     * Loads custom icons into the installer.
     *
     * @throws Exception
     */
    private void loadCustomIcons(IconsDatabase icons, ResourceManager resourceManager) throws Exception {
        // We try to load and add a custom langpack.
        InputStream inXML = null;
        try {
            inXML = resourceManager.getInputStream(CUSTOM_ICONS_RESOURCEFILE);
        }
        catch (Throwable exception) {
            Debug.trace("Resource " + CUSTOM_ICONS_RESOURCEFILE
                    + " not defined. No custom icons available.");
            return;
        }
        Debug.trace("Custom icons available.");
        URL url;
        ImageIcon img;
        IXMLElement icon;

        // Initialises the parser
        IXMLParser parser = new XMLParser();

        // We get the data
        IXMLElement data = parser.parse(inXML);

        // We load the icons
        Vector<IXMLElement> children = data.getChildrenNamed("icon");
        int size = children.size();
        for (int i = 0; i < size; i++) {
            icon = children.get(i);
            url = InstallerFrame.class.getResource(icon.getAttribute("res"));
            img = new ImageIcon(url);
            Debug.trace("Icon with id found: " + icon.getAttribute("id"));
            icons.put(icon.getAttribute("id"), img);
        }

        // We load the Swing-specific icons
        children = data.getChildrenNamed("sysicon");
        size = children.size();
        for (int i = 0; i < size; i++) {
            icon = children.get(i);
            url = InstallerFrame.class.getResource(icon.getAttribute("res"));
            img = new ImageIcon(url);
            UIManager.put(icon.getAttribute("id"), img);
        }
    }
}
