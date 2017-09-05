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

import com.tripod.api.Field;

/**
 * Represents an entity that may be indexed and/or searched.
 *
 * All entities must have an id field of type string.
 *
 * @author bbende
 */
public interface Entity {

    /**
     * @return the id of this entity
     */
    String getId();

    /**
     * @return the id field in the index for this entity
     */
    Field getIdField();

}
