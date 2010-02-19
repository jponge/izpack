package com.izforge.izpack.installer;

import com.izforge.izpack.util.AbstractUIProgressHandler;

import java.io.File;

public interface IMultiVolumeUnpackerHelper
{
    public void init(AutomatedInstallData idata, AbstractUIProgressHandler handler);

    public File enterNextMediaMessage(String volumename, boolean lastcorrupt);

    public File enterNextMediaMessage(String volumename);
}