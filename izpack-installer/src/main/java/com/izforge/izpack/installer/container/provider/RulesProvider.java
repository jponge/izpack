package com.izforge.izpack.installer.container.provider;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Map;
import java.util.logging.Logger;

import org.picocontainer.injectors.Provider;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLParser;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.rules.Condition;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.core.rules.ConditionContainer;
import com.izforge.izpack.core.rules.RulesEngineImpl;

/**
 * Injection provider for rules.
 *
 * @author Anthonin Bonnefoy
 * @author Tim Anderson
 */
public class RulesProvider implements Provider
{
    private static final Logger logger = Logger.getLogger(RulesProvider.class.getName());

    /**
     * Resource name of the conditions specification
     */
    private static final String CONDITIONS_SPECRESOURCENAME = "conditions.xml";

    /**
     * Reads the conditions specification file and initializes the rules engine.
     *
     * @param installData        the installation data
     * @param variables          the variables
     * @param conditionContainer the condition container
     * @param resources          the resources
     * @return a new rules engine
     */
    public RulesEngine provide(AutomatedInstallData installData, DefaultVariables variables,
                               ConditionContainer conditionContainer, Resources resources)
    {
        RulesEngine result = new RulesEngineImpl(installData, conditionContainer);
        Map<String, Condition> conditions = readConditions(resources);
        if (conditions != null && !conditions.isEmpty())
        {
            result.readConditionMap(conditions);
        }
        else
        {
            IXMLElement xml = readConditions();
            if (xml != null)
            {
                result.analyzeXml(xml);
            }
        }
        installData.setRules(result);
        variables.setRules(result);
        return result;
    }

    /**
     * Reads conditions using the resources.
     * <p/>
     * This looks for a serialized resource named <em>"rules"</em>.
     *
     * @param resources the resources
     * @return the conditions, keyed on id, or <tt>null</tt> if the resource doesn't exist or cannot be read
     */
    @SuppressWarnings("unchecked")
    private Map<String, Condition> readConditions(Resources resources)
    {
        Map<String, Condition> rules = null;
        try
        {
            InputStream in = resources.getInputStream("rules");
            if (in != null)
            {
                ObjectInputStream objIn = new ObjectInputStream(in);
                rules = (Map) objIn.readObject();
                objIn.close();
            }
        }
        catch (Exception exception)
        {
            logger.fine("No optional rules found");
        }
        return rules;
    }

    /**
     * Reads conditions from the class path.
     * <p/>
     * This looks for an XML resource named <em>"conditions.xml"</em>.
     *
     * @return the conditions, or <tt>null</tt> if they cannot be read
     */
    private IXMLElement readConditions()
    {
        IXMLElement conditions = null;
        try
        {
            InputStream input = ClassLoader.getSystemResourceAsStream(CONDITIONS_SPECRESOURCENAME);
            if (input != null)
            {
                XMLParser xmlParser = new XMLParser();
                conditions = xmlParser.parse(input);
            }
        }
        catch (Exception e)
        {
            logger.fine("No optional resource found: " + CONDITIONS_SPECRESOURCENAME);
        }
        return conditions;
    }

}
