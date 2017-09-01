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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Query results and any metadata about the results.
 *
 * @author bbende
 */
public class QueryResults<QR extends QueryResult> {

    private final int pageSize;
    private final long offset;
    private final long totalResults;
    private final String cursorMark;

    private final List<QR> results;
    private final List<FacetResult> facetResults;

    protected QueryResults(Builder<QR> builder) {
        this.offset = builder.offset;
        this.pageSize = builder.pageSize;
        this.totalResults = builder.totalResults;
        this.cursorMark = builder.cursorMark;
        this.results = Collections.unmodifiableList(new ArrayList<QR>(builder.results));
        this.facetResults = Collections.unmodifiableList(new ArrayList<>(builder.facetResults));
    }

    public long getOffset() {
        return offset;
    }

    public int getPageSize() {
        return pageSize;
    }

    public long getTotalResults() {
        return totalResults;
    }

    public String getCursorMark() {
        return cursorMark;
    }

    public List<QR> getResults() {
        return results;
    }

    public List<FacetResult> getFacetResults() {
        return facetResults;
    }

    /**
     * Builder for QueryResults.
     *
     * @param <QR> the type of result
     */
    public static class Builder<QR extends QueryResult> {
        private int pageSize;
        private long offset;
        private long totalResults;
        private String cursorMark;
        private List<QR> results = new ArrayList<>();
        private List<FacetResult> facetResults = new ArrayList<>();

        public Builder<QR> offset(long offset) {
            this.offset = offset;
            return this;
        }

        public Builder<QR> pageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public Builder<QR> totalResults(long totalResults) {
            this.totalResults = totalResults;
            return this;
        }

        public Builder<QR> cursorMark(String cursorMark) {
            this.cursorMark = cursorMark;
            return this;
        }

        public Builder<QR> results(List<QR> results) {
            if (results != null) {
                this.results.clear();
                this.results.addAll(results);
            }
            return this;
        }

        public Builder<QR> addResult(QR result) {
            if (result != null) {
                this.results.add(result);
            }
            return this;
        }

        public Builder<QR> facetResults(List<FacetResult> facetResults) {
            if (facetResults != null) {
                this.facetResults.clear();
                this.facetResults.addAll(facetResults);
            }
            return this;
        }

        public Builder<QR> addFacetResult(FacetResult facetResult) {
            if (facetResult != null) {
                this.facetResults.add(facetResult);
            }
            return this;
        }

        public QueryResults<QR> build() {
            return new QueryResults<>(this);
        }
    }
}
