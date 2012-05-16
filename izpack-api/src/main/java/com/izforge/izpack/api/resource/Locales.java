package com.izforge.izpack.api.resource;


import java.util.List;
import java.util.Locale;

import com.izforge.izpack.api.exception.ResourceNotFoundException;


/**
 * Supported locales.
 *
 * @author Tim Anderson
 */
public interface Locales
{

    /**
     * Returns the current locale.
     *
     * @return the current locale. May be <tt>null</tt>
     */
    Locale getLocale();

    /**
     * Sets the current locale.
     *
     * @param locale the locale. May be <tt>null</tt>
     */
    void setLocale(Locale locale);

    /**
     * Returns the locale corresponding to the supplied ISO2/ISO3 code.
     *
     * @param code the 2 or 3 character ISO code
     * @return the corresponding locale, or {@code null} if the locale isn't supported
     */
    Locale getLocale(String code);

    /**
     * Returns the supported locales.
     *
     * @return the supported locales
     */
    List<Locale> getLocales();

    /**
     * Returns messages for the current locale.
     *
     * @return messages for the current locale
     * @throws ResourceNotFoundException if the messages resource cannot be found
     */
    Messages getMessages();

    /**
     * Returns the named messages for the current locale.
     *
     * @param name the message resource name
     * @return messages for the current locale
     * @throws ResourceNotFoundException if the named resource cannot be found
     */
    Messages getMessages(String name);

}
