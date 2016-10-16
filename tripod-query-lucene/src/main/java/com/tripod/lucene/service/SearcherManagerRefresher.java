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
package com.tripod.lucene.service;

import org.apache.lucene.search.SearcherManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Refreshes a SearcherManager by calling maybeRefresh() on a fixed interval.
 *
 * A SearcherManagerRefresher should be used to refresh the SearcherManager when using a LuceneQueryService.
 *
 * @author bbende
 */
public class SearcherManagerRefresher {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearcherManagerRefresher.class);

    private final SearcherManager searcherManager;
    private final ScheduledExecutorService executorService;
    private final long refreshMillis;

    public SearcherManagerRefresher(final SearcherManager searcherManager, final long refreshMillis) {
        this.searcherManager = searcherManager;
        this.refreshMillis = refreshMillis;
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    searcherManager.maybeRefresh();
                } catch (IOException e) {
                    LOGGER.warn("Error refreshing SearcherManager: " + e.getMessage(), e);
                }
            }
        }, refreshMillis, refreshMillis, TimeUnit.MILLISECONDS)    ;
    }

    public void stop() {
        // Disable new tasks from being submitted
        executorService.shutdown();
        try {
            // Wait a while for existing tasks to terminate
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                executorService.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!executorService.awaitTermination(30, TimeUnit.SECONDS))
                    LOGGER.error("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            executorService.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

}
