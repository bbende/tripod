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
package com.tripod.lucene.index;

import com.tripod.api.Field;
import com.tripod.api.TransformException;
import com.tripod.api.entity.Entity;
import com.tripod.api.index.IndexException;
import com.tripod.api.index.Indexer;
import org.apache.commons.lang.Validate;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;

import java.io.IOException;

/**
 * A service for adding entities to a Lucene index.
 *
 * @author bbende
 */
public class LuceneIndexer<E extends Entity> implements Indexer<E> {

    private final IndexWriter indexWriter;
    private final FacetsConfig facetsConfig;
    private final LuceneIndexTransformer<E> indexTransformer;

    public LuceneIndexer(final IndexWriter indexWriter,
                         final LuceneIndexTransformer<E> indexTransformer) {
        this(indexWriter, null, indexTransformer);
    }

    public LuceneIndexer(final IndexWriter indexWriter,
                         final FacetsConfig facetsConfig,
                         final LuceneIndexTransformer<E> indexTransformer) {
        this.indexWriter = indexWriter;
        this.facetsConfig = facetsConfig;
        this.indexTransformer = indexTransformer;
        Validate.notNull(indexWriter);
        Validate.notNull(indexTransformer);
    }

    @Override
    public void index(final E entity) throws IndexException {
        if (entity == null) {
            return;
        }

        try {
            Document doc = indexTransformer.transform(entity);
            if (facetsConfig != null) {
                doc = facetsConfig.build(doc);
            }

            indexWriter.addDocument(doc);

        } catch (IOException | TransformException e) {
            throw new IndexException("Unable to index entity due to: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(final E entity) throws IndexException {
        if (entity == null) {
            return;
        }

        // delete the existing document by id
        final Term idTerm = new Term(entity.getIdField().getName(), entity.getId());

        try {
            Document updatedDoc = indexTransformer.transform(entity);
            if (facetsConfig != null) {
                updatedDoc = facetsConfig.build(updatedDoc);
            }

            indexWriter.updateDocument(idTerm, updatedDoc);

        } catch (TransformException | IOException e) {
            throw new IndexException("Unable to update entity due to: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(E entity) throws IndexException {
        if (entity == null) {
            return;
        }

        delete(entity.getIdField(), entity.getId());
    }

    @Override
    public void delete(Field idField, String id) throws IndexException {
        if (idField == null) {
            throw new IllegalArgumentException("Id field cannot be null");
        }

        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }

        try {
            final Term idTerm = new Term(idField.getName(), id);
            indexWriter.deleteDocuments(idTerm);
        } catch (IOException e) {
            throw new IndexException("Unable to update entity due to: " + e.getMessage(), e);
        }
    }

    @Override
    public void commit() throws IndexException {
        try {
            indexWriter.commit();
        } catch (IOException e) {
            throw new IndexException("Unable to commit due to: " + e.getMessage(), e);
        }
    }
}
