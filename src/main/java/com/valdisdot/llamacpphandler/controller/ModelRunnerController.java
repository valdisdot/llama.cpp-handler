package com.valdisdot.llamacpphandler.controller;

import com.valdisdot.llamacpphandler.domain.ModelConfig;
import com.valdisdot.llamacpphandler.service.ModelRunnerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing Llama.cpp model inference operations.
 * <p>
 * This controller provides HTTP endpoints for:
 * <ul>
 *   <li>Checking service availability</li>
 *   <li>Listing available models</li>
 *   <li>Starting and stopping model inference processes</li>
 *   <li>Retrieving model metadata and status</li>
 * </ul>
 *
 * <p>
 * All endpoints are prefixed with {@code /api/v1}.
 * </p>
 *
 * <h2>API Endpoints</h2>
 * <table border="1">
 *   <tr>
 *     <th>Method</th>
 *     <th>Path</th>
 *     <th>Status Codes</th>
 *     <th>Description</th>
 *   </tr>
 *   <tr>
 *     <td>GET</td>
 *     <td>{@code /api/v1}</td>
 *     <td>200, 403</td>
 *     <td>Checks service availability</td>
 *   </tr>
 *   <tr>
 *     <td>GET</td>
 *     <td>{@code /api/v1/models}</td>
 *     <td>200</td>
 *     <td>Lists all available models</td>
 *   </tr>
 *   <tr>
 *     <td>GET</td>
 *     <td>{@code /api/v1/model/{pid}}</td>
 *     <td>404, 200</td>
 *     <td>Returns model metadata for a specific process ID</td>
 *   </tr>
 *   <tr>
 *     <td>GET</td>
 *     <td>{@code /api/v1/model/all}</td>
 *     <td>200</td>
 *     <td>Returns metadata for all running models</td>
 *   </tr>
 *   <tr>
 *     <td>POST</td>
 *     <td>{@code /api/v1/model}</td>
 *     <td>200</td>
 *     <td>Runs a model with provided configuration</td>
 *   </tr>
 *   <tr>
 *     <td>DELETE</td>
 *     <td>{@code /api/v1/model/{pid}}</td>
 *     <td>404, 200</td>
 *     <td>Stops a model with the given process ID</td>
 *   </tr>
 *   <tr>
 *     <td>DELETE</td>
 *     <td>{@code /api/v1/model/all}</td>
 *     <td>200</td>
 *     <td>Stops all running models</td>
 *   </tr>
 * </table>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * // Check service availability
 * GET /api/v1
 *
 * // List available models
 * GET /api/v1/models
 *
 * // Start a model
 * POST /api/v1/model
 * {
 *   "model": "gemma3-4b-q4_0-qat",
 *   "context": 0,
 *   "cpu_threads": 4,
 *   "port": 10000,
 *   "serve_local": true,
 *   "disable_vision": false,
 *   "api_key": "an-api-key",
 *   "lock_in_ram": true,
 *   "k_cache_quant": 16,
 *   "v_cache_quant": 16,
 *   "offload_kv": true,
 *   "layers_upload": -1
 * }
 *
 * // Get running model metadata
 * GET /api/v1/model/12345
 *
 * // Stop a specific model
 * DELETE /api/v1/model/12345
 *
 * // Stop all models
 * DELETE /api/v1/model/all
 * }</pre>
 */
@RestController
@RequestMapping("/api/v1")
public class ModelRunnerController {

    /**
     * Service for managing and running Llama.cpp model inference processes.
     */
    private final ModelRunnerService modelRunnerService;

    /**
     * Constructs a new ModelRunnerController instance.
     *
     * @param modelRunnerService The service to delegate model operations to
     */
    public ModelRunnerController(ModelRunnerService modelRunnerService) {
        this.modelRunnerService = modelRunnerService;
    }

    /**
     * Checks if the service is available and ready to accept requests.
     * <p>
     * This endpoint returns a 200 OK response if the service is running,
     * or a 403 Forbidden response if the service is not available.
     * </p>
     *
     * @return {@code 200 OK} if service is available, {@code 403 Forbidden} otherwise
     */
    @GetMapping
    ResponseEntity<?> checkAvailability() {
        return ResponseEntity.ok().build();
    }

