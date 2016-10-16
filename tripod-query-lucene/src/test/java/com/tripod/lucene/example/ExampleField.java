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
package com.tripod.lucene.example;

import com.tripod.lucene.LuceneField;
import org.apache.lucene.search.SortField;

/**
 * Example field implementation.
 *
 * @author bbende
 */
public enum ExampleField implements LuceneField {

    ID("id", SortField.Type.STRING),
    TITLE("title", SortField.Type.STRING),
    BODY("body", SortField.Type.STRING),
    COLOR("color", SortField.Type.STRING),
    CREATE_DATE("create_date", SortField.Type.LONG);

    private String fieldName;
    private SortField.Type sortType;

    ExampleField(String fieldName, SortField.Type sortType) {
        this.fieldName = fieldName;
        this.sortType = sortType;
    }

    @Override
    public String getName() {
        return fieldName;
    }

    @Override
    public SortField.Type getSortType() {
        return sortType;
    }

}
