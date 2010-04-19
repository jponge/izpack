package com.izforge.izpack.panels.treepacks;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.GUIInstallData;
import com.izforge.izpack.api.data.LocaleDatabase;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.installer.base.InstallerFrame;
import com.izforge.izpack.installer.base.IzPanel;
import com.izforge.izpack.installer.debugger.Debugger;
import com.izforge.izpack.installer.web.WebAccessor;
import com.izforge.izpack.panels.imgpacks.ImgPacksPanelAutomationHelper;
import com.izforge.izpack.panels.packs.PacksModel;
import com.izforge.izpack.panels.packs.PacksPanelInterface;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.IoHelper;

import javax.swing.*;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Anthonin Bonnefoy
 */
public class TreePacksPanel extends IzPanel implements PacksPanelInterface
{
    /**
     * Required (serializable)
     */
    private static final long serialVersionUID = 5684716698930628262L;

    // Common used Swing fields
    /**
     * The free space label.
     */
    protected JLabel freeSpaceLabel;

    /**
     * The space label.
     */
    protected JLabel spaceLabel;

    /**
     * The tip label.
     */
    protected JTextArea descriptionArea;

    /**
     * The dependencies label.
     */
    protected JTextArea dependencyArea;

    /**
     * The packs tree.
     */
    protected JTree packsTree;

    /**
     * The packs model.
     */
    protected PacksModel packsModel;

    /**
     * The tablescroll.
     */
    protected JScrollPane tableScroller;

    // Non-GUI fields
    /**
     * Map that connects names with pack objects
     */
    private Map<String, Pack> names;

    /**
     * The bytes of the current pack.
     */
    protected long bytes = 0;

    /**
     * The free bytes of the current selected disk.
     */
    protected long freeBytes = 0;

    /**
     * Are there dependencies in the packs
     */
    protected boolean dependenciesExist = false;

    /**
     * The packs locale database.
     */
    private LocaleDatabase langpack = null;

    /**
     * The name of the XML file that specifies the panel langpack
     */
    private static final String LANG_FILE_NAME = "packsLang.xml";

    private HashMap<String, Pack> idToPack;
    private HashMap<String, List<String>> treeData;
    private HashMap<Pack, Integer> packToRowNumber;

    private HashMap<String, CheckBoxNode> idToCheckBoxNode = new HashMap<String, CheckBoxNode>();
    //private boolean created = false;   // UNUSED

    private CheckTreeController checkTreeController;
    private RulesEngine rules;

    /**
     * The constructor.
     *
     * @param rules
     * @param parent The parent window.
     * @param idata  The installation installDataGUI.
     */
    public TreePacksPanel(InstallerFrame parent, GUIInstallData idata, ResourceManager resourceManager, RulesEngine rules)
    {
        super(parent, idata, resourceManager);
        // Load langpack.
        try
        {
            this.langpack = installData.getLangpack();
            InputStream langPackStream = null;
            String webdir = idata.getInfo().getWebDirURL();
            if (webdir != null)
            {
                try
                {
                    java.net.URL url = new java.net.URL(webdir + "/langpacks/" + LANG_FILE_NAME + idata.getLocaleISO3());
                    langPackStream = new WebAccessor(null).openInputStream(url);
                }
                catch (Exception e)
                {
                    // just ignore this. we use the fallback below
                }
            }

            if (langPackStream == null)
            {
                langPackStream = this.resourceManager.getInputStream(LANG_FILE_NAME);
            }

            this.langpack.add(langPackStream);
            langPackStream.close();
        }
        catch (Throwable exception)
        {
            Debug.trace(exception);
        }

        // init the map
        computePacks(idata.getAvailablePacks());

        this.rules = rules;
    }

    /**
     * The Implementation of this method should create the layout for the current class.
     */

