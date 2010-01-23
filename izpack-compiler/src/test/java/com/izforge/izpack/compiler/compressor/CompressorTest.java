package com.izforge.izpack.compiler.compressor;

import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.provider.JarOutputStreamProvider;
import com.izforge.izpack.compiler.stream.JarOutputStream;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.tools.zip.ZipEntry;
import org.junit.Test;

import java.io.IOException;

/**
 * Test compressor stream
 *
 * @author Anthonin Bonnefoy
 */
public class CompressorTest {

    @Test
    public void testBzip2Compression() throws IOException, CompressorException {
        CompilerData data = new CompilerData("", "", "output.jar");
        data.setComprFormat("bzip2");
        data.setComprLevel(5);
        JarOutputStreamProvider jarOutputStreamProvider = new JarOutputStreamProvider();
        JarOutputStream jarOutputStream = jarOutputStreamProvider.provide(data);
        ZipEntry zipEntry = new ZipEntry("test");
        zipEntry.setMethod(java.util.zip.ZipEntry.STORED);
        zipEntry.setComment("bzip2");
        jarOutputStream.putNextEntry(zipEntry);
    }
}
