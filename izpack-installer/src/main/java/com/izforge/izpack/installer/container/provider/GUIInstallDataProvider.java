package com.izforge.izpack.installer.container.provider;

import com.izforge.izpack.api.container.BindeableContainer;
import com.izforge.izpack.api.data.GUIPrefs;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.gui.ButtonFactory;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.merge.resolve.ClassPathCrawler;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.OsVersion;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;
import java.awt.*;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * Provide installData for GUI :
 * Load install data with l&f and GUIPrefs
 */
public class GUIInstallDataProvider extends AbstractInstallDataProvider
{

    private static Map<String, String> substanceVariants = new HashMap<String, String>();
    private static Map<String, String> looksVariants = new HashMap<String, String>();

    static
    {
        substanceVariants.put("default", "org.pushingpixels.substance.api.skin.SubstanceBusinessLookAndFeel");
        substanceVariants.put("business", "org.pushingpixels.substance.api.skin.SubstanceBusinessLookAndFeel");
        substanceVariants.put("business-blue", "org.pushingpixels.substance.api.skin.SubstanceBusinessBlueSteelLookAndFeel");
        substanceVariants.put("business-black", "org.pushingpixels.substance.api.skin.SubstanceBusinessBlackSteelLookAndFeel");
        substanceVariants.put("creme", "org.pushingpixels.substance.api.skin.SubstanceCremeLookAndFeel");
        substanceVariants.put("sahara", "org.pushingpixels.substance.api.skin.SubstanceSaharaLookAndFeel");
        substanceVariants.put("moderate", "org.pushingpixels.substance.api.skin.SubstanceModerateLookAndFeel");
        substanceVariants.put("officesilver", "org.pushingpixels.substance.api.skin.SubstanceOfficeSilver2007LookAndFeel");

        looksVariants.put("windows", "com.jgoodies.looks.windows.WindowsLookAndFeel");
        looksVariants.put("plastic", "com.jgoodies.looks.plastic.PlasticLookAndFeel");
        looksVariants.put("plastic3D", "com.jgoodies.looks.plastic.Plastic3DLookAndFeel");
        looksVariants.put("plasticXP", "com.jgoodies.looks.plastic.Plastic3DLookAndFeel");
    }


    public GUIInstallData provide(ResourceManager resourceManager, VariableSubstitutor variableSubstitutor, Properties variables, ClassPathCrawler classPathCrawler, BindeableContainer container) throws Exception
    {
        this.resourceManager = resourceManager;
        this.variableSubstitutor = variableSubstitutor;
        this.classPathCrawler = classPathCrawler;
        final GUIInstallData guiInstallData = new GUIInstallData(variables, variableSubstitutor);
        // Loads the installation data
        loadInstallData(guiInstallData);
        // Load custom action data.
//        loadCustomData(guiInstallData, container, pathResolver);

        loadGUIInstallData(guiInstallData);
        loadInstallerRequirements(guiInstallData);
        loadDynamicVariables(guiInstallData);
        // Load custom langpack if exist.
        addCustomLangpack(guiInstallData);
        loadDefaultLocale(guiInstallData);
        loadLookAndFeel(guiInstallData);
        return guiInstallData;
    }

