package com.izforge.izpack.test;

import java.util.HashMap;
import java.util.Properties;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.parameters.ComponentParameter;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.core.container.ConditionContainer;
import com.izforge.izpack.core.rules.RulesEngineImpl;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.merge.resolve.MergeableResolver;

/**
 * Container for condition tests.
 *
 * @author Anthonin Bonnefoy
 */
public class TestConditionContainer extends AbstractContainer
{
    public void fillContainer(MutablePicoContainer picoContainer)
    {
        pico
                .addComponent(AutomatedInstallData.class, GUIInstallData.class)
                .addComponent(RulesEngine.class, RulesEngineImpl.class)
                .addComponent(VariableSubstitutor.class, VariableSubstitutorImpl.class)
                .addComponent(MutablePicoContainer.class, pico)
                .addComponent(MergeableResolver.class, MergeableResolver.class, new ComponentParameter(HashMap.class))
                .addComponent(HashMap.class)
                .addComponent(Properties.class)
                .addComponent(ConditionContainer.class)
                .addComponent(AbstractContainer.class, this);
    }
}
