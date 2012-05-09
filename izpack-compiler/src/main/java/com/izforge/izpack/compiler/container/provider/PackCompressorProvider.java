package com.izforge.izpack.compiler.container.provider;

import org.picocontainer.injectors.Provider;

import com.izforge.izpack.compiler.compressor.BZip2PackCompressor;
import com.izforge.izpack.compiler.compressor.DefaultPackCompressor;
import com.izforge.izpack.compiler.compressor.PackCompressor;
import com.izforge.izpack.compiler.compressor.RawPackCompressor;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.merge.MergeManager;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Anthonin Bonnefoy
 */
public class PackCompressorProvider implements Provider
{
    public PackCompressor provide(CompilerData compilerData, MergeManager mergeManager)
    {
        String format = compilerData.getComprFormat();
        if (format.equals("bzip2"))
        {
            return new BZip2PackCompressor(mergeManager);
        }
        else if (format.equals("raw"))
        {
            return new RawPackCompressor();
        }
        return new DefaultPackCompressor();
    }
}
