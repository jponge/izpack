/*
 * IzPack Version 3.0.0 rc1 (build 2002.07.03)
 * Copyright (C) 2001,2002 Julien Ponge
 *
 * File :               Installer.java
 * Description :        The Installer class.
 * Author's email :     julien@izforge.com
 * Author's Website :   http://www.izforge.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.izforge.izpack.installer;

import com.izforge.izpack.*;
import com.izforge.izpack.gui.*;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.event.*;
import javax.swing.plaf.metal.*;

import net.n3.nanoxml.*;

public class Installer
{
    //.....................................................................

    // The fields
    private LocaleDatabase langpack;        // Contains the language pack
    private InstallData installdata;        // The installation data

    // The constructor (normal mode)
    public Installer() throws Exception
    {
        super();

        // Loads the installation data
        loadInstallData();

        // Sets up the GUI L&F
        loadLookAndFeel();

        // Loads the suitable langpack
        loadLangPack();

        // We launch the installer GUI
        loadGUI();
    }

    // The constructor (automated mode)
    public Installer(File input) throws Exception
    {
        super();

        // Loads the installation data
        loadInstallData();

        // Loads the xml data
        loadXMLData(input);

        // Loads the langpack
        installdata.localeISO3 = installdata.xmlData.getAttribute("langpack");
        InputStream in = getClass().getResourceAsStream("/langpacks/" +
                                                        installdata.localeISO3 +
                                                        ".xml");
        langpack = new LocaleDatabase(in);

        // Loads the installer frame
        InstallerFrame installerFrame = new InstallerFrame(langpack, installdata);
    }

    //.....................................................................
    // The methods

    // Loads the xml data for the automated mode
    private void loadXMLData(File input) throws Exception
    {
        FileInputStream in = new FileInputStream(input);

        // Initialises the parser
        StdXMLParser parser = new StdXMLParser();
        parser.setBuilder(new StdXMLBuilder());
        parser.setReader(new StdXMLReader(in));
        parser.setValidator(new NonValidator());

        // We get the data
        installdata.xmlData = (XMLElement) parser.parse();

        in.close();
    }

    // Loads the suitable langpack
    private void loadLangPack() throws Exception
    {
        // Initialisations
        ArrayList availableLangPacks = getAvailableLangPacks();
        int npacks = availableLangPacks.size();
        if (npacks == 0) throw new Exception("no language pack available");
        String selectedPack;

        // We get the langpack name
        if (npacks != 1)
        {
            LanguageDialog picker = new LanguageDialog(availableLangPacks.toArray());
            picker.setSelection(Locale.getDefault().getISO3Country().toLowerCase());
            picker.setModal(true);
            picker.show();

            selectedPack = (String) picker.getSelection();
            if (selectedPack == null) throw new Exception("installation canceled");
        }
        else
        {
            selectedPack = (String) availableLangPacks.get(0);
        }

        // We add an xml data information
        installdata.xmlData.setAttribute("langpack", selectedPack);

        // We load the langpack
        installdata.localeISO3 = selectedPack;
        InputStream in = getClass().getResourceAsStream("/langpacks/" + selectedPack + ".xml");
        langpack = new LocaleDatabase(in);
    }

    // Returns an ArrayList of the available langpacks ISO3 codes
    private ArrayList getAvailableLangPacks() throws Exception
    {
        // We read from the langpacks file in the jar
        ArrayList available = new ArrayList();
        InputStream in = getClass().getResourceAsStream("/langpacks.info");
        DataInputStream datIn = new DataInputStream(in);
        int size = datIn.readInt();
        for (int i = 0; i < size; i++) available.add(datIn.readUTF());
        datIn.close();

        return available;
    }

    // Loads the installation data
    private void loadInstallData() throws Exception
    {
        // Usefull variables
        InputStream in;
        DataInputStream datIn;
        ObjectInputStream objIn;
        int size;
        int i;

        // We load the variables
        Properties variables = null;
        in = getClass().getResourceAsStream("/vars");
        if (null != in) 
        {
            objIn = new ObjectInputStream(in);
            variables = (Properties) objIn.readObject();
            objIn.close();
        }

        EnrollInfo enrollInfo = null;

        // We load the EnrollPanel informations
        if (null != in) 
        {
            in = getClass().getResourceAsStream("/enroll");
            objIn = new ObjectInputStream(in);
            enrollInfo = (EnrollInfo) objIn.readObject();
            objIn.close();
        }

        // We load the Info data
        in = getClass().getResourceAsStream("/info");
        objIn = new ObjectInputStream(in);
        Info inf = (Info) objIn.readObject();
        objIn.close();

        // We load the GUIPrefs
        in = getClass().getResourceAsStream("/GUIPrefs");
        objIn = new ObjectInputStream(in);
        GUIPrefs guiPrefs = (GUIPrefs) objIn.readObject();
        objIn.close();

        // We read the panels order data
        in = getClass().getResourceAsStream("/panelsOrder");
        datIn = new DataInputStream(in);
        size = datIn.readInt();
        ArrayList panelsOrder = new ArrayList();
        for (i = 0; i < size; i++) panelsOrder.add(datIn.readUTF());
        datIn.close();

        // We read the packs data
        in = getClass().getResourceAsStream("/packs.info");
        objIn = new ObjectInputStream(in);
        size = objIn.readInt();
        ArrayList availablePacks = new ArrayList();
        for (i = 0; i < size; i++) availablePacks.add(objIn.readObject());
        objIn.close();

        // We determine the operating system and the initial installation path
        String os = System.getProperty("os.name");
        String user = System.getProperty("user.name");
        String dir;
        String installPath;
        if (os.regionMatches(true, 0, "windows", 0, 7))
            dir = System.getProperty("user.home").substring(0,3) + "Program Files" + File.separator;
        else if (os.regionMatches(true, 0, "macosx", 0, 6))
            dir = "/Applications" + File.separator;
        else if (os.regionMatches(true, 0, "mac", 0, 3))
            dir = "";
        else 
        {
            if(user.equals("root")) 
                dir = "/usr/local" + File.separator;
            else 
                dir = System.getProperty("user.home") + File.separator;
        }
        installPath = dir + inf.getAppName();

        // We read the installation kind
        in = getClass().getResourceAsStream("/kind");
        datIn = new DataInputStream(in);
        String kind = datIn.readUTF();
        datIn.close();

        // We build a new InstallData
        installdata = InstallData.getInstance();
        installdata.setInstallPath(installPath);
        installdata.setVariable
            (ScriptParser.JAVA_HOME, System.getProperty("java.home"));
        installdata.setVariable
            (ScriptParser.USER_HOME, System.getProperty("user.home"));
        installdata.setVariable
            (ScriptParser.USER_NAME, System.getProperty("user.name"));
        installdata.setVariable
            (ScriptParser.FILE_SEPARATOR, File.separator);
        if (null != variables) 
        {
            Enumeration enum = variables.keys();
            String varName = null;
            String varValue = null;
            while (enum.hasMoreElements()) 
            {
                varName = (String)enum.nextElement();
                varValue = (String) variables.getProperty(varName);
                installdata.setVariable(varName, varValue);
            }
        }
        installdata.enrollInfo = enrollInfo;
        installdata.guiPrefs = guiPrefs;
        installdata.info = inf;
        installdata.kind = kind;
        installdata.panelsOrder = panelsOrder;
        installdata.availablePacks = availablePacks;
        installdata.selectedPacks = (ArrayList) availablePacks.clone();
    }

    // Loads the suitable L&F
    private void loadLookAndFeel() throws Exception
    {
        if (installdata.kind.equalsIgnoreCase("standard") ||
            installdata.kind.equalsIgnoreCase("web") )
        {
            // We simply put our nice theme
            MetalLookAndFeel.setCurrentTheme(new IzPackMetalTheme());
        }
        else
        if (installdata.kind.equalsIgnoreCase("standard-kunststoff") ||
            installdata.kind.equalsIgnoreCase("web-kunststoff"))
        {
            // We change the highlight color for the buttons
            installdata.buttonsHColor = new Color(255, 255, 255);

            // Some reflection ...
            Class laf = Class.forName("com.incors.plaf.kunststoff.KunststoffLookAndFeel");
            Class mtheme = Class.forName("javax.swing.plaf.metal.MetalTheme");
            Class[] params = { mtheme };
            Class theme = Class.forName("com.izforge.izpack.gui.IzPackKMetalTheme");
            Method setCurrentThemeMethod = laf.getMethod("setCurrentTheme", params);

            // We invoke and place Kunststoff as our L&F
            LookAndFeel kunststoff = (LookAndFeel) laf.newInstance();
            MetalTheme ktheme = (MetalTheme) theme.newInstance();
            Object[] kparams = { ktheme };
            UIManager.setLookAndFeel(kunststoff);
            setCurrentThemeMethod.invoke(kunststoff, kparams);
        }
    }

    // Loads the GUI
    private void loadGUI() throws Exception
    {
        UIManager.put("OptionPane.yesButtonText", langpack.getString("installer.yes"));
        UIManager.put("OptionPane.noButtonText", langpack.getString("installer.no"));
        UIManager.put("OptionPane.cancelButtonText", langpack.getString("installer.cancel"));

        String title = langpack.getString("installer.title") + installdata.info.getAppName();
        InstallerFrame installerFrame = new InstallerFrame(title, langpack, installdata);
    }

    //.....................................................................

    // The main method (program entry-point)
    public static void main(String[] args)
    {
        try
        {
            int nargs = args.length;
            Installer ins;

            if (nargs == 0)
            {
                ins = new Installer();
            }
            else
            {
                ins = new Installer(new File(args[0]));
            }
        }
        catch (Exception e)
        {
            System.err.println("- Error -");
            System.err.println(e.toString());
            e.printStackTrace();
            System.exit(0);
        }
    }

    // Used to prompt the user for the language
    class LanguageDialog extends JDialog implements ActionListener
    {
        // The fields
        private JComboBox comboBox;
        private JButton okButton;

        // The constructor
        public LanguageDialog(Object[] items)
        {
            super();

            // We build the GUI
            addWindowListener(new WindowHandler());
            JPanel contentPane = (JPanel) getContentPane();
            setTitle("Language selection");
            GridBagLayout layout = new GridBagLayout();
            contentPane.setLayout(layout);
            GridBagConstraints gbConstraints = new GridBagConstraints();
            gbConstraints.anchor = GridBagConstraints.CENTER;
            gbConstraints.insets = new Insets(5,5,5,5);
            gbConstraints.fill = GridBagConstraints.NONE;
            gbConstraints.gridx = 0;
            gbConstraints.weightx = 1.0;
            gbConstraints.weighty = 1.0;

            ImageIcon img = getImage();
            JLabel imgLabel = new JLabel(img);
            gbConstraints.gridy = 0;
            contentPane.add(imgLabel);

            gbConstraints.fill = GridBagConstraints.HORIZONTAL;
            JLabel label1 = new JLabel("Please select your language (ISO3 code)", SwingConstants.CENTER);
            gbConstraints.gridy = 1;
            gbConstraints.insets = new Insets(5,5,0,5);
            layout.addLayoutComponent(label1, gbConstraints);
            contentPane.add(label1);
            JLabel label2 = new JLabel("for install instructions:", SwingConstants.CENTER);
            gbConstraints.gridy = 2;
            gbConstraints.insets = new Insets(0,5,5,5);
            layout.addLayoutComponent(label2, gbConstraints);
            contentPane.add(label2);
            gbConstraints.insets = new Insets(5,5,5,5);

            comboBox = new JComboBox(items);
            comboBox.setRenderer(new FlagRenderer());
            gbConstraints.fill = GridBagConstraints.HORIZONTAL;
            gbConstraints.gridy = 3;
            layout.addLayoutComponent(comboBox, gbConstraints);
            contentPane.add(comboBox);

            okButton = new JButton("Ok");
            okButton.addActionListener(this);
            gbConstraints.fill = GridBagConstraints.NONE;
            gbConstraints.gridy = 4;
            gbConstraints.anchor = GridBagConstraints.CENTER;
            layout.addLayoutComponent(okButton, gbConstraints);
            contentPane.add(okButton);

            // Packs and centers
            pack();
            Dimension frameSize = getSize();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            setLocation( (screenSize.width - frameSize.width) / 2,
                         (screenSize.height - frameSize.height) / 2 - 10);
            setResizable(true);
        }

        // Loads an image to show if it is present
        public ImageIcon getImage()
        {
            ImageIcon img;
            try
            {
                 img =  new ImageIcon(this.getClass().getResource(
                                      "/res/installer.langsel.img"));
            }
            catch (NullPointerException err)
            {
                img = null;
            }
            return img;
        }

        // Gets the selected object
        public Object getSelection()
        {
            return comboBox.getSelectedItem();
        }

        // Sets the selection
        public void setSelection(Object item)
        {
            comboBox.setSelectedItem(item);
        }

        // Closer
        public void actionPerformed(ActionEvent e)
        {
            dispose();
        }

        // The window events handler
        class WindowHandler extends WindowAdapter
        {
            // We can't avoid the exit here ... so don't call exit
            public void windowClosing(WindowEvent e)
            {
                System.exit(0);
            }
        }
    }

    // A list cell renderer that adds the flags
    class FlagRenderer extends JLabel implements ListCellRenderer
    {
        // Used to cache the icons
        private TreeMap icons = new TreeMap();
        private TreeMap grayIcons = new TreeMap();

        // Returns a suitable cell
        public Component getListCellRendererComponent(JList list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus)
        {
            // We avoid that the icon is gray while the combo isn't deployed
            if (index == -1) isSelected = true;

            // We put the label
            String iso3 = (String) value;
            setText(iso3);
            if (isSelected)
            {
                setOpaque(true);
                setForeground(list.getSelectionForeground());
                setBackground(list.getSelectionBackground());
            }
            else setOpaque(false);

            // We put the icon
            if (!icons.containsKey(iso3))
            {
                ImageIcon icon = new ImageIcon(this.getClass().getResource("/res/flag." + iso3));
                icons.put(iso3, icon);
                grayIcons.put(iso3, new ImageIcon(GrayFilter.createDisabledImage(icon.getImage())));
            }
            if (isSelected)
                setIcon((ImageIcon)icons.get(iso3));
            else
                setIcon((ImageIcon)grayIcons.get(iso3));

            // We return
            return this;
        }
    }

    //.....................................................................
}
