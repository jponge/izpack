package com.izforge.izpack.compiler.provider;

import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.stream.JarOutputStream;
import org.apache.tools.bzip2.CBZip2OutputStream;
import org.picocontainer.injectors.Provider;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;

/**
 * OutputStream Provider.
 *
 * @author Anthonin Bonnefoy
 */
public class OutputStreamProvider implements Provider {

    public JarOutputStream provide(CompilerData compilerData) throws IOException {
        JarOutputStream jarOutputStream;
        OutputStream outputStream = new FileOutputStream(compilerData.getOutput());

        String comprFormat = compilerData.getComprFormat();
        if (comprFormat.equals("bzip2")) {
            outputStream = new CBZip2OutputStream(outputStream);
        }
        jarOutputStream = new JarOutputStream(outputStream);
        jarOutputStream.setLevel(Deflater.BEST_COMPRESSION);
        jarOutputStream.setPreventClose(true); // Needed at using FilterOutputStreams which calls close

        return jarOutputStream;
    }
}
