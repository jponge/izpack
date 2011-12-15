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

/**
 * Encapsulates details of the operating system platform.
 *
 * @author Tim Anderson
 */
public class Platform
{

    /**
     * Platform family name.
     */
    public enum Name
    {
        UNIX,
        LINUX(Name.UNIX),
        AIX(Name.UNIX),
        DEBIAN_LINUX(Name.LINUX),
        FEDORA_LINUX(Name.LINUX),
        FREEBSD(Name.UNIX),
        HP_UX(Name.UNIX),
        MAC,
        MAC_OSX(Name.UNIX),
        MANDRAKE_LINUX(Name.LINUX),
        MANDRIVA_LINUX(Name.LINUX), // formerly 'Mandrake'
        OS_2,
        RED_HAT_LINUX(Name.LINUX),
        SUNOS(Name.UNIX),
        SUSE_LINUX(Name.LINUX),
        UBUNTU_LINUX(Name.LINUX),
        WINDOWS,
        UNKNOWN;

        /**
         * Constructs a <tt>Name</tt> with no parent family.
         */
        Name()
        {
            this(null);
        }

        /**
         * Constructs a <tt>Name</tt> with a parent family.
         *
         * @param parent the parent family name. May be <tt>null</tt>
         */
        Name(Name parent)
        {
            this.parent = parent;
        }

        /**
         * Returns the parent family name.
         *
         * @return the parent family name. May be <tt>null</tt>
         */
        public Name getParent()
        {
            return parent;
        }

        /**
         * The parent platform family. May be <tt>null</tt>
         */
        private final Name parent;
    }

    /**
     * The operating system architecture.
     */
    public enum Arch
    {
        X86, X64, PPC, SPARC, UNKNOWN
    }

    /**
     * The platform family name.
     */
    private final Name name;

    /**
     * The symbolic name. May be <tt>null</tt>
     */
    private final String symbolicName;

    /**
     * The platform architecture.
     */
    private final Arch arch;

    /**
     * The OS version.
     */
    private final String version;


    /**
     * Constructs a <tt>Platform</tt> from the specified name.
     *
     * @param name the platform name
     */
    public Platform(Name name)
    {
        this(name, (String) null);
    }

    /**
     * Constructs a <tt>Platform</tt> from the specified name and version.
     *
     * @param name    the platform name
     * @param version the platform version. May be <tt>null</tt>
     */
    public Platform(Name name, String version)
    {
        this(name, null, version);
    }

    /**
     * Constructs a <tt>Platform</tt>.
     *
     * @param name         the platform name
     * @param symbolicName the symbolic name. May be <tt>null</tt>
     * @param version      the platform version. May be <tt>null</tt>
     * @throws IllegalArgumentException if <tt>symbolicName</tt> contains spaces or commas
     */
    public Platform(Name name, String symbolicName, String version)
    {
        this(name, symbolicName, version, null);
    }

    /**
     * Constructs a <tt>Platform</tt>.
     *
     * @param name the platform name
     * @param arch the platform architecture. May be <tt>null</tt>
     */
    public Platform(Name name, Arch arch)
    {
        this(name, null, arch);
    }

    /**
     * Constructs a <tt>Platform</tt>.
     *
     * @param name         the platform name
     * @param symbolicName the symbolic name. May be <tt>null</tt>
     * @param arch         the platform architecture. May be <tt>null</tt>
     * @throws IllegalArgumentException if <tt>symbolicName</tt> contains spaces or commas
     */
    public Platform(Name name, String symbolicName, Arch arch)
    {
        this(name, symbolicName, null, arch);
    }

