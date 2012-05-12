package com.izforge.izpack.uninstaller.resource;

import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.data.ExecutableFile;
import com.izforge.izpack.util.FileExecutor;
import com.izforge.izpack.util.OsConstraintHelper;


/**
 * The uninstaller {@link ExecutableFile}s.
 *
 * @author Tim Anderson
 */
public class Executables
{

    /**
     * The executables.
     */
    private final List<ExecutableFile> executables;

    /**
     * The handler for reporting errors.
     */
    private final AbstractUIHandler handler;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(Executables.class.getName());


    /**
     * Constructs an <tt>Executables</tt>.
     *
     * @param resources used to locate the <em>executables</em> resource
     * @param handler   the handler for reporting errors
     * @throws IzPackException if the executables cannot be read
     */
    public Executables(Resources resources, AbstractUIHandler handler)
    {
        this.handler = handler;
        executables = read(resources);
    }

    /**
     * Runs the {@link ExecutableFile}s.
     * <p/>
     * TODO - should this update the uninstall progress?
     *
     * @return <tt>true</tt> if they were run successfully
     */
    public boolean run()
    {
        for (ExecutableFile file : executables)
        {
            if (file.executionStage == ExecutableFile.UNINSTALL
                    && OsConstraintHelper.oneMatchesCurrentSystem(file.osList))
            {
                if (!run(file))
                {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Runs an executable file.
     *
     * @param file the file to execute
     * @return <tt>true</tt> if it ran successfully, otherwise <tt>false</tt>
     */
    protected boolean run(ExecutableFile file)
    {
        FileExecutor executor = new FileExecutor(Arrays.asList(file));
        int status = executor.executeFiles(ExecutableFile.UNINSTALL, handler);
        if (status != 0)
        {
            logger.severe("Executable=" + file.path + " exited with status=" + status);
            return false;
        }
        return true;
    }

    /**
     * Reads the executables.
     *
     * @return the executables
     * @throws IzPackException if the executables cannot be read
     */
    private List<ExecutableFile> read(Resources resources)
    {
        List<ExecutableFile> executables = new ArrayList<ExecutableFile>();
        try
        {
            ObjectInputStream in = new ObjectInputStream(resources.getInputStream("executables"));
            int count = in.readInt();
            for (int i = 0; i < count; i++)
            {
                ExecutableFile file = (ExecutableFile) in.readObject();
                executables.add(file);
            }
        }
        catch (Exception exception)
        {
            throw new IzPackException("Failed to read executable resources", exception);
        }
        return executables;
    }

}
