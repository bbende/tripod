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
package com.bbende.tripod.solr.example.test;

import com.bbende.tripod.solr.example.ExampleField;
import com.bbende.tripod.solr.util.EmbeddedSolrServerFactory;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

import java.text.SimpleDateFormat;
import java.util.Arrays;

/**
 * Base class for Example/ExampleSummary tests.
 *
 * @author bbende
 */
public abstract class TestExampleBase {

    static final String EXAMPLE_CORE = "exampleCollection";

    static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    protected static SolrClient solrClient;

    @BeforeClass
    public static void setupClass() {
        try {
            solrClient = EmbeddedSolrServerFactory.create(EXAMPLE_CORE);

            final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

            // create some test documents
            SolrInputDocument doc1 = new SolrInputDocument();
            doc1.addField(ExampleField.ID.getName(), "1");
            doc1.addField(ExampleField.TITLE.getName(), "Title 1");
            doc1.addField(ExampleField.BODY.getName(), "Body 1 Solr is cool");
            doc1.addField(ExampleField.COLOR.getName(), "BLUE");
            doc1.addField(ExampleField.CREATE_DATE.getName(), dateFormat.parse("2016-10-01T01:00:00Z"));

            SolrInputDocument doc2 = new SolrInputDocument();
            doc2.addField(ExampleField.ID.getName(), "2");
            doc2.addField(ExampleField.TITLE.getName(), "Title 2");
            doc2.addField(ExampleField.BODY.getName(), "Body 2 Lucene is cool");
            doc2.addField(ExampleField.COLOR.getName(), "RED");
            doc2.addField(ExampleField.CREATE_DATE.getName(), dateFormat.parse("2016-10-01T02:00:00Z"));

            SolrInputDocument doc3 = new SolrInputDocument();
            doc3.addField(ExampleField.ID.getName(), "3");
            doc3.addField(ExampleField.TITLE.getName(), "Title 3");
            doc3.addField(ExampleField.BODY.getName(), "Body 3");
            doc3.addField(ExampleField.COLOR.getName(), "GREEN");
            doc3.addField(ExampleField.CREATE_DATE.getName(), dateFormat.parse("2016-10-01T03:00:00Z"));

            SolrInputDocument doc4 = new SolrInputDocument();
            doc4.addField(ExampleField.ID.getName(), "4");
            doc4.addField(ExampleField.TITLE.getName(), "Title 4");
            doc4.addField(ExampleField.BODY.getName(), "Body 4");
            doc4.addField(ExampleField.COLOR.getName(), "BLUE");
            doc4.addField(ExampleField.CREATE_DATE.getName(), dateFormat.parse("2016-10-01T04:00:00Z"));

            SolrInputDocument doc5 = new SolrInputDocument();
            doc5.addField(ExampleField.ID.getName(), "5");
            doc5.addField(ExampleField.TITLE.getName(), "Title 5");
            doc5.addField(ExampleField.BODY.getName(), "Body 5");
            doc5.addField(ExampleField.COLOR.getName(), "RED");
            doc5.addField(ExampleField.CREATE_DATE.getName(), dateFormat.parse("2016-10-01T05:00:00Z"));

            // add the test data to the index
            solrClient.add(Arrays.asList(doc1, doc2, doc3, doc4, doc5));
            solrClient.commit();
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @AfterClass
    public static void teardownClass() {
        try {
            solrClient.close();
        } catch (Exception e) {
        }
    }

}
