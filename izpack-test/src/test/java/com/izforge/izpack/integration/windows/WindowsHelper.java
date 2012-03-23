package com.izforge.izpack.integration.windows;

import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.core.os.RegistryHandler;
import com.izforge.izpack.util.Librarian;
import com.izforge.izpack.util.os.ShellLink;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Helper for Windows tests.
 *
 * @author Tim Anderson
 */
public class WindowsHelper
{
    /**
     * Verifies a shortcut matches that expected.
     *
     * @param linkType    the type of link, one of the following values: <br>
     *                    <ul>
     *                    <li><code>ShellLink.DESKTOP</code>
     *                    <li><code>ShellLink.PROGRAM_MENU</code>
     *                    <li><code>ShellLink.START_MENU</code>
     *                    <li><code>ShellLink.STARTUP</code>
     *                    </ul>
     * @param userType    the type of user for the link path
     * @param group       the program group (directory) of this link. If the link is not part of a program
     *                    group, pass an empty string or null for this parameter. (...\\Desktop\\group).
     * @param name        the file name of this link. Do not include a file extension.
     * @param target      the expected target of the link
     * @param description the expected shortcut description
     * @param librarian   the librarian
     * @return the shortcut
     * @throws Exception for any error
     */
    public static File checkShortcut(int linkType, int userType, String group, String name, File target,
                                     String description, Librarian librarian) throws Exception
    {
        ShellLink link = new ShellLink(linkType, userType, group, name, librarian);
        assertEquals(linkType, link.getLinkType());
        assertEquals(userType, link.getUserType());
        assertEquals(target, new File(link.getTargetPath()));
        assertEquals(description, link.getDescription());

        // verify the shortcut file exists
        File shortcut = new File(link.getFileName());
        assertTrue(shortcut.exists());
        return shortcut;
    }

    /**
     * Determines if a key exists in the registry.
     *
     * @param handler the registry handler
     * @param key     the key to check
     * @return <tt>true</tt> if the key exists, otherwise <tt>false</tt>
     * @throws NativeLibException for any registry error
     */
    public static boolean registryKeyExists(RegistryDefaultHandler handler, String key) throws NativeLibException
    {
        RegistryHandler registry = handler.getInstance();
        assertNotNull(registry);
        registry.setRoot(RegistryHandler.HKEY_LOCAL_MACHINE);
        return registry.keyExist(key);
    }

    /**
     * Deletes an uninstallation registry key.
     *
     * @param handler the registry handler
     * @param key     the key to delete
     * @throws NativeLibException for any registry error
     */
    public static void registryDeleteUninstallKey(RegistryDefaultHandler handler, String key) throws NativeLibException
    {
        RegistryHandler registry = handler.getInstance();
        assertNotNull(registry);
        if (!key.matches(".*\\\\Uninstall\\\\.+")) {
            // don't want to delete too much
            throw new IllegalArgumentException("Invalid key for deletion: " + key);
        }
        registry.setRoot(RegistryHandler.HKEY_LOCAL_MACHINE);
        if (registry.keyExist(key))
        {
            registry.deleteKey(key);
        }
    }
}