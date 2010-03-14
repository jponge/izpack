package com.izforge.izpack.installer.language;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.data.GUIInstallData;
import com.izforge.izpack.api.data.LocaleDatabase;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.api.installer.InstallerRequirementDisplay;
import com.izforge.izpack.util.Debug;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.TreeMap;

/**
 * Used to prompt the user for the language. Languages can be displayed in iso3 or the native
 * notation or the notation of the default locale. Revising to native notation is based on code
 * from Christian Murphy (patch #395).
 *
 * @author Julien Ponge
 * @author Christian Murphy
 * @author Klaus Bartz
 */
public class LanguageDialog extends JDialog implements ActionListener, InstallerRequirementDisplay
{

    private static final long serialVersionUID = 3256443616359887667L;

    /**
     * The combo box.
     */
    private JComboBox comboBox;

    /**
     * The ISO3 to ISO2 HashMap
     */
    private HashMap<String, String> iso3Toiso2 = null;

    /**
     * iso3Toiso2 expanded ?
     */
    private boolean isoMapExpanded = false;
    /**
     * holds language to ISO-3 language code translation
     */
    private static HashMap isoTable;
    private static final String[][] LANG_CODES = {{"cat", "ca"}, {"chn", "zh"}, {"cze", "cs"},
            {"dan", "da"}, {"deu", "de"}, {"eng", "en"}, {"fin", "fi"}, {"fra", "fr"},
            {"hun", "hu"}, {"ita", "it"}, {"jpn", "ja"}, {"mys", "ms"}, {"ned", "nl"},
            {"nor", "no"}, {"pol", "pl"}, {"por", "pt"}, {"rom", "or"}, {"rus", "ru"},
            {"spa", "es"}, {"svk", "sk"}, {"swe", "sv"}, {"tur", "tr"}, {"ukr", "uk"}};
    private GUIInstallData installdata;
    /**
     * defined modifier for language display type.
     */
    private static final String[] LANGUAGE_DISPLAY_TYPES = {"iso3", "native", "default"};
    private ResourceManager resourceManager;
    private JFrame frame;
    private ConditionCheck conditionCheck;


    /**
     * The constructor.
     *
     * @param installDataGUI
     * @param conditionCheck
     */
    public LanguageDialog(JFrame frame, ResourceManager resourceManager, GUIInstallData installDataGUI, ConditionCheck conditionCheck) throws Exception
    {
        super(frame);
        this.frame = frame;
        this.resourceManager = resourceManager;
        this.installdata = installDataGUI;
        this.conditionCheck = conditionCheck;
        this.setName(GuiId.DIALOG_PICKER.id);
        // We build the GUI
        addWindowListener(new WindowHandler());
        JPanel contentPane = (JPanel) getContentPane();
        setTitle("Language Selection");
        GridBagLayout layout = new GridBagLayout();
        contentPane.setLayout(layout);
        GridBagConstraints gbConstraints = new GridBagConstraints();
        gbConstraints.anchor = GridBagConstraints.CENTER;
        gbConstraints.insets = new Insets(5, 5, 5, 5);
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        gbConstraints.gridx = 0;
        gbConstraints.weightx = 1.0;
        gbConstraints.weighty = 1.0;
        gbConstraints.ipadx = 0;
        gbConstraints.ipady = 6;

        ImageIcon img = getImage();
        JLabel imgLabel = new JLabel(img);
        gbConstraints.gridy = 0;
        contentPane.add(imgLabel);

        String firstMessage = "Please select your language";
        if (getLangType().equals(LANGUAGE_DISPLAY_TYPES[0]))
        // iso3
        {
            firstMessage = "Please select your language below";
        }

        JLabel label1 = new JLabel(firstMessage, SwingConstants.LEADING);
        gbConstraints.gridy = 1;
        gbConstraints.insets = new Insets(15, 5, 5, 5);
        layout.addLayoutComponent(label1, gbConstraints);
        contentPane.add(label1);

        gbConstraints.insets = new Insets(5, 5, 5, 5);
        Object[] listAvaibleLangPacks = resourceManager.getAvailableLangPacks().toArray();
        listAvaibleLangPacks = reviseItems(listAvaibleLangPacks);

        comboBox = new JComboBox(listAvaibleLangPacks);
        comboBox.setName(GuiId.COMBO_BOX_LANG_FLAG.id);
        if (useFlags())
        {
            comboBox.setRenderer(new FlagRenderer(resourceManager));
        }
        gbConstraints.gridy = 3;
        layout.addLayoutComponent(comboBox, gbConstraints);
        contentPane.add(comboBox);

        gbConstraints.insets = new Insets(15, 5, 15, 5);
        JButton okButton = new JButton("OK");
        okButton.setName(GuiId.BUTTON_LANG_OK.id);
        okButton.addActionListener(this);
        gbConstraints.fill = GridBagConstraints.NONE;
        gbConstraints.gridy = 4;
        gbConstraints.anchor = GridBagConstraints.CENTER;
        layout.addLayoutComponent(okButton, gbConstraints);
        contentPane.add(okButton);
        getRootPane().setDefaultButton(okButton);

        // Packs and centers
        // Fix for bug "Installer won't show anything on OSX"
        if (System.getProperty("mrj.version") == null)
        {
            pack();
        }
        setSize(getPreferredSize());

        Dimension frameSize = getSize();
        Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        setLocation(center.x - frameSize.width / 2, center.y - frameSize.height / 2 - 10);
        setResizable(true);
        this.setSelection(Locale.getDefault().getISO3Language().toLowerCase());
        this.setModal(true);
        this.toFront();
    }

