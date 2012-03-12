/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2004 Klaus Bartz
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

package com.izforge.izpack.event;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.api.exception.WrappedNativeLibException;
import com.izforge.izpack.api.handler.AbstractUIProgressHandler;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.api.unpacker.IDiscardInterruptable;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.core.os.RegistryHandler;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.util.CleanupClient;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.helper.SpecHelper;

import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Installer custom action for handling registry entries on Windows. On Unix nothing will be done.
 * The actions which should be performed are defined in a resource file named "RegistrySpec.xml".
 * This resource should be declared in the installation definition file (install.xml), else an
 * exception will be raised during execution of this custom action. The related DTD is
 * appl/install/IzPack/resources/registry.dtd.
 *
 * @author Klaus Bartz
 */
public class RegistryInstallerListener extends NativeInstallerListener implements CleanupClient
{
    private static final Logger logger = Logger.getLogger(RegistryInstallerListener.class.getName());

    /**
     * The name of the XML file that specifies the registry entries.
     */
    private static final String SPEC_FILE_NAME = "RegistrySpec.xml";

    private static final String REG_KEY = "key";

    private static final String REG_VALUE = "value";

    private static final String REG_ROOT = "root";

    private static final String REG_BASENAME = "name";

    private static final String REG_KEYPATH = "keypath";

    private static final String REG_DWORD = "dword";

    private static final String REG_STRING = "string";

    private static final String REG_MULTI = "multi";

    private static final String REG_BIN = "bin";

    private static final String REG_DATA = "data";

    private static final String REG_OVERRIDE = "override";

    private static final String SAVE_PREVIOUS = "saveprevious";

    private RulesEngine rules;

    private List registryModificationLog;

    private IDiscardInterruptable unpacker;
    private VariableSubstitutor variableSubstitutor;

    /**
     * The installation data.
     */
    private final AutomatedInstallData installData;

    /**
     * The uninstallation data.
     */
    private final UninstallData uninstallData;

    /**
     * The house-keeper.
     */
    private final Housekeeper housekeeper;

    /**
     * The registry handler reference.
     */
    private final RegistryDefaultHandler handler;


    /**
     * Constructs a <tt>RegistryInstallerListener</tt>.
     *
     * @param unpacker            the unpacker
     * @param variableSubstitutor the variable substituter
     * @param installData         the installation data
     * @param uninstallData       the uninstallation data
     * @param resources           the resource manager
     * @param housekeeper         the housekeeper
     * @param handler             the registry handler reference
     */
    public RegistryInstallerListener(IDiscardInterruptable unpacker, VariableSubstitutor variableSubstitutor,
                                     AutomatedInstallData installData, UninstallData uninstallData,
                                     ResourceManager resources, Housekeeper housekeeper, RegistryDefaultHandler handler)
    {
        super(resources, true);
        this.variableSubstitutor = variableSubstitutor;
        this.unpacker = unpacker;
        this.installData = installData;
        this.uninstallData = uninstallData;
        this.housekeeper = housekeeper;
        this.handler = handler;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.izforge.izpack.compiler.InstallerListener#beforePacks(com.izforge.izpack.installer.AutomatedInstallData,
     * int, com.izforge.izpack.api.handler.AbstractUIProgressHandler)
     */

    public void beforePacks(AutomatedInstallData installData, Integer npacks,
                            AbstractUIProgressHandler handler) throws Exception
    {
        super.beforePacks(installData, npacks, handler);
        rules = installData.getRules();
        initializeRegistryHandler(installData);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.izforge.izpack.compiler.InstallerListener#afterPacks(com.izforge.izpack.installer.AutomatedInstallData,
     * com.izforge.izpack.api.handler.AbstractUIProgressHandler)
     */

