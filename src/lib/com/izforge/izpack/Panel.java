/*
 * Created on Jan 30, 2004
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.izforge.izpack;

import java.io.Serializable;
import java.util.List;

/**
 * @author Jan Blok
 */
public class Panel implements Serializable
{
	/**  The panel classname. */
	public String className;

	/**  The target operation system of this panel */
	public List osConstraints = null;

}
