package com.izforge.izpack.panels;

import com.izforge.izpack.*;
import com.izforge.izpack.gui.*;
import com.izforge.izpack.installer.*;

import java.awt.*;
import java.awt.event.*;

import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;

import net.n3.nanoxml.*;

public class RegPanel extends IzPanel implements ActionListener
{
    //.....................................................................

    // The fields


    private JLabel userLabel;
    private JLabel infoLabel;
    private JTextField userTextField;
    private JTextField textField;

    private GridBagLayout layout;

    private GridBagConstraints gbConstraints;




    // The constructor
    public RegPanel(InstallerFrame parent, InstallData idata)
    {
        super(parent, idata);

        // We initialize our layout
        layout = new GridBagLayout();
        gbConstraints = new GridBagConstraints();
        setLayout(layout);

        // We create and put the components
        JLabel headerLabel = new JLabel(parent.langpack.getString("RegPanel.header"));

        parent.buildConstraints(gbConstraints, 0, 0, 2, 1, 0.0, 0.0);
        gbConstraints.insets = new Insets(5, 5, 5, 5);
        gbConstraints.fill = GridBagConstraints.NONE;
        gbConstraints.anchor = GridBagConstraints.EAST;
        layout.addLayoutComponent(headerLabel, gbConstraints);
        add(headerLabel);

        userLabel = new JLabel(parent.langpack.getString("RegPanel.user"));

        parent.buildConstraints(gbConstraints, 0, 2, 1, 1, 0.0, 0.0);
        gbConstraints.insets = new Insets(5, 5, 5, 5);
        gbConstraints.fill = GridBagConstraints.NONE;
        gbConstraints.anchor = GridBagConstraints.EAST;
        layout.addLayoutComponent(userLabel, gbConstraints);
        add(userLabel);

        userTextField = new JTextField(null, 40);
        userTextField.addActionListener(this);
        parent.buildConstraints(gbConstraints, 1, 2, 1, 1, 0.0, 0.0);
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        gbConstraints.anchor = GridBagConstraints.WEST;
        layout.addLayoutComponent(userTextField, gbConstraints);
        add(userTextField);


        infoLabel = new JLabel(parent.langpack.getString("RegPanel.serialnumber"));

        parent.buildConstraints(gbConstraints, 0, 3, 1, 1, 0.0, 0.0);
        gbConstraints.insets = new Insets(5, 5, 5, 5);
        gbConstraints.fill = GridBagConstraints.NONE;
        gbConstraints.anchor = GridBagConstraints.EAST;
        layout.addLayoutComponent(infoLabel, gbConstraints);
        add(infoLabel);

        textField = new JTextField(null, 40);
        textField.addActionListener(this);
        parent.buildConstraints(gbConstraints, 1, 3, 1, 1, 0.0, 0.0);
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        gbConstraints.anchor = GridBagConstraints.WEST;
        layout.addLayoutComponent(textField, gbConstraints);
        add(textField);

        /*
        browseButton = new HighlightJButton(parent.langpack.getString("TargetPanel.browse"),
                                            parent.icons.getImageIcon("open"),
                                            idata.buttonsHColor);
        browseButton.addActionListener(this);
        parent.buildConstraints(gbConstraints, 1, 1, 1, 1, 1.0, 0.0);
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        gbConstraints.anchor = GridBagConstraints.EAST;
        layout.addLayoutComponent(browseButton, gbConstraints);
        add(browseButton);
        */
    }

    //.....................................................................
    // The methods

