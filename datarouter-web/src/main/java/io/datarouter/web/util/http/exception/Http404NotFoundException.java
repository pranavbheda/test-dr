/**
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
package io.datarouter.web.util.http.exception;

import io.datarouter.httpclient.response.HttpStatusCode;

@SuppressWarnings("serial")
public class Http404NotFoundException extends HttpException{

	private static final int CODE = HttpStatusCode.SC_404_NOT_FOUND.getStatusCode();
	private static final String MESSAGE = HttpStatusCode.SC_404_NOT_FOUND.getMessage();

	public Http404NotFoundException(){
		super(MESSAGE, CODE);
	}

	public Http404NotFoundException(String message){
		super(message, CODE);
	}

	public Http404NotFoundException(String message, Throwable cause){
		super(message, cause, CODE);
	}

	public Http404NotFoundException(Throwable cause){
		super(MESSAGE, cause, CODE);
	}

}
