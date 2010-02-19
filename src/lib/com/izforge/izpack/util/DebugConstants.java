/*
 * $Id:$
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://www.izforge.com/izpack/
 * http://izpack.codehaus.org/
 *
 * Copyright 2007 Klaus Bartz
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
package com.izforge.izpack.util;

/**
 * This interface contains common used templates and channel names for debug messages using class
 * {@link com.izforge.izpack.util.Log <code>LogMessage</code>}
 *
 * @author Klaus Bartz
 */
public interface DebugConstants
{

    /**
     * Channel name for debug message about panel tracing.
     */
    public final static String PANEL_TRACE = "PanelTrace";

    /**
     * Channel name for debug message about layout tracing.
     */
    public final static String LAYOUT_TRACE = "LayoutTrace";

    /**
     * Channel name for debug message which will be triggered by the old logging system (not yet implemented).
     */
    public final static String OLD_DEBUG_TRACE = "DebugTrace";

    /**
     * Channel name for debug message which will be triggered by the old logging system (not yet implemented).
     */
    public final static String OLD_DEBUG_STACKTRACE = "DebugStackTrace";
}
