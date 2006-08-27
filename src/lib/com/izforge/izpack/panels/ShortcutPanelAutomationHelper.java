/**
 * 
 */
package com.izforge.izpack.panels;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import net.n3.nanoxml.XMLElement;

import com.izforge.izpack.ExecutableFile;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelAutomation;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.FileExecutor;
import com.izforge.izpack.util.OsConstraint;
import com.izforge.izpack.util.TargetFactory;
import com.izforge.izpack.util.os.Shortcut;



/**
 * @author marc.eppelmann
 *
 */
public class ShortcutPanelAutomationHelper implements PanelAutomation
{

    /** the one shortcut instance for reuse in many locations */

    
    /* (non-Javadoc)
     * @see com.izforge.izpack.installer.PanelAutomation#makeXMLData(com.izforge.izpack.installer.AutomatedInstallData, net.n3.nanoxml.XMLElement)
     */
    public void makeXMLData( AutomatedInstallData idata, XMLElement panelRoot )
    {
        Debug.log( this.getClass().getName() + "::entering makeXMLData()" );        
        //ShortcutPanel.getInstance().makeXMLData(  idata, panelRoot );
        
    }
    
    

    /* (non-Javadoc)
     * @see com.izforge.izpack.installer.PanelAutomation#runAutomated(com.izforge.izpack.installer.AutomatedInstallData, net.n3.nanoxml.XMLElement)
     */
    public boolean runAutomated( AutomatedInstallData installData, XMLElement panelRoot )
    {        
        Shortcut shortcut;

        /**
         * A list of ShortcutData> objects. Each object is the complete specification for one shortcut
         * that must be created.
         */
        Vector shortcuts = new Vector();
        
        Vector execFiles = new Vector();
        
        /**
         * Holds a list of all the shortcut files that have been created. Note: this variable contains
         * valid data only after createShortcuts() has been called. This list is created so that the
         * files can be added to the uninstaller.
         */
        Vector files = new Vector();
        
        Debug.log( this.getClass().getName() + " Entered runAutomated()");
        
        try
        {
            shortcut = (Shortcut) (TargetFactory.getInstance()
                    .makeObject("com.izforge.izpack.util.os.Shortcut"));
            shortcut.initialize(Shortcut.APPLICATIONS, "-");
        }
        catch (Throwable exception)
        {
            Debug.log("Could not create shortcut instance");
            exception.printStackTrace();
            return true;
        }

        // ----------------------------------------------------
        // if shortcuts are not supported, then we can not
        // create shortcuts, even if there was any install
        // data. Just return.
        // ----------------------------------------------------
        if (!shortcut.supported()) { Debug.log("shortcuts not supported here"); return true; }

        if (!OsConstraint.oneMatchesCurrentSystem(panelRoot)) { Debug.log("Shortcuts Not oneMatchesCurrentSystem"); return true; }

        shortcuts = new Vector();

        Vector shortcutElements;
        ShortcutData data;
        XMLElement dataElement;

        // ----------------------------------------------------
        // set the name of the program group
        // ----------------------------------------------------
        dataElement = panelRoot.getFirstChildNamed(ShortcutPanel.AUTO_KEY_PROGRAM_GROUP);
        String groupName = dataElement.getAttribute(ShortcutPanel.AUTO_ATTRIBUTE_NAME);

        if (groupName == null)
        {
            groupName = "";
        }

        // ----------------------------------------------------
        // add the details for each of the shortcuts
        // ----------------------------------------------------
        shortcutElements = panelRoot.getChildrenNamed(ShortcutPanel.AUTO_KEY_SHORTCUT);

        for (int i = 0; i < shortcutElements.size(); i++)            
        {
            Debug.log( this.getClass().getName() + "runAutomated:shortcutElements " + i);
            data = new ShortcutData();
            dataElement = (XMLElement) shortcutElements.elementAt(i);

            data.name = dataElement.getAttribute(ShortcutPanel.AUTO_ATTRIBUTE_NAME);
            data.addToGroup = Boolean.valueOf(dataElement.getAttribute(ShortcutPanel.AUTO_ATTRIBUTE_GROUP))
                    .booleanValue();
            
            
            try
            {
                data.type = Integer.valueOf(dataElement.getAttribute(ShortcutPanel.AUTO_ATTRIBUTE_TYPE)).intValue();
            }
            catch (NumberFormatException e)
            {
               Debug.log( "WARN: On Linux data.type is NOT an int. Ignored." );
            }
            
            
            data.commandLine = dataElement.getAttribute(ShortcutPanel.AUTO_ATTRIBUTE_COMMAND);
            data.description = dataElement.getAttribute(ShortcutPanel.AUTO_ATTRIBUTE_DESCRIPTION);
            data.iconFile = dataElement.getAttribute(ShortcutPanel.AUTO_ATTRIBUTE_ICON);
            data.iconIndex = Integer.valueOf(dataElement.getAttribute(ShortcutPanel.AUTO_ATTRIBUTE_ICON_INDEX))
                    .intValue();
            data.initialState = Integer.valueOf(
                    dataElement.getAttribute(ShortcutPanel.AUTO_ATTRIBUTE_INITIAL_STATE)).intValue();
            data.target = dataElement.getAttribute(ShortcutPanel.AUTO_ATTRIBUTE_TARGET);
            data.workingDirectory = dataElement.getAttribute(ShortcutPanel.AUTO_ATTRIBUTE_WORKING_DIR);
            
            // Linux
            data.deskTopEntryLinux_Encoding = dataElement.getAttribute( ShortcutPanel.SPEC_ATTRIBUTE_ENCODING, "");
            data.deskTopEntryLinux_MimeType = dataElement.getAttribute( ShortcutPanel.SPEC_ATTRIBUTE_MIMETYPE, "");
            data.deskTopEntryLinux_Terminal = dataElement.getAttribute( ShortcutPanel.SPEC_ATTRIBUTE_TERMINAL, "");
            data.deskTopEntryLinux_TerminalOptions = dataElement.getAttribute( ShortcutPanel.SPEC_ATTRIBUTE_TERMINAL_OPTIONS, "");
            data.deskTopEntryLinux_Type = dataElement.getAttribute( ShortcutPanel.SPEC_ATTRIBUTE_TYPE, "");

            data.deskTopEntryLinux_URL = dataElement.getAttribute( ShortcutPanel.SPEC_ATTRIBUTE_URL, "");

            data.deskTopEntryLinux_X_KDE_SubstituteUID = dataElement.getAttribute(
            ShortcutPanel.SPEC_ATTRIBUTE_KDE_SUBST_UID, "");

            data.createForAll = new Boolean(dataElement.getAttribute(ShortcutPanel.CREATE_FOR_ALL, "false"));
            data.userType = Integer.valueOf( dataElement.getAttribute( ShortcutPanel.USER_TYPE, Integer.toString( Shortcut.CURRENT_USER) ) ).intValue();
            //END LINUX

            shortcuts.add( data );
        }

        
        System.out.print("[ creating shortcuts ");
           

           // ShortcutData data;

            for (int i = 0; i < shortcuts.size(); i++)
            {
                data = (ShortcutData) shortcuts.elementAt(i);

                try
                {
                    groupName = groupName + data.subgroup;
                    shortcut.setUserType( data.userType );
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
                                || ((data.type == Shortcut.DESKTOP) ))
                        {
                            // save the shortcut
                            
                            System.out.print(".");
                            System.out.flush();
                            
                            
                            shortcut.save();

                            // add the file and directory name to the file list
                            String fileName = shortcut.getFileName();
                            files.add(0, fileName);

                            File file = new File(fileName);
                            File base = new File(shortcut.getBasePath());
                            Vector intermediates = new Vector();

                            // String directoryName = shortcut.getDirectoryCreated ();
                            execFiles.add(new ExecutableFile(fileName, ExecutableFile.UNINSTALL,
                                    ExecutableFile.IGNORE, new ArrayList(), false));

                            files.add(fileName);

                            while ((file = file.getParentFile()) != null)
                            {
                                if (file.equals(base))
                                {
                                    break;
                                }

                                intermediates.add(file);
                            }

                            if (file != null)
                            {
                                Enumeration filesEnum = intermediates.elements();

                                while (filesEnum.hasMoreElements())
                                {
                                    files.add(0, filesEnum.nextElement().toString());
                                }
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

            // }
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
                    // evaluate executor.executeFiles( ExecutableFile.NEVER, null );
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

            // /////parent.unlockNextButton();
        System.out.println( " done. ]");

        return true;
    }

}
