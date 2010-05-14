package com.izforge.izpack.integration;

import com.izforge.izpack.compiler.container.TestIntegrationContainer;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.junit.PicoRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for event binding
 *
 * @author Anthonin Bonnefoy
 */

@RunWith(PicoRunner.class)
@Container(TestIntegrationContainer.class)
public class EventTest
{
    @Test
    @InstallFile("samples/event/event.xml")
    public void eventInitialization() throws Exception
    {
    }
}
