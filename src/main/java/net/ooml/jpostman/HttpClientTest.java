package net.ooml.jpostman;

import net.ooml.jpostman.config.PathConfig;
import net.ooml.jpostman.model.*;
import net.ooml.jpostman.model.enums.AuthType;
import net.ooml.jpostman.model.enums.BodyType;
import net.ooml.jpostman.model.enums.HttpMethod;
import net.ooml.jpostman.service.http.HttpClientService;
import net.ooml.jpostman.service.variable.EnvironmentService;
import net.ooml.jpostman.service.variable.VariableResolver;
import net.ooml.jpostman.service.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * HTTP Client Test - Test HTTP request functionality
 */
public class HttpClientTest {
    private static final Logger log = LoggerFactory.getLogger(HttpClientTest.class);

    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("  JPostman HTTP Client Test");
        System.out.println("=".repeat(80));
        System.out.println();

        try {
            // Initialize
            PathConfig.initializeDirectories();
            String projectName = PathConfig.getDefaultProjectName();
            StorageService storage = StorageService.getInstance();

            // Ensure project exists
            if (!storage.projectExists(projectName)) {
                storage.createProject(projectName);
            }

            // Create environment with variables
            System.out.println("Setting up environment with variables...");
            Environment testEnv = Environment.createNew("Test");
            testEnv.setVariable("baseUrl", "https://httpbin.org");
            testEnv.setVariable("apiKey", "test-key-123");
            storage.saveEnvironment(projectName, testEnv);

            // Create EnvironmentService and set current environment
            EnvironmentService envService = new EnvironmentService(storage);
            envService.initialize(projectName);
            envService.setCurrentEnvironment("Test");

            // Create variable resolver
            VariableResolver variableResolver = envService.createVariableResolver();

            // Create HTTP client
            HttpClientService httpClient = new HttpClientService(variableResolver, 10000);
            System.out.println("✓ HTTP Client initialized\n");

            // Test 1: Simple GET request
            System.out.println("Test 1: Simple GET request");
            System.out.println("-".repeat(80));
            testSimpleGet(httpClient);
            System.out.println();

            // Test 2: GET with query parameters
            System.out.println("Test 2: GET with query parameters");
            System.out.println("-".repeat(80));
            testGetWithParams(httpClient);
            System.out.println();

            // Test 3: POST with JSON body
            System.out.println("Test 3: POST with JSON body");
            System.out.println("-".repeat(80));
            testPostJson(httpClient);
            System.out.println();

            // Test 4: Request with headers
            System.out.println("Test 4: Request with custom headers");
            System.out.println("-".repeat(80));
            testWithHeaders(httpClient);
            System.out.println();

            // Test 5: Basic Authentication
            System.out.println("Test 5: Basic Authentication");
            System.out.println("-".repeat(80));
            testBasicAuth(httpClient);
            System.out.println();

            // Test 6: Bearer Token Authentication
            System.out.println("Test 6: Bearer Token Authentication");
            System.out.println("-".repeat(80));
            testBearerToken(httpClient);
            System.out.println();

            // Test 7: Variable resolution in URL
            System.out.println("Test 7: Variable resolution in URL");
            System.out.println("-".repeat(80));
            testVariableResolution(httpClient);
            System.out.println();

            // Shutdown
            httpClient.shutdown();

            // Summary
            System.out.println("=".repeat(80));
            System.out.println("✓ All HTTP tests completed!");
            System.out.println("=".repeat(80));

        } catch (Exception e) {
            log.error("Test failed", e);
            System.err.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void testSimpleGet(HttpClientService httpClient) {
        Request request = Request.createNew("Get IP", HttpMethod.GET);
        request.setUrl("https://httpbin.org/ip");

        Response response = httpClient.execute(request);
        printResponse(response);
    }

    private static void testGetWithParams(HttpClientService httpClient) {
        Request request = Request.createNew("Get with params", HttpMethod.GET);
        request.setUrl("https://httpbin.org/get?foo=bar&test=123");

        Response response = httpClient.execute(request);
        printResponse(response);
    }

    private static void testPostJson(HttpClientService httpClient) {
        Request request = Request.createNew("Post JSON", HttpMethod.POST);
        request.setUrl("https://httpbin.org/post");

        String jsonBody = "{\n" +
                "  \"name\": \"John Doe\",\n" +
                "  \"email\": \"john@example.com\",\n" +
                "  \"age\": 30\n" +
                "}";
        request.setBody(RequestBody.createJson(jsonBody));

        Response response = httpClient.execute(request);
        printResponse(response);
    }

    private static void testWithHeaders(HttpClientService httpClient) {
        Request request = Request.createNew("Get with headers", HttpMethod.GET);
        request.setUrl("https://httpbin.org/headers");

        List<Header> headers = new ArrayList<>();
        headers.add(Header.builder()
                .key("X-Custom-Header")
                .value("CustomValue123")
                .enabled(true)
                .build());
        headers.add(Header.builder()
                .key("X-Test-Header")
                .value("TestValue456")
                .enabled(true)
                .build());
        request.setHeaders(headers);

        Response response = httpClient.execute(request);
        printResponse(response);
    }

    private static void testBasicAuth(HttpClientService httpClient) {
        Request request = Request.createNew("Basic Auth", HttpMethod.GET);
        request.setUrl("https://httpbin.org/basic-auth/user/passwd");

        AuthConfig auth = AuthConfig.builder()
                .type(AuthType.BASIC)
                .username("user")
                .password("passwd")
                .build();
        request.setAuth(auth);

        Response response = httpClient.execute(request);
        printResponse(response);
    }

    private static void testBearerToken(HttpClientService httpClient) {
        Request request = Request.createNew("Bearer Token", HttpMethod.GET);
        request.setUrl("https://httpbin.org/bearer");

        AuthConfig auth = AuthConfig.builder()
                .type(AuthType.BEARER)
                .token("test-token-12345")
                .build();
        request.setAuth(auth);

        Response response = httpClient.execute(request);
        printResponse(response);
    }

    private static void testVariableResolution(HttpClientService httpClient) {
        Request request = Request.createNew("Variable Resolution", HttpMethod.GET);
        request.setUrl("{{baseUrl}}/get");

        List<Header> headers = new ArrayList<>();
        headers.add(Header.builder()
                .key("X-API-Key")
                .value("{{apiKey}}")
                .enabled(true)
                .build());
        request.setHeaders(headers);

        System.out.println("Request URL (with variables): {{baseUrl}}/get");
        System.out.println("Request Header: X-API-Key = {{apiKey}}");
        System.out.println();

        Response response = httpClient.execute(request);
        printResponse(response);
    }

    private static void printResponse(Response response) {
        System.out.println("Status: " + response.getStatusCode() + " " + response.getStatusText());
        System.out.println("Duration: " + response.getDuration() + "ms");
        System.out.println("Size: " + response.getSize() + " bytes");
        System.out.println("Successful: " + response.isSuccessful());

        if (response.getHeaders() != null && !response.getHeaders().isEmpty()) {
            System.out.println("\nResponse Headers (" + response.getHeaders().size() + "):");
            response.getHeaders().stream()
                    .limit(5)
                    .forEach(h -> System.out.println("  " + h.getKey() + ": " + h.getValue()));
            if (response.getHeaders().size() > 5) {
                System.out.println("  ... and " + (response.getHeaders().size() - 5) + " more");
            }
        }

        if (response.getBody() != null) {
            String body = response.getBody();
            System.out.println("\nResponse Body:");
            if (body.length() > 500) {
                System.out.println(body.substring(0, 500) + "...");
                System.out.println("(truncated, total length: " + body.length() + ")");
            } else {
                System.out.println(body);
            }
        }

        System.out.println();
    }
}