    /**
     * Revises iso3 language items depending on the language display type.
     *
     * @param items item array to be revised
     * @return the revised array
     */
    private Object[] reviseItems(Object[] items)
    {
        String langType = getLangType();
        // iso3: nothing todo.
        if (langType.equals(LANGUAGE_DISPLAY_TYPES[0]))
        {
            return (items);
        }
        // native: get the names as they are written in that language.
        if (langType.equals(LANGUAGE_DISPLAY_TYPES[1]))
        {
            return (expandItems(items, (new JComboBox()).getFont()));
        }
        // default: get the names as they are written in the default
        // language.
        if (langType.equals(LANGUAGE_DISPLAY_TYPES[2]))
        {
            return (expandItems(items, null));
        }
        // Should never be.
        return (items);
    }

    /**
     * Expands the given iso3 codes to language names. If a testFont is given, the codes are
     * tested whether they can displayed or not. If not, or no font given, the language name
     * will be returned as written in the default language of this VM.
     *
     * @param items    item array to be expanded to the language name
     * @param testFont font to test wheter a name is displayable
     * @return aray of expanded items
     */
    private Object[] expandItems(Object[] items, Font testFont)
    {
        int i;
        if (iso3Toiso2 == null)
        { // Loasd predefined langs into HashMap.
            iso3Toiso2 = new HashMap<String, String>(32);
            isoTable = new HashMap();
            for (i = 0; i < LANG_CODES.length; ++i)
            {
                iso3Toiso2.put(LANG_CODES[i][0], LANG_CODES[i][1]);
            }
        }
        for (i = 0; i < items.length; i++)
        {
            Object it = expandItem(items[i], testFont);
            isoTable.put(it, items[i]);
            items[i] = it;
        }
        return items;
    }

    /**
     * Expands the given iso3 code to a language name. If a testFont is given, the code will be
     * tested whether it is displayable or not. If not, or no font given, the language name will
     * be returned as written in the default language of this VM.
     *
     * @param item     item to be expanded to the language name
     * @param testFont font to test wheter the name is displayable
     * @return expanded item
     */
    private Object expandItem(Object item, Font testFont)
    {
        Object iso2Str = iso3Toiso2.get(item);
        int i;
        if (iso2Str == null && !isoMapExpanded)
        { // Expand iso3toiso2 only if needed because it needs some time.
            isoMapExpanded = true;
            Locale[] loc = Locale.getAvailableLocales();
            for (i = 0; i < loc.length; ++i)
            {
                iso3Toiso2.put(loc[i].getISO3Language(), loc[i].getLanguage());
            }
            iso2Str = iso3Toiso2.get(item);
        }
        if (iso2Str == null)
        // Unknown item, return it self.
        {
            return (item);
        }
        Locale locale = new Locale((String) iso2Str);
        if (testFont == null)
        // Return the language name in the spelling of the default locale.
        {
            return (locale.getDisplayLanguage());
        }
        // Get the language name in the spelling of that language.
        String str = locale.getDisplayLanguage(locale);
        int cdut = testFont.canDisplayUpTo(str);
        if (cdut > -1)
        // Test font cannot render it;
        // use language name in the spelling of the default locale.
        {
            str = locale.getDisplayLanguage();
        }
        return (str);
    }

    /**
     * Loads an image.
     *
     * @return The image icon.
     */
    public ImageIcon getImage()
    {
        ImageIcon img;
        try
        {
            img = resourceManager.getImageIconResource("installer.langsel.img");
        }
        catch (Exception err)
        {
            img = null;
        }
        return img;
    }

    /**
     * Gets the selected object.
     *
     * @return The selected item.
     */
    public Object getSelection()
    {
        Object retval = null;
        if (isoTable != null)
        {
            retval = isoTable.get(comboBox.getSelectedItem());
        }
        return (retval != null) ? retval : comboBox.getSelectedItem();
    }

    /**
     * Sets the selection.
     *
     * @param item The item to be selected.
     */
    public void setSelection(Object item)
    {
        Object mapped = null;
        if (isoTable != null)
        {
            for (Object key : isoTable.keySet())
            {
                if (isoTable.get(key).equals(item))
                {
                    mapped = key;
                    break;
                }
            }
        }
        if (mapped == null)
        {
            mapped = item;
        }
        comboBox.setSelectedItem(mapped);
    }

