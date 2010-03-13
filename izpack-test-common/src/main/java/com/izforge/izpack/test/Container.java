package com.izforge.izpack.test;

import com.izforge.izpack.api.container.BindeableContainer;

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
    public abstract Class<? extends BindeableContainer> value();
}
