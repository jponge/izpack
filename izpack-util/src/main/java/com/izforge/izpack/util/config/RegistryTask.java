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

import java.io.IOException;

import org.ini4j.Reg;

import com.izforge.izpack.util.Debug;

public class RegistryTask extends SingleConfigurableTask {

  /*
   * Instance variables.
   */

  protected String key;
  protected String fromKey;


  /**
  * Location of the configuration file to be edited; required.
  */
  public void setKey(String key) {
    this.key = key;
  }

  /**
  * Location of the configuration file to be patched from; optional.
  */
  public void setFromKey(String key) {
    this.fromKey = key;
  }

  public static class Entry extends SingleConfigurableTask.Entry {

    public void setKey(String key) {
      // Name of the root key in registry
      this.section = key;
    }

    public void setValue(String value) {
      // Name of the registry value ("value" is a key in registry meaning)
      this.key = value;
    }

    /**
     * Registry data
     */
    public void setData(String data) {
      this.value = data;
    }

  }

  protected void readSourceConfigurable() throws Exception {
    // deal with a registry key to patch from
    if (this.fromKey != null) {
      try {
          Debug.log("Loading from registry: " + this.fromKey);
          fromConfigurable = new Reg(this.fromKey);
      } catch (IOException ioe) {
        throw new Exception(ioe.toString());
      }
    }
  }

  protected void readConfigurable() throws Exception {
    if (this.key != null) {
      try {
          Debug.log("Loading from registry: " + this.key);
          configurable = new Reg(this.key);
      } catch (IOException ioe) {
        throw new Exception(ioe.toString());
      }
    }
  }

  protected void writeConfigurable() throws Exception {

    if (configurable == null) {
        Debug.log("Registry key " +
          this.key +
          " did not exist and is not allowed to be created");
      return;
    }

    try {
      Reg r = (Reg) configurable;
      r.store();
    } catch (IOException ioe) {
      throw new Exception(ioe);
    }
  }

  protected void checkAttributes() throws Exception {
    if (this.key == null) {
      throw new Exception("Key attribute must be set");
    }
  }

}
