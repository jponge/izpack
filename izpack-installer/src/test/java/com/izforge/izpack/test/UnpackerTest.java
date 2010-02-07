package com.izforge.izpack.test;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.unpacker.Unpacker;
import com.izforge.izpack.util.AbstractUIProgressHandler;
import com.izforge.izpack.util.substitutor.VariableSubstitutorImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Anthonin Bonnefoy
 */
public class UnpackerTest {
    private VariableSubstitutor variableSubstitutor;
    private RulesEngine rules;
    private AbstractUIProgressHandler progressHandler;
    private ResourceManager resourceManager;
    private AutomatedInstallData idata;
    private Unpacker unpacker;
    private UninstallData udata;

    @Before
    public void setUp() {
        variableSubstitutor = new VariableSubstitutorImpl(System.getProperties());
        rules = Mockito.mock(RulesEngine.class);
        resourceManager = Mockito.mock(ResourceManager.class);
        progressHandler = Mockito.mock(AbstractUIProgressHandler.class);
        idata = Mockito.mock(AutomatedInstallData.class);
        udata = Mockito.mock(UninstallData.class);
        unpacker = new Unpacker(idata, resourceManager, progressHandler, rules, variableSubstitutor, udata);
    }

    @Test
    public void testUnpackUninstaller() throws Exception {
        Info info = new Info();
        info.setUninstallerPath("/tmp/uninstaller.jar");
        Mockito.when(idata.getInfo()).thenReturn(info);

        unpacker.putUninstaller();

    }
}
