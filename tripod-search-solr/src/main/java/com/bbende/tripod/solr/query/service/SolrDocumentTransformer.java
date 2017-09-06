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
package com.bbende.tripod.solr.query.service;

import com.bbende.tripod.api.Transformer;
import com.bbende.tripod.api.entity.Entity;
import org.apache.solr.common.SolrDocument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Transforms a SolrDocument to the given type of QueryResult.
 *
 * @author bbende
 */
public interface SolrDocumentTransformer<E extends Entity> extends Transformer<SolrDocument,E> {

    /**
     * Safely gets an Set of Strings for the given field.
     *
     * @param solrDocument the document to get the field from
     * @param field the field to get
     * @return the strings values for the field
     */
    default Set<String> getStrings(SolrDocument solrDocument, String field) {
        final Collection<Object> objects = solrDocument.getFieldValues(field);
        Set<String> strs = new HashSet<>();
        if (objects != null) {
            for (Object obj : objects) {
                strs.add((String) obj);
            }
        }
        return strs;
    }

    /**
     * Safely gets a String for the given field.
     *
     * @param solrDocument the docoument to get the field from
     * @param field the field to get
     * @return the String value of the field
     */
    default String getString(SolrDocument solrDocument, String field) {
        String returnVal = null;
        final Object object = solrDocument.getFieldValue(field);
        if (object != null) {
            if (object instanceof String) {
                returnVal = (String) object;
            } else if (object instanceof ArrayList) {
                Collection<Object> objects = solrDocument.getFieldValues(field);
                if (objects.size() > 0) {
                    returnVal = (String) objects.iterator().next();
                }
            } else {
                returnVal = object.toString();
            }
        }
        return returnVal;
    }

    /**
     * Safely gets a Long for the given field.
     *
     * @param solrDocument the document to get the field from
     * @param field the field to get
     * @return the Long value of the field, or 0 if the value was null
     */
    default Long getLong(SolrDocument solrDocument, String field) {
        final Object object = solrDocument.getFieldValue(field);
        if (object != null) {
            return (Long) object;
        } else {
            return new Long(0);
        }
    }

    /**
     * Safely gets a Date for the given field.
     *
     * @param solrDocument the document to get the field from
     * @param field the field to get
     * @return the Date value of the field, or null
     */
    default Date getDate(SolrDocument solrDocument, String field) {
        final Object object = solrDocument.getFieldValue(field);
        if (object != null) {
            return (Date) object;
        } else {
            return null;
        }
    }

}
