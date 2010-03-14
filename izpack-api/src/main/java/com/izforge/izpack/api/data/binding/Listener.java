package com.izforge.izpack.api.data.binding;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Listener entity
 *
 * @author Anthonin Bonnefoy
 */
public class Listener implements Serializable
{
    private String classname;

    private Stage stage;

    private List<OsModel> os;

    private String jar;

    /**
     * Files necessary for the listener. Filled by the compiler during installation generation.
     */
    private List<String> files;

    public Listener(String classname, Stage stage, List<OsModel> os, String jar)
    {
        this.classname = classname;
        this.stage = stage;
        this.os = os;
        this.jar = jar;
        files = new ArrayList<String>();
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

    public List<OsModel> getOs()
    {
        return os;
    }

    @Override
    public String toString()
    {
        return "Listener{" +
                "classname='" + classname + '\'' +
                ", stage=" + stage +
                ", os=" + os +
                '}';
    }

    public List<String> getFiles()
    {
        return files;
    }

    public void setFiles(List<String> files)
    {
        this.files = files;
    }
}
