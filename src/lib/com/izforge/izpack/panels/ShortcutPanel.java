/*
 * IzPack - Copyright 2001-2005 Julien Ponge, All Rights Reserved.
 * 
 * http://www.izforge.com/izpack/ 
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2002 Elmar Grom
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.izforge.izpack.panels;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.n3.nanoxml.NonValidator;
import net.n3.nanoxml.StdXMLBuilder;
import net.n3.nanoxml.StdXMLParser;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLElement;

import com.izforge.izpack.ExecutableFile;
import com.izforge.izpack.Pack;
import com.izforge.izpack.gui.ButtonFactory;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.installer.ResourceNotFoundException;
import com.izforge.izpack.installer.UninstallData;
import com.izforge.izpack.util.FileExecutor;
import com.izforge.izpack.util.MultiLineLabel;
import com.izforge.izpack.util.OsConstraint;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.StringTool;
import com.izforge.izpack.util.TargetFactory;
import com.izforge.izpack.util.UnixHelper;
import com.izforge.izpack.util.VariableSubstitutor;
import com.izforge.izpack.util.os.Shortcut;

//
//import com.izforge.izpack.panels.ShortcutData;

/*---------------------------------------------------------------------------*/

/**
 * This class implements a panel for the creation of shortcuts. The panel prompts the user to select
 * a program group for shortcuts, accept the creation of desktop shortcuts and actually creates the
 * shortcuts.
 * 
 * <h4>Important</h4>
 * It is neccesary that the installation has been completed before this panel is called. To
 * successfully create shortcuts this panel needs to have the following in place: <br>
 * <br>
 * 
 * <ul>
 * <li>the launcher files that the shortcuts point to must exist</li>
 * <li>it must be known which packs are installed</li>
 * <li>where the launcher for the uninstaller is located</li>
 * </ul>
 * 
 * It is ok to present other panels after this one, as long as these conditions are met.
 * 
 * @author Elmar Grom
 * @version 0.0.1 / 2/26/02
 * 
 * @see com.izforge.izpack.util.os.ShellLink
 * 
 * @version 0.0.1 / 2/26/02
 * @author Elmar Grom
 */

/*---------------------------------------------------------------------------*/

// !!! To Do !
//
// - see if I can't get multiple instances of the shortcut to work
// - need a clean way to get pack name
public class ShortcutPanel extends IzPanel implements ActionListener, ListSelectionListener
{
    /** 	 
     * 	 
     */ 	 
    private static final long serialVersionUID = 3256722870838112311L;
    
    /** a VectorList of Files wich should be make executable */
    private Vector execFiles = new Vector();

    /** SPEC_ATTRIBUTE_KDE_SUBST_UID = "KdeSubstUID" */
    private final static String SPEC_ATTRIBUTE_KDE_SUBST_UID = "KdeSubstUID";

    /** SPEC_ATTRIBUTE_URL = "url" */
    private final static String SPEC_ATTRIBUTE_URL = "url";

    /** SPEC_ATTRIBUTE_TYPE = "type" */
    private final static String SPEC_ATTRIBUTE_TYPE = "type";

    /** SPEC_ATTRIBUTE_TERMINAL_OPTIONS = "terminalOptions" */
    private final static String SPEC_ATTRIBUTE_TERMINAL_OPTIONS = "terminalOptions";

    /** SPEC_ATTRIBUTE_TERMINAL = "terminal" */
    private final static String SPEC_ATTRIBUTE_TERMINAL = "terminal";

    /** SPEC_ATTRIBUTE_MIMETYPE = "mimetype" */
    private final static String SPEC_ATTRIBUTE_MIMETYPE = "mimetype";

    /** SPEC_ATTRIBUTE_ENCODING = "encoding" */
    private final static String SPEC_ATTRIBUTE_ENCODING = "encoding";

    /** LOCATION_APPLICATIONS=applications */
    private static final String LOCATION_APPLICATIONS = "applications";

    /** LOCATION_START_MENU = "startMenu" */
    private static final String LOCATION_START_MENU = "startMenu";

    /**
     * SEPARATOR_LINE =
     * "--------------------------------------------------------------------------------";
     */
    private static final String SEPARATOR_LINE = "--------------------------------------------------------------------------------";

    /**
     * The default file name for the text file in which the shortcut information should be stored,
     * in case shortcuts can not be created on a particular target system. TEXT_FILE_NAME =
     * "Shortcuts.txt"
     */
    private static final String TEXT_FILE_NAME = "Shortcuts.txt";

    /** The name of the XML file that specifies the shortcuts SPEC_FILE_NAME = "shortcutSpec.xml"; */
    private static final String SPEC_FILE_NAME = "shortcutSpec.xml";

    // ------------------------------------------------------
    // spec file section keys
    // ------------------------------------------------------
    private static final String SPEC_KEY_SKIP_IFNOT_SUPPORTED = "skipIfNotSupported";

    /** SPEC_KEY_NOT_SUPPORTED = "notSupported" */
    private static final String SPEC_KEY_NOT_SUPPORTED = "notSupported";

    /** SPEC_KEY_PROGRAM_GROUP = "programGroup" */
    private static final String SPEC_KEY_PROGRAM_GROUP = "programGroup";

    /** SPEC_KEY_SHORTCUT = "shortcut" */
    private static final String SPEC_KEY_SHORTCUT = "shortcut";

    /** SPEC_KEY_PACKS = "createForPack" */
    private static final String SPEC_KEY_PACKS = "createForPack";

    // ------------------------------------------------------
    // spec file key attributes
    // ------------------------------------------------------

    /** SPEC_ATTRIBUTE_DEFAULT_GROUP = "defaultName" */
    private static final String SPEC_ATTRIBUTE_DEFAULT_GROUP = "defaultName";

    /** SPEC_ATTRIBUTE_LOCATION = "location" */
    private static final String SPEC_ATTRIBUTE_LOCATION = "location";

    /** SPEC_ATTRIBUTE_NAME = "name" */
    private static final String SPEC_ATTRIBUTE_NAME = "name";

    /** SPEC_ATTRIBUTE_SUBGROUP = "subgroup" */
    private static final String SPEC_ATTRIBUTE_SUBGROUP = "subgroup";

    /** SPEC_ATTRIBUTE_DESCRIPTION = "description" */
    private static final String SPEC_ATTRIBUTE_DESCRIPTION = "description";

    /** SPEC_ATTRIBUTE_TARGET = "target" */
    private static final String SPEC_ATTRIBUTE_TARGET = "target";

    /** SPEC_ATTRIBUTE_COMMAND = "commandLine" */
    private static final String SPEC_ATTRIBUTE_COMMAND = "commandLine";

    /** SPEC_ATTRIBUTE_ICON "iconFile" */
    private static final String SPEC_ATTRIBUTE_ICON = "iconFile";

    /** SPEC_ATTRIBUTE_ICON_INDEX "iconIndex" */
    private static final String SPEC_ATTRIBUTE_ICON_INDEX = "iconIndex";

    /** SPEC_ATTRIBUTE_WORKING_DIR = "workingDirectory" */
    private static final String SPEC_ATTRIBUTE_WORKING_DIR = "workingDirectory";

