package com.izforge.izpack.compiler.packager;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.hamcrest.number.IsGreaterThan;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for packager helper methods
 *
 * @author Anthonin Bonnefoy
 */
public class PackagerHelperTest {
    @Test
    public void testGetPackagesFileFromJar() throws Exception {
        List<File> listBzipClasses = PackagerHelper.getClassesFileInClasspath("org/apache/tools/bzip2");
        assertThat(listBzipClasses, IsNull.<Object>notNullValue());
        assertThat(listBzipClasses.size(), Is.is(3));
    }

    @Test
    public void testGetPackagesFileFromTarget() throws Exception {
        List<File> listBzipClasses = PackagerHelper.getClassesFileInClasspath("com/izforge");
        assertThat(listBzipClasses, IsNull.<Object>notNullValue());
        assertThat(listBzipClasses.size(), new IsGreaterThan<Integer>(2));
    }

    @Test
    public void testGetClasseFile() throws Exception {
        File classeFile = PackagerHelper.getClasseFile("org/apache/tools/bzip2/CRC.class");
        assertThat(classeFile, IsNull.<Object>notNullValue());
    }
}
