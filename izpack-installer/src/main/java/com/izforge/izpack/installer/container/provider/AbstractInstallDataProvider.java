package com.izforge.izpack.installer.container.provider;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.DynamicInstallerRequirementValidator;
import com.izforge.izpack.api.data.DynamicVariable;
import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.api.data.Info.TempDir;
import com.izforge.izpack.api.data.InstallerRequirement;
import com.izforge.izpack.api.data.LocaleDatabase;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.api.data.ScriptParserConstant;
import com.izforge.izpack.api.data.Value;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.IoHelper;
import com.izforge.izpack.util.OsConstraintHelper;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.TemporaryDirectory;
import org.picocontainer.injectors.Provider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract class sharing commons instanciation methods beetween installData
 */
public abstract class AbstractInstallDataProvider implements Provider
{
    private static final Logger logger = Logger.getLogger(AbstractInstallDataProvider.class.getName());

    /**
     * The base name of the XML file that specifies the custom langpack. Searched is for the file
     * with the name expanded by _ISO3.
     */
    protected static final String LANG_FILE_NAME = "CustomLangPack.xml";
    protected ResourceManager resourceManager;
    protected VariableSubstitutor variableSubstitutor;
    protected Housekeeper housekeeper;

    /**
     * Loads the installation data. Also sets environment variables to <code>installdata</code>.
     * All system properties are available as $SYSTEM_<variable> where <variable> is the actual
     * name _BUT_ with all separators replaced by '_'. Properties with null values are never stored.
     * Example: $SYSTEM_java_version or $SYSTEM_os_name
     *
     * @param installdata Where to store the installation data.
     * @throws Exception Description of the Exception
     */
    protected void loadInstallData(AutomatedInstallData installdata) throws IOException, ClassNotFoundException, InstallerException
    {
        // We load the variables
        Properties variables = (Properties) readObject("vars");

        // We load the Info data
        Info info = (Info) readObject("info");

        // We put the Info data as variables
        installdata.setVariable(ScriptParserConstant.APP_NAME, info.getAppName());
        if (info.getAppURL() != null)
        {
            installdata.setVariable(ScriptParserConstant.APP_URL, info.getAppURL());
        }
        installdata.setVariable(ScriptParserConstant.APP_VER, info.getAppVersion());
        if (info.getUninstallerCondition() != null)
        {
            installdata.setVariable("UNINSTALLER_CONDITION", info.getUninstallerCondition());
        }

        installdata.setInfo(info);
        // Set the installation path in a default manner
        String dir = getDir();
        String installPath = dir + info.getAppName();
        if (info.getInstallationSubPath() != null)
        { // A subpath was defined, use it.
            installPath = IoHelper.translatePath(dir + info.getInstallationSubPath(),
                    variableSubstitutor);
        }

        installdata.setDefaultInstallPath(installPath);
        // Pre-set install path from a system property,
        // for instance in unattended installations
        installPath = System.getProperty(AutomatedInstallData.INSTALL_PATH);
        if (installPath != null)
        {
            installdata.setInstallPath(installPath);
        }


        // We read the panels order data
        List<Panel> panelsOrder = (List<Panel>) readObject("panelsOrder");

        // We read the packs data
        InputStream in = resourceManager.getInputStream("packs.info");
        ObjectInputStream objIn = new ObjectInputStream(in);
        int size = objIn.readInt();
        List<Pack> availablePacks = new ArrayList<Pack>();
        List<Pack> allPacks = new ArrayList<Pack>();

        for (int i = 0; i < size; i++)
        {
            Pack pack = (Pack) objIn.readObject();
            allPacks.add(pack);
            if (OsConstraintHelper.oneMatchesCurrentSystem(pack.osConstraints))
            {
                availablePacks.add(pack);
            }
        }
        objIn.close();

        // We determine the hostname and IPAdress
        String hostname;
        String IPAddress;

        try
        {
            InetAddress addr = InetAddress.getLocalHost();

            // Get IP Address
            IPAddress = addr.getHostAddress();

            // Get hostname
            hostname = addr.getHostName();
        }
        catch (Exception e)
        {
            hostname = "";
            IPAddress = "";
        }

        installdata.setVariable("APPLICATIONS_DEFAULT_ROOT", dir);
        dir += File.separator;
        installdata.setVariable(ScriptParserConstant.JAVA_HOME, System.getProperty("java.home"));
        installdata.setVariable(ScriptParserConstant.CLASS_PATH, System.getProperty("java.class.path"));
        installdata.setVariable(ScriptParserConstant.USER_HOME, System.getProperty("user.home"));
        installdata.setVariable(ScriptParserConstant.USER_NAME, System.getProperty("user.name"));
        installdata.setVariable(ScriptParserConstant.IP_ADDRESS, IPAddress);
        installdata.setVariable(ScriptParserConstant.HOST_NAME, hostname);
        installdata.setVariable(ScriptParserConstant.FILE_SEPARATOR, File.separator);

        Set<String> systemProperties = System.getProperties().stringPropertyNames();
        for (String varName : systemProperties)
        {
            String varValue = System.getProperty(varName);
            if (varValue != null)
            {
                varName = varName.replace('.', '_');
                installdata.setVariable("SYSTEM_" + varName, varValue);
            }
        }

        if (null != variables)
        {
            Set<String> vars = variables.stringPropertyNames();
            for (String varName : vars)
            {
                installdata.setVariable(varName, variables.getProperty(varName));
            }
        }

        installdata.setPanelsOrder(panelsOrder);
        installdata.setAvailablePacks(availablePacks);
        installdata.setAllPacks(allPacks);

        // get list of preselected packs
        for (Pack availablePack : availablePacks)
        {
            if (availablePack.preselected)
            {
                installdata.getSelectedPacks().add(availablePack);
            }
        }

        // Create any temp directories
        Set<TempDir> tempDirs = info.getTempDirs();
        if (null != tempDirs && tempDirs.size() > 0)
        {
            for (TempDir tempDir : tempDirs)
            {
                TemporaryDirectory directory = new TemporaryDirectory(tempDir, installdata, housekeeper);
                directory.create();
                directory.cleanUp();
            }
        }

    }