    /**
     * Lists all available models in the models directory.
     * <p>
     * Returns a list of {@link com.valdisdot.llamacpphandler.domain.ModelRecord} objects,
     * each containing information about a model including:
     * <ul>
     *     <li>Model name (directory name)</li>
     *     <li>Whether vision processing is supported (mmproj.gguf exists)</li>
     *     <li>Whether embeddings are available</li>
     * </ul>
     * </p>
     *
     * @return {@code 200 OK} with a list of available models
     */
    @GetMapping("/models")
    ResponseEntity<?> listAvailableModels() {
        return ResponseEntity.ok(modelRunnerService.listAvailableModels());
    }

    /**
     * Checks if a model is currently running by its process ID.
     * <p>
     * This endpoint returns a 200 OK response if the model with the given
     * process ID is running, or a 404 Not Found response if it is not.
     * </p>
     *
     * @param pid The process ID to check
     * @return {@code 200 OK} if model is running, {@code 404 Not Found} otherwise
     */
    @RequestMapping(method = RequestMethod.OPTIONS, path = "/model/{pid}")
    ResponseEntity<?> isRunning(@PathVariable Long pid) {
        return modelRunnerService.isRunning(pid) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    /**
     * Retrieves metadata for a running model by its process ID.
     * <p>
     * Returns a {@link com.valdisdot.llamacpphandler.domain.ModelEntry} object containing:
     * <ul>
     *     <li>Process ID</li>
     *     <li>Host address</li>
     *     <li>Model configuration</li>
     * </ul>
     * </p>
     * <p>
     * Returns a 404 Not Found response if no model is running with the given process ID.
     * </p>
     *
     * @param pid The process ID of the running model
     * @return {@code 200 OK} with model metadata, {@code 404 Not Found} if not running
     */
    @GetMapping("/model/{pid}")
    ResponseEntity<?> getRunningModel(@PathVariable Long pid) {
        return modelRunnerService.getRunningModel(pid).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves metadata for all currently running models.
     * <p>
     * Returns a list of {@link com.valdisdot.llamacpphandler.domain.ModelEntry} objects,
     * each containing information about a running model including:
     * <ul>
     *     <li>Process ID</li>
     *     <li>Host address</li>
     *     <li>Model configuration</li>
     * </ul>
     * </p>
     *
     * @return {@code 200 OK} with a list of all running models
     */
    @GetMapping("/model/all")
    ResponseEntity<?> getAllRunningModels() {
        return ResponseEntity.ok(modelRunnerService.getAllRunningModels());
    }

    /**
     * Starts a new model inference process with the provided configuration.
     * <p>
     * Accepts a {@link com.valdisdot.llamacpphandler.domain.ModelConfig} object in the request body
     * and returns a {@link com.valdisdot.llamacpphandler.domain.ModelEntry} containing:
     * <ul>
     *     <li>Process ID</li>
     *     <li>Host address</li>
     *     <li>Model configuration</li>
     * </ul>
     * </p>
     * <p>
     * Returns a 200 OK response with the model entry upon successful startup.
     * </p>
     *
     * @param config The model configuration to use for starting the process
     * @return {@code 200 OK} with the model entry containing process ID and configuration
     */
    @PostMapping("/model")
    ResponseEntity<?> runModel(@RequestBody ModelConfig config) {
        return ResponseEntity.ok(modelRunnerService.runModel(config));
    }

    /**
     * Stops a running model by its process ID.
     * <p>
     * Terminates the process and all its child processes, then removes the model
     * from the running registry. Returns a 200 OK response if the model was stopped,
     * or a 404 Not Found response if no model was running with the given process ID.
     * </p>
     *
     * @param pid The process ID of the model to stop
     * @return {@code 200 OK} if model was stopped, {@code 404 Not Found} otherwise
     */
    @DeleteMapping("/model/{pid}")
    ResponseEntity<?> stopModel(@PathVariable Long pid) {
        if (modelRunnerService.isRunning(pid)) {
            modelRunnerService.stopModel(pid);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Stops all currently running models.
     * <p>
     * Iterates through all registered models and terminates each one.
     * Useful for cleanup during service shutdown or when all models need to be stopped.
     * </p>
     *
     * @return {@code 200 OK} upon successful completion
     */
    @DeleteMapping("/model/all")
    ResponseEntity<?> stopAllModels() {
        modelRunnerService.stopAllModels();
        return ResponseEntity.ok().build();
    }
}