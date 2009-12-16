package com.izforge.izpack.compiler.data;

/**
 * Data for compiler
 */
public class CompilerData {

    /**
     * The installer kind.
     */
    private String kind = "standard";

    /**
     * The xml install file
     */
    private String filename;

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

    private int comprLevel = -1;
    private String comprFormat = "default";
    /**
     * The IzPack version.
     */
    public final static String IZPACK_VERSION = "4.3.2";

    /**
     * The IzPack home directory.
     */
    public static String IZPACK_HOME = ".";

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

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
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


}
