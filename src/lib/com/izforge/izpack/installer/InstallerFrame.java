/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001,2002 Julien Ponge
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

import com.izforge.izpack.*;
import com.izforge.izpack.gui.*;
import com.izforge.izpack.util.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;
import java.util.zip.*;
import java.lang.reflect.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.*;

import net.n3.nanoxml.*;

/**
 *  The IzPack installer frame.
 *
 * @author     Julien Ponge
 * @created    October 27, 2002
 */
public class InstallerFrame extends JFrame
{
  /**  The language pack. */
  public LocaleDatabase langpack;

  /**  The installation data. */
  private InstallData installdata;

  /**  The icons database. */
  public IconsDatabase icons;

  /**  The panels container. */
  private JPanel panelsContainer;

  /**  The frame content pane. */
  private JPanel contentPane;

  /**  The previous button. */
  private JButton prevButton;

  /**  The next button. */
  private JButton nextButton;

  /**  The quit button. */
  private JButton quitButton;

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
  public InstallerFrame(String title, LocaleDatabase langpack, InstallData installdata)
     throws Exception
  {
    super(title);
    this.langpack = langpack;
    this.installdata = installdata;

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
   *  The constructor (automated mode)
   *
   * @param  langpack       The language pack.
   * @param  installdata    The installation data.
   * @exception  Exception  Description of the Exception
   */
  public InstallerFrame(LocaleDatabase langpack, InstallData installdata)
     throws Exception
  {
    super("IzPack - automated installation");
    this.langpack = langpack;
    this.installdata = installdata;

    // Loadings to make the panels able to be run properly
    loadIcons();
    loadPanels();
    buildGUI();
    switchPanel(0);

    // Runs the automated process
    runAutomation();

    // Bye
    Housekeeper.getInstance().shutDown(0);
  }


  /**
   *  Runs the automated mode.
   *
   * @exception  Exception  Description of the Exception
   */
  private void runAutomation() throws Exception
  {
    // Echoes a start message
    System.out.println("[ Running automated installation ... ]");

    // We process each panel
    int size = installdata.panels.size();
    for (int i = 0; i < size; i++)
    {
      // We get the panel
      IzPanel panel = (IzPanel) installdata.panels.get(i);
      String className = (String) installdata.panelsOrder.get(i);

      // We get its root xml markup
      XMLElement panelRoot = installdata.xmlData.getFirstChildNamed(className);

      // We invoke it
      panel.runAutomated(panelRoot);
    }

    // Echoes a end message
    System.out.println("[ Automated installation done ]");
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
		ResourceManager rm = new ResourceManager(installdata);
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
  private void switchPanel(int last)
  {
    panelsContainer.setVisible(false);
    IzPanel panel = (IzPanel) installdata.panels.get(installdata.curPanelNumber);
    IzPanel l_panel = (IzPanel) installdata.panels.get(last);
    l_panel.makeXMLData(installdata.xmlData.getChildAtIndex(last));
    panelsContainer.remove(l_panel);
    panelsContainer.add((JPanel) panel);
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
      ArrayList files = udata.getFilesList();
      ZipOutputStream outJar = installdata.uninstallOutJar;

      // We write the files log
      outJar.putNextEntry(new ZipEntry("install.log"));
      BufferedWriter logWriter = new BufferedWriter(new OutputStreamWriter(outJar));
      int size = files.size();
      int lim = size - 1;
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
      logWriter.write(udata.getUninstallerJarFilename());
      logWriter.newLine();
      logWriter.write(udata.getUninstallerPath());
      logWriter.flush();
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
   * @return                The resource value
   * @exception  Exception  Description of the Exception
   */
  public InputStream getResource(String res) throws Exception
  {
    return getClass().getResourceAsStream("/res/" + res);
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
  private void wipeAborted()
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
    cleanWipe(new File(InstallData.getInstance().getInstallPath()));
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
  public void install(InstallListener listener)
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
   * @author     julien
   * @created    October 27, 2002
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
   * @created    October 27, 2002
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

