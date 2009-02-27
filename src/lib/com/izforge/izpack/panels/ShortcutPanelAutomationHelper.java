/*
 * $Id: copyright-notice-template 1421 2006-03-12 16:32:32Z jponge $
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2006 Marc Eppelmann (marc.eppelmann&#064;gmx.de)
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
package com.izforge.izpack.panels;

import com.izforge.izpack.ExecutableFile;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelAutomation;
import com.izforge.izpack.installer.UninstallData;
import com.izforge.izpack.util.*;
import com.izforge.izpack.util.os.Shortcut;
import com.izforge.izpack.adaptator.IXMLElement;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

/**
 * The ShortcutPanelAutomationHelper is responsible to create Shortcuts during the automated
 * installation. Most code comes copied from the ShortcutPanel
 *
 * @author Marc Eppelmann (marc.eppelmann&#064;gmx.de)
 * @version $Revision: 1540 $
 */
public class ShortcutPanelAutomationHelper implements PanelAutomation
{

    // ~ Methods ****************************************************************************

    /**
     * dummy method
     *
     * @param idata     DOCUMENT ME!
     * @param panelRoot DOCUMENT ME!
     */
    public void makeXMLData(AutomatedInstallData idata, IXMLElement panelRoot)
    {
        Debug.log(this.getClass().getName() + "::entering makeXMLData()");

        // not used here - during automatic installation, no automatic
        // installation information is generated
    }

    /**
     * Implementation of the Shortcut Specific Automation Code
     *
     * @param installData DOCUMENT ME!
     * @param panelRoot   DOCUMENT ME!
     */
    public void runAutomated(AutomatedInstallData installData, IXMLElement panelRoot)
    {
        Shortcut shortcut;

        /**
         * A list of ShortcutData> objects. Each object is the complete specification for one
         * shortcut that must be created.
         */
        Vector<ShortcutData> shortcuts = new Vector<ShortcutData>();

        Vector<ExecutableFile> execFiles = new Vector<ExecutableFile>();

        /**
         * Holds a list of all the shortcut files that have been created. Note: this variable
         * contains valid data only after createShortcuts() has been called. This list is created so
         * that the files can be added to the uninstaller.
         */
        Vector<String> files = new Vector<String>();

        Debug.log(this.getClass().getName() + " Entered runAutomated()");

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

            return;
        }

        // ----------------------------------------------------
        // if shortcuts are not supported, then we can not
        // create shortcuts, even if there was any install
        // data. Just return.
        // ----------------------------------------------------
        if (!shortcut.supported())
        {
            Debug.log("shortcuts not supported here");

            return;
        }

        if (!OsConstraint.oneMatchesCurrentSystem(panelRoot))
        {
            Debug.log("Shortcuts Not oneMatchesCurrentSystem");

            return;
        }

        shortcuts = new Vector<ShortcutData>();

        Vector<IXMLElement> shortcutElements;
        ShortcutData data;
        IXMLElement dataElement;

        // ----------------------------------------------------
        // set the name of the program group
        // ----------------------------------------------------
        dataElement = panelRoot.getFirstChildNamed(ShortcutPanel.AUTO_KEY_PROGRAM_GROUP);
        String groupName = null;

        if (dataElement != null)
        {
            groupName = dataElement.getAttribute(ShortcutPanel.AUTO_ATTRIBUTE_NAME);
        }

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
            Debug.log(this.getClass().getName() + "runAutomated:shortcutElements " + i);
            data = new ShortcutData();
            dataElement = shortcutElements.elementAt(i);

            data.name = dataElement.getAttribute(ShortcutPanel.AUTO_ATTRIBUTE_NAME);
            data.addToGroup = Boolean.valueOf(
                    dataElement.getAttribute(ShortcutPanel.AUTO_ATTRIBUTE_GROUP));

            if (OsVersion.IS_WINDOWS)
            {
                data.type = Integer.valueOf(
                        dataElement.getAttribute(ShortcutPanel.AUTO_ATTRIBUTE_TYPE));
            }
            else
            {
                Debug.log("WARN: On Linux data.type is NOT an int. Ignored.");
            }

