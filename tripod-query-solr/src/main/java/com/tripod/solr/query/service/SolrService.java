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

import com.tripod.api.TransformException;
import com.tripod.api.query.Query;
import com.tripod.api.query.result.FacetCount;
import com.tripod.api.query.result.FacetResult;
import com.tripod.api.query.result.Highlight;
import com.tripod.api.query.result.QueryResult;
import com.tripod.api.query.result.QueryResults;
import com.tripod.api.query.service.QueryException;
import com.tripod.solr.query.SolrQueryTransformer;
import org.apache.commons.lang.Validate;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Base class for all Solr services.
 *
 * @author bbende
 */
public abstract class SolrService<Q extends Query, QR extends QueryResult> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolrQueryService.class);

    protected final SolrClient solrClient;
    protected final SolrQueryTransformer<Q> queryFactory;
    protected final SolrDocumentTransformer<QR> solrDocumentTransformer;
    protected SolrRequest.METHOD defaultMethod = SolrRequest.METHOD.GET;

    public SolrService(final SolrClient solrClient,
                       final SolrQueryTransformer<Q> queryFactory,
                       final SolrDocumentTransformer<QR> solrDocumentTransformer) {
        this.solrClient = solrClient;
        this.queryFactory = queryFactory;
        this.solrDocumentTransformer = solrDocumentTransformer;
        Validate.notNull(this.solrClient);
        Validate.notNull(this.queryFactory);
        Validate.notNull(this.solrDocumentTransformer);
    }

    public void setDefautMethod(SolrRequest.METHOD method) {
        Validate.notNull(method);
        this.defaultMethod = method;
    }

    /**
     * Common logic for sub-classes to perform searches.
     *
     * @param query the query
     * @return the QueryResults
     * @throws QueryException if an error ocurred performing the search
     */
    protected QueryResults<QR> performSearch(Q query) throws QueryException {
        try {
            // Convert from Query API to SolrQuery
            final SolrQuery solrQuery = queryFactory.transform(query);
            final SolrRequest.METHOD method = getMethod(query);

            // Start the results builder with the offset and rows from the query
            final QueryResults.Builder<QR> resultsBuilder = new QueryResults.Builder<QR>()
                    .offset(query.getOffset())
                    .pageSize(query.getRows());

            // Perform the actual Solr query
            long startTime = System.currentTimeMillis();
            final QueryResponse response = solrClient.query(solrQuery, method);
            LOGGER.debug("Query executed in " + (System.currentTimeMillis() - startTime));

            final SolrDocumentList solrDocs = response.getResults();
            final Map<String, Map<String, List<String>>> highlighting = response.getHighlighting();

            // Transform each Solr doc to a QueryResult and add highlights if they exist
            for (SolrDocument solrDoc : solrDocs) {
                final QR queryResult = solrDocumentTransformer.transform(solrDoc);
                if (queryResult != null) {
                    processHighlighting(queryResult, highlighting);
                    resultsBuilder.addResult(queryResult);
                }
            }

            // Process faceting results
            final List<FacetField> facetFields = response.getFacetFields();
            processFacetResults(resultsBuilder, facetFields);

            resultsBuilder.totalResults(solrDocs.getNumFound());
            return resultsBuilder.build();

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

    /**
     * Determine the request method.
     *
     * @param query incoming query
     * @return the method on the query, or the default method
     */
    private SolrRequest.METHOD getMethod(Q query) {
        SolrRequest.METHOD method = defaultMethod;
        if (query.getRequestMethod() != null) {
            switch (query.getRequestMethod()) {
                case GET:
                    method = SolrRequest.METHOD.GET;
                    break;
                case POST:
                    method = SolrRequest.METHOD.POST;
                    break;
            }
        }
        return method;
    }

    /**
     * Add any faceting results to the result builder.
     *
     * @param resultsBuilder query result builder
     * @param facetFields faceting results
     */
    protected void processFacetResults(QueryResults.Builder<QR> resultsBuilder, List<FacetField> facetFields) {
        if (facetFields != null) {
            for (FacetField facetField : facetFields) {
                String fieldName = facetField.getName();

                List<FacetCount> facetCounts = new ArrayList<>();
                for (FacetField.Count count : facetField.getValues()) {
                    facetCounts.add(new FacetCount(count.getName(), count.getCount()));
                }

                FacetResult facetResult = new FacetResult(fieldName, facetCounts);
                resultsBuilder.addFacetResult(facetResult);
            }
        }
    }

    /**
     * Adds any highlighting results to the given query result if applicable.
     *
     * @param queryResult the query result
     * @param highlighting all highlighting results for the result set
     */
    protected void processHighlighting(QR queryResult, Map<String, Map<String, List<String>>> highlighting) {
        final String queryResultId = queryResult.getId().toString();

        Map<String, List<String>> docHighlighting = null;
        if (highlighting != null && highlighting.containsKey(queryResultId)) {
            docHighlighting = highlighting.get(queryResultId);
        }

        if (highlighting == null) {
            return;
        }

        List<Highlight> highlights = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : docHighlighting.entrySet()) {
            Highlight highlight = new Highlight(entry.getKey(), entry.getValue());
            highlights.add(highlight);
        }

        queryResult.setHighlights(highlights);
    }

}
