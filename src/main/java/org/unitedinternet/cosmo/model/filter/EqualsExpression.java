/*
 * Copyright 2007 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.model.filter;

import java.util.Map;

/**
 * FilterExpression that matches a value.
 */
public class EqualsExpression extends FilterExpression {

    public EqualsExpression(Object value) {
        super(value);
    }

    @Override
    public void bind(StringBuffer expBuf, String propName, Map<String, Object> params) {
        String param = "param" + params.size();
        expBuf.append(propName);
        if (isNegated()) {
            expBuf.append("!=");
        } else {
            expBuf.append("=");
        }

        params.put(param, getValue());
        expBuf.append(":" + param);
    }

}
