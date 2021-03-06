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
package com.bbende.tripod.api.query.service;

import com.bbende.tripod.api.entity.Entity;
import com.bbende.tripod.api.query.RetrievalQuery;

/**
 * A service used to retrieve a single entity.
 *
 * @author bbende
 */
public interface RetrievalService<E extends Entity> {

    /**
     * Performs a query that is expected to return a single result.
     *
     * @param query the query to execute
     * @return the result of the query
     * @throws QueryException if an error occurs or if more than one result is found
     */
    E find(final RetrievalQuery query) throws QueryException;

}
