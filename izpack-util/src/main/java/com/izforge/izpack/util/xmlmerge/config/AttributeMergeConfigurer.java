/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2009 Laurent Bovet, Alex Mathey
 * Copyright 2010 Rene Krell
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

package com.izforge.izpack.util.xmlmerge.config;

import com.izforge.izpack.util.xmlmerge.*;
import com.izforge.izpack.util.xmlmerge.action.*;
import com.izforge.izpack.util.xmlmerge.factory.*;
import com.izforge.izpack.util.xmlmerge.mapper.NamespaceFilterMapper;
import com.izforge.izpack.util.xmlmerge.matcher.*;

/**
 * Configure to apply actions declared as attributes in the patch DOM.
 *
 * @author Laurent Bovet (LBO)
 * @author Alex Mathey (AMA)
 */
public class AttributeMergeConfigurer implements Configurer
{

    /**
     * Attribute namespace.
     */
    public static final String ATTRIBUTE_NAMESPACE = "http://izpack.org";

    /**
     * Action attribute.
     */
    public static final String ACTION_ATTRIBUTE = "action";

    /**
     * Matcher attribute.
     */
    public static final String MATCHER_ATTRIBUTE = "matcher";

    /**
     * {@inheritDoc}
     */
    public void configure(XmlMerge xmlMerge) throws ConfigurationException
    {

        MergeAction defaultMergeAction = new FullMergeAction();

        Mapper mapper = new NamespaceFilterMapper(ATTRIBUTE_NAMESPACE);

        defaultMergeAction.setMapperFactory(new StaticOperationFactory(mapper));

        // Configure the action factory
        OperationResolver actionResolver = new OperationResolver(StandardActions.class);

        defaultMergeAction.setActionFactory(new AttributeOperationFactory(defaultMergeAction,
                actionResolver, ACTION_ATTRIBUTE, ATTRIBUTE_NAMESPACE));

        // Configure the matcher factory
        Matcher defaultMatcher = new AttributeMatcher();

        OperationResolver matcherResolver = new OperationResolver(StandardMatchers.class);

        defaultMergeAction.setMatcherFactory(new AttributeOperationFactory(defaultMatcher,
                matcherResolver, MATCHER_ATTRIBUTE, ATTRIBUTE_NAMESPACE));

        xmlMerge.setRootMapper(mapper);
        xmlMerge.setRootMergeAction(defaultMergeAction);
    }

}
