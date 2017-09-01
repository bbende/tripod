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
package com.tripod.api.entity;

import org.apache.commons.lang.Validate;

import java.util.Objects;

/**
 * Base class for all entities.
 *
 * @author bbende
 */
public abstract class Entity<ID> {

    private final ID id;

    public Entity(final ID id) {
        this.id = id;
        Validate.notNull(this.id);
    }

    public ID getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof Entity)) {
            return false;
        }

        final Entity other = (Entity) obj;
        return Objects.equals(this.id, other.getId());
    }

}
