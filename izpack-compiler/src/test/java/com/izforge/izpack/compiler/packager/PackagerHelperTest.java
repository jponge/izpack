package com.izforge.izpack.compiler.packager;

import com.izforge.izpack.compiler.merge.MergeManager;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.hamcrest.number.IsGreaterThan;
import org.junit.Before;
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
    private MergeManager mergeManager;

    @Before
    public void setUp() {
    }

    @Test
    public void testGetPackagesFileFromJar() throws Exception {
        mergeManager = new MergeManager("org/apache/tools/bzip2");
        List<File> listBzipClasses = mergeManager.getClassesFileInClasspath("org/apache/tools/bzip2");
        assertThat(listBzipClasses, IsNull.<Object>notNullValue());
        assertThat(listBzipClasses.size(), Is.is(3));
    }

    @Test
    public void testGetPackagesFileFromTarget() throws Exception {
        List<File> listBzipClasses = mergeManager.getClassesFileInClasspath("com/izforge");
        assertThat(listBzipClasses, IsNull.<Object>notNullValue());
        assertThat(listBzipClasses.size(), new IsGreaterThan<Integer>(2));
    }

    @Test
    public void testGetClasseFile() throws Exception {
        File classeFile = mergeManager.getClasseFile("org/apache/tools/bzip2/CRC.class");
        assertThat(classeFile, IsNull.<Object>notNullValue());
    }
}
