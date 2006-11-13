package com.izforge.izpack.rules;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.izforge.izpack.Pack;
import com.izforge.izpack.util.Debug;

import net.n3.nanoxml.XMLElement;

/**
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 * @version $Id: PackselectionCondition.java,v 1.1 2006/11/03 13:03:26 dennis Exp $
 */
public class PackselectionCondition extends Condition {

    protected String packid;

    /**
     *
     */
    public PackselectionCondition() {
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
    * @see de.reddot.installer.rules.Condition#isTrue(java.util.Properties)
    */
    public boolean isTrue(Properties variables) {
        // no information about selected packs given, so return false
        return false;
    }

    /* (non-Javadoc)
    * @see de.reddot.installer.rules.Condition#readFromXML(net.n3.nanoxml.XMLElement)
    */
    public void readFromXML(XMLElement xmlcondition) {
        try {
            this.packid = xmlcondition.getFirstChildNamed("packid").getContent();
        }
        catch (Exception e) {
            Debug.log("missing element in <condition type=\"variable\"/>");
        }
    }

    public boolean isTrue(Properties variables, List selectedpacks) {
        if (selectedpacks != null) {
            for (Iterator iter = selectedpacks.iterator(); iter.hasNext();) {
                Pack p = (Pack) iter.next();
                if (packid.equals(p.id)) {
                    // pack is selected
                    return true;
                }
            }
        }
        // pack is not selected
        return false;
    }

}
