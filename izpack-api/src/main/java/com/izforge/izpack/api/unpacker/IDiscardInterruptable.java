package com.izforge.izpack.api.unpacker;

/**
 * Interface for registryInstallerListener to interrupt unpacker
 */
public interface IDiscardInterruptable
{
    void setDiscardInterrupt(boolean di);
}