    protected void createNormalLayout()
    {
        this.removeAll();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        createLabel("PacksPanel.info", "preferences", null, null);
        add(Box.createRigidArea(new Dimension(0, 3)));
        createLabel("PacksPanel.tip", "tip", null, null);
        add(Box.createRigidArea(new Dimension(0, 5)));
        tableScroller = new JScrollPane();
        packsTree = createPacksTree(300, tableScroller, null, null);
        if (dependenciesExist)
        {
            dependencyArea = createTextArea("PacksPanel.dependencyList", null, null, null);
        }
        descriptionArea = createTextArea("PacksPanel.description", null, null, null);
        spaceLabel = createPanelWithLabel("PacksPanel.space", null, null);
        if (IoHelper.supported("getFreeSpace"))
        {
            add(Box.createRigidArea(new Dimension(0, 3)));
            freeSpaceLabel = createPanelWithLabel("PacksPanel.freespace", null, null);
        }
    }

    /*
    * (non-Javadoc)
    *
    * @see com.izforge.izpack.panels.packs.PacksPanelInterface#getLangpack()
    */

    public LocaleDatabase getLangpack()
    {
        return (langpack);
    }

    /*
    * (non-Javadoc)
    *
    * @see com.izforge.izpack.panels.packs.PacksPanelInterface#getBytes()
    */

    public long getBytes()
    {
        return (bytes);
    }

    /*
    * (non-Javadoc)
    *
    * @see com.izforge.izpack.panels.packs.PacksPanelInterface#setBytes(int)
    */

    public void setBytes(long bytes)
    {
        this.bytes = bytes;
    }

    /*
    * (non-Javadoc)
    *
    * @see com.izforge.izpack.panels.packs.PacksPanelInterface#showSpaceRequired()
    */

    public void showSpaceRequired()
    {
        if (spaceLabel != null)
        {
            spaceLabel.setText(Pack.toByteUnitsString(bytes));
        }
    }

    /*
    * (non-Javadoc)
    *
    * @see com.izforge.izpack.panels.packs.PacksPanelInterface#showFreeSpace()
    */

    public void showFreeSpace()
    {
        if (IoHelper.supported("getFreeSpace") && freeSpaceLabel != null)
        {
            String msg = null;
            freeBytes = IoHelper.getFreeSpace(IoHelper.existingParent(
                    new File(this.installData.getInstallPath())).getAbsolutePath());
            if (freeBytes < 0)
            {
                msg = installData.getLangpack().getString("PacksPanel.notAscertainable");
            }
            else
            {
                msg = Pack.toByteUnitsString(freeBytes);
            }
            freeSpaceLabel.setText(msg);
        }
    }

    public Debugger getDebugger()
    {
        return null;
    }

    /**
     * Indicates wether the panel has been validated or not.
     *
     * @return true if the needed space is less than the free space, else false
     */
    public boolean isValidated()
    {
        refreshPacksToInstall();
        if (IoHelper.supported("getFreeSpace") && freeBytes >= 0 && freeBytes <= bytes)
        {
            JOptionPane.showMessageDialog(this, installData.getLangpack()
                    .getString("PacksPanel.notEnoughSpace"), installData.getLangpack()
                    .getString("installer.error"), JOptionPane.ERROR_MESSAGE);
            return (false);
        }
        return (true);
    }

    /**
     * Asks to make the XML panel installDataGUI.
     *
     * @param panelRoot The XML tree to write the installDataGUI in.
     */
    public void makeXMLData(IXMLElement panelRoot)
    {
        new ImgPacksPanelAutomationHelper().makeXMLData(this.installData, panelRoot);
    }


    /**
     * This method tries to resolve the localized name of the given pack. If this is not possible,
     * the name given in the installation description file in ELEMENT <pack> will be used.
     *
     * @param pack for which the name should be resolved
     * @return localized name of the pack
     */
    private String getI18NPackName(Pack pack)
    {
        // Internationalization code
        String packName = pack.name;
        String key = pack.id;
        if (langpack != null && pack.id != null && !"".equals(pack.id))
        {
            packName = langpack.getString(key);
        }
        if ("".equals(packName) || key == null || key.equals(packName))
        {
            packName = pack.name;
        }
        return (packName);
    }

