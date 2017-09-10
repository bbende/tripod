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

import com.bbende.tripod.api.index.IndexException;
import com.bbende.tripod.api.query.result.FacetCount;
import com.bbende.tripod.api.query.result.FacetResult;
import com.bbende.tripod.api.query.result.QueryResults;
import com.bbende.tripod.api.query.service.QueryException;
import com.bbende.tripod.lucene.example.Example;
import com.bbende.tripod.lucene.example.ExampleSummary;
import com.bbende.tripod.lucene.example.index.ExampleIndexer;
import com.bbende.tripod.lucene.index.LuceneIndexer;
import org.apache.lucene.util.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
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
public class TestExampleIndexer extends TestIndexerBase {

    private LuceneIndexer<Example> indexer;

    @Before
    public void setup() throws IndexException {
        indexer = new ExampleIndexer(directory, indexWriterConfig, facetsConfig);
        indexer.open();
    }

    @After
    public void cleanup() throws IOException {
        IOUtils.closeWhileHandlingException(indexer);
    }

    @Test
    public void testIndexExamples() throws IndexException, QueryException, ParseException, IOException {
        // index some example entities
        final Example e1 = indexExample(indexer, "1", "BLUE");
        final Example e2 = indexExample(indexer, "2", "RED");
        indexer.commit();

        // now verify the correct docs exist

        QueryResults<ExampleSummary> results = queryAllDocs();

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

        // query again and verify the update took place
        QueryResults<ExampleSummary> updatedResults = queryAllDocs();

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

        // query again and verify the deletes took place
        QueryResults<ExampleSummary> emptyResults = queryAllDocs();
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
