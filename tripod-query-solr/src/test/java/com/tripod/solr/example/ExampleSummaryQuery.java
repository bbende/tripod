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

import java.util.Arrays;

/**
 * Example of extending Query to define common parameters for a type of query.
 *
 * @author bbende
 */
public class ExampleSummaryQuery extends Query {

    public ExampleSummaryQuery(String query) {
        super(query);
        init();
    }

    public ExampleSummaryQuery(String query, Integer offset, Integer rows) {
        super(query, offset, rows);
        init();
    }

    private void init() {
        setReturnFields(Arrays.asList(
                ExampleField.ID,
                ExampleField.TITLE,
                ExampleField.CREATE_DATE
        ));
    }

}
