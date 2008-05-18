/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2008 J. Chris Folsom
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

package com.izforge.izpack.gui;

import com.izforge.izpack.LocaleDatabase;
import com.izforge.izpack.installer.ResourceManager;
import com.izforge.izpack.util.Debug;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.InputStream;

/**
 * Allows a file if it is a directory or if it ends with .xml. The description
 * is set using the langpack id <b>FinishPanel.automated.dialog.filterdesc</b>
 * <br />
 * <br />
 * <p/>
 * Note:
 * <p/>
 * This file could be replaced in java 1.6 by
 * javax.swing.filechooser.FileNameExtensionFilter, although loading the value
 * from a lang pack would have to use an external reference, so it may be better
 * to keep this class since it has the language pack logic encapsulated.
 *
 * @author J. Chris Folsom <jchrisfolsom@gmail.com>
 */
public class AutomatedInstallScriptFilter extends FileFilter
{
    /**
     * The default description for the file filter if it cannot be loaded from
     * the LocaleDatabase.
     */
    public static final String DEFAULT_DESCRIPTION = "XML Files";

    /**
     * This key will be used to search the locale database for the description
     * to display. If it cannot be found the default value will be used.
     */
    public static final String DESCRIPTION_LOCALE_DATABASE_KEY = "FinishPanel.auto.dialog.filterdesc";

    /*
      * (non-Javadoc)
      *
      * @see java.io.FileFilter#accept(java.io.File)
      */
    public boolean accept(File pathname)
    {
        /*
           * Return true only if the file is a directory or ends with ".xml"
           */
        return pathname != null
                && (pathname.isDirectory() || pathname.getName().endsWith(
                ".xml"));
    }

    /**
     *
     */
    public String getDescription()
    {
        // Load the lang pack using the current locale
        ResourceManager resourceManager = ResourceManager.getInstance();
        String locale = resourceManager.getLocale();

        String description = null;

        try
        {
            //All of this will be changed to a few lines of code after the locale database refactor.
            InputStream in = this.getClass().getResourceAsStream(
                    "/langpacks/" + locale + ".xml");
            LocaleDatabase langpack = new LocaleDatabase(in);

            description = langpack.getString(DESCRIPTION_LOCALE_DATABASE_KEY);

        }
        catch (Exception e)
        {
            /*
                * We'll just log the exception if something happens and provide the
                * default value.
                */
            Debug.log(e);
        }

        if (description == null)
        {
            description = DEFAULT_DESCRIPTION;
        }

        return description;
    }
}
