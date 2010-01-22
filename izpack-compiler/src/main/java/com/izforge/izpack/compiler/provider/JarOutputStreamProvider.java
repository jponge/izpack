package com.izforge.izpack.compiler.provider;

import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.stream.JarOutputStream;
import org.picocontainer.injectors.Provider;

import java.io.File;
import java.io.IOException;
import java.util.zip.Deflater;

/**
 * OutputStream Provider.
 *
 * @author Anthonin Bonnefoy
 */
public class JarOutputStreamProvider implements Provider {

    public JarOutputStream provide(CompilerData compilerData) throws IOException {
        JarOutputStream jarOutputStream;
        File file = new File(compilerData.getOutput());
        if (file.exists()) {
            file.delete();
        }
        jarOutputStream = new JarOutputStream(file);
        int level = compilerData.getComprLevel();
        jarOutputStream.setLevel(Deflater.BEST_COMPRESSION);
        if (level >= 0 && level < 10) {
            jarOutputStream.setLevel(level);
        }
        jarOutputStream.setPreventClose(true); // Needed at using FilterOutputStreams which calls close
        return jarOutputStream;
    }
}
