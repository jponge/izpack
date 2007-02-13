package com.izforge.izpack.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * This is a grabber for stdout and stderr. It will be launched once at command execution end
 * terminates if the apropriate stream runs out of data.
 * 
 * @author Olexij Tkatchenko <ot@parcs.de>
 */
public class MonitorInputStream implements Runnable
{

    private BufferedReader reader;

    private BufferedWriter writer;

    private boolean shouldStop = false;

    /**
     * Construct a new monitor.
     * 
     * @param in The input to read.
     * @param out The writer to write to.
     */
    public MonitorInputStream(Reader in, Writer out)
    {
        this.reader = new BufferedReader(in);
        this.writer = new BufferedWriter(out);
    }

    /**
     * Request stopping this thread.
     */
    public void doStop()
    {
        this.shouldStop = true;
    }

    /**
     * {@inheritDoc}
     */
    public void run()
    {
        try
        {
            String line;
            while ((line = this.reader.readLine()) != null)
            {
                this.writer.write(line);
                this.writer.newLine();
                this.writer.flush();
                if (this.shouldStop) return;
            }
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace(System.out);
        }
    }
}