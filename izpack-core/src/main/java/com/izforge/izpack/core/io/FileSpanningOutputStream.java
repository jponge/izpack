/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/ http://izpack.codehaus.org/
 *
 * Copyright 2007 Dennis Reil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.izforge.izpack.core.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

/**
 * An <tt>OutputStream</tt> which transparently spans over multiple volumes. The size of the volumes and an
 * additional space for the first volume can be specified.
 *
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 * @author Tim Anderson
 */
public class FileSpanningOutputStream extends OutputStream
{

    /**
     * One kilobyte.
     */
    public static final long KB = 1000;

    /**
     * One megabyte.
     */
    public static final long MB = 1000 * KB;

    /**
     * The default size of a volume.
     */
    public static final long DEFAULT_VOLUME_SIZE = 650 * MB;

    /**
     * The no. of bytes allocated to the magic number written at the start of each volume.
     */
    protected static final int MAGIC_NUMBER_LENGTH = 10;

    /**
     * The spanning output stream.
     */
    private SpanningOutputStream spanningOutputStream;

    /**
     * The stream that .
     */
    private GZIPOutputStream gzipOutputStream;

    /**
     * The current offset in the (uncompressed) output stream.
     */
    private long filePointer;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(FileSpanningOutputStream.class.getName());


    /**
     * /**
     * Creates a new spanning output stream with the specified volume path and a maximum volume size.
     *
     * @param volumePath    the path to the first volume
     * @param maxVolumeSize the maximum volume size
     * @throws IOException for any I/O error
     */
    public FileSpanningOutputStream(String volumePath, long maxVolumeSize) throws IOException
    {
        this(new File(volumePath), maxVolumeSize);
    }

    /**
     * Creates a new spanning output stream with specified initial volume and a maximum volume size.
     *
     * @param volume        the first volume
     * @param maxVolumeSize the maximum volume size
     * @throws IOException for any I/O error
     */
    public FileSpanningOutputStream(File volume, long maxVolumeSize) throws IOException
    {
        spanningOutputStream = new SpanningOutputStream(volume, maxVolumeSize);
        gzipOutputStream = new GZIPOutputStream(spanningOutputStream);
    }

    /**
     * @see java.io.OutputStream#close()
     */
    @Override
    public void close() throws IOException
    {
        flush();
        gzipOutputStream.close();
    }

    /**
     * @see java.io.OutputStream#write(byte[], int, int)
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
        gzipOutputStream.write(b, off, len);
        // increase filePointer by written bytes
        filePointer += len;
    }

    /**
     * (non Javadoc)
     *
     * @see java.io.OutputStream#write(byte[])
     */
    @Override
    public void write(byte[] b) throws IOException
    {
        write(b, 0, b.length);
    }

    /**
     * (non Javadoc)
     *
     * @see java.io.OutputStream#write(int)
     */
    @Override
    public void write(int b) throws IOException
    {
        gzipOutputStream.write(b);
        // increase filePointer by written byte
        filePointer++;
    }

    /**
     * (non Javadoc)
     *
     * @see java.io.OutputStream#flush()
     */
    @Override
    public void flush() throws IOException
    {
        gzipOutputStream.finish();
    }

    /**
     * Returns the number of volumes spanned.
     *
     * @return the number of volumes
     */
    public int getVolumes()
    {
        return spanningOutputStream.getVolumes();
    }

    /**
     * Sets the size of the space to leave free on the first volume.
     * <p/>
     * This may be used to allocate space for additional files on CD beside the pack files.
     */
    public void setFirstVolumeFreeSpaceSize(long size)
    {
        spanningOutputStream.setFirstVolumeFreeSpaceSize(size);
    }

    /**
     * Returns the current offset in the (uncompressed) output stream.
     *
     * @return the current offset
     */
    public long getFilePointer()
    {
        return filePointer;
    }

    /**
     * Helper to format the volume magic number.
     *
     * @param magic the magic number
     * @return the formatted magic number
     */
    static String formatMagic(byte[] magic)
    {
        StringBuilder builder = new StringBuilder();
        for (byte b : magic)
        {
            if (builder.length() != 0)
            {
                builder.append(' ');
            }
            builder.append(Integer.toHexString((int) b & 0xFF));
        }
        return builder.toString();
    }

