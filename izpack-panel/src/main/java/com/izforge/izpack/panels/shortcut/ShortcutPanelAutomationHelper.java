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

package com.izforge.izpack.panels.shortcut;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.binding.OsModel;
import com.izforge.izpack.data.ExecutableFile;
import com.izforge.izpack.installer.automation.PanelAutomation;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.util.*;
import com.izforge.izpack.util.os.Shortcut;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * The ShortcutPanelAutomationHelper is responsible to create Shortcuts during the automated
 * installation. Most code comes copied from the ShortcutPanel
 *
 * @author Marc Eppelmann (marc.eppelmann&#064;gmx.de)
 * @version $Revision: 1540 $
 */
public class ShortcutPanelAutomationHelper implements PanelAutomation
{
    private UninstallData uninstallData;

    public ShortcutPanelAutomationHelper(UninstallData uninstallData)
    {
        this.uninstallData = uninstallData;
    }

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
        List<ShortcutData> shortcuts = new ArrayList<ShortcutData>();

        List<ExecutableFile> execFiles = new ArrayList<ExecutableFile>();

        /**
         * Holds a list of all the shortcut files that have been created. Note: this variable
         * contains valid installDataGUI only after createShortcuts() has been called. This list is created so
         * that the files can be added to the uninstaller.
         */
        List<String> files = new ArrayList<String>();

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
        // installDataGUI. Just return.
        // ----------------------------------------------------
        if (!shortcut.supported())
        {
            Debug.log("shortcuts not supported here");

            return;
        }

        if (!OsConstraintHelper.oneMatchesCurrentSystem(panelRoot))
        {
            Debug.log("Shortcuts Not oneMatchesCurrentSystem");

            return;
        }

        List<IXMLElement> shortcutElements;
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

        for (IXMLElement shortcutElement : shortcutElements)
        {
            Debug.log(this.getClass().getName() + "runAutomated:shortcutElements " + shortcutElement);
            ShortcutData data = new ShortcutData();

            data.name = shortcutElement.getAttribute(ShortcutPanel.AUTO_ATTRIBUTE_NAME);
            data.addToGroup = Boolean.valueOf(
                    shortcutElement.getAttribute(ShortcutPanel.AUTO_ATTRIBUTE_GROUP));

            if (OsVersion.IS_WINDOWS)
            {
                data.type = Integer.valueOf(
                        shortcutElement.getAttribute(ShortcutPanel.AUTO_ATTRIBUTE_TYPE));
            }
            else
            {
                Debug.log("WARN: On Linux installDataGUI.type is NOT an int. Ignored.");
            }

            data.commandLine = shortcutElement.getAttribute(ShortcutPanel.AUTO_ATTRIBUTE_COMMAND);
            data.description = shortcutElement.getAttribute(ShortcutPanel.AUTO_ATTRIBUTE_DESCRIPTION);
            data.iconFile = shortcutElement.getAttribute(ShortcutPanel.AUTO_ATTRIBUTE_ICON);
            data.iconIndex = Integer.valueOf(
                    shortcutElement.getAttribute(ShortcutPanel.AUTO_ATTRIBUTE_ICON_INDEX));
            data.initialState = Integer.valueOf(
                    shortcutElement.getAttribute(ShortcutPanel.AUTO_ATTRIBUTE_INITIAL_STATE));
            data.target = shortcutElement.getAttribute(ShortcutPanel.AUTO_ATTRIBUTE_TARGET);
            data.workingDirectory = shortcutElement
                    .getAttribute(ShortcutPanel.AUTO_ATTRIBUTE_WORKING_DIR);

            // Linux
            data.deskTopEntryLinux_Encoding = shortcutElement.getAttribute(
                    ShortcutPanel.SPEC_ATTRIBUTE_ENCODING, "");
            data.deskTopEntryLinux_MimeType = shortcutElement.getAttribute(
                    ShortcutPanel.SPEC_ATTRIBUTE_MIMETYPE, "");
            data.deskTopEntryLinux_Terminal = shortcutElement.getAttribute(
                    ShortcutPanel.SPEC_ATTRIBUTE_TERMINAL, "");
            data.deskTopEntryLinux_TerminalOptions = shortcutElement.getAttribute(
                    ShortcutPanel.SPEC_ATTRIBUTE_TERMINAL_OPTIONS, "");
            data.deskTopEntryLinux_Type = shortcutElement.getAttribute(
                    ShortcutPanel.SPEC_ATTRIBUTE_TYPE, "");

            data.deskTopEntryLinux_URL = shortcutElement.getAttribute(ShortcutPanel.SPEC_ATTRIBUTE_URL,
                    "");

            data.deskTopEntryLinux_X_KDE_SubstituteUID = shortcutElement.getAttribute(
                    ShortcutPanel.SPEC_ATTRIBUTE_KDE_SUBST_UID, "false");

            data.deskTopEntryLinux_X_KDE_UserName = shortcutElement.getAttribute(
                    ShortcutPanel.SPEC_ATTRIBUTE_KDE_USERNAME, "root");

            data.Categories = shortcutElement.getAttribute(ShortcutPanel.SPEC_CATEGORIES,
                    "Application;Development");

            data.TryExec = shortcutElement.getAttribute(ShortcutPanel.SPEC_TRYEXEC, "");

            data.createForAll = Boolean.valueOf(shortcutElement.getAttribute(ShortcutPanel.CREATE_FOR_ALL,
                    "false"));
            data.userType = Integer.valueOf(
                    shortcutElement.getAttribute(ShortcutPanel.USER_TYPE, Integer
                            .toString(Shortcut.CURRENT_USER)));
            // END LINUX
            shortcuts.add(data);
        }

        System.out.print("[ Creating shortcuts ");

        // ShortcutData installDataGUI;
        for (ShortcutData shortcutData : shortcuts)
        {
            try
            {
                if (shortcutData.subgroup != null)
                {
                    groupName = groupName + shortcutData.subgroup;
                }
                shortcut.setUserType(shortcutData.userType);
                shortcut.setLinkName(shortcutData.name);

                if (OsVersion.IS_WINDOWS)
                {
                    shortcut.setLinkType(shortcutData.type);
                }

                shortcut.setArguments(shortcutData.commandLine);
                shortcut.setDescription(shortcutData.description);
                shortcut.setIconLocation(shortcutData.iconFile, shortcutData.iconIndex);

                shortcut.setShowCommand(shortcutData.initialState);
                shortcut.setTargetPath(shortcutData.target);
                shortcut.setWorkingDirectory(shortcutData.workingDirectory);
                shortcut.setEncoding(shortcutData.deskTopEntryLinux_Encoding);
                shortcut.setMimetype(shortcutData.deskTopEntryLinux_MimeType);

                shortcut.setTerminal(shortcutData.deskTopEntryLinux_Terminal);
                shortcut.setTerminalOptions(shortcutData.deskTopEntryLinux_TerminalOptions);

                if (!OsVersion.IS_WINDOWS)
                {
                    shortcut.setType(shortcutData.deskTopEntryLinux_Type);
                }

                shortcut.setKdeSubstUID(shortcutData.deskTopEntryLinux_X_KDE_SubstituteUID);
                shortcut.setURL(shortcutData.deskTopEntryLinux_URL);
                shortcut.setCreateForAll(shortcutData.createForAll);

                if (shortcutData.addToGroup)
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
                    List<File> intermediates = new ArrayList<File>();

                    // String directoryName = shortcut.getDirectoryCreated ();
                    execFiles.add(new ExecutableFile(fileName, ExecutableFile.UNINSTALL,
                            ExecutableFile.IGNORE, new ArrayList<OsModel>(), false));

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
                        for (File intermediateFile : intermediates)
                        {
                            files.add(0, intermediateFile.toString());
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

        for (String file : files)
        {
            uninstallData.addFile(file, true);
            System.out.print(".");
            System.out.flush();
        }

        System.out.println(" done. ]");
    }
}
