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
package com.bbende.tripod.api.query.result;

import com.bbende.tripod.api.Field;
import com.bbende.tripod.api.entity.AbstractEntity;

import java.util.List;

/**
 * Base implementation for QueryResult.
 *
 * @author bbende
 */
public abstract class AbstractQueryResult extends AbstractEntity
        implements QueryResult {

    private List<Highlight> highlights;

    public AbstractQueryResult(final Field idField, final String id) {
        super(idField, id);
    }

    @Override
    public List<Highlight> getHighlights() {
        return highlights;
    }

    @Override
    public void setHighlights(List<Highlight> highlights) {
        this.highlights = highlights;
    }

}
