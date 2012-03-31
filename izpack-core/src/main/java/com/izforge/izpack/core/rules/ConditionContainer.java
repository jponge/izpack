package com.izforge.izpack.core.rules;

import com.izforge.izpack.api.container.Container;
import com.izforge.izpack.core.container.AbstractDelegatingContainer;


/**
 * Condition container.
 *
 * @author Anthonin Bonnefoy
 */
public class ConditionContainer extends AbstractDelegatingContainer
{

    /**
     * Constructs a <tt>ConditionContainer</tt>.
     *
     * @param parent the parent container
     */
    public ConditionContainer(Container parent)
    {
        super(parent.createChildContainer());
    }

}
