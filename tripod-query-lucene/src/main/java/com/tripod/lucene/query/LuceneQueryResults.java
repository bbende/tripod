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
package com.tripod.lucene.query;

import com.tripod.api.query.result.FacetResult;
import com.tripod.api.query.result.QueryResult;
import com.tripod.api.query.result.QueryResults;
import org.apache.lucene.search.ScoreDoc;

import java.util.List;

/**
 * Lucene specific QueryResults.
 *
 * @author bbende
 */
public class LuceneQueryResults<QR extends QueryResult> extends QueryResults<QR> {

    private final ScoreDoc afterDoc;

    private LuceneQueryResults(Builder<QR> builder) {
        super(builder.queryResultsBuilder);
        this.afterDoc = builder.afterDoc;
    }

    public ScoreDoc getAfterDoc() {
        return afterDoc;
    }

    /**
     * Builder for LuceneQueryResults wrapping the QueryResults builder.
     *
     * @param <QR> the type of QueryResult
     */
    public static final class Builder<QR extends QueryResult> {

        private ScoreDoc afterDoc;
        private QueryResults.Builder<QR> queryResultsBuilder = new QueryResults.Builder<>();

        public Builder<QR> offset(long offset) {
            queryResultsBuilder.offset(offset);
            return this;
        }

        public Builder<QR> pageSize(int pageSize) {
            this.queryResultsBuilder.pageSize(pageSize);
            return this;
        }

        public Builder<QR> totalResults(long totalResults) {
            this.queryResultsBuilder.totalResults(totalResults);
            return this;
        }

        public Builder<QR> results(List<QR> results) {
            this.queryResultsBuilder.results(results);
            return this;
        }

        public Builder<QR> addResult(QR result) {
            this.queryResultsBuilder.addResult(result);
            return this;
        }

        public Builder<QR> facetResults(List<FacetResult> facetResults) {
            this.queryResultsBuilder.facetResults(facetResults);
            return this;
        }

        public Builder<QR> addFacetResult(FacetResult facetResult) {
            this.queryResultsBuilder.addFacetResult(facetResult);
            return this;
        }

        public Builder<QR> afterDoc(ScoreDoc afterDoc) {
            this.afterDoc = afterDoc;
            return this;
        }

        public LuceneQueryResults<QR> build() {
            return new LuceneQueryResults<>(this);
        }

    }

}
