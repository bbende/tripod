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
package com.bbende.tripod.solr.example.query;

import com.bbende.tripod.solr.example.ExampleField;
import com.bbende.tripod.solr.example.ExampleSummary;
import com.bbende.tripod.solr.query.service.SolrDocumentTransformer;
import org.apache.solr.common.SolrDocument;

import java.util.Date;

/**
 * SolrDocumentTransformer for ExampleSummary.
 *
 * @author bbende
 */
public class ExampleSummaryTransformer implements SolrDocumentTransformer<ExampleSummary> {

    @Override
    public ExampleSummary transform(SolrDocument input) {
        String id = getString(input, ExampleField.ID.getName());
        String title = getString(input, ExampleField.TITLE.getName());
        String color = getString(input, ExampleField.COLOR.getName());
        Date createDate = getDate(input, ExampleField.CREATE_DATE.getName());

        ExampleSummary result = new ExampleSummary(id);
        result.setTitle(title);
        result.setColor(color);
        result.setCreateDate(createDate);
        return result;
    }

}
