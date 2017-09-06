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
package com.bbende.tripod.lucene.query.service;

import com.bbende.tripod.api.Field;
import com.bbende.tripod.api.TransformException;
import com.bbende.tripod.api.query.Query;
import com.bbende.tripod.api.query.result.FacetCount;
import com.bbende.tripod.api.query.result.FacetResult;
import com.bbende.tripod.api.query.result.Highlight;
import com.bbende.tripod.api.query.result.QueryResult;
import com.bbende.tripod.api.query.result.QueryResults;
import com.bbende.tripod.api.query.service.QueryException;
import com.bbende.tripod.lucene.SortTypeFactory;
import com.bbende.tripod.lucene.query.LuceneCursorMark;
import com.bbende.tripod.lucene.query.LuceneQueryTransformer;
import com.bbende.tripod.lucene.query.serialization.FieldDocSerializer;
import com.bbende.tripod.lucene.query.serialization.StandardFieldDocSerializer;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.LabelAndValue;
import org.apache.lucene.facet.sortedset.DefaultSortedSetDocValuesReaderState;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetCounts;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesReaderState;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.FieldDoc;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiCollector;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLEncoder;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.TextFragment;
import org.apache.lucene.search.highlight.TokenSources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Base class for all Lucene services.
 *
 * NOTE: Clients should use SearcherManagerRefresher in order to periodically open new searchers and see new data.
 *
 * @author bbende
 */
