/*
 * IzPack - Copyright 2001-2005 Julien Ponge, All Rights Reserved.
 * 
 * http://www.izforge.com/izpack/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2002 Jan Blok
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.installer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
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
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;

import net.n3.nanoxml.NonValidator;
import net.n3.nanoxml.StdXMLBuilder;
import net.n3.nanoxml.StdXMLParser;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLElement;
import net.n3.nanoxml.XMLWriter;

import com.izforge.izpack.CustomData;
import com.izforge.izpack.ExecutableFile;
import com.izforge.izpack.LocaleDatabase;
import com.izforge.izpack.Panel;
import com.izforge.izpack.gui.ButtonFactory;
import com.izforge.izpack.gui.EtchedLineBorder;
import com.izforge.izpack.gui.IconsDatabase;
import com.izforge.izpack.util.AbstractUIProgressHandler;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.OsConstraint;

/**
 * The IzPack installer frame.
 * 
 * @author Julien Ponge created October 27, 2002
 * @author Fabrice Mirabile added fix for alert window on cross button, July 06 2005
 */
public class InstallerFrame extends JFrame
{

    private static final long serialVersionUID = 3257852069162727473L;

    /** VM version to use version dependent methods calls */
    private static final float JAVA_SPECIFICATION_VERSION = Float.parseFloat(System
            .getProperty("java.specification.version"));

    /** The language pack. */
    public LocaleDatabase langpack;

    /** The installation data. */
    protected InstallData installdata;

    /** The icons database. */
    public IconsDatabase icons;

    /** The panels container. */
    protected JPanel panelsContainer;

    /** The frame content pane. */
    protected JPanel contentPane;

    /** The previous button. */
    protected JButton prevButton;

    /** The next button. */
    protected JButton nextButton;

    /** The quit button. */
    protected JButton quitButton;

    /** Image */
    private JLabel iconLabel;

    /** Count for discarded interrupt trials. */
    private int interruptCount = 1;

    /** Maximum of discarded interrupt trials. */
    private static final int MAX_INTERRUPT = 3;

    /**
     * The constructor (normal mode).
     * 
     * @param title The window title.
     * @param installdata The installation data.
     * @exception Exception Description of the Exception
     */
    public InstallerFrame(String title, InstallData installdata) throws Exception
    {
        super(title);
        this.installdata = installdata;
        this.langpack = installdata.langpack;

        // Sets the window events handler
        addWindowListener(new WindowHandler());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        // Builds the GUI
        loadIcons();
        loadPanels();
        buildGUI();

        // We show the frame
        showFrame();
        switchPanel(0);
    }

    /**
     * Loads the panels.
     * 
     * @exception Exception Description of the Exception
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
        Object[] params = { this, installdata};

        // We load each of them
        for (i = 0; i < size; i++)
        {
            // We add the panel
            Panel p = (Panel) panelsOrder.get(i);

            if (!OsConstraint.oneMatchesCurrentSystem(p.osConstraints)) continue;
            className = (String) p.className;
            String praefix = "com.izforge.izpack.panels.";
            if (className.compareTo(".") > -1)
            // Full qualified class name
                praefix = "";
            objectClass = Class.forName(praefix + className);
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
     * Loads the icons.
     * 
     * @exception Exception Description of the Exception
     */
    private void loadIcons() throws Exception
    {
        // Initialisations
        icons = new IconsDatabase();
        URL url;
        ImageIcon img;
        XMLElement icon;
        InputStream inXML = InstallerFrame.class
                .getResourceAsStream("/com/izforge/izpack/installer/icons.xml");

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
            url = InstallerFrame.class.getResource(icon.getAttribute("res"));
            img = new ImageIcon(url);
            icons.put(icon.getAttribute("id"), img);
        }

