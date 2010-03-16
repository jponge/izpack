package com.izforge.izpack.compiler.data;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Data for compiler
 */
public class CompilerData {

    /**
     * The IzPack home directory.
     */
    public static String IZPACK_HOME = ".";
    /**
     * The compiler version.
     */
    public final static String VERSION = "1.0";

    /**
     * Standard installer.
     */
    public final static String STANDARD = "standard";
    /**
     * Web installer.
     */
    public final static String WEB = "web";


    private String comprFormat = "default";

    /**
     * The installer kind.
     */
    private String kind = STANDARD;

    /**
     * The xml install file
     */
    private String installFile;

    /**
     * The xml install configuration text
     */
    private String installText;

    /**
     * The base directory.
     */
    protected String basedir;

    /**
     * The output jar filename.
     */
    private String output;

    /**
     * Compression level
     */
    private int comprLevel = -1;

    /**
     * The IzPack version.
     */
    public final static String IZPACK_VERSION = ResourceBundle.getBundle("version").getString("izpack.version");
    
    private final static String IZ_TEST_FILE = "ShellLink.dll";

    private final static String IZ_TEST_SUBDIR = "bin" + File.separator + "native" + File.separator + "izpack";

    private CompilerData() {
        // We get the IzPack home directory
        String izHome = System.getProperty("izpack.home");
        if (izHome != null) {
            IZPACK_HOME = izHome;
        } else {
            izHome = System.getenv("IZPACK_HOME");
            if (izHome != null) {
                IZPACK_HOME = izHome;
            }
        }
    }

    public CompilerData(String installFile, String basedir, String output) {
        this();
        this.installFile = installFile;
        this.basedir = basedir;
        this.output = output;
    }

    public CompilerData(String comprFormat, String kind, String installFile, String installText, String basedir, String output, int comprLevel) {
        this();
        this.comprFormat = comprFormat;
        this.kind = kind;
        this.installFile = installFile;
        this.installText = installText;
        this.basedir = basedir;
        this.output = output;
        this.comprLevel = comprLevel;
    }

    /**
     * Set the IzPack home directory
     *
     * @param izHome - the izpack home directory
     */
    public static void setIzpackHome(String izHome) {
        IZPACK_HOME = izHome;
    }

    /**
     * Access the installation kind.
     *
     * @return the installation kind.
     */
    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getInstallFile() {
        return installFile;
    }

    public void setInstallFile(String installFile) {
        this.installFile = installFile;
    }

    public String getInstallText() {
        return installText;
    }

    public void setInstallText(String installText) {
        this.installText = installText;
    }

    public String getBasedir() {
        return basedir;
    }

    public void setBasedir(String basedir) {
        this.basedir = basedir;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getComprFormat() {
        return comprFormat;
    }

    public void setComprFormat(String comprFormat) {
        this.comprFormat = comprFormat;
    }

    public int getComprLevel() {
        return comprLevel;
    }

    public void setComprLevel(int comprLevel) {
        this.comprLevel = comprLevel;
    }

    /**
     * Try to resolve IzPack home from IZPACK_HOME value
     */
    public void resolveIzpackHome() {
        IZPACK_HOME = resolveIzPackHome(IZPACK_HOME);
    }

    private static String resolveIzPackHome(String home) {
        File test = new File(home, IZ_TEST_SUBDIR + File.separator + IZ_TEST_FILE);
        if (test.exists()) {
            return (home);
        }
        // Try to resolve the path using compiler.jar which also should be under
        // IZPACK_HOME.
        String self = CompilerData.class.getName();
        self = self.replace('.', '/');
        self = "/" + self + ".class";
        URL url = Compiler.class.getResource(self);
        String np = url.getFile();
        int start = np.indexOf(self);
        np = np.substring(0, start);
        if (np.endsWith("!")) { // Where shut IZPACK_HOME at the standalone-compiler be??
            // No idea.
            if (np.endsWith("standalone-compiler.jar!")
                    || np.endsWith("standalone-compiler-4.0.0.jar!")
                    || np.matches("standalone-compiler-[\\d\\.]+.jar!")) {
                return (".");
            }
            np = np.substring(0, np.length() - 1);
        }
        File root;
        if (URI.create(np).isAbsolute()) {
            root = new File(URI.create(np));
        } else {
            root = new File(np);
        }
        while (true) {
            if (root == null) {
                throw new IllegalArgumentException(
                        "No valid IzPack home directory found");
            }
            test = new File(root, IZ_TEST_SUBDIR + File.separator + IZ_TEST_FILE);
            if (test.exists()) {
                return (root.getAbsolutePath());
            }
            root = root.getParentFile();
        }
    }
}
