package com.izforge.izpack.compiler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Helper class for packager classes
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 */
public class PackagerHelper
{    
    /**
     * Copies all the data from the specified input stream to the specified output stream.
     * 
     * @param in the input stream to read
     * @param out the output stream to write
     * @return the total number of bytes copied
     * @exception IOException if an I/O error occurs
     */
    public static long copyStream(InputStream in, OutputStream out) throws IOException
    {
        byte[] buffer = new byte[5120];
        long bytesCopied = 0;
        int bytesInBuffer;
        while ((bytesInBuffer = in.read(buffer)) != -1)
        {
            out.write(buffer, 0, bytesInBuffer);
            bytesCopied += bytesInBuffer;
        }
        return bytesCopied;
    }
}
