/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2007 Dennis Reil
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

package com.izforge.izpack.core.rules;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.XMLException;
import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.adaptator.impl.XMLWriter;
import com.izforge.izpack.api.container.BindeableContainer;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.rules.Condition;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.core.container.ConditionContainer;
import com.izforge.izpack.core.rules.logic.AndCondition;
import com.izforge.izpack.core.rules.logic.NotCondition;
import com.izforge.izpack.core.rules.logic.OrCondition;
import com.izforge.izpack.core.rules.logic.XorCondition;
import com.izforge.izpack.core.rules.process.JavaCondition;
import com.izforge.izpack.core.rules.process.PackselectionCondition;
import com.izforge.izpack.merge.resolve.ClassPathCrawler;
import com.izforge.izpack.util.Debug;

import java.io.OutputStream;
import java.util.*;


/**
 * The rules engine class is the central point for checking conditions
 *
 * @author Dennis Reil, <Dennis.Reil@reddot.de> created: 09.11.2006, 13:48:39
 */
public class RulesEngineImpl implements RulesEngine
{

    private static final long serialVersionUID = 3966346766966632860L;

    protected Map<String, String> panelconditions;

    protected Map<String, String> packconditions;

    protected Map<String, String> optionalpackconditions;

    protected Map<String, Condition> conditionsmap = new HashMap<String, Condition>();

    protected AutomatedInstallData installdata;
    private ClassPathCrawler classPathCrawler;
    private BindeableContainer container;

    public RulesEngineImpl(AutomatedInstallData installdata, ClassPathCrawler classPathCrawler, ConditionContainer container)
    {
        this.installdata = installdata;
        this.classPathCrawler = classPathCrawler;
        this.container = container;
        conditionsmap = new HashMap<String, Condition>();
        this.panelconditions = new HashMap<String, String>();
        this.packconditions = new HashMap<String, String>();
        this.optionalpackconditions = new HashMap<String, String>();
        initStandardConditions();
    }

    public RulesEngineImpl(ClassPathCrawler classPathCrawler, ConditionContainer container)
    {
        this(null, classPathCrawler, container);
    }

    /**
     * initializes builtin conditions like os conditions and package conditions
     */
    private void initStandardConditions()
    {
        Debug.trace("RulesEngine.initStandardConditions()");
        initOsConditions();
        if ((installdata != null) && (installdata.getAllPacks() != null))
        {
            Debug.trace("Initializing builtin conditions for packs.");
            for (Pack pack : installdata.getAllPacks())
            {
                if (pack.id != null)
                {
                    // automatically add packselection condition
                    PackselectionCondition packselcond = new PackselectionCondition();
                    packselcond.setInstalldata(installdata);
                    packselcond.setId("izpack.selected." + pack.id);
                    packselcond.setPackid(pack.id);
                    conditionsmap.put(packselcond.getId(), packselcond);

                    Debug.trace("Pack.getCondition(): " + pack.getCondition() + " for pack "
                            + pack.id);
                    if ((pack.getCondition() != null) && pack.getCondition().length() > 0)
                    {
                        Debug.trace("Adding pack condition " + pack.getCondition() + " for pack "
                                + pack.id);
                        packconditions.put(pack.id, pack.getCondition());
                    }
                }
            }
        }
    }

    private void initOsConditions()
    {
        createBuiltinOsCondition("IS_AIX", "izpack.aixinstall");
        createBuiltinOsCondition("IS_WINDOWS", "izpack.windowsinstall");
        createBuiltinOsCondition("IS_WINDOWS_XP", "izpack.windowsinstall.xp");
        createBuiltinOsCondition("IS_WINDOWS_2003", "izpack.windowsinstall.2003");
        createBuiltinOsCondition("IS_WINDOWS_VISTA", "izpack.windowsinstall.vista");
        createBuiltinOsCondition("IS_WINDOWS_7", "izpack.windowsinstall.7");
        createBuiltinOsCondition("IS_LINUX", "izpack.linuxinstall");
        createBuiltinOsCondition("IS_SUNOS", "izpack.solarisinstall");
        createBuiltinOsCondition("IS_MAC", "izpack.macinstall");
        createBuiltinOsCondition("IS_SUNOS", "izpack.solarisinstall");
        createBuiltinOsCondition("IS_SUNOS_X86", "izpack.solarisinstall.x86");
        createBuiltinOsCondition("IS_SUNOS_SPARC", "izpack.solarisinstall.sparc");
    }

    private void createBuiltinOsCondition(String osVersionField, String conditionId)
    {
        JavaCondition condition = new JavaCondition("com.izforge.izpack.util.OsVersion", osVersionField, true, "true", "boolean");
        condition.setInstalldata(installdata);
        condition.setId(conditionId);
        conditionsmap.put(condition.getId(), condition);
    }