    /**
     * Loads the suitable L&F.
     *
     * @param installdata
     * @throws Exception Description of the Exception
     */
    protected void loadLookAndFeel(GUIInstallData installdata) throws Exception
    {
        // Do we have any preference for this OS ?
        String syskey = "unix";
        if (OsVersion.IS_WINDOWS)
        {
            syskey = "windows";
        }
        else if (OsVersion.IS_OSX)
        {
            syskey = "mac";
        }
        String lookAndFeelName = null;
        if (installdata.guiPrefs.lookAndFeelMapping.containsKey(syskey))
        {
            lookAndFeelName = installdata.guiPrefs.lookAndFeelMapping.get(syskey);
        }

        // Let's use the system LAF
        // Resolve whether button icons should be used or not.
        boolean useButtonIcons = true;
        if (installdata.guiPrefs.modifier.containsKey("useButtonIcons")
                && "no".equalsIgnoreCase(installdata.guiPrefs.modifier
                .get("useButtonIcons")))
        {
            useButtonIcons = false;
        }
        ButtonFactory.useButtonIcons(useButtonIcons);
        boolean useLabelIcons = true;
        if (installdata.guiPrefs.modifier.containsKey("useLabelIcons")
                && "no".equalsIgnoreCase(installdata.guiPrefs.modifier
                .get("useLabelIcons")))
        {
            useLabelIcons = false;
        }
        LabelFactory.setUseLabelIcons(useLabelIcons);
        if (installdata.guiPrefs.modifier.containsKey("labelFontSize"))
        {  //'labelFontSize' modifier found in 'guiprefs'
            final String valStr =
                    installdata.guiPrefs.modifier.get("labelFontSize");
            try
            {      //parse value and enter as label-font-size multiplier:
                LabelFactory.setLabelFontSize(Float.parseFloat(valStr));
            }
            catch (NumberFormatException ex)
            {      //error parsing value; log message
                Debug.log("Error parsing guiprefs 'labelFontSize' value (" +
                        valStr + ')');
            }
        }

        if (lookAndFeelName == null)
        {
            if (!"mac".equals(syskey))
            {
                // In Linux we will use the English locale, because of a bug in
                // JRE6. In Korean, Persian, Chinese, japanese and some other
                // locales the installer throws and exception and doesn't load
                // at all. See http://jira.jboss.com/jira/browse/JBINSTALL-232.
                // This is a workaround until this bug gets fixed.
                if ("unix".equals(syskey))
                {
                    Locale.setDefault(Locale.ENGLISH);
                }
                String syslaf = UIManager.getSystemLookAndFeelClassName();
                UIManager.setLookAndFeel(syslaf);
                if (UIManager.getLookAndFeel() instanceof MetalLookAndFeel)
                {
                    ButtonFactory.useButtonIcons(useButtonIcons);
                }
            }
            return;
        }

        // Kunststoff (http://www.incors.org/)
        if ("kunststoff".equals(lookAndFeelName))
        {
            ButtonFactory.useHighlightButtons();
            // Reset the use button icons state because useHighlightButtons
            // make it always true.
            ButtonFactory.useButtonIcons(useButtonIcons);
            installdata.buttonsHColor = new Color(255, 255, 255);
            Class<LookAndFeel> lafClass = (Class<LookAndFeel>) Class.forName("com.incors.plaf.kunststoff.KunststoffLookAndFeel");
            Class mtheme = Class.forName("javax.swing.plaf.metal.MetalTheme");
            Class[] params = {mtheme};
            Class<MetalTheme> theme = (Class<MetalTheme>) Class.forName("com.izforge.izpack.gui.IzPackKMetalTheme");
            Method setCurrentThemeMethod = lafClass.getMethod("setCurrentTheme", params);

            // We invoke and place Kunststoff as our L&F
            LookAndFeel kunststoff = lafClass.newInstance();
            MetalTheme ktheme = theme.newInstance();
            Object[] kparams = {ktheme};
            UIManager.setLookAndFeel(kunststoff);
            setCurrentThemeMethod.invoke(kunststoff, kparams);
            return;
        }

        // Liquid (http://liquidlnf.sourceforge.net/)
        if ("liquid".equals(lookAndFeelName))
        {
            UIManager.setLookAndFeel("com.birosoft.liquid.LiquidLookAndFeel");

            Map<String, String> params = installdata.guiPrefs.lookAndFeelParams.get(lookAndFeelName);
            if (params.containsKey("decorate.frames"))
            {
                String value = params.get("decorate.frames");
                if ("yes".equals(value))
                {
                    JFrame.setDefaultLookAndFeelDecorated(true);
                }
            }
            if (params.containsKey("decorate.dialogs"))
            {
                String value = params.get("decorate.dialogs");
                if ("yes".equals(value))
                {
                    JDialog.setDefaultLookAndFeelDecorated(true);
                }
            }

            return;
        }

        // Metouia (http://mlf.sourceforge.net/)
        if ("metouia".equals(lookAndFeelName))
        {
            UIManager.setLookAndFeel("net.sourceforge.mlf.metouia.MetouiaLookAndFeel");
            return;
        }

        // Nimbus (http://nimbus.dev.java.net/)
        if ("nimbus".equals(lookAndFeelName))
        {
            UIManager.setLookAndFeel("org.jdesktop.swingx.plaf.nimbus.NimbusLookAndFeel");
            return;
        }

        // JGoodies Looks (http://looks.dev.java.net/)
        if ("looks".equals(lookAndFeelName))
        {
            String variant = looksVariants.get("plasticXP");

            Map<String, String> params = installdata.guiPrefs.lookAndFeelParams.get(lookAndFeelName);
            if (params.containsKey("variant"))
            {
                String param = params.get("variant");
                if (looksVariants.containsKey(param))
                {
                    variant = looksVariants.get(param);
                }
            }

            UIManager.setLookAndFeel(variant);
            return;
        }

        // Substance (http://substance.dev.java.net/)
        if ("substance".equals(lookAndFeelName))
        {
            final String variant;
            Map<String, String> params = installdata.guiPrefs.lookAndFeelParams.get(lookAndFeelName);
            if (params.containsKey("variant"))
            {
                String param = params.get("variant");
                if (substanceVariants.containsKey(param))
                {
                    variant = substanceVariants.get(param);
                }
                else
                {
                    variant = substanceVariants.get("default");
                }
            }
            else
            {
                variant = substanceVariants.get("default");
            }

            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        UIManager.setLookAndFeel(variant);
                    }
                    catch (Exception e)
                    {
                        throw new IzPackException(e);
                    }
                }
            });

        }
    }

    /**
     * Load GUI preference information.
     *
     * @param installdata
     * @throws Exception
     */
    private void loadGUIInstallData(GUIInstallData installdata) throws Exception
    {
        InputStream in = resourceManager.getInputStream("GUIPrefs");
        ObjectInputStream objIn = new ObjectInputStream(in);
        installdata.guiPrefs = (GUIPrefs) objIn.readObject();
        objIn.close();
    }


}
