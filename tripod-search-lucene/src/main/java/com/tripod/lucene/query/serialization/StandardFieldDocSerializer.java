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
package com.tripod.lucene.query.serialization;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import org.apache.lucene.search.FieldDoc;

/**
 * Standard implementation of FieldDocSerializer that uses Kryo.
 *
 * @author bbende
 */
public class StandardFieldDocSerializer implements FieldDocSerializer {

    private ObjectMapper mapper = new ObjectMapper(new SmileFactory());

    public StandardFieldDocSerializer() {
        // for Jackson to only look at fields and ignore getters/setters/is methods
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        // needed so that complex objects (BytesRef) don't get serialized as LinkedHashMap
        mapper.enableDefaultTyping();
    }

    @Override
    public byte[] serialize(final FieldDoc doc) {
        if (doc == null) {
            throw new IllegalArgumentException("FieldDoc cannot be null");
        }

        try {
            final SerializableFieldDoc sFieldDoc = new SerializableFieldDoc();
            sFieldDoc.setScore(doc.score);
            sFieldDoc.setDoc(doc.doc);
            sFieldDoc.setShardIndex(doc.shardIndex);
            sFieldDoc.setFields(doc.fields);

            return mapper.writeValueAsBytes(sFieldDoc);
        } catch (Exception e) {
            throw new SerializationException("Unable to serialize FieldDoc due to :" + e.getMessage(), e);
        }
    }

    @Override
    public FieldDoc deserialize(byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("Bytes cannot be null");
        }

        try {
            final SerializableFieldDoc sFieldDoc = mapper.readValue(bytes, SerializableFieldDoc.class);

            final FieldDoc fieldDoc = new FieldDoc(
                    sFieldDoc.getDoc(),
                    sFieldDoc.getScore(),
                    sFieldDoc.getFields(),
                    sFieldDoc.getShardIndex()
            );

            return fieldDoc;
        } catch (Exception e) {
            throw new SerializationException("Unable to deserialize bytes due to :" + e.getMessage(), e);
        }
    }

}
