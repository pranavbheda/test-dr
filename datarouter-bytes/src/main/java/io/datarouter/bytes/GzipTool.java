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
package io.datarouter.bytes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.zip.GZIPOutputStream;

import io.datarouter.scanner.Ref;
import io.datarouter.scanner.Scanner;

public class GzipTool{

	public static byte[] encode(byte[] input){
		var byteArrayOutputStream = new ByteArrayOutputStream();
		try(var gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)){
			gzipOutputStream.write(input, 0, input.length);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		return byteArrayOutputStream.toByteArray();
	}

	public static Scanner<byte[]> encode(InputStream input){
		var inputBuffer = new byte[8192];
		var outputBuffer = new ByteArrayOutputStream();
		var closed = new Ref<>(false);
		try{
			var gzipOutputStream = new GZIPOutputStream(outputBuffer);
			return Scanner.generate(() -> InputStreamTool.readUntilLength(input, inputBuffer, 0, inputBuffer.length))
					.advanceUntil($ -> closed.get())
					.map(numRead -> {
						if(numRead == -1){
							OutputStreamTool.close(gzipOutputStream);
							closed.set(true);
						}
						if(numRead > 0){
							OutputStreamTool.write(gzipOutputStream, inputBuffer, 0, numRead);
						}
						// Check if gzip flushed anything before allocating a new array
						if(outputBuffer.size() > 0){
							byte[] outputChunk = outputBuffer.toByteArray();
							outputBuffer.reset();
							return outputChunk;
						}else{
							return EmptyArray.BYTE;
						}
					});
		}catch(IOException e){
			throw new UncheckedIOException(e);
		}
	}

	public static InputStream encodeToInputStream(InputStream input){
		return encode(input).apply(MultiByteArrayInputStream::new);
	}

}
