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

import com.tripod.api.index.IndexException;
import com.tripod.api.index.Indexer;
import com.tripod.api.query.Query;
import com.tripod.api.query.Sort;
import com.tripod.api.query.SortOrder;
import com.tripod.api.query.result.QueryResults;
import com.tripod.api.query.service.QueryException;
import com.tripod.api.query.service.QueryService;
import com.tripod.solr.example.Example;
import com.tripod.solr.example.ExampleField;
import com.tripod.solr.example.index.ExampleIndexer;
import com.tripod.solr.example.query.ExampleSummary;
import com.tripod.solr.example.query.ExampleSummaryQuery;
import com.tripod.solr.example.query.ExampleSummaryQueryService;
import com.tripod.solr.util.EmbeddedSolrServerFactory;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author bbende
 */
public class TestExampleIndexer {
    static final String EXAMPLE_CORE = "exampleCollection";

    static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    private SolrClient solrClient;
    private Indexer<Example> indexer;

    @Before
    public void setup() throws IOException, SolrServerException {
        solrClient = EmbeddedSolrServerFactory.create(EXAMPLE_CORE);
        indexer = new ExampleIndexer(solrClient);
    }

    @Test
    public void testIndexExamples() throws IndexException, QueryException {
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

        // now verify the documents in the index

        final QueryService<Query,ExampleSummary> queryService = new ExampleSummaryQueryService(solrClient);

        final Query query = new ExampleSummaryQuery("*:*");
        query.setSorts(Arrays.asList(new Sort(ExampleField.ID, SortOrder.ASC)));

        final QueryResults<ExampleSummary> results = queryService.search(query);

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
    }

    @After
    public void cleanup() throws IOException {
        solrClient.close();
    }
}