    /**
     * Add the contents of a custom langpack (if exist) to the previos loaded comman langpack. If
     * not exist, trace an info and do nothing more.
     *
     * @param idata install data to be used
     */
    protected void addCustomLangpack(AutomatedInstallData idata)
    {
        // We try to load and add a custom langpack.
        try
        {
            idata.getLangpack().add(resourceManager.getInputStream(LANG_FILE_NAME));
        }
        catch (Throwable exception)
        {
            logger.warning("No custom langpack for " + idata.getLocaleISO3() + " available");
            return;
        }
        logger.fine("Found custom langpack for " + idata.getLocaleISO3());
    }


    private String getDir()
    {
        // We determine the operating system and the initial installation path
        String dir;
        if (OsVersion.IS_WINDOWS)
        {
            dir = buildWindowsDefaultPath();
        }
        else if (OsVersion.IS_OSX)
        {
            dir = "/Applications/";
        }
        else
        {
            if (new File("/usr/local/").canWrite())
            {
                dir = "/usr/local/";
            }
            else
            {
                dir = System.getProperty("user.home") + File.separatorChar;
            }
        }
        return dir;
    }

    /**
     * Get the default path for Windows (i.e Program Files/...).
     * Windows has a Setting for this in the environment and in the registry.
     * Just try to use the setting in the environment. If it fails for whatever reason, we take the former solution (buildWindowsDefaultPathFromProps).
     *
     * @return The Windows default installation path for applications.
     */
    private String buildWindowsDefaultPath()
    {
        try
        {
            //get value from environment...
            String prgFilesPath = IoHelper.getenv("ProgramFiles");
            if (prgFilesPath != null && prgFilesPath.length() > 0)
            {
                return prgFilesPath + File.separatorChar;
            }
            else
            {
                return buildWindowsDefaultPathFromProps();
            }
        }
        catch (Exception x)
        {
            x.printStackTrace();
            return buildWindowsDefaultPathFromProps();
        }
    }

