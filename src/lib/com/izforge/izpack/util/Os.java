package com.izforge.izpack.util;

/**
 * Performs matching of OS specified on construction time 
 * against execution platform.
 *
 * @author Olexij Tkatchenko <ot@parcs.de>
 */
public class Os implements java.io.Serializable
{
    /** The OS family */
   private String family;
    /** OS name from java system properties */
    private String name;
    /** OS version from java system properties */
    private String version;
    /** OS architecture from java system properties */
    private String arch;

    /**
     * Constructs a new instance. 
     * Please remember, MacOSX belongs to Unix family.
     */
    public Os(String family, String name, String version, String arch) {
    this.family=(family != null) ? family.toLowerCase() : null;
    this.name=(name != null) ? name.toLowerCase() : null;
    this.version=(version != null) ? version.toLowerCase() : null;
    this.arch=(arch != null) ? arch.toLowerCase() : null;
    }


    /**
     * Matches OS specification in this class against current system properties.
     */
    public boolean matchCurrentSystem() {
    boolean match = true;
    String osName = System.getProperty("os.name").toLowerCase();

    if((arch != null) && (arch.length() != 0)) {
        match = System.getProperty("os.arch").toLowerCase().equals(arch);
    }
    if(match && (version != null) && (version.length() != 0)) {
        match = System.getProperty("os.version").toLowerCase().equals(version);
    }
    if(match && (name != null) && (name.length() != 0)) {
        match = osName.equals(name);
    }

    if(match && (family != null)) {
            if (family.equals("windows")) {
                match = (osName.indexOf("windows") > -1);
            } else if (family.equals("mac")) {
                match = ((osName.indexOf("mac") > -1)
             && !(osName.endsWith("x")));
            } else if (family.equals("unix")) {
        String pathSep = System.getProperty("path.separator");
                match = (pathSep.equals(":") && (!osName.startsWith("mac") ||
                         osName.endsWith("x")));
            }
    }

    return match && ((family != null) || 
             (name != null) || 
             (version != null) || 
             (arch != null));
    }

    public void setFamily(String f) {
    family = f.toLowerCase();
    }
    public String getFamily() {
    return family;
    }
    public void setName(String n) {
    name = n.toLowerCase();
    }
    public String getName() {
    return name;
    }
    public void setVersion(String v) {
    version = v.toLowerCase();
    }
    public String getVersion() {
    return version;
    }
    public void setArch(String a) {
    arch = a.toLowerCase();
    }
    public String getArch() {
    return arch;
    }


}
