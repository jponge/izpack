package com.izforge.izpack.event;

import java.util.List;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.event.PackListener;
import com.izforge.izpack.api.event.ProgressListener;
import com.izforge.izpack.api.event.ProgressNotifiers;
import com.izforge.izpack.api.exception.IzPackException;


/**
 * Abstract implementation of {@link PackListener}.
 * <p/>
 * This provides no-op versions of each of the methods, to simplify implementation of listeners that only need
 * some methods.
 *
 * @author Tim Anderson
 */
public abstract class AbstractPackListener implements PackListener
{

    /**
     * The installation data.
     */
    private final InstallData installData;

    /**
     * The notifiers. May be {@code null}.
     */
    private final ProgressNotifiers notifiers;


    /**
     * Constructs an {@code AbstractPackListener}.
     *
     * @param installData the installation data
     */
    public AbstractPackListener(InstallData installData)
    {
        this(installData, null);
    }


    /**
     * Constructs an {@code AbstractPackListener}.
     *
     * @param installData the installation data
     * @param notifiers   the progress notifiers. May be {@code null}
     */
    public AbstractPackListener(InstallData installData, ProgressNotifiers notifiers)
    {
        this.installData = installData;
        this.notifiers = notifiers;
    }

    /**
     * Initialises the listener.
     *
     * @throws IzPackException for any error
     */
    @Override
    public void initialise()
    {
    }

    /**
     * Invoked before packs are installed.
     *
     * @param packs    the packs to be installed
     * @param listener the progress listener
     * @throws IzPackException for any error
     */
    @Override
    public void beforePacks(List<Pack> packs, ProgressListener listener)
    {
    }

    /**
     * Invoked before a pack is installed.
     *
     * @param pack     the pack
     * @param i        the pack number
     * @param listener the progress listener
     * @throws IzPackException for any error
     */
    @Override
    public void beforePack(Pack pack, int i, ProgressListener listener)
    {
    }

    /**
     * Invoked after a pack is installed.
     *
     * @param pack     the pack
     * @param i        the pack number
     * @param listener the progress listener
     * @throws IzPackException for any error
     */
    @Override
    public void afterPack(Pack pack, int i, ProgressListener listener)
    {
    }

    /**
     * Invoked after packs are installed.
     *
     * @param packs    the installed packs
     * @param listener the progress listener
     * @throws IzPackException for any error
     */
    @Override
    public void afterPacks(List<Pack> packs, ProgressListener listener)
    {
    }

    /**
     * Determines if listeners should notify a {@link ProgressListener}.
     *
     * @return {@code true} if the {@link ProgressListener} should be notified
     */
    protected boolean notifyProgress()
    {
        return (notifiers != null && notifiers.notifyProgress());
    }

    /**
     * Returns the progress notifier id of this listener.
     *
     * @return the progress notifier id of this listener, or {@code 0} if this is not registered
     */
    protected int getProgressNotifierId()
    {
        return notifiers != null ? notifiers.indexOf(this) + 1 : 0;
    }

    /**
     * Register this listener as a progress notifier.
     */
    protected void setProgressNotifier()
    {
        if (notifiers != null)
        {
            notifiers.addNotifier(this);
        }
    }

    /**
     * Returns the progress notifiers.
     *
     * @return the progress notifiers, or {@code null} if none was supplied at construction
     */
    protected ProgressNotifiers getProgressNotifiers()
    {
        return notifiers;
    }

    /**
     * Returns the installation data.
     *
     * @return the installation data
     */
    protected InstallData getInstallData()
    {
        return installData;
    }

    /**
     * Helper to return a localised message, given its identifier.
     *
     * @param id the message identifier
     * @return the corresponding message, or {@code id} if it doesn't exist
     */
    protected String getMessage(String id)
    {
        return installData.getMessages().get(id);
    }

}
