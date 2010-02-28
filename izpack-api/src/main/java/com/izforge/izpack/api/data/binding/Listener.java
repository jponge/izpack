package com.izforge.izpack.api.data.binding;

import java.util.List;

/**
 * Listener entity
 *
 * @author Anthonin Bonnefoy
 */
public class Listener
{
    private String classname;

    private Stage stage;

    private List<OsModel> osList;

    private String jar;

    public Listener(String classname, Stage stage, List<OsModel> osList, String jar)
    {
        this.classname = classname;
        this.stage = stage;
        this.osList = osList;
        this.jar = jar;
    }

    public String getClassname()
    {
        return classname;
    }

    public String getJar()
    {
        return jar;
    }

    public Stage getStage()
    {
        return stage;
    }

    public List<OsModel> getOsList()
    {
        return osList;
    }

    @Override
    public String toString()
    {
        return "Listener{" +
                "classname='" + classname + '\'' +
                ", stage=" + stage +
                ", osList=" + osList +
                '}';
    }
}
