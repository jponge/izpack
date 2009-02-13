/**
 * File Name: PanelAction.java
 * 
 * Copyright (c) 2009 BISON Schweiz AG, All Rights Reserved.
 *
 * Version: $Id: PanelAction.java,v 1.1.2.1 2009/02/12 12:21:01 blf Exp $
 */
package com.izforge.izpack.installer;

import com.izforge.izpack.util.AbstractUIHandler;

/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/ http://izpack.codehaus.org/
 * 
 * Copyright 2008 Florian Bühlmann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
public interface PanelAction
{

    /**
     * tag-name of the panel action
     */
    public static final String PANEL_ACTIONS_TAG = "actions";

    /**
     * tag-name of the panel action
     */
    public static final String PANEL_ACTION_TAG = "action";

    /**
     * attribute for the stage of the action
     */
    public static final String PANEL_ACTION_STAGE_TAG = "stage";

    /**
     * pre panel activation stage
     */
    public static enum ActionStage {
        preconstruct, preactivate, prevalidate, postvalidate
    }

    /**
     * attribute for class to use
     */
    public static final String PANEL_ACTION_CLASSNAME_TAG = "classname";

    /**
     * @param adata - AutomatedInstallData instance
     * @param handler - actual UIHandler (NOTE: on a preconstruct action the handler is null because
     * it is not available until it is constructed)
     */
    public void executeAction(final AutomatedInstallData adata, AbstractUIHandler handler);
}
