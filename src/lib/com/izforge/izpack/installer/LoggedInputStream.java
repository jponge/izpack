package com.izforge.izpack.installer;

import java.io.IOException;
import java.awt.event.*;
import java.io.InputStream;
import javax.swing.*;
import com.izforge.izpack.Pack;

/**
 * 
 * Wraps an InputStream in order to track how much bytes are being read, and
 * then updates the progress dialog. When the stream is opened the progress 
 * dialog shows up. When the stream is closed the dialog is disposed. Make sure
 * you are closing the streams.
 * 
 * @author <a href="vralev@redhat.com">Vladimir Ralev</a>
 * @version $Revision: 1.1 $
 */
public class LoggedInputStream extends InputStream
{
   private long bytesRead = 0;
   private InputStream is;
   private DownloadPanel downloader;
   private WebAccessor webAccessor;
   private boolean cancelled = false;
   private long lastTime = -1;
   private long lastBytes = -1;
   
   public void setCancelled(boolean cancel)
   {
      cancelled = cancel;
   }
   
   public LoggedInputStream(InputStream is, WebAccessor webAccessor)
   {
      if(is == null) throw new RuntimeException("Unable to connect");
      this.is = is;
      this.webAccessor = webAccessor;
      
      String sizeStr;
      if(webAccessor.getContentLength()>0)
         sizeStr = "(" + Pack.toByteUnitsString(webAccessor.getContentLength()) + ")";
      else
         sizeStr = "";
      
      downloader = new DownloadPanel(this);
      downloader.setTitle("Downloading");
      downloader.setFileLabel(webAccessor.getUrl() + " " + sizeStr);
      downloader.setLocationRelativeTo(null);
      downloader.setVisible(true);
      if(webAccessor.getContentLength()>0)
      {
         downloader.setProgressMax(webAccessor.getContentLength());
         downloader.setProgressCurrent(0);
      }
   }
   
   public int available() throws IOException
   {
      return is.available();
   }

   public void close() throws IOException
   {
      downloader.setVisible(false);
      downloader.dispose();
      is.close();
   }

   public synchronized void mark(int readlimit)
   {
      is.mark(readlimit);
   }

   public boolean markSupported()
   {
      return is.markSupported();
   }

   public synchronized void reset() throws IOException
   {
      is.reset();
   }

   public long skip(long n) throws IOException
   {
      return is.skip(n);
   }
   
   public int read(byte[] b, int off, int len) throws IOException
   {
      int bytes = is.read(b, off, len);
      if(bytes > 0) bytesRead += bytes;
      update();
      return bytes;
   }

   public int read(byte[] b) throws IOException
   {
      int bytes =  is.read(b);
      if(bytes > 0) bytesRead += bytes;
      update();
      return bytes;
   }

   public long getBytesRead()
   {
      return bytesRead;
   }

   public int read() throws IOException
   {
      int bytes =  is.read();
      if(bytes > 0) bytesRead += 1;
      update();
      return bytes;
   }
   
   private void update()
   {
      if(lastTime > 0)
      {
         long currTime = System.currentTimeMillis();
         long diff = currTime - lastTime;
         if(diff > 800)
         {
            double bps = (double)(bytesRead-lastBytes)/((double)(diff)/1000.);
            downloader.setStatusLabel(Pack.toByteUnitsString(Math.round(bps)) + "/s");
            lastTime = currTime;
            lastBytes = bytesRead;
         }
      }
      else
      {
         lastTime = System.currentTimeMillis();
         lastBytes = bytesRead;
      }
      downloader.setProgressCurrent((int)bytesRead);
      if(cancelled)
         throw new RuntimeException("Cancelled");
   }
}