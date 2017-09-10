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
import com.bbende.tripod.api.entity.Entity;
import com.bbende.tripod.api.index.IndexException;
import org.apache.commons.lang.Validate;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.index.IndexCommit;
import org.apache.lucene.index.IndexDeletionPolicy;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.PersistentSnapshotDeletionPolicy;
import org.apache.lucene.index.SnapshotDeletionPolicy;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A transactional Lucene Indexer.
 *
 * @author bbende
 */
public class LuceneTransactionalIndexer<E extends Entity> extends LuceneIndexer<E> {

    /**
     * Possible states the transaction can be in.
     */
    enum TransactionState {
        STARTED,
        COMMITTED,
        ROLLEDBACK;
    }

    private final SnapshotDirectoryFactory snapshotDirectoryFactory;
    private SnapshotDeletionPolicy snapshotPolicy;

    private AtomicBoolean isClosed = new AtomicBoolean(false);
    private AtomicReference<TransactionState> transactionState = new AtomicReference<>(null);
    private AtomicReference<IndexCommit> lastCommit = new AtomicReference<>(null);

    public LuceneTransactionalIndexer(
            final Directory indexDirectory,
            final IndexWriterConfig indexWriterConfig,
            final SnapshotDirectoryFactory snapshotDirectoryFactory,
            final LuceneIndexTransformer<E> indexTransformer) {
        super(indexDirectory, indexWriterConfig, indexTransformer);
        this.snapshotDirectoryFactory = snapshotDirectoryFactory;
        Validate.notNull(this.snapshotDirectoryFactory);
    }

    public LuceneTransactionalIndexer(
            final Directory indexDirectory,
            final IndexWriterConfig indexWriterConfig,
            final FacetsConfig facetsConfig,
            final SnapshotDirectoryFactory snapshotDirectoryFactory,
            final LuceneIndexTransformer<E> indexTransformer) {
        super(indexDirectory, indexWriterConfig, facetsConfig, indexTransformer);
        this.snapshotDirectoryFactory = snapshotDirectoryFactory;
        Validate.notNull(this.snapshotDirectoryFactory);
    }

    @Override
    public void open() throws IndexException {
        final Directory snapshotDirectory = snapshotDirectoryFactory.createSnapshotDirectory();
        final IndexDeletionPolicy originalDeletionPolicy = indexWriterConfig.getIndexDeletionPolicy();

        try {
            this.snapshotPolicy = createSnapshotPolicy(originalDeletionPolicy, snapshotDirectory);

            final IndexWriterConfig copiedConfig = copyIndexWriterConfig(indexWriterConfig);
            copiedConfig.setIndexDeletionPolicy(snapshotPolicy);

            this.indexWriter = new IndexWriter(directory, copiedConfig);
            this.opened = true;
        } catch (IOException e) {
            throw new IndexException("Unable to open Indexer due to: " + e.getMessage(), e);
        }
    }

    protected SnapshotDeletionPolicy createSnapshotPolicy(final IndexDeletionPolicy originalPolicy,
                                                          final Directory snapshotDirectory) throws IOException {
        return new PersistentSnapshotDeletionPolicy(originalPolicy, snapshotDirectory);
    }

    public synchronized void beginTransaction() throws IndexException {
        ensureOpen();

        if (transactionState.get() != null) {
            throw new IllegalStateException("A transaction is already in progress");
        }

        try {
            final IndexCommit snapshotCommit = snapshotPolicy.snapshot();
            lastCommit.set(snapshotCommit);
            transactionState.set(TransactionState.STARTED);
        } catch (IOException e) {
            lastCommit.set(null);
            transactionState.set(null);
            throw new IndexException("Unable to start transaction due to: " + e.getMessage(), e);
        }
    }

    @Override
    public synchronized void index(E entity) throws IndexException {
        ensureOpen();
        ensureTransactionIsStarted();
        super.index(entity);
    }

    @Override
    public synchronized void update(E entity) throws IndexException {
        ensureOpen();
        ensureTransactionIsStarted();
        super.update(entity);
    }

