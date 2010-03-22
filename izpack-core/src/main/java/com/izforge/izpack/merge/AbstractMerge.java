package com.izforge.izpack.merge;

import com.izforge.izpack.api.merge.Mergeable;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Abstract classes for all mergeable element.
 * Contains helper methods to managed the mergeContent map.
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
