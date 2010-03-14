package com.izforge.izpack.compiler;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLParser;
import com.izforge.izpack.api.data.DynamicVariable;
import com.izforge.izpack.api.exception.CompilerException;
import com.izforge.izpack.compiler.container.TestCompilerContainerMock;
import com.izforge.izpack.compiler.packager.IPackager;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.junit.PicoRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Test of compiler config with mock
 *
 * @author Anthonin Bonnefoy
 */
@RunWith(PicoRunner.class)
@Container(TestCompilerContainerMock.class)
public class CompilerConfigMockedTest
{
    private Map<String, List<DynamicVariable>> mapStringListDyn;
    private IPackager packager;
    private XMLParser xmlParser;
    private CompilerConfig compilerConfig;

    public CompilerConfigMockedTest(Map<String, List<DynamicVariable>> mapStringListDyn, IPackager packager, XMLParser xmlParser, CompilerConfig compilerConfig)
    {
        this.mapStringListDyn = mapStringListDyn;
        this.packager = packager;
        this.xmlParser = xmlParser;
        this.compilerConfig = compilerConfig;
    }

    @Test
    public void testAddTwoVariables() throws Exception
    {
        Mockito.when(mapStringListDyn.containsKey("myPath")).thenReturn(false);
        Mockito.when(packager.getDynamicVariables()).thenReturn(mapStringListDyn);
        Properties variable = new Properties();
        Mockito.when(packager.getVariables()).thenReturn(variable);

        IXMLElement element = xmlParser.parse("<root><dynamicvariables><variable name=\"myPath\" value=\"$INSTALLPATH / test\"/></dynamicvariables></root>");
        compilerConfig.addDynamicVariables(element);
        element = xmlParser.parse("<root><variables><variable name=\"INSTALLPATH\" value=\"thePath\"/></variables></root>");
        compilerConfig.addVariables(element);

        verifyCallToMap(mapStringListDyn, "myPath", "thePath/test");
    }

    @Test
    public void testAddDynamicVariable() throws CompilerException
    {
        Mockito.when(mapStringListDyn.containsKey("myPath")).thenReturn(false);
        Mockito.when(packager.getDynamicVariables()).thenReturn(mapStringListDyn);

        IXMLElement element = xmlParser.parse("<root><dynamicvariables><variable name=\"myPath\" value=\"$INSTALLPATH / test\"/></dynamicvariables></root>");
        compilerConfig.addDynamicVariables(element);

        verifyCallToMap(mapStringListDyn, "myPath", "$INSTALLPATH/test");
    }

    private void verifyCallToMap(Map<String, List<DynamicVariable>> mapStringListDyn, String name, String value)
    {
        DynamicVariable dyn = new DynamicVariable();
        dyn.setName(name);
        dyn.setValue(value);
        ArrayList<DynamicVariable> list = new ArrayList<DynamicVariable>();
        list.add(dyn);
        Mockito.verify(mapStringListDyn).put(name, list);
    }

    @Test
    public void compilerShouldAddVariable() throws Exception
    {
        IXMLElement xmlData = xmlParser.parse("<root><variables><variable name=\"scriptFile\" value=\"script.bat\"/></variables></root>");
        Properties variable = Mockito.mock(Properties.class);
        Mockito.when(packager.getVariables()).thenReturn(variable);
        compilerConfig.addVariables(xmlData);
        Mockito.verify(variable).setProperty("scriptFile", "script.bat");
    }

    @Test
    public void shouldAddDynamicVariable() throws Exception
    {
        IXMLElement xmlData = xmlParser.parse("<root><dynamicvariables><variable name='myPath' value='$INSTALLPATH/test'/></dynamicvariables></root>");
        Map variable = Mockito.mock(Map.class);

        Mockito.when(variable.containsKey("myPath")).thenReturn(false);
        Mockito.when(packager.getDynamicVariables()).thenReturn(variable);

        compilerConfig.addDynamicVariables(xmlData);

        new ArrayList();
        DynamicVariable dyn = new DynamicVariable();
        dyn.setName("myPath");
        dyn.setValue("$INSTALLPATH/test");
        ArrayList<DynamicVariable> list = new ArrayList<DynamicVariable>();
        list.add(dyn);
        Mockito.verify(variable).put("myPath", list);
    }

}