    public String getI18NPackName(String packId)
    {
        Pack pack = idToPack.get(packId);
        if (pack == null)
        {
            return packId;
        }
        return getI18NPackName(pack);
    }

    /**
     * Layout helper method:<br>
     * Creates an label with a message given by msgId and an icon given by the iconId. If layout and
     * constraints are not null, the label will be added to layout with the given constraints. The
     * label will be added to this object.
     *
     * @param msgId       identifier for the IzPack langpack
     * @param iconId      identifier for the IzPack icons
     * @param layout      layout to be used
     * @param constraints constraints to be used
     * @return the created label
     */
    protected JLabel createLabel(String msgId, String iconId, GridBagLayout layout,
                                 GridBagConstraints constraints)
    {
        JLabel label = LabelFactory.create(installData.getLangpack().getString(msgId), parent.icons
                .get(iconId), TRAILING);
        if (layout != null && constraints != null)
        {
            layout.addLayoutComponent(label, constraints);
        }
        add(label);
        return (label);
    }

    /**
     * Creates a panel containing a anonymous label on the left with the message for the given msgId
     * and a label on the right side with initial no text. The right label will be returned. If
     * layout and constraints are not null, the label will be added to layout with the given
     * constraints. The panel will be added to this object.
     *
     * @param msgId       identifier for the IzPack langpack
     * @param layout      layout to be used
     * @param constraints constraints to be used
     * @return the created (right) label
     */
    protected JLabel createPanelWithLabel(String msgId, GridBagLayout layout,
                                          GridBagConstraints constraints)
    {
        JPanel panel = new JPanel();
        JLabel label = new JLabel();
        panel.setAlignmentX(LEFT_ALIGNMENT);
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(LabelFactory.create(installData.getLangpack().getString(msgId)));
        panel.add(Box.createHorizontalGlue());
        panel.add(label);
        if (layout != null && constraints != null)
        {
            layout.addLayoutComponent(panel, constraints);
        }
        add(panel);
        return (label);
    }

    private void refreshPacksToInstall()
    {
        this.installData.getSelectedPacks().clear();
        CheckBoxNode rootCheckBoxNode = (CheckBoxNode) getTree().getModel().getRoot();
        Enumeration checkBoxNodeEnumeration = rootCheckBoxNode.depthFirstEnumeration();
        while (checkBoxNodeEnumeration.hasMoreElements())
        {
            CheckBoxNode checkBox = (CheckBoxNode) checkBoxNodeEnumeration.nextElement();
            if (checkBox.isSelected() || checkBox.isPartial())
            {
                this.installData.getSelectedPacks().add(checkBox.getPack());
            }
        }
    }

    /**
     * Creates a text area with standard settings and the title given by the msgId. If scroller is
     * not null, the create text area will be added to the scroller and the scroller to this object,
     * else the text area will be added directly to this object. If layout and constraints are not
     * null, the text area or scroller will be added to layout with the given constraints. The text
     * area will be returned.
     *
     * @param msgId       identifier for the IzPack langpack
     * @param scroller    the scroller to be used
     * @param layout      layout to be used
     * @param constraints constraints to be used
     * @return the created text area
     */
    protected JTextArea createTextArea(String msgId, JScrollPane scroller, GridBagLayout layout,
                                       GridBagConstraints constraints)
    {
        JTextArea area = new JTextArea();
        // area.setMargin(new Insets(2, 2, 2, 2));
        area.setAlignmentX(LEFT_ALIGNMENT);
        area.setCaretPosition(0);
        area.setEditable(false);
        area.setEditable(false);
        area.setOpaque(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createTitledBorder(installData.getLangpack().getString(msgId)));
        area.setFont(getControlTextFont());

        if (layout != null && constraints != null)
        {
            if (scroller != null)
            {
                layout.addLayoutComponent(scroller, constraints);
            }
            else
            {
                layout.addLayoutComponent(area, constraints);
            }
        }
        if (scroller != null)
        {
            scroller.setViewportView(area);
            add(scroller);
        }
        else
        {
            add(area);
        }
        return (area);

    }