    /** SPEC_ATTRIBUTE_INITIAL_STATE = "initialState" */
    private static final String SPEC_ATTRIBUTE_INITIAL_STATE = "initialState";

    /** SPEC_ATTRIBUTE_DESKTOP = "desktop" */
    private static final String SPEC_ATTRIBUTE_DESKTOP = "desktop";

    /** SPEC_ATTRIBUTE_APPLICATIONS = "applications" */
    private static final String SPEC_ATTRIBUTE_APPLICATIONS = "applications";

    /** SPEC_ATTRIBUTE_START_MENU = "startMenu" */
    private static final String SPEC_ATTRIBUTE_START_MENU = "startMenu";

    /** SPEC_ATTRIBUTE_STARTUP = "startup" */
    private static final String SPEC_ATTRIBUTE_STARTUP = "startup";

    /** SPEC_ATTRIBUTE_PROGRAM_GROUP = "programGroup" */
    private static final String SPEC_ATTRIBUTE_PROGRAM_GROUP = "programGroup";

    // ------------------------------------------------------
    // spec file attribute values
    // ------------------------------------------------------

    /** SPEC_VALUE_APPLICATIONS = "applications" */
    private static final String SPEC_VALUE_APPLICATIONS = "applications";

    /** SPEC_VALUE_START_MENU = "startMenu" */
    private static final String SPEC_VALUE_START_MENU = "startMenu";

    /** SPEC_VALUE_NO_SHOW = "noShow" */
    private static final String SPEC_VALUE_NO_SHOW = "noShow";

    /** SPEC_VALUE_NORMAL = "normal" */
    private static final String SPEC_VALUE_NORMAL = "normal";

    /** SPEC_VALUE_MAXIMIZED = "maximized" */
    private static final String SPEC_VALUE_MAXIMIZED = "maximized";

    /** SPEC_VALUE_MINIMIZED = "minimized" */
    private static final String SPEC_VALUE_MINIMIZED = "minimized";

    // ------------------------------------------------------
    // automatic script section keys
    // ------------------------------------------------------

    /**   */

    /** AUTO_KEY_PROGRAM_GROUP = "programGroup" */
    private static final String AUTO_KEY_PROGRAM_GROUP = "programGroup";

    /** AUTO_KEY_SHORTCUT = "shortcut" */
    private static final String AUTO_KEY_SHORTCUT = "shortcut";

    // ------------------------------------------------------
    // automatic script keys attributes
    // ------------------------------------------------------

    /** AUTO_ATTRIBUTE_NAME = "name" */
    private static final String AUTO_ATTRIBUTE_NAME = "name";

    /** AUTO_ATTRIBUTE_GROUP = "group" */
    private static final String AUTO_ATTRIBUTE_GROUP = "group";

    /** AUTO_ATTRIBUTE_TYPE "type" */
    private static final String AUTO_ATTRIBUTE_TYPE = "type";

    /** AUTO_ATTRIBUTE_COMMAND = "commandLine" */
    private static final String AUTO_ATTRIBUTE_COMMAND = "commandLine";

    /** AUTO_ATTRIBUTE_DESCRIPTION = "description" */
    private static final String AUTO_ATTRIBUTE_DESCRIPTION = "description";

    /** AUTO_ATTRIBUTE_ICON = "icon" */
    private static final String AUTO_ATTRIBUTE_ICON = "icon";

    /** AUTO_ATTRIBUTE_ICON_INDEX = "iconIndex" */
    private static final String AUTO_ATTRIBUTE_ICON_INDEX = "iconIndex";

    /** AUTO_ATTRIBUTE_INITIAL_STATE = "initialState" */
    private static final String AUTO_ATTRIBUTE_INITIAL_STATE = "initialState";

    /** AUTO_ATTRIBUTE_TARGET = "target" */
    private static final String AUTO_ATTRIBUTE_TARGET = "target";

    /** AUTO_ATTRIBUTE_WORKING_DIR = "workingDirectory" */
    private static final String AUTO_ATTRIBUTE_WORKING_DIR = "workingDirectory";

    // permission flags
    private static final String CREATE_FOR_ALL = "createForAll";

    // ------------------------------------------------------------------------
    // Variable Declarations
    // ------------------------------------------------------------------------

    /** UI element to label the list of existing program groups */
    private JLabel listLabel;

    /** UI element to present the list of existing program groups for selection */
    private JList groupList;

    /** UI element for listing the intended shortcut targets */
    private JList targetList;

    /**
     * UI element to present the default name for the program group and to support editing of this
     * name.
     */
    private JTextField programGroup;

    /**
     * UI element to allow the user to revert to the default name of the program group
     */
    private JButton defaultButton;

    /**
     * UI element to allow the user to save a text file with the shortcut information
     */
    private JButton saveButton;

    /**
     * UI element to allow the user to decide if shortcuts should be placed on the desktop or not.
     */
    private JCheckBox allowDesktopShortcut;

    private JCheckBox createShortcuts;

    /**
     * UI element instruct this panel to create shortcuts for the current user only
     */
    private JRadioButton currentUser;

    /** UI element instruct this panel to create shortcuts for all users */
    private JRadioButton allUsers;

    /** The layout for this panel */
    private GridBagLayout layout;

    /** The contraints object to use whan creating the layout */
    private GridBagConstraints constraints;

    /**
     * The default name to use for the program group. This comes from the XML specification.
     */
    private String suggestedProgramGroup;

    /** The name chosen by the user for the program group, */
    private String groupName;

    /**
     * The location for placign the program group. This is the same as the location (type) of a
     * shortcut, only that it applies to the program group. Note that there are only two locations
     * that make sense as location for a program group: <br>
     * 
     * <ul>
     * <li>applications</li>
     * <li>start manu</li>
     * </ul>
     *  
     */
    private int groupLocation;

    /** The parsed result from reading the XML specification from the file */
    private XMLElement spec;

    /**
     * Set to <code>true</code> by <code>analyzeShortcutSpec()</code> if there are any desktop
     * shortcuts to create.
     */
    private boolean hasDesktopShortcuts = false;

    /** Tells wether to skip if the platform is not supported. */
    private boolean skipIfNotSupported = false;

    /** the one shortcut instance for reuse in many locations */
    private Shortcut shortcut;

    /**
     * A list of <code>ShortcutData</code> objects. Each object is the complete specification for
     * one shortcut that must be created.
     */
    private Vector shortcuts = new Vector();

    /**
     * Holds a list of all the shortcut files that have been created. <b>Note: </b> this variable
     * contains valid data only after <code>createShortcuts()</code> has been called. This list is
     * created so that the files can be added to the uninstaller.
     */
    private Vector files = new Vector();

    /**
     * If <code>true</code> it indicates that there are shortcuts to create. The value is set by
     * <code>analyzeShortcutSpec()</code>
     */
    private boolean shortcutsToCreate = false;

    /**
     * If <code>true</code> it indicates that the spec file is existing and could be read.
     */
    private boolean haveShortcutSpec = false;

    /**
     * This is set to true if the shortcut spec instructs to simulate running on an operating system
     * that is not supported.
     */
    private boolean simulteNotSupported = false;

