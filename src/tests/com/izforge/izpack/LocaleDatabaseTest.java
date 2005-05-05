package com.izforge.izpack;

import junit.framework.TestCase;

public class LocaleDatabaseTest extends TestCase
{

    public void testGetString() throws Exception
    {
        LocaleDatabase db = new LocaleDatabase(LocaleDatabaseTest.class
                .getResourceAsStream("testing-langpack.xml"));
        
        TestCase.assertEquals("String Text", db.getString("string"));
        TestCase.assertEquals("none(???)", db.getString("none"));
    }

}
