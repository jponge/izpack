package com.izforge.izpack.compiler.container.provider;

import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.stream.JarOutputStream;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.tools.zip.ZipEntry;
import org.picocontainer.injectors.Provider;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Anthonin Bonnefoy
 */
public class CompressedOutputStreamProvider implements Provider {

    public OutputStream provide(CompilerData compilerData, JarOutputStream jarOutputStream) throws CompressorException, IOException {
        OutputStream outputStream = jarOutputStream;
        String comprFormat = compilerData.getComprFormat();
        if (comprFormat.equals("bzip2")) {
            ZipEntry entry = new ZipEntry("bzip2");
            entry.setMethod(ZipEntry.STORED);
            entry.setComment("bzip2");
            // We must set the entry before we get the compressed stream
            // because some writes initialize data (e.g. bzip2).
            jarOutputStream.putNextEntry(entry);
            jarOutputStream.flush(); // flush before we start counting

            outputStream = new CompressorStreamFactory().createCompressorOutputStream("bzip2", jarOutputStream);
            jarOutputStream.closeEntry();
        }
        return outputStream;
    }
}
