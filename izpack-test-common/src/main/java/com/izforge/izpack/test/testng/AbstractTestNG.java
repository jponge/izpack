package com.izforge.izpack.test.testng;

import org.testng.IObjectFactory;
import org.testng.annotations.ObjectFactory;

/**
 * Abstract test class for testNG configuring pico as the object factory
 *
 * @author Anthonin Bonnefoy
 */
public abstract class AbstractTestNG
{
    @ObjectFactory
    public IObjectFactory factory()
    {
        return new PicoNGFactory();
    }

}
