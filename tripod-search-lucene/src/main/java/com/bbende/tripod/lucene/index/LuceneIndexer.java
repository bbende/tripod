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
package com.bbende.tripod.lucene.index;

import com.bbende.tripod.api.Field;
import com.bbende.tripod.api.TransformException;
import com.bbende.tripod.api.entity.Entity;
import com.bbende.tripod.api.index.IndexException;
import com.bbende.tripod.api.index.Indexer;
import org.apache.commons.lang.Validate;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;

import java.io.IOException;

/**
 * A service for adding entities to a Lucene index.
 *
 * @author bbende
 */
public class LuceneIndexer<E extends Entity> implements Indexer<E> {

    protected final Directory directory;
    protected final IndexWriterConfig indexWriterConfig;
    protected final FacetsConfig facetsConfig;
    protected final LuceneIndexTransformer<E> indexTransformer;

    protected volatile IndexWriter indexWriter;
    protected volatile boolean opened = false;

    public LuceneIndexer(final Directory directory,
                         final IndexWriterConfig indexWriterConfig,
                         final LuceneIndexTransformer<E> indexTransformer) {
        this(directory, indexWriterConfig, null, indexTransformer);
    }

    public LuceneIndexer(final Directory directory,
                         final IndexWriterConfig indexWriterConfig,
                         final FacetsConfig facetsConfig,
                         final LuceneIndexTransformer<E> indexTransformer) {
        this.directory = directory;
        this.indexWriterConfig = indexWriterConfig;
        this.facetsConfig = facetsConfig;
        this.indexTransformer = indexTransformer;
        Validate.notNull(directory);
        Validate.notNull(indexWriterConfig);
        Validate.notNull(indexTransformer);
    }

    @Override
    public synchronized void open() throws IndexException {
        if (opened) {
            throw new IllegalStateException("Indexer already opened");
        }

        try {
            this.indexWriter = new IndexWriter(directory, indexWriterConfig);
            this.opened = true;
        } catch (IOException e) {
            throw new IndexException("Unable to open Indexer due to: " + e.getMessage(), e);
        }
    }

    @Override
    public void index(final E entity) throws IndexException {
        ensureOpen();

        if (entity == null) {
            return;
        }

        update(entity);
    }

    @Override
    public void update(final E entity) throws IndexException {
        ensureOpen();

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
    public void delete(final E entity) throws IndexException {
        ensureOpen();

        if (entity == null) {
            return;
        }

        delete(entity.getIdField(), entity.getId());
    }

    @Override
    public void delete(final Field idField, final String id) throws IndexException {
        ensureOpen();

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
        ensureOpen();
        try {
            indexWriter.commit();
        } catch (IOException e) {
            throw new IndexException("Unable to commit due to: " + e.getMessage(), e);
        }
    }

    @Override
    public synchronized void close() throws IOException {
        ensureOpen();
        this.opened = false;
        indexWriter.close();
    }

    private void ensureOpen() {
        if (!opened) {
            throw new IllegalStateException("Indexer must be opened");
        }
    }
}
