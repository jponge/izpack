/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2007 Vladimir Ralev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.panels;

import com.izforge.izpack.LocaleDatabase;
import com.izforge.izpack.Pack;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.installer.*;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.IoHelper;
import com.izforge.izpack.util.VariableSubstitutor;
import net.n3.nanoxml.XMLElement;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.List;

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
    private HashMap<String, ArrayList<String>> treeData;
    private HashMap<Pack, Integer> packToRowNumber;

    private HashMap<String, CheckBoxNode> idToCheckBoxNode = new HashMap<String, CheckBoxNode>();
    //private boolean created = false;   // UNUSED

    private CheckTreeController checkTreeController;

    /**
     * The constructor.
     *
     * @param parent The parent window.
     * @param idata  The installation data.
     */
    public TreePacksPanel(InstallerFrame parent, InstallData idata)
    {
        super(parent, idata);
        // Load langpack.
        try
        {
            this.langpack = parent.langpack;
            InputStream langPackStream;
            String webdir = idata.info.getWebDirURL();
            if (webdir != null)
            {
                try
                {
                    java.net.URL url = new java.net.URL(webdir + "/langpacks/" + LANG_FILE_NAME + idata.localeISO3);
                    langPackStream = new WebAccessor(null).openInputStream(url);
                }
                catch (Exception e)
                {
                    langPackStream = ResourceManager.getInstance().getInputStream(LANG_FILE_NAME);
                }
            }
            else
            {
                langPackStream = ResourceManager.getInstance().getInputStream(LANG_FILE_NAME);
            }

            this.langpack.add(langPackStream);
            langPackStream.close();
        }
        catch (Throwable exception)
        {
            Debug.trace(exception);
        }

        // init the map
        computePacks(idata.availablePacks);

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
    * @see com.izforge.izpack.panels.PacksPanelInterface#getLangpack()
    */
    public LocaleDatabase getLangpack()
    {
        return (langpack);
    }

    /*
    * (non-Javadoc)
    * 
    * @see com.izforge.izpack.panels.PacksPanelInterface#getBytes()
    */
    public long getBytes()
    {
        return (bytes);
    }

    /*
    * (non-Javadoc)
    * 
    * @see com.izforge.izpack.panels.PacksPanelInterface#setBytes(int)
    */
    public void setBytes(long bytes)
    {
        this.bytes = bytes;
    }

    /*
    * (non-Javadoc)
    * 
    * @see com.izforge.izpack.panels.PacksPanelInterface#showSpaceRequired()
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
    * @see com.izforge.izpack.panels.PacksPanelInterface#showFreeSpace()
    */
    public void showFreeSpace()
    {
        if (IoHelper.supported("getFreeSpace") && freeSpaceLabel != null)
        {
            String msg = null;
            freeBytes = IoHelper.getFreeSpace(IoHelper.existingParent(
                    new File(idata.getInstallPath())).getAbsolutePath());
            if (freeBytes < 0)
            {
                msg = parent.langpack.getString("PacksPanel.notAscertainable");
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
            JOptionPane.showMessageDialog(this, parent.langpack
                    .getString("PacksPanel.notEnoughSpace"), parent.langpack
                    .getString("installer.error"), JOptionPane.ERROR_MESSAGE);
            return (false);
        }
        return (true);
    }

    /**
     * Asks to make the XML panel data.
     *
     * @param panelRoot The XML tree to write the data in.
     */
    public void makeXMLData(XMLElement panelRoot)
    {
        new ImgPacksPanelAutomationHelper().makeXMLData(idata, panelRoot);
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
        JLabel label = LabelFactory.create(parent.langpack.getString(msgId), parent.icons
                .getImageIcon(iconId), TRAILING);
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
        if (label == null)
        {
            label = new JLabel("");
        }
        panel.setAlignmentX(LEFT_ALIGNMENT);
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(LabelFactory.create(parent.langpack.getString(msgId)));
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
        idata.selectedPacks.clear();
        CheckBoxNode cbn = (CheckBoxNode) getTree().getModel().getRoot();
        Enumeration e = cbn.depthFirstEnumeration();
        while (e.hasMoreElements())
        {
            CheckBoxNode c = (CheckBoxNode) e.nextElement();
            if (c.isSelected() || c.isPartial())
            {
                idata.selectedPacks.add(c.getPack());
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
        area.setBorder(BorderFactory.createTitledBorder(parent.langpack.getString(msgId)));
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
        JTree tree = new JTree((CheckBoxNode) populateTreePacks(null));
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
        scroller.setPreferredSize(new Dimension(width, (idata.guiPrefs.height / 3 + 30)));

        if (layout != null && constraints != null)
        {
            layout.addLayoutComponent(scroller, constraints);
        }
        add(scroller);
        return (tree);
    }

    /**
     * Computes pack related data like the names or the dependencies state.
     *
     * @param packs
     */
    private void computePacks(List packs)
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
     * Refresh tree data from the PacksModel. This functions serves as a bridge
     * between the flat PacksModel and the tree data model.
     */
    public void fromModel()
    {
        TreeModel model = this.packsTree.getModel();
        CheckBoxNode root = (CheckBoxNode) model.getRoot();
        updateModel(root);
    }

    private int getRowIndex(Pack pack)
    {
        Object o = packToRowNumber.get(pack);
        if (o == null)
        {
            return -1;
        }
        Integer ret = (Integer) o;
        return ret;
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
                    CheckBoxNode cbn = (CheckBoxNode) toBeDeselected.nextElement();
                    boolean chDirty = cbn.isSelected() || cbn.isPartial() || cbn.isEnabled();
                    dirty = dirty || chDirty;
                    if (chDirty)
                    {
                        cbn.setPartial(false);
                        cbn.setSelected(false);
                        cbn.setEnabled(false);
                        setModelValue(cbn);
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
                if (childRowIndex > 0)
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
     * Updates a value for pack in PacksModel with data from a checkbox node
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
        treeData = new HashMap<String, ArrayList<String>>();
        idToPack = new HashMap<String, Pack>();

        java.util.Iterator iter = idata.availablePacks.iterator();
        while (iter.hasNext())
        {
            Pack p = (Pack) iter.next();
            idToPack.put(p.id, p);
            if (p.parent != null)
            {
                ArrayList<String> kids = null;
                if (treeData.containsKey(p.parent))
                {
                    kids = treeData.get(p.parent);
                }
                else
                {
                    kids = new ArrayList<String>();
                }
                kids.add(p.id);
                treeData.put(p.parent, kids);
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
        VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
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
            desc = vs.substitute(desc, null);
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
            List<String> dep = pack.dependencies;
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
                for (int q = 0; q < idata.availablePacks.size(); q++)
                {
                    Pack otherpack = (Pack) idata.availablePacks.get(q);
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
    private Object populateTreePacks(String parent)
    {
        if (parent == null) // the root node
        {
            java.util.Iterator iter = idata.availablePacks.iterator();
            ArrayList rootNodes = new ArrayList();
            while (iter.hasNext())
            {
                Pack p = (Pack) iter.next();
                if (p.parent == null)
                {
                    rootNodes.add(populateTreePacks(p.id));
                }
            }
            TreeNode nv = new CheckBoxNode("Root", "Root", rootNodes.toArray(), true);
            return nv;
        }
        else
        {
            ArrayList links = new ArrayList();
            Object kidsObject = treeData.get(parent);
            Pack p = idToPack.get(parent);
            String translated = getI18NPackName(parent);

            if (kidsObject != null)
            {
                ArrayList kids = (ArrayList) kidsObject;
                for (Object kid : kids)
                {
                    String kidId = (String) kid;
                    links.add(populateTreePacks(kidId));
                }

                CheckBoxNode cbn = new CheckBoxNode(parent, translated, links.toArray(), true);
                idToCheckBoxNode.put(cbn.getId(), cbn);
                cbn.setPack(p);
                cbn.setTotalSize(p.nbytes);
                return cbn;
            }
            else
            {
                CheckBoxNode cbn = new CheckBoxNode(parent, translated, true);
                idToCheckBoxNode.put(cbn.getId(), cbn);
                cbn.setPack(p);
                cbn.setTotalSize(p.nbytes);
                return cbn;
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
            packsModel = new PacksModel(this, idata, this.parent.getRules())
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
            java.util.Iterator rowpack = idata.availablePacks.iterator();
            while (rowpack.hasNext())
            {
                Pack p = (Pack) rowpack.next();
                packToRowNumber.put(p, idata.availablePacks.indexOf(p));
            }

            // Init tree structures
            createTreeData();

            // Create panel GUI (and populate the TJtree)
            createNormalLayout();

            // Reload the data from the PacksModel into the tree in order the initial
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
            java.util.Iterator iter = idata.availablePacks.iterator();
            bytes = 0;
            while (iter.hasNext())
            {
                Pack p = (Pack) iter.next();
                if (p.required)
                {
                    bytes += p.nbytes;
                    continue;
                }
                if (idata.selectedPacks.contains(p))
                {
                    bytes += p.nbytes;
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
        Iterator iter = idata.selectedPacks.iterator();
        boolean first = true;
        while (iter.hasNext())
        {
            if (!first)
            {
                retval.append("<br>");
            }
            first = false;
            Pack pack = (Pack) iter.next();
            if (langpack != null && pack.id != null && !"".equals(pack.id))
            {
                retval.append(langpack.getString(pack.id));
            }
            else
            {
                retval.append(pack.name);
            }
        }
        return (retval.toString());
    }


    public JTree getTree()
    {
        return packsTree;
    }

}

/**
 * The renderer model for individual checkbox nodes in a JTree. It renders the
 * checkbox and a label for the pack size.
 *
 * @author <a href="vralev@redhat.com">Vladimir Ralev</a>
 * @version $Revision: 1.1 $
 */
class CheckBoxNodeRenderer implements TreeCellRenderer
{
    private static final JPanel rendererPanel = new JPanel();
    private static final JLabel packSizeLabel = new JLabel();
    private static final JCheckBox checkbox = new JCheckBox();
    private static final JCheckBox normalCheckBox = new JCheckBox();
    private static final java.awt.Font normalFont = new JCheckBox().getFont();
    private static final java.awt.Font boldFont = new java.awt.Font(normalFont.getFontName(),
            java.awt.Font.BOLD,
            normalFont.getSize());
    private static final java.awt.Font plainFont = new java.awt.Font(normalFont.getFontName(),
            java.awt.Font.PLAIN,
            normalFont.getSize());
    private static final Color annotationColor = new Color(0, 0, 120); // red
    private static final Color changedColor = new Color(200, 0, 0);

    private static Color selectionForeground, selectionBackground,
            textForeground, textBackground;

    TreePacksPanel treePacksPanel;

    public CheckBoxNodeRenderer(TreePacksPanel t)
    {
        selectionForeground = UIManager.getColor("Tree.selectionForeground");
        selectionBackground = UIManager.getColor("Tree.selectionBackground");
        textForeground = UIManager.getColor("Tree.textForeground");
        textBackground = UIManager.getColor("Tree.textBackground");
        treePacksPanel = t;

        int treeWidth = t.getTree().getPreferredSize().width;
        int height = checkbox.getPreferredSize().height;
        int cellWidth = treeWidth - treeWidth / 4;

        //Don't touch, it fixes various layout bugs in swing/awt
        rendererPanel.setLayout(new java.awt.BorderLayout(0, 0));
        rendererPanel.setBackground(textBackground);
        rendererPanel.add(java.awt.BorderLayout.WEST, checkbox);

        rendererPanel.setAlignmentX((float) 0);
        rendererPanel.setAlignmentY((float) 0);
        rendererPanel.add(java.awt.BorderLayout.EAST, packSizeLabel);

        rendererPanel.setMinimumSize(new Dimension(cellWidth, height));
        rendererPanel.setPreferredSize(new Dimension(cellWidth, height));
        rendererPanel.setSize(new Dimension(cellWidth, height));

        rendererPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean selected, boolean expanded, boolean leaf, int row,
                                                  boolean hasFocus)
    {
        treePacksPanel.fromModel();

        if (selected)
        {
            checkbox.setForeground(selectionForeground);
            checkbox.setBackground(selectionBackground);
            rendererPanel.setForeground(selectionForeground);
            rendererPanel.setBackground(selectionBackground);
            packSizeLabel.setBackground(selectionBackground);
        }
        else
        {
            checkbox.setForeground(textForeground);
            checkbox.setBackground(textBackground);
            rendererPanel.setForeground(textForeground);
            rendererPanel.setBackground(textBackground);
            packSizeLabel.setBackground(textBackground);
        }

        if ((value != null) && (value instanceof CheckBoxNode))
        {
            CheckBoxNode node = (CheckBoxNode) value;

            if (node.isTotalSizeChanged())
            {
                packSizeLabel.setForeground(changedColor);
            }
            else
            {
                if (selected)
                {
                    packSizeLabel.setForeground(selectionForeground);
                }
                else
                {
                    packSizeLabel.setForeground(annotationColor);
                }
            }

            checkbox.setText(node.getTranslatedText());

            packSizeLabel.setText(Pack.toByteUnitsString(node.getTotalSize()));

            if (node.isPartial())
            {
                checkbox.setSelected(false);
            }
            else
            {
                checkbox.setSelected(node.isSelected());
            }

            checkbox.setEnabled(node.isEnabled());
            packSizeLabel.setEnabled(node.isEnabled());

            if (node.getChildCount() > 0)
            {
                checkbox.setFont(boldFont);
                packSizeLabel.setFont(boldFont);
            }
            else
            {
                checkbox.setFont(normalFont);
                packSizeLabel.setFont(plainFont);
            }

            if (node.isPartial())
            {
                checkbox.setIcon(new PartialIcon());
            }
            else
            {
                checkbox.setIcon(normalCheckBox.getIcon());
            }
        }
        return rendererPanel;
    }

    public Component getCheckRenderer()
    {
        return rendererPanel;
    }

}

/**
 * The model structure for a JTree node.
 *
 * @author <a href="vralev@redhat.com">Vladimir Ralev</a>
 * @version $Revision: 1.1 $
 */
class CheckBoxNode extends DefaultMutableTreeNode
{

    /**
     * Required (serializable)
     */
    private static final long serialVersionUID = 8743154051564336973L;
    String id;
    boolean selected;
    boolean partial;
    boolean enabled;
    boolean totalSizeChanged;
    String translatedText;
    Pack pack;
    long totalSize;

    public CheckBoxNode(String id, String translated, boolean selected)
    {
        this.id = id;
        this.selected = selected;
        this.translatedText = translated;
    }

    public CheckBoxNode(String id, String translated, Object elements[], boolean selected)
    {
        this.id = id;
        this.translatedText = translated;
        for (int i = 0, n = elements.length; i < n; i++)
        {
            CheckBoxNode tn = (CheckBoxNode) elements[i];
            add(tn);
        }
    }

    public boolean isLeaf()
    {
        return this.getChildCount() == 0;
    }

    public boolean isSelected()
    {
        return selected;
    }

    public void setSelected(boolean newValue)
    {
        selected = newValue;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String newValue)
    {
        id = newValue;
    }

    public String toString()
    {
        return getClass().getName() + "[" + id + "/" + selected + "]";
    }

    public boolean isPartial()
    {
        return partial;
    }

    public void setPartial(boolean partial)
    {
        this.partial = partial;
        if (partial)
        {
            setSelected(true);
        }
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public String getTranslatedText()
    {
        return translatedText;
    }

    public void setTranslatedText(String translatedText)
    {
        this.translatedText = translatedText;
    }

    public Pack getPack()
    {
        return pack;
    }

    public void setPack(Pack pack)
    {
        this.pack = pack;
    }

    public long getTotalSize()
    {
        return totalSize;
    }

    public void setTotalSize(long totalSize)
    {
        this.totalSize = totalSize;
    }

    public boolean isTotalSizeChanged()
    {
        return totalSizeChanged;
    }

    public void setTotalSizeChanged(boolean totalSizeChanged)
    {
        this.totalSizeChanged = totalSizeChanged;
    }
}

/**
 * Special checkbox icon which shows partially selected nodes.
 *
 * @author <a href="vralev@redhat.com">Vladimir Ralev</a>
 * @version $Revision: 1.1 $
 */
class PartialIcon implements Icon
{
    protected int getControlSize()
    {
        return 13;
    }

    public void paintIcon(Component c, Graphics g, int x, int y)
    {
        int controlSize = getControlSize();
        g.setColor(MetalLookAndFeel.getControlShadow());
        g.fillRect(x, y, controlSize - 1, controlSize - 1);
        drawBorder(g, x, y, controlSize, controlSize);

        g.setColor(Color.green);
        drawCheck(c, g, x, y);
    }

    private void drawBorder(Graphics g, int x, int y, int w, int h)
    {
        g.translate(x, y);

        // outer frame rectangle
        g.setColor(MetalLookAndFeel.getControlDarkShadow());
        g.setColor(new Color(0.4f, 0.4f, 0.4f));
        g.drawRect(0, 0, w - 2, h - 2);

        // middle frame
        g.setColor(MetalLookAndFeel.getControlHighlight());
        g.setColor(new Color(0.6f, 0.6f, 0.6f));
        g.drawRect(1, 1, w - 2, h - 2);

        // background
        g.setColor(new Color(0.99f, 0.99f, 0.99f));
        g.fillRect(2, 2, w - 3, h - 3);

        //some extra lines for FX
        g.setColor(MetalLookAndFeel.getControl());
        g.drawLine(0, h - 1, 1, h - 2);
        g.drawLine(w - 1, 0, w - 2, 1);
        g.translate(-x, -y);
    }

    protected void drawCheck(Component c, Graphics g, int x, int y)
    {
        int controlSize = getControlSize();
        g.setColor(new Color(0.0f, 0.7f, 0.0f));

        g.fillOval(x + controlSize / 2 - 2, y + controlSize / 2 - 2, 6, 6);
    }

    public int getIconWidth()
    {
        return getControlSize();
    }

    public int getIconHeight()
    {
        return getControlSize();
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
            CheckBoxNode cbn = (CheckBoxNode) e.nextElement();
            if (cbn.getPack().excludeGroup != null)
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
        List<String> deps = pack.getDependencies();
        if (deps == null)
        {
            return;
        }
        Iterator<String> e = deps.iterator();
        while (e.hasNext())
        {
            String depId = e.next();
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
            CheckBoxNode c = (CheckBoxNode) e.nextElement();
            long size = initTotalSize(c, markChanged);
            if (c.isSelected() || c.isPartial())
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