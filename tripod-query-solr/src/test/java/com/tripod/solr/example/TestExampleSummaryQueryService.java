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
package com.tripod.solr.example;

import com.tripod.api.query.Query;
import com.tripod.api.query.result.QueryResults;
import com.tripod.api.query.service.QueryException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Tests for ExampleSummaryQueryService.
 *
 * @author bbende
 */
public class TestExampleSummaryQueryService extends TestExampleBase {

    private ExampleSummaryQueryService queryService;

    @Before
    public void setup() {
        this.queryService = new ExampleSummaryQueryService(solrClient);
    }

    @Test
    public void testSimpleQuery() throws QueryException, ParseException {
        Query query = new Query("id:1");
        QueryResults<ExampleSummary> results = queryService.search(query);

        Assert.assertNotNull(results);
        Assert.assertNotNull(results.getResults());
        Assert.assertEquals(1, results.getResults().size());

        ExampleSummary result = results.getResults().get(0);
        Assert.assertEquals("1", result.getId());
        Assert.assertEquals("Title 1", result.getTitle());
        Assert.assertEquals(new SimpleDateFormat(DATE_FORMAT).parse("2016-10-01T01:00:00Z"), result.getCreateDate());
    }

    @Test
    public void testExampleQuery() throws QueryException, ParseException {
        Query query = new ExampleSummaryQuery("id:1");
        QueryResults<ExampleSummary> results = queryService.search(query);

        Assert.assertNotNull(results);
        Assert.assertNotNull(results.getResults());
        Assert.assertEquals(1, results.getResults().size());

        ExampleSummary result = results.getResults().get(0);
        Assert.assertEquals("1", result.getId());
        Assert.assertEquals("Title 1", result.getTitle());
        Assert.assertEquals(new SimpleDateFormat(DATE_FORMAT).parse("2016-10-01T01:00:00Z"), result.getCreateDate());
    }

}
