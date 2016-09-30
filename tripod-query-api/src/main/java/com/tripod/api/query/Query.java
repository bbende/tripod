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
package com.tripod.api.query;

import org.apache.commons.lang.Validate;

import java.util.List;

/**
 * Standard Query.
 *
 * @author bbende
 */
public class Query {

    public static final String QUERY_ALL = "*:*";

    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int DEFAULT_OFFSET = 0;

    private final String query;
    private final Integer offset;
    private final Integer rows;

    private List<Field> returnFields;
    private List<Field> highlightFields;
    private List<Field> facetFields;

    private List<String> filterQueries;
    private List<SortOrder> sortOrders;

    private Operator defaultOperator = Operator.AND;

    public Query(final String query) {
        this(query, DEFAULT_OFFSET, DEFAULT_PAGE_SIZE);
    }

    public Query(final String query, final Integer offset, final Integer rows) {
        this.query = query;
        this.offset = offset;
        this.rows = rows;

        Validate.notEmpty(query);
        Validate.notNull(offset);
        Validate.isTrue(offset >= 0);
        Validate.notNull(rows);
        Validate.isTrue(rows > 0);
    }

    public String getQuery() {
        return query;
    }

    public Integer getOffset() {
        return offset;
    }

    public Integer getRows() {
        return rows;
    }

    public List<Field> getReturnFields() {
        return returnFields;
    }

    public void setReturnFields(List<Field> returnFields) {
        this.returnFields = returnFields;
    }

    public List<Field> getHighlightFields() {
        return highlightFields;
    }

    public void setHighlightFields(List<Field> highlightFields) {
        this.highlightFields = highlightFields;
    }

    public List<Field> getFacetFields() {
        return facetFields;
    }

    public void setFacetFields(List<Field> facetFields) {
        this.facetFields = facetFields;
    }

    public List<String> getFilterQueries() {
        return filterQueries;
    }

    public void setFilterQueries(List<String> filterQueries) {
        this.filterQueries = filterQueries;
    }

    public List<SortOrder> getSortOrders() {
        return sortOrders;
    }

    public void setSortOrders(List<SortOrder> sortOrders) {
        this.sortOrders = sortOrders;
    }

    public Operator getDefaultOperator() {
        return defaultOperator;
    }

    public void setDefaultOperator(Operator defaultOperator) {
        this.defaultOperator = defaultOperator;
    }

}
