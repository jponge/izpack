package com.izforge.izpack.compiler.helper;

import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.compiler.CompilerException;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Anthonin Bonnefoy
 */
public class AssertionHelper {

    /**
     * Create parse error with consistent messages. Includes file name. For use When parent is
     * unknown.
     *
     * @param message     Brief message explaining error
     * @param installFile
     */
    public static void parseError(String message, String installFile) throws CompilerException {
        throw new CompilerException(installFile + ":" + message);
    }

    /**
     * Create parse error with consistent messages. Includes file name and line # of parent. It is
     * an error for 'parent' to be null.
     *
     * @param parent      The element in which the error occured
     * @param message     Brief message explaining error
     * @param installFile
     */
    public static void parseError(IXMLElement parent, String message, String installFile) throws CompilerException {
        throw new CompilerException(installFile + ":" + parent.getLineNr() + ": " + message);
    }

    /**
     * Create a chained parse error with consistent messages. Includes file name and line # of
     * parent. It is an error for 'parent' to be null.
     *
     * @param installFile
     * @param parent      The element in which the error occured
     * @param message     Brief message explaining error
     */
    public static void parseError(IXMLElement parent, String message, Throwable cause, String installFile)
            throws CompilerException {
        throw new CompilerException(installFile + ":" + parent.getLineNr() + ": " + message, cause);
    }

    /**
     * Create a parse warning with consistent messages. Includes file name and line # of parent. It
     * is an error for 'parent' to be null.
     *
     * @param parent      The element in which the warning occured
     * @param message     Warning message
     * @param installFile
     */
    public static void parseWarn(IXMLElement parent, String message, String installFile) {
        System.out.println("Warning: " + installFile + ":" + parent.getLineNr() + ": " + message);
    }

    /**
     * Checks whether a File instance is a regular file, exists and is readable. Throws appropriate
     * CompilerException to report violations of these conditions.
     *
     * @throws com.izforge.izpack.compiler.CompilerException
     *          if the file is either not existing, not a regular file or not
     *          readable.
     */
    public static void assertIsNormalReadableFile(File fileToCheck, String fileDescription)
            throws CompilerException {
        if (fileToCheck != null) {
            if (!fileToCheck.exists()) {
                throw new CompilerException(fileDescription
                        + " does not exist: " + fileToCheck);
            }
            if (!fileToCheck.isFile()) {
                throw new CompilerException(fileDescription
                        + " is not a regular file: " + fileToCheck);
            }
            if (!fileToCheck.canRead()) {
                throw new CompilerException(fileDescription
                        + " is not readable by application: " + fileToCheck);
            }
        }
    }

}
