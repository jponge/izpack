package com.izforge.izpack.installer.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;


/**
 * I/O streams to support prompting and keyboard input from the console.
 *
 * @author Tim Anderson
 */
public class Console
{

    /**
     * Input stream.
     */
    private final BufferedReader in;

    /**
     * Output stream.
     */
    private final PrintWriter out;

    /**
     * Constructs a <tt>Console</tt> with <tt>System.in</tt> and <tt>System.out</tt> as the I/O streams.
     */
    public Console()
    {
        this(System.in, System.out);
    }

    /**
     * Constructs a <tt>Console</tt>.
     *
     * @param in  the input stream
     * @param out the output stream
     */
    public Console(InputStream in, OutputStream out)
    {
        this.in = new BufferedReader(new InputStreamReader(in));
        this.out = new PrintWriter(out, true);
    }

    /**
     * Reads a line of text.  A line is considered to be terminated by any one
     * of a line feed ('\\n'), a carriage return ('\\r'), or a carriage return
     * followed immediately by a linefeed.
     *
     * @return a String containing the contents of the line, not including any line-termination characters, or
     *         null if the end of the stream has been reached
     * @throws IOException if an I/O error occurs
     */
    public String readLine() throws IOException
    {
        return in.readLine();
    }

    /**
     * Prints a message to the console.
     *
     * @param message the message to print
     */
    public void print(String message)
    {
        out.print(message);
    }

    /**
     * Prints a message to the console with a new line.
     *
     * @param message the message to print
     */
    public void println(String message)
    {
        out.println(message);
    }
}