    /**
     * Loads up the "dir" resource associated with TargetPanel.
     * Acceptable dir resource names:
     *   <code>
     *   TargetPanel.dir.macosx
     *   TargetPanel.dir.mac
     *   TargetPanel.dir.windows
     *   TargetPanel.dir.unix
     *   TargetPanel.dir.xxx,
     *     where xxx is the lower case version of System.getProperty("os.name"),
     *     with any spaces replace with underscores
     *   TargetPanel.dir (generic that will be applied if none of above is found)
     *   </code>
     * As with all IzPack resources, each the above ids should be associated with
     * a separate filename, which is set in the install.xml file at compile time.
     */
    /*
    public void loadDefaultDir()
    {
    BufferedReader br = null;
    try
    {
        String os = System.getProperty("os.name");
        InputStream in = null;

        if (os.regionMatches(true, 0, "windows", 0, 7)) {
        in = parent.getResource("TargetPanel.dir.windows");

        } else if (os.regionMatches(true, 0, "macosx", 0, 6)) {
        in = parent.getResource("TargetPanel.dir.macosx");

        } else if (os.regionMatches(true, 0, "mac", 0, 3)) {
        in = parent.getResource("TargetPanel.dir.mac");

        } else {
        // first try to look up by specific os name
        os.replace(' ', '_');         // avoid spaces in file names
        os = os.toLowerCase();        // for consistency among TargetPanel res files
        in = parent.getResource("TargetPanel.dir.".concat(os));
        // if not specific os, try getting generic 'unix' resource file
        if (in == null) {
            in = parent.getResource("TargetPanel.dir.unix");
        }
        // if all those failed, try to look up a generic dir file
        if (in == null) {
            in = parent.getResource("TargetPanel.dir");
        }
        }

        // if all above tests failed, there is no resource file,
        // so use system default
        if (in == null) { return; }

        // now read the file, once we've identified which one to read
        InputStreamReader isr = new InputStreamReader(in);
        br = new BufferedReader(isr);
        String line = null;
        while ((line = br.readLine()) != null) {
        line = line.trim();
        // use the first non-blank line
        if ( ! line.equals("") ) {
            break;
        }
        }
        defaultDir = line;
    }
    catch (Exception e) {
        defaultDir = null;   // leave unset to take the system default set by Installer class
    }
    finally {
        try {
        if (br != null) { br.close(); }
        } catch(IOException ignored) { }
    }
    }
    */
    // Indicates wether the panel has been validated or not
    public boolean isValidated()
    {
        idata.user = userTextField.getText();
        idata.serialNumber = textField.getText();
        if (  isValidSerialNumber(idata.user, idata.serialNumber)) {
            return true;
        }
        else {
            JOptionPane.showMessageDialog(this, "The serial number is invalid.");
            return false;
        }
    }

    // Actions-handling method
    public void actionPerformed(ActionEvent e)
    {
        Object source = e.getSource();

        if (source == textField)
        {
            // The text has changed
            idata.serialNumber = textField.getText();
        }
        else if (source == userTextField)   {
            idata.user = userTextField.getText();
        }
    }

    // Asks to make the XML panel data
    public void makeXMLData(XMLElement panelRoot)
    {
        // Installation path markup

        XMLElement ipath = null;
        ipath = panelRoot.getFirstChildNamed("serialNumber");
        if (ipath != null) {
            ipath.setContent(idata.serialNumber);
        }
        else {
            ipath =  new XMLElement("serialNumber");
            panelRoot.addChild(ipath);
        }

        ipath = panelRoot.getFirstChildNamed("user");
        if (ipath != null) {
            ipath.setContent(idata.user);
        }
        else {
            ipath =  new XMLElement("user");
            panelRoot.addChild(ipath);
        }
    }

    // Asks to run in the automated mode
    public void runAutomated(XMLElement panelRoot)
    {
        // We set the installation path
        XMLElement ipath = panelRoot.getFirstChildNamed("serialNumber");
        idata.serialNumber = ipath.getContent();
        ipath = panelRoot.getFirstChildNamed("user");
        idata.user = ipath.getContent();
    }


    //.....................................................................
    protected boolean isValidSerialNumber(String  user, String serailNumber)
    {
        if (user.startsWith("xmlizer") && serailNumber.startsWith("2.0")) {
            return true;
        }
        return false;
    }
}