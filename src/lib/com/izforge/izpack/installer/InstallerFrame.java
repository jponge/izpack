/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2003 Julien Ponge
 *
 *  File :               InstallerFrame.java
 *  Description :        The Installer frame class.
 *  Author's email :     julien@izforge.com
 *  Author's Website :   http://www.izforge.com
 *
 *  Portions are Copyright (C) 2002 Jan Blok (jblok@profdata.nl - PDM - www.profdata.nl)
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

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

import net.n3.nanoxml.NonValidator;
import net.n3.nanoxml.StdXMLBuilder;
import net.n3.nanoxml.StdXMLParser;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLElement;
import net.n3.nanoxml.XMLWriter;

import com.izforge.izpack.ExecutableFile;
import com.izforge.izpack.LocaleDatabase;
import com.izforge.izpack.gui.ButtonFactory;
import com.izforge.izpack.gui.EtchedLineBorder;
import com.izforge.izpack.gui.IconsDatabase;
import com.izforge.izpack.util.AbstractUIProgressHandler;
import com.izforge.izpack.util.Housekeeper;

/**
 *  The IzPack installer frame.
 *
 * @author     Julien Ponge
 * created    October 27, 2002
 */
public class InstallerFrame extends JFrame
{
  /**  The language pack. */
  public LocaleDatabase langpack;

  /**  The installation data. */
  protected InstallData installdata;

  /**  The icons database. */
  public IconsDatabase icons;

  /**  The panels container. */
  protected JPanel panelsContainer;

  /**  The frame content pane. */
  protected JPanel contentPane;

  /**  The previous button. */
  protected JButton prevButton;

  /**  The next button. */
  protected JButton nextButton;

  /**  The quit button. */
  protected JButton quitButton;

  /**  The 'made with izpack' label, please KEEP IT THERE. */
  private JLabel madewithLabel;


  /**
   *  The constructor (normal mode).
   *
   * @param  title          The window title.
   * @param  langpack       The language pack.
   * @param  installdata    The installation data.
   * @exception  Exception  Description of the Exception
   */
  public InstallerFrame(String title, InstallData installdata)
     throws Exception
  {
    super(title);
    this.installdata = installdata;
    this.langpack = installdata.langpack;

    // Sets the window events handler
    addWindowListener(new WindowHandler());

    // Builds the GUI
    loadIcons();
    loadPanels();
    buildGUI();

    // We show the frame
    showFrame();
    switchPanel(0);
  }

  /**
   *  Loads the panels.
   *
   * @exception  Exception  Description of the Exception
   */
  private void loadPanels() throws Exception
  {
    // Initialisation
    java.util.List panelsOrder = installdata.panelsOrder;
    int i;
    int size = panelsOrder.size();
    String className;
    Class objectClass;
    Constructor constructor;
    Object object;
    IzPanel panel;
    Class[] paramsClasses = new Class[2];
    paramsClasses[0] = Class.forName("com.izforge.izpack.installer.InstallerFrame");
    paramsClasses[1] = Class.forName("com.izforge.izpack.installer.InstallData");
    Object[] params = {this, installdata};

    // We load each of them
    for (i = 0; i < size; i++)
    {
      // We add the panel
      className = (String) panelsOrder.get(i);
      objectClass = Class.forName("com.izforge.izpack.panels." + className);
      constructor = objectClass.getDeclaredConstructor(paramsClasses);
      object = constructor.newInstance(params);
      panel = (IzPanel) object;
      installdata.panels.add(panel);

      // We add the XML data panel root
      XMLElement panelRoot = new XMLElement(className);
      installdata.xmlData.addChild(panelRoot);
    }
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
    InputStream inXML = getClass().getResourceAsStream("/com/izforge/izpack/installer/icons.xml");

    // Initialises the parser
    StdXMLParser parser = new StdXMLParser();
    parser.setBuilder(new StdXMLBuilder());
    parser.setReader(new StdXMLReader(inXML));
    parser.setValidator(new NonValidator());

    // We get the data
    XMLElement data = (XMLElement) parser.parse();

    // We load the icons
    Vector children = data.getChildrenNamed("icon");
    int size = children.size();
    for (int i = 0; i < size; i++)
    {
      icon = (XMLElement) children.get(i);
      url = getClass().getResource(icon.getAttribute("res"));
      img = new ImageIcon(url);
      icons.put(icon.getAttribute("id"), img);
    }
    
    // We load the Swing-specific icons
    children = data.getChildrenNamed("sysicon");
    size = children.size();
    for (int i = 0; i < size; i++)
    {
      icon = (XMLElement) children.get(i);
      url = getClass().getResource(icon.getAttribute("res"));
      img = new ImageIcon(url);
      UIManager.put(icon.getAttribute("id"), img);
    }    
  }


