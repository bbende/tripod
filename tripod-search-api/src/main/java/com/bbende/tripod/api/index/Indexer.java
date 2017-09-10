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
package com.bbende.tripod.api.index;

import com.bbende.tripod.api.Field;
import com.bbende.tripod.api.entity.Entity;

import java.io.Closeable;

/**
 * A service indexing entities.
 *
 * @author bbende
 */
public interface Indexer<E extends Entity> extends Closeable {

    /**
     * Must be called after instantiating the Indexer and before performing any operations.
     *
     * @throws IndexException if an error occurs during initialization
     */
    void open() throws IndexException;

    /**
     * Adds the given entity to the index.
     *
     * @param entity the entity to index
     * @throws IndexException if an error occurs while adding the entity to the index
     */
    void index(E entity) throws IndexException;

    /**
     * Updates the given entity by reindexing all fields of the given entity.
     *
     * @param entity the entity to update
     * @throws IndexException if an error occurs while updating the index
     */
    void update(E entity) throws IndexException;

    /**
     * Deletes the given entity from the index.
     *
     * @param entity the entity to delete
     * @throws IndexException if an error occurs deleting the entity from the index
     */
    void delete(E entity) throws IndexException;

    /**
     * Deletes documents with the given id field, where the id field is equal to the given id.
     *
     * @param idField the id field
     * @param id the value of the id field
     * @throws IndexException if an error occurs deleting from the index
     */
    void delete(Field idField, String id) throws IndexException;

    /**
     * Commits any changes to the index.
     *
     * @throws IndexException if an error occurs attempting commit
     */
    void commit() throws IndexException;

}
