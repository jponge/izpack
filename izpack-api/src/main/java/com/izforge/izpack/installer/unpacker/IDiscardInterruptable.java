package com.izforge.izpack.installer.unpacker;

/**
 * Interface for registryInstallerListener to interrupt unpacker
 */
public interface IDiscardInterruptable {
    void setDiscardInterrupt(boolean di);
}
