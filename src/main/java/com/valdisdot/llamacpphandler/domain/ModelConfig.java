package com.valdisdot.llamacpphandler.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Configuration for model inference to run.<br>
 * <p>
 * {
 * "model": "gemma3-4b-q4_0-qat",
 * "context": 0,
 * "cpu_threads": 4,
 * "port": 10000,
 * "serve_local": true,
 * "disable_vision": false,
 * "api_key": "an-api-key",
 * "lock_in_ram": true,
 * "k_cache_quant": 16,
 * "v_cache_quant": 16,
 * "offload_kv": true,
 * "layers_upload": -1
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModelConfig {
    /**
     * Model name for -a and -m arguments.
     * Required: must be specified.
     */
    @JsonProperty("model")
    private String model;

    /**
     * Context length for -c argument.
     */
    @JsonProperty("context")
    private Integer context;

    /**
     * Number of CPU threads for -t argument.
     * Required: must be specified and non-zero.
     */
    @JsonProperty("cpu_threads")
    private Integer cpuThreads;

    /**
     * Port number for --port argument.
     * Required: must be specified and non-zero.
     */
    @JsonProperty("port")
    private Integer port;

    /**
     * Whether to serve locally (localhost) or externally.
     * Required: must be specified.
     */
    @JsonProperty("serve_local")
    private Boolean serveLocal;

    /**
     * Whether to ignore mmproj.gguf (disable vision).
     * Optional: if true, disables vision processing.
     */
    @JsonProperty("disable_vision")
    private Boolean disableVision;

    /**
     * API key for --api-key argument.
     * The value will be stripped out potential shell injection characters just in case, allowing only alphanumerics, dashes, and underscores!
     * Required: must be specified.
     */
    @JsonProperty("api_key")
    private String apikey;

    /**
     * Whether to lock model in RAM (--mlock).
     * Optional: if true, prevents model from being swapped to disk.
     */
    @JsonProperty("lock_in_ram")
    private Boolean lockInRam;

    /**
     * KV cache quantization type for --cache-type-k argument.
     * Optional: specifies quantization for key cache.
     */
    @JsonProperty("k_cache_quant")
    private Integer kCacheQuant;

    /**
     * V cache quantization type for --cache-type-v argument.
     * Optional: specifies quantization for value cache.
     */
    @JsonProperty("v_cache_quant")
    private Integer vCacheQuant;

    /**
     * Flag for --no-kv-offload (applied if false)
     */
    @JsonProperty("offload_kv")
    private Boolean offloadKV;

    /**
     * Number of GPU layers to upload for --n-gpu-layers argument.
     * Optional: specifies how many layers to offload to GPU.
     */
    @JsonProperty("layers_upload")
    private Integer layersUpload;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getContext() {
        return context;
    }

    public void setContext(Integer context) {
        this.context = context;
    }

    public Integer getCpuThreads() {
        return cpuThreads;
    }

    public void setCpuThreads(Integer cpuThreads) {
        this.cpuThreads = cpuThreads;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Boolean getServeLocal() {
        return serveLocal;
    }

    public void setServeLocal(Boolean serveLocal) {
        this.serveLocal = serveLocal;
    }

    public Boolean getDisableVision() {
        return disableVision;
    }

    public void setDisableVision(Boolean disableVision) {
        this.disableVision = disableVision;
    }

    public String getApikey() {
        return apikey;
    }

    public void setApikey(String apikey) {
        this.apikey = apikey;
    }

    public Boolean getLockInRam() {
        return lockInRam;
    }

    public void setLockInRam(Boolean lockInRam) {
        this.lockInRam = lockInRam;
    }

    public Integer getKCacheQuant() {
        return kCacheQuant;
    }

    public void setKCacheQuant(Integer kCacheQuant) {
        this.kCacheQuant = kCacheQuant;
    }

    public Integer getVCacheQuant() {
        return vCacheQuant;
    }

    public void setVCacheQuant(Integer vCacheQuant) {
        this.vCacheQuant = vCacheQuant;
    }

    public Boolean getOffloadKV() {
        return offloadKV;
    }

    public void setOffloadKV(Boolean offloadKV) {
        this.offloadKV = offloadKV;
    }

    public Integer getLayersUpload() {
        return layersUpload;
    }

    public void setLayersUpload(Integer layersUpload) {
        this.layersUpload = layersUpload;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ModelConfig that = (ModelConfig) o;
        return Objects.equals(model, that.model) && Objects.equals(context, that.context) && Objects.equals(cpuThreads, that.cpuThreads) && Objects.equals(port, that.port) && Objects.equals(serveLocal, that.serveLocal) && Objects.equals(disableVision, that.disableVision) && Objects.equals(apikey, that.apikey) && Objects.equals(lockInRam, that.lockInRam) && Objects.equals(kCacheQuant, that.kCacheQuant) && Objects.equals(vCacheQuant, that.vCacheQuant) && Objects.equals(offloadKV, that.offloadKV) && Objects.equals(layersUpload, that.layersUpload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(model, context, cpuThreads, port, serveLocal, disableVision, apikey, lockInRam, kCacheQuant, vCacheQuant, offloadKV, layersUpload);
    }
}
