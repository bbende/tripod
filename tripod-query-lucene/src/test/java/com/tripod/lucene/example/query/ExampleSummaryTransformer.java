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
package com.tripod.lucene.example.query;

import com.tripod.lucene.example.ExampleField;
import com.tripod.lucene.query.service.LuceneDocumentTransformer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;

import java.util.Date;

/**
 * LuceneDocumentTransformer for ExampleSummary.
 *
 * @author bbende
 */
public class ExampleSummaryTransformer implements LuceneDocumentTransformer<ExampleSummary> {

    @Override
    public ExampleSummary transform(Document input) {
        String id = input.get(ExampleField.ID.getName());
        String title = input.get(ExampleField.TITLE.getName());
        String color = input.get(ExampleField.COLOR.getName());

        Date createDate = null;
        IndexableField indexableField = input.getField(ExampleField.CREATE_DATE.getName());
        if (indexableField != null) {
            createDate = new Date(indexableField.numericValue().longValue());
        }

        ExampleSummary result = new ExampleSummary(id);
        result.setTitle(title);
        result.setColor(color);
        result.setCreateDate(createDate);
        return result;
    }

}
