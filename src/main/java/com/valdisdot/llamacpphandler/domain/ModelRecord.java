package com.valdisdot.llamacpphandler.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO for representing available models from the model's directory.
 * Used for deserializing model information for API responses.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModelRecord {
    /**
     * The name of the model.
     */
    @JsonProperty("model")
    private String model;

    /**
     * Indicates whether the model supports embeddings.
     */
    @JsonProperty("embeddings")
    private boolean embeddings;

    /**
     * Indicates whether the model supports vision capabilities.
     */
    @JsonProperty("vision")
    private boolean vision;

    public boolean getEmbeddings() {
        return embeddings;
    }

    public void setEmbeddings(boolean embeddings) {
        this.embeddings = embeddings;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public boolean getVision() {
        return vision;
    }

    public void setVision(boolean vision) {
        this.vision = vision;
    }
}
