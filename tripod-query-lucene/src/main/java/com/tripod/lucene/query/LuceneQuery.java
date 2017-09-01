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
package com.tripod.lucene.query;

import com.tripod.api.query.Query;
import com.tripod.lucene.LuceneField;
import org.apache.commons.lang.Validate;
import org.apache.lucene.search.ScoreDoc;

/**
 * Lucene specific Query.
 *
 * @author bbende
 */
public class LuceneQuery extends Query<LuceneField> {

    private final ScoreDoc afterDoc;

    private String highlightPreTag = "<strong>";
    private String highlightPostTag = "</strong>";

    public LuceneQuery(final String query) {
        super(query);
        this.afterDoc = null;
    }

    public LuceneQuery(final String query, final ScoreDoc afterDoc, final Integer rows) {
        super(query, 0, rows);
        this.afterDoc = afterDoc;
    }

    public ScoreDoc getAfterDoc() {
        return afterDoc;
    }

    public String getHighlightPreTag() {
        return highlightPreTag;
    }

    public void setHighlightPreTag(String highlightPreTag) {
        Validate.notNull(highlightPreTag);
        this.highlightPreTag = highlightPreTag;
    }

    public String getHighlightPostTag() {
        return highlightPostTag;
    }

    public void setHighlightPostTag(String highlightPostTag) {
        Validate.notNull(highlightPostTag);
        this.highlightPostTag = highlightPostTag;
    }
}
