/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2003 Julien Ponge
 *
 *  File :               GUIInstaller.java
 *  Description :        The graphical installer class.
 *  Author's email :     julien@izforge.com
 *  Author's Website :   http://www.izforge.com
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.izforge.izpack.installer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TreeMap;

import javax.swing.GrayFilter;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.LookAndFeel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;

import com.izforge.izpack.GUIPrefs;
import com.izforge.izpack.LocaleDatabase;
import com.izforge.izpack.gui.ButtonFactory;
import com.izforge.izpack.gui.IzPackMetalTheme;

/**
 *  The IzPack graphical installer class.
 *
 * @author     Julien Ponge
 */
public class GUIInstaller extends InstallerBase
{
  /**  The installation data. */
  private InstallData installdata;

  /**  The L&F. */
  protected String lnf;


  /**
   *  The constructor.
   *
   * @exception  Exception  Description of the Exception
   */
  public GUIInstaller() throws Exception
  {
    super();

		this.installdata = new InstallData();

    // Loads the installation data
    loadInstallData(installdata);

		// add the GUI install data
		loadGUIInstallData();

    // Checks the Java version
    checkJavaVersion();

    // Sets up the GUI L&F
    loadLookAndFeel();

    // Loads the suitable langpack
    loadLangPack();

    // create the resource manager (after the language selection!)
    ResourceManager.create (this.installdata);

    // We launch the installer GUI
    loadGUI();
  }

  /**
	 * Load GUI preference information.
	 *
	 * @throws Exception
	 */
  public void loadGUIInstallData() throws Exception 
  {
		InputStream in = getClass().getResourceAsStream("/GUIPrefs");
		ObjectInputStream objIn = new ObjectInputStream(in);
		this.installdata.guiPrefs = (GUIPrefs) objIn.readObject();
		objIn.close();
	}


  /**
   *  Checks the Java version.
   *
   * @exception  Exception  Description of the Exception
   */
  private void checkJavaVersion() throws Exception
  {
    String version = System.getProperty("java.version");
    String required = this.installdata.info.getJavaVersion();
    if (version.compareTo(required) < 0)
    {
      System.out.println("Can't install !");
      System.out.println("> The minimum Java version required is " +
        required);
      System.out.println("> Your version is " + version);
      System.out.println("Please upgrade to the minimum version.");
      System.exit(1);
    }
  }


  /**
   *  Loads the suitable langpack.
   *
   * @exception  Exception  Description of the Exception
   */
  private void loadLangPack() throws Exception
  {
    // Initialisations
    ArrayList availableLangPacks = getAvailableLangPacks();
    int npacks = availableLangPacks.size();
    if (npacks == 0)
      throw new Exception("no language pack available");
    String selectedPack;

    // We get the langpack name
    if (npacks != 1)
    {
      LanguageDialog picker = new LanguageDialog(availableLangPacks.toArray());
      picker.setSelection(Locale.getDefault().getISO3Country().toLowerCase());
      picker.setModal(true);
      picker.show();

      selectedPack = (String) picker.getSelection();
      if (selectedPack == null)
        throw new Exception("installation canceled");
    }
    else
      selectedPack = (String) availableLangPacks.get(0);

    // We add an xml data information
    this.installdata.xmlData.setAttribute("langpack", selectedPack);

    // We load the langpack
    installdata.localeISO3 = selectedPack;
    installdata.setVariable(ScriptParser.ISO3_LANG, installdata.localeISO3);
    InputStream in = getClass().getResourceAsStream("/langpacks/" + selectedPack + ".xml");
    this.installdata.langpack = new LocaleDatabase(in);
  }


  /**
   *  Returns an ArrayList of the available langpacks ISO3 codes.
   *
   * @return                The available langpacks list.
   * @exception  Exception  Description of the Exception
   */
  private ArrayList getAvailableLangPacks() throws Exception
  {
    // We read from the langpacks file in the jar
    ArrayList available = new ArrayList();
    InputStream in = getClass().getResourceAsStream("/langpacks.info");
    DataInputStream datIn = new DataInputStream(in);
    int size = datIn.readInt();
    for (int i = 0; i < size; i++)
      available.add(datIn.readUTF());
    datIn.close();

    return available;
  }


