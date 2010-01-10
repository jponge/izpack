package com.izforge.izpack.compiler;

import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.adaptator.impl.XMLParser;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.data.PropertyManager;
import com.izforge.izpack.compiler.helper.CompilerHelper;
import com.izforge.izpack.compiler.helper.impl.XmlCompilerHelper;
import com.izforge.izpack.data.DynamicVariable;
import com.izforge.izpack.util.substitutor.VariableSubstitutor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Anthonin Bonnefoy
 */
@RunWith(MockitoJUnitRunner.class)
public class CompilerConfigTest {
    private XMLParser xmlParser;
    private CompilerConfig compilerConfig;
    @Mock
    private CompilerData data;
    @Mock
    private VariableSubstitutor variableSubstitutor;
    @Mock
    private Compiler compiler;
    @Mock
    private CompilerHelper compilerHelper;
    @Mock
    private PropertyManager propertyManager;
    @Mock
    private XmlCompilerHelper xmlCompilerHerlper;
    @Mock
    private Map<String, List<DynamicVariable>> mapStringListDyn;

    @Before
    public void setUp() {
        compilerConfig = new CompilerConfig(data, variableSubstitutor, compiler, compilerHelper, xmlCompilerHerlper, propertyManager);
        xmlParser = new XMLParser();
    }

    @Test
    public void testAddTwoVariables() throws Exception {
        Mockito.when(mapStringListDyn.containsKey("myPath")).thenReturn(false);
        Mockito.when(compiler.getDynamicVariables()).thenReturn(mapStringListDyn);
        Properties variable = new Properties();
        Mockito.when(compiler.getVariables()).thenReturn(variable);

        IXMLElement element = xmlParser.parse("<root><dynamicvariables><variable name=\"myPath\" value=\"$INSTALLPATH / test\"/></dynamicvariables></root>");
        compilerConfig.addDynamicVariables(element);
        element = xmlParser.parse("<root><variables><variable name=\"INSTALLPATH\" value=\"thePath\"/></variables></root>");
        compilerConfig.addVariables(element);

        verifyCallToMap(mapStringListDyn, "myPath", "thePath/test");
    }

    @Test
    public void testAddDynamicVariable() throws CompilerException {
        Mockito.when(mapStringListDyn.containsKey("myPath")).thenReturn(false);
        Mockito.when(compiler.getDynamicVariables()).thenReturn(mapStringListDyn);

        IXMLElement element = xmlParser.parse("<root><dynamicvariables><variable name=\"myPath\" value=\"$INSTALLPATH / test\"/></dynamicvariables></root>");
        compilerConfig.addDynamicVariables(element);

        verifyCallToMap(mapStringListDyn, "myPath", "$INSTALLPATH/test");
    }

    private void verifyCallToMap(Map<String, List<DynamicVariable>> mapStringListDyn, String name, String value) {
        DynamicVariable dyn = new DynamicVariable();
        dyn.setName(name);
        dyn.setValue(value);
        ArrayList<DynamicVariable> list = new ArrayList<DynamicVariable>();
        list.add(dyn);
        Mockito.verify(mapStringListDyn).put(name, list);
    }

}
