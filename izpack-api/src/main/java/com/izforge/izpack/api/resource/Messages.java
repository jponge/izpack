package com.izforge.izpack.api.resource;


import java.text.MessageFormat;

/**
 * Locale-specific messages.
 *
 * @author Tim Anderson
 */
public interface Messages
{

    /**
     * Formats the message with the specified identifier, replacing placeholders with the supplied arguments.
     * <p/>
     * This uses {@link MessageFormat} to format the message.
     *
     * @param id   the message identifier
     * @param args message arguments to replace placeholders in the message with
     * @return the corresponding message, or {@code id} if the message does not exist
     */
    String get(String id, Object... args);

}