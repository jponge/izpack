//package org.izforge.izpack;
//
//import com.izforge.izpack.installer.InstallData;
//import com.izforge.izpack.installer.InstallerBase;
//import com.izforge.izpack.rules.RulesEngine;
//import org.fest.swing.fixture.FrameFixture;
//import org.hamcrest.core.Is;
//import org.junit.Before;
//import org.junit.Test;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import java.awt.*;
//import java.io.File;
//import java.lang.reflect.Constructor;
//import java.net.URL;
//
//import static org.hamcrest.MatcherAssert.assertThat;
//
///**
// * Test for an installation using mock data
// */
//public class MockInstallationTest {
//
//    private FrameFixture window;
//
//    @Mock
//    private InstallData mockInstallData;
//
//    @Mock
//    private InstallerBase mockInstallerBase;
//    @Mock
//    private RulesEngine mockRulesEngine;
//
//    @Before
//    public void setUp() throws Exception {
//        MockitoAnnotations.initMocks(getClass());
//        configureStub();
//        URL urls[] = {};
//        JarFileLoader jarLoader = new JarFileLoader(urls);
//        File file = new File(getClass().getClassLoader().getResource("samples1/out.jar").getFile());
//        assertThat(file.exists(), Is.is(true));
//        jarLoader.addFile(file.getAbsolutePath());
//        Class c = jarLoader.loadClass("com.izforge.izpack.installer.InstallerFrame");
//        Constructor ctor = c.getDeclaredConstructor(new Class[]{String.class, InstallData.class, InstallerBase.class});
//        ctor.setAccessible(true);
//        Object installerFrame = ctor.newInstance("test", mockInstallData, mockInstallerBase);
//        window = new FrameFixture((Frame) installerFrame);
//        window.show();
//        Thread.sleep(1000);
//    }
//
//    private void configureStub() {
//    }
//
//    @Test
//    public void testInstallSamples1() throws Exception {
//
//    }
//}