  /**
   *  Loads the suitable L&F.
   *
   * @exception  Exception  Description of the Exception
   */
  protected void loadLookAndFeel() throws Exception
  {
    if (this.installdata.kind.equalsIgnoreCase("standard") ||
      this.installdata.kind.equalsIgnoreCase("web"))
    {
      if (getClass().getResourceAsStream("/res/useNativeLAF") != null)
      {
        String nlaf = UIManager.getSystemLookAndFeelClassName();
        UIManager.setLookAndFeel(nlaf);
      }
    	if (UIManager.getLookAndFeel() instanceof MetalLookAndFeel)
    	{
		      // We simply put our nice theme
          MetalLookAndFeel.setCurrentTheme(new IzPackMetalTheme());
          ButtonFactory.useHighlightButtons();
          this.installdata.buttonsHColor = new Color(182, 182, 204);
      }
      lnf = "swing";
    }
    else
      if (this.installdata.kind.equalsIgnoreCase("standard-kunststoff") ||
      this.installdata.kind.equalsIgnoreCase("web-kunststoff"))
    {
    	ButtonFactory.useHighlightButtons();
      // We change the highlight color for the buttons
      this.installdata.buttonsHColor = new Color(255, 255, 255);

      // Some reflection ...
      Class laf = Class.forName("com.incors.plaf.kunststoff.KunststoffLookAndFeel");
      Class mtheme = Class.forName("javax.swing.plaf.metal.MetalTheme");
      Class[] params = {mtheme};
      Class theme = Class.forName("com.izforge.izpack.gui.IzPackKMetalTheme");
      Method setCurrentThemeMethod = laf.getMethod("setCurrentTheme", params);

      // We invoke and place Kunststoff as our L&F
      LookAndFeel kunststoff = (LookAndFeel) laf.newInstance();
      MetalTheme ktheme = (MetalTheme) theme.newInstance();
      Object[] kparams = {ktheme};
      UIManager.setLookAndFeel(kunststoff);
      setCurrentThemeMethod.invoke(kunststoff, kparams);

      lnf = "kunststoff";
    }
    ButtonFactory.useButtonIcons();
  }


  /**
   *  Loads the GUI.
   *
   * @exception  Exception  Description of the Exception
   */
  private void loadGUI() throws Exception
  {
    UIManager.put("OptionPane.yesButtonText", installdata.langpack.getString("installer.yes"));
    UIManager.put("OptionPane.noButtonText", installdata.langpack.getString("installer.no"));
    UIManager.put("OptionPane.cancelButtonText", installdata.langpack.getString("installer.cancel"));

    String title = installdata.langpack.getString("installer.title") + this.installdata.info.getAppName();
    new InstallerFrame(title, this.installdata);
  }


  /**
   *  Used to prompt the user for the language.
   *
   * @author     Julien Ponge
   */
  class LanguageDialog extends JDialog implements ActionListener
  {
    /**  The combo box. */
    private JComboBox comboBox;

    /**  The ok button. */
    private JButton okButton;


