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
package com.bbende.tripod.lucene;

import com.bbende.tripod.api.Field;
import org.apache.lucene.search.SortField;

/**
 * Provides a look-up to obtain the SortField.Type for a given field.
 *
 * @author bbende
 */
public interface SortTypeFactory {

    /**
     * @param f a field being sorted on
     * @return the sort type for the field
     */
    SortField.Type getSortType(Field f);

}
