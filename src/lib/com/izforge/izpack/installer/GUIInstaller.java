/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2004 Julien Ponge
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
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.GrayFilter;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.LookAndFeel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;

import com.izforge.izpack.GUIPrefs;
import com.izforge.izpack.LocaleDatabase;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.gui.ButtonFactory;
import com.izforge.izpack.gui.IzPackMetalTheme;
import com.izforge.izpack.gui.LabelFactory;

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
    this.installdata = new InstallData();

    // Loads the installation data
    loadInstallData(installdata);

    // add the GUI install data
    loadGUIInstallData();

    // Sets up the GUI L&F
    loadLookAndFeel();

    // Checks the Java version
    checkJavaVersion();

    // Loads the suitable langpack
    loadLangPack();

    // create the resource manager (after the language selection!)
    ResourceManager.create(this.installdata);

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
    InputStream in = GUIInstaller.class.getResourceAsStream("/GUIPrefs");
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
      StringBuffer msg = new StringBuffer();
      msg.append("The application that you are trying to install requires a ");
      msg.append(required);
      msg.append(" version or later of the Java platform.\n");
      msg.append("You are running a ");
      msg.append(version);
      msg.append(" version of the Java platform.\n");
      msg.append("Please upgrade to a newer version.");

      System.out.println(msg.toString());
      JOptionPane.showMessageDialog(null, msg.toString(), "Error",
          JOptionPane.ERROR_MESSAGE);
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
    List availableLangPacks = getAvailableLangPacks();
    int npacks = availableLangPacks.size();
    if (npacks == 0) throw new Exception("no language pack available");
    String selectedPack;

    // Dummy Frame
    JFrame frame = new JFrame();
    frame.setIconImage( new ImageIcon(
      this.getClass().getResource( "/img/JFrameIcon.png" )).getImage() );

    Dimension frameSize = frame.getSize();
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setLocation((screenSize.width - frameSize.width) / 2,
      (screenSize.height - frameSize.height) / 2 - 10);

    // We get the langpack name
    if (npacks != 1)
    {
      LanguageDialog picker = new LanguageDialog(frame, availableLangPacks.toArray());
      picker.setSelection(Locale.getDefault().getISO3Country().toLowerCase());
      picker.setModal(true);
      picker.toFront();
      frame.show();
      frame.hide();
      picker.show();

      selectedPack = (String) picker.getSelection();
      if (selectedPack == null) throw new Exception("installation canceled");
    }
    else
      selectedPack = (String) availableLangPacks.get(0);

    // We add an xml data information
    this.installdata.xmlData.setAttribute("langpack", selectedPack);

    // We load the langpack
    installdata.localeISO3 = selectedPack;
    installdata.setVariable(ScriptParser.ISO3_LANG, installdata.localeISO3);
    InputStream in = getClass().getResourceAsStream(
        "/langpacks/" + selectedPack + ".xml");
    this.installdata.langpack = new LocaleDatabase(in);
  }

  /**
   *  Returns an ArrayList of the available langpacks ISO3 codes.
   *
   * @return                The available langpacks list.
   * @exception  Exception  Description of the Exception
   */
  private List getAvailableLangPacks() throws Exception
  {
    // We read from the langpacks file in the jar
    InputStream in = getClass().getResourceAsStream("/langpacks.info");
    ObjectInputStream objIn = new ObjectInputStream(in);
    List available = (List) objIn.readObject();
    objIn.close();

    return available;
  }

  /**
   *  Loads the suitable L&F.
   *
   * @exception  Exception  Description of the Exception
   */
  protected void loadLookAndFeel() throws Exception
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
    String laf = null;
    if (installdata.guiPrefs.lookAndFeelMapping.containsKey(syskey))
    {
      laf = (String) installdata.guiPrefs.lookAndFeelMapping.get(syskey);
    }

    // Let's use the system LAF
    // Resolve whether button icons should be used or not.
    boolean useButtonIcons = true;
    if(installdata.guiPrefs.modifier.containsKey("useButtonIcons") &&
      ((String)installdata.guiPrefs.modifier.
      get("useButtonIcons")).equalsIgnoreCase("no") )
      useButtonIcons = false;
    ButtonFactory.useButtonIcons(useButtonIcons);
    boolean useLabelIcons = true;
    if(installdata.guiPrefs.modifier.containsKey("useLabelIcons") &&
      ((String)installdata.guiPrefs.modifier.
      get("useLabelIcons")).equalsIgnoreCase("no") )
      useLabelIcons = false;
    LabelFactory.setUseLabelIcons(useLabelIcons);
    if (laf == null)
    {
      if (!syskey.equals("mac"))
      {
        String syslaf = UIManager.getSystemLookAndFeelClassName();
        UIManager.setLookAndFeel(syslaf);
        if (UIManager.getLookAndFeel() instanceof MetalLookAndFeel)
        {
          MetalLookAndFeel.setCurrentTheme(new IzPackMetalTheme());
          ButtonFactory.useHighlightButtons();
          // Reset the use button icons state because useHighlightButtons
          // make it always true.
          ButtonFactory.useButtonIcons(useButtonIcons);
          installdata.buttonsHColor = new Color(182, 182, 204);
        }
      }
      lnf = "swing";
      return;
    }

    // Kunststoff (http://www.incors.org/)
    if (laf.equals("kunststoff"))
    {
      ButtonFactory.useHighlightButtons();
      // Reset the use button icons state because useHighlightButtons
      // make it always true.
      ButtonFactory.useButtonIcons(useButtonIcons);
      installdata.buttonsHColor = new Color(255, 255, 255);
      Class lafClass = Class
          .forName("com.incors.plaf.kunststoff.KunststoffLookAndFeel");
      Class mtheme = Class.forName("javax.swing.plaf.metal.MetalTheme");
      Class[] params = { mtheme};
      Class theme = Class.forName("com.izforge.izpack.gui.IzPackKMetalTheme");
      Method setCurrentThemeMethod = lafClass.getMethod("setCurrentTheme", params);

      // We invoke and place Kunststoff as our L&F
      LookAndFeel kunststoff = (LookAndFeel) lafClass.newInstance();
      MetalTheme ktheme = (MetalTheme) theme.newInstance();
      Object[] kparams = { ktheme};
      UIManager.setLookAndFeel(kunststoff);
      setCurrentThemeMethod.invoke(kunststoff, kparams);

      lnf = "kunststoff";
      return;
    }

    // Liquid (http://liquidlnf.sourceforge.net/)
    if (laf.equals("liquid"))
    {
      UIManager.setLookAndFeel("com.birosoft.liquid.LiquidLookAndFeel");
      lnf = "liquid";

      Map params = (Map)installdata.guiPrefs.lookAndFeelParams.get(laf);
      if (params.containsKey("decorate.frames"))
      {
        String value = (String)params.get("decorate.frames");
        if (value.equals("yes"))
        {
          JFrame.setDefaultLookAndFeelDecorated(true);
        }
      }
      if (params.containsKey("decorate.dialogs"))
      {
        String value = (String)params.get("decorate.dialogs");
        if (value.equals("yes"))
        {
          JDialog.setDefaultLookAndFeelDecorated(true);
        }
      }

      return;
    }

    // Metouia (http://mlf.sourceforge.net/)
    if (laf.equals("metouia"))
    {
      UIManager.setLookAndFeel("net.sourceforge.mlf.metouia.MetouiaLookAndFeel");
      lnf = "metouia";
      return;
    }

    // JGoodies Looks (http://looks.dev.java.net/)
    if (laf.equals("looks"))
    {
      Map variants = new TreeMap();
      variants.put("extwin", "com.jgoodies.plaf.windows.ExtWindowsLookAndFeel");
      variants.put("plastic", "com.jgoodies.plaf.plastic.PlasticLookAndFeel");
      variants.put("plastic3D", "com.jgoodies.plaf.plastic.Plastic3DLookAndFeel");
      variants.put("plasticXP", "com.jgoodies.plaf.plastic.PlasticXPLookAndFeel");
      String variant = (String)variants.get("plasticXP");

      Map params = (Map)installdata.guiPrefs.lookAndFeelParams.get(laf);
      if (params.containsKey("variant"))
      {
        String param = (String)params.get("variant");
        if (variants.containsKey(param))
        {
          variant = (String)variants.get(param);
        }
      }

      UIManager.setLookAndFeel(variant);
    }
  }

  /**
   *  Loads the GUI.
   *
   * @exception  Exception  Description of the Exception
   */
  private void loadGUI() throws Exception
  {
    UIManager.put("OptionPane.yesButtonText", installdata.langpack
        .getString("installer.yes"));
    UIManager.put("OptionPane.noButtonText", installdata.langpack
        .getString("installer.no"));
    UIManager.put("OptionPane.cancelButtonText", installdata.langpack
        .getString("installer.cancel"));

    String title = installdata.langpack.getString("installer.title")
        + this.installdata.info.getAppName();
    new InstallerFrame(title, this.installdata);
  }

  /**
   *  Used to prompt the user for the language.
   *
   * @author     Julien Ponge
   */
  private final class LanguageDialog extends JDialog implements ActionListener
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
    public LanguageDialog(JFrame frame, Object[] items)
    {
      super(frame);

      try
      {
        loadLookAndFeel();
      }
      catch (Exception err)
      {
        err.printStackTrace();
      }

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
      JLabel label1 = new JLabel("Please select your language (ISO3 code)",
          SwingConstants.CENTER);
      gbConstraints.gridy = 1;
      gbConstraints.insets = new Insets(5, 5, 0, 5);
      layout.addLayoutComponent(label1, gbConstraints);
      contentPane.add(label1);
      JLabel label2 = new JLabel("for install instructions:",
          SwingConstants.CENTER);
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
        img = new ImageIcon(LanguageDialog.class.getResource(
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
    private class WindowHandler extends WindowAdapter
    {

      /**
       *  We can't avoid the exit here, so don't call exit anywhere else.
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
  private static class FlagRenderer extends JLabel implements ListCellRenderer
  {

    /**  Icons cache. */
    private TreeMap icons = new TreeMap();

    /**  Grayed icons cache. */
    private TreeMap grayIcons = new TreeMap();

    public FlagRenderer()
    {
      setOpaque(true);
    }

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
    public Component getListCellRendererComponent(JList list, Object value,
        int index, boolean isSelected, boolean cellHasFocus)
    {
      // We put the label
      String iso3 = (String) value;
      setText(iso3);
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
        icon = new ImageIcon(this.getClass().getResource("/res/flag." + iso3));
        icons.put(iso3, icon);
        icon = new ImageIcon(GrayFilter.createDisabledImage(icon.getImage()));
        grayIcons.put(iso3, icon);
      }
      if (isSelected || index == -1)
        setIcon((ImageIcon) icons.get(iso3));
      else
        setIcon((ImageIcon) grayIcons.get(iso3));

      // We return
      return this;
    }
  }
}
