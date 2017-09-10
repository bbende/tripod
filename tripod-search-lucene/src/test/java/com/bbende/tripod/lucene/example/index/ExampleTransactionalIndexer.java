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
package com.bbende.tripod.lucene.example.index;

import com.bbende.tripod.lucene.example.Example;
import com.bbende.tripod.lucene.index.LuceneTransactionalIndexer;
import com.bbende.tripod.lucene.index.SnapshotRAMDirectoryFactory;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;

/**
 * Transactional Lucene Indexer for Example entities.
 *
 * @author bbende
 */
public class ExampleTransactionalIndexer extends LuceneTransactionalIndexer<Example> {

    public ExampleTransactionalIndexer(
            final Directory directory,
            final IndexWriterConfig indexWriterConfig,
            final FacetsConfig facetsConfig) {
        super(directory, indexWriterConfig, facetsConfig,
                new SnapshotRAMDirectoryFactory(),
                new ExampleIndexTransformer());
    }

}
