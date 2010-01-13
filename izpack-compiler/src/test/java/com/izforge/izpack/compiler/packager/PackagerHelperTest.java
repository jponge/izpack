package com.izforge.izpack.compiler.packager;

import org.junit.Test;

/**
 * Test for packager helper methods
 *
 * @author Anthonin Bonnefoy
 */
public class PackagerHelperTest {
    @Test
    public void testGetClasseFile() throws Exception {
        PackagerHelper.getClasseFile("org.apache.tools.bzip2.CRC");
    }
}
