package com.izforge.izpack.compiler;

import junit.framework.TestCase;
import java.io.*;

public class ByteCountingOutputStreamTest extends TestCase
{

    public void testWriting() throws IOException
    {
        File temp = File.createTempFile("foo", "bar");
        FileOutputStream fout = new FileOutputStream(temp);
        ByteCountingOutputStream out = new ByteCountingOutputStream(fout);

        byte[] data = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        out.write(data);
        out.write(data, 3, 2);
        out.write(1024);
        out.close();
        
        TestCase.assertEquals(16, out.getByteCount());
    }

}
