package com.izforge.izpack.compiler;

import com.izforge.izpack.adaptator.impl.XMLParser;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.data.PropertyManager;
import com.izforge.izpack.compiler.helper.CompilerHelper;
import com.izforge.izpack.compiler.helper.impl.XmlCompilerHelper;
import com.izforge.izpack.data.DynamicVariable;
import com.izforge.izpack.util.substitutor.VariableSubstitutor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Anthonin Bonnefoy
 */
public class CompilerConfigTest {
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

    private XMLParser xmlParser;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(getClass());
        compilerConfig = new CompilerConfig(data, variableSubstitutor, compiler, compilerHelper, xmlCompilerHerlper, propertyManager);
        xmlParser = new XMLParser();
    }

    @Test
    public void testAddDynamicVariable() throws CompilerException {
        Map<String, List<DynamicVariable>> mapStringListDyn = Mockito.mock((Map.class));

        Mockito.when(mapStringListDyn.containsKey("myPath")).thenReturn(false);
        Mockito.when(compiler.getDynamicVariables()).thenReturn(mapStringListDyn);

        compilerConfig.addDynamicVariables(xmlParser.parse("<root><dynamicvariables><variables><variable name=\"myPath\" value=\"$INSTALLPATH / test\"/></variables></dynamicvariables></root>"));

        DynamicVariable dyn = new DynamicVariable();
        dyn.setName("myPath");
        dyn.setValue("$INSTALLPATH/test");
        ArrayList<DynamicVariable> list = new ArrayList<DynamicVariable>();
        list.add(dyn);
        Mockito.verify(mapStringListDyn).put("myPath", list);
    }

}
