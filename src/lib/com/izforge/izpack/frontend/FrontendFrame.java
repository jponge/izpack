/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2003 Julien Ponge
 *
 *  File :               FrontendFrame.java
 *  Description :        The Frontend frame class.
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
package com.izforge.izpack.frontend;

import com.izforge.izpack.*;
import com.izforge.izpack.gui.*;

import java.io.*;
import java.net.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import net.n3.nanoxml.*;

import com.incors.plaf.kunststoff.*;

/**
 *  The frontend frame class.
 *
 * @author     Julien Ponge
 * @created    October 26, 2002
 */
public class FrontendFrame extends JFrame
{
  /**  The actions. */
  private TreeMap actions;

  /**  The icons. */
  private IconsDatabase icons;

  /**  The language pack. */
  protected LocaleDatabase langpack;

  /**  The installation XML tree. */
  private XMLElement installation;

  /**  The current filename. */
  private String curFilename;

  /**  The current base path. */
  private String basepath = "";

  /**  The menu bar. */
  private JMenuBar menuBar;

  /**  The toolbar. */
  private JToolBar toolBar;

  /**  The tabbed pane. */
  private JTabbedPane tabbedPane;

  /**  The content pane. */
  private JPanel contentPane;

  /**  The tabs list. */
  private ArrayList tabs;

  /**  The list renderer, for the Kunststoff L&F. */
  public final static ListCellRenderer LIST_RENDERER = new ModifiedDefaultListCellRenderer();

  /**  The higlighting color for the buttons. */
  public final static Color buttonsHColor = new Color(255, 255, 255);


  /**
   *  The constructor.
   *
   * @param  title          The title bar text.
   * @param  langpack       The language pack.
   * @exception  Exception  Description of the Exception
   */
  public FrontendFrame(String title, LocaleDatabase langpack) throws Exception
  {
    super(title);
    this.langpack = langpack;
    contentPane = (JPanel) getContentPane();
   
    // Loaders
    installation = Frontend.createBlankInstallation();
    Frontend.splashWindow.update(3, "Loading the actions ...");
    loadActions();
    Frontend.splashWindow.update(4, "Loading the icons ...");
    loadIcons();
    
    // Sets the Kunststoff L&F as the right one
    KunststoffLookAndFeel klnf = new KunststoffLookAndFeel();
    UIManager.setLookAndFeel(klnf);
    KunststoffLookAndFeel.setCurrentTheme(new IzPackKMetalTheme());

    // Builds the GUI
    Frontend.splashWindow.update(5, "Building the GUI ...");
    buildGUI();
    pack();
    setSize(640, 480);
    setResizable(false);
    centerFrame(this);
    Frontend.splashWindow.stop();
    setVisible(true);
    
  }


  /**  Loads the actions.  */
  private void loadActions()
  {
    actions = new TreeMap();

    actions.put("files", new FilesHandler());
    actions.put("compiler", new CompilerHandler());
    actions.put("others", new OthersHandler());
  }


  /**
   *  Loads the GUI.
   *
   * @exception  Exception  Description of the Exception
   */
  private void buildGUI() throws Exception
  {
    // We initialize the buttons factory
    ButtonFactory.useButtonIcons();
    ButtonFactory.useHighlightButtons();
  
    // Window events handler
    addWindowListener(new WindowHandler());

    // Sets the frame icon
    setIconImage(icons.getImageIcon("JFrameIcon").getImage());

    // Sets the menubar
    menuBar = new FrontendMenuBar(actions, icons, langpack);
    setJMenuBar(menuBar);

    // Sets the toolbar
    toolBar = new FrontendToolBar(actions, icons, langpack);
    contentPane.add(toolBar, BorderLayout.NORTH);

    // Creates the tabbed pane
    tabbedPane = new JTabbedPane();

    tabbedPane.addTab(langpack.getString("tabs.info.title"),
      icons.getImageIcon("information"),
      new FrontendInfoTab(installation, icons, langpack));
    tabbedPane.addTab(langpack.getString("tabs.loc.title"),
      icons.getImageIcon("search"),
      new FrontendLocTab(installation, icons, langpack));
    tabbedPane.addTab(langpack.getString("tabs.res.title"),
      icons.getImageIcon("properties"),
      new FrontendResTab(installation, icons, langpack));
    tabbedPane.addTab(langpack.getString("tabs.pan.title"),
      icons.getImageIcon("paste"),
      new FrontendPanTab(installation, icons, langpack));
    tabbedPane.addTab(langpack.getString("tabs.packs.title"),
      icons.getImageIcon("preferences"),
      new FrontendPacksTab(installation, icons, langpack));
    contentPane.add(tabbedPane, BorderLayout.CENTER);

    // Fills the tab array & make the tabs use the XML data
    int ntabs = tabbedPane.getTabCount();
    tabs = new ArrayList(ntabs);
    for (int i = 0; i < ntabs; i++)
    {
      FrontendTab tab = (FrontendTab) tabbedPane.getComponentAt(i);
      tabs.add(tab);
      tab.updateComponents();
    }

    // Filenaming
    curFilename = langpack.getString("frontend.untitled");
    updateTitle();
  }


