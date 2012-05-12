package com.izforge.izpack.uninstaller.container;


import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.List;

import org.picocontainer.injectors.Provider;

import com.izforge.izpack.api.event.UninstallerListener;
import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.uninstaller.event.UninstallerListeners;


/**
 * A provider of {@link UninstallerListeners}.
 *
 * @author Tim Anderson
 */
public class UninstallerListenersProvider implements Provider
{

    /**
     * Provides an {@link UninstallerListeners} by reading the <em>uninstallerListeners</em> resource.
     *
     * @param resources used to locate the <em>uninstallerListeners</em> resource
     * @param factory   the factory to create the {@link UninstallerListener}s
     * @return the listeners
     * @throws IOException               for any I/O error
     * @throws ClassNotFoundException    if a class of a serialized object cannot be found
     * @throws ResourceNotFoundException if <em>uninstallerListeners</em> cannot be found
     */
    @SuppressWarnings("unchecked")
    public UninstallerListeners provide(Resources resources, ObjectFactory factory)
            throws IOException, ClassNotFoundException
    {
        UninstallerListeners result = new UninstallerListeners();

        InputStream in;
        ObjectInputStream objIn;
        in = resources.getInputStream("uninstallerListeners");
        objIn = new ObjectInputStream(in);
        List<String> classNames = (List<String>) objIn.readObject();
        objIn.close();
        for (String className : classNames)
        {
            UninstallerListener listener = factory.create(className, UninstallerListener.class);
            result.add(listener);
        }
        return result;
    }

}
