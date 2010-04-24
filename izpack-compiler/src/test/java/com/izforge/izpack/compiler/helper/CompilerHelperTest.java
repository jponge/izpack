package com.izforge.izpack.compiler.helper;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;


/**
 * Compiler helper test
 */
public class CompilerHelperTest
{
    private CompilerHelper helper = new CompilerHelper();

    @Test
    public void testResolveJarPath() throws Exception
    {
        assertThat(helper.resolveCustomActionsJarPath("SummaryLogger"), is("bin/customActions/izpack-summary-logger.jar"));
    }

    @Test
    public void testConvertCamelToHyphen() throws Exception
    {
        assertThat(helper.convertNameToDashSeparated("abCdeF").toString(), is("-ab-cde-f"));
        assertThat(helper.convertNameToDashSeparated("aClassName").toString(), is("-a-class-name"));
    }
}