    /**
     * Avoids bogus behaviour when the user goes back then returns to this panel.
     */
    private boolean firstTime = true;

    private File itsProgramFolder;

    private int itsUserType;

    static boolean create;
    
    private static boolean isRootUser;

    /*
     * --------------------------------------------------------------------------
     */
    /**
     * Constructor.
     * 
     * @param parent reference to the application frame
     * @param installData shared information about the installation
     */
    /*
     * --------------------------------------------------------------------------
     */
    public ShortcutPanel(InstallerFrame parent, InstallData installData)
    {
        super(parent, installData, "link16x16");
        
        try
        {
            readShortcutSpec();
        }
        catch (Throwable exception)
        {
            System.out.println("could not read shortcut spec!");
            exception.printStackTrace();
        }

        layout = new GridBagLayout();
        constraints = new GridBagConstraints();
        setLayout(layout);

        // Create the UI elements
        try
        {
            shortcut = (Shortcut) (TargetFactory.getInstance()
                    .makeObject("com.izforge.izpack.util.os.Shortcut"));
            shortcut.initialize(Shortcut.APPLICATIONS, "-");
        }
        catch (Throwable exception)
        {
            System.out.println("could not create shortcut instance");
            exception.printStackTrace();
        }
    }

    //~ Methods
    // **************************************************************************************************************************************************

