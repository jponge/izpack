/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2010 Rene Krell
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

package com.izforge.izpack.util.config;

import com.izforge.izpack.util.Debug;
import org.ini4j.Ini;

import java.io.File;
import java.io.IOException;

public class SingleIniFileTask extends ConfigFileTask
{

    public static class Entry extends ConfigFileTask.Entry
    {

        /**
         * Name of the property name/value pair
         */
        public void setSection(String value)
        {
            this.section = value;
        }

    }

    protected void readSourceConfigurable() throws Exception
    {
        // deal with the single file to patch from
        if (!(oldFile != null && oldFile.exists()))
        {
            Debug.log("No file " + oldFile.getAbsolutePath()
                    + " to patch from found");
            return;
        }
        try
        {
            Debug.log("Loading INI file: " + oldFile.getAbsolutePath());
            // Configuration file type must be the same as the target type
            fromConfigurable = new Ini(this.oldFile);
        }
        catch (IOException ioe)
        {
            throw new Exception(ioe.toString());
        }
    }

    protected void readConfigurable() throws Exception
    {
        if (newFile != null)
        {
            try
            {
                if (!newFile.exists())
                {
                    throw new Exception("Reference file "
                            + newFile.getAbsolutePath() + " for patch cannot be found");
                }
                Debug.log("Loading INI file: " + newFile.getAbsolutePath());
                configurable = new Ini(newFile);
            }
            catch (IOException ioe)
            {
                throw new Exception(ioe.toString());
            }
        }
        else
        {
            configurable = new Ini();
        }
    }

    protected void writeConfigurable() throws Exception
    {

        try
        {
            if (!toFile.exists())
            {
                if (createConfigurable)
                {
                    File parent = toFile.getParentFile();
                    if (parent != null && !parent.exists())
                    {
                        parent.mkdirs();
                    }
                    Debug.log("Creating empty INI file: " + toFile.getAbsolutePath());
                    toFile.createNewFile();
                }
                else
                {
                    Debug.log("INI file " + toFile.getAbsolutePath()
                            + " did not exist and is not allowed to be created");
                    return;
                }
            }
            Ini ini = (Ini) configurable;
            ini.setFile(toFile);
            ini.setComment(getComment());
            ini.store();
        }
        catch (IOException ioe)
        {
            throw new Exception(ioe);
        }

        if (cleanup && oldFile.exists())
        {
            if (!oldFile.delete())
            {
                Debug.log("File " + oldFile + " could not be cleant up");
            }
        }
    }
}
