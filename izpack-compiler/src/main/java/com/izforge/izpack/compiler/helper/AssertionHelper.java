package com.izforge.izpack.compiler.helper;

import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.compiler.CompilerException;

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
}
