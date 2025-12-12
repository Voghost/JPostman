package net.ooml.jpostman.service.http;

import net.ooml.jpostman.config.Constants;
import net.ooml.jpostman.model.Request;
import net.ooml.jpostman.model.Response;
import net.ooml.jpostman.service.http.interceptors.LoggingInterceptor;
import net.ooml.jpostman.service.variable.VariableResolver;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * HTTP client service using OkHttp
 * Manages HTTP requests execution
 */
public class HttpClientService {
    private static final Logger log = LoggerFactory.getLogger(HttpClientService.class);

    private final OkHttpClient client;
    private final HttpRequestBuilder requestBuilder;
    private final HttpResponseHandler responseHandler;

    /**
     * Callback interface for async requests
     */
    public interface ResponseCallback {
        void onSuccess(Response response);
        void onFailure(Response response);
    }

    public HttpClientService(VariableResolver variableResolver) {
        this(variableResolver, Constants.DEFAULT_TIMEOUT_MS);
    }

    public HttpClientService(VariableResolver variableResolver, int timeoutMs) {
        this.client = createOkHttpClient(timeoutMs);
        this.requestBuilder = new HttpRequestBuilder(variableResolver);
        this.responseHandler = new HttpResponseHandler();
    }

    /**
     * Create configured OkHttpClient
     */
    private OkHttpClient createOkHttpClient(int timeoutMs) {
        return new OkHttpClient.Builder()
                .connectTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .readTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .writeTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .followRedirects(Constants.DEFAULT_FOLLOW_REDIRECTS)
                .followSslRedirects(Constants.DEFAULT_FOLLOW_REDIRECTS)
                .addInterceptor(new LoggingInterceptor())
                .build();
    }

    /**
     * Execute HTTP request synchronously
     */
    public Response execute(Request request) {
        log.info("Executing HTTP request: {} {}", request.getMethod(), request.getUrl());

        long startTime = System.currentTimeMillis();

        try {
            // Build OkHttp request
            okhttp3.Request okHttpRequest = requestBuilder.build(request);

            // Execute request
            okhttp3.Response okHttpResponse = client.newCall(okHttpRequest).execute();

            // Calculate duration
            long duration = System.currentTimeMillis() - startTime;

            // Handle response
            Response response = responseHandler.handleResponse(okHttpResponse, duration);

            log.info("Request completed: {} {} ({}ms)",
                    response.getStatusCode(), request.getUrl(), duration);

            return response;

        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            return Response.createError("Invalid request: " + e.getMessage());

        } catch (java.net.SocketTimeoutException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Request timeout: {} ({}ms)", request.getUrl(), duration);
            return Response.createTimeout();

        } catch (IOException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Request failed: {} ({}ms)", request.getUrl(), duration, e);
            return responseHandler.handleError(e);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Unexpected error during request: {} ({}ms)", request.getUrl(), duration, e);
            return Response.createError("Unexpected error: " + e.getMessage());
        }
    }

    /**
     * Execute HTTP request asynchronously
     */
    public void executeAsync(Request request, ResponseCallback callback) {
        log.info("Executing async HTTP request: {} {}", request.getMethod(), request.getUrl());

        long startTime = System.currentTimeMillis();

        try {
            // Build OkHttp request
            okhttp3.Request okHttpRequest = requestBuilder.build(request);

            // Execute request asynchronously
            client.newCall(okHttpRequest).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, okhttp3.Response okHttpResponse) {
                    long duration = System.currentTimeMillis() - startTime;
                    Response response = responseHandler.handleResponse(okHttpResponse, duration);

                    log.info("Async request completed: {} {} ({}ms)",
                            response.getStatusCode(), request.getUrl(), duration);

                    if (callback != null) {
                        if (response.isSuccessful()) {
                            callback.onSuccess(response);
                        } else {
                            callback.onFailure(response);
                        }
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    long duration = System.currentTimeMillis() - startTime;
                    log.error("Async request failed: {} ({}ms)", request.getUrl(), duration, e);

                    Response response = responseHandler.handleError(e);

                    if (callback != null) {
                        callback.onFailure(response);
                    }
                }
            });

        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            Response response = Response.createError("Invalid request: " + e.getMessage());
            if (callback != null) {
                callback.onFailure(response);
            }

        } catch (Exception e) {
            log.error("Unexpected error during async request: {}", request.getUrl(), e);
            Response response = Response.createError("Unexpected error: " + e.getMessage());
            if (callback != null) {
                callback.onFailure(response);
            }
        }
    }

    /**
     * Cancel all pending requests
     */
    public void cancelAll() {
        client.dispatcher().cancelAll();
        log.info("All pending requests cancelled");
    }

    /**
     * Get active request count
     */
    public int getActiveRequestCount() {
        return client.dispatcher().runningCallsCount();
    }

    /**
     * Shutdown the client
     */
    public void shutdown() {
        client.dispatcher().executorService().shutdown();
        client.connectionPool().evictAll();
        log.info("HTTP client shutdown");
    }
}
