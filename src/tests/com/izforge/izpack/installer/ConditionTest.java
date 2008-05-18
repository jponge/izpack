/*
 * IzPack - Copyright 2001-2006 Julien Ponge, All Rights Reserved.
 * 
 * http://www.izforge.com/izpack/ http://izpack.codehaus.org/
 * 
 * Copyright 2007 Dennis Reil
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.izforge.izpack.installer;

import com.izforge.izpack.rules.RulesEngine;
import junit.framework.TestCase;
import net.n3.nanoxml.XMLElement;


/**
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 */
public class ConditionTest extends TestCase
{

    public final static String RDE_VCS_REVISION = "$Revision: $";

    public final static String RDE_VCS_NAME = "$Name:  $";

    protected static InstallData idata = new InstallData();

    protected RulesEngine rules;

    /**
     * @param arg0
     */
    public ConditionTest(String arg0)
    {
        super(arg0);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        XMLElement conditionspec = new XMLElement();
        conditionspec.setName("conditions");

        conditionspec.addChild(this.createVariableCondition("test.true", "TEST", "true"));
        conditionspec.addChild(this.createRefCondition("test.true2", "test.true"));
        //conditionspec.addChild(createNotCondition("test.not.true", createVariableCondition("test.true", "TEST", "true")));
        conditionspec.addChild(createNotCondition("test.not.true", createRefCondition("", "test.true")));
        rules = new RulesEngine(conditionspec, idata);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        if (idata != null)
        {
            idata.variables.clear();
        }
    }

    protected XMLElement createNotCondition(String id, XMLElement condition)
    {
        XMLElement not = new XMLElement();
        not.setName("condition");
        not.setAttribute("type", "not");
        not.setAttribute("id", id);
        not.addChild(condition);

        return not;
    }

    protected XMLElement createVariableCondition(String id, String variable, String expvalue)
    {
        XMLElement variablecondition = new XMLElement();
        variablecondition.setName("condition");
        variablecondition.setAttribute("type", "variable");
        variablecondition.setAttribute("id", id);

        XMLElement name = new XMLElement();
        name.setName("name");
        name.setContent(variable);

        XMLElement value = new XMLElement();
        value.setName("value");
        value.setContent(expvalue);

        variablecondition.addChild(name);
        variablecondition.addChild(value);

        return variablecondition;
    }

    protected XMLElement createRefCondition(String id, String refid)
    {
        XMLElement refcondition = new XMLElement();
        refcondition.setName("condition");
        refcondition.setAttribute("type", "ref");
        refcondition.setAttribute("refid", refid);
        refcondition.setAttribute("id", id);

        return refcondition;
    }

    public void testNotCondition()
    {
        assertNull(RulesEngine.getCondition("test.not"));
        assertNotNull(RulesEngine.getCondition("test.not.true"));
        assertTrue(rules.isConditionTrue("test.not.true", idata.variables));

        assertNotNull(RulesEngine.getCondition("!test.not.true"));

        assertFalse(rules.isConditionTrue("!test.not.true", idata.variables));
    }

    public void testVariableCondition()
    {

        assertNotNull(RulesEngine.getCondition("test.true"));
        assertNotNull(RulesEngine.getCondition("test.true2"));

        assertFalse(rules.isConditionTrue("test.true", idata.variables));
        assertFalse(rules.isConditionTrue("test.true2", idata.variables));

        idata.setVariable("TEST", "true");

        assertTrue(rules.isConditionTrue("test.true", idata.variables));
        assertTrue(rules.isConditionTrue("test.true2", idata.variables));

        assertFalse(rules.isConditionTrue("!test.true", idata.variables));
        assertFalse(rules.isConditionTrue("!test.true2", idata.variables));

        assertTrue(rules.isConditionTrue("test.true+test.true2", idata.variables));
        assertTrue(rules.isConditionTrue("test.true2+test.true", idata.variables));

        assertFalse(rules.isConditionTrue("!test.true2+test.true", idata.variables));

        assertTrue(rules.isConditionTrue("test.true2|test.true", idata.variables));

        assertFalse(rules.isConditionTrue("test.true2\\test.true", idata.variables));
    }
}