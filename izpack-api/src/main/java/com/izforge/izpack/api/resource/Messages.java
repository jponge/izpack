package com.izforge.izpack.api.resource;


import java.text.MessageFormat;
import java.util.Map;

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

    /**
     * Adds messages.
     * <p/>
     * This merges the supplied messages with the current messages. If an existing message exists with the same
     * identifier as that supplied, it will be replaced.
     *
     * @param messages the messages to add
     */
    void add(Messages messages);

    /**
     * Returns the messages.
     *
     * @return the message identifiers, and their corresponding formats
     */
    Map<String, String> getMessages();

    /**
     * Creates a new messages instance from the named resource that inherits the current messages.
     *
     * @param name the messages resource name
     * @return the messages
     */
    Messages newMessages(String name);
}