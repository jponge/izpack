package com.izforge.izpack.api.data.binding;

/**
 * Help element for panel.
 *
 * @author Anthonin Bonnefoy
 */
public class Help
{
    /**
     * languagae of the help
     */
    private String iso3;
    /**
     * html source of the help
     */
    private String src;

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
