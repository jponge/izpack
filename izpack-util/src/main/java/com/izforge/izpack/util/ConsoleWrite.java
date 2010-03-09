package com.izforge.izpack.util;

/**
 * Created by IntelliJ IDEA.
 * User: sora
 * Date: Nov 23, 2009
 * Time: 11:37:06 PM
 * To change this template use File | Settings | File Templates.
 */
class ConsoleWrite implements Runnable
{

    private ConsoleTextArea textArea;

    private String str;

    public ConsoleWrite(ConsoleTextArea textArea, String str)
    {
        this.textArea = textArea;
        this.str = str;
    }

    public void run()
    {
        textArea.write(str);
    }
}

