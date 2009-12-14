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

    private int compr_level;
    private String compr_format = "default";
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

    public String getCompr_format() {
        return compr_format;
    }

    public void setCompr_format(String compr_format) {
        this.compr_format = compr_format;
    }

    public int getCompr_level() {
        return compr_level;
    }

    public void setCompr_level(int compr_level) {
        this.compr_level = compr_level;
    }
}
