package com.izforge.izpack.api.data.binding;

import java.io.Serializable;

/**
 * Action for panel
 *
 * @author Anthonin Bonnefoy
 */
public class Action implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String classname;

    private ActionStage actionStage;

    public Action(String classname, ActionStage actionStage)
    {
        this.classname = classname;
        this.actionStage = actionStage;
    }

    public String getClassname()
    {
        return classname;
    }

    public ActionStage getActionStage()
    {
        return actionStage;
    }
}
