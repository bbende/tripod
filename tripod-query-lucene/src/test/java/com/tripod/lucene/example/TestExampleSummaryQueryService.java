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
package com.tripod.lucene.example;

import com.tripod.api.query.Sort;
import com.tripod.api.query.SortOrder;
import com.tripod.api.query.result.FacetCount;
import com.tripod.api.query.result.FacetResult;
import com.tripod.api.query.result.QueryResults;
import com.tripod.api.query.service.QueryException;
import com.tripod.lucene.LuceneField;
import com.tripod.lucene.query.LuceneQuery;
import com.tripod.lucene.query.LuceneQueryResults;
import com.tripod.lucene.service.LuceneQueryService;
import com.tripod.lucene.service.SearcherManagerRefresher;
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
import java.util.Arrays;
import java.util.Date;

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

    private LuceneQueryService<LuceneQuery,ExampleSummary> queryService;

    @Before
    public void setup() {
        this.queryService = new ExampleSummaryQueryService(searcherManager, defaultField, analyzer);
    }

    /**
     * Tests the simplest query returning all fields.
     */
    @Test
    public void testSimpleQuery() throws QueryException, ParseException {
        LuceneQuery query = new LuceneQuery("id:1");
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
        LuceneQuery query = new LuceneQuery("id:1");
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
        LuceneQuery query = new LuceneQuery("solr");
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
        LuceneQuery query = new LuceneQuery(ExampleField.BODY.getName() + ":\"Solr is cool\"");
        query.setHighlightFields(Arrays.asList(LuceneField.ALL_LUCENE_FIELDS));

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
        LuceneQuery query = new LuceneQuery(ExampleField.BODY.getName() + ":\"Solr is cool\"");
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
        LuceneQuery query = new LuceneQuery("*:*");
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
    }

    @Test
    public void testSorting() throws QueryException {
        LuceneQuery query = new LuceneQuery("*:*");
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
        LuceneQuery query = new LuceneQuery(ExampleField.BODY.getName() + ":Solr");

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

        // query 0 to 2
        LuceneQuery query = new LuceneQuery("*:*", null, pageSize);
        query.setSorts(Arrays.asList(new Sort(ExampleField.ID, SortOrder.ASC)));

        LuceneQueryResults<ExampleSummary> results = queryService.search(query);

        assertNotNull(results);
        assertNotNull(results.getResults());
        assertEquals(2, results.getResults().size());
        assertEquals(5, results.getTotalResults());
        assertEquals(offset, results.getOffset());
        assertEquals(pageSize, results.getPageSize());

        // query 2 to 4
        query = new LuceneQuery("*:*", results.getAfterDoc(), pageSize);
        query.setSorts(Arrays.asList(new Sort(ExampleField.ID, SortOrder.ASC)));
        results = queryService.search(query);

        assertNotNull(results);
        assertNotNull(results.getResults());
        assertEquals(2, results.getResults().size());
        assertEquals(5, results.getTotalResults());
        assertEquals(offset, results.getOffset());
        assertEquals(pageSize, results.getPageSize());

        // query 4 to 6
        query = new LuceneQuery("*:*", results.getAfterDoc(), pageSize);
        query.setSorts(Arrays.asList(new Sort(ExampleField.ID, SortOrder.ASC)));
        results = queryService.search(query);

        assertNotNull(results);
        assertNotNull(results.getResults());
        assertEquals(1, results.getResults().size());
        assertEquals(5, results.getTotalResults());
        assertEquals(offset, results.getOffset());
        assertEquals(pageSize, results.getPageSize());
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
        LuceneQuery query = new LuceneQuery("id:99");
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
            query = new LuceneQuery("id:99");
            results = queryService.search(query);

            assertNotNull(results);
            assertNotNull(results.getResults());
            assertEquals(1, results.getResults().size());
        } finally {
            refresher.stop();
        }
    }
}
