package com.izforge.izpack.api.data.binding;

import java.io.Serializable;

/**
 * Help element for panel.
 *
 * @author Anthonin Bonnefoy
 */
public class Help implements Serializable
{
    /**
     * auto-generated version number
     */
    private static final long serialVersionUID = -2560125306490380153L;
    /**
     * language of the help
     */
    private String iso3;
    /**
     * html source of the help
     */
    private String src;

    public Help(String iso3, String src)
    {
        this.iso3 = iso3;
        this.src = src;
    }

    public String getIso3()
    {
        return iso3;
    }

    public void setIso3(String iso3)
    {
        this.iso3 = iso3;
    }

    public String getSrc()
    {
        return src;
    }

    public void setSrc(String src)
    {
        this.src = src;
    }
}
