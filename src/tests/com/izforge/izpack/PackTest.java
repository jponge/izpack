package com.izforge.izpack;

import junit.framework.TestCase;

public class PackTest extends TestCase
{

    public void testToByteUnitsString()
    {
        TestCase.assertEquals("5 bytes", Pack.toByteUnitsString(5));
        TestCase.assertEquals("1 KB", Pack.toByteUnitsString(1024));
        TestCase.assertEquals("2 KB", Pack.toByteUnitsString(2048));
        TestCase.assertEquals("1 MB", Pack.toByteUnitsString(1024 * 1024));
        TestCase.assertEquals("1 GB", Pack.toByteUnitsString(1024 * 1024 * 1024));
    }

}
