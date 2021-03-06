/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.test.unit.index.mapper.timestamp;

import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.ParsedDocument;
import org.elasticsearch.index.mapper.SourceToParse;
import org.elasticsearch.index.mapper.internal.TimestampFieldMapper;
import org.elasticsearch.test.unit.index.mapper.MapperTests;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

/**
 */
public class TimestampMappingTests {

    @Test
    public void testSimpleDisabled() throws Exception {
        String mapping = XContentFactory.jsonBuilder().startObject().startObject("type").endObject().string();
        DocumentMapper docMapper = MapperTests.newParser().parse(mapping);
        BytesReference source = XContentFactory.jsonBuilder()
                .startObject()
                .field("field", "value")
                .endObject()
                .bytes();
        ParsedDocument doc = docMapper.parse(SourceToParse.source(source).type("type").id("1").timestamp(1));

        assertThat(doc.rootDoc().getField("_timestamp"), equalTo(null));
    }

    @Test
    public void testEnabled() throws Exception {
        String mapping = XContentFactory.jsonBuilder().startObject().startObject("type")
                .startObject("_timestamp").field("enabled", "yes").field("store", "yes").endObject()
                .endObject().endObject().string();
        DocumentMapper docMapper = MapperTests.newParser().parse(mapping);
        BytesReference source = XContentFactory.jsonBuilder()
                .startObject()
                .field("field", "value")
                .endObject()
                .bytes();
        ParsedDocument doc = docMapper.parse(SourceToParse.source(source).type("type").id("1").timestamp(1));

        assertThat(doc.rootDoc().getField("_timestamp").fieldType().stored(), equalTo(true));
        assertThat(doc.rootDoc().getField("_timestamp").fieldType().indexed(), equalTo(true));
        assertThat(doc.rootDoc().getField("_timestamp").tokenStream(docMapper.indexAnalyzer()), notNullValue());
    }

    @Test
    public void testDefaultValues() throws Exception {
        String mapping = XContentFactory.jsonBuilder().startObject().startObject("type").endObject().string();
        DocumentMapper docMapper = MapperTests.newParser().parse(mapping);
        assertThat(docMapper.timestampFieldMapper().enabled(), equalTo(TimestampFieldMapper.Defaults.ENABLED));
        assertThat(docMapper.timestampFieldMapper().stored(), equalTo(TimestampFieldMapper.Defaults.TIMESTAMP_FIELD_TYPE.stored()));
        assertThat(docMapper.timestampFieldMapper().indexed(), equalTo(TimestampFieldMapper.Defaults.TIMESTAMP_FIELD_TYPE.indexed()));
        assertThat(docMapper.timestampFieldMapper().path(), equalTo(null));
        assertThat(docMapper.timestampFieldMapper().dateTimeFormatter().format(), equalTo(TimestampFieldMapper.DEFAULT_DATE_TIME_FORMAT));
    }


    @Test
    public void testSetValues() throws Exception {
        String mapping = XContentFactory.jsonBuilder().startObject().startObject("type")
                .startObject("_timestamp")
                .field("enabled", "yes").field("store", "yes").field("index", "no")
                .field("path", "timestamp").field("format", "year")
                .endObject()
                .endObject().endObject().string();
        DocumentMapper docMapper = MapperTests.newParser().parse(mapping);
        assertThat(docMapper.timestampFieldMapper().enabled(), equalTo(true));
        assertThat(docMapper.timestampFieldMapper().stored(), equalTo(true));
        assertThat(docMapper.timestampFieldMapper().indexed(), equalTo(false));
        assertThat(docMapper.timestampFieldMapper().path(), equalTo("timestamp"));
        assertThat(docMapper.timestampFieldMapper().dateTimeFormatter().format(), equalTo("year"));
    }
}