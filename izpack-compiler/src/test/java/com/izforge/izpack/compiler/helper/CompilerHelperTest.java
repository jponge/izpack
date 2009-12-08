package com.izforge.izpack.compiler.helper;

import org.hamcrest.core.Is;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;


/**
 * Compiler helper test
 */
public class CompilerHelperTest {
    private CompilerHelper helper = new CompilerHelper();

    @Test
    public void testResolveJarPath() throws Exception {
        assertThat(helper.resolveCustomActionsJarPath("SummaryLogger"), Is.is("bin/customActions/izpack-summary-logger.jar"));
    }

    @Test
    public void testConvertCamelToHyphen() throws Exception {
        assertThat(helper.convertNameToDashSeparated("abCdeF").toString(), Is.is("-ab-cde-f"));
        assertThat(helper.convertNameToDashSeparated("aClassName").toString(), Is.is("-a-class-name"));
    }
}
