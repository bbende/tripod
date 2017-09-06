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
package com.bbende.tripod.api.entity;

import com.bbende.tripod.api.Field;
import org.apache.commons.lang.Validate;

/**
 * Base class for entities.
 *
 * @author bbende
 */
public abstract class AbstractEntity implements Entity {

    private final String id;
    private final Field idField;

    public AbstractEntity(final Field idField, final String id) {
        this.id = id;
        this.idField = idField;
        Validate.notNull(id);
        Validate.notNull(idField);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Field getIdField() {
        return idField;
    }
}
