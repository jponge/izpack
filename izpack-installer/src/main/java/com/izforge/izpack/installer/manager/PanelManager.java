package com.izforge.izpack.installer.manager;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.container.BindeableContainer;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.installer.DataValidator;
import com.izforge.izpack.data.PanelAction;
import com.izforge.izpack.installer.base.IzPanel;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.util.OsConstraintHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Load panels in the container.
 */
public class PanelManager
{

    /**
     * The installation data.
     */
    private GUIInstallData installData;

    /**
     * The installer container.
     */
    private BindeableContainer installerContainer;

    /**
     * The factory for {@link DataValidator} and {@link PanelAction} instances.
     */
    private final ObjectFactory factory;


    private int lastVis;

    /**
     * Mapping from "raw" panel number to visible panel number.
     */
    private final ArrayList<Integer> visiblePanelMapping = new ArrayList<Integer>();


    /**
     * Constructs a <tt>PanelManager</tt>.
     *
     * @param installData        the installation data
     * @param installerContainer the installer container
     * @param factory            the factory for {@link DataValidator} instances
     */
    public PanelManager(GUIInstallData installData, BindeableContainer installerContainer, ObjectFactory factory)
    {
        this.installData = installData;
        this.installerContainer = installerContainer;
        this.factory = factory;
    }

    /**
     * Creates the panels.
     * <p/>
     * This invokes any pre-construction actions associated with them.
     *
     * @throws ClassNotFoundException if a class cannot be found
     * @throws IllegalStateException  if a class does not extend {@link IzPanel}.
     */
    public void createPanels() throws ClassNotFoundException
    {
        int curVisPanelNumber = 0;
        lastVis = 0;
        int count = 0;
        Map<Object, Panel> panels = addComponents();
        for (Map.Entry<Object, Panel> entry : panels.entrySet())
        {
            Object key = entry.getKey();
            Panel panel = entry.getValue();

            executePreConstructionActions(panel);
            IzPanel izPanel = (IzPanel) installerContainer.getComponent(key);
            izPanel.setMetadata(panel);
            String dataValidator = panel.getValidator();
            if (dataValidator != null)
            {
                izPanel.setValidationService(factory.create(dataValidator, DataValidator.class));
            }
            izPanel.setHelpUrl(panel.getHelpUrl(installData.getLocaleISO3()));

            addPreActivationActions(panel, izPanel);
            addPreValidateActions(panel, izPanel);
            addPostValidationActions(panel, izPanel);

            installData.getPanels().add(izPanel);
            if (izPanel.isHidden())
            {
                visiblePanelMapping.add(count, -1);
            }
            else
            {
                visiblePanelMapping.add(count, curVisPanelNumber);
                curVisPanelNumber++;
                lastVis = count;
            }
            count++;
            // We add the XML installDataGUI izPanel root
            IXMLElement panelRoot = new XMLElementImpl(panel.getClassName(), installData.getXmlData());
            // if set, we add the id as an attribute to the panelRoot
            String panelId = panel.getPanelid();
            if (panelId != null)
            {
                panelRoot.setAttribute("id", panelId);
            }
            installData.getXmlData().addChild(panelRoot);
            visiblePanelMapping.add(count, lastVis);
        }
    }

    public boolean isVisible(int panelNumber)
    {
        return !(visiblePanelMapping.get(panelNumber) == -1);
    }

    public boolean isLast(int panelNumber)
    {
        return (visiblePanelMapping.get(installData.getPanels().size()) == panelNumber);
    }

    public int getPanelVisibilityNumber(int panel)
    {
        return visiblePanelMapping.get(panel);
    }

    public int getCountVisiblePanel()
    {
        return lastVis;
    }

    /**
     * Adds the panel classes to the container.
     *
     * @return a map of the panels, keyed on their PicoContainer component type
     * @throws ClassNotFoundException if a panel class cannot be found
     */
    private Map<Object, Panel> addComponents() throws ClassNotFoundException
    {
        Map<Object, Panel> panels = new LinkedHashMap<Object, Panel>();
        for (Panel panel : installData.getPanelsOrder())
        {
            if (OsConstraintHelper.oneMatchesCurrentSystem(panel.getOsConstraints()))
            {
                Class<IzPanel> panelClass = getPanelClass(panel);
                String panelId = panel.getPanelid();
                if (panelId != null)
                {
                    if (panels.put(panelId, panel) != null)
                    {
                        throw new IllegalStateException("Duplicate panel: " + panelId);
                    }
                    installerContainer.addComponent(panelId, panelClass);
                }
                else
                {
                    if (panels.put(panelClass, panel) != null)
                    {
                        throw new IllegalStateException("Duplicate panel: " + panelClass);
                    }
                    installerContainer.addComponent(panelClass);
                }
            }
        }
        return panels;
    }

    /**
     * Executes pre-construction action associated with a panel.
     *
     * @param panel the panel
     */
    private void executePreConstructionActions(Panel panel)
    {
        List<String> classNames = panel.getPreConstructionActions();
        if (classNames != null)
        {
            for (String className : classNames)
            {
                PanelAction action = factory.create(className, PanelAction.class);
                action.initialize(panel.getPanelActionConfiguration(className));
                action.executeAction(installData, null);
            }
        }
    }

    /**
     * Adds any pre-activation actions to the panel.
     *
     * @param panel   the panel meta-data
     * @param izPanel the panel to add the actions to
     */
    private void addPreActivationActions(Panel panel, IzPanel izPanel)
    {
        List<String> classNames = panel.getPreActivationActions();
        if (classNames != null)
        {
            for (String className : classNames)
            {
                PanelAction action = factory.create(className, PanelAction.class);
                action.initialize(panel.getPanelActionConfiguration(className));
                izPanel.addPreActivationAction(action);
            }
        }
    }

    /**
     * Adds any pre-validation actions to the panel.
     *
     * @param panel   the panel meta-data
     * @param izPanel the panel to add the actions to
     */
    private void addPreValidateActions(Panel panel, IzPanel izPanel)
    {
        List<String> classNames = panel.getPreValidationActions();
        if (classNames != null)
        {
            for (String className : classNames)
            {
                PanelAction action = factory.create(className, PanelAction.class);
                action.initialize(panel.getPanelActionConfiguration(className));
                izPanel.addPreValidationAction(action);
            }
        }
    }

    /**
     * Adds any post-validation actions to the panel.
     *
     * @param panel   the panel meta-data
     * @param izPanel the panel to add the actions to
     */
    private void addPostValidationActions(Panel panel, IzPanel izPanel)
    {
        List<String> classNames = panel.getPostValidationActions();
        if (classNames != null)
        {
            for (String className : classNames)
            {
                PanelAction action = factory.create(className, PanelAction.class);
                action.initialize(panel.getPanelActionConfiguration(className));
                izPanel.addPostValidationAction(action);
            }
        }
    }

    /**
     * Returns the implementing class for a panel.
     *
     * @param panel the panel
     * @return the class that implements the panel
     * @throws ClassNotFoundException if the class cannot be found
     * @throws IllegalStateException  if the specified class does not extend {@link IzPanel}.
     */
    @SuppressWarnings("unchecked")
    private Class<IzPanel> getPanelClass(Panel panel) throws ClassNotFoundException
    {
        Class result = Class.forName(panel.getClassName()); // compiler emits fully qualified class names
        if (!IzPanel.class.isAssignableFrom(result))
        {
            throw new IllegalStateException("Class " + result + " does not extend "
                                                    + IzPanel.class.getName());
        }
        return result;
    }

}
