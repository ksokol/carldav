/*
 * ServletContextUtil.java Jan 22, 2013
 * 
 * Copyright (c) 2013 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.servletcontext;

import java.util.Properties;

import javax.servlet.ServletContext;

public class ServletContextUtil {
    public static final String PROPERTIES_LOCATION = "propertiesLocation";
    
    public static Properties extractApplicationProperties(ServletContext servletContext) {
        String propertiesLocation = servletContext
                .getInitParameter(PROPERTIES_LOCATION);

        if (propertiesLocation == null) {
            return null;
        }

        return new Properties();
    }
}
