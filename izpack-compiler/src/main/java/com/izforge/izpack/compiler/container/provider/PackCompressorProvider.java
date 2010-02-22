package com.izforge.izpack.compiler.container.provider;

import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.compiler.compressor.BZip2PackCompressor;
import com.izforge.izpack.compiler.compressor.DefaultPackCompressor;
import com.izforge.izpack.compiler.compressor.PackCompressor;
import com.izforge.izpack.compiler.compressor.RawPackCompressor;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.merge.MergeManager;
import org.picocontainer.injectors.Provider;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Anthonin Bonnefoy
 */
public class PackCompressorProvider implements Provider
{
    public PackCompressor provide(CompilerData compilerData, MergeManager mergeManager, VariableSubstitutor variableSubstitutor)
    {
        String format = compilerData.getComprFormat();
        if (format.equals("bzip2"))
        {
            return new BZip2PackCompressor(variableSubstitutor, mergeManager);
        }
        else if (format.equals("raw"))
        {
            return new RawPackCompressor(variableSubstitutor);
        }
        return new DefaultPackCompressor(variableSubstitutor);
    }
}
