package com.izforge.izpack.installer.container.provider;

import java.io.InputStream;
import java.net.URL;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.UIManager;

import org.picocontainer.injectors.Provider;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.IXMLParser;
import com.izforge.izpack.api.adaptator.impl.XMLParser;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.gui.IconsDatabase;
import com.izforge.izpack.installer.base.InstallerFrame;

/**
 * Provide icons database
 */
public class IconsProvider implements Provider
{
    private static final Logger logger = Logger.getLogger(IconsProvider.class.getName());

    /**
     * Resource name for custom icons
     */
    private static final String CUSTOM_ICONS_RESOURCEFILE = "customicons.xml";

    public IconsDatabase provide(ResourceManager resourceManager) throws Exception
    {
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
    private void loadIcons(IconsDatabase iconsDatabase) throws Exception
    {
        // Initialisations
        InputStream inXML = getClass().
                getResourceAsStream("icons.xml");

        parseXML(inXML, iconsDatabase);
    }

    /**
     * Loads custom icons into the installer.
     *
     * @throws Exception
     */
    private void loadCustomIcons(IconsDatabase icons, ResourceManager resourceManager) throws Exception
    {
        // We try to load and add a custom langpack.
        InputStream inXML = null;
        try
        {
            inXML = resourceManager.getInputStream(CUSTOM_ICONS_RESOURCEFILE);
        }
        catch (Throwable exception)
        {
            logger.warning("Resource " + CUSTOM_ICONS_RESOURCEFILE
                    + " not defined. No custom icons available");
            return;
        }
        logger.fine("Custom icons available");

        parseXML(inXML, icons);
    }

    /**
     * parse the xml and fill in the db
     *
     * @param inXML
     * @param icons
     */
    private void parseXML(InputStream inXML, IconsDatabase icons)
    {
        URL url;
        ImageIcon img;// Initialises the parser
        IXMLParser parser = new XMLParser();

        // We get the data
        IXMLElement data = parser.parse(inXML);

        // We load the icons
        for (IXMLElement icon : data.getChildrenNamed("icon"))
        {
            url = InstallerFrame.class.getResource(icon.getAttribute("res"));
            img = new ImageIcon(url);
            logger.fine("Icon with id found: " + icon.getAttribute("id"));
            icons.put(icon.getAttribute("id"), img);
        }

        // We load the Swing-specific icons
        for (IXMLElement icon : data.getChildrenNamed("sysicon"))
        {
            url = InstallerFrame.class.getResource(icon.getAttribute("res"));
            img = new ImageIcon(url);
            UIManager.put(icon.getAttribute("id"), img);
        }
    }

}
