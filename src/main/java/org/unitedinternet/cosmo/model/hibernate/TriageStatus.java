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
package org.unitedinternet.cosmo.model.hibernate;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class TriageStatus {

    public static final String LABEL_NOW = "NOW";
    public static final String LABEL_LATER = "LATER";
    public static final String LABEL_DONE = "DONE";
    public static final int CODE_NOW = 100;
    public static final int CODE_LATER = 200;
    public static final int CODE_DONE = 300;

    @Column(name = "triagestatuscode")
    private Integer code;

    @Column(name = "triagestatusrank", precision = 12, scale = 2)
    private BigDecimal rank;
    
    @Column(name = "isautotriage")
    private boolean autoTriage;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public BigDecimal getRank() {
        return rank;
    }

    public void setRank(BigDecimal rank) {
        this.rank = rank;
    }

    public boolean getAutoTriage() {
        return autoTriage;
    }

    public void setAutoTriage(boolean autoTriage) {
        this.autoTriage = autoTriage;
    }
}
