package com.izforge.izpack.matcher;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.TypeSafeMatcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Matcher for object serialized inside a jar
 *
 * @author Anthonin Bonnefoy
 */
public class ObjectInputMatcher extends TypeSafeMatcher<File>
{
    private Matcher<Object> listMatcher;
    private String resourceId;

    ObjectInputMatcher(String resourceId, Matcher<Object> listMatcher)
    {
        this.listMatcher = listMatcher;
        this.resourceId = resourceId;
    }

    @Override
    public boolean matchesSafely(File file)
    {
        try
        {
            Object object = getObjectFromZip(file, resourceId);
            MatcherAssert.assertThat(object, listMatcher);
            return true;
        }
        catch (Exception e)
        {
            throw new AssertionError(e);
        }
    }


    public static Object getObjectFromZip(File file, String resourceId)
            throws IOException, ClassNotFoundException
    {
        FileInputStream fis = new FileInputStream(file);
        ZipInputStream zis = new ZipInputStream(fis);
        Object result = null;
        ZipEntry ze;
        while ((ze = zis.getNextEntry()) != null)
        {
            if (ze.getName().equals(resourceId))
            {
                ObjectInputStream inputStream = new ObjectInputStream(zis);
                result = inputStream.readObject();
            }
            zis.closeEntry();
        }
        zis.close();
        return result;
    }

    public void describeTo(Description description)
    {
        description.appendText("Excepting file containing ").appendValue(listMatcher);
    }

    public static ObjectInputMatcher isInputMatching(String resourceId, Matcher<Object> objectMatcher)
    {
        return new ObjectInputMatcher(resourceId, objectMatcher);
    }


}
