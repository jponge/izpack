/*
 * $Id$
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2005 Klaus Bartz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.izforge.izpack.compiler.compressor;

import com.izforge.izpack.util.substitutor.VariableSubstitutor;

import java.io.OutputStream;


/**
 * IzPack will be able to support different compression methods for the
 * packs included in the installation jar file.
 * This abstract class implements the interface PackCompressor for
 * the common needed methods.
 *
 * @author Klaus Bartz
 */

public abstract class PackCompressorBase implements PackCompressor {

    protected String[] formatNames = null;
    protected String decoderMapper = null;
    protected String encoderClassName = null;

    protected Class[] paramsClasses = null;

    private int level = -1;

    protected VariableSubstitutor variableSubstitutor;

    /**
     * @param variableSubstitutor
     */
    public PackCompressorBase(VariableSubstitutor variableSubstitutor) {
        super();
        this.variableSubstitutor = variableSubstitutor;
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.compressor.PackCompressor#getEncoderClassName()
     */

    public String getEncoderClassName() {
        return (encoderClassName);
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.compressor.PackCompressor#useStandardCompression()
     */

    public boolean useStandardCompression() {
        return (false);
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.compressor.PackCompressor#getCompressionFormatSymbols()
     */

    public String[] getCompressionFormatSymbols() {
        return formatNames;
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.compressor.PackCompressor#getDecoderMapperName()
     */

    public String getDecoderMapperName() {
        return (decoderMapper);
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.compressor.PackCompressor#setCompressionLevel(int)
     */

    public void setCompressionLevel(int level) {
        this.level = level;
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.compressor.PackCompressor#getCompressionLevel()
     */

    public int getCompressionLevel() {
        return (level);
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.compressor.PackCompressor#needsBufferedOutputStream()
     */

    public boolean needsBufferedOutputStream() {
        return (true);
    }

    /**
     * Returns a newly created instance of the output stream which should be
     * used by this pack compressor. This method do not declare the
     * return value as FilterOutputStream although there must be an constructor
     * with a slave output stream as argument. This is done in this way because
     * some encoding streams from third party are only implemented as
     * "normal" output stream.
     *
     * @param slave output stream to be used as slave
     * @return a newly created instance of the output stream which should be
     *         used by this pack compressor
     * @throws Exception
     */
//    protected OutputStream getOutputInstance(OutputStream slave)
//            throws Exception {
//        if (needsBufferedOutputStream()) {
//            slave = new BufferedOutputStream(slave);
//        }
//        Object[] params = resolveConstructorParams(slave);
//        if (constructor == null) {
//            loadClass(getEncoderClassName());
//        }
//        if (constructor == null) {
//            return (null);
//        }
//        Object instance;
//        instance = constructor.newInstance(params);
//        if (!OutputStream.class.isInstance(instance)) {
//            compiler.parseError("'" + getEncoderClassName() + "' must be derived from "
//                    + OutputStream.class.toString());
//        }
//        return ((OutputStream) instance);
//    }

    /**
     * This method will be used to support different constructor signatures.
     * The default is
     * <pre>XXXOutputStream( OutputStream slave )</pre>
     * if level is -1 or
     * <pre>XXXOutputStream( OutputStream slave, int level )</pre>
     * if level is other than -1.<br>
     * If the signature of the used output stream will be other, overload
     * this method in the derived pack compressor class.
     *
     * @param slave output stream to be used as slave
     * @return the constructor params as Object [] to be used as construction
     *         of the constructor via reflection
     * @throws Exception
     */
    protected Object[] resolveConstructorParams(OutputStream slave) throws Exception {
        if (level == -1) {
            paramsClasses = new Class[1];
            paramsClasses[0] = Class.forName("java.io.OutputStream");
            Object[] params = {slave};
            return (params);
        }
        paramsClasses = new Class[2];
        paramsClasses[0] = Class.forName("java.io.OutputStream");
        paramsClasses[1] = java.lang.Integer.TYPE;
        Object[] params = {slave, level};
        return (params);
    }

}
