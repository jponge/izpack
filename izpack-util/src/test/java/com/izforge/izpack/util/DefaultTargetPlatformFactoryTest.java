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

import org.junit.Test;

import java.net.URL;

import static com.izforge.izpack.util.Platform.Arch;
import static com.izforge.izpack.util.Platform.Name;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests the {@link DefaultTargetPlatformFactory} class.
 *
 * @author Tim Anderson
 */
public class DefaultTargetPlatformFactoryTest
{

    /**
     * Verifies that the <tt>TargetPlatformFactory.properties</tt> file has been loaded successfully.
     */
    @Test
    public void testParser()
    {
        DefaultTargetPlatformFactory factory = new DefaultTargetPlatformFactory() {
            @Override
            protected Parser createParser(Platforms platforms, URL url)
            {
                return new Parser(platforms, url) {
                    @Override
                    protected void warning(String message)
                    {
                        throw new IllegalStateException("Unexpected parser warning: " + message);
                    }
                };
            }
        };
        DefaultTargetPlatformFactory.Implementations implementations = factory.getImplementations(A.class);
        assertNotNull(implementations);

        assertEquals(DefaultA.class.getName(), implementations.getDefault());

        assertEquals(8, implementations.getPlatforms().size());
        assertEquals(WinA.class.getName(), implementations.getImplementation(Platforms.WINDOWS));
        assertEquals(WinX86.class.getName(), implementations.getImplementation(new Platform(Name.WINDOWS, Arch.X86)));
        assertEquals(WinX64.class.getName(), implementations.getImplementation(new Platform(Name.WINDOWS, Arch.X64)));
        assertEquals(Win7.class.getName(), implementations.getImplementation(Platforms.WINDOWS_7));
        assertEquals(Win7X64.class.getName(), implementations.getImplementation(
                new Platform(Name.WINDOWS, "WINDOWS_7", OsVersionConstants.WINDOWS_7_VERSION, Arch.X64)));
        assertEquals(DebianA.class.getName(), implementations.getImplementation(Platforms.DEBIAN_LINUX));
        assertEquals(LinuxA.class.getName(), implementations.getImplementation(Platforms.LINUX));
        assertEquals(UnixA.class.getName(), implementations.getImplementation(Platforms.UNIX));
    }

    /**
     * Tests the {@link DefaultTargetPlatformFactory#create(Class, Platform)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testCreate() throws Exception
    {
        TargetPlatformFactory factory = new DefaultTargetPlatformFactory();

        assertEquals(WinA.class, factory.create(A.class, Platforms.WINDOWS).getClass());

        // all windows versions that don't specify an architecture should use WinA
        assertEquals(WinA.class, factory.create(A.class, Platforms.WINDOWS_2003).getClass());
        assertEquals(WinA.class, factory.create(A.class, Platforms.WINDOWS_XP).getClass());
        assertEquals(WinA.class, factory.create(A.class, Platforms.WINDOWS_VISTA).getClass());

        // check windows platforms that specify an architecture
        Platform windowsX86 = new Platform(Name.WINDOWS, Arch.X86);
        Platform windowsX64 = new Platform(Name.WINDOWS, Arch.X64);
        assertEquals(WinX86.class, factory.create(A.class, windowsX86).getClass());
        assertEquals(WinX64.class, factory.create(A.class, windowsX64).getClass());

        // unix implementations
        assertEquals(UnixA.class, factory.create(A.class, Platforms.UNIX).getClass());
        assertEquals(UnixA.class, factory.create(A.class, Platforms.SUNOS_SPARC).getClass());
        assertEquals(UnixA.class, factory.create(A.class, Platforms.SUNOS_X86).getClass());
        assertEquals(UnixA.class, factory.create(A.class, Platforms.MAC_OSX).getClass());

        // linux implementations
        assertEquals(LinuxA.class, factory.create(A.class, Platforms.LINUX).getClass());
        assertEquals(LinuxA.class, factory.create(A.class, Platforms.UBUNTU_LINUX).getClass());

        // specific linux impl
        assertEquals(DebianA.class, factory.create(A.class, Platforms.DEBIAN_LINUX).getClass());

        // default impl
        assertEquals(DefaultA.class, factory.create(A.class, Platforms.OS_2).getClass());

        // check implementations registered via symbolic name
        assertEquals(Win7.class, factory.create(A.class, Platforms.WINDOWS_7).getClass());

        Platform win7x64 = new Platform(Platforms.WINDOWS_7, Arch.X64);
        assertEquals(Win7X64.class, factory.create(A.class, win7x64).getClass());

        // no specific implementation registered for x86, so should pick up windows_7 impl
        Platform win7x32 = new Platform(Platforms.WINDOWS_7, Arch.X86);
        assertEquals(Win7.class, factory.create(A.class, win7x32).getClass());
    }


    /**
     * Test classes.
     */
    public static interface A
    {
    }

    public static class WinA implements A
    {
    }

    public static class Win7 extends WinA
    {
    }

    public static class WinX86 extends WinA
    {
    }

    public static class WinX64 extends WinA
    {
    }

    public static class Win7X64 extends WinX64
    {
    }

    public static class UnixA implements A
    {
    }

    public static class LinuxA extends UnixA
    {
    }

    public static class DebianA extends LinuxA
    {
    }

    public static class DefaultA implements A
    {
    }

}
