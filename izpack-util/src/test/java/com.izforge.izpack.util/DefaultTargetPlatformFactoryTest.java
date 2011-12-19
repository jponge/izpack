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

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Tests the {@link DefaultTargetPlatformFactory} class.
 *
 * @author Tim Anderson
 */
public class DefaultTargetPlatformFactoryTest
{

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
        Platform windowsX86 = new Platform(Platform.Name.WINDOWS, Platform.Arch.X86);
        Platform windowsX64 = new Platform(Platform.Name.WINDOWS, Platform.Arch.X64);
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

        Platform win7x64 = new Platform(Platforms.WINDOWS_7, Platform.Arch.X64);
        assertEquals(Win7X64.class, factory.create(A.class, win7x64).getClass());

        // no specific implementation registered for x86, so should pick up windows_7 impl
        Platform win7x32 = new Platform(Platforms.WINDOWS_7, Platform.Arch.X86);
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