    /**
     * The <tt>SpanningOutputStream</tt> sits between the <tt>GZIPOutputStream</tt> and the volume
     * <tt>FileOutputStream</tt>. When a volume fills, it is closed and a new one opened and written to.
     */
    private static class SpanningOutputStream extends ByteCountingOutputStream
    {
        /**
         * The maximum size of each volume.
         */
        private final long maxVolumeSize;

        /**
         * The index of the current volume.
         */
        private int index;

        /**
         * The base path to each volume.
         */
        private String basePath;

        /**
         * The magic number written at the start of each volume.
         */
        private byte[] magic;

        /**
         * The space to leave on the first volume.
         */
        private long firstVolumeFreeSpaceSize = 0;


        /**
         * Constructs a <tt>SpanningOutputStream</tt>.
         *
         * @param volume        the first volume
         * @param maxVolumeSize the maximum volume size
         * @throws IOException for any I/O error
         */
        public SpanningOutputStream(File volume, long maxVolumeSize) throws IOException
        {
            super(new FileOutputStream(volume));
            if (maxVolumeSize < MAGIC_NUMBER_LENGTH + 1)
            {
                // need to be able to fit at least the magic no. + 1 other byte in each volume
                throw new IllegalArgumentException("Argument 'maxVolumeSize' is invalid: " + maxVolumeSize);
            }
            basePath = volume.getAbsolutePath();
            this.maxVolumeSize = maxVolumeSize;
            magic = generateMagicNumber();

            initVolume();
        }

        /**
         * Sets the size of the space to leave free on the first volume.
         * <p/>
         * This may be used to allocate space for additional files on CD beside the pack files.
         */
        public void setFirstVolumeFreeSpaceSize(long size)
        {
            firstVolumeFreeSpaceSize = size;
        }

        /**
         * (non Javadoc)
         *
         * @see java.io.OutputStream#write(byte[], int, int)
         */
        @Override
        public void write(byte[] b, int off, int len) throws IOException
        {
            // calculate the available bytes
            long available = getAvailable();

            if (available < len)
            {
                // there's not enough space available, so write as much as possible, create the next volume, and
                // call this recursively
                logger.fine("Not enough space left on volume. (available: " + available + ")");
                if (available > 0)
                {
                    super.write(b, off, (int) available);
                    off += available;
                    len -= available;
                }
                createNextVolume();
                write(b, off, len);
            }
            else
            {
                super.write(b, off, len);
            }
        }

        /**
         * (non Javadoc)
         *
         * @see java.io.OutputStream#write(int)
         */
        @Override
        public void write(int b) throws IOException
        {
            long available = getAvailable();
            if (available == 0)
            {
                createNextVolume();
            }
            super.write(b);
        }

        /**
         * Closes the current volume and creates the next.
         *
         * @throws IOException for any I/O error
         */
        private void createNextVolume() throws IOException
        {
            // close current volume
            close();

            // create the next volume
            ++index;
            String name = basePath + "." + index;
            setOutputStream(new FileOutputStream(name));
            initVolume();
        }

        /**
         * Returns the number of volumes spanned.
         *
         * @return the number of volumes
         */
        public int getVolumes()
        {
            return index + 1;
        }

        /**
         * Initialises the volume.
         * <p/>
         * This writes a random byte array at the start of the volume. Each volume in the collection will have the same
         * bytes at the start, to detect an incorrect volume being used when read back in.
         *
         * @throws IOException
         */
        private void initVolume() throws IOException
        {
            write(magic);
        }

        /**
         * Returns the available space in the volume.
         *
         * @return the available space, in bytes
         */
        private long getAvailable()
        {
            long count = getByteCount();
            if (index == 0)
            {
                // this is the first volume so add the free space
                count += firstVolumeFreeSpaceSize;
            }
            return maxVolumeSize - count;
        }

        /**
         * Generates the magic number to write to each volume.
         *
         * @return the magic number
         */
        private byte[] generateMagicNumber()
        {
            byte[] result = new byte[MAGIC_NUMBER_LENGTH];
            Random random = new Random();
            random.nextBytes(result);
            if (logger.isLoggable(Level.FINE))
            {
                logger.fine("Created new magic number for SpanningOutputStream: " + formatMagic(magic));
            }
            return result;
        }
    }

}
