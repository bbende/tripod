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
package com.tripod.solr.index;

import com.tripod.api.Field;
import com.tripod.api.entity.Entity;
import com.tripod.api.index.IndexException;
import com.tripod.api.index.Indexer;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrInputDocument;

/**
 * A service for adding entities to a Solr index.
 *
 * @author bbende
 */
public class SolrIndexer<E extends Entity> implements Indexer<E> {

    private final SolrClient solrClient;
    private final SolrIndexTransformer<E> solrIndexTransformer;

    public SolrIndexer(final SolrClient solrClient, final SolrIndexTransformer<E> solrIndexTransformer) {
        this.solrClient = solrClient;
        this.solrIndexTransformer = solrIndexTransformer;
    }

    @Override
    public void index(final E entity) throws IndexException {
        if (entity == null) {
            return;
        }

        try {
            final SolrInputDocument doc = solrIndexTransformer.transform(entity);
            solrClient.add(doc);
        } catch (Exception e) {
            throw new IndexException("Unable to index entity due to: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(final E entity) throws IndexException {
            index(entity);
    }

    @Override
    public void delete(final E entity) throws IndexException {
        if (entity == null) {
            return;
        }

        delete(entity.getIdField(), entity.getId());
    }

    @Override
    public void delete(final Field idField, final String id) throws IndexException {
        if (idField == null) {
            throw new IllegalArgumentException("Id field cannot be null");
        }

        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }

        try {
            solrClient.deleteByQuery(idField.getName() + ":" + id);
        } catch (Exception e) {
            throw new IndexException("Unable to index entity due to: " + e.getMessage(), e);
        }
    }

    @Override
    public void commit() throws IndexException {
        try {
            solrClient.commit();
        } catch (Exception e) {
            throw new IndexException("Unable to commit due to: " + e.getMessage(), e);
        }
    }
}
