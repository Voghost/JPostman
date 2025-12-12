package net.ooml.jpostman.service.variable;

import net.ooml.jpostman.config.Constants;
import net.ooml.jpostman.model.Environment;
import net.ooml.jpostman.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Variable resolver for {{variable}} syntax
 * Supports environment variables, global variables, and local variables
 */
public class VariableResolver {
    private static final Logger log = LoggerFactory.getLogger(VariableResolver.class);

    private static final Pattern VARIABLE_PATTERN = Pattern.compile(Constants.VARIABLE_PATTERN);
    private static final int MAX_RECURSION_DEPTH = 10; // Prevent infinite recursion

    private Environment globalVariables;
    private Environment environmentVariables;
    private Map<String, String> localVariables;

    public VariableResolver() {
        this.localVariables = new HashMap<>();
    }

    /**
     * Set global variables
     */
    public void setGlobalVariables(Environment globalVariables) {
        this.globalVariables = globalVariables;
    }

    /**
     * Set environment variables
     */
    public void setEnvironmentVariables(Environment environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    /**
     * Set local variable
     */
    public void setLocalVariable(String key, String value) {
        this.localVariables.put(key, value);
    }

    /**
     * Set multiple local variables
     */
    public void setLocalVariables(Map<String, String> variables) {
        this.localVariables.putAll(variables);
    }

    /**
     * Clear local variables
     */
    public void clearLocalVariables() {
        this.localVariables.clear();
    }

    /**
     * Resolve variables in text
     * Example: "{{baseUrl}}/users" -> "https://api.example.com/users"
     */
    public String resolve(String text) {
        if (StringUtil.isEmpty(text)) {
            return text;
        }

        return resolve(text, 0);
    }

    /**
     * Resolve variables recursively with depth limit
     */
    private String resolve(String text, int depth) {
        if (depth >= MAX_RECURSION_DEPTH) {
            log.warn("Maximum recursion depth reached while resolving variables");
            return text;
        }

        Matcher matcher = VARIABLE_PATTERN.matcher(text);
        StringBuffer result = new StringBuffer();
        boolean hasVariables = false;

        while (matcher.find()) {
            hasVariables = true;
            String variableName = matcher.group(1).trim();
            String variableValue = getVariableValue(variableName);

            if (variableValue != null) {
                // Escape special regex characters in the replacement string
                matcher.appendReplacement(result, Matcher.quoteReplacement(variableValue));
                log.debug("Resolved variable: {} = {}", variableName, variableValue);
            } else {
                // Keep the original variable syntax if not found
                matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group(0)));
                log.debug("Variable not found: {}", variableName);
            }
        }
        matcher.appendTail(result);

        String resolvedText = result.toString();

        // If we replaced any variables and the result still contains variables, resolve again
        if (hasVariables && VARIABLE_PATTERN.matcher(resolvedText).find()) {
            return resolve(resolvedText, depth + 1);
        }

        return resolvedText;
    }

    /**
     * Get variable value by name
     * Search order: Local -> Environment -> Global
     */
    private String getVariableValue(String variableName) {
        // 1. Check local variables
        if (localVariables.containsKey(variableName)) {
            return localVariables.get(variableName);
        }

        // 2. Check environment variables
        if (environmentVariables != null) {
            String value = environmentVariables.getVariableValue(variableName);
            if (value != null) {
                return value;
            }
        }

        // 3. Check global variables
        if (globalVariables != null) {
            String value = globalVariables.getVariableValue(variableName);
            if (value != null) {
                return value;
            }
        }

        return null;
    }

    /**
     * Check if text contains variables
     */
    public boolean containsVariables(String text) {
        if (StringUtil.isEmpty(text)) {
            return false;
        }
        return VARIABLE_PATTERN.matcher(text).find();
    }

    /**
     * Extract all variable names from text
     */
    public java.util.List<String> extractVariableNames(String text) {
        java.util.List<String> variableNames = new java.util.ArrayList<>();
        if (StringUtil.isEmpty(text)) {
            return variableNames;
        }

        Matcher matcher = VARIABLE_PATTERN.matcher(text);
        while (matcher.find()) {
            String variableName = matcher.group(1).trim();
            if (!variableNames.contains(variableName)) {
                variableNames.add(variableName);
            }
        }
        return variableNames;
    }

    /**
     * Create a copy of this resolver
     */
    public VariableResolver copy() {
        VariableResolver copy = new VariableResolver();
        copy.globalVariables = this.globalVariables;
        copy.environmentVariables = this.environmentVariables;
        copy.localVariables = new HashMap<>(this.localVariables);
        return copy;
    }
}
