package com.izforge.izpack.compiler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLParser;
import com.izforge.izpack.api.data.DynamicVariable;
import com.izforge.izpack.api.data.Value;
import com.izforge.izpack.api.exception.CompilerException;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.data.PropertyManager;
import com.izforge.izpack.compiler.helper.AssertionHelper;
import com.izforge.izpack.compiler.helper.XmlCompilerHelper;
import com.izforge.izpack.compiler.merge.resolve.ClassPathCrawler;
import com.izforge.izpack.compiler.merge.resolve.CompilerPathResolver;
import com.izforge.izpack.compiler.packager.IPackager;
import com.izforge.izpack.compiler.resource.ResourceFinder;
import com.izforge.izpack.core.data.DynamicVariableImpl;
import com.izforge.izpack.core.variable.PlainValue;
import com.izforge.izpack.merge.MergeManager;

/**
 * Test of compiler config with mock
 *
 * @author Anthonin Bonnefoy
 */
public class CompilerConfigMockedTest
{
    private Map<String, List<DynamicVariable>> mapStringListDyn;
    private XMLParser xmlParser = new XMLParser();
    private CompilerConfig compilerConfig;
    private IPackager packager;

    @Before
    public void setUp()
    {
        mapStringListDyn = Mockito.mock(Map.class);
        packager = Mockito.mock(IPackager.class);
        compilerConfig = new TestCompilerConfig(packager);
    }

    @Test
    public void testAddTwoVariables() throws Exception
    {
        Mockito.when(mapStringListDyn.containsKey("myPath")).thenReturn(false);
        Mockito.when(packager.getDynamicVariables()).thenReturn(mapStringListDyn);
        Properties variable = new Properties();
        Mockito.when(packager.getVariables()).thenReturn(variable);

        IXMLElement element = xmlParser.parse(
                "<root><dynamicvariables><variable name=\"myPath\" value=\"$INSTALLPATH/test\"/></dynamicvariables></root>");
        compilerConfig.addDynamicVariables(element);
        element = xmlParser.parse(
                "<root><variables><variable name=\"INSTALLPATH\" value=\"thePath\"/></variables></root>");
        compilerConfig.addVariables(element);

        verifyCallToMap(mapStringListDyn, "myPath", new PlainValue("thePath/test"));
    }

    @Test
    public void testAddDynamicVariable() throws CompilerException
    {
        Mockito.when(mapStringListDyn.containsKey("myPath")).thenReturn(false);
        Mockito.when(packager.getDynamicVariables()).thenReturn(mapStringListDyn);

        IXMLElement element = xmlParser.parse(
                "<root><dynamicvariables><variable name=\"myPath\" value=\"$INSTALLPATH/test\"/></dynamicvariables></root>");
        compilerConfig.addDynamicVariables(element);

        verifyCallToMap(mapStringListDyn, "myPath", new PlainValue("$INSTALLPATH/test"));
    }

    private void verifyCallToMap(Map<String, List<DynamicVariable>> mapStringListDyn, String name, Value value)
    {
        DynamicVariable dynamicVariable = new DynamicVariableImpl();
        dynamicVariable.setName(name);
        dynamicVariable.setValue(value);
        ArrayList<DynamicVariable> list = new ArrayList<DynamicVariable>();
        list.add(dynamicVariable);
        Mockito.verify(mapStringListDyn).put(name, list);
    }

    @Test
    public void compilerShouldAddVariable() throws Exception
    {
        IXMLElement xmlData = xmlParser.parse(
                "<root><variables><variable name=\"scriptFile\" value=\"script.bat\"/></variables></root>");
        Properties variable = Mockito.mock(Properties.class);
        Mockito.when(packager.getVariables()).thenReturn(variable);
        compilerConfig.addVariables(xmlData);
        Mockito.verify(variable).setProperty("scriptFile", "script.bat");
    }

    @Test
    public void shouldAddDynamicVariable() throws Exception
    {
        IXMLElement xmlData = xmlParser.parse(
                "<root><dynamicvariables><variable name='myPath' value='$INSTALLPATH/test'/></dynamicvariables></root>");
        Map variable = Mockito.mock(Map.class);

        Mockito.when(variable.containsKey("myPath")).thenReturn(false);
        Mockito.when(packager.getDynamicVariables()).thenReturn(variable);

        compilerConfig.addDynamicVariables(xmlData);

        new ArrayList();
        DynamicVariable dynamicVariable = new DynamicVariableImpl();
        dynamicVariable.setName("myPath");
        dynamicVariable.setValue(new PlainValue("$INSTALLPATH/test"));
        ArrayList<DynamicVariable> list = new ArrayList<DynamicVariable>();
        list.add(dynamicVariable);
        Mockito.verify(variable).put("myPath", list);
    }

    /**
     * A test version of {@link CompilerConfig} which mocks most attributes.
     */
    private class TestCompilerConfig extends CompilerConfig
    {

        public TestCompilerConfig(IPackager packager)
        {
            super(Mockito.mock(CompilerData.class), Mockito.mock(VariableSubstitutor.class),
                  Mockito.mock(Compiler.class), new XmlCompilerHelper(Mockito.mock(AssertionHelper.class)),
                  Mockito.mock(PropertyManager.class), Mockito.mock(MergeManager.class),
                  Mockito.mock(AssertionHelper.class), Mockito.mock(ClassPathCrawler.class),
                  Mockito.mock(RulesEngine.class), Mockito.mock(CompilerPathResolver.class),
                  Mockito.mock(ResourceFinder.class), Mockito.mock(ObjectFactory.class));
            setPackager(packager);
        }
    }

}