    /**
     * FIXME Creates the JTree component and calls all initialization tasks
     *
     * @param width
     * @param scroller
     * @param layout
     * @param constraints
     * @return
     */
    protected JTree createPacksTree(int width, JScrollPane scroller, GridBagLayout layout,
                                    GridBagConstraints constraints)
    {
        JTree tree = new JTree(populateTreePacks(null));
        packsTree = tree;
        tree.setCellRenderer(new CheckBoxNodeRenderer(this));
        tree.setEditable(false);
        tree.setShowsRootHandles(true);
        tree.setRootVisible(false);
        checkTreeController = new CheckTreeController(this);
        tree.addMouseListener(checkTreeController);
        tree.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        tree.setBackground(Color.white);
        tree.setToggleClickCount(0);
        //tree.setRowHeight(0);

        //table.getSelectionModel().addTreeSelectionListener(this);
        scroller.setViewportView(tree);
        scroller.setAlignmentX(LEFT_ALIGNMENT);
        scroller.getViewport().setBackground(Color.white);
        scroller.setPreferredSize(new Dimension(width, (this.installData.guiPrefs.height / 3 + 30)));

        if (layout != null && constraints != null)
        {
            layout.addLayoutComponent(scroller, constraints);
        }
        add(scroller);
        return (tree);
    }

    /**
     * Computes pack related installDataGUI like the names or the dependencies state.
     *
     * @param packs
     */
    private void computePacks(java.util.List packs)
    {
        names = new HashMap<String, Pack>();
        dependenciesExist = false;
        for (Object pack1 : packs)
        {
            Pack pack = (Pack) pack1;
            names.put(pack.name, pack);
            if (pack.dependencies != null || pack.excludeGroup != null)
            {
                dependenciesExist = true;
            }
        }
    }

    /**
     * Refresh tree installDataGUI from the PacksModel. This functions serves as a bridge
     * between the flat PacksModel and the tree installDataGUI model.
     */
    public void fromModel()
    {
        TreeModel model = this.packsTree.getModel();
        CheckBoxNode root = (CheckBoxNode) model.getRoot();
        updateModel(root);
    }

    private int getRowIndex(Pack pack)
    {
        Integer rowNumber = packToRowNumber.get(pack);
        if (rowNumber == null)
        {
            return -1;
        }
        return rowNumber;
    }

    /**
     * Helper function for fromModel() - runs the recursion
     *
     * @param rnode
     */
    private void updateModel(CheckBoxNode rnode)
    {
        int rowIndex = getRowIndex(rnode.getPack());
        if (rowIndex > 0)
        {
            Integer state = (Integer) packsModel.getValueAt(rowIndex, 0);
            if ((state == -2) && rnode.getChildCount() > 0)
            {
                boolean dirty = false;
                Enumeration toBeDeselected = rnode.depthFirstEnumeration();
                while (toBeDeselected.hasMoreElements())
                {
                    CheckBoxNode checkBoxNode = (CheckBoxNode) toBeDeselected.nextElement();
                    boolean chDirty = checkBoxNode.isSelected() || checkBoxNode.isPartial() || checkBoxNode.isEnabled();
                    dirty = dirty || chDirty;
                    if (chDirty)
                    {
                        checkBoxNode.setPartial(false);
                        checkBoxNode.setSelected(false);
                        checkBoxNode.setEnabled(false);
                        setModelValue(checkBoxNode);
                    }
                }
                if (dirty)
                {
                    fromModel();
                }
                return;
            }
        }

        Enumeration e = rnode.children();
        while (e.hasMoreElements())
        {
            Object next = e.nextElement();
            CheckBoxNode cbnode = (CheckBoxNode) next;
            String nodeText = cbnode.getId();
            Object nodePack = idToPack.get(nodeText);
            if (!cbnode.isPartial())
            {
                int childRowIndex = getRowIndex((Pack) nodePack);
                if (childRowIndex >= 0)
                {
                    Integer state = (Integer) packsModel.getValueAt(childRowIndex, 0);
                    cbnode.setEnabled(state >= 0);
                    cbnode.setSelected(Math.abs(state.intValue()) == 1);
                }
            }
            updateModel(cbnode);
        }
    }

