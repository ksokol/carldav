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

import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class TriageStatus {

    private Integer code;
    private BigDecimal rank;

    @Column(name = "triagestatuscode")
    public Integer getCode() {
        return code;
    }

    @Column(name = "triagestatusrank", precision = 12, scale = 2)
    @Type(type="org.hibernate.type.BigDecimalType")
    public BigDecimal getRank() {
        return rank;
    }

    public void setCode(final Integer code) {
        this.code = code;
    }

    public void setRank(final BigDecimal rank) {
        this.rank = rank;
    }
}