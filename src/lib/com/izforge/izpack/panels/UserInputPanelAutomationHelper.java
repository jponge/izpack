/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2003 Jonathan Halliday
 * Copyright 2002 Elmar Grom
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

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelAutomation;
import com.izforge.izpack.installer.InstallerException;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.adaptator.impl.XMLElementImpl;

import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

/**
 * Functions to support automated usage of the UserInputPanel
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
    private Map<String, String> entries;

    /**
     * Default constructor, used during automated installation.
     */
    public UserInputPanelAutomationHelper()
    {
        this.entries = null;
    }

    /**
     * @param entries String-String key-value pairs representing the state of the Panel
     */
    public UserInputPanelAutomationHelper(Map<String, String> entries)
    {
        this.entries = entries;
    }

    /**
     * Serialize state to XML and insert under panelRoot.
     *
     * @param idata     The installation data.
     * @param panelRoot The XML root element of the panels blackbox tree.
     */
    public void makeXMLData(AutomatedInstallData idata, IXMLElement panelRoot)
    {
        IXMLElement userInput;
        IXMLElement dataElement;

        // ----------------------------------------------------
        // add the item that combines all entries
        // ----------------------------------------------------
        userInput = new XMLElementImpl(AUTO_KEY_USER_INPUT,panelRoot);
        panelRoot.addChild(userInput);

        // ----------------------------------------------------
        // add all entries
        // ----------------------------------------------------
        Iterator<String> keys = this.entries.keySet().iterator();
        while (keys.hasNext())
        {
            String key = keys.next();
            String value = this.entries.get(key);
            dataElement = new XMLElementImpl(AUTO_KEY_ENTRY,userInput);
            dataElement.setAttribute(AUTO_ATTRIBUTE_KEY, key);
            dataElement.setAttribute(AUTO_ATTRIBUTE_VALUE, value);

            userInput.addChild(dataElement);
        }
    }

    /**
     * Deserialize state from panelRoot and set idata variables accordingly.
     *
     * @param idata     The installation data.
     * @param panelRoot The XML root element of the panels blackbox tree.
     * @throws InstallerException if some elements are missing.
     */
    public void runAutomated(AutomatedInstallData idata, IXMLElement panelRoot) throws InstallerException
    {
        IXMLElement userInput;
        IXMLElement dataElement;
        String variable;
        String value;

        // ----------------------------------------------------
        // get the section containing the user entries
        // ----------------------------------------------------
        userInput = panelRoot.getFirstChildNamed(AUTO_KEY_USER_INPUT);

        if (userInput == null)
        {
            throw new InstallerException("Missing userInput element on line " + panelRoot.getLineNr());
        }

        Vector<IXMLElement> userEntries = userInput.getChildrenNamed(AUTO_KEY_ENTRY);

        if (userEntries == null)
        {
            throw new InstallerException("Missing entry element(s) on line " + panelRoot.getLineNr());
        }

        // ----------------------------------------------------
        // retieve each entry and substitute the associated
        // variable
        // ----------------------------------------------------
        for (int i = 0; i < userEntries.size(); i++)
        {
            dataElement = userEntries.elementAt(i);
            variable = dataElement.getAttribute(AUTO_ATTRIBUTE_KEY);
            value = dataElement.getAttribute(AUTO_ATTRIBUTE_VALUE);

            Debug.trace("UserInputPanel: setting variable " + variable + " to " + value);
            idata.setVariable(variable, value);
        }
    }
}
