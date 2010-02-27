package com.izforge.izpack.api.data.binding;

/**
 * Listener entity
 *
 * @author Anthonin Bonnefoy
 */
public class Listener
{
    private String className;

    private Stage stage;

    private OsModel os;

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

    @Override
    public String toString()
    {
        return "Listener{" +
                "className='" + className + '\'' +
                ", stage=" + stage +
                ", os=" + os +
                '}';
    }
}
