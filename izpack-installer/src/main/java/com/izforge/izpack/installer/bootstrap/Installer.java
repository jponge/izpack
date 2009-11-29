/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2003 Jonathan Halliday
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

package com.izforge.izpack.installer.bootstrap;

import com.izforge.izpack.data.ResourceManager;
import com.izforge.izpack.installer.base.*;
import com.izforge.izpack.installer.provider.IconsProvider;
import com.izforge.izpack.installer.provider.InstallDataProvider;
import com.izforge.izpack.installer.provider.InstallerFrameProvider;
import com.izforge.izpack.installer.provider.RulesProvider;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.StringTool;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.injectors.ProviderAdapter;

import javax.swing.*;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * The program entry point. Selects between GUI and text install modes.
 *
 * @author Jonathan Halliday
 */
public class Installer {

    public static final int INSTALLER_GUI = 0, INSTALLER_AUTO = 1, INSTALLER_CONSOLE = 2;
    public static final int CONSOLE_INSTALL = 0, CONSOLE_GEN_TEMPLATE = 1, CONSOLE_FROM_TEMPLATE = 2,
            CONSOLE_FROM_SYSTEMPROPERTIES = 3, CONSOLE_FROM_SYSTEMPROPERTIESMERGE = 4;

    private DefaultPicoContainer pico;

    /*
    * The main method (program entry point).
    *
    * @param args The arguments passed on the command-line.
    */

    public static void main(String[] args) {
        Installer installer = new Installer();
        installer.initBindings();
        installer.start(args);
    }

    private void start(String[] args) {
        Debug.log(" - Logger initialized at '" + new Date(System.currentTimeMillis()) + "'.");
        Debug.log(" - commandline args: " + StringTool.stringArrayToSpaceSeparatedString(args));

        // OS X tweakings
        if (System.getProperty("mrj.version") != null) {
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "IzPack");
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
            System.setProperty("com.apple.mrj.application.live-resize", "true");
        }

        try {
            Iterator<String> args_it = Arrays.asList(args).iterator();

            int type = INSTALLER_GUI;
            int consoleAction = CONSOLE_INSTALL;
            String path = null;
            String langcode = null;

            while (args_it.hasNext()) {
                String arg = args_it.next().trim();
                try {
                    if ("-console".equalsIgnoreCase(arg)) {
                        type = INSTALLER_CONSOLE;
                    } else if ("-options-template".equalsIgnoreCase(arg)) {
                        consoleAction = CONSOLE_GEN_TEMPLATE;
                        path = args_it.next().trim();
                    } else if ("-options".equalsIgnoreCase(arg)) {
                        consoleAction = CONSOLE_FROM_TEMPLATE;
                        path = args_it.next().trim();
                    } else if ("-options-system".equalsIgnoreCase(arg)) {
                        consoleAction = CONSOLE_FROM_SYSTEMPROPERTIES;
                    } else if ("-options-auto".equalsIgnoreCase(arg)) {
                        consoleAction = CONSOLE_FROM_SYSTEMPROPERTIESMERGE;
                        path = args_it.next().trim();
                    } else if ("-language".equalsIgnoreCase(arg)) {
                        langcode = args_it.next().trim();
                    } else {
                        type = INSTALLER_AUTO;
                        path = arg;
                    }
                }
                catch (NoSuchElementException e) {
                    System.err.println("- ERROR -");
                    System.err.println("Option \"" + arg + "\" requires an argument!");
                    System.exit(1);
                }
            }
            launchInstall(type, consoleAction, path, langcode);

        } catch (Exception e) {
            System.err.println("- ERROR -");
            System.err.println(e.toString());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void launchInstall(int type, int consoleAction, String path, String langcode) throws Exception {
        switch (type) {
            case INSTALLER_GUI:
                InstallerFrame installerFrame = pico.getComponent(InstallerFrame.class);
                loadGui(installerFrame);
                break;

            case INSTALLER_AUTO:
                pico.getComponent(AutomatedInstaller.class).doInstall();
                break;

            case INSTALLER_CONSOLE:
                ConsoleInstaller consoleInstaller = pico.getComponent(ConsoleInstaller.class);
                consoleInstaller.setLangCode(langcode);
                consoleInstaller.run(consoleAction, path);
                break;
        }
    }


    public void loadGui(final InstallerFrame installerFrame) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    installerFrame.enableFrame();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initBindings() {
        pico = new DefaultPicoContainer(new Caching());
        pico.addAdapter(new ProviderAdapter(new InstallDataProvider()))
                .addAdapter(new ProviderAdapter(new IconsProvider()))
                .addAdapter(new ProviderAdapter(new InstallerFrameProvider()))
                .addAdapter(new ProviderAdapter(new RulesProvider()));
        pico
                .addComponent(LanguageDialog.class)
                .addComponent(GUIInstaller.class)
                .addComponent(ResourceManager.class)
                .addComponent(ConsoleInstaller.class)
                .addComponent(AutomatedInstaller.class);
    }
}