    /**
     *  The constructor.
     *
     * @param  items  The items to display in the box.
     */
    public LanguageDialog(Object[] items)
    {
      super();

      try
      {
        loadLookAndFeel();
      }
      catch (Exception err) { }

      // We build the GUI
      addWindowListener(new WindowHandler());
      JPanel contentPane = (JPanel) getContentPane();
      setTitle("Language selection");
      GridBagLayout layout = new GridBagLayout();
      contentPane.setLayout(layout);
      GridBagConstraints gbConstraints = new GridBagConstraints();
      gbConstraints.anchor = GridBagConstraints.CENTER;
      gbConstraints.insets = new Insets(5, 5, 5, 5);
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
      gbConstraints.insets = new Insets(5, 5, 0, 5);
      layout.addLayoutComponent(label1, gbConstraints);
      contentPane.add(label1);
      JLabel label2 = new JLabel("for install instructions:", SwingConstants.CENTER);
      gbConstraints.gridy = 2;
      gbConstraints.insets = new Insets(0, 5, 5, 5);
      layout.addLayoutComponent(label2, gbConstraints);
      contentPane.add(label2);
      gbConstraints.insets = new Insets(5, 5, 5, 5);

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
      getRootPane().setDefaultButton(okButton);

      // Packs and centers
      // Fix for bug "Installer won't show anything on OSX"
      if (System.getProperty("mrj.version") == null)
        pack();
      else
        setSize(getPreferredSize());

      Dimension frameSize = getSize();
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      setLocation((screenSize.width - frameSize.width) / 2,
        (screenSize.height - frameSize.height) / 2 - 10);
      setResizable(true);
    }


    /**
     *  Loads an image.
     *
     * @return    The image icon.
     */
    public ImageIcon getImage()
    {
      ImageIcon img;
      try
      {
        img = new ImageIcon(this.getClass().getResource(
          "/res/installer.langsel.img"));
      }
      catch (NullPointerException err)
      {
        img = null;
      }
      return img;
    }


    /**
     *  Gets the selected object.
     *
     * @return    The selected item.
     */
    public Object getSelection()
    {
      return comboBox.getSelectedItem();
    }


    /**
     *  Sets the selection.
     *
     * @param  item  The item to be selected.
     */
    public void setSelection(Object item)
    {
      comboBox.setSelectedItem(item);
    }


    /**
     *  Closer.
     *
     * @param  e  The event.
     */
    public void actionPerformed(ActionEvent e)
    {
      dispose();
    }


    /**
     *  The window events handler.
     *
     * @author     Julien Ponge
     */
    class WindowHandler extends WindowAdapter
    {
      /**
       *  We can't avoid the exit here ... so don't call exit.
       *
       * @param  e  the event.
       */
      public void windowClosing(WindowEvent e)
      {
        System.exit(0);
      }
    }
  }


  /**
   *  A list cell renderer that adds the flags on the display.
   *
   * @author     Julien Ponge
   */
  class FlagRenderer extends JLabel implements ListCellRenderer
  {
    /**  Icons cache. */
    private TreeMap icons = new TreeMap();

    /**  Grayed icons cache. */
    private TreeMap grayIcons = new TreeMap();


    /**
     *  Returns a suitable cell.
     *
     * @param  list          The list.
     * @param  value         The object.
     * @param  index         The index.
     * @param  isSelected    true if it is selected.
     * @param  cellHasFocus  Description of the Parameter
     * @return               The cell.
     */
    public Component getListCellRendererComponent(JList list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus)
    {
      // We avoid that the icon is gray while the combo isn't deployed
      if (index == -1)
        isSelected = true;

      // We put the label
      String iso3 = (String) value;
      setText(iso3);
      if (isSelected)
      {
        if (lnf.equalsIgnoreCase("swing"))
          setOpaque(true);
        else if (index == -1)
          setOpaque(false);
        else
          setOpaque(true);
        setForeground(list.getSelectionForeground());
        setBackground(list.getSelectionBackground());
      }
      else
        setOpaque(false);

      // We put the icon
      if (!icons.containsKey(iso3))
      {
        ImageIcon icon = new ImageIcon(this.getClass().getResource("/res/flag." + iso3));
        icons.put(iso3, icon);
        grayIcons.put(iso3, new ImageIcon(GrayFilter.createDisabledImage(icon.getImage())));
      }
      if (isSelected)
        setIcon((ImageIcon) icons.get(iso3));
      else
        setIcon((ImageIcon) grayIcons.get(iso3));

      // We return
      return this;
    }
  }
}

