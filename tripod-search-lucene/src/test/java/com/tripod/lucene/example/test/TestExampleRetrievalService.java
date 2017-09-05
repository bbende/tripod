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

import com.tripod.api.query.RetrievalQuery;
import com.tripod.api.query.service.QueryException;
import com.tripod.lucene.example.Example;
import com.tripod.lucene.example.query.ExampleRetrievalService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Tests for ExampleRetrievalService with Lucene.
 *
 * @author bbende
 */
public class TestExampleRetrievalService extends TestExampleLuceneBase {

    private ExampleRetrievalService retrievalService;

    @Before
    public void setup() {
        this.retrievalService = new ExampleRetrievalService(searcherManager, defaultField, analyzer);
    }

    @Test
    public void testSimpleRetrieval() throws QueryException, ParseException {
        RetrievalQuery query = new RetrievalQuery("id:1");
        Example result = retrievalService.find(query);

        Assert.assertNotNull(result);
        Assert.assertEquals("1", result.getId());
        Assert.assertEquals("Title 1", result.getTitle());
        Assert.assertNotNull(result.getBody());
        Assert.assertTrue(result.getBody().startsWith("Body 1"));
        Assert.assertEquals(new SimpleDateFormat(DATE_FORMAT).parse("2016-10-01T01:00:00Z"), result.getCreateDate());
    }

    @Test
    public void testWhenDoesNotExist() throws QueryException, ParseException {
        RetrievalQuery query = new RetrievalQuery("id:ABCD");
        Example result = retrievalService.find(query);
        Assert.assertNull(result);
    }

}
