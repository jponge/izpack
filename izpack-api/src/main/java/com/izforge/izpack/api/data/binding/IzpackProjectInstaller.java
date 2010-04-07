package com.izforge.izpack.api.data.binding;

import com.izforge.izpack.api.data.Panel;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Global model that will contain all xml information
 *
 * @author Anthonin Bonnefoy
 */
public class IzpackProjectInstaller implements Serializable
{

    private List<Listener> listeners;

    private List<Panel> panels;

    public void add(Listener listener)
    {
        this.listeners.add(listener);
    }


    public List<Listener> getListeners()
    {
        return listeners;
    }

    public List<Panel> getPanels()
    {
        return panels;
    }

    public void fillWithDefault()
    {
        if (listeners == null)
        {
            listeners = Collections.emptyList();
        }
    }
}
