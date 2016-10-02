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
package com.tripod.solr.query;

import com.tripod.api.query.Query;
import com.tripod.api.query.Sort;
import com.tripod.api.query.SortOrder;
import org.apache.solr.client.solrj.SolrQuery;

/**
 * Standard factory for creating SolrQuery instances from the given Query.
 *
 * @author bbende
 */
public class StandardSolrQueryFactory<Q extends Query> implements SolrQueryFactory<Q> {

    @Override
    public SolrQuery create(final Q query) {
        final SolrQuery solrQuery = new SolrQuery(query.getQuery());
        solrQuery.setStart(query.getOffset());
        solrQuery.setRows(query.getRows());
        solrQuery.setParam("q.op", query.getDefaultOperator().name());

        if (query.getReturnFields() != null) {
            query.getReturnFields().stream().forEach(f -> solrQuery.addField(f.getName()));
        }

        if (query.getHighlightFields() != null && !query.getHighlightFields().isEmpty()) {
            solrQuery.setHighlight(true);
            query.getHighlightFields().stream().forEach(hf -> solrQuery.addHighlightField(hf.getName()));
        }

        if (query.getFacetFields() != null) {
            query.getFacetFields().stream().forEach(ff -> solrQuery.addFacetField(ff.getName()));
        }

        if (query.getSorts() != null) {
            for (Sort sort : query.getSorts()) {
                SolrQuery.ORDER solrOrder = sort.getSortOrder() == SortOrder.ASC ? SolrQuery.ORDER.asc : SolrQuery.ORDER.desc;
                SolrQuery.SortClause sortClause = new SolrQuery.SortClause(sort.getField().getName(), solrOrder);
                solrQuery.addSort(sortClause);
            }
        }

        if (query.getFilterQueries() != null) {
            query.getFilterQueries().stream().forEach(fq -> solrQuery.addFilterQuery(fq));
        }

        return solrQuery;
    }

}
