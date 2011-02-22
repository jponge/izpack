package com.izforge.izpack.merge.resolve;

import java.net.URL;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for resolveUtils
 */
public class ResolveUtilsTest {

    @Test
    public void testConvertPathToPosixPath() throws Exception 
    {
        assertThat(ResolveUtils.convertPathToPosixPath("C:\\Users\\gaou\\.m2") , is("C:/Users/gaou/.m2"));
    }
    
    @Test
    public void testIsFileInJar() throws Exception
    {
        URL container = ClassLoader.getSystemResource("com/izforge/izpack/merge/test/jar-hellopanel-1.0-SNAPSHOT.jar");
        URL resource = new URL(container.toString() + "!/jar/izforge/izpack/panels/hello/HelloPanel.class");
        assertThat(ResolveUtils.isFileInJar(resource), is(true));
        
        resource = new URL(container.toString() + "!/jar/izforge/izpack/panels/hello/");
        assertThat(ResolveUtils.isFileInJar(resource), is(false));
    }
}
