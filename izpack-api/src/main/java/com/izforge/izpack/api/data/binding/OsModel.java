package com.izforge.izpack.api.data.binding;

import java.io.Serializable;

public class OsModel implements Serializable
{
    /**
     * OS architecture from java system properties
     */
    public String arch;
    /**
     * The OS family
     */
    public String family;
    /**
     * JRE version used for installation
     */
    public String jre;
    /**
     * OS name from java system properties
     */
    public String name;
    /**
     * OS version from java system properties
     */
    public String version;

    public OsModel(String arch, String family, String jre, String name, String version)
    {
        this.arch = arch;
        this.family = family;
        this.jre = jre;
        this.name = name;
        this.version = version;
    }

    public String getArch()
    {
        return arch;
    }

    public String getFamily()
    {
        return family;
    }

    public String getJre()
    {
        return jre;
    }

    public String getName()
    {
        return name;
    }

    public String getVersion()
    {
        return version;
    }

    @Override
    public String toString()
    {
        return "OsModel{" +
                "arch='" + arch + '\'' +
                ", family='" + family + '\'' +
                ", jre='" + jre + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}