  /**
   *  Loads the icons.
   *
   * @exception  Exception  Description of the Exception
   */
  private void loadIcons() throws Exception
  {
    // Initialisations
    icons = new IconsDatabase();
    URL url;
    ImageIcon img;
    XMLElement icon;
    InputStream inXML = getClass().getResourceAsStream("/com/izforge/izpack/frontend/icons.xml");

    // Initialises the parser
    StdXMLParser parser = new StdXMLParser();
    parser.setBuilder(new StdXMLBuilder());
    parser.setReader(new StdXMLReader(inXML));
    parser.setValidator(new NonValidator());

    // We get the data
    XMLElement data = (XMLElement) parser.parse();

    // We load the icons
    Vector children = data.getChildren();
    int size = children.size();
    for (int i = 0; i < size; i++)
    {
      icon = (XMLElement) children.get(i);
      url = getClass().getResource(icon.getAttribute("res"));
      img = new ImageIcon(url);
      icons.put(icon.getAttribute("id"), img);
    }
  }


  /**  Ensures that the data is up to date.  */
  private void updateXMLTree()
  {
    int size = tabs.size();
    for (int i = 0; i < size; i++)
    {
      FrontendTab tab = (FrontendTab) tabbedPane.getComponentAt(i);
      tab.updateXMLTree();
    }
  }


  /**  Informs the tabs that the XML tree has changed.  */
  private void installationUpdated()
  {
    int size = tabs.size();
    for (int i = 0; i < size; i++)
    {
      FrontendTab tab = (FrontendTab) tabbedPane.getComponentAt(i);
      tab.installationUpdated(installation);
    }
  }


  /**  Makes a clean exit.  */
  protected void exit()
  {
    Frontend.saveConfig();

    System.exit(0);
  }


  /**  Updates the title text.  */
  private void updateTitle()
  {
    setTitle(langpack.getString("frontend.title") + " : [" +
      curFilename + "]");
  }


  /**  Shows the license text.  */
  protected void showLicence()
  {
    new FrontendLicence(this, langpack, icons);
  }


  /**  Shows the about box.  */
  protected void showAbout()
  {
    new FrontendAbout(this, langpack, icons);
  }


  /**  Makes a new file.  */
  protected void nnew()
  {
    installation = Frontend.createBlankInstallation();
    installationUpdated();
    curFilename = langpack.getString("frontend.untitled");
    updateTitle();
    basepath = "";
  }


