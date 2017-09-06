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
package com.bbende.tripod.lucene.example.test;

import com.bbende.tripod.api.query.FilterQuery;
import com.bbende.tripod.api.query.Query;
import com.bbende.tripod.api.query.Sort;
import com.bbende.tripod.api.query.SortOrder;
import com.bbende.tripod.api.query.result.FacetCount;
import com.bbende.tripod.api.query.result.FacetResult;
import com.bbende.tripod.api.query.result.QueryResults;
import com.bbende.tripod.api.query.service.QueryException;
import com.bbende.tripod.api.query.service.QueryService;
import com.bbende.tripod.lucene.example.ExampleField;
import com.bbende.tripod.lucene.example.ExampleSummary;
import com.bbende.tripod.lucene.example.query.ExampleSummaryQueryService;
import com.bbende.tripod.lucene.query.LuceneCursorMark;
import com.bbende.tripod.lucene.query.service.SearcherManagerRefresher;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.util.BytesRef;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for Lucene ExampleSummaryQueryService.
 *
 * @author bbende
 */
public class TestExampleSummaryQueryService extends TestExampleLuceneBase {

    private QueryService<ExampleSummary> queryService;

    @Before
    public void setup() {
        this.queryService = new ExampleSummaryQueryService(searcherManager, defaultField, analyzer, facetsConfig);
    }

    /**
     * Tests the simplest query returning all fields.
     */
    @Test
    public void testSimpleQuery() throws QueryException, ParseException {
        Query query = new Query("id:1");
        QueryResults<ExampleSummary> results = queryService.search(query);

        assertNotNull(results);
        assertNotNull(results.getResults());
        assertEquals(1, results.getResults().size());

        ExampleSummary result = results.getResults().get(0);
        assertEquals("1", result.getId());
        assertEquals("Title 1", result.getTitle());
        assertEquals("BLUE", result.getColor());
        assertEquals(new SimpleDateFormat(DATE_FORMAT).parse("2016-10-01T01:00:00Z"), result.getCreateDate());
    }

    @Test
    public void testReturnFields() throws QueryException {
        Query query = new Query("id:1");
        query.setReturnFields(Arrays.asList(ExampleField.ID));

        QueryResults<ExampleSummary> results = queryService.search(query);

        assertNotNull(results);
        assertNotNull(results.getResults());
        assertEquals(1, results.getResults().size());

        ExampleSummary result = results.getResults().get(0);
        assertEquals("1", result.getId());
        assertNull(result.getTitle());
        assertNull(result.getCreateDate());
        assertNull(result.getColor());
    }

    @Test
    public void testDefaultSearchField() throws QueryException {
        Query query = new Query("solr");
        QueryResults<ExampleSummary> results = queryService.search(query);

        // should search the body field by default and get two results
        assertNotNull(results);
        assertNotNull(results.getResults());
        assertEquals(2, results.getResults().size());
    }

    /**
     * Tests highlighting on all fields which should match on catch all field _text_.
     */
    @Test
    public void testHighlightAllFields() throws QueryException {
        Query query = new Query(ExampleField.BODY.getName() + ":\"Solr is cool\"");
        query.setHighlightFields(Arrays.asList(com.bbende.tripod.api.Field.ALL_FIELDS));

        QueryResults<ExampleSummary> results = queryService.search(query);

        assertNotNull(results);
        assertNotNull(results.getResults());
        assertEquals(1, results.getResults().size());

        ExampleSummary result = results.getResults().get(0);
        assertEquals("1", result.getId());

        assertNotNull(result.getHighlights());
        assertTrue(result.getHighlights().size() > 0);
    }

    /**
     * Tests highlighting on all fields which should match on catch all field _text_.
     */
    @Test
    public void testHighlightSpecificFields() throws QueryException {
        Query query = new Query(ExampleField.BODY.getName() + ":\"Solr is cool\"");
        query.setHighlightFields(Arrays.asList(ExampleField.BODY));

        QueryResults<ExampleSummary> results = queryService.search(query);

        assertNotNull(results);
        assertNotNull(results.getResults());
        assertEquals(1, results.getResults().size());

        ExampleSummary result = results.getResults().get(0);
        assertEquals("1", result.getId());

        assertNotNull(result.getHighlights());
        assertTrue(result.getHighlights().size() > 0);
    }


