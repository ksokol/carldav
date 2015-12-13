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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.hibernate.annotations.Type;
import org.unitedinternet.cosmo.model.TriageStatus;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Hibernate persistent TriageStatus.
 */
@Embeddable
public class HibTriageStatus implements TriageStatus {

    @Column(name = "triagestatuscode")
    private Integer code = null;
    
    @Column(name = "triagestatusrank", precision = 12, scale = 2)
    @Type(type="org.hibernate.type.BigDecimalType")
    private BigDecimal rank = null;
    
    @Column(name = "isautotriage")
    private Boolean autoTriage = null;

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.TriageStatus#getCode()
     */
    public Integer getCode() {
        return code;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.TriageStatus#setCode(java.lang.Integer)
     */
    public void setCode(Integer code) {
        this.code = code;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.TriageStatus#getRank()
     */
    public BigDecimal getRank() {
        return rank;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.TriageStatus#setRank(java.math.BigDecimal)
     */
    public void setRank(BigDecimal rank) {
        this.rank = rank;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.TriageStatus#getAutoTriage()
     */
    public Boolean getAutoTriage() {
        return autoTriage;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.TriageStatus#setAutoTriage(java.lang.Boolean)
     */
    public void setAutoTriage(Boolean autoTriage) {
        this.autoTriage = autoTriage;
    }

    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof HibTriageStatus)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        HibTriageStatus ts = (HibTriageStatus) obj;
        return new EqualsBuilder().
            append(code, ts.code).
            append(rank, ts.rank).
            append(autoTriage, ts.autoTriage).
            isEquals();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((autoTriage == null) ? 0 : autoTriage.hashCode());
        result = prime * result + ((code == null) ? 0 : code.hashCode());
        result = prime * result + ((rank == null) ? 0 : rank.hashCode());
        return result;
    }

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
}
