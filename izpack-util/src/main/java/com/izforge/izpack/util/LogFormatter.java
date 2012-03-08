package com.izforge.izpack.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * A variant of the default Java logging SimpleFormatter, not being so messy
 * @author rkrell
 */
public class LogFormatter extends Formatter
{
    Date date = new Date();
    private final static String format = "{0,date} {0,time}";
    private final String lineSeparator = System.getProperty("line.separator");
    private MessageFormat formatter;

    private Object args[] = new Object[1];

    /**
     * Format the given LogRecord.
     *
     * @param record the log record to be formatted.
     * @return a formatted log record
     */
    public synchronized String format(LogRecord record)
    {
        StringBuffer sb = new StringBuffer();
        // Minimize memory allocations here.
        date.setTime(record.getMillis());
        args[0] = date;
        StringBuffer text = new StringBuffer();
        if (formatter == null)
        {
            formatter = new MessageFormat(format);
        }
        formatter.format(args, text, null);
        sb.append(text);
        sb.append(" ");

        if (Debug.isDEBUG())
        {
            if (record.getSourceClassName() != null)
            {
                sb.append(record.getSourceClassName());
            }
            else
            {
                sb.append(record.getLoggerName());
            }
            if (record.getSourceMethodName() != null)
            {
                sb.append(" ");
                sb.append(record.getSourceMethodName());
            }

            sb.append(lineSeparator);
        }

        // Append log message
        String message = formatMessage(record);
        sb.append(record.getLevel().getLocalizedName());
        sb.append(": ");
        sb.append(message);

        // Append stacktrace
        if (Debug.isSTACKTRACE() && (record.getThrown() != null))
        {
            sb.append(lineSeparator);
            try
            {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                record.getThrown().printStackTrace(pw);
                pw.close();
                sb.append(sw.toString());
            }
            catch (Exception ex)
            {}
        }

        sb.append(lineSeparator);

        return sb.toString();
    }
}
