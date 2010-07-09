package com.izforge.izpack.installer.data;

import java.util.Properties;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.util.OsVersion;

public class InstallData extends AutomatedInstallData
{
    public InstallData(Properties variables, VariableSubstitutor variableSubstitutor)
    {
        super(variables, variableSubstitutor);
    }

    @Override
    public void setInstallPath(String path)
    {
        setVariable(INSTALL_PATH, path);
        setInstallDriveFromPath(path, INSTALL_DRIVE);
    }

    @Override
    public String getInstallPath()
    {
        return getVariable(INSTALL_PATH);
    }

    @Override
    public void setDefaultInstallPath(String path)
    {
        setVariable(DEFAULT_INSTALL_PATH, path);
        setInstallDriveFromPath(path, DEFAULT_INSTALL_DRIVE);
    }

    @Override
    public String getDefaultInstallPath()
    {
        return getVariable(DEFAULT_INSTALL_PATH);
    }

    private void setInstallDriveFromPath(String path, String variable)
    {
        if (OsVersion.IS_WINDOWS)
        {
            String[] parts = path.trim().split(":", 2);
            if (parts.length > 0 && parts[0].length() == 1)
            {
                setVariable(variable, parts[0] + ":");
            }
        }
    }

}
