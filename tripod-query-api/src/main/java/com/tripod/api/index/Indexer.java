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
package com.tripod.api.index;

import com.tripod.api.entity.Entity;

/**
 * A service indexing entities.
 *
 * @author bbende
 */
public interface Indexer<E extends Entity> {

    /**
     * Adds the given entity to the index.
     *
     * @param entity the entity to index
     * @throws IndexException if an error occurs while adding the entity to the index
     */
    void index(E entity) throws IndexException;

    /**
     * Commits any changes to the index.
     *
     * @throws IndexException if an error occurs attempting commit
     */
    void commit() throws IndexException;

}