    /**
     * Updates a value for pack in PacksModel with installDataGUI from a checkbox node
     *
     * @param cbnode This is the checkbox node which contains model values
     */
    public void setModelValue(CheckBoxNode cbnode)
    {
        String id = cbnode.getId();
        Object nodePack = idToPack.get(id);
        int value = 0;
        if (cbnode.isEnabled() && cbnode.isSelected())
        {
            value = 1;
        }
        if (!cbnode.isEnabled() && cbnode.isSelected())
        {
            value = -1;
        }
        if (!cbnode.isEnabled() && !cbnode.isSelected())
        {
            value = -2;
        }
        int rowIndex = getRowIndex((Pack) nodePack);
        if (rowIndex > 0)
        {
            Integer newValue = value;
            Integer modelValue = (Integer) packsModel.getValueAt(rowIndex, 0);
            if (!newValue.equals(modelValue))
            {
                packsModel.setValueAt(newValue, rowIndex, 0);
            }
        }
    }

    /**
     * Initialize tree model sructures
     */
    private void createTreeData()
    {
        treeData = new HashMap<String, List<String>>();
        idToPack = new HashMap<String, Pack>();

        for (Pack pack : this.installData.getAvailablePacks())
        {
            idToPack.put(pack.id, pack);
            if (pack.parent != null)
            {
                List<String> kids = null;
                if (treeData.containsKey(pack.parent))
                {
                    kids = treeData.get(pack.parent);
                }
                else
                {
                    kids = new ArrayList<String>();
                }
                kids.add(pack.id);
                treeData.put(pack.parent, kids);
            }
        }
    }

    /**
     * Shows and updates the description text in the panel
     *
     * @param id
     */
    public void setDescription(String id)
    {
        if (descriptionArea != null)
        {
            Pack pack = idToPack.get(id);
            String desc = "";
            String key = pack.id + ".description";
            if (langpack != null && pack.id != null && !"".equals(pack.id))
            {
                desc = langpack.getString(key);
            }
            if ("".equals(desc) || key.equals(desc))
            {
                desc = pack.description;
            }
            desc = variableSubstitutor.substitute(desc);
            descriptionArea.setText(desc);
        }
    }

    /**
     * Shows and updates the dependencies text in the panel
     *
     * @param id
     */
    public void setDependencies(String id)
    {
        if (dependencyArea != null)
        {
            Pack pack = idToPack.get(id);
            java.util.List<String> dep = pack.dependencies;
            String list = "";
            if (dep != null)
            {
                list += (langpack == null) ? "Dependencies: " : langpack
                        .getString("PacksPanel.dependencies");
            }
            for (int j = 0; dep != null && j < dep.size(); j++)
            {
                String name = dep.get(j);
                list += getI18NPackName(names.get(name));
                if (j != dep.size() - 1)
                {
                    list += ", ";
                }
            }

            // add the list of the packs to be excluded
            String excludeslist = (langpack == null) ? "Excludes: " : langpack
                    .getString("PacksPanel.excludes");
            int numexcludes = 0;
            int i = getRowIndex(pack);
            if (pack.excludeGroup != null)
            {
                for (int q = 0; q < this.installData.getAvailablePacks().size(); q++)
                {
                    Pack otherpack = this.installData.getAvailablePacks().get(q);
                    String exgroup = otherpack.excludeGroup;
                    if (exgroup != null)
                    {
                        if (q != i && pack.excludeGroup.equals(exgroup))
                        {

                            excludeslist += getI18NPackName(otherpack) + ", ";
                            numexcludes++;
                        }
                    }
                }
            }
            // concatenate
            if (dep != null)
            {
                excludeslist = "    " + excludeslist;
            }
            if (numexcludes > 0)
            {
                list += excludeslist;
            }
            if (list.endsWith(", "))
            {
                list = list.substring(0, list.length() - 2);
            }

            // and display the result
            dependencyArea.setText(list);
        }
    }