    /**
     * Closer.
     *
     * @param e The event.
     */
    public void actionPerformed(ActionEvent e)
    {
        String selectedPack = (String) this.getSelection();
        if (selectedPack == null)
        {
            throw new RuntimeException("installation canceled");
        }
        try
        {
            propagateLocale(selectedPack);
            // Configure buttons after locale has been loaded
            installdata.configureGuiButtons();
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
        }
        dispose();
    }

    public void runPicker()
    {
        // frame.setVisible(true);
        frame.setVisible(false);
        this.setVisible(true);
    }

    /**
     * The window events handler.
     *
     * @author Julien Ponge
     */
    private class WindowHandler extends WindowAdapter
    {

        /**
         * We can't avoid the exit here, so don't call exit anywhere else.
         *
         * @param e the event.
         */
        public void windowClosing(WindowEvent e)
        {
            System.exit(0);
        }
    }

    /**
     * A list cell renderer that adds the flags on the display.
     *
     * @author Julien Ponge
     */
    private static class FlagRenderer extends JLabel implements ListCellRenderer
    {

        private static final long serialVersionUID = 3832899961942782769L;

        /**
         * Icons cache.
         */
        private TreeMap<String, ImageIcon> icons = new TreeMap<String, ImageIcon>();

        /**
         * Grayed icons cache.
         */
        private TreeMap<String, ImageIcon> grayIcons = new TreeMap<String, ImageIcon>();

        private ResourceManager resourceManager;

        public FlagRenderer(ResourceManager resourceManager)
        {
            this.resourceManager = resourceManager;
            setOpaque(true);
        }

        /**
         * Returns a suitable cell.
         *
         * @param list         The list.
         * @param value        The object.
         * @param index        The index.
         * @param isSelected   true if it is selected.
         * @param cellHasFocus Description of the Parameter
         * @return The cell.
         */
        public Component getListCellRendererComponent(JList list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus)
        {
            // We put the label
            String iso3 = (String) value;
            setText(iso3);
            if (isoTable != null)
            {
                iso3 = (String) isoTable.get(iso3);
            }
            if (isSelected)
            {
                setForeground(list.getSelectionForeground());
                setBackground(list.getSelectionBackground());
            }
            else
            {
                setForeground(list.getForeground());
                setBackground(list.getBackground());
            }
            // We put the icon

            if (!icons.containsKey(iso3))
            {
                ImageIcon icon;
                icon = resourceManager.getImageIconResource("flag." + iso3);
                icons.put(iso3, icon);
                icon = new ImageIcon(GrayFilter.createDisabledImage(icon.getImage()));
                grayIcons.put(iso3, icon);
            }
            if (isSelected || index == -1)
            {
                setIcon(icons.get(iso3));
            }
            else
            {
                setIcon(grayIcons.get(iso3));
            }

            // We return
            return this;
        }
    }

    /**
     * Returns the type in which the language should be displayed in the language selction dialog.
     * Possible are "iso3", "native" and "usingDefault".
     *
     * @return language display type
     */
    protected String getLangType()
    {
        if (installdata.guiPrefs.modifier.containsKey("langDisplayType"))
        {
            String val = installdata.guiPrefs.modifier.get("langDisplayType");
            val = val.toLowerCase();
            // Verify that the value is valid, else return the default.
            for (String aLANGUAGE_DISPLAY_TYPES : LANGUAGE_DISPLAY_TYPES)
            {
                if (val.equalsIgnoreCase(aLANGUAGE_DISPLAY_TYPES))
                {
                    return (val);
                }
            }
            Debug.trace("Value for language display type not valid; value: " + val);
        }
        return (LANGUAGE_DISPLAY_TYPES[0]);
    }


    /**
     * Returns whether flags should be used in the language selection dialog or not.
     *
     * @return whether flags should be used in the language selection dialog or not
     */
    protected boolean useFlags()
    {
        if (installdata.guiPrefs.modifier.containsKey("useFlags")
                && "no".equalsIgnoreCase(installdata.guiPrefs.modifier.get("useFlags")))
        {
            return (false);
        }
        return (true);
    }


    public void initLangPack() throws Exception
    {
        // Checks the Java version
        conditionCheck.check();

        // Loads the suitable langpack
        java.util.List<String> availableLangPacks = resourceManager.getAvailableLangPacks();
        int npacks = availableLangPacks.size();
        // We get the langpack name
        if (npacks != 1)
        {
            this.runPicker();
        }

        // check installer conditions
        if (!conditionCheck.checkInstallerRequirements(this))
        {
            Debug.log("not all installerconditions are fulfilled.");
            System.exit(-1);
            return;
        }
    }

    /**
     * Set locales on installData and resourceManager
     *
     * @param selectedPack
     * @throws Exception
     */
    private void propagateLocale(String selectedPack) throws Exception
    {
        InputStream in = resourceManager.getInputStream("langpacks/" + selectedPack + ".xml");
        installdata.setAndProcessLocal(selectedPack, new LocaleDatabase(in));
        resourceManager.setLocale(selectedPack);
    }

    public void showMissingRequirementMessage(String message)
    {
        JOptionPane.showMessageDialog(null, message);
    }
}
