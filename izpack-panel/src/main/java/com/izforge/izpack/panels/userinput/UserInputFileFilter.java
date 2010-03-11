package com.izforge.izpack.panels.userinput;

import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Anthonin Bonnefoy
 */
public class UserInputFileFilter extends FileFilter
{

    String fileext = "";

    String description = "";

    public void setFileExt(String fileext)
    {
        this.fileext = fileext;
    }

    public void setFileExtDesc(String desc)
    {
        this.description = desc;
    }

    public boolean accept(File pathname)
    {
        if (pathname.isDirectory())
        {
            return true;
        }
        else
        {
            return pathname.getAbsolutePath().endsWith(this.fileext);
        }
    }

    public String getDescription()
    {
        return this.description;
    }
}