    public void afterPacks(AutomatedInstallData idata, AbstractUIProgressHandler handler)
            throws Exception
    {
        RegistryHandler registryHandler = this.handler.getInstance();
        if (registryHandler == null)
        {
            return;
        }
        try
        {
            // Register for cleanup
            housekeeper.registerForCleanup(this);

            // Start logging
            IXMLElement uninstallerPack = null;
            // No interrupt desired after writing registry entries.
            unpacker.setDiscardInterrupt(true);
            registryHandler.activateLogging();

            if (getSpecHelper().getSpec() != null)
            {
                // Get the special pack "UninstallStuff" which contains values
                // for the uninstaller entry.
                uninstallerPack = getSpecHelper().getPackForName("UninstallStuff");
                performPack(uninstallerPack, variableSubstitutor, registryHandler);

                // Now perform the selected packs.
                for (Pack selectedPack : idata.getSelectedPacks())
                {
                    // Resolve data for current pack.
                    IXMLElement pack = getSpecHelper().getPackForName(selectedPack.name);
                    performPack(pack, variableSubstitutor, registryHandler);

                }
            }
            String uninstallSuffix = idata.getVariable("UninstallKeySuffix");
            if (uninstallSuffix != null)
            {
                registryHandler.setUninstallName(registryHandler.getUninstallName() + " " + uninstallSuffix);
            }
            // Generate uninstaller key automatically if not defined in spec.
            if (uninstallerPack == null)
            {
                registryHandler.registerUninstallKey();
            }
            // Get the logging info from the registry class and put it into
            // the uninstaller. The RegistryUninstallerListener loads that data
            // and rewind the made entries.
            // This is the common way to transport informations from an
            // installer CustomAction to the corresponding uninstaller
            // CustomAction.
            List<Object> info = registryHandler.getLoggingInfo();
            if (info != null)
            {
                uninstallData.addAdditionalData("registryEntries", info);
            }
            // Remember all registry info to rewind registry modifications in case of failed installation
            registryModificationLog = info;
        }
        catch (Exception e)
        {
            if (e instanceof NativeLibException)
            {
                throw new WrappedNativeLibException(e);
            }
            else
            {
                throw e;
            }
        }
    }