  /**
   *  Imports a file with relative paths (not made with this frontend).
   */
  protected void iimport()
  {
    try
    {
      // We get the filename
      JFileChooser fileChooser = new JFileChooser(Frontend.lastDir);
      fileChooser.addChoosableFileFilter(fileChooser.getAcceptAllFileFilter());
      fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
      {
        // We get the base path
        Frontend.lastDir = fileChooser.getSelectedFile().getParentFile().getAbsolutePath();
        JFileChooser fileChooser2 = new JFileChooser(Frontend.lastDir);
        Frontend.lastDir = fileChooser.getSelectedFile().getAbsolutePath();
        fileChooser2.setDialogTitle(langpack.getString("frontend.basepath"));
        fileChooser2.addChoosableFileFilter(fileChooser2.getAcceptAllFileFilter());
        fileChooser2.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fileChooser2.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
          basepath = fileChooser2.getSelectedFile().getAbsolutePath();
        else
          return;

        // We load the file
        FileInputStream in = new FileInputStream(fileChooser.getSelectedFile());
        StdXMLParser parser = new StdXMLParser();
        parser.setBuilder(new StdXMLBuilder());
        parser.setReader(new StdXMLReader(in));
        parser.setValidator(new NonValidator());
        XMLElement data = (XMLElement) parser.parse();
        installation = data;
        installationUpdated();
        curFilename = fileChooser.getSelectedFile().getAbsolutePath();
        updateTitle();
        in.close();

        // Bookmarks
        String fpath = fileChooser.getSelectedFile().getAbsolutePath();
        int size = Frontend.bookmarks.size();
        boolean found = false;
        for (int i = 0; i < size; i++)
        {
          XMLElement el = (XMLElement) Frontend.bookmarks.get(i);
          if (el.getContent().equalsIgnoreCase(fpath))
            found = true;
        }
        if (found)
          return;
        XMLElement bookmark = new XMLElement("file");
        bookmark.setContent(fpath);
        bookmark.setAttribute("base", fileChooser2.getSelectedFile().getAbsolutePath());
        Frontend.bookmarks.add(0, bookmark);
        if (Frontend.bookmarks.size() == 5)
          Frontend.bookmarks.remove(4);
      }
    }
    catch (Exception err)
    {
      err.printStackTrace();
      JOptionPane.showMessageDialog(this, err.toString(),
        langpack.getString("frontend.error"),
        JOptionPane.ERROR_MESSAGE);
    }
  }


  /**
   *  Imports a bookmarked file.
   *
   * @param  file  The filename.
   */
  protected void bookmarkLoad(String file)
  {
    try
    {
      // We get the base path
      int index = -1;
      // We get the base path
      int size = Frontend.bookmarks.size();
      for (int i = 0; i < size; i++)
      {
        XMLElement e = (XMLElement) Frontend.bookmarks.get(i);
        if (e.getContent().equalsIgnoreCase(file))
          index = i;
      }
      if (index == -1)
        return;
      XMLElement el = (XMLElement) Frontend.bookmarks.get(index);
      basepath = el.getAttribute("base");

      // We load the file
      FileInputStream in = new FileInputStream(file);
      StdXMLParser parser = new StdXMLParser();
      parser.setBuilder(new StdXMLBuilder());
      parser.setReader(new StdXMLReader(in));
      parser.setValidator(new NonValidator());
      XMLElement data = (XMLElement) parser.parse();
      installation = data;
      installationUpdated();
      curFilename = file;
      updateTitle();
      in.close();
    }
    catch (Exception err)
    {
      err.printStackTrace();
      JOptionPane.showMessageDialog(this, err.toString(),
        langpack.getString("frontend.error"),
        JOptionPane.ERROR_MESSAGE);
    }
  }


  /**  Opens a file.  */
  protected void open()
  {
    try
    {
      // We get the filename
      JFileChooser fileChooser = new JFileChooser(Frontend.lastDir);
      fileChooser.addChoosableFileFilter(fileChooser.getAcceptAllFileFilter());
      fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
      {
        // We load the file
        Frontend.lastDir = fileChooser.getSelectedFile().getParentFile().getAbsolutePath();
        FileInputStream in = new FileInputStream(fileChooser.getSelectedFile());
        StdXMLParser parser = new StdXMLParser();
        parser.setBuilder(new StdXMLBuilder());
        parser.setReader(new StdXMLReader(in));
        parser.setValidator(new NonValidator());
        XMLElement data = (XMLElement) parser.parse();
        installation = data;
        installationUpdated();
        curFilename = fileChooser.getSelectedFile().getAbsolutePath();
        updateTitle();
        in.close();

        // Bookmarks
        String fpath = fileChooser.getSelectedFile().getAbsolutePath();
        int size = Frontend.bookmarks.size();
        boolean found = false;
        for (int i = 0; i < size; i++)
        {
          XMLElement el = (XMLElement) Frontend.bookmarks.get(i);
          if (el.getContent().equalsIgnoreCase(fpath))
            found = true;
        }
        if (found)
          return;
        XMLElement bookmark = new XMLElement("file");
        bookmark.setAttribute("base", "");
        bookmark.setContent(fpath);
        Frontend.bookmarks.add(0, bookmark);
        if (Frontend.bookmarks.size() == 5)
          Frontend.bookmarks.remove(4);
      }
    }
    catch (Exception err)
    {
      err.printStackTrace();
      JOptionPane.showMessageDialog(this, err.toString(),
        langpack.getString("frontend.error"),
        JOptionPane.ERROR_MESSAGE);
    }
  }


