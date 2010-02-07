package com.izforge.izpack.installer.data;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.rules.RulesEngineImpl;
import com.izforge.izpack.data.CustomData;
import com.izforge.izpack.data.ExecutableFile;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.IoHelper;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

public class UninstallDataWriter {
    private static final String UNINSTALLER_CONDITION = "UNINSTALLER_CONDITION";
    private static final String LOGFILE_PATH = "InstallerFrame.logfilePath";
    private VariableSubstitutor variableSubstitutor;

    public UninstallDataWriter(VariableSubstitutor variableSubstitutor) {
        this.variableSubstitutor = variableSubstitutor;
    }

    /**
     * Write uninstall data.
     *
     * @param installdata The install data to use.
     * @return true if the infos were successfuly written, false otherwise.
     */
    public boolean write(AutomatedInstallData installdata) {


        BufferedWriter extLogWriter = getExternLogFile(installdata);
        try {
            String condition = installdata.getVariable(UNINSTALLER_CONDITION);
            if (condition != null) {
                if (!RulesEngineImpl.getCondition(condition).isTrue()) {
                    // condition for creating the uninstaller is not fulfilled.
                    return false;
                }
            }
            // We get the data
            UninstallData udata = UninstallData.getInstance();
            List<String> files = udata.getUninstalableFilesList();
            ZipOutputStream outJar = installdata.getUninstallOutJar();

            if (outJar == null) {
                return true; // it is allowed not to have an installer
            }

            System.out.println("[ Writing the uninstaller data ... ]");

            writeFilesLog(installdata, extLogWriter, files, outJar);

            writeUninstallerJarFileLog(udata, outJar);

            writeExecutables(udata, outJar);

            writeAdditionalUninstallData(udata, outJar);

            writeScriptFiles(udata, outJar);

            // Cleanup
            outJar.flush();
            outJar.close();
            return true;
        }
        catch (Exception err) {
            err.printStackTrace();
            return false;
        }
    }

    // Show whether a separated logfile should be also written or not.

    private BufferedWriter getExternLogFile(AutomatedInstallData installdata) {
        String logfile = installdata.getVariable(LOGFILE_PATH);
        BufferedWriter extLogWriter = null;
        if (logfile != null) {
            if (logfile.toLowerCase().startsWith("default")) {
                logfile = installdata.getInfo().getUninstallerPath() + "/install.log";
            }
            logfile = IoHelper.translatePath(logfile, variableSubstitutor);
            File outFile = new File(logfile);
            if (!outFile.getParentFile().exists()) {
                outFile.getParentFile().mkdirs();
            }
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(outFile);
            }
            catch (FileNotFoundException e) {
                Debug.trace("Cannot create logfile!");
                Debug.error(e);
            }
            if (out != null) {
                extLogWriter = new BufferedWriter(new OutputStreamWriter(out));
            }
        }

