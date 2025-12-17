package net.ooml.jpostman.ui.components.right;

import net.ooml.jpostman.model.Header;
import net.ooml.jpostman.model.Request;
import net.ooml.jpostman.model.RequestBody;
import net.ooml.jpostman.model.Response;
import net.ooml.jpostman.model.enums.HttpMethod;
import net.ooml.jpostman.ui.MainFrame;
import net.ooml.jpostman.ui.i18n.I18nManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Request Editor Panel - Main panel for editing and sending requests
 */
public class RequestEditorPanel extends JPanel {
    private static final Logger log = LoggerFactory.getLogger(RequestEditorPanel.class);

    private final MainFrame mainFrame;

    // Current request being edited
    private Request currentRequest;

    // SwingWorker for async requests (to support cancellation)
    private SwingWorker<Response, Void> currentWorker;

    // Flag to prevent circular updates between URL and Params
    private boolean updatingUrlParams = false;

    // UI Components
    private RequestInfoPanel infoPanel;
    private RequestConfigPanel configPanel;
    private JTabbedPane requestTabbedPane;
    private RequestParamsPanel paramsPanel;
    private RequestHeadersPanel headersPanel;
    private RequestBodyPanel bodyPanel;
    private RequestAuthPanel authPanel;
    private JSplitPane splitPane;
    private ResponsePanel responsePanel;

    public RequestEditorPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initializeUI();
        createNewRequest();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // Top panel with config and info
        JPanel topPanel = new JPanel(new BorderLayout());

        // Request config (Method + URL + Send)
        configPanel = new RequestConfigPanel(mainFrame);
        configPanel.setSendAction(this::sendRequest);
        configPanel.setSaveAction(this::saveCurrentRequest);
        configPanel.setCurlAction(this::showCurlDialog);
        topPanel.add(configPanel, BorderLayout.NORTH);

        // Request info (ID, created, updated)
        infoPanel = new RequestInfoPanel();
        topPanel.add(infoPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // Center: Split pane with request editor and response viewer
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.5);

        // Top of split: Request details (tabs)
        requestTabbedPane = new JTabbedPane();

        paramsPanel = new RequestParamsPanel();
        requestTabbedPane.addTab(I18nManager.get("tab.params"), paramsPanel);

        headersPanel = new RequestHeadersPanel();
        requestTabbedPane.addTab(I18nManager.get("tab.headers"), headersPanel);

        bodyPanel = new RequestBodyPanel();
        bodyPanel.setContentTypeChangeListener(this::updateContentTypeHeader);
        requestTabbedPane.addTab(I18nManager.get("tab.body"), bodyPanel);

        authPanel = new RequestAuthPanel();
        requestTabbedPane.addTab(I18nManager.get("tab.auth"), authPanel);

        splitPane.setTopComponent(requestTabbedPane);

        // Setup URL and Params synchronization
        configPanel.setUrlChangeListener(this::onUrlChanged);
        paramsPanel.setDataChangeListener(this::onParamsChanged);

        // Bottom of split: Response viewer
        responsePanel = new ResponsePanel();
        splitPane.setBottomComponent(responsePanel);

