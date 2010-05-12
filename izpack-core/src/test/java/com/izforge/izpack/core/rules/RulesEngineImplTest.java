package com.izforge.izpack.core.rules;


import com.izforge.izpack.api.rules.Condition;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.core.rules.logic.NotCondition;
import com.izforge.izpack.core.rules.process.JavaCondition;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;


public class RulesEngineImplTest
{
    private RulesEngine engine = null;

    @Before
    public void setUp() throws Exception
    {
        engine = new RulesEngineImpl(null, null);

        Map<String, Condition> conditionsmap = new HashMap<String, Condition>();
        Condition alwaysFalse = new JavaCondition();
        conditionsmap.put("false", alwaysFalse);

        Condition alwaysTrue = NotCondition.createFromCondition(alwaysFalse, engine, null);
        conditionsmap.put("true", alwaysTrue);

        engine.readConditionMap(conditionsmap);
    }

    @Test
    public void testSimpleNot() throws Exception
    {
        Condition condition = null;

        condition = engine.getCondition("@!false");
        assertEquals(!false, condition.isTrue());

        condition = engine.getCondition("@!true");
        assertEquals(!true, condition.isTrue());
    }

    @Test
    public void testSimpleAnd() throws Exception
    {
        Condition condition = null;

        condition = engine.getCondition("@false && false");
        assertEquals(false && false, condition.isTrue());

        condition = engine.getCondition("@false && true");
        assertEquals(false && true, condition.isTrue());

        condition = engine.getCondition("@true && false");
        assertEquals(true && false, condition.isTrue());

        condition = engine.getCondition("@true && true");
        assertEquals(true && true, condition.isTrue());
    }

    @Test
    public void testSimpleOr() throws Exception
    {
        Condition condition = null;

        condition = engine.getCondition("@false || false");
        assertEquals(false || false, condition.isTrue());

        condition = engine.getCondition("@false || true");
        assertEquals(false || true, condition.isTrue());

        condition = engine.getCondition("@true || false");
        assertEquals(true || false, condition.isTrue());

        condition = engine.getCondition("@true || true");
        assertEquals(true || true, condition.isTrue());
    }

    @Test
    public void testSimpleXor() throws Exception
    {
        Condition condition = null;

        condition = engine.getCondition("@false ^ false");
        assertEquals(false ^ false, condition.isTrue());

        condition = engine.getCondition("@false ^ true");
        assertEquals(false ^ true, condition.isTrue());

        condition = engine.getCondition("@true ^ false");
        assertEquals(true ^ false, condition.isTrue());

        condition = engine.getCondition("@true ^ true");
        assertEquals(true ^ true, condition.isTrue());
    }


    @Test

    public void testComplexNot() throws Exception
    {
        Condition condition = null;

        condition = engine.getCondition("@!false || false");
        assertEquals(!false || false, condition.isTrue());

        condition = engine.getCondition("@!true || false");
        assertEquals(!true || false, condition.isTrue());

        condition = engine.getCondition("@false || !false");
        assertEquals(false || !false, condition.isTrue());

        condition = engine.getCondition("@true || !false");
        assertEquals(true || !false, condition.isTrue());

        condition = engine.getCondition("@!false && true");
        assertEquals(!false && true, condition.isTrue());

        condition = engine.getCondition("@true && !false");
        assertEquals(true && !false, condition.isTrue());

    }

