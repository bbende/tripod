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
package com.bbende.tripod.solr.query.service;

import com.bbende.tripod.api.TransformException;
import com.bbende.tripod.api.entity.Entity;
import com.bbende.tripod.api.query.RetrievalQuery;
import com.bbende.tripod.api.query.service.QueryException;
import com.bbende.tripod.api.query.service.RetrievalService;
import com.bbende.tripod.solr.query.SolrQueryTransformer;
import org.apache.commons.lang.Validate;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Solr implementation of RetrievalService.
 *
 * @author bbende
 */
public class SolrRetrievalService<E extends Entity> implements RetrievalService<E> {

    static final Logger LOGGER = LoggerFactory.getLogger(SolrRetrievalService.class);

    protected final SolrClient solrClient;
    protected final SolrQueryTransformer queryTransformer;
    protected final SolrDocumentTransformer<E> documentTransformer;

    public SolrRetrievalService(final SolrClient solrClient,
                                final SolrQueryTransformer queryTransformer,
                                final SolrDocumentTransformer<E> documentTransformer) {
        this.solrClient = solrClient;
        this.queryTransformer = queryTransformer;
        this.documentTransformer = documentTransformer;
        Validate.notNull(this.solrClient);
        Validate.notNull(this.queryTransformer);
        Validate.notNull(this.documentTransformer);
    }

    @Override
    public E find(final RetrievalQuery query) throws QueryException {
        final List<E> results = performSearch(query);
        if (results.size() > 1) {
            throw new QueryException("RetrievalQuery returned more than one result");
        }

        if (results.size() == 0) {
            return null;
        } else {
            return results.get(0);
        }
    }

    protected List<E> performSearch(final RetrievalQuery query) throws QueryException {
        try {
            // Convert from Query API to SolrQuery
            final SolrQuery solrQuery = queryTransformer.transform(query);

            // Perform the actual Solr query
            long startTime = System.currentTimeMillis();
            final QueryResponse response = solrClient.query(solrQuery);
            LOGGER.debug("Query executed in " + (System.currentTimeMillis() - startTime));

            // Transform each Solr doc to an Entity
            final List<E> results = new ArrayList<>();
            for (SolrDocument solrDoc : response.getResults()) {
                final E result = documentTransformer.transform(solrDoc);
                if (result != null) {
                    results.add(result);
                }
            }
            return  results;

        } catch (SolrServerException e) {
            LOGGER.error(e.getMessage(), e);
            throw new QueryException("An unexpected error occurred performing the search operation", e);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new QueryException("An unexpected error occurred communicating with the query service", e);
        } catch (TransformException e) {
            throw new QueryException("A transform error occurred", e);
        }
    }
}