    @Override
    public synchronized void delete(E entity) throws IndexException {
        ensureOpen();
        ensureTransactionIsStarted();
        super.delete(entity);
    }

    @Override
    public synchronized void delete(Field idField, String id) throws IndexException {
        ensureOpen();
        ensureTransactionIsStarted();
        super.delete(idField, id);
    }

    @Override
    public synchronized void commit() throws IndexException {
        ensureOpen();
        ensureTransactionIsStarted();
        super.commit();
        transactionState.set(TransactionState.COMMITTED);
    }

    public synchronized void rollback() throws IndexException {
        ensureOpen();

        if (transactionState.get() == null) {
            throw new IllegalStateException("A transaction is not in progress");
        }

        if (transactionState.get() == TransactionState.ROLLEDBACK) {
            throw new IllegalStateException("Transaction already rolled back, must be ended");
        }

        try {
            indexWriter.rollback();

            final IndexWriterConfig config = copyIndexWriterConfig(indexWriterConfig);
            config.setIndexCommit(lastCommit.get());

            indexWriter = new IndexWriter(directory, config);
            indexWriter.commit();

            transactionState.set(TransactionState.ROLLEDBACK);
        } catch (IOException e) {
            throw new IndexException("Unable to rollback due to: " + e.getMessage(), e);
        }
    }

    public synchronized void endTransaction() throws IndexException {
        ensureOpen();

        if (transactionState.get() == null) {
            throw new IllegalStateException("A transaction is not in progress");
        }

        if (transactionState.get() != TransactionState.COMMITTED
                && transactionState.get() != TransactionState.ROLLEDBACK) {
            throw new IllegalStateException("Transaction must be committed or rolled back before ending");
        }

        try {
            snapshotPolicy.release(lastCommit.get());
        } catch (IOException e) {
            // TODO
            e.printStackTrace();
        }

        lastCommit.set(null);
        transactionState.set(null);
    }

    @Override
    public void close() throws IOException {
        if (isClosed.get()) {
            return;
        }

        isClosed.set(true);
        super.close();
    }

    private void ensureOpen() {
        if (isClosed.get()) {
            throw new IllegalStateException("Indexer already closed");
        }
    }

    private void ensureTransactionIsStarted() {
        if (transactionState.get() == null) {
            throw new IllegalStateException("Transaction must be started before performing this action");
        }

        if (transactionState.get() == TransactionState.COMMITTED) {
            throw new IllegalStateException("Transaction already committed, must be rolled back or ended");
        }

        if (transactionState.get() == TransactionState.ROLLEDBACK) {
            throw new IllegalStateException("Transaction already rolled back, must be ended");
        }
    }

    private IndexWriterConfig copyIndexWriterConfig(IndexWriterConfig baseConfig) {
        final IndexWriterConfig config = new IndexWriterConfig(baseConfig.getAnalyzer());
        config.setOpenMode(baseConfig.getOpenMode());
        config.setCodec(baseConfig.getCodec());
        config.setCommitOnClose(baseConfig.getCommitOnClose());
        config.setIndexDeletionPolicy(baseConfig.getIndexDeletionPolicy());
        if (baseConfig.getIndexSort() != null) {
            config.setIndexSort(baseConfig.getIndexSort());
        }
        config.setInfoStream(baseConfig.getInfoStream());
        config.setMaxBufferedDeleteTerms(baseConfig.getMaxBufferedDeleteTerms());
        config.setMaxBufferedDocs(baseConfig.getMaxBufferedDocs());
        config.setMergedSegmentWarmer(baseConfig.getMergedSegmentWarmer());
        config.setMergePolicy(baseConfig.getMergePolicy());
        config.setMergeScheduler(baseConfig.getMergeScheduler());
        config.setRAMBufferSizeMB(baseConfig.getRAMBufferSizeMB());
        config.setRAMPerThreadHardLimitMB(baseConfig.getRAMPerThreadHardLimitMB());
        config.setReaderPooling(baseConfig.getReaderPooling());
        config.setSimilarity(baseConfig.getSimilarity());
        config.setUseCompoundFile(baseConfig.getUseCompoundFile());
        return config;
    }
}

