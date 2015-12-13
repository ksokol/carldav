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

/**
 * Represents a compound triage status value.
 */
public interface TriageStatus {

    /** */
    String LABEL_NOW = "NOW";
    /** */
    String LABEL_LATER = "LATER";
    /** */
    String LABEL_DONE = "DONE";
    /** */
    int CODE_NOW = 100;
    /** */
    int CODE_LATER = 200;
    /** */
    int CODE_DONE = 300;

    Integer getCode();

    void setCode(Integer code);

    BigDecimal getRank();

    void setRank(BigDecimal rank);

    Boolean getAutoTriage();

    void setAutoTriage(Boolean autoTriage);

}