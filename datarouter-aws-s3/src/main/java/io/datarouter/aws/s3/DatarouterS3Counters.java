/*
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.aws.s3;

import io.datarouter.instrumentation.count.Counters;

public class DatarouterS3Counters{

	private static final String PREFIX = "Datarouter client S3";
	private static final String BUCKET_NAME_ALL_BUCKETS = "allBuckets";

	public static void inc(String bucket, S3CounterSuffix suffix, long by){
		incNoBucket(suffix, by);
		incBucket(bucket, suffix, by);
	}

	public static void incNoBucket(S3CounterSuffix suffix, long by){
		incBucket(BUCKET_NAME_ALL_BUCKETS, suffix, by);
	}

	private static void incBucket(String bucket, S3CounterSuffix suffix, long by){
		String name = String.format("%s %s %s", PREFIX, bucket, suffix.suffix);
		Counters.inc(name, by);
	}

	public enum S3CounterSuffix{
		COPY_REQUESTS("copy requests"),
		DELETE_REQUESTS("delete requests"),
		DELETE_VERSION_REQUESTS("deleteVersion requests"),
		DELETE_MULTI_REQUESTS("deleteMulti requests"),
		DELETE_MULTI_KEYS("deleteMulti keys"),
		DELETE_VERSIONS_REQUESTS("deleteVersions requests"),
		DELETE_VERSIONS_KEYS("deleteVersions keys"),
		DOWNLOAD_FILE_REQUESTS("downloadFile requests"),
		DOWNLOAD_FILE_BYTES("downloadFile bytes"),
		HEAD_REQUESTS("head requests"),
		HEAD_HIT("head hits"),
		HEAD_MISS("head misses"),
		LIST_BUCKETS_REQUESTS("listBuckets requests"),
		LIST_BUCKETS_ROWS("listBuckets rows"),
		LIST_OBJECTS_REQUESTS("listObjects requests"),
		LIST_OBJECTS_ROWS("listObjects rows"),
		LIST_VERSIONS_REQUESTS("listVersions requests"),
		LIST_VERSIONS_ROWS("listVersions rows"),
		READ_REQUESTS("read requests"),
		READ_BYTES("read bytes"),
		READ_INPUT_STREAM_REQUESTS("readInputStream requests"),
		READ_PARTIAL_REQUESTS("readPartial requests"),
		READ_PARTIAL_BYTES("readPartial bytes"),
		SCAN_OBJECTS_SCANS("scanObjects scans"),
		SCAN_OBJECTS_AFTER_SCANS("scanObjectsAfter scans"),
		SCAN_BUCKETS_SCANS("scanBuckets scans"),
		SCAN_VERSIONS_SCANS("scanVersions scans"),
		SCAN_VERSIONS_FROM_SCANS("scanVersionsFrom scans"),
		UPLOAD_FILE_REQUESTS("uploadFile requests"),
		UPLOAD_FILE_BYTES("uploadFile bytes"),
		WRITE_REQUESTS("write requests"),
		WRITE_BYTES("write bytes");

		public final String suffix;

		S3CounterSuffix(String suffix){
			this.suffix = suffix;
		}
	}
}