    /**
     * Remove all registry entries on failed installation
     */
    public void cleanUp()
    {
        // installation was not successful now rewind all registry changes
        if (installData.isInstallSuccess() || registryModificationLog == null || registryModificationLog.size() < 1)
        {
            return;
        }
        RegistryHandler registryHandler = handler.getInstance();
        if (registryHandler != null)
        {
            try
            {
                registryHandler.activateLogging();
                registryHandler.setLoggingInfo(registryModificationLog);
                registryHandler.rewind();
            }
            catch (Exception e)
            {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    /**
     * Performs the registry settings for the given pack.
     *
     * @param pack            XML elemtent which contains the registry settings for one pack
     * @param registryHandler the registry handler
     * @throws Exception
     */
    private void performPack(IXMLElement pack, VariableSubstitutor substitutor, RegistryHandler registryHandler)
            throws Exception
    {
        if (pack == null)
        {
            return;
        }
        String packcondition = pack.getAttribute("condition");
        if (packcondition != null)
        {
            logger.fine("Condition \"" + packcondition + "\" found for pack of registry entries");
            if (!rules.isConditionTrue(packcondition))
            {
                // condition not fulfilled, continue with next element.
                logger.fine("Condition \"" + packcondition + "\" not true");
                return;
            }
        }

        // Get all entries for registry settings.
        List<IXMLElement> regEntries = pack.getChildren();
        if (regEntries == null)
        {
            return;
        }
        for (IXMLElement regEntry : regEntries)
        {
            String condition = regEntry.getAttribute("condition");
            if (condition != null)
            {
                logger.fine("Condition " + condition + " found for registry entry");
                if (!rules.isConditionTrue(condition))
                {
                    // condition not fulfilled, continue with next element.
                    logger.fine("Condition \"" + condition + "\" not true");
                    continue;
                }
            }

            // Perform one registry entry.
            String type = regEntry.getName();
            if (type.equalsIgnoreCase(REG_KEY))
            {
                performKeySetting(regEntry, substitutor, registryHandler);
            }
            else if (type.equalsIgnoreCase(REG_VALUE))
            {
                performValueSetting(regEntry, substitutor, registryHandler);
            }
            else
            // No valid type.
            {
                getSpecHelper().parseError(regEntry,
                        "Non-valid type of entry; only 'key' and 'value' are allowed.");
            }

        }

    }

    /**
     * Perform the setting of one value.
     *
     * @param regEntry        element which contains the description of the value to be set
     * @param substitutor     variable substitutor to be used for revising the regEntry contents
     * @param registryHandler the registry handler
     */
    private void performValueSetting(IXMLElement regEntry, VariableSubstitutor substitutor,
                                     RegistryHandler registryHandler)
            throws Exception
    {
        SpecHelper specHelper = getSpecHelper();
        String name = specHelper.getRequiredAttribute(regEntry, REG_BASENAME);
        name = substitutor.substitute(name);
        String keypath = specHelper.getRequiredAttribute(regEntry, REG_KEYPATH);
        keypath = substitutor.substitute(keypath);
        String root = specHelper.getRequiredAttribute(regEntry, REG_ROOT);
        int rootId = resolveRoot(regEntry, root, substitutor);

        registryHandler.setRoot(rootId);

        String override = regEntry.getAttribute(REG_OVERRIDE, "true");
        if (!"true".equalsIgnoreCase(override))
        { // Do not set value if override is not true and the value exist.

            if (registryHandler.getValue(keypath, name, null) != null)
            {
                return;
            }
        }

        //set flag for logging previous contents if "saveprevious"
        // attribute not specified or specified as 'true':
        registryHandler.setLogPrevSetValueFlag("true".equalsIgnoreCase(
                regEntry.getAttribute(SAVE_PREVIOUS, "true")));

        String value = regEntry.getAttribute(REG_DWORD);
        if (value != null)
        { // Value type is DWord; placeholder possible.
            value = substitutor.substitute(value);
            registryHandler.setValue(keypath, name, Long.parseLong(value));
            return;
        }
        value = regEntry.getAttribute(REG_STRING);
        if (value != null)
        { // Value type is string; placeholder possible.
            value = substitutor.substitute(value);
            registryHandler.setValue(keypath, name, value);
            return;
        }
        List<IXMLElement> values = regEntry.getChildrenNamed(REG_MULTI);
        if (values != null && !values.isEmpty())
        { // Value type is REG_MULTI_SZ; placeholder possible.
            Iterator<IXMLElement> multiIter = values.iterator();
            String[] multiString = new String[values.size()];
            for (int i = 0; multiIter.hasNext(); ++i)
            {
                IXMLElement element = multiIter.next();
                multiString[i] = specHelper.getRequiredAttribute(element, REG_DATA);
                multiString[i] = substitutor.substitute(multiString[i]);
            }
            registryHandler.setValue(keypath, name, multiString);
            return;
        }
        values = regEntry.getChildrenNamed(REG_BIN);
        if (values != null && !values.isEmpty())
        { // Value type is REG_BINARY; placeholder possible or not ??? why not
            // ...
            Iterator<IXMLElement> multiIter = values.iterator();

            StringBuffer buf = new StringBuffer();
            for (int i = 0; multiIter.hasNext(); ++i)
            {
                IXMLElement element = multiIter.next();
                String tmp = specHelper.getRequiredAttribute(element, REG_DATA);
                buf.append(tmp);
                if (!tmp.endsWith(",") && multiIter.hasNext())
                {
                    buf.append(",");
                }
            }
            byte[] bytes = extractBytes(regEntry, substitutor.substitute(buf.toString()));
            registryHandler.setValue(keypath, name, bytes);
            return;
        }
        specHelper.parseError(regEntry, "No data found.");

    }

    private byte[] extractBytes(IXMLElement element, String byteString) throws Exception
    {
        StringTokenizer st = new StringTokenizer(byteString, ",");
        byte[] retval = new byte[st.countTokens()];
        int i = 0;
        while (st.hasMoreTokens())
        {
            byte value = 0;
            String token = st.nextToken().trim();
            try
            { // Unfortenly byte is signed ...
                int tval = Integer.parseInt(token, 16);
                if (tval < 0 || tval > 0xff)
                {
                    throw new NumberFormatException("Value out of range.");
                }
                if (tval > 0x7f)
                {
                    tval -= 0x100;
                }
                value = (byte) tval;
            }
            catch (NumberFormatException nfe)
            {
                getSpecHelper()
                        .parseError(element,
                                "Bad entry for REG_BINARY; a byte should be written as 2 digit hexvalue followed by a ','.");
            }
            retval[i++] = value;
        }
        return (retval);

    }

    /**
     * Perform the setting of one key.
     *
     * @param regEntry        element which contains the description of the key to be created
     * @param substitutor     variable substitutor to be used for revising the regEntry contents
     * @param registryHandler the registry handler
     */
    private void performKeySetting(IXMLElement regEntry, VariableSubstitutor substitutor,
                                   RegistryHandler registryHandler)
            throws Exception
    {
        String keypath = getSpecHelper().getRequiredAttribute(regEntry, REG_KEYPATH);
        keypath = substitutor.substitute(keypath);
        String root = getSpecHelper().getRequiredAttribute(regEntry, REG_ROOT);
        int rootId = resolveRoot(regEntry, root, substitutor);
        registryHandler.setRoot(rootId);
        if (!registryHandler.keyExist(keypath))
        {
            registryHandler.createKey(keypath);
        }
    }

    private int resolveRoot(IXMLElement regEntry, String root, VariableSubstitutor substitutor)
            throws Exception
    {
        String root1 = substitutor.substitute(root);
        Integer tmp = RegistryHandler.ROOT_KEY_MAP.get(root1);
        if (tmp != null)
        {
            return (tmp);
        }
        getSpecHelper().parseError(regEntry, "Unknown value (" + root1 + ")for registry root.");
        return 0;
    }

    private void initializeRegistryHandler(AutomatedInstallData idata) throws Exception
    {
        RegistryHandler registryHandler = handler.getInstance();
        if (registryHandler != null)
        {
            registryHandler.verify(idata);
            getSpecHelper().readSpec(SPEC_FILE_NAME);
        }
    }

}
