/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2004 Klaus Bartz
 *
 *  File :               ChmodCompilerListener.java
 *  Description :        Example for custom action for packaging time.
 *  Author's email :     klaus.bartz@coi.de
 *  Author's Website :   http://www.coi.de/
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.myCompany.tools.install.listener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import net.n3.nanoxml.XMLElement;
import com.izforge.izpack.compiler.SimpleCompilerListener;
import com.izforge.izpack.compiler.CompilerException;


/**
 * <p>CompilerListener for file and directory permissions.</p>
 *
 * @author  Klaus Bartz
 *
 */
public class ChmodCompilerListener extends SimpleCompilerListener
{


  /* (non-Javadoc)
   * @see com.izforge.izpack.compiler.CompilerListener#reviseAdditionalDataMap(java.util.Map, net.n3.nanoxml.XMLElement)
   */
  public Map reviseAdditionalDataMap(Map existentDataMap, XMLElement element)
    throws CompilerException
  {
    Map retval = existentDataMap != null ? 
      existentDataMap : new  HashMap();
    Vector dataList = element.getChildrenNamed("additionaldata");
    Iterator iter = null;
    if( dataList == null ||  dataList.size() == 0  )
      return( existentDataMap);
    iter = dataList.iterator();
    while( iter.hasNext() )
    {
      XMLElement data = (XMLElement) iter.next();
      String [] relevantKeys = { "permission.dir", "permission.file"};
      for( int i = 0; i < relevantKeys.length; ++i )
      {
        String key = data.getAttribute("key");
        if( key.equalsIgnoreCase(relevantKeys[i]))
        {
          String value = data.getAttribute("value");
          if (value == null || value.length() == 0)
            continue;
          try
          {
            int radix = value.startsWith("0") ? 8 : 10;
            retval.put(key,Integer.valueOf(value, radix));
          } catch (NumberFormatException x)
          {
            throw new CompilerException("'" + relevantKeys[i] + "' must be an integer");
          }
        }
      }
    }
    return retval;
  }
}
