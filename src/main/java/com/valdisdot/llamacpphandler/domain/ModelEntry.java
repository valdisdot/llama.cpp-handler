package com.valdisdot.llamacpphandler.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;

/**
 * Represents a model entry returned from the /run endpoint.
 * Contains the final parameters used to start the model process including the PID.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModelEntry {
    /**
     * The process ID of the running model.
     */
    @JsonProperty("pid")
    private Long pid;

    @JsonProperty("host")
    private String host;

    /**
     * Error message if model run fails.
     */
    @JsonProperty("error_message")
    private String errorMessage;

    /**
     * Logs if model run fails.
     */
    @JsonProperty("logs")
    private Collection<String> logs;

    /**
     * The request data that was used to start the model, filled with default values.
     */
    @JsonProperty("model")
    private ModelConfig model;

    public ModelEntry() {
    }

    public ModelEntry(Long pid, String host, ModelConfig model) {
        this.pid = pid;
        this.host = host;
        this.model = model;
    }

    public ModelEntry(String errorMessage, Collection<String> logs) {
        this.errorMessage = errorMessage;
        this.logs = logs;
    }

    public Long getPid() {
        return pid;
    }

    public void setPid(Long pid) {
        this.pid = pid;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public ModelConfig getModel() {
        return model;
    }

    public void setModel(ModelConfig model) {
        this.model = model;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Collection<String> getLogs() {
        return logs;
    }

    public void setLogs(Collection<String> logs) {
        this.logs = logs;
    }
}
