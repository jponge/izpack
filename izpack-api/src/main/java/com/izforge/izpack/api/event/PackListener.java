package com.izforge.izpack.api.event;

import java.util.List;

import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.exception.IzPackException;

/**
 * A listener for pack installation events.
 *
 * @author Tim Anderson
 */
public interface PackListener extends InstallListener
{

    /**
     * Initialises the listener.
     *
     * @throws IzPackException for any error
     */
    void initialise();

    /**
     * Invoked before packs are installed.
     *
     * @param packs    the packs to be installed
     * @param listener the progress listener
     * @throws IzPackException for any error
     */
    void beforePacks(List<Pack> packs, ProgressListener listener);

    /**
     * Invoked before a pack is installed.
     *
     * @param pack     the pack
     * @param i        the pack number
     * @param listener the progress listener
     * @throws IzPackException for any error
     */
    void beforePack(Pack pack, int i, ProgressListener listener);

    /**
     * Invoked after a pack is installed.
     *
     * @param pack     the pack
     * @param i        the pack number
     * @param listener the progress listener
     * @throws IzPackException for any error
     */
    void afterPack(Pack pack, int i, ProgressListener listener);

    /**
     * Invoked after packs are installed.
     *
     * @param packs    the installed packs
     * @param listener the progress listener
     */
    void afterPacks(List<Pack> packs, ProgressListener listener);

}