            data.commandLine = dataElement.getAttribute(ShortcutPanel.AUTO_ATTRIBUTE_COMMAND);
            data.description = dataElement.getAttribute(ShortcutPanel.AUTO_ATTRIBUTE_DESCRIPTION);
            data.iconFile = dataElement.getAttribute(ShortcutPanel.AUTO_ATTRIBUTE_ICON);
            data.iconIndex = Integer.valueOf(
                    dataElement.getAttribute(ShortcutPanel.AUTO_ATTRIBUTE_ICON_INDEX));
            data.initialState = Integer.valueOf(
                    dataElement.getAttribute(ShortcutPanel.AUTO_ATTRIBUTE_INITIAL_STATE));
            data.target = dataElement.getAttribute(ShortcutPanel.AUTO_ATTRIBUTE_TARGET);
            data.workingDirectory = dataElement
                    .getAttribute(ShortcutPanel.AUTO_ATTRIBUTE_WORKING_DIR);

            // Linux
            data.deskTopEntryLinux_Encoding = dataElement.getAttribute(
                    ShortcutPanel.SPEC_ATTRIBUTE_ENCODING, "");
            data.deskTopEntryLinux_MimeType = dataElement.getAttribute(
                    ShortcutPanel.SPEC_ATTRIBUTE_MIMETYPE, "");
            data.deskTopEntryLinux_Terminal = dataElement.getAttribute(
                    ShortcutPanel.SPEC_ATTRIBUTE_TERMINAL, "");
            data.deskTopEntryLinux_TerminalOptions = dataElement.getAttribute(
                    ShortcutPanel.SPEC_ATTRIBUTE_TERMINAL_OPTIONS, "");
            data.deskTopEntryLinux_Type = dataElement.getAttribute(
                    ShortcutPanel.SPEC_ATTRIBUTE_TYPE, "");

            data.deskTopEntryLinux_URL = dataElement.getAttribute(ShortcutPanel.SPEC_ATTRIBUTE_URL,
                    "");

            data.deskTopEntryLinux_X_KDE_SubstituteUID = dataElement.getAttribute(
                    ShortcutPanel.SPEC_ATTRIBUTE_KDE_SUBST_UID, "false");

            data.deskTopEntryLinux_X_KDE_UserName = dataElement.getAttribute(
                    ShortcutPanel.SPEC_ATTRIBUTE_KDE_USERNAME, "root");

            data.Categories = dataElement.getAttribute(ShortcutPanel.SPEC_CATEGORIES,
                    "Application;Development");

            data.TryExec = dataElement.getAttribute(ShortcutPanel.SPEC_TRYEXEC, "");

            data.createForAll = Boolean.valueOf(dataElement.getAttribute(ShortcutPanel.CREATE_FOR_ALL,
                    "false"));
            data.userType = Integer.valueOf(
                    dataElement.getAttribute(ShortcutPanel.USER_TYPE, Integer
                            .toString(Shortcut.CURRENT_USER)));
            // END LINUX
            shortcuts.add(data);
        }

        System.out.print("[ Creating shortcuts ");

        // ShortcutData data;
        for (int i = 0; i < shortcuts.size(); i++)
        {
            data = shortcuts.elementAt(i);

            try
            {
                if (data.subgroup != null)
                {
                    groupName = groupName + data.subgroup;
                }
                shortcut.setUserType(data.userType);
                shortcut.setLinkName(data.name);

                if (OsVersion.IS_WINDOWS)
                {
                    shortcut.setLinkType(data.type);
                }

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

                if (!OsVersion.IS_WINDOWS)
                {
                    shortcut.setType(data.deskTopEntryLinux_Type);
                }

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
                    // save the shortcut
                    System.out.print(".");
                    System.out.flush();

                    shortcut.save();

                    // add the file and directory name to the file list
                    String fileName = shortcut.getFileName();
                    files.add(0, fileName);

                    File file = new File(fileName);
                    File base = new File(shortcut.getBasePath());
                    Vector<File> intermediates = new Vector<File>();

                    // String directoryName = shortcut.getDirectoryCreated ();
                    execFiles.add(new ExecutableFile(fileName, ExecutableFile.UNINSTALL,
                            ExecutableFile.IGNORE, new ArrayList<OsConstraint>(), false));

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
                        Enumeration<File> filesEnum = intermediates.elements();

                        while (filesEnum.hasMoreElements())
                        {
                            files.add(0, filesEnum.nextElement().toString());
                        }
                    }
                }
                catch (Exception exception)
                {
                }
            }
            catch (Throwable exception)
            {
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

        System.out.println(" done. ]");
        System.out.print("[ Add shortcuts to uninstaller ");

        UninstallData uninstallData = UninstallData.getInstance();

        for (int i = 0; i < files.size(); i++)
        {
            uninstallData.addFile(files.elementAt(i), true);
            System.out.print(".");
            System.out.flush();
        }

        System.out.println(" done. ]");
    }
}
