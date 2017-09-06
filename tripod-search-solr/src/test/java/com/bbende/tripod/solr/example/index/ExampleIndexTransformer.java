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
package com.bbende.tripod.solr.example.index;

import com.bbende.tripod.api.TransformException;
import com.bbende.tripod.solr.example.Example;
import com.bbende.tripod.solr.example.ExampleField;
import com.bbende.tripod.solr.index.SolrIndexTransformer;
import org.apache.solr.common.SolrInputDocument;

/**
 * Transforms Example entities to SolrInputDocuments.
 *
 * @author bbende
 */
public class ExampleIndexTransformer implements SolrIndexTransformer<Example> {

    @Override
    public SolrInputDocument transform(final Example e) throws TransformException {
        SolrInputDocument doc = new SolrInputDocument();
        doc.addField(ExampleField.ID.getName(), e.getId());
        doc.addField(ExampleField.TITLE.getName(), e.getTitle());
        doc.addField(ExampleField.BODY.getName(), e.getBody());
        doc.addField(ExampleField.COLOR.getName(), e.getColor());
        doc.addField(ExampleField.CREATE_DATE.getName(), e.getCreateDate());
        return doc;
    }

}
