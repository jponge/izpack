package com.izforge.izpack.unpacker;

/**
 * Interface for registryInstallerListener to interrupt unpacker
 */
public interface IDiscardInterruptable {
    void setDiscardInterrupt(boolean di);
}
