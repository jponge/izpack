package com.izforge.izpack.merge;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Anthonin Bonnefoy
 */
public abstract class AbstractMerge implements Mergeable
{
    protected Map<OutputStream, List<String>> mergeContent;

    protected List<String> getMergeList(OutputStream outputStream)
    {
        if (!mergeContent.containsKey(outputStream))
        {
            mergeContent.put(outputStream, new ArrayList<String>());
        }
        return mergeContent.get(outputStream);
    }
}
