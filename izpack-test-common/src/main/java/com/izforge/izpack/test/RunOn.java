package com.izforge.izpack.test;

import com.izforge.izpack.util.Platform;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify platforms to run the test on.
 *
 * @author Tim Anderson
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Inherited
public @interface RunOn
{
    Platform.Name[] value();
}
