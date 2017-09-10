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
import com.bbende.tripod.api.query.result.QueryResults;
import com.bbende.tripod.api.query.service.QueryException;
import com.bbende.tripod.lucene.example.Example;
import com.bbende.tripod.lucene.example.ExampleSummary;
import com.bbende.tripod.lucene.example.index.ExampleTransactionalIndexer;
import com.bbende.tripod.lucene.index.LuceneTransactionalIndexer;
import org.apache.lucene.util.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test for ExampleTransactionalIndexer.
 *
 * @author bbende
 */
public class TestExampleTransactionalIndexer extends TestIndexerBase {

    private LuceneTransactionalIndexer<Example> indexer;

    @Before
    public void setup() throws IndexException {
        indexer = new ExampleTransactionalIndexer(directory, indexWriterConfig, facetsConfig);
        indexer.open();
    }

    @After
    public void after() {
        IOUtils.closeWhileHandlingException(indexer);
    }

    @Test
    public void testSuccessfulTransaction() throws IndexException, IOException, QueryException {
        // start a transaction
        indexer.beginTransaction();

        // index an Example entity
        indexExample(indexer,"1", "BLUE");

        // commit the transaction
        indexer.commit();

        // end the transaction which means rollback no longer possible
        indexer.endTransaction();

        // verify the example was indexed
        final QueryResults<ExampleSummary> results = queryAllDocs();
        assertNotNull(results);
        assertNotNull(results.getResults());
        assertEquals(1, results.getResults().size());
    }

    @Test
    public void testRollbackWithoutCommitting() throws IndexException, IOException, QueryException {
        // start a transaction
        indexer.beginTransaction();

        // index an Example entity
        indexExample(indexer,"1", "BLUE");

        // don't commit here to simulate  a rollback before commit was ever called

        // rollback the transaction
        indexer.rollback();

        // end the transaction which means rollback no longer possible
        indexer.endTransaction();

        // verify the example is not in the index
        final QueryResults<ExampleSummary> results = queryAllDocs();
        assertNotNull(results);
        assertNotNull(results.getResults());
        assertEquals(0, results.getResults().size());
    }

    @Test
    public void testRollbackWithCommit() throws IndexException, IOException, QueryException {
        // start a transaction
        indexer.beginTransaction();

        // index an Example entity
        indexExample(indexer,"1", "BLUE");

        // commit to simulate rolling back to previous state after commit was already called
        indexer.commit();

        // rollback the transaction
        indexer.rollback();

        // end the transaction which means rollback no longer possible
        indexer.endTransaction();

        // verify the example is not in the index
        final QueryResults<ExampleSummary> results = queryAllDocs();
        assertNotNull(results);
        assertNotNull(results.getResults());
        assertEquals(0, results.getResults().size());
    }

    @Test
    public void testRollbackAfterAddingDocs() throws IndexException, IOException, QueryException {
        final int numDocs = 1000;

        for (int i=0; i < numDocs; i++) {
            indexer.beginTransaction();
            indexExample(indexer, String.valueOf(i), "BLUE");
            indexer.commit();
            indexer.endTransaction();
        }

        // verify the example is not in the index
        QueryResults<ExampleSummary> results = queryAllDocs();
        assertNotNull(results);
        assertEquals(numDocs, results.getTotalResults());

        // index, commit, and rollback
        indexer.beginTransaction();
        indexExample(indexer,String.valueOf(numDocs + 1), "BLUE");
        indexer.commit();
        indexer.rollback();
        indexer.endTransaction();

        // verify there are still only numDocs
        results = queryAllDocs();
        assertNotNull(results);
        assertEquals(numDocs, results.getTotalResults());
    }

}
