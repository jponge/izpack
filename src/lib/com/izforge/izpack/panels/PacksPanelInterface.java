/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2004 Gaganis Giorgos
 *
 *  File :               PacksPanelInterface.java
 *  Description :        Provides a to access both PacksPanel & ImgPacksPanel.
 *  Author's email :     gaganis@users.berlios.de
 *  Author's Website :   http://www.izforge.com
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
package com.izforge.izpack.panels;

import com.izforge.izpack.LocaleDatabase;

/**
 * This interface is used to be able to access the common information
 * in the PackPanel and the ImgPacksPAnel through a common type.
 * I introduced it so that I can remove the duplicate PacksModel from each
 * class and create a common one for both.
 *
 * This could be avoided by inheriting ImgPacksPanel from PacksPanel
 *
 * User: Gaganis Giorgos
 * Date: Sep 17, 2004
 * Time: 8:29:22 AM
 */

/*
 * @todo evaluate whether we want to eliminate this interface with
 * inheritance
 */
public interface PacksPanelInterface {

    public LocaleDatabase getLangpack();

    public int getBytes();

    public void setBytes(int bytes);

    public void showSpaceRequired();
}
