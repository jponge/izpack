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

package com.izforge.izpack.test;

import java.util.Properties;

import junit.framework.TestCase;

import org.w3c.dom.Document;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.data.GUIInstallData;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.core.container.ConditionContainer;
import com.izforge.izpack.core.rules.RulesEngineImpl;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import org.hamcrest.Matcher;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 */
@RunWith(PicoRunner.class)
@Container(TestConditionContainer.class)
public class ConditionTest
{

    private static final Matcher<Boolean> IS_TRUE = Is.is(true);
    private static final Matcher<Boolean> IS_FALSE = Is.is(false);
    private static final Matcher<Object> IS_NULL = IsNull.nullValue();
    private static final Matcher<Object> IS_NOT_NULL = IsNull.notNullValue();
    private RulesEngine rules;
    private GUIInstallData idata;
    private ClassPathCrawler classPathCrawler;
    private ConditionContainer conditionContainer;

    public ConditionTest(ClassPathCrawler classPathCrawler, GUIInstallData idata, ConditionContainer conditionContainer)
    {
        this.classPathCrawler = classPathCrawler;
        this.idata = idata;
        this.conditionContainer = conditionContainer;
    }

    @Before
    public void setUp() throws Exception
    {
        IXMLElement conditionspec = new XMLElementImpl("conditions");
        Document ownerDocument = conditionspec.getElement().getOwnerDocument();
        conditionspec.addChild(createVariableCondition("test.true", "TEST", "true", ownerDocument));
        conditionspec.addChild(createRefCondition("test.true2", "test.true", ownerDocument));
//        conditionspec.addChild(createNotCondition("test.not.true", createVariableCondition("test.true", "TEST", "true", ownerDocument), ownerDocument));
        conditionspec.addChild(createNotCondition("test.not.true", createRefCondition("", "test.true", ownerDocument), ownerDocument));
        rules = new RulesEngineImpl(idata, classPathCrawler, conditionContainer);
        conditionContainer.addComponent(RulesEngine.class, rules);
        rules.analyzeXml(conditionspec);
    }

    public IXMLElement createNotCondition(String id, IXMLElement condition, Document ownerDocument)
    {
        IXMLElement not = new XMLElementImpl("condition", ownerDocument);
        not.setAttribute("type", "not");
        not.setAttribute("id", id);
        not.addChild(condition);

        return not;
    }

    public IXMLElement createVariableCondition(String id, String variable, String expvalue, Document ownerDocument)
    {
        IXMLElement variablecondition = new XMLElementImpl("condition", ownerDocument);
        variablecondition.setAttribute("type", "variable");
        variablecondition.setAttribute("id", id);
        IXMLElement name = new XMLElementImpl("name", ownerDocument);
        name.setContent(variable);
        IXMLElement value = new XMLElementImpl("value", ownerDocument);
        value.setContent(expvalue);

        variablecondition.addChild(name);
        variablecondition.addChild(value);

        return variablecondition;
    }

    public IXMLElement createRefCondition(String id, String refid, Document ownerDocument)
    {
        IXMLElement refcondition = new XMLElementImpl("condition", ownerDocument);
        refcondition.setAttribute("type", "ref");
        refcondition.setAttribute("refid", refid);
        refcondition.setAttribute("id", id);

        return refcondition;
    }

    @Test
    public void testNotCondition()
    {
        assertThat(rules.getCondition("test.not"), IS_NULL);
        assertThat(rules.getCondition("test.not.true"), IS_NOT_NULL);
        assertThat(rules.isConditionTrue("test.not.true", idata.getVariables()), IS_TRUE);

        assertThat(rules.getCondition("!test.not.true"), IS_NOT_NULL);

        assertThat(rules.isConditionTrue("!test.not.true", idata.getVariables()), IS_FALSE);
    }

    @Test
    public void testVariableCondition()
    {
        assertThat(rules.getCondition("test.true"), IS_NOT_NULL);
        assertThat(rules.getCondition("test.true2"), IS_NOT_NULL);

        assertThat(rules.isConditionTrue("test.true", idata.getVariables()), IS_FALSE);
        assertThat(rules.isConditionTrue("test.true2", idata.getVariables()), IS_FALSE);

        idata.setVariable("TEST", "true");

        assertThat(rules.isConditionTrue("test.true", idata.getVariables()), IS_TRUE);
        assertThat(rules.isConditionTrue("test.true2", idata.getVariables()), IS_TRUE);

        assertThat(rules.isConditionTrue("!test.true", idata.getVariables()), IS_FALSE);
        assertThat(rules.isConditionTrue("!test.true2", idata.getVariables()), IS_FALSE);

        assertThat(rules.isConditionTrue("test.true+test.true2", idata.getVariables()), IS_TRUE);
        assertThat(rules.isConditionTrue("test.true2+test.true", idata.getVariables()), IS_TRUE);

        assertThat(rules.isConditionTrue("!test.true2+test.true", idata.getVariables()), IS_FALSE);

        assertThat(rules.isConditionTrue("test.true2|test.true", idata.getVariables()), IS_TRUE);

        assertThat(rules.isConditionTrue("test.true2\\test.true", idata.getVariables()), IS_FALSE);
    }
}