    @Test
    public void testFaceting() throws QueryException {
        Query query = new Query("*:*");
        query.setFacetFields(Arrays.asList(ExampleField.COLOR));

        QueryResults<ExampleSummary> results = queryService.search(query);

        assertNotNull(results);
        assertNotNull(results.getResults());
        assertEquals(5, results.getResults().size());

        assertNotNull(results.getFacetResults());
        assertEquals(1, results.getFacetResults().size());

        FacetResult facetResult = results.getFacetResults().get(0);
        assertEquals(ExampleField.COLOR.getName(), facetResult.getField());

        boolean foundBlue = false;
        boolean foundRed = false;
        boolean foundGreen = false;

        for (FacetCount facetCount : facetResult.getFacetCounts()) {
            if (facetCount.getValue().equals("BLUE")) {
                foundBlue = true;
                assertEquals(2, facetCount.getCount().longValue());
            } else if (facetCount.getValue().equals("RED")) {
                foundRed = true;
                assertEquals(2, facetCount.getCount().longValue());
            } else if (facetCount.getValue().equals("GREEN")) {
                foundGreen = true;
                assertEquals(1, facetCount.getCount().longValue());
            }
        }

        assertTrue(foundBlue);
        assertTrue(foundRed);
        assertTrue(foundGreen);

        // Now drill down to a specific facet

        Query drillDownQuery = new Query("*:*");
        drillDownQuery.addFacetField(ExampleField.COLOR);

        FilterQuery filterQuery = new FilterQuery(ExampleField.COLOR, "GREEN");
        drillDownQuery.addFilterQuery(filterQuery);

        QueryResults<ExampleSummary> drillDownResults = queryService.search(drillDownQuery);

        assertNotNull(drillDownResults);
        assertNotNull(drillDownResults.getResults());
        assertEquals(1, drillDownResults.getResults().size());

        final ExampleSummary exampleSummary = drillDownResults.getResults().get(0);
        assertNotNull(exampleSummary);
        assertEquals("GREEN", exampleSummary.getColor());
    }

    @Test
    public void testSorting() throws QueryException {
        Query query = new Query("*:*");
        query.setSorts(Arrays.asList(new Sort(ExampleField.CREATE_DATE, SortOrder.DESC)));

        QueryResults<ExampleSummary> results = queryService.search(query);

        assertNotNull(results);
        assertNotNull(results.getResults());
        assertEquals(5, results.getResults().size());

        for (int i=0; i < results.getResults().size(); i++) {
            ExampleSummary summary = results.getResults().get(i);
            assertEquals(String.valueOf(5-i), summary.getId());
        }
    }

    @Test
    public void testDefaultSorting() throws QueryException {
        Query query = new Query(ExampleField.BODY.getName() + ":Solr");

        QueryResults<ExampleSummary> results = queryService.search(query);

        assertNotNull(results);
        assertNotNull(results.getResults());
        assertEquals(2, results.getResults().size());

        // Default sort should be by relevance and doc 3 has "Solr" twice so should be first
        assertEquals("3", results.getResults().get(0).getId());
        assertEquals("1", results.getResults().get(1).getId());
    }

    @Test
    public void testPaging() throws QueryException {
        int offset = 0;
        int pageSize = 2;
        List<ExampleSummary> allResults = new ArrayList<>();

        // query 0 to 2
        Query query = new Query("*:*", offset, pageSize);
        query.setSorts(Arrays.asList(new Sort(ExampleField.ID, SortOrder.DESC)));

        QueryResults<ExampleSummary> results = queryService.search(query);

        assertNotNull(results);
        assertNotNull(results.getResults());
        assertEquals(2, results.getResults().size());
        assertEquals(5, results.getTotalResults());
        assertEquals(offset, results.getOffset());
        assertEquals(pageSize, results.getPageSize());
        allResults.addAll(results.getResults());

        // query 2 to 4
        offset = offset + pageSize;
        query = new Query("*:*", offset, pageSize);
        query.setSorts(Arrays.asList(new Sort(ExampleField.ID, SortOrder.DESC)));
        results = queryService.search(query);

        assertNotNull(results);
        assertNotNull(results.getResults());
        assertEquals(2, results.getResults().size());
        assertEquals(5, results.getTotalResults());
        assertEquals(offset, results.getOffset());
        assertEquals(pageSize, results.getPageSize());
        allResults.addAll(results.getResults());

        // query 4 to 6
        offset = offset + pageSize;
        query = new Query("*:*", offset, pageSize);
        query.setSorts(Arrays.asList(new Sort(ExampleField.ID, SortOrder.DESC)));
        results = queryService.search(query);

        assertNotNull(results);
        assertNotNull(results.getResults());
        assertEquals(1, results.getResults().size());
        assertEquals(5, results.getTotalResults());
        assertEquals(offset, results.getOffset());
        assertEquals(pageSize, results.getPageSize());
        allResults.addAll(results.getResults());

        assertEquals(5, allResults.size());

        for (int i=0; i < allResults.size(); i++) {
            ExampleSummary summary = allResults.get(i);
            assertEquals(String.valueOf(5-i), summary.getId());
        }
    }

