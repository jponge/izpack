/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package com.izforge.izpack.installer.requirement;

import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.api.installer.RequirementChecker;

public class LangPackChecker implements RequirementChecker
{
    private final ResourceManager resources;

    public LangPackChecker(ResourceManager resources)
    {
        this.resources = resources;
    }

    /**
     * Determines if installation requirements are met.
     *
     * @return <tt>true</tt> if requirements are met, otherwise <tt>false</tt>
     */
    @Override
    public boolean check()
    {
        return !resources.getAvailableLangPacks().isEmpty();
    }
}
