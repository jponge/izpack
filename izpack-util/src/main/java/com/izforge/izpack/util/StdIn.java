package com.izforge.izpack.util;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: sora
 * Date: Nov 23, 2009
 * Time: 11:37:06 PM
 * To change this template use File | Settings | File Templates.
 */
class StdIn extends Thread {

    private BufferedReader kb;

    private boolean processRunning;

    private PrintWriter op;

    public StdIn(Process p, ConsoleTextArea cta) {
        setDaemon(true);
        InputStreamReader ir = new InputStreamReader(cta.getIn());
        kb = new BufferedReader(ir);

        BufferedOutputStream os = new BufferedOutputStream(p.getOutputStream());
        op = new PrintWriter((new OutputStreamWriter(os)), true);
        processRunning = true;
    }

    public void run() {
        try {
            while (kb.ready() || processRunning) {
                if (kb.ready()) {
                    op.println(kb.readLine());
                }
            }
        }
        catch (IOException e) {
            System.err.println("Problem reading standard input.");
            System.err.println(e);
        }
    }

    public void done() {
        processRunning = false;
    }
}