    @Test
    public void testPagingWithCursorMark() throws QueryException {
        int pageSize = 2;
        String cursorMark = LuceneCursorMark.START;

        boolean done = false;
        List<String> ids = new ArrayList<>();

        // page through until the returned cursorMark is equal to the previous cursorMark

        while (!done) {
            // required to include a sort on the uniqueKey field to use cursorMark
            Query query = new Query("*:*", cursorMark, pageSize);
            query.addSort(ExampleField.CREATE_DATE, SortOrder.DESC);
            query.addSort(ExampleField.ID, SortOrder.ASC);

            QueryResults<ExampleSummary> results = queryService.search(query);

            assertNotNull(results);
            assertNotNull(results.getResults());
            assertTrue(results.getResults().size() <= pageSize);
            assertEquals(5, results.getTotalResults());
            assertEquals(0, results.getOffset());
            assertEquals(pageSize, results.getPageSize());
            assertNotNull(results.getCursorMark());

            results.getResults().stream().forEach(r -> ids.add(r.getId()));

            if (results.getCursorMark().equals(cursorMark)) {
                done = true;
            }

            cursorMark = results.getCursorMark();
        }

        // verify we got ids 1-5 in order
        assertEquals(5, ids.size());
        for (int i=0; i < 5; i++) {
            assertEquals(String.valueOf(5-i), ids.get(i));
        }

    }


    @Test
    public void testRefreshingSearcherManager() throws IOException, ParseException, QueryException, InterruptedException {
        // Add a new document
        final IndexWriterConfig config = new IndexWriterConfig(analyzer);
        try (IndexWriter writer = new IndexWriter(directory, config)) {
            final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

            Document doc = new Document();
            doc.add(new Field(ExampleField.ID.getName(), "99", StringField.TYPE_STORED));
            doc.add(new SortedDocValuesField(ExampleField.ID.getName(), new BytesRef("1")));
            doc.add(new Field(ExampleField.TITLE.getName(), "Title 99", TextField.TYPE_STORED));
            doc.add(new Field(ExampleField.BODY.getName(), "Body 99", TextField.TYPE_STORED));
            doc.add(new Field(ExampleField.COLOR.getName(), "BLUE", StringField.TYPE_STORED));
            doc.add(new SortedSetDocValuesFacetField(ExampleField.COLOR.getName(), "BLUE"));

            Date createDate1 = dateFormat.parse("2016-11-01T01:00:00Z");
            doc.add(new NumericDocValuesField(ExampleField.CREATE_DATE.getName(), createDate1.getTime()));
            doc.add(new StoredField(ExampleField.CREATE_DATE.getName(), createDate1.getTime()));
            writer.addDocument(facetsConfig.build(doc));

            writer.commit();
        }

        // Query for the new document and shouldn't get it
        Query query = new Query("id:99");
        QueryResults<ExampleSummary> results = queryService.search(query);

        assertNotNull(results);
        assertNotNull(results.getResults());
        assertEquals(0, results.getResults().size());

        // Start a refresher for the SearchManager
        SearcherManagerRefresher refresher = new SearcherManagerRefresher(searcherManager, 2000);
        try {
            // Start the refresher and then wait slightly longer than refresh interval
            refresher.start();
            Thread.sleep(3000);

            // Query again should get a result now
            query = new Query("id:99");
            results = queryService.search(query);

            assertNotNull(results);
            assertNotNull(results.getResults());
            assertEquals(1, results.getResults().size());
        } finally {
            refresher.stop();
        }
    }
}
