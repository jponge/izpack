/*
 * IzPack - Copyright 2001-2011 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/ http://izpack.codehaus.org/
 *
 * Copyright 2011 Tim Anderson
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.izforge.izpack.util;

import static com.izforge.izpack.util.Platform.Arch;
import static com.izforge.izpack.util.Platform.Name;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;


/**
 * Tests the {@link Platforms} class.
 *
 * @author Tim Anderson
 */
public class PlatformsTest extends AbstractPlatformTest
{

    /**
     * Tests the {@link Platforms#getPlatform()} method.
     */
    @Test
    public void testGetPlatform()
    {
        Platforms platforms = new Platforms();
        Platform platform = platforms.getPlatform();

        String osName = System.getProperty(OsVersionConstants.OSNAME);
        Name name = platforms.getName(osName);
        Arch arch = platforms.getArch(System.getProperty(OsVersionConstants.OSARCH));
        String version = System.getProperty(OsVersionConstants.OSVERSION);

        Platform match = platforms.findMatch(osName, name, arch, version);
        Platform expected = platforms.getPlatform(match, arch, version);

        checkPlatform(expected, platform);
    }

    /**
     * Tests the {@link Platforms#getPlatform(String, String)} method.
     */
    @Test
    public void testGetPlatformByNameArch()
    {
        Platforms platforms = new Platforms();
        Platform platform1 = platforms.getPlatform("Windows 7", OsVersionConstants.AMD64);
        checkPlatform(platform1, Name.WINDOWS, null, null, Arch.X64); // need the version to work out its windows 7.

        Platform platform2 = platforms.getPlatform("Mac OS X", OsVersionConstants.X86);
        checkPlatform(platform2, Name.MAC_OSX, null, null, Arch.X86);
    }

    /**
     * Verifies that Linux platforms are returned correctly, by simulating the responses of the getText() and exists()
     * methods.
     */
    @Test
    public void testGetLinuxPlatform()
    {
        checkLinuxPlatform(Name.DEBIAN_LINUX, null, null, true);

        checkLinuxPlatform(Name.FEDORA_LINUX, OsVersionConstants.FEDORA, null, false);

        checkLinuxPlatform(Name.MANDRAKE_LINUX, OsVersionConstants.MANDRAKE, null, false);

        checkLinuxPlatform(Name.MANDRIVA_LINUX, OsVersionConstants.MANDRIVA, null, false);

        checkLinuxPlatform(Name.RED_HAT_LINUX, OsVersionConstants.REDHAT, null, false);

        checkLinuxPlatform(Name.SUSE_LINUX, OsVersionConstants.SUSE, null, false);

        checkLinuxPlatform(Name.UBUNTU_LINUX, null, OsVersionConstants.UBUNTU, false);
    }

    /**
     * Tests the {@link Platforms#getPlatform(String, String, String)} method.
     */
    @Test
    public void testGetPlatformByNameArchVersion()
    {
        Platforms platforms = new Platforms();
        Platform windows7x64 = platforms.getPlatform("Windows 7", OsVersionConstants.AMD64,
                OsVersionConstants.WINDOWS_7_VERSION);
        assertEquals(Name.WINDOWS, windows7x64.getName());
        assertEquals("WINDOWS_7", windows7x64.getSymbolicName());
        assertEquals(Arch.X64, windows7x64.getArch());
        assertEquals("6.1", windows7x64.getVersion());

    }

    /**
     * Ensures all of the public static Platform instances are registered in {@link Platforms#PLATFORMS}.
     */
    @Test
    public void testPlatforms()
    {
        int expected = 0;
        for (Field field : Platforms.class.getFields())
        {
            int mod = field.getModifiers();
            if (Modifier.isStatic(mod) && Modifier.isPublic(mod) && field.getType().equals(Platform.class))
            {
                ++expected;
            }
        }
        assertEquals(expected, Platforms.PLATFORMS.length);
    }

    /**
     * Verifies that Linux platforms are returned correctly, by simulating the responses of the getText() and exists()
     * methods.
     *
     * @param expectedName    the expected name
     * @param fromRelease     if non-null simulate text from an /etc/*-release file
     * @param fromProcVersion if non-null, simulate text from /proc/version
     * @param debianExists    if <tt>true</tt>, simulate existence of /etc/debian_version
     */
    private void checkLinuxPlatform(Name expectedName, final String fromRelease, final String fromProcVersion,
                                    final boolean debianExists)
    {
        Platforms platforms = new Platforms()
        {

            @Override
            protected String getReleasePath()
            {
                return "";
            }

            @Override
            protected List<String> getText(String path)
            {
                if (OsVersionConstants.PROC_VERSION.equals(path))
                {
                    if (fromProcVersion != null)
                    {
                        return Arrays.asList(fromProcVersion);
                    }
                }
                else if (fromRelease != null)
                {
                    return Arrays.asList(fromRelease);
                }
                return null;
            }

            @Override
            protected boolean exists(String path)
            {
                return debianExists;
            }
        };

        String version = "some version";
        Platform platform = platforms.getPlatform(OsVersionConstants.LINUX, OsVersionConstants.X86, version);
        checkPlatform(platform, expectedName, null, version, Arch.X86);
    }

}
