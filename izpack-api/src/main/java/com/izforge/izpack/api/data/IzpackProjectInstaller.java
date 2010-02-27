package com.izforge.izpack.api.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Global model that will contains all xml information
 *
 * @author Anthonin Bonnefoy
 */
public class IzpackProjectInstaller
{
    private List<IzpackListener> izpackListeners = new ArrayList<IzpackListener>();


    public void addIzpackListeners(IzpackListener izpackListener)
    {
        this.izpackListeners.add(izpackListener);
    }

    public List<IzpackListener> getIzpackListeners()
    {
        return izpackListeners;
    }
}