    @Test
    public void testComplexAnd() throws Exception
    {
        Condition condition = null;

        condition = engine.getCondition("@false || false && false || false");
        assertEquals(false || false && false || false, condition.isTrue());

        condition = engine.getCondition("@false || false && false || true");
        assertEquals(false || false && false || true, condition.isTrue());

        condition = engine.getCondition("@false || false && true || false");
        assertEquals(false || false && true || false, condition.isTrue());

        condition = engine.getCondition("@false || false && true || true");
        assertEquals(false || false && true || true, condition.isTrue());

        condition = engine.getCondition("@false || true && false || false");
        assertEquals(false || true && false || false, condition.isTrue());

        condition = engine.getCondition("@false || true && false || false");
        assertEquals(false || true && false || false, condition.isTrue());

        condition = engine.getCondition("@false || true && false || true");
        assertEquals(false || true && false || true, condition.isTrue());

        condition = engine.getCondition("@false || true && true || false");
        assertEquals(false || true && true || false, condition.isTrue());

        condition = engine.getCondition("@false || true && true || true");
        assertEquals(false || true && true || true, condition.isTrue());

        condition = engine.getCondition("@true || false && false || false");
        assertEquals(true || false && false || false, condition.isTrue());

        condition = engine.getCondition("@true || false && false || true");
        assertEquals(true || false && false || true, condition.isTrue());

        condition = engine.getCondition("@true || false && true || false");
        assertEquals(true || false && true || false, condition.isTrue());

        condition = engine.getCondition("@true || false && true || true");
        assertEquals(true || false && true || true, condition.isTrue());

        condition = engine.getCondition("@true || true && false || false");
        assertEquals(true || true && false || false, condition.isTrue());

        condition = engine.getCondition("@true || true && false || false");
        assertEquals(true || true && false || false, condition.isTrue());

        condition = engine.getCondition("@true || true && false || true");
        assertEquals(true || true && false || true, condition.isTrue());

        condition = engine.getCondition("@true || true && true || false");
        assertEquals(true || true && true || false, condition.isTrue());

        condition = engine.getCondition("@true || true && true || true");
        assertEquals(true || true && true || true, condition.isTrue());

    }

    @Test
    public void testComplexOr() throws Exception
    {
        Condition condition = null;

        condition = engine.getCondition("@false && false || false && false");
        assertEquals(false && false || false && false, condition.isTrue());

        condition = engine.getCondition("@false && false || false && true");
        assertEquals(false && false || false && true, condition.isTrue());

        condition = engine.getCondition("@false && false || true && false");
        assertEquals(false && false || true && false, condition.isTrue());

        condition = engine.getCondition("@false && false || true && true");
        assertEquals(false && false || true && true, condition.isTrue());

        condition = engine.getCondition("@false && true || false && false");
        assertEquals(false && true || false && false, condition.isTrue());

        condition = engine.getCondition("@false && true || false && false");
        assertEquals(false && true || false && false, condition.isTrue());

        condition = engine.getCondition("@false && true || false && true");
        assertEquals(false && true || false && true, condition.isTrue());

        condition = engine.getCondition("@false && true || true && false");
        assertEquals(false && true || true && false, condition.isTrue());

        condition = engine.getCondition("@false && true || true && true");
        assertEquals(false && true || true && true, condition.isTrue());

        condition = engine.getCondition("@true && false || false && false");
        assertEquals(true && false || false && false, condition.isTrue());

        condition = engine.getCondition("@true && false || false && true");
        assertEquals(true && false || false && true, condition.isTrue());

        condition = engine.getCondition("@true && false || true && false");
        assertEquals(true && false || true && false, condition.isTrue());

        condition = engine.getCondition("@true && false || true && true");
        assertEquals(true && false || true && true, condition.isTrue());

        condition = engine.getCondition("@true && true || false && false");
        assertEquals(true && true || false && false, condition.isTrue());

        condition = engine.getCondition("@true && true || false && false");
        assertEquals(true && true || false && false, condition.isTrue());

        condition = engine.getCondition("@true && true || false && true");
        assertEquals(true && true || false && true, condition.isTrue());

        condition = engine.getCondition("@true && true || true && false");
        assertEquals(true && true || true && false, condition.isTrue());

        condition = engine.getCondition("@true && true || true && true");
        assertEquals(true && true || true && true, condition.isTrue());
    }