    /**
     * Constructs a platform.
     *
     * @param name         the platform name
     * @param symbolicName the symbolic name. May be <tt>null</tt>
     * @param version      the platform version. May be <tt>null</tt>
     * @param arch         the platform architecture. May be <tt>null</tt>
     * @throws IllegalArgumentException if <tt>symbolicName</tt> contains spaces or commas
     */
    public Platform(Name name, String symbolicName, String version, Arch arch)
    {
        if (symbolicName != null && (symbolicName.indexOf(' ') > 0 || symbolicName.indexOf(',') > 0))
        {
            throw new IllegalArgumentException("Argument 'symbolicName' should not contain spaces or commas");
        }
        this.name = name;
        this.symbolicName = symbolicName;
        this.version = version;
        this.arch = (arch != null) ? arch : Arch.UNKNOWN;
    }

    /**
     * Returns the platform family name.
     *
     * @return the platform family name
     */
    public Name getName()
    {
        return name;
    }

    /**
     * Returns the symbolic name for the platform.
     * <p/>
     * This is not the OS name. It is an arbitrary name that may be used to help identify a platform.
     * E.g. windows_7 for name=WINDOWS,version=6.1.
     *
     * @return the symbolic name for the platform. May be <tt>null</tt>
     */
    public String getSymbolicName()
    {
        return symbolicName;
    }

    /**
     * Returns the operating system version.
     *
     * @return the operating system version. May be <tt>null</tt>
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * Returns the operating system architecture.
     *
     * @return the operating system architecture
     */
    public Arch getArch()
    {
        return arch;
    }

    /**
     * Determines if this platform is an instance of another.
     *
     * @param platform the platform to compare against
     * @return <tt>true</tt> if the platform is an instance of <tt>platform</tt>
     */
    public boolean isA(Platform platform)
    {
        boolean result = false;
        if (isA(platform.name) && hasSymbolicName(platform.symbolicName) && hasArch(platform.arch)
                && hasVersion(platform.version))
        {
            result = true;
        }
        return result;
    }

    /**
     * Determines if this platform is an instance of the platform family name.
     *
     * @param name the platform family name
     * @return <tt>true</tt> if the platform is an instance of <tt>name</tt>
     */
    public boolean isA(Name name)
    {
        boolean result = false;
        Name n = this.name;
        while (n != null)
        {
            if (n == name)
            {
                result = true;
                break;
            }
            n = n.parent;
        }
        return result;
    }

    /**
     * Determines if this platform is the specified architecture.
     *
     * @param arch the architecture
     * @return <tt>true</tt> if this platform is the specified architecture, otherwise <tt>false</tt>
     */
    public boolean isA(Arch arch)
    {
        return this.arch == arch;
    }

    /**
     * Determines if this platform equals another.
     *
     * @param other the other instance
     * @return <tt>true</tt> if the name, arch and version are identical, otherwise <tt>false</tt>
     */
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (other instanceof Platform)
        {
            Platform p = (Platform) other;
            if (name == p.name && arch == p.arch
                    && ((version == null && p.version == null) || (version != null && version.equals(p.version))))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a hash for the platform.
     *
     * @return a hash for the platform
     */
    public int hashCode()
    {
        int hash = name.hashCode() ^ arch.hashCode();
        if (version != null)
        {
            hash ^= version.hashCode();
        }
        return hash;
    }

    /**
     * Determines if the symbolic name matches another.
     *
     * @param other the other name. May be <tt>null</tt>
     * @return <tt>true</tt> if either <tt>other</tt> or <tt>symbolicName</tt> are null, or if they are equal
     */
    private boolean hasSymbolicName(String other)
    {
        return symbolicName == null || other == null || symbolicName.equals(other);
    }

    /**
     * Determines if the architecture matches another.
     *
     * @param other the other architecture
     * @return <tt>true</tt> if they are equal, or the other architecture is unknown
     */
    private boolean hasArch(Arch other)
    {
        return arch == other || other == Arch.UNKNOWN;
    }

    /**
     * Determines if the version name matches another.
     *
     * @param other the other version. May be <tt>null</tt>
     * @return <tt>true</tt> if <tt>other</tt> is null, or if they are equal
     */
    private boolean hasVersion(String other)
    {
        return (other == null) || (version != null && version.equals(other));
    }

}
