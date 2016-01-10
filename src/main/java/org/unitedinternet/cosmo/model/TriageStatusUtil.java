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
package org.unitedinternet.cosmo.model;

import java.math.BigDecimal;
import java.util.Date;

public class TriageStatusUtil {

    private static String LABEL_NOW = "NOW";
    private static String LABEL_LATER = "LATER";
    private static String LABEL_DONE = "DONE";

    public static int CODE_NOW = 100;
    public static int CODE_LATER = 200;
    public static int CODE_DONE = 300;

    public static Integer code(String label) {
        if (label.equals(LABEL_NOW)) {
            return Integer.valueOf(CODE_NOW);
        }
        if (label.equals(LABEL_LATER)) {
            return Integer.valueOf(CODE_LATER);
        }
        if (label.equals(LABEL_DONE)) {
            return Integer.valueOf(CODE_DONE);
        }
        throw new IllegalStateException("Unknown label " + label);
    }
    
    public static TriageStatus initialize(TriageStatus ts) {
        ts.setCode(Integer.valueOf(CODE_NOW));
        ts.setRank(getRank(System.currentTimeMillis()));
        return ts;
    }
    
    public static BigDecimal getRank(long date) {
        String time = (date / 1000) + ".00";
        return new BigDecimal(time).negate();
    }
    
    public static Date getDateFromRank(BigDecimal rank) {
        return new Date(rank.negate().scaleByPowerOfTen(3).longValue());
    }
}
