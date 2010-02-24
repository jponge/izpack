package com.izforge.izpack.api.data;

/**
 * Listener entity
 *
 * @author Anthonin Bonnefoy
 */
public class IzpackListener
{
    private String className;

    private Stage stage;

    public IzpackListener(String className, Stage stage)
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