  /**  Builds the GUI.  */
  private void buildGUI()
  {
    // Sets the frame icon
    setIconImage(icons.getImageIcon("JFrameIcon").getImage());

    // Prepares the glass pane to block the gui interaction when needed
    JPanel glassPane = (JPanel) getGlassPane();
    glassPane.addMouseListener(
      new MouseAdapter()
      {
      });
    glassPane.addMouseMotionListener(
      new MouseMotionAdapter()
      {
      });
    glassPane.addKeyListener(
      new KeyAdapter()
      {
      });

    // We set the layout & prepare the constraint object
    contentPane = (JPanel) getContentPane();
    contentPane.setLayout(new BorderLayout());//layout);

    // We add the panels container
    panelsContainer = new JPanel();
    panelsContainer.setBorder(BorderFactory.createEmptyBorder(10,10,0,10));
    panelsContainer.setLayout(new GridLayout(1, 1));
    contentPane.add(panelsContainer,BorderLayout.CENTER);

    // We put the first panel
    installdata.curPanelNumber = 0;
    IzPanel panel_0 = (IzPanel) installdata.panels.get(0);
    panelsContainer.add(panel_0);

    // We add the navigation buttons & labels

    NavigationHandler navHandler = new NavigationHandler();

    JPanel navPanel = new JPanel();
    navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.X_AXIS));
    navPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(8,8,8,8), BorderFactory.createTitledBorder(new EtchedLineBorder(), langpack.getString("installer.madewith")+" ")));
    navPanel.add(Box.createHorizontalGlue());

    prevButton = ButtonFactory.createButton(langpack.getString("installer.prev"),
      icons.getImageIcon("stepback"),
      installdata.buttonsHColor);
    navPanel.add(prevButton);
    prevButton.addActionListener(navHandler);

    navPanel.add(Box.createRigidArea(new Dimension(5, 0)));

    nextButton = ButtonFactory.createButton(langpack.getString("installer.next"),
      icons.getImageIcon("stepforward"),
      installdata.buttonsHColor);
    navPanel.add(nextButton);
    nextButton.addActionListener(navHandler);


    navPanel.add(Box.createRigidArea(new Dimension(5, 0)));

    quitButton = ButtonFactory.createButton(langpack.getString("installer.quit"),
      icons.getImageIcon("stop"),
      installdata.buttonsHColor);
    navPanel.add(quitButton);
    quitButton.addActionListener(navHandler);
    contentPane.add(navPanel,BorderLayout.SOUTH);

    try
    {
      ResourceManager rm = ResourceManager.getInstance();
      ImageIcon icon = rm.getImageIconResource("Installer.image");
      if (icon != null)
      {
        JPanel imgPanel = new JPanel();
        imgPanel.setLayout(new BorderLayout());
        imgPanel.setBorder(BorderFactory.createEmptyBorder(10,10,0,0));
        JLabel label = new JLabel(icon);
        label.setBorder(BorderFactory.createLoweredBevelBorder());
        imgPanel.add(label,BorderLayout.CENTER);
        contentPane.add(imgPanel,BorderLayout.WEST);
      }
    }
    catch (Exception e)
    {
      //ignore
    }

     getRootPane().setDefaultButton(nextButton);
  }


  /**  Shows the frame.  */
  private void showFrame()
  {
    pack();
    setSize(installdata.guiPrefs.width, installdata.guiPrefs.height);
    setResizable(installdata.guiPrefs.resizable);
    centerFrame(this);
    setVisible(true);
  }


  /**
   *  Switches the current panel.
   *
   * @param  last  Description of the Parameter
   */
  protected void switchPanel(int last)
  {
    panelsContainer.setVisible(false);
    IzPanel panel = (IzPanel) installdata.panels.get(installdata.curPanelNumber);
    IzPanel l_panel = (IzPanel) installdata.panels.get(last);
    l_panel.makeXMLData(installdata.xmlData.getChildAtIndex(last));
    panelsContainer.remove(l_panel);
    panelsContainer.add(panel);
    if (installdata.curPanelNumber == 0)
    {
      prevButton.setVisible(false);
      lockPrevButton();
      unlockNextButton();// if we push the button back at the license panel
    }
    else if (installdata.curPanelNumber == installdata.panels.size() - 1)
    {
      prevButton.setVisible(false);
      nextButton.setVisible(false);
      lockNextButton();
    }
    else
    {
      prevButton.setVisible(true);
      nextButton.setVisible(true);
      unlockPrevButton();
      unlockNextButton();
    }
    l_panel.panelDeactivate();
    panel.panelActivate();
    panelsContainer.setVisible(true);
  }


  /**  Writes the uninstalldata.  */
  private void writeUninstallData()
  {
    try
    {
      // We get the data
      UninstallData udata = UninstallData.getInstance();
      List files = udata.getFilesList();
      ZipOutputStream outJar = installdata.uninstallOutJar;

      if (outJar == null) return;
      
      // We write the files log
      outJar.putNextEntry(new ZipEntry("install.log"));
      BufferedWriter logWriter = new BufferedWriter(new OutputStreamWriter(outJar));
      logWriter.write(installdata.getInstallPath());
      logWriter.newLine();
      Iterator iter = files.iterator();
      while (iter.hasNext())
      {
        logWriter.write((String) iter.next());
        if (iter.hasNext())
          logWriter.newLine();
      }
      logWriter.flush();
      outJar.closeEntry();

      // We write the uninstaller jar file log
      outJar.putNextEntry(new ZipEntry("jarlocation.log"));
      logWriter = new BufferedWriter(new OutputStreamWriter(outJar));
      logWriter.write(udata.getUninstallerJarFilename());
      logWriter.newLine();
      logWriter.write(udata.getUninstallerPath());
      logWriter.flush();
      outJar.closeEntry();

      // Write out executables to execute on uninstall
      outJar.putNextEntry(new ZipEntry("executables"));
      ObjectOutputStream execStream = new ObjectOutputStream(outJar);
      iter = udata.getExecutablesList().iterator();
      execStream.writeInt(udata.getExecutablesList().size());
      while (iter.hasNext())
      {
        ExecutableFile file = (ExecutableFile)iter.next();
        execStream.writeObject(file);
      }
      execStream.flush();
      outJar.closeEntry();

      // Cleanup
      outJar.flush();
      outJar.close();
    }
    catch (Exception err)
    {
      err.printStackTrace();
    }
  }


  /**
   *  Gets the stream to a resource.
   *
   * @param  res            The resource id.
   * @return                The resource value, null if not found
   */
  public InputStream getResource(String res)
  {
    try
    {
      //System.out.println ("retrieving resource " + res);
      return ResourceManager.getInstance().getInputStream (res);
    }
    catch (ResourceNotFoundException e)
    {
      return null;
    }
  }


  /**
   *  Centers a window on screen.
   *
   * @param  frame  The window tp center.
   */
  public void centerFrame(Window frame)
  {
    Dimension frameSize = frame.getSize();
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setLocation((screenSize.width - frameSize.width) / 2,
      (screenSize.height - frameSize.height) / 2 - 10);
  }


  /**
   *  Returns the panels container size.
   *
   * @return    The panels container size.
   */
  public Dimension getPanelsContainerSize()
  {
    return panelsContainer.getSize();
  }


  /**
   *  Sets the parameters of a GridBagConstraints object.
   *
   * @param  gbc  The constraints object.
   * @param  gx   The x coordinates.
   * @param  gy   The y coordinates.
   * @param  gw   The width.
   * @param  wx   The x wheight.
   * @param  wy   The y wheight.
   * @param  gh   Description of the Parameter
   */
  public void buildConstraints(GridBagConstraints gbc,
                               int gx, int gy, int gw, int gh, double wx, double wy)
  {
    gbc.gridx = gx;
    gbc.gridy = gy;
    gbc.gridwidth = gw;
    gbc.gridheight = gh;
    gbc.weightx = wx;
    gbc.weighty = wy;
  }


  /**  Makes a clean closing.  */
  public void exit()
  {
    if (installdata.canClose)
    {
      // Everything went well
      if (installdata.info.getWriteUninstaller())
        writeUninstallData();
      Housekeeper.getInstance().shutDown(0);
    }
    else
    {
      // The installation is not over
      int res = JOptionPane.showConfirmDialog(this,
        langpack.getString("installer.quit.message"),
        langpack.getString("installer.quit.title"),
        JOptionPane.YES_NO_OPTION);
      if (res == JOptionPane.YES_OPTION)
      {
        wipeAborted();
        Housekeeper.getInstance().shutDown(0);
      }
    }
  }


  /**  Wipes the written files when you abort the installation.  */
  protected void wipeAborted()
  {
    Iterator it;

    // We check for running unpackers
    ArrayList unpackers = Unpacker.getRunningInstances();
    it = unpackers.iterator();
    while (it.hasNext())
    {
      Thread t = (Thread) it.next();
      t.interrupt();
      // The unpacker process might keep writing stuffs so we wait :-/
      try
      {
        Thread.sleep(3000, 0);
      }
      catch (Exception e)
      {}
    }

    // Wipes them all in 2 stages
    UninstallData u = UninstallData.getInstance();
    it = u.getFilesList().iterator();
    if (!it.hasNext())
      return;
    while (it.hasNext())
    {
      String p = (String) it.next();
      File f = new File(p);
      f.delete();
    }
    cleanWipe(new File(installdata.getInstallPath()));
  }


  /**
   *  Recursive files wiper.
   *
   * @param  file  The file to wipe.
   */
  private void cleanWipe(File file)
  {
    if (file.isDirectory())
    {
      File[] files = file.listFiles();
      int size = files.length;
      for (int i = 0; i < size; i++)
        cleanWipe(files[i]);
    }
    file.delete();
  }


  /**
   *  Launches the installation.
   *
   * @param  listener  The installation listener.
   */
  public void install(AbstractUIProgressHandler listener)
  {
    Unpacker unpacker = new Unpacker(installdata, listener);
    unpacker.start();
  }


  /**
   *  Writes an XML tree.
   *
   * @param  root           The XML tree to write out.
   * @param  out            The stream to write on.
   * @exception  Exception  Description of the Exception
   */
  public void writeXMLTree(XMLElement root, OutputStream out) throws Exception
  {
    XMLWriter writer = new XMLWriter(out);
    writer.write(root);
  }


  /**  Blocks GUI interaction.  */
  public void blockGUI()
  {
    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    getGlassPane().setVisible(true);
    getGlassPane().setEnabled(true);
  }


  /**  Releases GUI interaction.  */
  public void releaseGUI()
  {
    getGlassPane().setEnabled(false);
    getGlassPane().setVisible(false);
    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
  }


  /**  Locks the 'previous' button.  */
  public void lockPrevButton()
  {
    prevButton.setEnabled(false);
  }


  /**  Locks the 'next' button.  */
  public void lockNextButton()
  {
    nextButton.setEnabled(false);
  }


  /**  Unlocks the 'previous' button.  */
  public void unlockPrevButton()
  {
    prevButton.setEnabled(true);
  }


  /**  Unlocks the 'next' button.  */
  public void unlockNextButton()
  {
    nextButton.setEnabled(true);
    nextButton.requestFocus();
  }


  /**  Allows a panel to ask to be skipped.  */
  public void skipPanel()
  {
    if (installdata.curPanelNumber < installdata.panels.size() - 1)
    {
      installdata.curPanelNumber++;
      switchPanel(installdata.curPanelNumber - 1);
    }
  }


  /**
   *  Handles the events from the navigation bar elements.
   *
   * @author     Julien Ponge
   */
  class NavigationHandler implements ActionListener
  {
    /**
     *  Actions handler.
     *
     * @param  e  The event.
     */
    public void actionPerformed(ActionEvent e)
    {
      Object source = e.getSource();
      if (source == prevButton)
      {
        if ((installdata.curPanelNumber > 0))
        {
          installdata.curPanelNumber--;
          switchPanel(installdata.curPanelNumber + 1);
        }
      }
      else
        if (source == nextButton)
      {
        if ((installdata.curPanelNumber < installdata.panels.size() - 1) &&
          ((IzPanel) installdata.panels.get(installdata.curPanelNumber)).isValidated())
        {
          installdata.curPanelNumber++;
          switchPanel(installdata.curPanelNumber - 1);
        }
      }
      else
        if (source == quitButton)
        exit();

    }
  }


  /**
   *  The window events handler.
   *
   * @author     julien
   * created    October 27, 2002
   */
  class WindowHandler extends WindowAdapter
  {
    /**
     *  We can't avoid the exit here ... so don't call exit.
     *
     * @param  e  The event.
     */
    public void windowClosing(WindowEvent e)
    {
      // We show an alert anyway
      if (!installdata.canClose)
        JOptionPane.showMessageDialog(null, langpack.getString("installer.quit.message"),
          langpack.getString("installer.warning"),
          JOptionPane.ERROR_MESSAGE);
      wipeAborted();
      Housekeeper.getInstance().shutDown(0);
    }
  }
}

