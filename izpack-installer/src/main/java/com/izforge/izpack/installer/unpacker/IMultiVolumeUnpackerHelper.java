package com.izforge.izpack.installer.unpacker;

import java.io.File;

public interface IMultiVolumeUnpackerHelper
{
    public File enterNextMediaMessage(String volumename, boolean lastcorrupt);

    public File enterNextMediaMessage(String volumename);
}