  /**  Saves the file.  */
  protected void save()
  {
    if (curFilename.equalsIgnoreCase(langpack.getString("frontend.untitled")))
      saveAs();
    else
      try
      {
        updateXMLTree();

        // We save the file
        FileOutputStream out = new FileOutputStream(curFilename);
        XMLWriter writer = new XMLWriter(out);
        writer.write(installation);
        out.close();
      }
      catch (Exception err)
      {
        err.printStackTrace();
        JOptionPane.showMessageDialog(this, err.toString(),
          langpack.getString("frontend.error"),
          JOptionPane.ERROR_MESSAGE);
      }
  }


  /**  Saves the file 'as ...'.  */
  protected void saveAs()
  {
    try
    {
      updateXMLTree();

      // We get the filename
      JFileChooser fileChooser = new JFileChooser(Frontend.lastDir);
      fileChooser.addChoosableFileFilter(fileChooser.getAcceptAllFileFilter());
      fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
      {
        Frontend.lastDir = fileChooser.getSelectedFile().getParentFile().getAbsolutePath();

        // We save the file
        FileOutputStream out = new FileOutputStream(fileChooser.getSelectedFile());
        XMLWriter writer = new XMLWriter(out);
        writer.write(installation);
        out.close();
        curFilename = fileChooser.getSelectedFile().getAbsolutePath();
        updateTitle();

        // Bookmarks
        String fpath = fileChooser.getSelectedFile().getAbsolutePath();
        int size = Frontend.bookmarks.size();
        boolean found = false;
        for (int i = 0; i < size; i++)
        {
          XMLElement el = (XMLElement) Frontend.bookmarks.get(i);
          if (el.getContent().equalsIgnoreCase(fpath))
            found = true;
        }
        if (found)
          return;
        XMLElement bookmark = new XMLElement("file");
        bookmark.setAttribute("base", basepath);
        bookmark.setContent(fpath);
        Frontend.bookmarks.add(0, bookmark);
        if (Frontend.bookmarks.size() == 5)
          Frontend.bookmarks.remove(4);
      }
    }
    catch (Exception err)
    {
      err.printStackTrace();
      JOptionPane.showMessageDialog(this, err.toString(),
        langpack.getString("frontend.error"),
        JOptionPane.ERROR_MESSAGE);
    }
  }


  /**
   *  Calls the compiler.
   *
   * @param  kind  The compiler kind.
   */
  protected void compile(String kind)
  {
    // We ensure that the file is saved
    if (curFilename.equalsIgnoreCase(langpack.getString("frontend.untitled")))
    {
      JOptionPane.showMessageDialog(this, langpack.getString("frontend.save_first"),
        langpack.getString("frontend.error"),
        JOptionPane.ERROR_MESSAGE);
      return;
    }
    save();

    // We get the desired output file
    String output;
    JFileChooser fileChooser = new JFileChooser((new File(curFilename)).getParentFile().getAbsolutePath());
    fileChooser.addChoosableFileFilter(fileChooser.getAcceptAllFileFilter());
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
      return;
    output = fileChooser.getSelectedFile().getAbsolutePath();

    // We make the compilation
    try
    {
      com.izforge.izpack.compiler.Compiler compiler =
        new com.izforge.izpack.compiler.Compiler(curFilename, basepath, kind, output);
	    com.izforge.izpack.compiler.Compiler.IZPACK_HOME = Frontend.IZPACK_HOME;
      compiler.setPackagerListener(new FrontendCompilerDialog(this, langpack, icons));
      compiler.compile();
    }
    catch (Exception err)
    {
      err.printStackTrace();
      JOptionPane.showMessageDialog(this, err.toString(),
        langpack.getString("frontend.error"),
        JOptionPane.ERROR_MESSAGE);
    }
  }


