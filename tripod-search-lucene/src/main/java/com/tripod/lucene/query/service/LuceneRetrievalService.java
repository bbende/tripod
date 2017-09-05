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
package com.tripod.lucene.query.service;

import com.tripod.api.TransformException;
import com.tripod.api.entity.Entity;
import com.tripod.api.query.RetrievalQuery;
import com.tripod.api.query.service.QueryException;
import com.tripod.api.query.service.RetrievalService;
import com.tripod.lucene.SortTypeFactory;
import com.tripod.lucene.query.LuceneQueryTransformer;
import org.apache.commons.lang.Validate;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopFieldCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A RetrievalService for Lucene.
 *
 * @author bbende
 */
public class LuceneRetrievalService<E extends Entity> implements RetrievalService<E> {

    static final Logger LOGGER = LoggerFactory.getLogger(LuceneRetrievalService.class);

    protected final Analyzer analyzer;
    protected final SearcherManager searcherManager;
    protected final LuceneQueryTransformer queryTransformer;
    protected final LuceneDocumentTransformer<E> documentTransformer;
    protected final SortTypeFactory sortTypeFactory;

    public LuceneRetrievalService(final SearcherManager searcherManager,
                                  final Analyzer analyzer,
                                  final LuceneQueryTransformer queryTransformer,
                                  final LuceneDocumentTransformer<E> documentTransformer,
                                  final SortTypeFactory sortTypeFactory) {
        this.searcherManager = searcherManager;
        this.analyzer = analyzer;
        this.queryTransformer = queryTransformer;
        this.documentTransformer = documentTransformer;
        this.sortTypeFactory = sortTypeFactory;
        Validate.notNull(this.searcherManager);
        Validate.notNull(this.queryTransformer);
        Validate.notNull(this.documentTransformer);
        Validate.notNull(this.sortTypeFactory);
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
        IndexSearcher searcher = null;
        try {
            // Acquire an IndexSearcher
            searcher = searcherManager.acquire();

            // Create a searcher and get a Lucene query
            final Query luceneQuery = queryTransformer.transform(query);

            // Get the return fields
            final Set<String> fieldsToLoad = new HashSet<>();
            if (query.getReturnFields() != null) {
                query.getReturnFields().stream().forEach(f -> fieldsToLoad.add(f.getName()));
            }

            final Sort sort = LuceneServiceUtil.getSort(query.getSorts(), sortTypeFactory);

            // Collector for sorted/paged results
            final TopFieldCollector topFieldCollector = TopFieldCollector.create(
                    sort, query.getRows(), null, true, false, false);

            // Perform the Lucene query
            final long startTime = System.currentTimeMillis();
            searcher.search(luceneQuery, topFieldCollector);
            LOGGER.debug("Query executed in " + (System.currentTimeMillis() - startTime));

            // Transform each Lucene Document to an Entity
            final List<E> results = new ArrayList<>();
            for (ScoreDoc scoreDoc : topFieldCollector.topDocs().scoreDocs) {
                final Document doc = LuceneServiceUtil.getDoc(searcher, scoreDoc.doc, fieldsToLoad);
                final E result = documentTransformer.transform(doc);
                results.add(result);
            }
            return results;

        } catch (TransformException e) {
            throw new QueryException("A transform error occurred");
        } catch (IOException e) {
            throw new QueryException("Unexpected error occurred performing query", e);
        } finally {
            if (searcher != null) {
                try {
                    searcherManager.release(searcher);
                } catch (IOException e) {
                    LOGGER.warn("Error releasing IndexSearcher: " + e.getMessage(), e);
                }
            }
        }
    }

}
