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

import com.bbende.tripod.lucene.example.ExampleField;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Base class for Lucene Example tests.
 *
 * @author bbende
 */
public class TestExampleLuceneBase {

    protected static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    protected Analyzer analyzer;
    protected String defaultField = ExampleField.BODY.getName();

    protected Directory directory;
    protected SearcherManager searcherManager;
    protected FacetsConfig facetsConfig;

    @Before
    public void setupBase() throws IOException, ParseException {
        analyzer = new StandardAnalyzer();
        directory = new RAMDirectory();

        facetsConfig = new FacetsConfig();
        facetsConfig.setIndexFieldName(ExampleField.COLOR.getName(), ExampleField.COLOR.getName());

        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        try (IndexWriter writer = new IndexWriter(directory, config)) {
            final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

            // Doc1
            Document doc1 = new Document();
            doc1.add(new Field(ExampleField.ID.getName(), "1", StringField.TYPE_STORED));
            doc1.add(new SortedDocValuesField(ExampleField.ID.getName(), new BytesRef("1")));
            doc1.add(new Field(ExampleField.TITLE.getName(), "Title 1", TextField.TYPE_STORED));
            doc1.add(new Field(ExampleField.BODY.getName(), "Body 1 Solr is cool", TextField.TYPE_STORED));
            doc1.add(new Field(ExampleField.COLOR.getName(), "BLUE", StringField.TYPE_STORED));
            doc1.add(new SortedSetDocValuesFacetField(ExampleField.COLOR.getName(), "BLUE"));

            Date createDate1 = dateFormat.parse("2016-10-01T01:00:00Z");
            doc1.add(new NumericDocValuesField(ExampleField.CREATE_DATE.getName(), createDate1.getTime()));
            doc1.add(new StoredField(ExampleField.CREATE_DATE.getName(), createDate1.getTime()));
            writer.addDocument(facetsConfig.build(doc1));

            // Doc2
            Document doc2 = new Document();
            doc2.add(new Field(ExampleField.ID.getName(), "2", StringField.TYPE_STORED));
            doc2.add(new SortedDocValuesField(ExampleField.ID.getName(), new BytesRef("2")));
            doc2.add(new Field(ExampleField.TITLE.getName(), "Title 2", TextField.TYPE_STORED));
            doc2.add(new Field(ExampleField.BODY.getName(), "Body 2 Lucene is cool", TextField.TYPE_STORED));
            doc2.add(new Field(ExampleField.COLOR.getName(), "RED", StringField.TYPE_STORED));
            doc2.add(new SortedSetDocValuesFacetField(ExampleField.COLOR.getName(), "RED"));

            Date createDate2 = dateFormat.parse("2016-10-01T02:00:00Z");
            doc2.add(new NumericDocValuesField(ExampleField.CREATE_DATE.getName(), createDate2.getTime()));
            doc2.add(new StoredField(ExampleField.CREATE_DATE.getName(), createDate2.getTime()));
            writer.addDocument(facetsConfig.build(doc2));

            // Doc3
            Document doc3 = new Document();
            doc3.add(new Field(ExampleField.ID.getName(), "3", StringField.TYPE_STORED));
            doc3.add(new SortedDocValuesField(ExampleField.ID.getName(), new BytesRef("3")));
            doc3.add(new Field(ExampleField.TITLE.getName(), "Title 3", TextField.TYPE_STORED));
            doc3.add(new Field(ExampleField.BODY.getName(), "Body 3 Solr is Great, Solr is Fun", TextField.TYPE_STORED));
            doc3.add(new Field(ExampleField.COLOR.getName(), "GREEN", StringField.TYPE_STORED));
            doc3.add(new SortedSetDocValuesFacetField(ExampleField.COLOR.getName(), "GREEN"));

            Date createDate3 = dateFormat.parse("2016-10-01T03:00:00Z");
            doc3.add(new NumericDocValuesField(ExampleField.CREATE_DATE.getName(), createDate3.getTime()));
            doc3.add(new StoredField(ExampleField.CREATE_DATE.getName(), createDate3.getTime()));
            writer.addDocument(facetsConfig.build(doc3));

            // Doc4
            Document doc4 = new Document();
            doc4.add(new Field(ExampleField.ID.getName(), "4", StringField.TYPE_STORED));
            doc4.add(new SortedDocValuesField(ExampleField.ID.getName(), new BytesRef("4")));
            doc4.add(new Field(ExampleField.TITLE.getName(), "Title 4", TextField.TYPE_STORED));
            doc4.add(new Field(ExampleField.BODY.getName(), "Body 4", TextField.TYPE_STORED));
            doc4.add(new Field(ExampleField.COLOR.getName(), "BLUE", StringField.TYPE_STORED));
            doc4.add(new SortedSetDocValuesFacetField(ExampleField.COLOR.getName(), "BLUE"));

            Date createDate4 = dateFormat.parse("2016-10-01T04:00:00Z");
            doc4.add(new NumericDocValuesField(ExampleField.CREATE_DATE.getName(), createDate4.getTime()));
            doc4.add(new StoredField(ExampleField.CREATE_DATE.getName(), createDate4.getTime()));
            writer.addDocument(facetsConfig.build(doc4));

            // Doc5
            Document doc5 = new Document();
            doc5.add(new Field(ExampleField.ID.getName(), "5", StringField.TYPE_STORED));
            doc5.add(new SortedDocValuesField(ExampleField.ID.getName(), new BytesRef("5")));
            doc5.add(new Field(ExampleField.TITLE.getName(), "Title 5", TextField.TYPE_STORED));
            doc5.add(new Field(ExampleField.BODY.getName(), "Body 5", TextField.TYPE_STORED));
            doc5.add(new Field(ExampleField.COLOR.getName(), "RED", StringField.TYPE_STORED));
            doc5.add(new SortedSetDocValuesFacetField(ExampleField.COLOR.getName(), "RED"));

            Date createDate5 = dateFormat.parse("2016-10-01T05:00:00Z");
            doc5.add(new NumericDocValuesField(ExampleField.CREATE_DATE.getName(), createDate5.getTime()));
            doc5.add(new StoredField(ExampleField.CREATE_DATE.getName(), createDate5.getTime()));
            writer.addDocument(facetsConfig.build(doc5));

            // commit docs
            writer.commit();
        }

        // needs to be opened after the writer is closed otherwise it won't see the test data
        searcherManager = new SearcherManager(directory, null);
    }

    @After
    public void afterBase() {
        if (searcherManager != null) {
            try {
                searcherManager.close();
            } catch (IOException e) {
            }
        }
        if (directory != null) {
            try {
                directory.close();
            } catch (IOException e) {
            }
        }
    }

}
