package com.izforge.izpack.compiler.listener;

import com.izforge.izpack.compiler.packager.PackagerListener;

/**
 * Used to handle the packager messages in the command-line mode.
 *
 * @author julien created October 26, 2002
 */
public class CmdlinePackagerListener implements PackagerListener {

    /**
     * Print a message to the console at default priority (MSG_INFO).
     *
     * @param info The information.
     */
    public void packagerMsg(String info) {
        packagerMsg(info, MSG_INFO);
    }

    /**
     * Print a message to the console at the specified priority.
     *
     * @param info     The information.
     * @param priority priority to be used for the message prefix
     */
    public void packagerMsg(String info, int priority) {
        final String prefix;
        switch (priority) {
            case MSG_DEBUG:
                prefix = "[ DEBUG ] ";
                break;
            case MSG_ERR:
                prefix = "[ ERROR ] ";
                break;
            case MSG_WARN:
                prefix = "[ WARNING ] ";
                break;
            case MSG_INFO:
            case MSG_VERBOSE:
            default: // don't die, but don't prepend anything
                prefix = "";
        }

        System.out.println(prefix + info);
    }

    /**
     * Called when the packager starts.
     */
    public void packagerStart() {
        System.out.println("[ Begin ]");
        System.out.println();
    }

    /**
     * Called when the packager stops.
     */
    public void packagerStop() {
        System.out.println();
        System.out.println("[ End ]");
    }
}
