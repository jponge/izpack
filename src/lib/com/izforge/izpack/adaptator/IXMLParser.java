/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright (c) 2008, 2009 Anthonin Bonnefoy
 * Copyright (c) 2008, 2009 David Duponchel
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

package com.izforge.izpack.adaptator;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Interface for the adaptator of the javax Xml parser
 *
 * @author Anthonin Bonnefoy
 * @author David Duponchel
 */
public interface IXMLParser
{
    /**
     * Parse the given stream to a XML
     *
     * @param inputStream Stream to parse
     *
     * @return Root element of the parsed xml
     */
    IXMLElement parse(InputStream inputStream);

    /**
     * Parse the given text as an xml
     *
     * @param inputString Xml written in a string
     *
     * @return Root element of the parsed xml
     */
    IXMLElement parse(String inputString);

    /**
     * Parse the resource at the url specified
     *
     * @param inputURL Url of the resource
     *
     * @return Root element of the parsed xml
     *
     * @throws IOException Exception in the parsed of the resource
     */
    IXMLElement parse(URL inputURL) throws IOException;
}