    /*--------------------------------------------------------------------------*/
    /**
     * This method represents the <code>ActionListener</code> interface, invoked when an action
     * occurs.
     * 
     * @param event the action event.
     */
    /*--------------------------------------------------------------------------*/
    public void actionPerformed(ActionEvent event)
    {
        Object eventSource = event.getSource();

        // ----------------------------------------------------
        // create shortcut for the current user was selected
        // refresh the list of program groups accordingly and
        // reset the program group to the default setting.
        // ----------------------------------------------------
        if (eventSource.equals(currentUser))
        {
            groupList.setListData(shortcut.getProgramGroups(Shortcut.CURRENT_USER));
            programGroup.setText(suggestedProgramGroup);
            shortcut.setUserType(itsUserType = Shortcut.CURRENT_USER);
        }
        // ----------------------------------------------------
        // create shortcut for all users was selected
        // refresh the list of program groups accordingly and
        // reset the program group to the default setting.
        // ----------------------------------------------------
        else if (eventSource.equals(allUsers))
        {
            groupList.setListData(shortcut.getProgramGroups(Shortcut.ALL_USERS));
            programGroup.setText(suggestedProgramGroup);
            shortcut.setUserType(itsUserType = Shortcut.ALL_USERS);
        }
        // ----------------------------------------------------
        // The reset button was pressed.
        // - clear the selection in the list box, because the
        //   selection is no longer valid
        // - refill the program group edit control with the
        //   suggested program group name
        // ----------------------------------------------------
        else if (eventSource.equals(defaultButton))
        {
            groupList.getSelectionModel().clearSelection();
            programGroup.setText(suggestedProgramGroup);
        }
        // ----------------------------------------------------
        // the save button was pressed. This is a request to
        // save shortcut information to a text file.
        // ----------------------------------------------------
        else if (eventSource.equals(saveButton))
        {
            saveToFile();

            // add the file to the uninstaller
            addToUninstaller();
        }
        else if (eventSource.equals(createShortcuts))
        {
            create = createShortcuts.isSelected();

            groupList.setEnabled(create);
            programGroup.setEnabled(create);
            currentUser.setEnabled(create);            
            defaultButton.setEnabled(create);
            allowDesktopShortcut.setEnabled(create);
            if( isRootUser )
                allUsers.setEnabled(create);
        }
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Returns <code>true</code> when all selections have valid settings. This indicates that it
     * is legal to procede to the next panel.
     * 
     * @return <code>true</code> if it is legal to procede to the next panel, otherwise
     * <code>false</code>.
     */
    /*--------------------------------------------------------------------------*/
    public boolean isValidated()
    {
        try
        {
            groupName = programGroup.getText();
        }
        catch (Throwable exception)
        {
            groupName = "";
        }
        create = createShortcuts.isSelected();

        createShortcuts();

        // add files and directories to the uninstaller
        addToUninstaller();

        return (true);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Called when the panel is shown to the user.
     */
    /*--------------------------------------------------------------------------*/
    public void panelActivate()
    {
        if (firstTime)
            firstTime = false;
        else
            return;

        analyzeShortcutSpec();

        if (shortcutsToCreate)
        {
            if (shortcut.supported() && !simulteNotSupported)
            {
                File allUsersProgramsFolder = getProgramsFolder(Shortcut.ALL_USERS);

                isRootUser = allUsersProgramsFolder.canWrite();

                if (isRootUser)
                    itsUserType = Shortcut.ALL_USERS;
                else
                    itsUserType = Shortcut.CURRENT_USER;

                buildUI( getProgramsFolder(isRootUser ? Shortcut.ALL_USERS : Shortcut.CURRENT_USER) );
            }
            else
            {
                if (skipIfNotSupported)
                {
                    parent.skipPanel();
                }
                else
                {
                    buildAlternateUI();
                    parent.unlockNextButton();
                    parent.lockPrevButton();
                }
            }
        }
        else
        {
          parent.skipPanel ();
        }
    }

    /**
     * Returns the ProgramsFolder for the current User
     * 
     * @return The Basedir
     */
    private File getProgramsFolder(int userType)
    {
        String path = shortcut.getProgramsFolder(userType);
        return (new File(path));
        //}
        //else
        //{
        // TODO
        // 0pt. Test if KDE is installed.
        //boolean isKdeInstalled = UnixHelper.kdeIsInstalled();
        // 1. Test if User can write into
        // File kdeRootShareApplinkDir = getKDERootShareApplinkDir();
        //   if so: return getKDERootShareApplinkDir()
        //   else
        //   return getKDEUsersShareApplinkDir() +
        //}
        //return(result);
    }

    /**
     * This method is called by the <code>groupList</code> when the user makes a selection. It
     * updates the content of the <code>programGroup</code> with the result of the selection.
     * 
     * @param event the list selection event
     */

    /*--------------------------------------------------------------------------*/
    public void valueChanged(ListSelectionEvent event)
    {
        if (programGroup == null) { return; }

        String value = "";

        try
        {
            value = (String) groupList.getSelectedValue();
        }
        catch (ClassCastException exception)
        {}

        if (value == null)
        {
            value = "";
        }

        programGroup.setText(value + File.separator + suggestedProgramGroup);
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Reads the XML specification for the shortcuts to create. The result is stored in spec.
     * 
     * @exception Exception for any problems in reading the specification
     */

    /*--------------------------------------------------------------------------*/
    private void readShortcutSpec() throws Exception
    {
        // open an input stream
        InputStream input = null;

        try
        {
            input = parent.getResource(TargetFactory.getCurrentOSPrefix() + SPEC_FILE_NAME);
        }
        catch (ResourceNotFoundException rnfE)
        {
            input = parent.getResource(SPEC_FILE_NAME);

            if (input == null)
            {
                haveShortcutSpec = false;

                return;
            }
        }

        //    if( input == null )
        //    {
        //      haveShortcutSpec = false;
        //      return;
        //    }
        // initialize the parser
        StdXMLParser parser = new StdXMLParser();
        parser.setBuilder(new StdXMLBuilder());
        parser.setValidator(new NonValidator());
        parser.setReader(new StdXMLReader(input));

        // get the data
        spec = (XMLElement) parser.parse();

        // close the stream
        input.close();
        haveShortcutSpec = true;
    }

    /*--------------------------------------------------------------------------*/

    /**
     * This method analyzes the specifications for creating shortcuts and builds a list of all the
     * Shortcuts that need to be created.
     */

    /*--------------------------------------------------------------------------*/
    private void analyzeShortcutSpec()
    {
        if (!haveShortcutSpec)
        {
            shortcutsToCreate = false;

            return;
        }

        XMLElement skipper = spec.getFirstChildNamed(SPEC_KEY_SKIP_IFNOT_SUPPORTED);
        skipIfNotSupported = (skipper != null);

        // ----------------------------------------------------
        // find out if we should simulate a not supported
        // scenario
        // ----------------------------------------------------
        XMLElement support = spec.getFirstChildNamed(SPEC_KEY_NOT_SUPPORTED);

        if (support != null)
        {
            simulteNotSupported = true;
        }

        // ----------------------------------------------------
        // find out in which program group the shortcuts should
        // be placed and where this program group should be
        // located
        // ----------------------------------------------------
        XMLElement group = spec.getFirstChildNamed(SPEC_KEY_PROGRAM_GROUP);
        String location = null;
        hasDesktopShortcuts = false;

        if (group != null)
        {
            suggestedProgramGroup = group.getAttribute(SPEC_ATTRIBUTE_DEFAULT_GROUP, "");
            location = group.getAttribute(SPEC_ATTRIBUTE_LOCATION, SPEC_VALUE_APPLICATIONS);
        }
        else
        {
            suggestedProgramGroup = "";
            location = SPEC_VALUE_APPLICATIONS;
        }

        if (location.equals(SPEC_VALUE_APPLICATIONS))
        {
            groupLocation = Shortcut.APPLICATIONS;
        }
        else if (location.equals(SPEC_VALUE_START_MENU))
        {
            groupLocation = Shortcut.START_MENU;
        }

        // ----------------------------------------------------
        // create a list of all shortcuts that need to be
        // created, containing all details about each shortcut
        // ----------------------------------------------------
        VariableSubstitutor substitutor = new VariableSubstitutor(idata.getVariables());
        String temp;
        Vector shortcutSpecs = spec.getChildrenNamed(SPEC_KEY_SHORTCUT);
        XMLElement shortcutSpec;
        ShortcutData data;

        for (int i = 0; i < shortcutSpecs.size(); i++)
        {
            shortcutSpec = (XMLElement) shortcutSpecs.elementAt(i);

            if (!OsConstraint.oneMatchesCurrentSystem(shortcutSpec)) continue;

            data = new ShortcutData();

            data.name = shortcutSpec.getAttribute(SPEC_ATTRIBUTE_NAME);
            data.subgroup = shortcutSpec.getAttribute(SPEC_ATTRIBUTE_SUBGROUP);
            data.description = shortcutSpec.getAttribute(SPEC_ATTRIBUTE_DESCRIPTION, "");

            //** Linux **//
            data.deskTopEntryLinux_Encoding = shortcutSpec
                    .getAttribute(SPEC_ATTRIBUTE_ENCODING, "");
            data.deskTopEntryLinux_MimeType = shortcutSpec
                    .getAttribute(SPEC_ATTRIBUTE_MIMETYPE, "");
            data.deskTopEntryLinux_Terminal = shortcutSpec
                    .getAttribute(SPEC_ATTRIBUTE_TERMINAL, "");
            data.deskTopEntryLinux_TerminalOptions = shortcutSpec.getAttribute(
                    SPEC_ATTRIBUTE_TERMINAL_OPTIONS, "");
            data.deskTopEntryLinux_Type = shortcutSpec.getAttribute(SPEC_ATTRIBUTE_TYPE, "");

            data.deskTopEntryLinux_URL = substitutor.substitute(shortcutSpec.getAttribute(
                    SPEC_ATTRIBUTE_URL, ""), null);

            data.deskTopEntryLinux_X_KDE_SubstituteUID = shortcutSpec.getAttribute(
                    SPEC_ATTRIBUTE_KDE_SUBST_UID, "");

            data.createForAll = new Boolean(shortcutSpec.getAttribute(CREATE_FOR_ALL, "false"));
            //** EndOf LINUX **//
            temp = fixSeparatorChar(shortcutSpec.getAttribute(SPEC_ATTRIBUTE_TARGET, ""));
            data.target = substitutor.substitute(temp, null);

            temp = shortcutSpec.getAttribute(SPEC_ATTRIBUTE_COMMAND, "");
            data.commandLine = substitutor.substitute(temp, null);

            temp = fixSeparatorChar(shortcutSpec.getAttribute(SPEC_ATTRIBUTE_ICON, ""));
            data.iconFile = substitutor.substitute(temp, null);
            data.iconIndex = Integer.parseInt(shortcutSpec.getAttribute(SPEC_ATTRIBUTE_ICON_INDEX,
                    "0"));

            temp = fixSeparatorChar(shortcutSpec.getAttribute(SPEC_ATTRIBUTE_WORKING_DIR, ""));
            data.workingDirectory = substitutor.substitute(temp, null);

            String initialState = shortcutSpec.getAttribute(SPEC_ATTRIBUTE_INITIAL_STATE, "");

            if (initialState.equals(SPEC_VALUE_NO_SHOW))
            {
                data.initialState = Shortcut.HIDE;
            }
            else if (initialState.equals(SPEC_VALUE_NORMAL))
            {
                data.initialState = Shortcut.NORMAL;
            }
            else if (initialState.equals(SPEC_VALUE_MAXIMIZED))
            {
                data.initialState = Shortcut.MAXIMIZED;
            }
            else if (initialState.equals(SPEC_VALUE_MINIMIZED))
            {
                data.initialState = Shortcut.MINIMIZED;
            }
            else
            {
                data.initialState = Shortcut.NORMAL;
            }

            // --------------------------------------------------
            // if the minimal data requirements are met to create
            // the shortcut, create one entry each for each of
            // the requested types.
            // Eventually this will cause the creation of one
            // shortcut in each of the associated locations.
            // --------------------------------------------------
            // without a name we can not create a shortcut
            if (data.name == null)
            {
                continue;
            }

            //1. Elmar: "Without a target we can not create a shortcut."
            //2. Marc: "No, Even on Linux a Link can be an URL and has no target."
            if (data.target == null)
            {
                continue;
            }

            // the shortcut is not actually required for any of the selected packs
            Vector forPacks = shortcutSpec.getChildrenNamed(SPEC_KEY_PACKS);

            if (!shortcutRequiredFor(forPacks))
            {
                continue;
            }
            // --------------------------------------------------
            // This section is executed if we don't skip.
            // --------------------------------------------------
            // For each of the categories set the type and if
            // the link should be placed in the program group,
            // then clone the data set to obtain an independent
            // instance and add this to the list of shortcuts
            // to be created. In this way, we will set up an
            // identical copy for each of the locations at which
            // a shortcut should be placed. Therefore you must
            // not use 'else if' statements!
            // --------------------------------------------------
            {
                if (attributeIsTrue(shortcutSpec, SPEC_ATTRIBUTE_DESKTOP))
                {
                    hasDesktopShortcuts = true;
                    data.addToGroup = false;
                    data.type = Shortcut.DESKTOP;
                    shortcuts.add(data.clone());
                }

                if (attributeIsTrue(shortcutSpec, SPEC_ATTRIBUTE_APPLICATIONS))
                {
                    data.addToGroup = false;
                    data.type = Shortcut.APPLICATIONS;
                    shortcuts.add(data.clone());
                }

                if (attributeIsTrue(shortcutSpec, SPEC_ATTRIBUTE_START_MENU))
                {
                    data.addToGroup = false;
                    data.type = Shortcut.START_MENU;
                    shortcuts.add(data.clone());
                }

                if (attributeIsTrue(shortcutSpec, SPEC_ATTRIBUTE_STARTUP))
                {
                    data.addToGroup = false;
                    data.type = Shortcut.START_UP;
                    shortcuts.add(data.clone());
                }

                if (attributeIsTrue(shortcutSpec, SPEC_ATTRIBUTE_PROGRAM_GROUP))
                {
                    data.addToGroup = true;
                    data.type = Shortcut.APPLICATIONS;
                    shortcuts.add(data.clone());
                }
            }
        }

        // ----------------------------------------------------
        // signal if there are any shortcuts to create
        // ----------------------------------------------------
        if (shortcuts.size() > 0)
        {
            shortcutsToCreate = true;
        }
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Creates all shortcuts based on the information in <code>shortcuts</code>.
     */

    /*--------------------------------------------------------------------------*/
    private void createShortcuts()
    {
        if (!create) return;
        ShortcutData data;

        for (int i = 0; i < shortcuts.size(); i++)
        {
            data = (ShortcutData) shortcuts.elementAt(i);

            try
            {
                groupName = groupName + data.subgroup;
                shortcut.setUserType(itsUserType);
                shortcut.setLinkName(data.name);
                shortcut.setLinkType(data.type);
                shortcut.setArguments(data.commandLine);
                shortcut.setDescription(data.description);
                shortcut.setIconLocation(data.iconFile, data.iconIndex);

                shortcut.setShowCommand(data.initialState);
                shortcut.setTargetPath(data.target);
                shortcut.setWorkingDirectory(data.workingDirectory);
                shortcut.setEncoding(data.deskTopEntryLinux_Encoding);
                shortcut.setMimetype(data.deskTopEntryLinux_MimeType);

                shortcut.setTerminal(data.deskTopEntryLinux_Terminal);
                shortcut.setTerminalOptions(data.deskTopEntryLinux_TerminalOptions);
                shortcut.setType(data.deskTopEntryLinux_Type);
                shortcut.setKdeSubstUID(data.deskTopEntryLinux_X_KDE_SubstituteUID);
                shortcut.setURL(data.deskTopEntryLinux_URL);
                shortcut.setCreateForAll(data.createForAll);

                if (data.addToGroup)
                {
                    shortcut.setProgramGroup(groupName);
                }
                else
                {
                    shortcut.setProgramGroup("");
                }

                try
                {
                    // ----------------------------------------------
                    // save the shortcut only if it is either not on
                    // the desktop or if it is on the desktop and
                    // the user has signalled that it is ok to place
                    // shortcuts on the desktop.
                    // ----------------------------------------------
                    if ((data.type != Shortcut.DESKTOP)
                            || ((data.type == Shortcut.DESKTOP) && allowDesktopShortcut
                                    .isSelected()))
                    {
                        // save the shortcut
                        shortcut.save();

                        // add the file and directory name to the file list
                        String fileName = shortcut.getFileName();
                        files.add(0, fileName);

                        File file = new File(fileName);
                        File base = new File(shortcut.getBasePath());
                        Vector intermediates = new Vector();

                        //String directoryName = shortcut.getDirectoryCreated ();
                        execFiles.add(new ExecutableFile(fileName, 2, ExecutableFile.WARN,
                                new ArrayList(), false));

                        files.add(fileName);

                        while ((file = file.getParentFile()) != null)
                        {
                            if (file.equals(base)) break;
                            intermediates.add(file);
                        }
                        if (file != null)
                        {
                            for (Iterator iter = intermediates.iterator(); iter.hasNext();)
                                files.add(0, iter.next().toString());
                        }
                    }
                }
                catch (Exception exception)
                {}
            }
            catch (Throwable exception)
            {
                continue;
            }
        }

        
        //
        try
        {

            if (execFiles != null)
            {
                FileExecutor executor = new FileExecutor(execFiles);
                // 
                // TODO: Hi Guys,
                // TODO The following commented-out line sometimes produces an uncatchable
                // nullpointer Exception!
                // TODO evaluate for what reason the files should exec.
                // TODO if there is a serious explanation, why to do that,
                // TODO the code must be more robust

                //evaluate executor.executeFiles( ExecutableFile.NEVER, null );
            }
        }
        catch (NullPointerException nep)
        {
            nep.printStackTrace();
        }
        catch (RuntimeException cannot)
        {
            cannot.printStackTrace();
        }

        parent.unlockNextButton();
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Verifies if the shortcut is required for any of the packs listed. The shortcut is required
     * for a pack in the list if that pack is actually selected for installation. <br>
     * <br>
     * <b>Note: </b> <br>
     * If the list of selected packs is empty then <code>true</code> is always returnd. The same
     * is true if the <code>packs</code> list is empty.
     * 
     * @param packs a <code>Vector</code> of <code>String</code>s. Each of the strings denotes
     * a pack for which the schortcut should be created if the pack is actually installed.
     * 
     * @return <code>true</code> if the shortcut is required for at least on pack in the list,
     * otherwise returns <code>false</code>.
     */

    /*--------------------------------------------------------------------------*/
    /*
     * $ @design
     * 
     * The information about the installed packs comes from InstallData.selectedPacks. This assumes
     * that this panel is presented to the user AFTER the PacksPanel.
     * --------------------------------------------------------------------------
     */
    private boolean shortcutRequiredFor(Vector packs)
    {
        String selected;
        String required;

        if (packs.size() == 0) { return (true); }

        for (int i = 0; i < idata.selectedPacks.size(); i++)
        {
            selected = ((Pack) idata.selectedPacks.get(i)).name;

            for (int k = 0; k < packs.size(); k++)
            {
                required = (String) ((XMLElement) packs.elementAt(k)).getAttribute(
                        SPEC_ATTRIBUTE_NAME, "");

                if (selected.equals(required)) { return (true); }
            }
        }

        return (false);
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Determines if the named attribute in true. True is represented by any of the following
     * strings and is not case sensitive. <br>
     * 
     * <ul>
     * <li>yes</li>
     * <li>1</li>
     * <li>true</li>
     * <li>on</li>
     * </ul>
     * 
     * <br>
     * Every other string, including the empty string as well as the non-existence of the attribute
     * will cuase <code>false</code> to be returned.
     * 
     * @param element the <code>XMLElement</code> to search for the attribute.
     * @param name the name of the attribute to test.
     * 
     * @return <code>true</code> if the attribute value equals one of the pre-defined strings,
     * <code>false</code> otherwise.
     */

    /*--------------------------------------------------------------------------*/
    private boolean attributeIsTrue(XMLElement element, String name)
    {
        String value = element.getAttribute(name, "").toUpperCase();

        if (value.equals("YES"))
        {
            return (true);
        }
        else if (value.equals("TRUE"))
        {
            return (true);
        }
        else if (value.equals("ON"))
        {
            return (true);
        }
        else if (value.equals("1")) { return (true); }

        return (false);
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Replaces any ocurrence of '/' or '\' in a path string with the correct version for the
     * operating system.
     * 
     * @param path a system path
     * 
     * @return a path string that uniformely uses the proper version of the separator character.
     */

    /*--------------------------------------------------------------------------*/
    private String fixSeparatorChar(String path)
    {
        String newPath = path.replace('/', File.separatorChar);
        newPath = newPath.replace('\\', File.separatorChar);

        return (newPath);
    }

    /*--------------------------------------------------------------------------*/

    /**
     * This method creates the UI for this panel.
     * 
     * @param groups A <code>Vector</code> that contains <code>Strings</code> with all the names
     * of the existing program groups. These will be placed in the <code>groupList</code>.
     */

    /*--------------------------------------------------------------------------*/
    private void buildUI(File groups)
    {
        itsProgramFolder = groups;        
         
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 3;
        constraints.gridheight = 1;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;        
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        /**/
        

        // Add a CheckBox which enables the user to entirely supress shortcut creation.  
        String menuKind = parent.langpack.getString("ShortcutPanel.regular.StartMenu:Start-Menu");
        if( OsVersion.IS_UNIX && UnixHelper.kdeIsInstalled() )
            menuKind = parent.langpack.getString("ShortcutPanel.regular.StartMenu:K-Menu");
        
        createShortcuts = new JCheckBox( StringTool.replace( parent.langpack.getString("ShortcutPanel.regular.create"), "StartMenu", menuKind ),
                true);
        createShortcuts.addActionListener(this);
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;   
        constraints.weighty = 0.2;
        layout.addLayoutComponent(createShortcuts, constraints);
        add(createShortcuts);
        
        // ----------------------------------------------------
        // check box to allow the user to decide if a desktop
        // shortcut should be created.
        // this should only be created if needed and requested
        // in the definition file.
        // ----------------------------------------------------
        if (hasDesktopShortcuts)
        {
          String initialAllowedValue = idata.getVariable( "DesktopShortcutCheckboxEnabled" );
          boolean initialAllowedFlag = false;
          if( initialAllowedValue==null) 
            initialAllowedFlag= false;
          else if( Boolean.TRUE.toString().equals(initialAllowedValue) ) 
            initialAllowedFlag=true;
                     
          allowDesktopShortcut = new JCheckBox(parent.langpack
                .getString("ShortcutPanel.regular.desktop"), initialAllowedFlag);
          constraints.gridx = 0;
          constraints.gridy = 2;
          constraints.gridwidth = 3;
          constraints.gridheight = 1;
          constraints.weighty = 0.2;
        
          layout.addLayoutComponent(allowDesktopShortcut, constraints);
          add(allowDesktopShortcut);
        }
        
        listLabel = LabelFactory.create(parent.langpack.getString("ShortcutPanel.regular.list"),
                JLabel.LEADING);
        
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.weightx = 0.2;
        constraints.weighty = 0.2;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        
        layout.addLayoutComponent(listLabel, constraints);
        add(listLabel);
        // ----------------------------------------------------
        // list box to list all of already existing folders as program groups
        // at the intended destination
        // ----------------------------------------------------
        addSelectionList( groups, 0, 4, 2, 1, GridBagConstraints.BOTH ); 

        // ----------------------------------------------------
        // radio buttons to select current user or all users.
        // ----------------------------------------------------
        if (shortcut.multipleUsers())
        {
            JPanel usersPanel = new JPanel(new GridLayout(2, 1));
            ButtonGroup usersGroup = new ButtonGroup();
            currentUser = new JRadioButton(parent.langpack
                    .getString("ShortcutPanel.regular.currentUser"), !isRootUser);
            currentUser.addActionListener(this);
            usersGroup.add(currentUser);
            usersPanel.add(currentUser);
            allUsers = new JRadioButton(
                    parent.langpack.getString("ShortcutPanel.regular.allUsers"), isRootUser);
            if (!isRootUser) allUsers.setEnabled(false);
            allUsers.addActionListener(this);
            usersGroup.add(allUsers);
            usersPanel.add(allUsers);

            TitledBorder border = new TitledBorder(new EmptyBorder(2, 2, 2, 2), parent.langpack
                    .getString("ShortcutPanel.regular.userIntro"));
            usersPanel.setBorder(border);

            constraints.gridx = 2;
            constraints.gridy = 4;
            constraints.gridwidth = 1;
            constraints.gridheight = 1;
            constraints.weighty = 1.0;
            constraints.weightx = 1.0;
            constraints.fill = GridBagConstraints.NONE;
            layout.addLayoutComponent(usersPanel, constraints);
            add(usersPanel);
        }

        // ----------------------------------------------------
        // edit box that contains the suggested program group
        // name, which can be modfied or substituted from the
        // list by the user
        // ----------------------------------------------------
        programGroup = new JTextField(suggestedProgramGroup, 40); // 40?

        constraints.gridx = 0;
        constraints.gridy = 5;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        constraints.weighty = 1.0;
        constraints.weightx = 10.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        layout.addLayoutComponent(programGroup, constraints);
        add(programGroup);

        // ----------------------------------------------------
        // reset button that allows the user to revert to the
        // original suggestion for the program group
        // ----------------------------------------------------
        defaultButton = ButtonFactory.createButton(parent.langpack
                .getString("ShortcutPanel.regular.default"), idata.buttonsHColor);
        defaultButton.addActionListener(this);

        constraints.gridx = 2;
        constraints.gridy = 5;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;       
        constraints.fill = GridBagConstraints.NONE;
        layout.addLayoutComponent(defaultButton, constraints);
        add(defaultButton);
    }
    
    /**
     * 
     * @param groups
     */
    private void addSelectionList(File groups, int aGridx, int aGridy, int aGridwidth, int aGridheight, int aFill)
    {
        Vector dirEntries = new Vector();

        File[] entries = groups.listFiles();
        for (int idx = 0; idx < entries.length; idx++)
        {
            if (entries[idx].isDirectory())
            {
                dirEntries.add(entries[idx].getName());
            }
        }
        groupList = new JList(dirEntries);
        groupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        groupList.getSelectionModel().addListSelectionListener(this);

        JScrollPane scrollPane = new JScrollPane(groupList);

        constraints.gridx = aGridx;
        constraints.gridy = aGridy;
        constraints.gridwidth = aGridwidth;
        constraints.gridheight = aGridheight;
        constraints.weightx = 10.0;
        constraints.weighty = 1.5;
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.fill = aFill;
        layout.addLayoutComponent(scrollPane, constraints);
        add(scrollPane);
    }


    /*--------------------------------------------------------------------------*/

    /**
     * This method creates an alternative UI for this panel. This UI can be used when the creation
     * of shortcuts is not supported on the target system. It displays an apology for the inability
     * to create shortcuts on this system, along with information about the intended targets. In
     * addition, there is a button that allows the user to save more complete information in a text
     * file. Based on this information the user might be able to create the necessary shortcut him
     * or herself. At least there will be information about how to launch the application.
     */

    /*--------------------------------------------------------------------------*/
    private void buildAlternateUI()
    {
        layout = new GridBagLayout();
        constraints = new GridBagConstraints();
        setLayout(layout);

        // ----------------------------------------------------
        // static text a the top of the panel, that apologizes
        // about the fact that we can not create shortcuts on
        // this particular target OS.
        // ----------------------------------------------------
        MultiLineLabel apologyLabel = new MultiLineLabel(parent.langpack
                .getString("ShortcutPanel.alternate.apology"), 0, 0);

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        layout.addLayoutComponent(apologyLabel, constraints);
        add(apologyLabel);

        // ----------------------------------------------------
        // label that explains the significance ot the list box
        // ----------------------------------------------------
        MultiLineLabel listLabel = new MultiLineLabel(parent.langpack
                .getString("ShortcutPanel.alternate.targetsLabel"), 0, 0);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        layout.addLayoutComponent(listLabel, constraints);
        add(listLabel);

        // ----------------------------------------------------
        // list box to list all of the intended shortcut targets
        // ----------------------------------------------------
        Vector targets = new Vector();

        for (int i = 0; i < shortcuts.size(); i++)
        {
            targets.add(((ShortcutData) shortcuts.elementAt(i)).target);
        }

        targetList = new JList(targets);

        JScrollPane scrollPane = new JScrollPane(targetList);

        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.fill = GridBagConstraints.BOTH;
        layout.addLayoutComponent(scrollPane, constraints);
        add(scrollPane);

        // ----------------------------------------------------
        // static text that explains about the text file
        // ----------------------------------------------------
        MultiLineLabel fileExplanation = new MultiLineLabel(parent.langpack
                .getString("ShortcutPanel.alternate.textFileExplanation"), 0, 0);

        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        layout.addLayoutComponent(fileExplanation, constraints);
        add(fileExplanation);

        // ----------------------------------------------------
        // button to save the text file
        // ----------------------------------------------------
        saveButton = ButtonFactory.createButton(parent.langpack
                .getString("ShortcutPanel.alternate.saveButton"), idata.buttonsHColor);
        saveButton.addActionListener(this);

        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.CENTER;
        layout.addLayoutComponent(saveButton, constraints);
        add(saveButton);
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Overriding the superclass implementation. This method returns the size of the container.
     * 
     * @return the size of the container
     */

    /*--------------------------------------------------------------------------*/
    public Dimension getSize()
    {
        Dimension size = getParent().getSize();
        Insets insets = getInsets();
        Border border = getBorder();
        Insets borderInsets = new Insets(0, 0, 0, 0);

        if (border != null)
        {
            borderInsets = border.getBorderInsets(this);
        }

        size.height = size.height - insets.top - insets.bottom - borderInsets.top
                - borderInsets.bottom - 50;
        size.width = size.width - insets.left - insets.right - borderInsets.left
                - borderInsets.right - 50;

        return (size);
    }

    /*--------------------------------------------------------------------------*/

    /**
     * This method saves all shortcut information to a text file.
     */

    /*--------------------------------------------------------------------------*/
    private void saveToFile()
    {
        File file = null;

        // ----------------------------------------------------
        // open a file chooser dialog to get a path / file name
        // ----------------------------------------------------
        JFileChooser fileDialog = new JFileChooser(idata.getInstallPath());
        fileDialog.setSelectedFile(new File(TEXT_FILE_NAME));

        if (fileDialog.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            file = fileDialog.getSelectedFile();
        }
        else
        {
            return;
        }

        // ----------------------------------------------------
        // save to the file
        // ----------------------------------------------------
        FileWriter output = null;
        StringBuffer buffer = new StringBuffer();
        String header = parent.langpack.getString("ShortcutPanel.textFile.header");

        String newline = System.getProperty("line.separator", "\n");

        try
        {
            output = new FileWriter(file);
        }
        catch (Throwable exception)
        {
            // !!! show an error dialog
            return;
        }

        // ----------------------------------------------------
        // break the header down into multiple lines based
        // on '\n' line breaks.
        // ----------------------------------------------------
        int nextIndex = 0;
        int currentIndex = 0;

        do
        {
            nextIndex = header.indexOf("\\n", currentIndex);

            if (nextIndex > -1)
            {
                buffer.append(header.substring(currentIndex, nextIndex));
                buffer.append(newline);
                currentIndex = nextIndex + 2;
            }
            else
            {
                buffer.append(header.substring(currentIndex, header.length()));
                buffer.append(newline);
            }
        }
        while (nextIndex > -1);

        buffer.append(SEPARATOR_LINE);
        buffer.append(newline);
        buffer.append(newline);

        for (int i = 0; i < shortcuts.size(); i++)
        {
            ShortcutData data = (ShortcutData) shortcuts.elementAt(i);

            buffer.append(parent.langpack.getString("ShortcutPanel.textFile.name"));
            buffer.append(data.name);
            buffer.append(newline);

            buffer.append(parent.langpack.getString("ShortcutPanel.textFile.location"));

            switch (data.type)
            {
            case Shortcut.DESKTOP: {
                buffer.append(parent.langpack.getString("ShortcutPanel.location.desktop"));

                break;
            }

            case Shortcut.APPLICATIONS: {
                buffer.append(parent.langpack.getString("ShortcutPanel.location.applications"));

                break;
            }

            case Shortcut.START_MENU: {
                buffer.append(parent.langpack.getString("ShortcutPanel.location.startMenu"));

                break;
            }

            case Shortcut.START_UP: {
                buffer.append(parent.langpack.getString("ShortcutPanel.location.startup"));

                break;
            }
            }

            buffer.append(newline);

            buffer.append(parent.langpack.getString("ShortcutPanel.textFile.description"));
            buffer.append(data.description);
            buffer.append(newline);

            buffer.append(parent.langpack.getString("ShortcutPanel.textFile.target"));
            buffer.append(data.target);
            buffer.append(newline);

            buffer.append(parent.langpack.getString("ShortcutPanel.textFile.command"));
            buffer.append(data.commandLine);
            buffer.append(newline);

            buffer.append(parent.langpack.getString("ShortcutPanel.textFile.iconName"));
            buffer.append(data.iconFile);
            buffer.append(newline);

            buffer.append(parent.langpack.getString("ShortcutPanel.textFile.iconIndex"));
            buffer.append(data.iconIndex);
            buffer.append(newline);

            buffer.append(parent.langpack.getString("ShortcutPanel.textFile.work"));
            buffer.append(data.workingDirectory);
            buffer.append(newline);

            buffer.append(newline);
            buffer.append(SEPARATOR_LINE);
            buffer.append(newline);
            buffer.append(newline);
        }

        try
        {
            output.write(buffer.toString());
        }
        catch (Throwable exception)
        {}
        finally
        {
            try
            {
                output.flush();
                output.close();
                files.add(file.getPath());
            }
            catch (Throwable exception)
            {
                // not really anything I can do here, maybe should show a dialog that
                // tells the user that data might not have been saved completely!?
            }
        }
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Adds all files and directories to the uninstaller.
     */

    /*--------------------------------------------------------------------------*/
    private void addToUninstaller()
    {
        UninstallData uninstallData = UninstallData.getInstance();

        for (int i = 0; i < files.size(); i++)
        {
            uninstallData.addFile((String) files.elementAt(i));
        }
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Adds iformation about the shortcuts that have been created during the installation to the XML
     * tree.
     * 
     * @param panelRoot the root of the XML tree
     */

    /*--------------------------------------------------------------------------*/
    /*
     * $ @design
     * 
     * The information needed to create shortcuts has been collected in the Vector 'shortcuts'. Take
     * the data from there and package it in XML form for storage by the installer. The group name
     * is only stored once in a separate XML element, since there is only one.
     * --------------------------------------------------------------------------
     */
    public void makeXMLData(XMLElement panelRoot)
    {
        // ----------------------------------------------------
        // if there are no shortcuts to create, shortcuts are
        // not supported, or we should simulate that they are
        // not supported, then we have nothing to add. Just
        // return
        // ----------------------------------------------------
        if (!shortcutsToCreate || !shortcut.supported() || groupName == null || simulteNotSupported) { return; }

        ShortcutData data;
        XMLElement dataElement;

        // ----------------------------------------------------
        // add the item that defines the name of the program group
        // ----------------------------------------------------
        dataElement = new XMLElement(AUTO_KEY_PROGRAM_GROUP);
        dataElement.setAttribute(AUTO_ATTRIBUTE_NAME, groupName);
        panelRoot.addChild(dataElement);

        // ----------------------------------------------------
        // add the details for each of the shortcuts
        // ----------------------------------------------------
        for (int i = 0; i < shortcuts.size(); i++)
        {
            data = (ShortcutData) shortcuts.elementAt(i);
            dataElement = new XMLElement(AUTO_KEY_SHORTCUT);

            dataElement.setAttribute(AUTO_ATTRIBUTE_NAME, data.name);
            dataElement.setAttribute(AUTO_ATTRIBUTE_GROUP, Boolean.valueOf(data.addToGroup)
                    .toString());
            dataElement.setAttribute(AUTO_ATTRIBUTE_TYPE, Integer.toString(data.type));
            dataElement.setAttribute(AUTO_ATTRIBUTE_COMMAND, data.commandLine);
            dataElement.setAttribute(AUTO_ATTRIBUTE_DESCRIPTION, data.description);
            dataElement.setAttribute(AUTO_ATTRIBUTE_ICON, data.iconFile);
            dataElement.setAttribute(AUTO_ATTRIBUTE_ICON_INDEX, Integer.toString(data.iconIndex));
            dataElement.setAttribute(AUTO_ATTRIBUTE_INITIAL_STATE, Integer
                    .toString(data.initialState));
            dataElement.setAttribute(AUTO_ATTRIBUTE_TARGET, data.target);
            dataElement.setAttribute(AUTO_ATTRIBUTE_WORKING_DIR, data.workingDirectory);

            // ----------------------------------------------
            // add the shortcut only if it is either not on
            // the desktop or if it is on the desktop and
            // the user has signalled that it is ok to place
            // shortcuts on the desktop.
            // ----------------------------------------------
            if ((data.type != Shortcut.DESKTOP)
                    || ((data.type == Shortcut.DESKTOP) && allowDesktopShortcut.isSelected()))
            {
                panelRoot.addChild(dataElement);
            }
        }
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Creates shortcuts based on teh information in <code>panelRoot</code> without UI.
     * 
     * @param panelRoot the root of the XML tree
     */

    /*--------------------------------------------------------------------------*/
    /*
     * $ @design
     * 
     * Reconstitute the information needed to create shortcuts from XML data that was previously
     * stored by the installer through makeXMLData(). Create a new Vector containing this data and
     * stroe it in 'shortcuts' for use by createShortcuts(). Once this has been completed, call
     * createShortcuts() to complete the operation.
     * --------------------------------------------------------------------------
     */
    public void runAutomated(XMLElement panelRoot)
    {
        // ----------------------------------------------------
        // if shortcuts are not supported, then we can not
        // create shortcuts, even if there was any install
        // data. Just return.
        // ----------------------------------------------------
        if (!shortcut.supported()) { return; }

        if (!OsConstraint.oneMatchesCurrentSystem(panelRoot)) { return; }

        shortcuts = new Vector();
        Vector shortcutElements;
        ShortcutData data;
        XMLElement dataElement;

        // ----------------------------------------------------
        // set the name of the program group
        // ----------------------------------------------------
        dataElement = panelRoot.getFirstChildNamed(AUTO_KEY_PROGRAM_GROUP);
        groupName = dataElement.getAttribute(AUTO_ATTRIBUTE_NAME);

        if (groupName == null)
        {
            groupName = "";
        }

        // ----------------------------------------------------
        // add the details for each of the shortcuts
        // ----------------------------------------------------
        shortcutElements = panelRoot.getChildrenNamed(AUTO_KEY_SHORTCUT);

        for (int i = 0; i < shortcutElements.size(); i++)
        {
            data = new ShortcutData();
            dataElement = (XMLElement) shortcutElements.elementAt(i);

            data.name = dataElement.getAttribute(AUTO_ATTRIBUTE_NAME);
            data.addToGroup = Boolean.valueOf(dataElement.getAttribute(AUTO_ATTRIBUTE_GROUP))
                    .booleanValue();
            data.type = Integer.valueOf(dataElement.getAttribute(AUTO_ATTRIBUTE_TYPE)).intValue();
            data.commandLine = dataElement.getAttribute(AUTO_ATTRIBUTE_COMMAND);
            data.description = dataElement.getAttribute(AUTO_ATTRIBUTE_DESCRIPTION);
            data.iconFile = dataElement.getAttribute(AUTO_ATTRIBUTE_ICON);
            data.iconIndex = Integer.valueOf(dataElement.getAttribute(AUTO_ATTRIBUTE_ICON_INDEX))
                    .intValue();
            data.initialState = Integer.valueOf(
                    dataElement.getAttribute(AUTO_ATTRIBUTE_INITIAL_STATE)).intValue();
            data.target = dataElement.getAttribute(AUTO_ATTRIBUTE_TARGET);
            data.workingDirectory = dataElement.getAttribute(AUTO_ATTRIBUTE_WORKING_DIR);

            shortcuts.add(data);
        }

        createShortcuts();
    }


}

/*---------------------------------------------------------------------------*/
