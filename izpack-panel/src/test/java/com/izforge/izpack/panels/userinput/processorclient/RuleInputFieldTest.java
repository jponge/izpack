package com.izforge.izpack.panels.userinput.processorclient;

import static org.junit.Assert.assertEquals;

import java.awt.Toolkit;
import java.util.Properties;

import org.junit.Test;
import org.mockito.Mockito;

import com.izforge.izpack.installer.data.GUIInstallData;


/**
 * Tests the {@link RuleInputField}.
 *
 * @author Tim Anderson
 */
public class RuleInputFieldTest
{

    /**
     * Tests {@link RuleInputField} support for entering IP addresses.
     */
    @Test
    public void testIPAddressRuleInputField()
    {
        String layout = "N:3:3 . N:3:3 . N:3:3 . N:3:3"; // IP address format
        String set = "0:192 1:168 2:0 3:1";              // default value
        String separator = null;
        String validator = null;
        String processor = null;
        Toolkit toolkit = Mockito.mock(Toolkit.class);

        GUIInstallData installData = new GUIInstallData(new Properties());

        RuleInputField field = new RuleInputField(layout, set, separator, validator, processor,
                                                  RuleInputField.DISPLAY_FORMAT, toolkit, installData);

        assertEquals(4, field.getNumFields());

        // check default value
        assertEquals("192.168.0.1", field.getText());
        assertEquals("192", field.getFieldContents(0));
        assertEquals("168", field.getFieldContents(1));
        assertEquals("0", field.getFieldContents(2));
        assertEquals("1", field.getFieldContents(3));

        // TODO - need to provide methods to update fields and verify field formats
    }
}
