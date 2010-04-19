package com.izforge.izpack.panels.treepacks;

import com.izforge.izpack.api.data.Pack;

import javax.swing.tree.DefaultMutableTreeNode;

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
            CheckBoxNode checkBoxNode = (CheckBoxNode) elements[i];
            add(checkBoxNode);
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
