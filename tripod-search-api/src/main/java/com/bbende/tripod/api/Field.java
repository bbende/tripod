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
package com.bbende.tripod.api;

/**
 * A field in the index to search on.
 *
 * Implementors should typically create an enum that implements Field.
 *
 * @author bbende
 */
public interface Field {

    /**
     * The field instance representing all fields.
     */
    Field ALL_FIELDS = new Field() {
        @Override
        public String getName() {
            return "*";
        }
    };

    /**
     * Standard id field.
     */
    Field ID = new Field() {
        @Override
        public String getName() {
            return "id";
        }
    };

    /**
     * @return the name of the field
     */
    String getName();

}