        // We load the Swing-specific icons
        children = data.getChildrenNamed("sysicon");
        size = children.size();
        for (int i = 0; i < size; i++)
        {
            icon = (XMLElement) children.get(i);
            url = InstallerFrame.class.getResource(icon.getAttribute("res"));
            img = new ImageIcon(url);
            UIManager.put(icon.getAttribute("id"), img);
        }
    }

    /** Builds the GUI. */
    private void buildGUI()
    {
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE); //patch 06/07/2005, Fabrice Mirabile
	    // Sets the frame icon
        setIconImage(icons.getImageIcon("JFrameIcon").getImage());

        // Prepares the glass pane to block the gui interaction when needed
        JPanel glassPane = (JPanel) getGlassPane();
        glassPane.addMouseListener(new MouseAdapter() {});
        glassPane.addMouseMotionListener(new MouseMotionAdapter() {});
        glassPane.addKeyListener(new KeyAdapter() {});
        glassPane.addFocusListener(new FocusAdapter() {});

        // We set the layout & prepare the constraint object
        contentPane = (JPanel) getContentPane();
        contentPane.setLayout(new BorderLayout()); // layout);

        // We add the panels container
        panelsContainer = new JPanel();
        panelsContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        panelsContainer.setLayout(new GridLayout(1, 1));
        contentPane.add(panelsContainer, BorderLayout.CENTER);

        // We put the first panel
        installdata.curPanelNumber = 0;
        IzPanel panel_0 = (IzPanel) installdata.panels.get(0);
        panelsContainer.add(panel_0);

        // We add the navigation buttons & labels

        NavigationHandler navHandler = new NavigationHandler();

        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.X_AXIS));
        navPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(8, 8,
                8, 8), BorderFactory.createTitledBorder(new EtchedLineBorder(), langpack
                .getString("installer.madewith")
                + " ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                new Font("Dialog", Font.PLAIN, 10))));
        navPanel.add(Box.createHorizontalGlue());

        prevButton = ButtonFactory.createButton(langpack.getString("installer.prev"), icons
                .getImageIcon("stepback"), installdata.buttonsHColor);
        navPanel.add(prevButton);
        prevButton.addActionListener(navHandler);

        navPanel.add(Box.createRigidArea(new Dimension(5, 0)));

        nextButton = ButtonFactory.createButton(langpack.getString("installer.next"), icons
                .getImageIcon("stepforward"), installdata.buttonsHColor);
        navPanel.add(nextButton);
        nextButton.addActionListener(navHandler);

        navPanel.add(Box.createRigidArea(new Dimension(5, 0)));

        quitButton = ButtonFactory.createButton(langpack.getString("installer.quit"), icons
                .getImageIcon("stop"), installdata.buttonsHColor);
        navPanel.add(quitButton);
        quitButton.addActionListener(navHandler);
        contentPane.add(navPanel, BorderLayout.SOUTH);

        try
        {
            ResourceManager rm = ResourceManager.getInstance();
            ImageIcon icon;
            try
            {
                icon = rm.getImageIconResource("Installer.image");
            }
            catch (Exception e) // This is not that clean ...
            {
                icon = rm.getImageIconResource("Installer.image.0");
            }
            if (icon != null)
            {
                JPanel imgPanel = new JPanel();
                imgPanel.setLayout(new BorderLayout());
                imgPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 0));
                iconLabel = new JLabel(icon);
                iconLabel.setBorder(BorderFactory.createLoweredBevelBorder());
                imgPanel.add(iconLabel, BorderLayout.NORTH);
                contentPane.add(imgPanel, BorderLayout.WEST);
            }
        }
        catch (Exception e)
        {
            // ignore
        }

        loadImage(0);
        getRootPane().setDefaultButton(nextButton);
    }

    private void loadImage(int panelNo)
    {
        try
        {
            ResourceManager rm = ResourceManager.getInstance();
            ImageIcon icon = rm.getImageIconResource("Installer.image." + panelNo);
            if (icon != null)
            {
                iconLabel.setVisible(false);
                iconLabel.setIcon(icon);
                iconLabel.setVisible(true);
            }
        }
        catch (Exception e)
        {
            // ignore
        }
    }

    /** Shows the frame. */
    private void showFrame()
    {
        pack();
        setSize(installdata.guiPrefs.width, installdata.guiPrefs.height);
        setResizable(installdata.guiPrefs.resizable);
        centerFrame(this);
        setVisible(true);
    }

    private boolean isBack = false;

    /**
     * Switches the current panel.
     * 
     * @param last Description of the Parameter
     */
    protected void switchPanel(int last)
    {
        try
        {
            if (installdata.curPanelNumber < last)
            {
                isBack = true;
            }
            panelsContainer.setVisible(false);
            IzPanel panel = (IzPanel) installdata.panels.get(installdata.curPanelNumber);
            IzPanel l_panel = (IzPanel) installdata.panels.get(last);
            //instead of writing data here which leads to duplicated entries in
            //auto-installation script (bug # 4551), let's make data only immediately before
            //writing out that script.
            //l_panel.makeXMLData(installdata.xmlData.getChildAtIndex(last));

            if (installdata.curPanelNumber == 0)
            {
                prevButton.setVisible(false);
                lockPrevButton();
                unlockNextButton(); // if we push the button back at the license
                // panel
            }
            else if (installdata.curPanelNumber == installdata.panels.size() - 1)
            {
                prevButton.setVisible(false);
                nextButton.setVisible(false);
                lockNextButton();

                // Set the default button to the only visible button.
                getRootPane().setDefaultButton(quitButton);
            }
            else
            {
                prevButton.setVisible(true);
                nextButton.setVisible(true);
                unlockPrevButton();
                unlockNextButton();
            }

            // Change panels container to the current one.
            panelsContainer.remove(l_panel);
            l_panel.panelDeactivate();
            panelsContainer.add(panel);

            if (panel.getInitialFocus() != null)
            { // Initial focus hint should be performed after current panel
                // was added to the panels container, else the focus hint will
                // be ignored.
                // Give a hint for the initial focus to the system.
                Component inFoc = panel.getInitialFocus();
                if (JAVA_SPECIFICATION_VERSION < 1.35)
                {
                    inFoc.requestFocus();
                }
                else
                {
                    inFoc.requestFocusInWindow();
                }

                /*
                 * On editable text components position the caret to the end of the cust existent
                 * text.
                 */
                if (inFoc instanceof JTextComponent)
                {
                    JTextComponent inText = (JTextComponent) inFoc;
                    if (inText.isEditable() && inText.getDocument() != null)
                    {
                        inText.setCaretPosition(inText.getDocument().getLength());
                    }
                }
            }
            panel.panelActivate();
            panelsContainer.setVisible(true);
            loadImage(installdata.curPanelNumber);
            isBack = false;
        }
        catch (Exception err)
        {
            err.printStackTrace();
        }
    }

    /** Writes the uninstalldata. */
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
                if (iter.hasNext()) logWriter.newLine();
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
                ExecutableFile file = (ExecutableFile) iter.next();
                execStream.writeObject(file);
            }
            execStream.flush();
            outJar.closeEntry();

            // Write out additional uninstall data
            // Do not "kill" the installation if there is a problem
            // with custom uninstall data. Therefore log it to Debug,
            // but do not throw.
            Map additionalData = udata.getAdditionalData();
            if (additionalData != null && !additionalData.isEmpty())
            {
                Iterator keys = additionalData.keySet().iterator();
                HashSet exist = new HashSet();
                while (keys != null && keys.hasNext())
                {
                    String key = (String) keys.next();
                    Object contents = additionalData.get(key);
                    if (key.equals("__uninstallLibs__"))
                    {
                        Iterator nativeLibIter = ((List) contents).iterator();
                        while (nativeLibIter != null && nativeLibIter.hasNext())
                        {
                            String nativeLibName = (String) ((List) nativeLibIter.next()).get(0);
                            byte[] buffer = new byte[5120];
                            long bytesCopied = 0;
                            int bytesInBuffer;
                            outJar.putNextEntry(new ZipEntry("native/" + nativeLibName));
                            InputStream in = getClass().getResourceAsStream(
                                    "/native/" + nativeLibName);
                            while ((bytesInBuffer = in.read(buffer)) != -1)
                            {
                                outJar.write(buffer, 0, bytesInBuffer);
                                bytesCopied += bytesInBuffer;
                            }
                            outJar.closeEntry();
                        }
                    }
                    else if (key.equals("uninstallerListeners") || key.equals("uninstallerJars"))
                    { // It is a ArrayList of ArrayLists which contains the
                        // full
                        // package paths of all needed class files.
                        // First we create a new ArrayList which contains only
                        // the full paths for the uninstall listener self; thats
                        // the first entry of each sub ArrayList.
                        ArrayList subContents = new ArrayList();

                        // Secound put the class into uninstaller.jar
                        Iterator listenerIter = ((List) contents).iterator();
                        while (listenerIter.hasNext())
                        {
                            byte[] buffer = new byte[5120];
                            long bytesCopied = 0;
                            int bytesInBuffer;
                            CustomData customData = (CustomData) listenerIter.next();
                            // First element of the list contains the listener
                            // class path;
                            // remind it for later.
                            if (customData.listenerName != null)
                                subContents.add(customData.listenerName);
                            Iterator liClaIter = customData.contents.iterator();
                            while (liClaIter.hasNext())
                            {
                                String contentPath = (String) liClaIter.next();
                                if (exist.contains(contentPath)) continue;
                                exist.add(contentPath);
                                try
                                {
                                    outJar.putNextEntry(new ZipEntry(contentPath));
                                }
                                catch (ZipException ze)
                                { // Ignore, or ignore not ?? May be it is a
                                    // exception because
                                    // a doubled entry was tried, then we should
                                    // ignore ...
                                    Debug.trace("ZipException in writing custom data: "
                                            + ze.getMessage());
                                    continue;
                                }
                                InputStream in = getClass().getResourceAsStream("/" + contentPath);
                                if (in != null)
                                {
                                    while ((bytesInBuffer = in.read(buffer)) != -1)
                                    {
                                        outJar.write(buffer, 0, bytesInBuffer);
                                        bytesCopied += bytesInBuffer;
                                    }
                                }
                                else
                                    Debug.trace("custom data not found: " + contentPath);
                                outJar.closeEntry();

                            }
                        }
                        // Third we write the list into the
                        // uninstaller.jar
                        outJar.putNextEntry(new ZipEntry(key));
                        ObjectOutputStream objOut = new ObjectOutputStream(outJar);
                        objOut.writeObject(subContents);
                        objOut.flush();
                        outJar.closeEntry();

                    }
                    else
                    {
                        outJar.putNextEntry(new ZipEntry(key));
                        if (contents instanceof ByteArrayOutputStream)
                        {
                            ((ByteArrayOutputStream) contents).writeTo(outJar);
                        }
                        else
                        {
                            ObjectOutputStream objOut = new ObjectOutputStream(outJar);
                            objOut.writeObject(contents);
                            objOut.flush();
                        }
                        outJar.closeEntry();
                    }
                }
            }

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
     * Gets the stream to a resource.
     * 
     * @param res The resource id.
     * @return The resource value, null if not found
     */
    public InputStream getResource(String res) throws Exception
    {
        InputStream result;
        String basePath = "";
        ResourceManager rm = null;

        try
        {
            rm = ResourceManager.getInstance();
            basePath = rm.resourceBasePath;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        result = this.getClass().getResourceAsStream(basePath + res);

        if (result == null) { throw new ResourceNotFoundException("Warning: Resource not found: "
                + res); }
        return result;
    }

    /**
     * Centers a window on screen.
     * 
     * @param frame The window tp center.
     */
    public void centerFrame(Window frame)
    {
        Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        Dimension frameSize = frame.getSize();
        frame.setLocation(center.x - frameSize.width / 2,
                center.y - frameSize.height / 2 - 10);
    }

    /**
     * Returns the panels container size.
     * 
     * @return The panels container size.
     */
    public Dimension getPanelsContainerSize()
    {
        return panelsContainer.getSize();
    }

    /**
     * Sets the parameters of a GridBagConstraints object.
     * 
     * @param gbc The constraints object.
     * @param gx The x coordinates.
     * @param gy The y coordinates.
     * @param gw The width.
     * @param wx The x wheight.
     * @param wy The y wheight.
     * @param gh Description of the Parameter
     */
    public void buildConstraints(GridBagConstraints gbc, int gx, int gy, int gw, int gh, double wx,
            double wy)
    {
        gbc.gridx = gx;
        gbc.gridy = gy;
        gbc.gridwidth = gw;
        gbc.gridheight = gh;
        gbc.weightx = wx;
        gbc.weighty = wy;
    }

    /** Makes a clean closing. */
    public void exit()
    {
        if (installdata.canClose)
        {
            // this does nothing if the uninstaller was not included
            writeUninstallData();
            Housekeeper.getInstance().shutDown(0);
        }
        else
        {
            // The installation is not over
            if (Unpacker.isDiscardInterrupt() && interruptCount < MAX_INTERRUPT)
            { // But we should not interrupt.
                interruptCount++;
                return;
            }
            int res = JOptionPane.showConfirmDialog(this, langpack
                    .getString("installer.quit.message"), langpack
                    .getString("installer.quit.title"), JOptionPane.YES_NO_OPTION);
            if (res == JOptionPane.YES_OPTION)
            {
                wipeAborted();
                Housekeeper.getInstance().shutDown(0);
            }
        }
    }

    /** Wipes the written files when you abort the installation. */
    protected void wipeAborted()
    {
        Iterator it;

        // We set interrupt to all running Unpacker and wait 40 sec for maximum.
        // If interrupt is discarded (return value false), return immediately:
        if (!Unpacker.interruptAll(40000)) return;

        // Wipes them all in 2 stages
        UninstallData u = UninstallData.getInstance();
        it = u.getFilesList().iterator();
        if (!it.hasNext()) return;
        while (it.hasNext())
        {
            String p = (String) it.next();
            File f = new File(p);
            f.delete();
        }
        String fullCleanup = installdata.getVariable("InstallerFrame.cleanAllAtInterrupt");
        if (fullCleanup == null || !fullCleanup.equalsIgnoreCase("no"))
            cleanWipe(new File(installdata.getInstallPath()));
    }

    /**
     * Recursive files wiper.
     * 
     * @param file The file to wipe.
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
     * Launches the installation.
     * 
     * @param listener The installation listener.
     */
    public void install(AbstractUIProgressHandler listener)
    {
        Unpacker unpacker = new Unpacker(installdata, listener);
        unpacker.start();
    }

    /**
     * Writes an XML tree.
     * 
     * @param root The XML tree to write out.
     * @param out The stream to write on.
     * @exception Exception Description of the Exception
     */
    public void writeXMLTree(XMLElement root, OutputStream out) throws Exception
    {
        XMLWriter writer = new XMLWriter(out);
        //fix bug# 4551
        //writer.write(root);
        for (int i = 0; i < installdata.panels.size(); i++)
        {
            IzPanel panel = (IzPanel)installdata.panels.get(i);
            panel.makeXMLData(installdata.xmlData.getChildAtIndex(i));
        }
        writer.write(installdata.xmlData);
    }

    /**
     * Changes the quit button text. If <tt>text</tt> is null, the default quit text is used.
     */
    public void setQuitButtonText(String text)
    {
        if (text == null) text = langpack.getString("installer.quit");
        quitButton.setText(text);
    }

    /*
     * FocusTraversalPolicy objects to handle keybord blocking; the declaration os Object allows to
     * use a pre version 1.4 VM.
     */
    private Object usualFTP = null;

    private Object blockFTP = null;

    /** Blocks GUI interaction. */
    public void blockGUI()
    {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        getGlassPane().setVisible(true);
        getGlassPane().setEnabled(true);
        // No traversal handling before VM version 1.4
        if (JAVA_SPECIFICATION_VERSION < 1.35) return;
        if (usualFTP == null) usualFTP = getFocusTraversalPolicy();
        if (blockFTP == null) blockFTP = new BlockFocusTraversalPolicy();
        setFocusTraversalPolicy((java.awt.FocusTraversalPolicy) blockFTP);
        getGlassPane().requestFocus();
    }

    /** Releases GUI interaction. */
    public void releaseGUI()
    {
        getGlassPane().setEnabled(false);
        getGlassPane().setVisible(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        // No traversal handling before VM version 1.4
        if (JAVA_SPECIFICATION_VERSION < 1.35) return;
        setFocusTraversalPolicy((java.awt.FocusTraversalPolicy) usualFTP);
    }

    /** Locks the 'previous' button. */
    public void lockPrevButton()
    {
        prevButton.setEnabled(false);
    }

    /** Locks the 'next' button. */
    public void lockNextButton()
    {
        nextButton.setEnabled(false);
    }

    /** Unlocks the 'previous' button. */
    public void unlockPrevButton()
    {
        prevButton.setEnabled(true);
    }

    /** Unlocks the 'next' button. */
    public void unlockNextButton()
    {
        nextButton.setEnabled(true);
        nextButton.requestFocus();
    }

    /** Allows a panel to ask to be skipped. */
    public void skipPanel()
    {
        if (installdata.curPanelNumber < installdata.panels.size() - 1)
        {
            if (isBack)
            {
                installdata.curPanelNumber--;
                switchPanel(installdata.curPanelNumber + 1);
            }
            else
            {
                installdata.curPanelNumber++;
                switchPanel(installdata.curPanelNumber - 1);
            }

        }
    }

    /** This function moves to the next panel */
    public void navigateNext()
    {
        // If the button is inactive this indicates that we cannot move
        // so we don't do the move
        if (!nextButton.isEnabled()) return;
        if ((installdata.curPanelNumber < installdata.panels.size() - 1)
                && ((IzPanel) installdata.panels.get(installdata.curPanelNumber)).isValidated())
        {
            installdata.curPanelNumber++;
            switchPanel(installdata.curPanelNumber - 1);
        }
    }

    /** This function moves to the previous panel */
    public void navigatePrevious()
    {
        // If the button is inactive this indicates that we cannot move
        // so we don't do the move
        if (!prevButton.isEnabled()) return;
        if ((installdata.curPanelNumber > 0))
        {
            installdata.curPanelNumber--;
            switchPanel(installdata.curPanelNumber + 1);
        }
    }

    /**
     * Handles the events from the navigation bar elements.
     * 
     * @author Julien Ponge
     */
    class NavigationHandler implements ActionListener
    {

        /**
         * Actions handler.
         * 
         * @param e The event.
         */
        public void actionPerformed(ActionEvent e)
        {
            Object source = e.getSource();
            if (source == prevButton)
            {
                navigatePrevious();
            }
            else if (source == nextButton)
            {
                navigateNext();
            }
            else if (source == quitButton) exit();

        }
    }

    /**
     * The window events handler.
     * 
     * @author julien created October 27, 2002
     */
	class WindowHandler extends WindowAdapter
	{
		/**
		* Window close is pressed, 
		* @param  e  The event.
		*/
		public void windowClosing(WindowEvent e)
		{
			// We ask for confirmation
			exit();
		}
		
		/** OLD VERSION
         * We can't avoid the exit here, so don't call exit anywhere else.
         * 
         * @param e The event.
         
        public void windowClosing(WindowEvent e)
        {
            if (Unpacker.isDiscardInterrupt() && interruptCount < MAX_INTERRUPT)
            { // But we should not interrupt.
                interruptCount++;
                return;
            }
            // We show an alert anyway
            if (!installdata.canClose)
                JOptionPane.showMessageDialog(null, langpack.getString("installer.quit.message"),
                        langpack.getString("installer.warning"), JOptionPane.ERROR_MESSAGE);
            wipeAborted();
            Housekeeper.getInstance().shutDown(0);
        }*/
	}

    /**
     * A FocusTraversalPolicy that only allows the block panel to have the focus
     */
    private class BlockFocusTraversalPolicy extends java.awt.DefaultFocusTraversalPolicy
    {

        private static final long serialVersionUID = 3258413928261169209L;

        /**
         * Only accepts the block panel
         * 
         * @param aComp the component to check
         * @return true if aComp is the block panel
         */
        protected boolean accept(Component aComp)
        {
            return aComp == getGlassPane();
        }
    }
}
