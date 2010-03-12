package org.codehaus.izpack.test.customrunner;

import com.izforge.izpack.installer.container.IInstallerContainer;

import java.lang.annotation.*;

/**
 * Annotation to specify used container
 *
 * @author Anthonin Bonnefoy
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface Container
{
    /**
     * @return the container class
     */
    public abstract Class<? extends IInstallerContainer> value();
}
