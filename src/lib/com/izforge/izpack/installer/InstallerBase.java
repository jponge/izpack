/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2003 Jonathan Halliday, Julien Ponge
 *
 *  File :               InstallerBase.java
 *  Description :        Utility functions shared by the GUI and headless installers.
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

import java.io.DataInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;

import com.izforge.izpack.Info;
import com.izforge.izpack.Pack;
import com.izforge.izpack.util.OsConstraint;

/**
 * Common utility functions for the GUI and text installers.
 * (Do not import swing/awt classes to this class.)
 *
 * @author Jonathan Halliday
 * @author Julien Ponge
 */
public class InstallerBase
{

  /**
   *  Loads the installation data.
   *
   * @exception  Exception  Description of the Exception
   */
  public void loadInstallData(AutomatedInstallData installdata) throws Exception
  {
    // Usefull variables
    InputStream in;
    DataInputStream datIn;
    ObjectInputStream objIn;
    int size;
    int i;

    // We load the variables
    Properties variables = null;
    in = getClass().getResourceAsStream("/vars");
    if (null != in)
    {
      objIn = new ObjectInputStream(in);
      variables = (Properties) objIn.readObject();
      objIn.close();
    }

    // We load the Info data
    in = getClass().getResourceAsStream("/info");
    objIn = new ObjectInputStream(in);
    Info inf = (Info) objIn.readObject();
    objIn.close();

    // We put the Info data as variables
    installdata.setVariable(ScriptParser.APP_NAME, inf.getAppName());
    installdata.setVariable(ScriptParser.APP_URL, inf.getAppURL());
    installdata.setVariable(ScriptParser.APP_VER, inf.getAppVersion());

    // We read the panels order data
    in = getClass().getResourceAsStream("/panelsOrder");
    datIn = new DataInputStream(in);
    size = datIn.readInt();
    ArrayList panelsOrder = new ArrayList();
    for (i = 0; i < size; i++)
      panelsOrder.add(datIn.readUTF());
    datIn.close();

    String os = System.getProperty("os.name");
    // We read the packs data
    in = getClass().getResourceAsStream("/packs.info");
    objIn = new ObjectInputStream(in);
    size = objIn.readInt();
    ArrayList availablePacks = new ArrayList();
    ArrayList allPacks = new ArrayList();
    for (i = 0; i < size; i++)
    {
      Pack pk = (Pack) objIn.readObject();
      allPacks.add(pk);
      if (OsConstraint.oneMatchesCurrentSystem(pk.osConstraints))
        availablePacks.add(pk);
    }
    objIn.close();

    // We determine the operating system and the initial installation path
    String user = System.getProperty("user.name");
    String dir;
    String installPath;
    if (os.regionMatches(true, 0, "windows", 0, 7))
    {
      dir = buildWindowsDefaultPath();
    }
    else if (os.regionMatches(true, 0, "mac os x", 0, 6))
    {
      dir = "/Applications" + File.separator;
    }
    else if (os.regionMatches(true, 0, "mac", 0, 3))
    {
      dir = "";
    }
    else
    {
      if (user.equals("root"))
      {
        dir = "/usr/local" + File.separator;
      }
      else
      {
        dir = System.getProperty("user.home") + File.separator;
      }
    }

    installPath = dir + inf.getAppName();

    // We read the installation kind
    in = getClass().getResourceAsStream("/kind");
    datIn = new DataInputStream(in);
    String kind = datIn.readUTF();
    datIn.close();

    installdata.setInstallPath(installPath);
    installdata.setVariable
      (ScriptParser.JAVA_HOME, System.getProperty("java.home"));
    installdata.setVariable
      (ScriptParser.USER_HOME, System.getProperty("user.home"));
    installdata.setVariable
      (ScriptParser.USER_NAME, System.getProperty("user.name"));
    installdata.setVariable
      (ScriptParser.FILE_SEPARATOR, File.separator);
    if (null != variables)
    {
      Enumeration enum = variables.keys();
      String varName = null;
      String varValue = null;
      while (enum.hasMoreElements())
      {
        varName = (String) enum.nextElement();
        varValue = variables.getProperty(varName);
        installdata.setVariable(varName, varValue);
      }
    }
    installdata.info = inf;
    installdata.kind = kind;
    installdata.panelsOrder = panelsOrder;
    installdata.availablePacks = availablePacks;
    installdata.allPacks = allPacks;

    // get list of preselected packs
    Iterator pack_it = availablePacks.iterator();
    while (pack_it.hasNext())
    {
      Pack pack = (Pack)pack_it.next();
      if (pack.preselected)
        installdata.selectedPacks.add (pack);
    }

  }

  /**
   * Builds the default path for Windows (i.e Program Files/...).
   * @ return The Windows default installation path.
   */
  private String buildWindowsDefaultPath()
  {
    String dpath = "";
    try
    {
      // We load the properties
      Properties props = new Properties();
      props.load(InstallerBase.class.getResourceAsStream(
        "/com/izforge/izpack/installer/win32-defaultpaths.properties"));

      // We look for the drive mapping
      String drive = System.getProperty("user.home");
      if (drive.length() > 3) drive = drive.substring(0, 3);

      // Now we have it :-)
      String language = Locale.getDefault().getLanguage();
      dpath = drive
            + props.getProperty(language, props.getProperty("en")) + "\\";
    }
    catch (Exception err)
    {
      dpath = "C:\\Program Files\\";
    }

    return dpath;
  }
}
