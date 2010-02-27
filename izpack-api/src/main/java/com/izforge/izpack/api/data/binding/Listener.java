package com.izforge.izpack.api.data.binding;

import com.izforge.izpack.api.data.Stage;

/**
 * Listener entity
 *
 * @author Anthonin Bonnefoy
 */
public class Listener
{
    private String className;

    private Stage stage;


    public Listener(String className, Stage stage)
    {
        this.className = className;
        this.stage = stage;
    }

    public String getClassName()
    {
        return className;
    }

    public Stage getStage()
    {
        return stage;
    }
}
