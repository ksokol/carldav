/*
 * Copyright 2005-2006 Open Source Applications Foundation
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
package org.unitedinternet.cosmo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Defines server-wide constant attributes.
 */
public final class CosmoConstants {

    /**
     * The "friendly" name of the product used for casual identification.
     */
    public static final String PRODUCT_NAME = "carldav";

    /**
     * The Cosmo release version number.
     */
    public static final String PRODUCT_VERSION;

    private static final String PRODUCT_SUFFIX = "//NONSGML//DE";

    /**
     * A string identifier for Cosmo used to distinguish it from other
     * software products.
     */
    public static final String PRODUCT_ID;

    /**
     * product Id key in cosmo.properties files, differs from brand to another
     * example : -//1&1 Mail & Media GmbH/WEB.DE Kalender Server//NONSGML//DE
     */

    private static final String PRODUCT_VERSION_KEY = "calendar.server.Version";

    // read the product version from VERSION_FILE

    private static final String PROPERTIES_FILE = "/application.properties";

    static {
        Properties props = loadCosmoProperties();

        PRODUCT_VERSION = props.getProperty(PRODUCT_VERSION_KEY);

        // form the product Id using current build version
        PRODUCT_ID = new StringBuilder(PRODUCT_NAME)
                .append(" ").append(PRODUCT_VERSION).append(PRODUCT_SUFFIX)
                .toString();
    }

    /**
     * @return Properties
     */
    private static Properties loadCosmoProperties() {
        InputStream is = CosmoConstants.class.getResourceAsStream(PROPERTIES_FILE);
        Properties props = new Properties();
        
        try {
            props.load(is);
        } catch (IOException e) {
            throw new CosmoIOException(e); 
        }finally{
            if(is!= null){
                try {
                    is.close();
                } catch (IOException e) {
                    throw new CosmoIOException(e);
                }
            }
        }
        return props;
    }
}
