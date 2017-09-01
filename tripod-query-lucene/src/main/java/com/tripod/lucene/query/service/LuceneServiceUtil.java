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
package com.tripod.lucene.query.service;

import com.tripod.api.Field;
import com.tripod.api.query.SortOrder;
import com.tripod.lucene.SortTypeFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Utility code to share across Lucene services.
 *
 * @author bbende
 */
public class LuceneServiceUtil {

    /**
     * Converts the Tripod Sort clauses to a Lucene Sort instance.
     *
     * @param sorts the Tripod Sorts
     * @return the Lucene Sort matching the given Tripod Sorts,
     *              or the Lucene Sort for relevance order if no sorts are specified
     */
    public static Sort getSort(final List<com.tripod.api.query.Sort> sorts,
                               final SortTypeFactory sortTypeFactory) {
        if (sorts == null || sorts.isEmpty()) {
            return Sort.RELEVANCE;
        } else {
            List<SortField> luceneSorts = new ArrayList<>();
            for (com.tripod.api.query.Sort sort : sorts) {
                boolean reverse = (sort.getSortOrder() == SortOrder.DESC);
                SortField.Type sortType =  sortTypeFactory.getSortType(sort.getField());
                luceneSorts.add(new SortField(sort.getField().getName(), sortType, reverse));
            }
            return new Sort(luceneSorts.toArray(new SortField[luceneSorts.size()]));
        }
    }

    /**
     * @param searcher the IndexSearcher
     * @param doc the doc to load
     * @param fieldsToLoad the fields of the doc to load
     * @return the Document with the given fields loaded
     * @throws IOException if an error occurs loading the Document
     */
    public static Document getDoc(final IndexSearcher searcher, final int doc, final Set<String> fieldsToLoad)
            throws IOException {
        if (fieldsToLoad == null || fieldsToLoad.size() == 0
                || (fieldsToLoad.size() == 1 && fieldsToLoad.contains(Field.ALL_FIELDS.getName()))) {
            return searcher.doc(doc);
        } else {
            return searcher.doc(doc, fieldsToLoad);
        }
    }

}
