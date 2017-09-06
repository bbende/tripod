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
package com.bbende.tripod.lucene.example.index;

import com.bbende.tripod.api.TransformException;
import com.bbende.tripod.lucene.example.Example;
import com.bbende.tripod.lucene.example.ExampleField;
import com.bbende.tripod.lucene.index.LuceneIndexTransformer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetField;
import org.apache.lucene.util.BytesRef;

import java.util.Date;

/**
 * Transforms Example entity to a Lucene Document.
 *
 * @author bbende
 */
public class ExampleIndexTransformer implements LuceneIndexTransformer<Example> {

    @Override
    public Document transform(final Example input) throws TransformException {
        final Document doc = new Document();

        doc.add(new Field(ExampleField.ID.getName(), input.getId(), StringField.TYPE_STORED));
        doc.add(new SortedDocValuesField(ExampleField.ID.getName(), new BytesRef(input.getId())));

        doc.add(new Field(ExampleField.TITLE.getName(), input.getTitle(), TextField.TYPE_STORED));
        doc.add(new Field(ExampleField.BODY.getName(), input.getBody(), TextField.TYPE_STORED));

        doc.add(new Field(ExampleField.COLOR.getName(), input.getColor(), StringField.TYPE_STORED));
        doc.add(new SortedSetDocValuesFacetField(ExampleField.COLOR.getName(), input.getColor()));

        final Date createDate = input.getCreateDate();
        doc.add(new NumericDocValuesField(ExampleField.CREATE_DATE.getName(), createDate.getTime()));
        doc.add(new StoredField(ExampleField.CREATE_DATE.getName(), createDate.getTime()));

        return doc;
    }

}