    public void readConditionMap(Map<String, Condition> rules)
    {
        conditionsmap = rules;
        for (String key : conditionsmap.keySet())
        {
            Condition condition = conditionsmap.get(key);
            condition.setInstalldata(installdata);
        }
    }

    /**
     * Returns the current known condition ids.
     *
     * @return
     */
    public String[] getKnownConditionIds()
    {
        String[] conditionids = conditionsmap.keySet().toArray(
                new String[conditionsmap.size()]);
        Arrays.sort(conditionids);
        return conditionids;
    }

    public Condition instanciateCondition(IXMLElement condition)
    {
        String condid = condition.getAttribute("id");
        String condtype = condition.getAttribute("type");
        Condition result = null;
        if (condtype != null)
        {
            String conditionclassname = "";
            if (condtype.indexOf('.') > -1)
            {
                conditionclassname = condtype;
            }
            else
            {
                String conditiontype = condtype.toLowerCase();
                conditionclassname = conditiontype.substring(0, 1).toUpperCase()
                        + conditiontype.substring(1, conditiontype.length());
                conditionclassname += "Condition";
            }
            try
            {
                Class<Condition> conditionclass = classPathCrawler.searchClassInClassPath(conditionclassname);
                container.addComponent(condid, conditionclass);
                result = (Condition) container.getComponent(condid);
                result.readFromXML(condition);
                if (condid != null)
                {
                    result.setId(condid);
                }
                result.setInstalldata(installdata);
            }
            catch (Exception e)
            {
                throw new IzPackException(e);
            }
        }
        return result;
    }

    /**
     * Read the spec for the conditions
     *
     * @param conditionsspec
     */
    public void analyzeXml(IXMLElement conditionsspec)
    {
        if (conditionsspec == null)
        {
            Debug.trace("No specification for conditions found.");
            return;
        }
        if (conditionsspec.hasChildren())
        {
            // read in the condition specs
            List<IXMLElement> childs = conditionsspec.getChildrenNamed("condition");

            for (IXMLElement condition : childs)
            {
                Condition cond = instanciateCondition(condition);
                if (cond != null)
                {
                    // this.conditionslist.add(cond);
                    String condid = cond.getId();
                    cond.setInstalldata(installdata);
                    if ((condid != null) && !("UNKNOWN".equals(condid)))
                    {
                        conditionsmap.put(condid, cond);
                    }
                }
            }

            List<IXMLElement> panelconditionels = conditionsspec
                    .getChildrenNamed("panelcondition");
            for (IXMLElement panelel : panelconditionels)
            {
                String panelid = panelel.getAttribute("panelid");
                String conditionid = panelel.getAttribute("conditionid");
                this.panelconditions.put(panelid, conditionid);
            }

            List<IXMLElement> packconditionels = conditionsspec
                    .getChildrenNamed("packcondition");
            for (IXMLElement panelel : packconditionels)
            {
                String panelid = panelel.getAttribute("packid");
                String conditionid = panelel.getAttribute("conditionid");
                this.packconditions.put(panelid, conditionid);
                // optional install allowed, if condition is not met?
                String optional = panelel.getAttribute("optional");
                if (optional != null)
                {
                    boolean optionalinstall = Boolean.valueOf(optional);
                    if (optionalinstall)
                    {
                        // optional installation is allowed
                        this.optionalpackconditions.put(panelid, conditionid);
                    }
                }
            }
        }
    }


    public Condition getCondition(String id)
    {
        Condition result = conditionsmap.get(id);
        if (result == null)
        {
            result = getConditionByExpr(new StringBuffer(id));
        }
        return result;
    }

    protected Condition getConditionByExpr(StringBuffer conditionexpr)
    {
        Condition result = null;
        int index = 0;
        while (index < conditionexpr.length())
        {
            char currentchar = conditionexpr.charAt(index);
            switch (currentchar)
            {
                case '+':
                    // and-condition
                    Condition op1 = conditionsmap.get(conditionexpr.substring(0, index));
                    conditionexpr.delete(0, index + 1);
                    result = new AndCondition(op1, getConditionByExpr(conditionexpr), this);
                    break;
                case '|':
                    // or-condition
                    op1 = conditionsmap.get(conditionexpr.substring(0, index));
                    conditionexpr.delete(0, index + 1);
                    result = new OrCondition(op1, getConditionByExpr(conditionexpr));

                    break;
                case '\\':
                    // xor-condition
                    op1 = conditionsmap.get(conditionexpr.substring(0, index));
                    conditionexpr.delete(0, index + 1);
                    result = new XorCondition(op1, getConditionByExpr(conditionexpr));
                    break;
                case '!':
                    // not-condition
                    if (index > 0)
                    {
                        Debug.trace("error: ! operator only allowed at position 0");
                    }
                    else
                    {
                        // delete not symbol
                        conditionexpr.deleteCharAt(index);
                        result = NotCondition.createFromCondition(getConditionByExpr(conditionexpr), this, installdata);
                    }
                    break;
                default:
                    // do nothing
            }
            index++;
        }
        if (conditionexpr.length() > 0)
        {
            result = conditionsmap.get(conditionexpr.toString());
            if (result != null)
            {
                result.setInstalldata(installdata);
                conditionexpr.delete(0, conditionexpr.length());
            }
        }
        return result;
    }

