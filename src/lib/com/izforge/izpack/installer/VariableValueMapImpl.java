
 /**
 * A Properties based implementation for VariableValueMap interface.
 *
 * @author Johannes Lehtinen <johannes.lehtinen@iki.fi>
 * @version $Revision$
 */
package com.izforge.izpack.installer;

import java.util.Properties;

public  final class VariableValueMapImpl extends Properties implements VariableValueMap {

    public String getVariable(String var) {
        return getProperty(var);
    }

    public void setVariable(String var, String val) {
        setProperty(var, val);
    }
}
