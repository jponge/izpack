package com.izforge.izpack.api.rules;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.Properties;

/**
 * Interface of rulesEngine
 */
public interface RulesEngine extends Serializable
{
    String[] getKnownConditionIds();

    boolean isConditionTrue(String id, Properties variables);

    boolean isConditionTrue(Condition cond, Properties variables);

    boolean isConditionTrue(String id);

    boolean isConditionTrue(Condition cond);

    boolean canShowPanel(String panelid, Properties variables);

    boolean canInstallPack(String packid, Properties variables);

    boolean canInstallPackOptional(String packid, Properties variables);

    void addCondition(Condition condition);

    void writeRulesXML(OutputStream out);
}
