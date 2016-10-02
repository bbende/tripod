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
package com.tripod.solr.service;

import com.tripod.api.query.RetrievalQuery;
import com.tripod.api.query.result.QueryResult;
import com.tripod.api.query.result.QueryResults;
import com.tripod.api.query.service.QueryException;
import com.tripod.api.query.service.RetrievalService;
import com.tripod.solr.query.SolrQueryFactory;
import org.apache.solr.client.solrj.SolrClient;

/**
 * Solr implementation of RetrievalService.
 *
 * @author bbende
 */
public class SolrRetrievalService<Q extends RetrievalQuery, QR extends QueryResult> extends AbstractSolrService<Q,QR>
    implements RetrievalService<Q,QR> {

    public SolrRetrievalService(final SolrClient solrClient,
                                final SolrQueryFactory<Q> queryFactory,
                                final SolrDocumentTransformer<QR> solrDocumentTransformer) {
        super(solrClient, queryFactory, solrDocumentTransformer);
    }

    @Override
    public QR find(Q query) throws QueryException {
        QueryResults<QR> results = performSearch(query);
        if (results.getResults().size() > 1) {
            throw new QueryException("RetrievalQuery returned more than one result");
        }

        if (results.getResults().size() == 0) {
            return null;
        } else {
            return results.getResults().get(0);
        }
    }

}
