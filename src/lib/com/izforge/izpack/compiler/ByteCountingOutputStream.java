/*
 *  $Id
 *  IzPack
 *  Copyright (C) 2001-2004 Julien Ponge
 *
 *  File :               ByteCountingOutputStream.java
 *  Description :        Counts bytes that are written
 *  Author's email :     julien@izforge.com
 *  Author's Website :   http://www.izforge.com
 *
 *  Portions are Copyright (c) 2001 Johannes Lehtinen
 *  johannes.lehtinen@iki.fi
 *  http://www.iki.fi/jle/
 *
 *  Portions are Copyright (c) 2002 Paul Wilkinson
 *  paulw@wilko.com
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.izforge.izpack.compiler;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Stream which countes the bytes written through it. Be sure to flush before
 * checking size.
 */
public class ByteCountingOutputStream extends OutputStream
{

    private long count;

    private OutputStream os;

    public ByteCountingOutputStream(OutputStream os)
    {
        this.os = os;
    }

    public void write(byte[] b, int off, int len) throws IOException
    {
        os.write(b, off, len);
        count += len;
    }

    public void write(byte[] b) throws IOException
    {
        os.write(b);
        count += b.length;
    }

    public void write(int b) throws IOException
    {
        os.write(b);
        count++;
    }

    public void close() throws IOException
    {
        os.close();
    }

    public void flush() throws IOException
    {
        os.flush();
    }

    public long getByteCount()
    {
        return count;
    }
}
