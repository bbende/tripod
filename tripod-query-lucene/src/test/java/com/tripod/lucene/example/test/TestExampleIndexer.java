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
package com.tripod.lucene.example.test;

import com.tripod.api.index.IndexException;
import com.tripod.api.query.Query;
import com.tripod.api.query.Sort;
import com.tripod.api.query.SortOrder;
import com.tripod.api.query.result.FacetCount;
import com.tripod.api.query.result.FacetResult;
import com.tripod.api.query.result.QueryResults;
import com.tripod.api.query.service.QueryException;
import com.tripod.api.query.service.QueryService;
import com.tripod.lucene.example.Example;
import com.tripod.lucene.example.ExampleField;
import com.tripod.lucene.example.ExampleSummary;
import com.tripod.lucene.example.index.ExampleIndexer;
import com.tripod.lucene.example.query.ExampleSummaryQueryService;
import com.tripod.lucene.index.LuceneIndexer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for ExampleIndexer.
 *
 * @author bbende
 */
public class TestExampleIndexer {

    static final String DEFAULT_FIELD = ExampleField.BODY.getName();

    private Analyzer analyzer;
    private Directory directory;
    private FacetsConfig facetsConfig;
    private IndexWriter indexWriter;

    private LuceneIndexer<Example> indexer;

    @Before
    public void setup() throws IOException {
        analyzer = new StandardAnalyzer();
        directory = new RAMDirectory();

        facetsConfig = new FacetsConfig();
        facetsConfig.setIndexFieldName(ExampleField.COLOR.getName(), ExampleField.COLOR.getName());

        final IndexWriterConfig config = new IndexWriterConfig(analyzer);
        indexWriter = new IndexWriter(directory, config);
        indexer = new ExampleIndexer(indexWriter, facetsConfig);
    }

    @After
    public void cleanup() throws IOException {
        IOUtils.closeWhileHandlingException(indexWriter);
        IOUtils.closeWhileHandlingException(directory);
    }

    @Test
    public void testIndexExamples() throws IndexException, QueryException, ParseException, IOException {
        final Example e1 = new Example("1");
        e1.setBody("Body of e1");
        e1.setTitle("Title of e1");
        e1.setColor("BLUE");
        e1.setCreateDate(new Date());
        indexer.index(e1);

        final Example e2 = new Example("2");
        e2.setBody("Body of e2");
        e2.setTitle("Title of e2");
        e2.setColor("RED");
        e2.setCreateDate(new Date());
        indexer.index(e2);

        indexer.commit();

        // now verify the correct docs exist

        SearcherManager searcherManager = new SearcherManager(directory, null);

        QueryService<ExampleSummary> queryService =
                new ExampleSummaryQueryService(searcherManager, DEFAULT_FIELD, analyzer, facetsConfig);

        Query query = new Query("*:*");
        query.setSorts(Arrays.asList(new Sort(ExampleField.ID, SortOrder.ASC)));
        query.addFacetField(ExampleField.COLOR);

        QueryResults<ExampleSummary> results = queryService.search(query);

        assertNotNull(results);
        assertNotNull(results.getResults());
        assertEquals(2, results.getResults().size());

        final ExampleSummary result = results.getResults().get(0);
        assertEquals(e1.getId(), result.getId());
        assertEquals(e1.getTitle(), result.getTitle());
        assertEquals(e1.getColor(), result.getColor());
        assertEquals(e1.getCreateDate(), result.getCreateDate());

        final ExampleSummary result2 = results.getResults().get(1);
        assertEquals(e2.getId(), result2.getId());
        assertEquals(e2.getTitle(), result2.getTitle());
        assertEquals(e2.getColor(), result2.getColor());
        assertEquals(e2.getCreateDate(), result2.getCreateDate());

        final List<FacetResult> facetResults = results.getFacetResults();
        assertEquals(1, facetResults.size());

        final List<FacetCount> facetCounts = facetResults.get(0).getFacetCounts();
        assertEquals(2, facetCounts.size());

        verifyFacetValuesExist(facetCounts, "BLUE", "RED");

        // now update one of the examples
        final Example e2updated = new Example("2");
        e2updated.setBody("Body of e2 updated");
        e2updated.setTitle("Title of e2 updated");
        e2updated.setColor("GREEN");
        e2updated.setCreateDate(new Date());
        indexer.update(e2updated);
        indexer.commit();

        // refresh the searcher
        searcherManager.maybeRefreshBlocking();

        // query again and verify the update took place
        QueryResults<ExampleSummary> updatedResults = queryService.search(query);

        assertNotNull(updatedResults);
        assertNotNull(updatedResults.getResults());
        assertEquals(2, updatedResults.getResults().size());

        final ExampleSummary updatedResult2 = updatedResults.getResults().get(1);
        assertEquals(e2updated.getId(), updatedResult2.getId());
        assertEquals(e2updated.getTitle(), updatedResult2.getTitle());
        assertEquals(e2updated.getColor(), updatedResult2.getColor());
        assertEquals(e2updated.getCreateDate(), updatedResult2.getCreateDate());

        final List<FacetResult> updatedFacetResults = updatedResults.getFacetResults();
        assertEquals(1, updatedFacetResults.size());

        final List<FacetCount> updatedFacetCounts = updatedFacetResults.get(0).getFacetCounts();
        assertEquals(2, updatedFacetCounts.size());

        verifyFacetValuesExist(updatedFacetCounts, "BLUE", "GREEN");

        // now delete the docs
        indexer.delete(e1);
        indexer.delete(e2);
        indexer.commit();

        // refresh the searcher
        searcherManager.maybeRefreshBlocking();

        // query again and verify the deletes took place
        QueryResults<ExampleSummary> emptyResults = queryService.search(query);
        assertNotNull(emptyResults);
        assertNotNull(emptyResults.getResults());
        assertEquals(0, emptyResults.getResults().size());
    }

    private void verifyFacetValuesExist(List<FacetCount> facetCounts, String ...facetValues) {
        for (String facetValue : facetValues) {
            boolean found = false;
            for (FacetCount facetCount : facetCounts) {
                if (facetCount.getValue().equals(facetValue)) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        }
    }

}
