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
package com.tripod.solr.query.service;

import com.tripod.api.query.Query;
import com.tripod.api.query.result.QueryResult;
import com.tripod.api.query.result.QueryResults;
import com.tripod.api.query.service.QueryException;
import com.tripod.api.query.service.QueryService;
import com.tripod.solr.query.SolrQueryTransformer;
import org.apache.solr.client.solrj.SolrClient;

/**
 * Solr implementation of QueryService.
 *
 * @author bbende
 */
public class SolrQueryService<Q extends Query, QR extends QueryResult> extends SolrService<Q,QR>
        implements QueryService<Q,QR> {

    public SolrQueryService(final SolrClient solrClient,
                            final SolrQueryTransformer<Q> queryFactory,
                            final SolrDocumentTransformer<QR> solrDocumentTransformer) {
        super(solrClient, queryFactory, solrDocumentTransformer);
    }

    @Override
    public QueryResults<QR> search(Q query) throws QueryException {
        return performSearch(query);
    }

}
