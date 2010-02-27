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

    public String getArch()
    {
        return arch;
    }

    public void setArch(String arch)
    {
        this.arch = arch;
    }

    public String getFamily()
    {
        return family;
    }

    public void setFamily(String family)
    {
        this.family = family;
    }

    public String getJre()
    {
        return jre;
    }

    public void setJre(String jre)
    {
        this.jre = jre;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
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