  /**
   *  Centers a window on screen.
   *
   * @param  frame  The window to center.
   */
  public static void centerFrame(Window frame)
  {
    Dimension frameSize = frame.getSize();
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setLocation((screenSize.width - frameSize.width) / 2,
      (screenSize.height - frameSize.height) / 2 - 10);
  }


  /**
   *  Sets the parameters of a GridBagConstraints object.
   *
   * @param  gbc  The constraints object.
   * @param  gx   The x grid coordinate.
   * @param  gy   The y grid coordinate.
   * @param  gw   The width (in cells units).
   * @param  wx   The x wheight.
   * @param  wy   The y wheight.
   * @param  gh   Description of the Parameter
   */
  public static void buildConstraints(GridBagConstraints gbc,
                                      int gx, int gy, int gw, int gh, double wx, double wy)
  {
    gbc.gridx = gx;
    gbc.gridy = gy;
    gbc.gridwidth = gw;
    gbc.gridheight = gh;
    gbc.weightx = wx;
    gbc.weighty = wy;
  }


  /**
   *  The window events handler.
   *
   * @author     julien
   * @created    October 26, 2002
   */
  class WindowHandler extends WindowAdapter
  {
    /**
     *  Called when the window is closed.
     *
     * @param  e  The event.
     */
    public void windowClosing(WindowEvent e)
    {
      exit();
    }
  }


  /**
   *  The files action events handler/
   *
   * @author     julien
   * @created    October 26, 2002
   */
  class FilesHandler implements ActionListener
  {
    /**
     *  Events handler.
     *
     * @param  e  The event.
     */
    public void actionPerformed(ActionEvent e)
    {
      // We get the action command string
      String command = e.getActionCommand();

      // We act depending of the string
      if (command.equalsIgnoreCase(langpack.getString("menu.exit")))
        exit();
      else if (command.equalsIgnoreCase(langpack.getString("menu.save")))
        save();
      else if (command.equalsIgnoreCase(langpack.getString("menu.save_as")))
        saveAs();
      else if (command.equalsIgnoreCase(langpack.getString("menu.open")))
        open();
      else if (command.equalsIgnoreCase(langpack.getString("menu.new")))
        nnew();
      else if (command.equalsIgnoreCase(langpack.getString("menu.import")))
        iimport();
      else
        bookmarkLoad(command);
    }
  }


  /**
   *  The compiler action events handler.
   *
   * @author     julien
   * @created    October 26, 2002
   */
  class CompilerHandler implements ActionListener
  {
    /**
     *  Events handler.
     *
     * @param  e  The event.
     */
    public void actionPerformed(ActionEvent e)
    {
      // We get the action command string
      String command = e.getActionCommand();

      // We see the installer kind requested
      String kind = "standard";
      if (command.equalsIgnoreCase(langpack.getString("menu.comp.standard")))
        kind = "standard";
      else if (command.equalsIgnoreCase(langpack.getString("menu.comp.standardk")))
        kind = "standard-kunststoff";
      else if (command.equalsIgnoreCase(langpack.getString("menu.comp.web")))
        kind = "web";
      else if (command.equalsIgnoreCase(langpack.getString("menu.comp.webk")))
        kind = "web-kunststoff";

      // We act
      compile(kind);
    }
  }


  /**
   *  The others action events handler.
   *
   * @author     julien
   * @created    October 26, 2002
   */
  class OthersHandler implements ActionListener
  {
    /**
     *  Events handler.
     *
     * @param  e  The event.
     */
    public void actionPerformed(ActionEvent e)
    {
      // We get the action command string
      String command = e.getActionCommand();

      if (command.equalsIgnoreCase(langpack.getString("menu.licence")))
        showLicence();
      else if (command.equalsIgnoreCase(langpack.getString("menu.about")))
        showAbout();
      else
      {
        // We change the langpack
        Frontend.config.getFirstChildNamed("langpack").setContent(command);
        JOptionPane.showMessageDialog(null, langpack.getString("frontend.nexttime"),
          langpack.getString("frontend.lpswitch"),
          JOptionPane.INFORMATION_MESSAGE);
      }
    }
  }
}

