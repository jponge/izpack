package com.izforge.izpack;

import java.io.Serializable;

/**
 * Encloses information about a executable file. This class abstracts the
 * way to do a system dependent postprocessing of installation.
 *
 * @author Olexij Tkatchenko <ot@parcs.de>
 */

public class ExecutableFile implements Serializable
{
    /** when to execute this file */
    public final static int POSTINSTALL = 0;
    public final static int NEVER = 1;

    /** type of a file */
    public final static int BIN = 0;
    public final static int JAR = 1;

    /** what to do if execution fails */
    public final static int ABORT = 0;
    public final static int WARN = 1;
    public final static int ASK = 2;

    /** The file path */
    public String path;

    /** Execution stage (NEVER, POSTINSTALL) */
    public int executionStage;

    /** Main class of jar file */
    public String mainClass ;

    /** type (BIN|JAR)*/
    public int type;

    /** Failure handling (ABORT, WARN, ASK) */
    public int onFailure;

    /** List of arguments */
    public java.util.ArrayList argList;

    /** List of operating systems to run on */
    public java.util.ArrayList osList;

    /**
     * Constructs a new uninitialized instance.
     */
    public ExecutableFile() {
        this.path = null;
        executionStage = NEVER;
        mainClass = null;
        type = BIN;
        onFailure = ASK;
        osList = null;
        argList = null;
    }

    /**
     * Constructs and initializes a new instance.
     *
     * @param path the file path
     * @param executionStage when to execute
     * @param onFailure what to do if execution fails
     * @param osList list of operating systems to run on
     */
    public ExecutableFile(String path, int executionStage,
                          int onFailure, java.util.ArrayList osList)
    {
        this.path = path;
        this.executionStage = executionStage;
        this.onFailure = onFailure;
        this.osList = osList;
    }

    public ExecutableFile(String path,
                          int type ,
                          String mainClass,
                          int executionStage,
                          int onFailure,
                          java.util.ArrayList argList,
                          java.util.ArrayList osList)  {
        this.path = path;
        this.mainClass = mainClass;
        this.type = type;
        this.executionStage = executionStage;
        this.onFailure = onFailure;
        this.argList = argList;
        this.osList = osList;
    }

}
