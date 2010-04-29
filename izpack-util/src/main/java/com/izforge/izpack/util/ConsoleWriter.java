package com.izforge.izpack.util;

import javax.swing.SwingUtilities;

/**
 * Created by IntelliJ IDEA.
 * User: sora
 * Date: Nov 23, 2009
 * Time: 11:37:07 PM
 * To change this template use File | Settings | File Templates.
 */
class ConsoleWriter extends java.io.OutputStream
{

    private ConsoleTextArea textArea;

    private StringBuffer buffer;

    public ConsoleWriter(ConsoleTextArea textArea)
    {
        this.textArea = textArea;
        buffer = new StringBuffer();
    }

    public synchronized void write(int ch)
    {
        buffer.append((char) ch);
        if (ch == '\n')
        {
            flushBuffer();
        }
    }

    public synchronized void write(char[] data, int off, int len)
    {
        for (int i = off; i < len; i++)
        {
            buffer.append(data[i]);
            if (data[i] == '\n')
            {
                flushBuffer();
            }
        }
    }

    public synchronized void flush()
    {
        if (buffer.length() > 0)
        {
            flushBuffer();
        }
    }

    public void close()
    {
        flush();
    }

    private void flushBuffer()
    {
        String str = buffer.toString();
        buffer.setLength(0);
        SwingUtilities.invokeLater(new ConsoleWrite(textArea, str));
    }
}