    @Test
    public void testComplexXor() throws Exception
    {
        Condition condition = null;

        condition = engine.getCondition("@false && false ^ false && false");
        assertEquals(false && false ^ false && false, condition.isTrue());

        condition = engine.getCondition("@false && false ^ false && true");
        assertEquals(false && false ^ false && true, condition.isTrue());

        condition = engine.getCondition("@false && false ^ true && false");
        assertEquals(false && false ^ true && false, condition.isTrue());

        condition = engine.getCondition("@false && false ^ true && true");
        assertEquals(false && false ^ true && true, condition.isTrue());

        condition = engine.getCondition("@false && true ^ false && false");
        assertEquals(false && true ^ false && false, condition.isTrue());

        condition = engine.getCondition("@false && true ^ false && false");
        assertEquals(false && true ^ false && false, condition.isTrue());

        condition = engine.getCondition("@false && true ^ false && true");
        assertEquals(false && true ^ false && true, condition.isTrue());

        condition = engine.getCondition("@false && true ^ true && false");
        assertEquals(false && true ^ true && false, condition.isTrue());

        condition = engine.getCondition("@false && true ^ true && true");
        assertEquals(false && true ^ true && true, condition.isTrue());

        condition = engine.getCondition("@true && false ^ false && false");
        assertEquals(true && false ^ false && false, condition.isTrue());

        condition = engine.getCondition("@true && false ^ false && true");
        assertEquals(true && false ^ false && true, condition.isTrue());

        condition = engine.getCondition("@true && false ^ true && false");
        assertEquals(true && false ^ true && false, condition.isTrue());

        condition = engine.getCondition("@true && false ^ true && true");
        assertEquals(true && false ^ true && true, condition.isTrue());

        condition = engine.getCondition("@true && true ^ false && false");
        assertEquals(true && true ^ false && false, condition.isTrue());

        condition = engine.getCondition("@true && true ^ false && false");
        assertEquals(true && true ^ false && false, condition.isTrue());

        condition = engine.getCondition("@true && true ^ false && true");
        assertEquals(true && true ^ false && true, condition.isTrue());

        condition = engine.getCondition("@true && true ^ true && false");
        assertEquals(true && true ^ true && false, condition.isTrue());

        condition = engine.getCondition("@true && true ^ true && true");
        assertEquals(true && true ^ true && true, condition.isTrue());

        condition = engine.getCondition("@false ^ false && false ^ false");
        assertEquals(false ^ false && false ^ false, condition.isTrue());

        condition = engine.getCondition("@false ^ false && false ^ true");
        assertEquals(false ^ false && false ^ true, condition.isTrue());

        condition = engine.getCondition("@false ^ false && true ^ false");
        assertEquals(false ^ false && true ^ false, condition.isTrue());

        condition = engine.getCondition("@false ^ false && true ^ true");
        assertEquals(false ^ false && true ^ true, condition.isTrue());

        condition = engine.getCondition("@false ^ true && false ^ false");
        assertEquals(false ^ true && false ^ false, condition.isTrue());

        condition = engine.getCondition("@false ^ true && false ^ false");
        assertEquals(false ^ true && false ^ false, condition.isTrue());

        condition = engine.getCondition("@false ^ true && false ^ true");
        assertEquals(false ^ true && false ^ true, condition.isTrue());

        condition = engine.getCondition("@false ^ true && true ^ false");
        assertEquals(false ^ true && true ^ false, condition.isTrue());

        condition = engine.getCondition("@false ^ true && true ^ true");
        assertEquals(false ^ true && true ^ true, condition.isTrue());

        condition = engine.getCondition("@true ^ false && false ^ false");
        assertEquals(true ^ false && false ^ false, condition.isTrue());

        condition = engine.getCondition("@true ^ false && false ^ true");
        assertEquals(true ^ false && false ^ true, condition.isTrue());

        condition = engine.getCondition("@true ^ false && true ^ false");
        assertEquals(true ^ false && true ^ false, condition.isTrue());

        condition = engine.getCondition("@true ^ false && true ^ true");
        assertEquals(true ^ false && true ^ true, condition.isTrue());

        condition = engine.getCondition("@true ^ true && false ^ false");
        assertEquals(true ^ true && false ^ false, condition.isTrue());

        condition = engine.getCondition("@true ^ true && false ^ false");
        assertEquals(true ^ true && false ^ false, condition.isTrue());

        condition = engine.getCondition("@true ^ true && false ^ true");
        assertEquals(true ^ true && false ^ true, condition.isTrue());

        condition = engine.getCondition("@true ^ true && true ^ false");
        assertEquals(true ^ true && true ^ false, condition.isTrue());

        condition = engine.getCondition("@true ^ true && true ^ true");
        assertEquals(true ^ true && true ^ true, condition.isTrue());
    }
}