        add(splitPane, BorderLayout.CENTER);
    }

    /**
     * Create new request
     */
    public void createNewRequest() {
        currentRequest = Request.createNew("New Request", HttpMethod.GET);
        currentRequest.setUrl("https://httpbin.org/get");
        loadRequest(currentRequest);
        responsePanel.clear();
        log.info("New request created");
    }

    /**
     * Load request into editor
     */
    public void loadRequest(Request request) {
        this.currentRequest = request;

        if (request == null) {
            clear();
            return;
        }

        // Load request info
        infoPanel.setRequest(request);

        // Load into panels
        configPanel.setMethod(request.getMethod());
        configPanel.setUrl(request.getUrl());
        paramsPanel.setParams(request.getQueryParams());
        headersPanel.setHeaders(request.getHeaders());

        if (request.getBody() != null) {
            bodyPanel.setBodyType(request.getBody().getType());
            bodyPanel.setBodyContent(request.getBody().getContent());
        } else {
            bodyPanel.clear();
        }

        authPanel.setAuthConfig(request.getAuth());

        log.debug("Request loaded: {}", request.getName());
    }

    /**
     * Save current request to model
     */
    public void saveCurrentRequest() {
        if (currentRequest == null) {
            return;
        }

        try {
            // Update request from UI
            currentRequest.setMethod(configPanel.getMethod());
            currentRequest.setUrl(configPanel.getUrl());
            currentRequest.setQueryParams(paramsPanel.getParams());
            currentRequest.setHeaders(headersPanel.getHeaders());

            // Update body
            RequestBody body = RequestBody.builder()
                    .type(bodyPanel.getBodyType())
                    .content(bodyPanel.getBodyContent())
                    .build();
            currentRequest.setBody(body);

            // Update auth
            currentRequest.setAuth(authPanel.getAuthConfig());

            // Update timestamp
            currentRequest.touch();

            // Update info panel to reflect new timestamp
            infoPanel.setRequest(currentRequest);

            // Persist to file if request belongs to a collection
            if (currentRequest.getCollectionId() != null) {
                String projectName = mainFrame.getCurrentProject();
                String collectionId = currentRequest.getCollectionId();

                // Load collection, update it, and save
                net.ooml.jpostman.model.Collection collection =
                    mainFrame.getStorageService().loadCollection(projectName, collectionId);

                // Find and update the request in the collection
                boolean found = false;
                for (int i = 0; i < collection.getRequests().size(); i++) {
                    if (collection.getRequests().get(i).getId().equals(currentRequest.getId())) {
                        collection.getRequests().set(i, currentRequest);
                        found = true;
                        break;
                    }
                }

                if (found) {
                    collection.touch();
                    mainFrame.getStorageService().saveCollection(projectName, collection);
                    log.info("Request saved and persisted: {}", currentRequest.getName());
                } else {
                    log.warn("Request not found in collection: {}", currentRequest.getId());
                }
            }

            mainFrame.setStatus(I18nManager.get("status.saved"));

        } catch (Exception e) {
            log.error("Failed to save request", e);
            mainFrame.showError("Failed to save request: " + e.getMessage());
        }
    }

    /**
     * Show cURL command dialog
     */
    public void showCurlDialog() {
        try {
            // First update currentRequest from UI
            saveCurrentRequest();

            if (currentRequest == null) {
                mainFrame.showError("No request available");
                return;
            }

            // Create and show the dialog
            net.ooml.jpostman.ui.dialogs.CurlDialog dialog =
                new net.ooml.jpostman.ui.dialogs.CurlDialog(mainFrame, currentRequest);
            dialog.setVisible(true);

        } catch (Exception e) {
            log.error("Failed to show cURL dialog", e);
            mainFrame.showError("Failed to generate cURL command: " + e.getMessage());
        }
    }

    /**
     * Send HTTP request
     */
    public void sendRequest() {
        try {
            // Save current state first
            saveCurrentRequest();

            if (currentRequest == null) {
                mainFrame.showError("No request to send");
                return;
            }

            // Validate URL
            String url = currentRequest.getUrl();
            if (url == null || url.trim().isEmpty()) {
                mainFrame.showError("URL cannot be empty");
                return;
            }

            mainFrame.setStatus(I18nManager.get("status.sending"));
            log.info("Sending request: {} {}", currentRequest.getMethod(), currentRequest.getUrl());

            // Execute request in background thread
            SwingWorker<Response, Void> worker = new SwingWorker<>() {
                @Override
                protected Response doInBackground() {
                    return mainFrame.getHttpClientService().execute(currentRequest);
                }

                @Override
                protected void done() {
                    try {
                        Response response = get();
                        responsePanel.displayResponse(response, currentRequest);

                        String statusMsg = String.format("%s - %d %s (%dms)",
                                I18nManager.get("status.request_complete"),
                                response.getStatusCode(),
                                response.getStatusText(),
                                response.getDuration());
                        mainFrame.setStatus(statusMsg);

                        log.info("Request completed: {} {}", response.getStatusCode(), currentRequest.getUrl());

                    } catch (Exception e) {
                        log.error("Request failed", e);
                        mainFrame.showError("Request failed: " + e.getMessage());
                        mainFrame.setStatus(I18nManager.get("status.error"));
                    }
                }
            };

            worker.execute();

        } catch (Exception e) {
            log.error("Failed to send request", e);
            mainFrame.showError("Failed to send request: " + e.getMessage());
            mainFrame.setStatus(I18nManager.get("status.error"));
        }
    }

    /**
     * Duplicate current request
     */
    public void duplicateCurrentRequest() {
        if (currentRequest == null) {
            return;
        }

        saveCurrentRequest();

        // Create copy
        Request copy = Request.createNew(currentRequest.getName() + " (Copy)", currentRequest.getMethod());
        copy.setUrl(currentRequest.getUrl());
        copy.setHeaders(currentRequest.getHeaders());
        copy.setBody(currentRequest.getBody());
        copy.setAuth(currentRequest.getAuth());

        this.currentRequest = copy;
        loadRequest(copy);

        log.info("Request duplicated");
        mainFrame.showInfo("Request duplicated");
    }

    /**
     * Clear editor
     */
    public void clear() {
        infoPanel.clear();
        configPanel.clear();
        headersPanel.clear();
        bodyPanel.clear();
        authPanel.clear();
        responsePanel.clear();
    }

    /**
     * Get current request
     */
    public Request getCurrentRequest() {
        return currentRequest;
    }

    /**
     * Update theme for syntax highlighting
     */
    public void updateTheme() {
        if (bodyPanel != null) {
            bodyPanel.updateTheme();
        }
        if (responsePanel != null) {
            responsePanel.updateTheme();
        }
    }

    /**
     * Update Content-Type header when body type changes
     */
    private void updateContentTypeHeader(String contentType) {
        if (headersPanel == null || contentType == null) {
            return;
        }

        // Get current headers
        List<Header> headers = headersPanel.getHeaders();

        // Check if Content-Type already exists
        boolean found = false;
        for (Header header : headers) {
            if ("Content-Type".equalsIgnoreCase(header.getKey())) {
                // Update existing Content-Type
                header.setValue(contentType);
                header.setEnabled(true);
                found = true;
                break;
            }
        }

        // If not found, add new Content-Type header
        if (!found) {
            Header contentTypeHeader = Header.builder()
                    .key("Content-Type")
                    .value(contentType)
                    .enabled(true)
                    .build();
            headers.add(contentTypeHeader);
        }

        // Update headers panel
        headersPanel.setHeaders(headers);
    }

    /**
     * Refresh UI after language change
     */
    public void refreshUI() {
        // Update tab labels
        if (requestTabbedPane != null) {
            requestTabbedPane.setTitleAt(0, I18nManager.get("tab.params"));
            requestTabbedPane.setTitleAt(1, I18nManager.get("tab.headers"));
            requestTabbedPane.setTitleAt(2, I18nManager.get("tab.body"));
            requestTabbedPane.setTitleAt(3, I18nManager.get("tab.auth"));
        }

        // Refresh sub-panels
        if (configPanel != null) {
            configPanel.refreshUI();
        }
        if (paramsPanel != null) {
            paramsPanel.refreshUI();
        }
        if (headersPanel != null) {
            headersPanel.refreshUI();
        }
        if (bodyPanel != null) {
            bodyPanel.refreshUI();
        }
        if (authPanel != null) {
            authPanel.refreshUI();
        }
        if (responsePanel != null) {
            responsePanel.refreshUI();
        }
        if (infoPanel != null) {
            infoPanel.refreshUI();
        }

        revalidate();
        repaint();
    }

    /**
     * Handle URL changes - parse query params and update params panel
     */
    private void onUrlChanged() {
        if (updatingUrlParams) {
            return;
        }

        try {
            updatingUrlParams = true;
            String url = configPanel.getUrl();
            if (url == null || url.trim().isEmpty()) {
                return;
            }

            // Parse URL and extract query parameters
            int queryStart = url.indexOf('?');
            if (queryStart >= 0 && queryStart < url.length() - 1) {
                String queryString = url.substring(queryStart + 1);
                List<Header> params = new java.util.ArrayList<>();

                // Split by &
                String[] pairs = queryString.split("&");
                for (String pair : pairs) {
                    if (pair.trim().isEmpty()) {
                        continue;
                    }

                    // Split by =
                    int equalsIndex = pair.indexOf('=');
                    String key, value;
                    if (equalsIndex >= 0) {
                        key = pair.substring(0, equalsIndex);
                        value = equalsIndex < pair.length() - 1 ? pair.substring(equalsIndex + 1) : "";
                    } else {
                        key = pair;
                        value = "";
                    }

                    // URL decode
                    try {
                        key = java.net.URLDecoder.decode(key, "UTF-8");
                        value = java.net.URLDecoder.decode(value, "UTF-8");
                    } catch (Exception e) {
                        // If decode fails, use original
                    }

                    params.add(Header.builder()
                            .key(key)
                            .value(value)
                            .enabled(true)
                            .build());
                }

                paramsPanel.setParams(params);
            } else {
                // No query params in URL, clear params panel
                paramsPanel.setParams(new java.util.ArrayList<>());
            }
        } finally {
            updatingUrlParams = false;
        }
    }

    /**
     * Handle params changes - build query string and update URL
     */
    private void onParamsChanged() {
        if (updatingUrlParams) {
            return;
        }

        try {
            updatingUrlParams = true;
            String url = configPanel.getUrl();
            if (url == null) {
                return;
            }

            // Remove existing query string
            int queryStart = url.indexOf('?');
            String baseUrl = queryStart >= 0 ? url.substring(0, queryStart) : url;

            // Build new query string from params
            List<Header> params = paramsPanel.getParams();
            StringBuilder queryString = new StringBuilder();

            for (Header param : params) {
                if (!Boolean.TRUE.equals(param.getEnabled()) || param.getKey() == null || param.getKey().trim().isEmpty()) {
                    continue;
                }

                if (queryString.length() > 0) {
                    queryString.append("&");
                }

                try {
                    String key = java.net.URLEncoder.encode(param.getKey().trim(), "UTF-8");
                    String value = param.getValue() != null ? java.net.URLEncoder.encode(param.getValue(), "UTF-8") : "";
                    queryString.append(key).append("=").append(value);
                } catch (Exception e) {
                    // If encode fails, use original
                    queryString.append(param.getKey().trim()).append("=").append(param.getValue() != null ? param.getValue() : "");
                }
            }

            // Update URL
            String newUrl = baseUrl;
            if (queryString.length() > 0) {
                newUrl = baseUrl + "?" + queryString;
            }

            configPanel.setUrl(newUrl);
        } finally {
            updatingUrlParams = false;
        }
    }
}
