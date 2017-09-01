<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at
      http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
# Tripod [![Build Status](https://travis-ci.org/bbende/tripod.svg?branch=master)](https://travis-ci.org/bbende/tripod)

A library for bootstrapping the development of search applications.

Creates an abstraction layer between the application and the underlying search platform.

# Overview

**tripod-query-api** provides a generic API for interacting with a Lucene based search platform.

**tripod-query-solr** provides a Solr based implementation of the API.

**tripod-query-lucene** provides a Lucene based implementation of the API.

# Maven Repository for Released Artifacts

    <repositories>
        <repository>
            <snapshots>
                <enabled>false</enabled>
            </snapshots>
            <id>bintray-bbende-maven-repo</id>
            <name>bintray</name>
            <url>http://dl.bintray.com/bbende/maven-repo</url>
        </repository>
    </repositories>

# How can I use this in my application with Solr?

1) Add a Maven dependency on tripod-query-solr:
  
        <dependency>
          <groupId>com.tripod</groupId>
          <artifactId>tripod-query-solr</artifactId>
          <version>${tripod.version}</version>
        </dependency>
    
2) Create an enumeration that defines the available fields and implements the Field interface:

       public enum FooField implements Field {
          ID("id"),
          TITLE("title);

          private String fieldName;

          ExampleField(String fieldName) {
              this.fieldName = fieldName;
          }
          @Override
          public String getName() {
              return fieldName;
          }
       }
    
3) Create a domain object that extends QueryResult:

        public class Foo extends AbstractQueryResult {

          private String title;

          public Foo(String id) {
              super(Foo.ID, id);
          }
          public String getTitle() {
              return title;
          }
          public void setTitle(String title) {
              this.title = title;
          }
        }
    
4) Create a transformer that takes a SolrDocument and produces the domain object above:

        public class FooTransformer implements SolrDocumentTransformer<Foo> {
            @Override
            public Foo transform(SolrDocument input) {
                String id = getString(input, FooField.ID.getName());
                String title = getString(input, FooField.TITLE.getName());

                Foo foo = new Foo(id);
                foo.setTitle(title);
                return foo;
            }
        }
    
5) Create a query service that extends SolrQueryService and uses the transformer above:

        public class FooQueryService extends SolrQueryService<Foo> {
            public FooQueryService(SolrClient solrClient) {
                super(solrClient, new StandardSolrQueryTransformer(), new FooTransformer());
            }
        }

6) Initialize the query service with the appropriate SolrClient and perform queries:

        SolrClient solrClient = ...
        QueryService<Foo> queryService = new FooQueryService(solrClient);

        Query query = new Query("id:1");
        QueryResults<Foo> results = queryService.search(query);
    
    
For additional information see the example in [tripod-query-solr/src/test/java](https://github.com/bbende/tripod/tree/master/tripod-query-solr/src/test/java/com/tripod/solr/example).

# How can I use this in my application with Lucene?

NOTE: Lucene support is not part of the Tripod 0.1.0 release.

1) Add a Maven dependency on tripod-query-lucene:

        <dependency>
          <groupId>com.tripod</groupId>
          <artifactId>tripod-query-lucene</artifactId>
          <version>${tripod.version}</version>
        </dependency>
    
2) Create an enumeration that defines the available fields and implements the Field interface:

        public enum FooField implements Field {
            ID("id"),
            TITLE("title"),

            private String fieldName;

            ExampleField(String fieldName) {
                this.fieldName = fieldName;
            }
            @Override
            public String getName() {
                return fieldName;
            }
        }

3) Create a SortTypeFactory that defines the sort type for each of the above fields:

        public class FooFieldSortTypeFactory implements SortTypeFactory {
        
            private static final Map<FooField,SortField.Type> SORT_FIELD_TYPES = new HashMap<>();
        
            static {
                SORT_FIELD_TYPES.put(ExampleField.ID, SortField.Type.STRING);
                SORT_FIELD_TYPES.put(ExampleField.TITLE, SortField.Type.STRING);
             }
        
            @Override
            public SortField.Type getSortType(final Field f) {
                return SORT_FIELD_TYPES.get(f);
            }
        
        }
    
    
3) Create a domain object that extends QueryResult:

        public class Foo extends AbstractQueryResult {

            private String title;

            public Foo(String id) {
                super(FooField.ID, id);
            }
            public String getTitle() {
                return title;
            }
            public void setTitle(String title) {
                this.title = title;
            }
        }
    
4) Create a transformer that takes a Lucene Document and produces the domain object above:

        public class FooTransformer implements LuceneDocumentTransformer<Foo> {
            @Override
            public Foo transform(Document input) {
                String id = input.get(ExampleField.ID.getName());
                String title = input.get(ExampleField.TITLE.getName());

                Foo foo = new Foo(id);
                foo.setTitle(title);
                return foo;
            }
        }
    
5) Create a query service that extends LuceneQueryService and uses the transformer above:

        public FooQueryService(final SearcherManager searcherManager, final String defaultField, 
                               final Analyzer analyzer, final FacetsConfig facetsConfig) 
                               extends LuceneQueryService<Foo> {
                super(searcherManager, analyzer,
                        new StandardLuceneQueryTransformer(defaultField, analyzer, facetsConfig),
                        new FooTransformer());
            }

6) Initialize the query service with the appropriate SeacherManager, Analyzer, and default field, and perform queries:

        String defaultField = ...
        Analyzer analyzer = ...
        SearcherManager searcherManager =...
        FacetsConfig facetsConfig = ...

        QueryService<Foo> queryService = new FooQueryService(searcherManager, defaultField, analyzer, facetsConfig);

        Query query = new Query("id:1");
        QueryResults<Foo> results = queryService.search(query);
    
    
For additional information see the example in [tripod-query-lucene/src/test/java](https://github.com/bbende/tripod/tree/master/tripod-query-lucene/src/test/java/com/tripod/lucene/example).

# Release Instructions

    mvn release:prepare -DdryRun=true -Pfull,sign
    mvn release:clean
    mvn release:prepare -Pfull
    mvn release:perform -Pfull
    git push --all && git push --tags