        return extLogWriter;
    }

    // We write the files log

    private void writeFilesLog(AutomatedInstallData installdata, BufferedWriter extLogWriter, List<String> files, ZipOutputStream outJar) throws IOException {
        outJar.putNextEntry(new ZipEntry("install.log"));
        BufferedWriter logWriter = new BufferedWriter(new OutputStreamWriter(outJar));
        logWriter.write(installdata.getInstallPath());
        logWriter.newLine();
        Iterator<String> iter = files.iterator();
        if (extLogWriter != null) { // Write intern (in uninstaller.jar) and extern log file.

            while (iter.hasNext()) {
                String txt = iter.next();
                logWriter.write(txt);
                extLogWriter.write(txt);
                if (iter.hasNext()) {
                    logWriter.newLine();
                    extLogWriter.newLine();
                }
            }
            logWriter.flush();
            extLogWriter.flush();
            extLogWriter.close();
        } else {
            while (iter.hasNext()) {
                String txt = iter.next();
                logWriter.write(txt);
                if (iter.hasNext()) {
                    logWriter.newLine();
                }
            }
            logWriter.flush();
        }
        outJar.closeEntry();
    }


    // Write out executables to execute on uninstall

    private void writeExecutables(UninstallData udata, ZipOutputStream outJar) throws IOException {
        outJar.putNextEntry(new ZipEntry("executables"));
        ObjectOutputStream execStream = new ObjectOutputStream(outJar);
        execStream.writeInt(udata.getExecutablesList().size());
        for (ExecutableFile file : udata.getExecutablesList()) {
            execStream.writeObject(file);
        }
        execStream.flush();
        outJar.closeEntry();
    }

    // We write the uninstaller jar file log

    private void writeUninstallerJarFileLog(UninstallData udata, ZipOutputStream outJar) throws IOException {
        BufferedWriter logWriter;
        outJar.putNextEntry(new ZipEntry("jarlocation.log"));
        logWriter = new BufferedWriter(new OutputStreamWriter(outJar));
        logWriter.write(udata.getUninstallerJarFilename());
        logWriter.newLine();
        logWriter.write(udata.getUninstallerPath());
        logWriter.flush();
        outJar.closeEntry();
    }

    // write the script files, which will
    // perform several complement and unindependend uninstall actions

    private void writeScriptFiles(UninstallData udata, ZipOutputStream outJar) throws IOException {

        ArrayList<String> unInstallScripts = udata.getUninstallScripts();
        ObjectOutputStream rootStream;
        int idx = 0;
        for (String unInstallScript : unInstallScripts) {
            outJar.putNextEntry(new ZipEntry(UninstallData.ROOTSCRIPT + Integer.toString(idx)));
            rootStream = new ObjectOutputStream(outJar);
            rootStream.writeUTF(unInstallScript);
            rootStream.flush();
            outJar.closeEntry();
            idx++;
        }
    }

    // Write out additional uninstall data
    // Do not "kill" the installation if there is a problem
    // with custom uninstall data. Therefore log it to Debug,
    // but do not throw.

    private void writeAdditionalUninstallData(UninstallData udata, ZipOutputStream outJar) throws IOException {
        Map<String, Object> additionalData = udata.getAdditionalData();
        if (additionalData != null && !additionalData.isEmpty()) {
            Iterator<String> keys = additionalData.keySet().iterator();
            Set<String> exist = new HashSet<String>();
            while (keys != null && keys.hasNext()) {
                String key = keys.next();
                Object contents = additionalData.get(key);
                if ("__uninstallLibs__".equals(key)) {
                    Iterator nativeLibIter = ((List) contents).iterator();
                    while (nativeLibIter != null && nativeLibIter.hasNext()) {
                        String nativeLibName = (String) ((List) nativeLibIter.next()).get(0);
                        byte[] buffer = new byte[5120];
                        long bytesCopied = 0;
                        int bytesInBuffer;
                        outJar.putNextEntry(new ZipEntry("native/" + nativeLibName));
                        InputStream in = getClass().getResourceAsStream(
                                "/native/" + nativeLibName);
                        while ((bytesInBuffer = in.read(buffer)) != -1) {
                            outJar.write(buffer, 0, bytesInBuffer);
                            bytesCopied += bytesInBuffer;
                        }
                        outJar.closeEntry();
                    }
                } else if ("uninstallerListeners".equals(key) || "uninstallerJars".equals(key)) {
                    // It is a ArrayList of ArrayLists which contains the full
                    // package paths of all needed class files.
                    // First we create a new ArrayList which contains only
                    // the full paths for the uninstall listener self; thats
                    // the first entry of each sub ArrayList.
                    ArrayList<String> subContents = new ArrayList<String>();

                    // Secound put the class into uninstaller.jar
                    for (Object o : ((List) contents)) {
                        byte[] buffer = new byte[5120];
                        long bytesCopied = 0;
                        int bytesInBuffer;
                        CustomData customData = (CustomData) o;
                        // First element of the list contains the listener
                        // class path;
                        // remind it for later.
                        if (customData.listenerName != null) {
                            subContents.add(customData.listenerName);
                        }
                        for (String content : customData.contents) {
                            if (exist.contains(content)) {
                                continue;
                            }
                            exist.add(content);
                            try {
                                outJar.putNextEntry(new ZipEntry(content));
                            }
                            catch (ZipException ze) { // Ignore, or ignore not ?? May be it is a
                                // exception because
                                // a doubled entry was tried, then we should
                                // ignore ...
                                Debug.trace("ZipException in writing custom data: "
                                        + ze.getMessage());
                                continue;
                            }
                            InputStream in = getClass().getResourceAsStream("/" + content);
                            if (in != null) {
                                while ((bytesInBuffer = in.read(buffer)) != -1) {
                                    outJar.write(buffer, 0, bytesInBuffer);
                                    bytesCopied += bytesInBuffer;
                                }
                            } else {
                                Debug.trace("custom data not found: " + content);
                            }
                            outJar.closeEntry();

                        }
                    }
                    // Third we write the list into the
                    // uninstaller.jar
                    outJar.putNextEntry(new ZipEntry(key));
                    ObjectOutputStream objOut = new ObjectOutputStream(outJar);
                    objOut.writeObject(subContents);
                    objOut.flush();
                    outJar.closeEntry();

                } else {
                    outJar.putNextEntry(new ZipEntry(key));
                    if (contents instanceof ByteArrayOutputStream) {
                        ((ByteArrayOutputStream) contents).writeTo(outJar);
                    } else {
                        ObjectOutputStream objOut = new ObjectOutputStream(outJar);
                        objOut.writeObject(contents);
                        objOut.flush();
                    }
                    outJar.closeEntry();
                }
            }
        }

    }
}