    /**
     * just plain wrong in case the programfiles are not stored where the developer expects them.
     * E.g. in custom installations of large companies or if used internationalized version of windows with a language pack.
     *
     * @return the program files path
     */
    private String buildWindowsDefaultPathFromProps()
    {
        StringBuffer dpath = new StringBuffer("");
        try
        {
            // We load the properties
            Properties props = new Properties();
            props.load(
                    resourceManager.getInputStream("/com/izforge/izpack/installer/win32-defaultpaths.properties"));

            // We look for the drive mapping
            String drive = System.getProperty("user.home");
            if (drive.length() > 3)
            {
                drive = drive.substring(0, 3);
            }

            // Now we have it :-)
            dpath.append(drive);

            // Ensure that we have a trailing backslash (in case drive was
            // something
            // like "C:")
            if (drive.length() == 2)
            {
                dpath.append("\\");
            }

            String language = Locale.getDefault().getLanguage();
            String country = Locale.getDefault().getCountry();
            String language_country = language + "_" + country;

            // Try the most specific combination first
            if (null != props.getProperty(language_country))
            {
                dpath.append(props.getProperty(language_country));
            }
            else if (null != props.getProperty(language))
            {
                dpath.append(props.getProperty(language));
            }
            else
            {
                dpath.append(props.getProperty(Locale.ENGLISH.getLanguage()));
            }
        }
        catch (Exception err)
        {
            dpath = new StringBuffer("C:\\Program Files");
        }

        return dpath.toString();
    }

    /**
     * Loads Dynamic Variables.
     *
     * @param automatedInstallData
     */
    protected void loadDynamicVariables(AutomatedInstallData automatedInstallData)
    {
        try
        {
            InputStream in = resourceManager.getInputStream("dynvariables");
            ObjectInputStream objIn = new ObjectInputStream(in);
            Map<String, List<DynamicVariable>> dynamicvariables = (Map<String, List<DynamicVariable>>) objIn.readObject();
            objIn.close();
            // Initialize to prepare variable substition on several attributes
            for (List<DynamicVariable> dynVarList : dynamicvariables.values())
            {
                for (DynamicVariable dynVar : dynVarList)
                {
                    Value value = dynVar.getValue();
                    value.setInstallData(automatedInstallData);
                }
            }
            automatedInstallData.setDynamicvariables(dynamicvariables);
        }
        catch (Exception e)
        {
            logger.log(Level.WARNING,
                    "Cannot find optional dynamic variables", e);
        }
    }

    /**
     * Loads dynamic conditions.
     *
     * @param automatedInstallData
     */
    protected void loadDynamicConditions(AutomatedInstallData automatedInstallData)
    {
        try
        {
            InputStream in = resourceManager.getInputStream("dynconditions");
            ObjectInputStream objIn = new ObjectInputStream(in);
            automatedInstallData.setDynamicinstallerrequirements((List<DynamicInstallerRequirementValidator>) objIn.readObject());
            objIn.close();
        }
        catch (Exception e)
        {
            logger.log(Level.WARNING,
                    "Cannot find optional dynamic conditions", e);
        }
    }

    /**
     * Load installer conditions
     *
     * @param automatedInstallData
     * @throws Exception
     */
    public void loadInstallerRequirements(AutomatedInstallData automatedInstallData) throws Exception
    {
        InputStream in = resourceManager.getInputStream("installerrequirements");
        ObjectInputStream objIn = new ObjectInputStream(in);
        automatedInstallData.setInstallerrequirements((List<InstallerRequirement>) objIn.readObject());
        objIn.close();
    }

    /**
     * Load a default locale in the installData
     *
     * @param automatedInstallData The installData to fill
     * @throws Exception
     */
    protected void loadDefaultLocale(AutomatedInstallData automatedInstallData) throws Exception
    {
        // Loads the suitable langpack
        List<String> availableLangPacks = resourceManager.getAvailableLangPacks();
        String selectedPack = availableLangPacks.get(0);
        InputStream in = resourceManager.getInputStream("langpacks/" + selectedPack + ".xml");
        automatedInstallData.setAndProcessLocal(selectedPack, new LocaleDatabase(in));
        resourceManager.setLocale(selectedPack);
    }

    public Object readObject(String resourceId) throws IOException, ClassNotFoundException
    {
        InputStream inputStream = resourceManager.getInputStream(resourceId);
        ObjectInputStream objIn = new ObjectInputStream(inputStream);
        Object model = objIn.readObject();
        objIn.close();
        return model;
    }
}
