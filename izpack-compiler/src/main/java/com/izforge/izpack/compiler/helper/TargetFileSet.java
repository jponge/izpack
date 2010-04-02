package com.izforge.izpack.compiler.helper;

import java.util.*;

import com.izforge.izpack.api.data.*;
import com.izforge.izpack.api.data.binding.OsModel;
import com.izforge.izpack.util.file.types.FileSet;


public class TargetFileSet extends FileSet
{
    private String targetDir;
    List<OsModel> osList;
    OverrideType override;
    String overrideRenameTo;
    Blockable blockable;
    Map additionals;
    String condition;

    public String getTargetDir()
    {
        return targetDir;
    }
    public void setTargetDir(String targetDir)
    {
        this.targetDir = targetDir;
    }

    public List<OsModel> getOsList()
    {
        return osList;
    }

    public void setOsList(List<OsModel> osList)
    {
        this.osList = osList;
    }

    public OverrideType getOverride()
    {
        return override;
    }

    public void setOverride(OverrideType override)
    {
        this.override = override;
    }

    public String getOverrideRenameTo()
    {
        return overrideRenameTo;
    }

    public void setOverrideRenameTo(String overrideRenameTo)
    {
        this.overrideRenameTo = overrideRenameTo;
    }
    public Blockable getBlockable()
    {
        return blockable;
    }

    public void setBlockable(Blockable blockable)
    {
        this.blockable = blockable;
    }

    public Map getAdditionals()
    {
        return additionals;
    }

    public void setAdditionals(Map additionals)
    {
        this.additionals = additionals;
    }

    public String getCondition()
    {
        return condition;
    }

    public void setCondition(String condition)
    {
        this.condition = condition;
    }
}
