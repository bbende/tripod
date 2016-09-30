/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tripod.api.query.result;

import org.apache.commons.lang.Validate;

import java.util.List;

/**
 * @author bbende
 */
public class FacetResult {

    private final String field;

    private final List<FacetCount> facetCounts;

    public FacetResult(final String field, final List<FacetCount> facetCounts) {
        this.field = field;
        this.facetCounts = facetCounts;
        Validate.notNull(field);
        Validate.notNull(facetCounts);
    }

    public String getField() {
        return field;
    }

    public List<FacetCount> getFacetCounts() {
        return facetCounts;
    }

}
