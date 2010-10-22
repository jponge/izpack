package com.izforge.izpack.merge.resolve;

import org.hamcrest.core.Is;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for resolveUtils
 */
public class ResolveUtilsTest {

    @Test
    public void testConvertPathToPosixPath() throws Exception {
        assertThat(ResolveUtils.convertPathToPosixPath("C:\\Users\\gaou\\.m2") , Is.is("C:/Users/gaou/.m2"));
    }
}
