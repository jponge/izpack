/*
 * IzPack version 3.1.0 pre2 (build 2002.10.19)
 * Copyright (C) 2002 Elmar Grom
 *
 * File :               ShortcutData.java
 * Description :        This class is used as data structure in ShortcutPanel
 * Author's email :     elmar@grom.net
 * Author's Website :   http://www.izforge.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package   com.izforge.izpack.panels;


/*---------------------------------------------------------------------------*/
/**
 * This class serves as a data structure in <code>{@link com.izforge.izpack.panels.ShortcutPanel}</code>
 *
 * @version  0.0.1 / 4/1/02
 * @author   Elmar Grom
 */
/*---------------------------------------------------------------------------*/
public class ShortcutData implements Cloneable
{
  public String   name;
  public String   description;
  public String   target;
  public String   commandLine;
  public int      type;
  public boolean  addToGroup  = false;
  public String   subgroup;
  public String   iconFile;
  public int      iconIndex;
  public int      initialState;
  public String   workingDirectory;
  
  public String   deskTopEntryLinux_MimeType;
  public String   deskTopEntryLinux_Terminal;
  public String   deskTopEntryLinux_TerminalOptions;
  public String   deskTopEntryLinux_Type;
  public String   deskTopEntryLinux_URL;
  public String   deskTopEntryLinux_Encoding;
  public String   deskTopEntryLinux_X_KDE_SubstituteUID;   
  
 /*--------------------------------------------------------------------------*/
 /**
  * Returns a clone (copy) of this object.
  *
  * @return    a copy of this object
 * @throws CloneNotSupportedException
  */
 /*--------------------------------------------------------------------------*/
  public Object clone () throws OutOfMemoryError
  {
    ShortcutData result = new ShortcutData ();

    result.type              = type;
    result.iconIndex         = iconIndex;
    result.initialState      = initialState;
    result.addToGroup        = addToGroup;

    result.name              = cloneString (name);
    result.description       = cloneString (description);
    result.target            = cloneString (target);
    result.commandLine       = cloneString (commandLine);
    result.subgroup          = cloneString (subgroup);
    result.iconFile          = cloneString (iconFile);
    result.workingDirectory  = cloneString (workingDirectory);
    result.deskTopEntryLinux_MimeType = cloneString( deskTopEntryLinux_MimeType );    
    result.deskTopEntryLinux_Terminal = cloneString( deskTopEntryLinux_Terminal );
    result.deskTopEntryLinux_TerminalOptions = cloneString( deskTopEntryLinux_TerminalOptions );
    result.deskTopEntryLinux_Type = cloneString( deskTopEntryLinux_Type );
    result.deskTopEntryLinux_URL = cloneString( deskTopEntryLinux_URL );
    result.deskTopEntryLinux_Encoding = cloneString( deskTopEntryLinux_Encoding );
    result.deskTopEntryLinux_X_KDE_SubstituteUID = cloneString( deskTopEntryLinux_X_KDE_SubstituteUID );
    return (result);
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Clones a <code>String</code>, that is it makes a copy of the content, not
  * of the reference. In addition, if the original is <code>null</code>
  * then an empty <code>String</code> is returned rather than <code>null</code>. 
  *
  * @param     original   the <code>String</code> to clone 
  *
  * @return    a clone of the original
  */
 /*--------------------------------------------------------------------------*/
  private String cloneString (String original)
  {
    if (original == null)
    {
      return ("");
    }
    else
    {
      return (new String (original));
    }
  }
}
/*---------------------------------------------------------------------------*/

