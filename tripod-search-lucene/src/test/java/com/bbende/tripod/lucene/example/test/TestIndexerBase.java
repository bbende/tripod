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
import com.bbende.tripod.api.index.Indexer;
import com.bbende.tripod.api.query.Query;
import com.bbende.tripod.api.query.Sort;
import com.bbende.tripod.api.query.SortOrder;
import com.bbende.tripod.api.query.result.QueryResults;
import com.bbende.tripod.api.query.service.QueryException;
import com.bbende.tripod.api.query.service.QueryService;
import com.bbende.tripod.lucene.example.Example;
import com.bbende.tripod.lucene.example.ExampleField;
import com.bbende.tripod.lucene.example.ExampleSummary;
import com.bbende.tripod.lucene.example.query.ExampleSummaryQueryService;
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

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

/**
 * Base class for Lucene Indexer tests.
 *
 * @author bbende
 */
public abstract class TestIndexerBase {

    static final String DEFAULT_FIELD = ExampleField.BODY.getName();

    protected Analyzer analyzer;
    protected Directory directory;
    protected IndexWriterConfig indexWriterConfig;
    protected FacetsConfig facetsConfig;

    protected SearcherManager searcherManager;
    protected QueryService<ExampleSummary> queryService;

    @Before
    public void setupBase() throws IOException {
        analyzer = new StandardAnalyzer();
        directory = new RAMDirectory();

        facetsConfig = new FacetsConfig();
        facetsConfig.setIndexFieldName(ExampleField.COLOR.getName(), ExampleField.COLOR.getName());

        indexWriterConfig = new IndexWriterConfig(analyzer);

        // need to open an index in the directory in order to create the SearcherManager here
        IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig());
        writer.close();

        searcherManager = new SearcherManager(directory, null);
        queryService = new ExampleSummaryQueryService(searcherManager, DEFAULT_FIELD, analyzer, facetsConfig);
    }

    @After
    public void afterBase() throws IOException {
        IOUtils.closeWhileHandlingException(searcherManager);
        IOUtils.closeWhileHandlingException(directory);
    }

    protected Example indexExample(Indexer<Example> indexer, String id, String color) throws IndexException {
        final Example e = new Example(id);
        e.setBody("Body of e" + id);
        e.setTitle("Title of e" + id);
        e.setColor(color);
        e.setCreateDate(new Date());
        indexer.index(e);
        return e;
    }

    protected QueryResults<ExampleSummary> queryAllDocs() throws IOException, QueryException {
        searcherManager.maybeRefreshBlocking();

        Query query = new Query("*:*");
        query.setSorts(Arrays.asList(new Sort(ExampleField.ID, SortOrder.ASC)));
        query.addFacetField(ExampleField.COLOR);

        return queryService.search(query);
    }

}
