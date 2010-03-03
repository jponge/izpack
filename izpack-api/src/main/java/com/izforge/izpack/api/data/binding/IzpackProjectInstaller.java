package com.izforge.izpack.api.data.binding;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Global model that will contains all xml information
 *
 * @author Anthonin Bonnefoy
 */
public class IzpackProjectInstaller implements Serializable
{

    private List<Listener> listeners = new ArrayList<Listener>();

    public void add(Listener listener)
    {
        this.listeners.add(listener);
    }

    public List<Listener> getListeners()
    {
        return listeners;
    }

    public void fillWithDefault()
    {
        if (listeners == null)
        {
            listeners = Collections.emptyList();
        }
    }
}
