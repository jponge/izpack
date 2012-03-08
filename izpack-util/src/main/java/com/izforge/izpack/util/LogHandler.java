package com.izforge.izpack.util;

import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

/**
 * A variant of the default Java logging ConsoleHandler, not being so messy
 * @author rkrell
 */
public class LogHandler extends StreamHandler
{
    private void configure()
    {
        LogManager manager = LogManager.getLogManager();
        String cname = getClass().getName();

        // Try loading configured formatter first
        String formatterName = manager.getProperty(cname + ".formatter");
        Formatter formatter = null;
        try {
            if (formatterName != null) {
                Class<Formatter> formatterClass = (Class<Formatter>) ClassLoader.getSystemClassLoader().loadClass(formatterName);
                formatter = (Formatter) formatterClass.newInstance();
            }
        } catch (Exception ex) {
            // We got one of a variety of exceptions in creating the
            // class or creating an instance.
            // Drop through.
        }

        // Load IzPack default formatter, if nothing was configured
        if (formatter == null)
        {
            formatter = new LogFormatter();
        }

        setFormatter(formatter);

        if (Debug.isDEBUG())
        {
            setLevel(Level.FINE);
        }
        else
        {
            setLevel(Level.INFO);
        }
    }

    /**
     * Create a <tt>LogHandler</tt> for <tt>System.err</tt>.
     * <p>
     * The <tt>LogHandler</tt> is configured based on <tt>LogManager</tt> properties (or their
     * default values).     *
     */
    public LogHandler()
    {
        configure();
        setOutputStream(System.err);
    }

    /**
     * Publish a <tt>LogRecord</tt>.
     * <p>
     * The logging request was made initially to a <tt>Logger</tt> object, which initialized the
     * <tt>LogRecord</tt> and forwarded it here.
     * <p>
     *
     * @param record description of the log event. A null record is silently ignored and is not
     * published
     */
    public void publish(LogRecord record)
    {
        super.publish(record);
        flush();
    }

    /**
     * Override <tt>LogHandler.close</tt> to do a flush but not to close the output stream. That
     * is, we do <b>not</b> close <tt>System.err</tt>.
     */
    public void close()
    {
        flush();
    }
}
