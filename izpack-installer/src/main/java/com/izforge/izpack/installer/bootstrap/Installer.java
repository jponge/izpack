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

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.izforge.izpack.installer.automation.AutomatedInstaller;
import com.izforge.izpack.installer.console.ConsoleInstaller;
import com.izforge.izpack.installer.container.impl.ConsoleInstallerContainer;
import com.izforge.izpack.installer.container.impl.InstallerContainer;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.StringTool;

/**
 * The program entry point. Selects between GUI and text install modes.
 *
 * @author Jonathan Halliday
 * @author René Krell
 */
public class Installer
{
    private static Logger logger;

    public static final int INSTALLER_GUI = 0, INSTALLER_AUTO = 1, INSTALLER_CONSOLE = 2;
    public static final int CONSOLE_INSTALL = 0, CONSOLE_GEN_TEMPLATE = 1, CONSOLE_FROM_TEMPLATE = 2,
            CONSOLE_FROM_SYSTEMPROPERTIES = 3, CONSOLE_FROM_SYSTEMPROPERTIESMERGE = 4;

    public static final String LOGGING_CONFIGURATION = "/com/izforge/izpack/installer/logging/logging.properties";

    /*
     * The main method (program entry point).
     *
     * @param args The arguments passed on the command-line.
     */
    public static void main(String[] args)
    {
        try
        {
            initializeLogging();
            Installer installer = new Installer();
            installer.start(args);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private static void initializeLogging()
    {
        LogManager manager = LogManager.getLogManager();
        InputStream stream = null;
        try
        {
            stream = Installer.class.getResourceAsStream(LOGGING_CONFIGURATION);
            if (stream != null)
            {
                manager.readConfiguration(stream);
                //System.out.println("Read logging configuration from resource " + LOGGING_CONFIGURATION);
            }
            else
            {
                //System.err.println("Logging configuration resource " + LOGGING_CONFIGURATION + " not found");
            }
        }
        catch (IOException e)
        {
            //System.err.println("Error loading logging configuration resource " + LOGGING_CONFIGURATION + ": " + e);
        }

        Logger rootLogger = Logger.getLogger("com.izforge.izpack");
        rootLogger.setUseParentHandlers(false);
        if (Debug.isDEBUG())
        {
            rootLogger.setLevel(Level.FINE);
        }
        else
        {
            rootLogger.setLevel(Level.INFO);
        }

        logger = Logger.getLogger(Installer.class.getName());
        logger.info("Logger initialized at level '" + logger.getParent().getLevel() + "'");
    }

    private void start(String[] args)
    {
        logger.info("Commandline arguments: " + StringTool.stringArrayToSpaceSeparatedString(args));

        // OS X tweakings
        if (System.getProperty("mrj.version") != null)
        {
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "IzPack");
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
            System.setProperty("com.apple.mrj.application.live-resize", "true");
        }

        try
        {
            Iterator<String> args_it = Arrays.asList(args).iterator();

            int type = INSTALLER_GUI;
            int consoleAction = CONSOLE_INSTALL;
            String path = null, langcode = null;

            while (args_it.hasNext())
            {
                String arg = args_it.next().trim();
                try
                {
                    if ("-console".equalsIgnoreCase(arg))
                    {
                        type = INSTALLER_CONSOLE;
                    }
                    else if ("-options-template".equalsIgnoreCase(arg))
                    {
                        type = INSTALLER_CONSOLE;
                        consoleAction = CONSOLE_GEN_TEMPLATE;
                        path = args_it.next().trim();
                    }
                    else if ("-options".equalsIgnoreCase(arg))
                    {
                        type = INSTALLER_CONSOLE;
                        consoleAction = CONSOLE_FROM_TEMPLATE;
                        path = args_it.next().trim();
                    }
                    else if ("-options-system".equalsIgnoreCase(arg))
                    {
                        type = INSTALLER_CONSOLE;
                        consoleAction = CONSOLE_FROM_SYSTEMPROPERTIES;
                    }
                    else if ("-options-auto".equalsIgnoreCase(arg))
                    {
                        type = INSTALLER_CONSOLE;
                        consoleAction = CONSOLE_FROM_SYSTEMPROPERTIESMERGE;
                        path = args_it.next().trim();
                    }
                    else if ("-language".equalsIgnoreCase(arg))
                    {
                        langcode = args_it.next().trim();
                    }
                    else
                    {
                        type = INSTALLER_AUTO;
                        path = arg;
                    }
                }
                catch (NoSuchElementException e)
                {
                    logger.log(Level.SEVERE, "Option \"" + arg + "\" requires an argument", e);
                    System.exit(1);
                }
            }

            launchInstall(type, consoleAction, path, langcode);

        }
        catch (Exception e)
        {
            logger.log(Level.SEVERE, e.getMessage(), e);
            System.exit(1);
        }
    }

    private void launchInstall(int type, int consoleAction, String path, String langcode) throws Exception
    {
        // if headless, just use the console mode
        if (type == INSTALLER_GUI && GraphicsEnvironment.isHeadless())
        {
            type = INSTALLER_CONSOLE;
        }

        switch (type)
        {
            case INSTALLER_GUI:
                InstallerGui.run();
                break;

            case INSTALLER_AUTO:
                launchAutomatedInstaller(path);
                break;

            case INSTALLER_CONSOLE:
                launchConsoleInstaller(consoleAction, path, langcode);
                break;
        }
    }

    /**
     * Launches an {@link AutomatedInstaller}.
     *
     * @param path the input file path
     * @throws Exception for any error
     */
    private void launchAutomatedInstaller(String path) throws Exception
    {
        InstallerContainer container = new ConsoleInstallerContainer();
        container.initBindings();
        AutomatedInstaller automatedInstaller = container.getComponent(AutomatedInstaller.class);
        automatedInstaller.init(path);
        automatedInstaller.doInstall();
    }

    /**
     * Launches an {@link ConsoleInstaller}.
     *
     * @param consoleAction the type of the action to perform
     * @param path          the path to use for the action. May be <tt>null</tt>
     * @param langCode      the language code. May be <tt>null</tt>
     */
    private void launchConsoleInstaller(int consoleAction, String path, String langCode)
    {
        InstallerContainer container = new ConsoleInstallerContainer();
        container.initBindings();
        ConsoleInstaller consoleInstaller = container.getComponent(ConsoleInstaller.class);
        consoleInstaller.setLangCode(langCode);
        consoleInstaller.run(consoleAction, path);
    }

}
