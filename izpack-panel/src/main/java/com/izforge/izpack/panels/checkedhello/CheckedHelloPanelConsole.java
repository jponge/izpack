package com.izforge.izpack.panels.checkedhello;


import static com.izforge.izpack.api.handler.Prompt.Option.YES;
import static com.izforge.izpack.api.handler.Prompt.Options.YES_NO;
import static com.izforge.izpack.api.handler.Prompt.Type.ERROR;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.panels.hello.HelloPanelConsoleHelper;
import com.izforge.izpack.util.Console;

/**
 * Console implementation of the {@link CheckedHelloPanel}.
 *
 * @author Tim Anderson
 */
public class CheckedHelloPanelConsole extends HelloPanelConsoleHelper
{

    /**
     * The registry helper.
     */
    private final RegistryHelper registryHelper;

    /**
     * Determines if the application is already installed.
     */
    private boolean registered;

    /**
     * The prompt.
     */
    private final Prompt prompt;

    /**
     * The logger.
     */
    private static final Logger log = Logger.getLogger(CheckedHelloPanelConsole.class.getName());

    /**
     * Constructs a <tt>CheckedHelloPanelConsole</tt>.
     *
     * @param handler the registry handler
     * @param prompt  the prompt
     * @throws NativeLibException for any native library error
     */
    public CheckedHelloPanelConsole(RegistryDefaultHandler handler, Prompt prompt)
            throws NativeLibException
    {
        registryHelper = new RegistryHelper(handler);
        this.prompt = prompt;
        registered = registryHelper.isRegistered();
    }

    /**
     * Runs the panel using the specified console.
     *
     * @param installData the installation data
     * @param console     the console
     * @return <tt>true</tt> if the panel ran successfully, otherwise <tt>false</tt>
     */
    @Override
    public boolean runConsole(AutomatedInstallData installData, Console console)
    {
        boolean result = true;
        if (registered)
        {
            result = multipleInstall(installData);
            if (result)
            {
                try
                {
                    registryHelper.updateUninstallName();
                    registered = false;
                }
                catch (NativeLibException exception)
                {
                    result = false;
                    log.log(Level.SEVERE, exception.getMessage(), exception);
                }
            }
        }
        if (result)
        {
            display(installData, console);
            result = promptEndPanel(installData, console);
        }
        return result;
    }

    /**
     * Invoked if the product is already installed.
     * <p/>
     * This prompts the user whether to install twice or quit.
     *
     * @param installData the installation data
     * @return <tt>true</tt> to install again, <tt>false</tt> to quit
     */
    protected boolean multipleInstall(AutomatedInstallData installData)
    {
        boolean result;
        try
        {
            String path = registryHelper.getInstallationPath();
            if (path == null)
            {
                path = "<not found>";
            }
            Messages messages = installData.getMessages();
            String message = messages.get("CheckedHelloPanel.productAlreadyExist0") + path + "\n"
                    + messages.get("CheckedHelloPanel.productAlreadyExist1");
            result = prompt.confirm(ERROR, message, YES_NO) == YES;
        }
        catch (NativeLibException exception)
        {
            result = false;
        }
        return result;
    }

}