    /**
     * Gives a CheckBoxNode instance from the id
     *
     * @param id
     * @return
     */
    public CheckBoxNode getCbnById(String id)
    {
        return this.idToCheckBoxNode.get(id);
    }

    /**
     * Reads the available packs and creates the JTree structure based on
     * the parent definitions.
     *
     * @param parent
     * @return
     */
    private TreeNode populateTreePacks(String parent)
    {
        if (parent == null) // the root node
        {
            List<TreeNode> rootNodes = new ArrayList<TreeNode>();
            for (Pack pack : this.installData.getAvailablePacks())
            {
                if (pack.parent == null)
                {
                    rootNodes.add(populateTreePacks(pack.id));
                }
            }
            TreeNode treeNode = new CheckBoxNode("Root", "Root", rootNodes.toArray(), true);
            return treeNode;
        }
        else
        {
            List<TreeNode> links = new ArrayList<TreeNode>();
            List<String> kids = treeData.get(parent);
            Pack pack = idToPack.get(parent);
            String translated = getI18NPackName(parent);

            if (kids != null)
            {
                for (String kidId : kids)
                {
                    links.add(populateTreePacks(kidId));
                }

                CheckBoxNode checkBoxNode = new CheckBoxNode(parent, translated, links.toArray(), true);
                idToCheckBoxNode.put(checkBoxNode.getId(), checkBoxNode);
                checkBoxNode.setPack(pack);
                checkBoxNode.setTotalSize(pack.nbytes);
                return checkBoxNode;
            }
            else
            {
                CheckBoxNode checkBoxNode = new CheckBoxNode(parent, translated, true);
                idToCheckBoxNode.put(checkBoxNode.getId(), checkBoxNode);
                checkBoxNode.setPack(pack);
                checkBoxNode.setTotalSize(pack.nbytes);
                return checkBoxNode;
            }
        }
    }

