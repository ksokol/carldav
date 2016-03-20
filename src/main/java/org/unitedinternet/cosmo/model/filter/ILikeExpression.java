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

import static java.util.Locale.ENGLISH;

/**
 * FilterExpression that performs a case insensitive substring match.
 */
public class ILikeExpression extends FilterExpression {

    public ILikeExpression(Object value) {
        super(value);
    }

    @Override
    public void bind(StringBuffer expBuf, String propName, Map<String, Object> params) {
        String param = "param" + params.size();
        expBuf.append("lower(" + propName + ")");
        if (isNegated()) {
            expBuf.append(" not like ");
        } else {
            expBuf.append(" like ");
        }

        params.put(param, formatForLike(getValue().toString().toLowerCase(ENGLISH)));
        expBuf.append(":" + param);
    }
}