    public boolean isConditionTrue(String id, Properties variables)
    {
        Condition cond = getCondition(id);
        cond.setInstalldata(installdata);
        if (cond == null)
        {
            Debug.trace("Condition (" + id + ") not found.");
            return true;
        }
        else
        {
            Debug.trace("Checking condition");
            try
            {
                return cond.isTrue();
            }
            catch (NullPointerException npe)
            {
                Debug.error("Nullpointerexception checking condition: " + id);
                return false;
            }
        }
    }

    public boolean isConditionTrue(Condition cond, Properties variables)
    {
        if (cond == null)
        {
            Debug.trace("Condition not found.");
            return true;
        }
        else
        {
            Debug.trace("Checking condition");
            return cond.isTrue();
        }
    }

    public boolean isConditionTrue(String id)
    {
        Condition cond = getCondition(id);
        if (cond != null)
        {
            cond.setInstalldata(installdata);
            return this.isConditionTrue(cond);
        }
        else
        {
            return false;
        }
    }

    public boolean isConditionTrue(Condition cond)
    {
        if (cond.getInstalldata() == null)
        {
            cond.setInstalldata(installdata);
        }
        return cond.isTrue();
    }

    /**
     * Can a panel be shown?
     *
     * @param panelid   - id of the panel, which should be shown
     * @param variables - the variables
     * @return true - there is no condition or condition is met false - there is a condition and the
     *         condition was not met
     */
    public boolean canShowPanel(String panelid, Properties variables)
    {
        Debug.trace("can show panel with id " + panelid + " ?");
        if (!this.panelconditions.containsKey(panelid))
        {
            Debug.trace("no condition, show panel");
            return true;
        }
        Debug.trace("there is a condition");
        Condition condition = getCondition(this.panelconditions.get(panelid));
        if (condition != null)
        {
            return condition.isTrue();
        }
        return false;
    }

    /**
     * Is the installation of a pack possible?
     *
     * @param packid
     * @param variables
     * @return true - there is no condition or condition is met false - there is a condition and the
     *         condition was not met
     */
    public boolean canInstallPack(String packid, Properties variables)
    {
        if (packid == null)
        {
            return true;
        }
        Debug.trace("can install pack with id " + packid + "?");
        if (!this.packconditions.containsKey(packid))
        {
            Debug.trace("no condition, can install pack");
            return true;
        }
        Debug.trace("there is a condition");
        Condition condition = getCondition(this.packconditions.get(packid));
        if (condition != null)
        {
            return condition.isTrue();
        }
        return false;
    }

    /**
     * Is an optional installation of a pack possible if the condition is not met?
     *
     * @param packid
     * @param variables
     * @return
     */
    public boolean canInstallPackOptional(String packid, Properties variables)
    {
        Debug.trace("can install pack optional with id " + packid + "?");
        if (!this.optionalpackconditions.containsKey(packid))
        {
            Debug.trace("not in optionalpackconditions.");
            return false;
        }
        else
        {
            Debug.trace("optional install possible");
            return true;
        }
    }

    /**
     * @param condition
     */
    public void addCondition(Condition condition)
    {
        if (condition != null)
        {
            if (conditionsmap.containsKey(condition.getId()))
            {
                Debug.error("Condition already registered.");
            }
            else
            {
                conditionsmap.put(condition.getId(), condition);
            }
        }
        else
        {
            Debug.error("Cannot add condition. Condition was null.");
        }
    }

    public void writeRulesXML(OutputStream out)
    {
        XMLWriter xmlOut = new XMLWriter();
        xmlOut.setOutput(out);
        XMLElementImpl conditionsel = new XMLElementImpl("conditions");
        for (Condition condition : conditionsmap.values())
        {
            IXMLElement conditionEl = createConditionElement(condition, conditionsel);
            condition.makeXMLData(conditionEl);
            conditionsel.addChild(conditionEl);
        }
        Debug.trace("Writing generated conditions specification.");
        try
        {
            xmlOut.write(conditionsel);
        }
        catch (XMLException e)
        {
            throw new IzPackException(e);
        }
    }

    public IXMLElement createConditionElement(Condition condition, IXMLElement root)
    {
        XMLElementImpl xml = new XMLElementImpl("condition", root);
        xml.setAttribute("id", condition.getId());
        xml.setAttribute("type", condition.getClass().getCanonicalName());
        return xml;
    }
}
