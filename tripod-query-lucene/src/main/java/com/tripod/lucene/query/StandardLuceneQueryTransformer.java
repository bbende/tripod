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

import com.tripod.api.query.QueryTransformException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.facet.DrillDownQuery;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;

/**
 * Standard QueryTransformer for creating Lucene Query instances.
 *
 * @author bbende
 */
public class StandardLuceneQueryTransformer<Q extends LuceneQuery> implements LuceneQueryTransformer<Q> {

    private final String defaultField;
    private final Analyzer analyzer;
    private final FacetsConfig facetsConfig;

    public StandardLuceneQueryTransformer(final String defaultField, final Analyzer analyzer) {
        this(defaultField, analyzer, null);
    }

    public StandardLuceneQueryTransformer(final String defaultField, final Analyzer analyzer, final FacetsConfig facetsConfig) {
        this.defaultField = defaultField;
        this.analyzer = analyzer;
        this.facetsConfig = facetsConfig;
    }

    @Override
    public org.apache.lucene.search.Query transform(Q input) throws QueryTransformException {
        try {
            org.apache.lucene.search.Query luceneQuery = new QueryParser(defaultField, analyzer).parse(input.getQuery());

            if (input.getFilterQueries() == null || facetsConfig == null) {
                return luceneQuery;
            }

            // wrap the original query in a DrillDownQuery
            DrillDownQuery drillDownQuery = new DrillDownQuery(facetsConfig, luceneQuery);

            // add all the filter queries to the DrillDownQuery
            input.getFilterQueries().stream().forEach(fq -> drillDownQuery.add(fq.getField().getName(), fq.getValue()));
            return drillDownQuery;

        } catch (ParseException e) {
            throw new QueryTransformException(e.getMessage(), e);
        }
    }

}
