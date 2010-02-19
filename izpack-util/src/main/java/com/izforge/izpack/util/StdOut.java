package com.izforge.izpack.util;

import javax.swing.*;
import java.io.InputStreamReader;

/**
 * Created by IntelliJ IDEA.
 * User: sora
 * Date: Nov 23, 2009
 * Time: 11:37:06 PM
 * To change this template use File | Settings | File Templates.
 */
class StdOut extends Thread
{

    private InputStreamReader output;

    private boolean processRunning;

    private ConsoleTextArea cta;

    private StringBuffer data;

    public StdOut(Process p, ConsoleTextArea cta)
    {
        setDaemon(true);
        output = new InputStreamReader(p.getInputStream());
        this.cta = cta;
        processRunning = true;
        data = new StringBuffer();
    }

    public void run()
    {
        try
        {
            /*
             * Loop as long as there is output from the process to be displayed or as long as the
             * process is still running even if there is presently no output.
             */
            while (output.ready() || processRunning)
            {

                // If there is output get it and display it.
                if (output.ready())
                {
                    char[] array = new char[255];
                    int num = output.read(array);
                    if (num != -1)
                    {
                        String s = new String(array, 0, num);
                        data.append(s);
                        SwingUtilities.invokeAndWait(new ConsoleWrite(cta, s));
                    }
                }
            }
        }
        catch (Exception e)
        {
            System.err.println("Problem writing to standard output.");
            System.err.println(e);
        }
    }

    public void done()
    {
        processRunning = false;
    }

    public String getData()
    {
        return data.toString();
    }
}
