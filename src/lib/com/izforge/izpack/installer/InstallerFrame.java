/*
 * IzPack Version 3.0.0 pre4 (build 2002.06.15)
 * Copyright (C) 2001,2002 Julien Ponge
 *
 * File :               InstallerFrame.java
 * Description :        The Installer frame class.
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
import javax.swing.event.*;

import net.n3.nanoxml.*;

public class InstallerFrame extends JFrame
{
    //.....................................................................

    // The fields
    public  LocaleDatabase langpack;            // Contains the language pack
    private InstallData installdata;            // The installation data

    // The GUI fields
    public  IconsDatabase icons;                // The icons database
    private GridBagLayout layout;               // The layout
    private GridBagConstraints gbConstraints;   // The constraints for the layout
    private JPanel panelsContainer;             // The panels container
    private JPanel contentPane;                 // The frame content pane
    private HighlightJButton prevButton;        // The previous button
    private HighlightJButton nextButton;        // The next button
    private HighlightJButton quitButton;        // The quit button
    private JLabel madewithLabel;               // The 'made with izpack' label (please keep it !)

    // The constructor (normal mode)
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

    // The constructor (automated mode)
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
        Housekeeper.getInstance ().shutDown (0);
    }

    //.....................................................................
    // The methods

    // Runs the automated mode
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

    // Loads the panels
    private void loadPanels() throws Exception
    {
        // Initialisation
        java.util.List panelsOrder = installdata.panelsOrder;
        int i, size = panelsOrder.size();
        String className;
        Class objectClass;
        Constructor constructor;
        Object object;
        IzPanel panel;
        Class[] paramsClasses = new Class[2];
        paramsClasses[0] = Class.forName("com.izforge.izpack.installer.InstallerFrame");
        paramsClasses[1] = Class.forName("com.izforge.izpack.installer.InstallData");
        Object[] params = { this, installdata };

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

    // Loads the icons
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

    // Builds the GUI
    private void buildGUI()
    {
        // Sets the frame icon
        setIconImage(icons.getImageIcon("JFrameIcon").getImage());

        // Prepares the glass pane to block the gui interaction when needed
        JPanel glassPane = (JPanel) getGlassPane();
        glassPane.addMouseListener(new MouseAdapter() {} );
        glassPane.addMouseMotionListener(new MouseMotionAdapter() {} );
        glassPane.addKeyListener(new KeyAdapter() {} );

        // We set the layout & prepare the constraint object
        contentPane = (JPanel) getContentPane();
        layout = new GridBagLayout();
        contentPane.setLayout(layout);
        gbConstraints = new GridBagConstraints();
        gbConstraints.insets = new Insets(5,5,5,5);

        // We add the panels container
        panelsContainer = new JPanel();
        /*
        JScrollPane scroller = new JScrollPane(panelsContainer,
                                               JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                               JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        buildConstraints(gbConstraints, 0, 0, 4, 1, 1.0, 1.0);
        gbConstraints.anchor = GridBagConstraints.CENTER;
        gbConstraints.fill = GridBagConstraints.BOTH;
        layout.addLayoutComponent(scroller, gbConstraints);
        contentPane.add(scroller);
        */
        panelsContainer.setBorder(BorderFactory.createMatteBorder(
            0, 0, 1, 0, Color.gray));
        panelsContainer.setLayout(new GridLayout(1, 1));
        buildConstraints(gbConstraints, 0, 0, 4, 1, 1.0, 1.0);
        gbConstraints.anchor = GridBagConstraints.CENTER;
        gbConstraints.fill = GridBagConstraints.BOTH;
        layout.addLayoutComponent(panelsContainer, gbConstraints);
        contentPane.add(panelsContainer);

        // We put the first panel
        installdata.curPanelNumber = 0;
        IzPanel panel_0 = (IzPanel) installdata.panels.get(0);
        panelsContainer.add(panel_0);

        // We add the navigation buttons & labels

        NavigationHandler navHandler = new NavigationHandler();

        prevButton = new HighlightJButton(langpack.getString("installer.prev"),
                                          icons.getImageIcon("stepback"),
                                          installdata.buttonsHColor);
        buildConstraints(gbConstraints, 0, 1, 1, 1, 0.25, 0.0);
        gbConstraints.anchor = GridBagConstraints.SOUTHWEST;
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        layout.addLayoutComponent(prevButton, gbConstraints);
        contentPane.add(prevButton);
        prevButton.addActionListener(navHandler);

        madewithLabel = new JLabel(langpack.getString("installer.madewith"));
        buildConstraints(gbConstraints, 1, 1, 1, 1, 0.25, 0.0);
        gbConstraints.anchor = GridBagConstraints.SOUTH;
        gbConstraints.fill = GridBagConstraints.NONE;
        layout.addLayoutComponent(madewithLabel, gbConstraints);
        contentPane.add(madewithLabel);

        nextButton = new HighlightJButton(langpack.getString("installer.next"),
                                          icons.getImageIcon("stepforward"),
                                          installdata.buttonsHColor);
        buildConstraints(gbConstraints, 2, 1, 1, 1, 0.25, 0.0);
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        layout.addLayoutComponent(nextButton, gbConstraints);
        contentPane.add(nextButton);
        nextButton.addActionListener(navHandler);

        quitButton = new HighlightJButton(langpack.getString("installer.quit"),
                                          icons.getImageIcon("stop"),
                                          installdata.buttonsHColor);
        buildConstraints(gbConstraints, 3, 1, 1, 1, 0.25, 0.0);
        gbConstraints.anchor = GridBagConstraints.SOUTHEAST;
        layout.addLayoutComponent(quitButton, gbConstraints);
        contentPane.add(quitButton);
        quitButton.addActionListener(navHandler);
    }

    // Shows the frame
    private void showFrame()
    {
        pack();
        setSize(installdata.guiPrefs.width, installdata.guiPrefs.height);
        setResizable(installdata.guiPrefs.resizable);
        centerFrame(this);
        setVisible(true);
    }

    // Switches the current panel
    private void switchPanel(int last)
    {
        panelsContainer.setVisible(false);
        IzPanel panel = (IzPanel) installdata.panels.get(installdata.curPanelNumber);
        IzPanel l_panel = (IzPanel) installdata.panels.get(last);
        l_panel.makeXMLData(installdata.xmlData.getChildAtIndex(last));
        panelsContainer.remove(l_panel);
        panelsContainer.add( (JPanel) panel);
        if (installdata.curPanelNumber == 0)
        {
            lockPrevButton();
            unlockNextButton(); // if we push the button back at the license panel
        }
        else if (installdata.curPanelNumber == installdata.panels.size() - 1)
            lockNextButton();
        else
        {
            unlockPrevButton();
            unlockNextButton();
        }
        panel.panelActivate();
        panelsContainer.setVisible(true);
    }

    // Writes the uninstalldata
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
                logWriter.write( (String) iter.next());
                if (iter.hasNext()) logWriter.newLine();
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

    //.....................................................................
    // Usefull methods that can be called by the panels through their parent object

    // Gets the stream to a resource
    public InputStream getResource(String res) throws Exception
    {
        return getClass().getResourceAsStream("/res/" + res);
    }

    // Centers a window on screen
    public void centerFrame(Window frame)
    {
        Dimension frameSize = frame.getSize();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation( (screenSize.width - frameSize.width) / 2,
                           (screenSize.height - frameSize.height) / 2 - 10);
    }

    // Returns the panels container size
    public Dimension getPanelsContainerSize()
    {
        return panelsContainer.getSize();
    }

    // Sets the parameters of a GridBagConstraints object
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

    // Makes a clean closing
    public void exit()
    {
        if (installdata.canClose)
        {
            // Everything went well
            writeUninstallData();
            Housekeeper.getInstance ().shutDown (0);
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
                Housekeeper.getInstance ().shutDown (0);
            }
        }
    }

    // Launches the installation
    public void install(InstallListener listener)
    {
        Unpacker unpacker = new Unpacker(installdata, listener);
        unpacker.start();
    }

    // Writes an XML tree
    public void writeXMLTree(XMLElement root, OutputStream out) throws Exception
    {
        XMLWriter writer = new XMLWriter(out);
        writer.write(root);
    }

    // Blocks GUI interaction
    public void blockGUI()
    {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        getGlassPane().setVisible(true);
        getGlassPane().setEnabled(true);
    }

    // Releases GUI interaction
    public void releaseGUI()
    {
        getGlassPane().setEnabled(false);
        getGlassPane().setVisible(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    // Locks the 'previous' button
    public void lockPrevButton()
    {
        prevButton.setEnabled(false);
    }

    // Locks the 'next' button
    public void lockNextButton()
    {
        nextButton.setEnabled(false);
    }

    // Unlocks the 'previous' button
    public void unlockPrevButton()
    {
        prevButton.setEnabled(true);
    }

    // Unlocks the 'next' button
    public void unlockNextButton()
    {
        nextButton.setEnabled(true);
    }

    // Allows a panel to ask to be skipped
    public void skipPanel()
    {
        if (installdata.curPanelNumber < installdata.panels.size() - 1)
        {
            installdata.curPanelNumber++;
            switchPanel(installdata.curPanelNumber - 1);
        }
    }

    //.....................................................................
    // Some event handler classes

    // Handles the events from the navigation bar elements
    class NavigationHandler implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            Object source = e.getSource();
            if (source == prevButton)
            {
                if ( (installdata.curPanelNumber > 0) )
                {
                    installdata.curPanelNumber--;
                    switchPanel(installdata.curPanelNumber + 1);
                }
            } else
            if (source == nextButton)
            {
                if ( (installdata.curPanelNumber < installdata.panels.size() - 1) &&
                     ((IzPanel)installdata.panels.get(installdata.curPanelNumber)).isValidated() )
                {
                    installdata.curPanelNumber++;
                    switchPanel(installdata.curPanelNumber - 1);
                }
            } else
            if (source == quitButton)
            {
                exit();
            }
        }
    }

    // The window events handler
    class WindowHandler extends WindowAdapter
    {
        // We can't avoid the exit here ... so don't call exit
        public void windowClosing(WindowEvent e)
        {
            // We show an alert anyway
            if (!installdata.canClose)
            JOptionPane.showMessageDialog(null, langpack.getString("installer.quit.message"),
                                         langpack.getString("installer.warning"),
                                         JOptionPane.ERROR_MESSAGE);


            Housekeeper.getInstance ().shutDown (0);
        }
    }

    //.....................................................................

}
