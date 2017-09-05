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
package com.tripod.api.query.result;

import org.apache.commons.lang.Validate;

import java.util.List;

/**
 * The highlighting results for a given field.
 *
 * @author bbende
 */
public class Highlight {

    private final String field;

    private final List<String> snippets;

    public Highlight(final String field, final List<String> snippets) {
        this.field = field;
        this.snippets = snippets;
        Validate.notNull(this.field);
        Validate.notEmpty(this.snippets);
    }

    public String getField() {
        return field;
    }

    public List<String> getSnippets() {
        return snippets;
    }

}
