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
package com.tripod.solr.example.test;

import com.tripod.api.Field;
import com.tripod.api.query.FilterQuery;
import com.tripod.api.query.Query;
import com.tripod.api.query.Sort;
import com.tripod.api.query.SortOrder;
import com.tripod.api.query.result.FacetCount;
import com.tripod.api.query.result.FacetResult;
import com.tripod.api.query.result.QueryResults;
import com.tripod.api.query.service.QueryException;
import com.tripod.api.query.service.QueryService;
import com.tripod.solr.example.ExampleField;
import com.tripod.solr.example.query.ExampleSummary;
import com.tripod.solr.example.query.ExampleSummaryQuery;
import com.tripod.solr.example.query.ExampleSummaryQueryService;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for ExampleSummaryQueryService.
 *
 * @author bbende
 */
public class TestExampleSummaryQueryService extends TestExampleBase {

    private QueryService<Query,ExampleSummary> queryService;

    @Before
    public void setup() {
        this.queryService = new ExampleSummaryQueryService(solrClient);
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

    /**
     * Tests using a custom Query implementation (ExampleQuery).
     */
    @Test
    public void testExampleQuery() throws QueryException, ParseException {
        Query query = new ExampleSummaryQuery("id:1");
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

    /**
     * Tests highlighting on all fields which should match on catch all field _text_.
     */
    @Test
    public void testHighlightFields() throws QueryException {
        Query query = new ExampleSummaryQuery("Solr is cool");
        query.setHighlightFields(Arrays.asList(Field.ALL_FIELDS));

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
        Query query = new ExampleSummaryQuery("*:*");
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

        Query drillDownQuery = new ExampleSummaryQuery("*:*");
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
        Query query = new ExampleSummaryQuery("*:*");
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
    public void testFilterQueries() throws QueryException {
        Query query = new ExampleSummaryQuery("*:*");
        query.addFilterQuery(new FilterQuery(ExampleField.COLOR, "BLUE"));

        QueryResults<ExampleSummary> results = queryService.search(query);

        assertNotNull(results);
        assertNotNull(results.getResults());
        assertEquals(2, results.getResults().size());

        for (ExampleSummary summary : results.getResults()) {
            assertEquals("BLUE", summary.getColor());
        }
    }

    /**
     * Test filter querying by using the generic params.
     */
    @Test
    public void testMiscQueryParams() throws QueryException {
        Query query = new ExampleSummaryQuery("*:*");
        query.addParam("fq", ExampleField.COLOR.getName() + ":BLUE");

        QueryResults<ExampleSummary> results = queryService.search(query);

        assertNotNull(results);
        assertNotNull(results.getResults());
        assertEquals(2, results.getResults().size());

        for (ExampleSummary summary : results.getResults()) {
            assertEquals("BLUE", summary.getColor());
        }
    }

    @Test
    public void testPaging() throws QueryException {
        int offset = 0;
        int pageSize = 2;

        // query 0 to 2
        Query query = new ExampleSummaryQuery("*:*", offset, pageSize);
        QueryResults<ExampleSummary> results = queryService.search(query);

        assertNotNull(results);
        assertNotNull(results.getResults());
        assertEquals(2, results.getResults().size());
        assertEquals(5, results.getTotalResults());
        assertEquals(offset, results.getOffset());
        assertEquals(pageSize, results.getPageSize());

        // query 2 to 4
        offset = offset + pageSize;
        query = new ExampleSummaryQuery("*:*", offset, pageSize);
        results = queryService.search(query);

        assertNotNull(results);
        assertNotNull(results.getResults());
        assertEquals(2, results.getResults().size());
        assertEquals(5, results.getTotalResults());
        assertEquals(offset, results.getOffset());
        assertEquals(pageSize, results.getPageSize());

        // query 4 to 6
        offset = offset + pageSize;
        query = new ExampleSummaryQuery("*:*", offset, pageSize);
        results = queryService.search(query);

        assertNotNull(results);
        assertNotNull(results.getResults());
        assertEquals(1, results.getResults().size());
        assertEquals(5, results.getTotalResults());
        assertEquals(offset, results.getOffset());
        assertEquals(pageSize, results.getPageSize());
    }

}
