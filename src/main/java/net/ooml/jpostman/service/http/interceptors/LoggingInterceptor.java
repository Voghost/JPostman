package net.ooml.jpostman.service.http.interceptors;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Logging interceptor for HTTP requests and responses
 */
public class LoggingInterceptor implements Interceptor {
    private static final Logger log = LoggerFactory.getLogger(LoggingInterceptor.class);

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        // Log request
        long startTime = System.currentTimeMillis();
        log.info("--> {} {}", request.method(), request.url());

        // Log request headers (in debug mode)
        if (log.isDebugEnabled()) {
            request.headers().forEach(header -> {
                log.debug("    {}: {}", header.getFirst(), header.getSecond());
            });
        }

        Response response;
        try {
            // Execute request
            response = chain.proceed(request);

            // Calculate duration
            long duration = System.currentTimeMillis() - startTime;

            // Log response
            log.info("<-- {} {} ({}ms)", response.code(), request.url(), duration);

            // Log response headers (in debug mode)
            if (log.isDebugEnabled()) {
                response.headers().forEach(header -> {
                    log.debug("    {}: {}", header.getFirst(), header.getSecond());
                });
            }

            return response;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("<-- HTTP FAILED: {} ({}ms)", request.url(), duration, e);
            throw e;
        }
    }
}
