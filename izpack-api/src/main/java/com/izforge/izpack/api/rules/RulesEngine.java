package com.izforge.izpack.api.rules;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.AutomatedInstallData;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Interface of rulesEngine
 */
public interface RulesEngine extends Serializable
{
    Set<String> getKnownConditionIds();

    boolean isConditionTrue(String id, AutomatedInstallData installData);
    boolean isConditionTrue(Condition cond, AutomatedInstallData installData);
    boolean isConditionTrue(String id);
    boolean isConditionTrue(Condition cond);

    boolean canShowPanel(String panelid, Properties variables);

    boolean canInstallPack(String packid, Properties variables);

    boolean canInstallPackOptional(String packid, Properties variables);

    void addCondition(Condition condition);

    void writeRulesXML(OutputStream out);

    Condition getCondition(String id);

    void readConditionMap(Map<String, Condition> rules);

    void analyzeXml(IXMLElement conditionsspec);

    Condition instanciateCondition(IXMLElement condition);

    /**
     * Check whether references condition exist This must be done after all conditions have been
     * read, to not depend on order of their definition in the XML
     */
    void resolveConditions() throws Exception;

    IXMLElement createConditionElement(Condition condition, IXMLElement root);
}
