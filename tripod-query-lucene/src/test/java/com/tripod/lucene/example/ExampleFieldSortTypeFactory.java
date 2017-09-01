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

import com.tripod.api.Field;
import com.tripod.lucene.SortTypeFactory;
import org.apache.lucene.search.SortField;

import java.util.HashMap;
import java.util.Map;

/**
 * SortTypeFactory for ExampleField.
 *
 * @author bbende
 */
public class ExampleFieldSortTypeFactory implements SortTypeFactory {

    private static final Map<ExampleField,SortField.Type> SORT_FIELD_TYPES = new HashMap<>();

    static {
        SORT_FIELD_TYPES.put(ExampleField.ID, SortField.Type.STRING);
        SORT_FIELD_TYPES.put(ExampleField.TITLE, SortField.Type.STRING);
        SORT_FIELD_TYPES.put(ExampleField.BODY, SortField.Type.STRING);
        SORT_FIELD_TYPES.put(ExampleField.COLOR, SortField.Type.STRING);
        SORT_FIELD_TYPES.put(ExampleField.CREATE_DATE, SortField.Type.LONG);
    }

    @Override
    public SortField.Type getSortType(final Field f) {
        return SORT_FIELD_TYPES.get(f);
    }

}
