package com.izforge.izpack.compiler;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLParser;
import com.izforge.izpack.api.data.DynamicVariable;
import com.izforge.izpack.api.exception.CompilerException;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.data.PropertyManager;
import com.izforge.izpack.compiler.helper.CompilerHelper;
import com.izforge.izpack.compiler.helper.XmlCompilerHelper;
import com.izforge.izpack.compiler.packager.IPackager;
import com.izforge.izpack.merge.MergeManager;
import com.izforge.izpack.merge.Mergeable;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.hamcrest.core.IsNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.verification.AtLeast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Anthonin Bonnefoy
 */
public class CompilerConfigTest {
    private XMLParser xmlParser;
    private CompilerConfig compilerConfig;
    private CompilerData data;
    private VariableSubstitutor variableSubstitutor;
    private Compiler compiler;
    private CompilerHelper compilerHelper;
    private PropertyManager propertyManager;
    private XmlCompilerHelper xmlCompilerHerlper;
    private Map<String, List<DynamicVariable>> mapStringListDyn;
    private MergeManager mergeManager;
    private IPackager packager;

    @Before
    public void setUp() {
        data = Mockito.mock(CompilerData.class);
        variableSubstitutor = Mockito.mock(VariableSubstitutor.class);
        compiler = Mockito.mock(Compiler.class);
        propertyManager = Mockito.mock(PropertyManager.class);
        compilerHelper = Mockito.mock(CompilerHelper.class);
        xmlCompilerHerlper = new XmlCompilerHelper(data.getInstallFile());
        mapStringListDyn = Mockito.mock(Map.class);
        packager = Mockito.mock(IPackager.class);
        mergeManager = new MergeManager();
        compilerConfig = new CompilerConfig(data, variableSubstitutor, compiler, compilerHelper, xmlCompilerHerlper, propertyManager, packager);
        xmlParser = new XMLParser();
    }

    @Test
    public void testAddTwoVariables() throws Exception {
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
    public void testAddDynamicVariable() throws CompilerException {
        Mockito.when(mapStringListDyn.containsKey("myPath")).thenReturn(false);
        Mockito.when(packager.getDynamicVariables()).thenReturn(mapStringListDyn);

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

    @Test
    public void compilerShouldAddVariable() throws Exception {
        IXMLElement xmlData = xmlParser.parse("<root><variables><variable name=\"scriptFile\" value=\"script.bat\"/></variables></root>");
        Properties variable = Mockito.mock(Properties.class);
        Mockito.when(packager.getVariables()).thenReturn(variable);
        compilerConfig.addVariables(xmlData);
        Mockito.verify(variable).setProperty("scriptFile", "script.bat");
    }

    @Test
    public void shouldAddDynamicVariable() throws Exception {
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


    @Test
    public void testGetMergeableFromPanelClass() throws Exception {
        Mergeable mergeable = mergeManager.getMergeableFromPanelClass("HelloPanel");
        ZipOutputStream outputStream = Mockito.mock(ZipOutputStream.class);
        assertThat(mergeable, IsNull.<Object>notNullValue());

        mergeable.merge(outputStream);
        Mockito.verify(outputStream, new AtLeast(2)).putNextEntry(Mockito.<ZipEntry>any());

    }
}
