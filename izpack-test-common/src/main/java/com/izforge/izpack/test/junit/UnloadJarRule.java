package com.izforge.izpack.test.junit;

import com.izforge.izpack.test.ClassUtils;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * Rule for unloading last jar
 *
 * @author Anthonin Bonnefoy
 */
public class UnloadJarRule implements MethodRule
{
    public Statement apply(final Statement base, FrameworkMethod method, Object target)
    {
        return new Statement()
        {
            @Override
            public void evaluate() throws Throwable
            {
                try
                {
                    base.evaluate();
                }
                finally
                {
                    ClassUtils.unloadLastJar();
                }
            }
        };
    }

}
