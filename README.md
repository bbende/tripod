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
# Tripod

A library for bootstrapping the development of search applications.

Creates an abstraction layer between the application and the underlying search platform.

# Overview

**tripod-query-api** provides a generic API for interacting with a Lucene based search platform.

**tripod-query-solr** provides a Solr based implementation of the API.

# How can I use this in my application?

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

    public class Foo extends QueryResult<String> {

        private String title;

        public Foo(String s) {
            super(s);
        }
        public String getTitle() {
            return title;
        }
        public void setTitle(String title) {
            this.title = title;
        }
    }
    
4) Create a transformer that can take a SolrDocument and produce the domain object above:

    public class FooTransformer implements SolrDocumentTransformer<Foo> {
        @Override
        public Foo transform(SolrDocument input) {
            String id = getString(input, ExampleField.ID.getName());
            String title = getString(input, ExampleField.TITLE.getName());
            
            Foo foo = new Foo(id);
            foo.setTitle(title);
            return foo;
        }
    }
    
5) Create a query service that extends SolrQueryService and uses the transformer above:

    public class FooQueryService extends SolrQueryService<Query,Foo> {
        public FooQueryService(SolrClient solrClient) {
            super(solrClient, new StandardSolrQueryFactory<>(), new FooTransformer());
        }
    }

6) Initialize the query service with the appropriate SolrClient and perform queries:

    SolrClient solrClient = ...
    FooQueryService queryService = new FooQueryService(solrClient);
    
    Query query = new Query("id:1");
    QueryResults<Foo> results = queryService.search(query);
    
    
For additional information see the example in [tripod-query-solr/src/test/java](https://github.com/bbende/tripod/tree/master/tripod-query-solr/src/test/java/com/tripod/solr/example).