public class LuceneService<QR extends QueryResult> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LuceneService.class);

    static final Integer DEFAULT_MAX_SEARCH_RESULTS = new Integer(10000);

    protected final Analyzer analyzer;
    protected final SearcherManager searcherManager;
    protected final LuceneQueryTransformer queryTransformer;
    protected final LuceneDocumentTransformer<QR> documentTransformer;
    protected final SortTypeFactory sortTypeFactory;
    protected final FieldDocSerializer fieldDocSerializer;
    protected final Integer maxSearchResults;

    private String highlightPreTag = "<strong>";
    private String highlightPostTag = "</strong>";

    public LuceneService(final SearcherManager searcherManager,
                         final Analyzer analyzer,
                         final LuceneQueryTransformer queryTransformer,
                         final LuceneDocumentTransformer<QR> documentTransformer,
                         final SortTypeFactory sortTypeFactory) {
        this(searcherManager, analyzer, queryTransformer, documentTransformer,
                sortTypeFactory, DEFAULT_MAX_SEARCH_RESULTS);
    }

    public LuceneService(final SearcherManager searcherManager,
                         final Analyzer analyzer,
                         final LuceneQueryTransformer queryTransformer,
                         final LuceneDocumentTransformer<QR> documentTransformer,
                         final SortTypeFactory sortTypeFactory,
                         final Integer maxSearchResults) {
        this.searcherManager = searcherManager;
        this.analyzer = analyzer;
        this.queryTransformer = queryTransformer;
        this.documentTransformer = documentTransformer;
        this.sortTypeFactory = sortTypeFactory;
        this.maxSearchResults = maxSearchResults;
        this.fieldDocSerializer = new StandardFieldDocSerializer();

        Validate.notNull(this.searcherManager);
        Validate.notNull(this.queryTransformer);
        Validate.notNull(this.documentTransformer);
        Validate.notNull(this.sortTypeFactory);
        Validate.notNull(this.maxSearchResults);
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

    /**
     * Common logic for sub-classes to perform searches.
     *
     * @param query the query
     * @return the QueryResults
     * @throws QueryException if an error occurred performing the search
     */
    protected QueryResults<QR> performSearch(final Query query) throws QueryException {
        IndexSearcher searcher = null;
        try {
            // Acquire an IndexSearcher
            searcher = searcherManager.acquire();

            // Start the results builder with the offset and rows from the query
            final QueryResults.Builder<QR> resultsBuilder = new QueryResults.Builder<QR>()
                    .offset(query.getOffset())
                    .pageSize(query.getRows());

            // Create a searcher and get a Lucene query
            final org.apache.lucene.search.Query luceneQuery = queryTransformer.transform(query);

            // Get the return fields
            final Set<String> fieldsToLoad = new HashSet<>();
            if (query.getReturnFields() != null) {
                query.getReturnFields().stream().forEach(f -> fieldsToLoad.add(f.getName()));
            }

            // Get the facet fields
            final Set<String> facetFields = new HashSet<>();
            if (query.getFacetFields() != null) {
                query.getFacetFields().stream().forEach(f -> facetFields.add(f.getName()));
            }

            final Sort sort = getSort(query.getSorts(), sortTypeFactory);
            final Highlighter highlighter = getHighlighter(query, luceneQuery);

            // Collector to use when faceting
            final FacetsCollector facetsCollector = new FacetsCollector();

            // fillFields needs to be true to get instances of FieldDoc coming back for the ScoreDoc[]
            final boolean fillFields = true;
            final boolean trackScores = false;
            final boolean trackMaxScore = false;

            final boolean usingCursorMark = query.getCursorMark() != null;

            // if using cursorMark we only need to find # of rows after the cursors, otherwise we need
            // to find all the results up to maxSearchResults
            final int numResults = usingCursorMark ? query.getRows() : maxSearchResults;

            // if not using cursorMark this should end up being null which would be
            // the same thing as calling TopFieldCollector.create without afterDoc
            final FieldDoc prevLastDoc = toFieldDoc(query.getCursorMark());

            final TopFieldCollector topFieldCollector = TopFieldCollector.create(
                    sort, numResults, prevLastDoc, fillFields, trackScores, trackMaxScore);

            // Wrapped collector depending on whether faceting or not
            final Collector collector = facetFields.isEmpty()
                    ? MultiCollector.wrap(topFieldCollector) : MultiCollector.wrap(topFieldCollector, facetsCollector);

            // Perform the Lucene query
            final long startTime = System.currentTimeMillis();

            ScoreDoc[] scoreDocs;
            if (usingCursorMark) {
                FacetsCollector.searchAfter(searcher, prevLastDoc, luceneQuery, query.getRows(), sort, collector);
                scoreDocs = topFieldCollector.topDocs().scoreDocs;
                LOGGER.debug("Queried with cursorMark = " + query.getCursorMark());
            } else {
                searcher.search(luceneQuery, collector);
                scoreDocs = topFieldCollector.topDocs(query.getOffset(), query.getRows()).scoreDocs;
                LOGGER.debug("Queried with offset = " + query.getOffset());
            }

            LOGGER.debug("Query executed in " + (System.currentTimeMillis() - startTime));

            // Transform each Lucene Document to a QueryResult
            ScoreDoc lastDoc = null;
            for (ScoreDoc scoreDoc : scoreDocs) {
                final Document doc = getDoc(searcher, scoreDoc.doc, fieldsToLoad);
                final QR result = documentTransformer.transform(doc);
                performHighlighting(searcher, query, scoreDoc, doc, highlighter, result);

                resultsBuilder.addResult(result);
                lastDoc = scoreDoc;
            }

            // Get faceting results
            processFacetResults(searcher, facetsCollector, facetFields, resultsBuilder);

            // Send back the last doc as a cursorMark so it can be passed back for the next page
            // If lastDoc is null it means there were no results, so send back the same cursorMark that was passed in
            if (usingCursorMark) {
                if (lastDoc == null) {
                    resultsBuilder.cursorMark(query.getCursorMark());
                } else {
                    resultsBuilder.cursorMark(toCursorMark((FieldDoc)lastDoc));
                }
            }

            resultsBuilder.totalResults(topFieldCollector.getTotalHits());
            return resultsBuilder.build();

        } catch (TransformException e) {
            throw new QueryException("A transform error occurred");
        } catch (IOException | InvalidTokenOffsetsException e) {
            throw new QueryException("Unexpected error occurred performing query", e);
        } finally {
            if (searcher != null) {
                try {
                    searcherManager.release(searcher);
                } catch (IOException e) {
                    LOGGER.warn("Error releasing IndexSearcher: " + e.getMessage(), e);
                }
            }
        }
    }

    private String toCursorMark(final FieldDoc fieldDoc) {
        final byte[] serializedFieldDoc = fieldDocSerializer.serialize(fieldDoc);
        return Base64.getEncoder().encodeToString(serializedFieldDoc);
    }

    private FieldDoc toFieldDoc(final String cursorMark) {
        if (cursorMark == null || LuceneCursorMark.START.equals(cursorMark)) {
            return null;
        }

        final byte[] rawCursorMark = Base64.getDecoder().decode(cursorMark);
        return fieldDocSerializer.deserialize(rawCursorMark);
    }

    /**
     * @param query the tripod query being performed
     * @param luceneQuery the Lucene query being performed
     * @return the highlighter to use if the tripod query has one or more highlight fields, or null
     */
    private Highlighter getHighlighter(final Query query, final org.apache.lucene.search.Query luceneQuery) {
        Highlighter highlighter = null;
        if (query.getHighlightFields() != null && query.getHighlightFields().size() > 0) {
            SimpleHTMLEncoder simpleHTMLEncoder = new SimpleHTMLEncoder();
            SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter(
                    getHighlightPreTag(), getHighlightPostTag());
            highlighter = new Highlighter(simpleHTMLFormatter, simpleHTMLEncoder, new QueryScorer(luceneQuery));
        }
        return highlighter;
    }

    /**
     * @param searcher the IndexSearcher
     * @param doc the doc to load
     * @param fieldsToLoad the fields of the doc to load
     * @return the Document with the given fields loaded
     * @throws IOException if an error occurs loading the Document
     */
    protected Document getDoc(final IndexSearcher searcher, final int doc, final Set<String> fieldsToLoad)
            throws IOException {
        return LuceneServiceUtil.getDoc(searcher, doc, fieldsToLoad);
    }

    /**
     * Converts the Tripod Sort clauses to a Lucene Sort instance.
     *
     * @param sorts the Tripod Sorts
     * @return the Lucene Sort matching the given Tripod Sorts,
     *              or the Lucene Sort for relevance order if no sorts are specified
     */
    protected Sort getSort(List<com.bbende.tripod.api.query.Sort> sorts, SortTypeFactory sortTypeFactory) {
        return LuceneServiceUtil.getSort(sorts, sortTypeFactory);
    }

    /**
     * Performs highlighting for a given query and a given document.
     *
     * @param indexSearcher the IndexSearcher performing the query
     * @param query the Tripod LuceneQuery
     * @param scoreDoc the Lucene ScoreDoc
     * @param doc the Lucene Document
     * @param highlighter the Highlighter to use
     * @param result the QueryResult to add the highlights to
     * @throws IOException if an error occurs performing the highlighting
     * @throws InvalidTokenOffsetsException if an error occurs performing the highlighting
     */
    protected void performHighlighting(final IndexSearcher indexSearcher, final Query query, final ScoreDoc scoreDoc,
                                       final Document doc, final Highlighter highlighter, final QR result)
            throws IOException, InvalidTokenOffsetsException {

        if (query.getHighlightFields() == null || query.getHighlightFields().isEmpty()) {
            return;
        }

        final List<Highlight> highlights = new ArrayList<>();
        final List<String> hlFieldNames = getHighlightFieldNames(query, doc);

        // process each field to highlight on
        for (String hlField : hlFieldNames) {
            final String text = doc.get(hlField);
            if (StringUtils.isEmpty(text)) {
                continue;
            }

            final List<String> snippets = new ArrayList<>();
            final Fields tvFields = indexSearcher.getIndexReader().getTermVectors(scoreDoc.doc);
            final int maxStartOffset = highlighter.getMaxDocCharsToAnalyze() -1;

            // get the snippets for the given field
            final TokenStream tokenStream = TokenSources.getTokenStream(hlField, tvFields, text, analyzer, maxStartOffset);
            final TextFragment[] textFragments = highlighter.getBestTextFragments(tokenStream, text, false, 10);
            for (TextFragment textFragment : textFragments) {
                if (textFragment != null && textFragment.getScore() > 0) {
                    snippets.add(textFragment.toString());
                }
            }

            // if we have snippets then add a highlight result to the QueryResult
            if (snippets.size() > 0) {
                highlights.add(new Highlight(hlField, snippets));
            }
        }

        result.setHighlights(highlights);
    }

    /**
     * @param query the query being performed
     * @param doc the doc being highlighted
     * @return the list of field names to highlight on coming from the query, or if the query has
     *              highlight on all fields then we get all the field names fromt he document
     */
    private List<String> getHighlightFieldNames(Query query, Document doc) {
        final List<String> hlFieldNames = new ArrayList<>();
        if (query.getHighlightFields().size() == 1
                && query.getHighlightFields().get(0).getName().equals(Field.ALL_FIELDS.getName())) {
            hlFieldNames.addAll(doc.getFields().stream().map(IndexableField::name)
                    .collect(Collectors.toList()));
        } else {
            hlFieldNames.addAll(query.getHighlightFields().stream().map(Field::getName)
                    .collect(Collectors.toList()));
        }
        return hlFieldNames;
    }

    /**
     * Processes the faceting results and adds them to the QueryResults builder.
     *
     * @param indexSearcher the IndexSearcher performing the query
     * @param facetsCollector the FacetsCollector that was used for the search
     * @param facetFields the fields to Facet on
     * @param resultBuilder the QueryResults.Builder
     * @throws IOException if an error occurs performing faceting
     */
    protected void processFacetResults(final IndexSearcher indexSearcher, final FacetsCollector facetsCollector, final Set<String> facetFields,
                                       final QueryResults.Builder<QR> resultBuilder) throws IOException {
        if (facetFields == null) {
            return;
        }

        for (String facetField : facetFields) {
            final List<FacetCount> facetResultCounts = new ArrayList<>();

            // TODO this will produce an exception if no documents were indexed with the field to facet on
            // java.lang.IllegalArgumentException: field "foo" was not indexed with SortedSetDocValues
            // at org.apache.lucene.facet.sortedset.DefaultSortedSetDocValuesReaderState.<init>(DefaultSortedSetDocValuesReaderState.java:72)

           if (facetsCollector.getMatchingDocs() != null && facetsCollector.getMatchingDocs().size() > 0) {
                final SortedSetDocValuesReaderState state = new DefaultSortedSetDocValuesReaderState(indexSearcher.getIndexReader(), facetField);
                final Facets facets = new SortedSetDocValuesFacetCounts(state, facetsCollector);

                org.apache.lucene.facet.FacetResult result = facets.getTopChildren(10, facetField);
                for (int i = 0; i < result.childCount; i++) {
                    LabelAndValue lv = result.labelValues[i];
                    facetResultCounts.add(new FacetCount(lv.label, lv.value.longValue()));
                }
            }
            resultBuilder.addFacetResult(new FacetResult(facetField, facetResultCounts));
        }
    }

}
