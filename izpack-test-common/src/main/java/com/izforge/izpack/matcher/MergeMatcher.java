package com.izforge.izpack.matcher;

import com.izforge.izpack.api.merge.Mergeable;
import com.izforge.izpack.mock.MockOutputStream;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.collection.IsCollectionContaining;
import org.hamcrest.core.Is;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Matcher for mergeable
 *
 * @author Anthonin Bonnefoy
 */
public class MergeMatcher extends TypeSafeMatcher<Mergeable>
{
    private Matcher<Iterable<String>> listMatcher;

    MergeMatcher(Matcher<Iterable<String>> listMatcher)
    {
        this.listMatcher = listMatcher;
    }

    @Override
    public boolean matchesSafely(Mergeable mergeable)
    {
        try
        {
            MockOutputStream outputStream = new MockOutputStream();
            mergeable.merge(outputStream);
            List<String> entryName = outputStream.getListEntryName();
            assertThat(entryName, listMatcher);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return true;
    }

    public void describeTo(Description description)
    {
        description.appendText("Excepting meargeable containing ").appendValue(listMatcher);
    }

    public static MergeMatcher isMergeableContainingFile(String fileName)
    {
        return new MergeMatcher(IsCollectionContaining.hasItem(Is.is(fileName)));
    }

    public static MergeMatcher isMergeableMatching(Matcher<Iterable<String>> matcher)
    {
        return new MergeMatcher(matcher);
    }

    public static MergeMatcher isMergeableContainingFiles(String... files)
    {
        return new MergeMatcher(IsCollectionContaining.hasItems(files));
    }


}