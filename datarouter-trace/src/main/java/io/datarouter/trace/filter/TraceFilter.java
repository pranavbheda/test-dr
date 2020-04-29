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
package io.datarouter.trace.filter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.httpclient.circuitbreaker.DatarouterHttpClientIoExceptionCircuitBreaker;
import io.datarouter.inject.DatarouterInjector;
import io.datarouter.instrumentation.trace.TraceDto;
import io.datarouter.instrumentation.trace.TraceEntityDto;
import io.datarouter.instrumentation.trace.TraceSpanDto;
import io.datarouter.instrumentation.trace.TraceThreadDto;
import io.datarouter.instrumentation.trace.Tracer;
import io.datarouter.instrumentation.trace.TracerThreadLocal;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.trace.conveyor.local.FilterToMemoryBufferForLocal;
import io.datarouter.trace.conveyor.publisher.FilterToMemoryBufferForPublisher;
import io.datarouter.trace.service.TraceUrlService;
import io.datarouter.trace.settings.DatarouterTraceFilterSettingRoot;
import io.datarouter.util.UlidTool;
import io.datarouter.util.tracer.DatarouterTracer;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.HandlerMetrics;
import io.datarouter.web.inject.InjectorRetriever;
import io.datarouter.web.user.session.CurrentSessionInfo;
import io.datarouter.web.user.session.service.Session;
import io.datarouter.web.util.RequestAttributeTool;
import io.datarouter.web.util.http.RequestTool;

public abstract class TraceFilter implements Filter, InjectorRetriever{
	private static final Logger logger = LoggerFactory.getLogger(TraceFilter.class);

	private DatarouterProperties datarouterProperties;
	private DatarouterTraceFilterSettingRoot traceSettings;
	private FilterToMemoryBufferForLocal traceBufferForLocal;
	private FilterToMemoryBufferForPublisher traceBufferForPublisher;
	private TraceUrlService urlService;
	private CurrentSessionInfo currentSessionInfo;
	private HandlerMetrics handlerMetrics;

	@Override
	public void init(FilterConfig filterConfig){
		DatarouterInjector injector = getInjector(filterConfig.getServletContext());
		datarouterProperties = injector.getInstance(DatarouterProperties.class);
		traceBufferForLocal = injector.getInstance(FilterToMemoryBufferForLocal.class);
		traceBufferForPublisher = injector.getInstance(FilterToMemoryBufferForPublisher.class);
		traceSettings = injector.getInstance(DatarouterTraceFilterSettingRoot.class);
		urlService = injector.getInstance(TraceUrlService.class);
		currentSessionInfo = injector.getInstance(CurrentSessionInfo.class);
		handlerMetrics = injector.getInstance(HandlerMetrics.class);
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain fc) throws IOException, ServletException{
		try{
			HttpServletRequest request = (HttpServletRequest)req;
			HttpServletResponse response = (HttpServletResponse)res;

			String traceId = UlidTool.nextUlid();
			RequestAttributeTool.set(request, BaseHandler.TRACE_URL_REQUEST_ATTRIBUTE, urlService
					.buildTraceForCurrentServer(traceId));
			if(traceSettings.addTraceIdHeader.get()){
				response.setHeader(DatarouterHttpClientIoExceptionCircuitBreaker.X_TRACE_ID, traceId);
			}
			Long created = System.currentTimeMillis();
			TraceDto trace = new TraceDto(traceId, created);
			trace.setContext(request.getContextPath());
			trace.setType(request.getRequestURI().toString());
			trace.setParams(request.getQueryString());

			// bind these to all threads, even if tracing is disabled
			String serverName = datarouterProperties.getServerName();
			Tracer tracer = new DatarouterTracer(serverName, traceId, null);
			TracerThreadLocal.bindToThread(tracer);

			String requestThreadName = (request.getContextPath() + " request").trim();
			tracer.createAndStartThread(requestThreadName, System.currentTimeMillis());

			boolean errored = false;
			try{
				fc.doFilter(req, res);
			}catch(Exception e){
				errored = true;
				throw e;
			}finally{
				tracer.finishThread();
				trace.markFinished();

				int saveCutoff = traceSettings.saveTracesOverMs.get();
				boolean requestForceSave = RequestTool.getBoolean(request, "trace", false);
				boolean tracerForceSave = tracer.getForceSave();
				String requestId = request.getHeader(DatarouterHttpClientIoExceptionCircuitBreaker.X_REQUEST_ID);

				// save requests
				Long traceDurationMs = trace.getDuration();
				if(traceDurationMs > saveCutoff || requestForceSave || tracerForceSave || errored){
					List<TraceThreadDto> threads = new ArrayList<>(tracer.getThreadQueue());
					List<TraceSpanDto> spans = new ArrayList<>(tracer.getSpanQueue());
					trace.setDiscardedThreadCount(tracer.getDiscardedThreadCount());
					TraceEntityDto entityDto = new TraceEntityDto(trace, threads, spans);
					String destination = offer(entityDto);
					String userAgent = RequestTool.getUserAgent(request);
					String userToken = currentSessionInfo.getSession(request)
							.map(Session::getUserToken)
							.orElse("unknown");
					logger.warn("Trace saved to={} traceId={} durationMs={} requestId={} path={} query={}"
							+ " userAgent=\"{}\" userToken={}", destination, trace.getTraceId(), traceDurationMs,
							requestId, trace.getType(), trace.getParams(), userAgent, userToken);
				}else if(traceDurationMs > traceSettings.logTracesOverMs.get()){
					// only log once
					logger.warn("Trace logged durationMs={} requestId={} path={} query={}", traceDurationMs,
							requestId, trace.getType(), trace.getParams());
				}
				Optional<Class<? extends BaseHandler>> handlerClassOpt = RequestAttributeTool
						.get(request, BaseHandler.HANDLER_CLASS);
				Optional<Method> handlerMethodOpt = RequestAttributeTool.get(request, BaseHandler.HANDLER_METHOD);
				if(handlerClassOpt.isPresent() && handlerMethodOpt.isPresent()){
					Class<? extends BaseHandler> handlerClass = handlerClassOpt.get();
					if(traceSettings.latencyRecordedHandlers.get().contains(handlerClass.getName())){
						handlerMetrics.saveMethodLatency(handlerClass, handlerMethodOpt.get(), traceDurationMs);
					}
				}
			}
		}finally{
			TracerThreadLocal.clearFromThread();
		}
	}

	private String offer(TraceEntityDto dto){
		return Stream.of(traceBufferForLocal.offer(dto), traceBufferForPublisher.offer(dto))
				.flatMap(Optional::stream)
				.collect(Collectors.joining(", "));
	}

}