    /**
     * Called when the panel becomes active. If a derived class implements this method also, it is
     * recomanded to call this method with the super operator first.
     */
    public void panelActivate()
    {
        try
        {

            // TODO the PacksModel could be patched such that isCellEditable
            // allows returns false. In that case the PacksModel must not be
            // adapted here.
            packsModel = new PacksModel(this, installData, rules)
            {
                /**
                 * Required (serializable)
                 */
                private static final long serialVersionUID = 697462278279845304L;

                public boolean isCellEditable(int rowIndex, int columnIndex)
                {
                    return false;
                }
            };

            //initialize helper map to increa performance
            packToRowNumber = new HashMap<Pack, Integer>();
            for (Pack pack : this.installData.getAvailablePacks())
            {
                packToRowNumber.put(pack, this.installData.getAvailablePacks().indexOf(pack));
            }

            // Init tree structures
            createTreeData();

            // Create panel GUI (and populate the TJtree)
            createNormalLayout();

            // Reload the installDataGUI from the PacksModel into the tree in order the initial
            // dependencies to be resolved and effective
            fromModel();

            // Init the pack sizes (individual and cumulative)
            CheckBoxNode root = (CheckBoxNode) packsTree.getModel().getRoot();
            checkTreeController.updateAllParents(root);
            CheckTreeController.initTotalSize(root, false);

            // Ugly repaint because of a bug in tree.treeDidChange
            packsTree.revalidate();
            packsTree.repaint();

            tableScroller.setColumnHeaderView(null);
            tableScroller.setColumnHeader(null);

            // set the JCheckBoxes to the currently selected panels. The
            // selection might have changed in another panel
            bytes = 0;
            for (Pack pack : this.installData.getAvailablePacks())
            {
                if (pack.required)
                {
                    bytes += pack.nbytes;
                    continue;
                }
                if (this.installData.getSelectedPacks().contains(pack))
                {
                    bytes += pack.nbytes;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        showSpaceRequired();
        showFreeSpace();
    }

    /*
    * (non-Javadoc)
    *
    * @see com.izforge.izpack.installer.IzPanel#getSummaryBody()
    */

    public String getSummaryBody()
    {
        StringBuffer retval = new StringBuffer(256);
        boolean first = true;
        for (Pack pack : this.installData.getSelectedPacks())
        {
            if (!first)
            {
                retval.append("<br>");
            }
            first = false;
            retval.append(getI18NPackName(pack));
        }
        return retval.toString();
    }


    public JTree getTree()
    {
        return packsTree;
    }

}

/**
 * Controller class which handles the mouse clicks on checkbox nodes. Also
 * contains utility methods to update the sizes and the states of the nodes.
 *
 * @author <a href="vralev@redhat.com">Vladimir Ralev</a>
 * @version $Revision: 1.1 $
 */
class CheckTreeController extends MouseAdapter
{
    JTree tree;
    TreePacksPanel treePacksPanel;
    int checkWidth = new JCheckBox().getPreferredSize().width;

    public CheckTreeController(TreePacksPanel p)
    {
        this.tree = p.getTree();
        this.treePacksPanel = p;
    }

    private void selectNode(CheckBoxNode current)
    {
        current.setPartial(false);
        treePacksPanel.setModelValue(current);
        Enumeration e = current.depthFirstEnumeration();
        while (e.hasMoreElements())
        {
            CheckBoxNode child = (CheckBoxNode) e.nextElement();
            child.setSelected(current.isSelected() || child.getPack().required);
            if (!child.isSelected())
            {
                child.setPartial(false);
            }
            treePacksPanel.setModelValue(child);
        }
        treePacksPanel.fromModel();
    }

    private boolean hasExcludes(CheckBoxNode node)
    {
        Enumeration e = node.depthFirstEnumeration();
        while (e.hasMoreElements())
        {
            CheckBoxNode checkBoxNode = (CheckBoxNode) e.nextElement();
            if (checkBoxNode.getPack().excludeGroup != null)
            {
                return true;
            }
        }
        return false;
    }

    public void mouseReleased(MouseEvent me)
    {
        TreePath path = tree.getPathForLocation(me.getX(), me.getY());
        if (path == null)
        {
            return;
        }
        CheckBoxNode current = (CheckBoxNode) path.getLastPathComponent();
        treePacksPanel.setDescription(current.getId());
        treePacksPanel.setDependencies(current.getId());
        if (me.getX() > tree.getPathBounds(path).x + checkWidth)
        {
            return;
        }

        // If this pack is required, leave it alone
        if (current.getPack().required)
        {
            return;
        }

        boolean currIsSelected = current.isSelected() & !current.isPartial();
        boolean currIsPartial = current.isPartial();
        boolean currHasExcludes = hasExcludes(current);
        CheckBoxNode root = (CheckBoxNode) current.getRoot();

        if (currIsPartial && currHasExcludes)
        {
            current.setSelected(false);
            selectNode(current); // deselect actually
            updateAllParents(root);
        }
        else
        {
            if (!currIsSelected)
            {
                selectAllChildNodes(current);
            }
            current.setSelected(!currIsSelected);
            selectNode(current);
            updateAllParents(root);
        }

        initTotalSize(root, true);

        // must override the bytes being computed at packsModel
        treePacksPanel.setBytes((int) root.getTotalSize());
        treePacksPanel.showSpaceRequired();
        tree.treeDidChange();
    }

    public void selectAllChildNodes(CheckBoxNode cbn)
    {
        Enumeration e = cbn.children();
        while (e.hasMoreElements())
        {
            CheckBoxNode subCbn = (CheckBoxNode) e.nextElement();
            selectAllDependencies(subCbn);
            if (subCbn.getChildCount() > 0)
            {
                selectAllChildNodes(subCbn);
            }

            subCbn.setSelected(true);
            // we need this, because the setModel ignored disabled values
            subCbn.setEnabled(true);
            treePacksPanel.setModelValue(subCbn);
            subCbn.setEnabled(!subCbn.getPack().required);
        }
    }

    public void selectAllDependencies(CheckBoxNode cbn)
    {
        Pack pack = cbn.getPack();
        java.util.List<String> deps = pack.getDependencies();
        if (deps == null)
        {
            return;
        }
        for (String depId : deps)
        {
            CheckBoxNode depCbn = treePacksPanel.getCbnById(depId);
            selectAllDependencies(depCbn);
            if (depCbn.getChildCount() > 0)
            {
                if (!depCbn.isSelected() || depCbn.isPartial())
                {
                    selectAllChildNodes(depCbn);
                }
            }
            depCbn.setSelected(true);
            // we need this, because the setModel ignored disabled values
            depCbn.setEnabled(true);
            treePacksPanel.setModelValue(depCbn);
            depCbn.setEnabled(!depCbn.getPack().required);
        }
    }

    /**
     * Updates partial/deselected/selected state of all parent nodes.
     * This is needed and is a patch to allow unrelated nodes (in terms of the tree)
     * to fire updates for each other.
     *
     * @param root
     */
    public void updateAllParents(CheckBoxNode root)
    {
        Enumeration rootEnum = root.depthFirstEnumeration();
        while (rootEnum.hasMoreElements())
        {
            CheckBoxNode child = (CheckBoxNode) rootEnum.nextElement();
            if (child.getParent() != null && !child.getParent().equals(root))
            {
                updateParents(child);
            }
        }
    }

    /**
     * Updates the parents of this particular node
     *
     * @param node
     */
    private void updateParents(CheckBoxNode node)
    {
        CheckBoxNode parent = (CheckBoxNode) node.getParent();
        if (parent != null && !parent.equals(parent.getRoot()))
        {
            Enumeration ne = parent.children();
            boolean allSelected = true;
            boolean allDeselected = true;
            while (ne.hasMoreElements())
            {
                CheckBoxNode child = (CheckBoxNode) ne.nextElement();
                if (child.isSelected())
                {
                    allDeselected = false;
                }
                else
                {
                    allSelected = false;
                }
                if (child.isPartial())
                {
                    allSelected = allDeselected = false;
                }
                if (!allSelected && !allDeselected)
                {
                    break;
                }
            }
            if (parent.getChildCount() > 0)
            {
                if (!allSelected && !allDeselected)
                {
                    setPartialParent(parent);
                }
                else
                {
                    parent.setPartial(false);
                }
                if (allSelected)
                {
                    parent.setSelected(true);
                }
                if (allDeselected)
                {
                    parent.setSelected(false);
                }
                treePacksPanel.setModelValue(parent);
                if (allSelected || allDeselected)
                {
                    updateParents(parent);
                }
            }
            //updateTotalSize(node);
        }
    }

    public static void setPartialParent(CheckBoxNode node)
    {
        node.setPartial(true);
        CheckBoxNode parent = (CheckBoxNode) node.getParent();
        if (parent != null && !parent.equals(parent.getRoot()))
        {
            setPartialParent(parent);
        }
    }

    public static long initTotalSize(CheckBoxNode node, boolean markChanged)
    {
        if (node.isLeaf())
        {
            return node.getPack().nbytes;
        }
        Enumeration e = node.children();
        Pack nodePack = node.getPack();
        long bytes = 0;
        if (nodePack != null)
        {
            bytes = nodePack.nbytes;
        }
        while (e.hasMoreElements())
        {
            CheckBoxNode checkBoxNode = (CheckBoxNode) e.nextElement();
            long size = initTotalSize(checkBoxNode, markChanged);
            if (checkBoxNode.isSelected() || checkBoxNode.isPartial())
            {
                bytes += size;
            }
        }
        if (markChanged)
        {
            long old = node.getTotalSize();
            if (old != bytes)
            {
                node.setTotalSizeChanged(true);
            }
            else
            {
                node.setTotalSizeChanged(false);
            }
        }
        node.setTotalSize(bytes);
        return bytes;
    }
}