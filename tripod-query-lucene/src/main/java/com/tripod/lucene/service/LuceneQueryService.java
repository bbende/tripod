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
package com.tripod.lucene.service;

import com.tripod.api.query.result.QueryResult;
import com.tripod.api.query.service.QueryException;
import com.tripod.api.query.service.QueryService;
import com.tripod.lucene.query.LuceneQuery;
import com.tripod.lucene.query.LuceneQueryResults;
import com.tripod.lucene.query.LuceneQueryTransformer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.SearcherManager;

/**
 * Lucene implementation of QueryService.
 *
 * NOTE: Clients should use SearcherManagerRefresher in order to periodically open new searchers and see new data.
 *
 * @author bbende
 */
public class LuceneQueryService<Q extends LuceneQuery, QR extends QueryResult> extends AbstractLuceneService<Q,QR>
        implements QueryService<Q,QR> {

    public LuceneQueryService(final SearcherManager searcherManager,
                              final Analyzer analyzer,
                              final LuceneQueryTransformer<Q> queryTransformer,
                              final LuceneDocumentTransformer<QR> documentTransformer) {
        super(searcherManager, analyzer, queryTransformer, documentTransformer);
    }

    @Override
    public LuceneQueryResults<QR> search(Q query) throws QueryException {
        return performSearch(query);
    }

}
