/*
 * $Id$
 * IzPack
 * Copyright (C) 2002-2003 Jonathan Halliday, Elmar Grom
 *
 * File :               UserInputPanelAutomationHelper.java
 * Description :        Automation support functions for UserInputPanel.
 * Author's email :     elmar@grom.net
 * Author's Website :   http://www.izforge.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.izforge.izpack.panels;

import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import net.n3.nanoxml.XMLElement;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelAutomation;
import com.izforge.izpack.util.Debug;

/**
 *  Functions to support automated usage of the UserInputPanel
 *
 * @author Jonathan Halliday
 * @author Elmar Grom
 */
public class UserInputPanelAutomationHelper implements PanelAutomation
{
  // ------------------------------------------------------
  // automatic script section keys
  // ------------------------------------------------------
  private static final String AUTO_KEY_USER_INPUT = "userInput";
  private static final String AUTO_KEY_ENTRY = "entry";

  // ------------------------------------------------------
  // automatic script keys attributes
  // ------------------------------------------------------
  private static final String AUTO_ATTRIBUTE_KEY = "key";
  private static final String AUTO_ATTRIBUTE_VALUE = "value";

  // ------------------------------------------------------
  // String-String key-value pairs
  // ------------------------------------------------------
  private Map entries;

  public UserInputPanelAutomationHelper ()
  {
    this.entries = null;
  }

  /**
   *
   * @param entries String-String key-value pairs representing the state of the Panel
   */
  public UserInputPanelAutomationHelper(Map entries)
  {
    this.entries = entries;
  }

  /**
   * Serialize state to XML and insert under panelRoot.
   *
   * @param idata The installation data.
   * @param panelRoot The XML root element of the panels blackbox tree.
   */
  public void makeXMLData(AutomatedInstallData idata, XMLElement panelRoot)
  {
    XMLElement userInput;
    XMLElement dataElement;

    // ----------------------------------------------------
    // add the item that combines all entries
    // ----------------------------------------------------
    userInput = new XMLElement(AUTO_KEY_USER_INPUT);
    panelRoot.addChild(userInput);

    // ----------------------------------------------------
    // add all entries
    // ----------------------------------------------------
    Iterator keys = entries.keySet().iterator();
    while (keys.hasNext())
    {
      String key = (String) keys.next();
      String value = (String) entries.get(key);

      dataElement = new XMLElement(AUTO_KEY_ENTRY);
      dataElement.setAttribute(AUTO_ATTRIBUTE_KEY, key);
      dataElement.setAttribute(AUTO_ATTRIBUTE_VALUE, value);

      userInput.addChild(dataElement);
    }
  }

  /**
   * Deserialize state from panelRoot and set idata variables accordingly.
   *
   * @param idata The installation data.
   * @param panelRoot The XML root element of the panels blackbox tree.
   */
  public void runAutomated(AutomatedInstallData idata, XMLElement panelRoot)
  {
    XMLElement userInput;
    XMLElement dataElement;
    String variable;
    String value;

    // ----------------------------------------------------
    // get the section containing the user entries
    // ----------------------------------------------------
    userInput = panelRoot.getFirstChildNamed(AUTO_KEY_USER_INPUT);

    if (userInput == null)
    {
      return;
    }

    Vector userEntries = userInput.getChildrenNamed(AUTO_KEY_ENTRY);

    if (userEntries == null)
    {
      return;
    }

    // ----------------------------------------------------
    // retieve each entry and substitute the associated
    // variable
    // ----------------------------------------------------
    for (int i = 0; i < userEntries.size(); i++)
    {
      dataElement = (XMLElement) userEntries.elementAt(i);
      variable = dataElement.getAttribute(AUTO_ATTRIBUTE_KEY);
      value = dataElement.getAttribute(AUTO_ATTRIBUTE_VALUE);

      Debug.trace ("UserInputPanel: setting variable "+variable+" to "+value);
      idata.setVariable(variable, value);
    }
  }
}
