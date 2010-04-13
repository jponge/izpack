package com.izforge.izpack.installer.container.provider;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLParser;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.core.container.ConditionContainer;
import com.izforge.izpack.core.rules.RulesEngineImpl;
import com.izforge.izpack.merge.resolve.ClassPathCrawler;
import com.izforge.izpack.util.Debug;
import org.picocontainer.injectors.Provider;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Map;

/**
 * Injection provider for rules.
 */
public class RulesProvider implements Provider
{

    /**
     * Resource name of the conditions specification
     */
    private static final String CONDITIONS_SPECRESOURCENAME = "conditions.xml";
    private ResourceManager resourceManager;

    /**
     * Reads the conditions specification file and initializes the rules engine.
     */
    public RulesEngine provide(AutomatedInstallData installdata, ClassPathCrawler classPathCrawler, ConditionContainer conditionContainer, ResourceManager resourceManager)
    {
        // try to load already parsed conditions
        RulesEngine res = null;
        try
        {
            InputStream in = resourceManager.getInputStream("rules");
            ObjectInputStream objIn = new ObjectInputStream(in);
            Map rules = (Map) objIn.readObject();
            if ((rules != null) && (rules.size() != 0))
            {
                res = new RulesEngineImpl(installdata, classPathCrawler, conditionContainer);
                res.readConditionMap(rules);
            }
            objIn.close();
        }
        catch (Exception e)
        {
            Debug.trace("Can not find optional rules");
        }
        if (res != null)
        {
            installdata.setRules(res);
            // rules already read
            return res;
        }
        try
        {
            InputStream input = ClassLoader.getSystemResourceAsStream(CONDITIONS_SPECRESOURCENAME);
            if (input == null)
            {
                res = new RulesEngineImpl(installdata, classPathCrawler, conditionContainer);
                return res;
            }
            XMLParser xmlParser = new XMLParser();

            // get the data
            IXMLElement conditionsxml = xmlParser.parse(input);
            res = new RulesEngineImpl(installdata, classPathCrawler, conditionContainer);
            res.analyzeXml(conditionsxml);
        }
        catch (Exception e)
        {
            Debug.trace("Can not find optional resource " + CONDITIONS_SPECRESOURCENAME);
            // there seem to be no conditions
            res = new RulesEngineImpl(installdata, classPathCrawler, conditionContainer);
        }
        installdata.setRules(res);
        return res;
    }
}
