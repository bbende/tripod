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
package com.bbende.tripod.lucene.query.serialization;

/**
 * Similar to FieldDoc but with a no-arg constructor to facilitate serialization with common libraries.
 *
 * @author bbende
 */
public class SerializableFieldDoc {

    private float score;

    private int doc;

    private int shardIndex;

    private Object[] fields;

    public SerializableFieldDoc() {

    }

    public SerializableFieldDoc(float score, int doc, int shardIndex, Object[] fields) {
        this.score = score;
        this.doc = doc;
        this.shardIndex = shardIndex;
        this.fields = fields;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public int getDoc() {
        return doc;
    }

    public void setDoc(int doc) {
        this.doc = doc;
    }

    public int getShardIndex() {
        return shardIndex;
    }

    public void setShardIndex(int shardIndex) {
        this.shardIndex = shardIndex;
    }

    public Object[] getFields() {
        return fields;
    }

    public void setFields(Object[] fields) {
        this.fields = fields;
    }
}
