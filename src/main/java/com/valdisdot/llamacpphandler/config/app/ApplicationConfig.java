package com.valdisdot.llamacpphandler.config.app;

import com.valdisdot.llamacpphandler.domain.ModelConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * Application configuration for Llama.cpp Handler.
 * <p>
 * This configuration class provides essential beans and validation checks
 * required for the application to function correctly. It includes:
 * <ul>
 *   <li>Default model configuration bean</li>
 *   <li>Binary executable validation</li>
 *   <li>Models directory validation</li>
 *   <li>Default model file validation</li>
 * </ul>
 *
 * <p>
 * All validation beans are registered as singleton beans and are checked
 * during application startup. If any validation fails, the application
 * will throw an exception and fail to start.
 * </p>
 *
 * <h2>Configuration Properties</h2>
 * <table border="1">
 *   <tr>
 *     <th>Property Prefix</th>
 *     <th>Description</th>
 *     <th>Example</th>
 *   </tr>
 *   <tr>
 *     <td>{@code application.defaults.*}</td>
 *     <td>Default values for model configuration</td>
 *     <td>{@code application.defaults.model=gemma3-4b-q4_0-qat}</td>
 *   </tr>
 *   <tr>
 *     <td>{@code application.llamacpp.bin}</td>
 *     <td>Path to llama.cpp binary executable</td>
 *     <td>{@code /usr/local/bin/llama-cpp-server}</td>
 *   </tr>
 *   <tr>
 *     <td>{@code application.models}</td>
 *     <td>Path to models directory</td>
 *     <td>{@code /models}</td>
 *   </tr>
 * </table>
 *
 * <h2>Validation Flow</h2>
 * <ol>
 *   <li>Application starts</li>
 *   <li>{@link #checkLlamaCppBin} validates binary exists and is executable</li>
 *   <li>{@link #checkModelsDir} validates models directory exists</li>
 *   <li>{@link #checkDefaultModel} validates default model file exists</li>
 *   <li>Application continues if all validations pass</li>
 * </ol>
 *
 * <h2>Example Configuration</h2>
 * <pre>{@code
 * # application.properties
 * application.llamacpp.bin=/usr/local/bin/llama-cpp-server
 * application.models=/models
 * application.defaults.model=gemma3-4b-q4_0-qat
 * application.defaults.context=0
 * application.defaults.cpu_threads=4
 * application.defaults.port=10000
 * application.defaults.serve_local=true
 * application.defaults.disable_vision=false
 * application.defaults.api_key=an-api-key
 * application.defaults.lock_in_ram=true
 * application.defaults.k_cache_quant=16
 * application.defaults.v_cache_quant=16
 * application.defaults.offload_kv=true
 * application.defaults.layers_upload=-1
 * }</pre>
 */
@Configuration
public class ApplicationConfig {

    /**
     * Creates a default model configuration bean from application properties.
     * <p>
     * This bean constructs a {@link ModelConfig} object with default values
     * specified in the {@code application.defaults.*} properties. The default
     * configuration is used by {@link com.valdisdot.llamacpphandler.service.ModelRunnerService}
     * to fill in missing or invalid values when starting model inference processes.
     * </p>
     *
     * <h3>Configuration Properties</h3>
     * <table border="1">
     *     <tr>
     *       <th>Property</th>
     *       <th>Description</th>
     *       <th>Default Example</th>
     *     </tr>
     *     <tr>
     *       <td>{@code application.defaults.model}</td>
     *       <td>Default model name</td>
     *       <td>{@code gemma3-4b-q4_0-qat}</td>
     *     </tr>
     *     <tr>
     *       <td>{@code application.defaults.context}</td>
     *       <td>Default context length</td>
     *       <td>{@code 0}</td>
     *     </tr>
     *     <tr>
     *       <td>{@code application.defaults.cpu_threads}</td>
     *       <td>Default CPU thread count</td>
     *       <td>{@code 4}</td>
     *     </tr>
     *     <tr>
     *       <td>{@code application.defaults.port}</td>
     *       <td>Default port number</td>
     *       <td>{@code 10000}</td>
     *     </tr>
     *     <tr>
     *       <td>{@code application.defaults.serve_local}</td>
     *       <td>Default serve locally flag</td>
     *       <td>{@code true}</td>
     *     </tr>
     *     <tr>
     *       <td>{@code application.defaults.disable_vision}</td>
     *       <td>Default disable vision flag</td>
     *       <td>{@code false}</td>
     *     </tr>
     *     <tr>
     *       <td>{@code application.defaults.api_key}</td>
     *       <td>Default API key</td>
     *       <td>{@code an-api-key}</td>
     *     </tr>
     *     <tr>
     *       <td>{@code application.defaults.lock_in_ram}</td>
     *       <td>Default lock in RAM flag</td>
     *       <td>{@code true}</td>
     *     </tr>
     *     <tr>
     *       <td>{@code application.defaults.k_cache_quant}</td>
     *       <td>Default KV cache quantization</td>
     *       <td>{@code 16}</td>
     *     </tr>
     *     <tr>
     *       <td>{@code application.defaults.v_cache_quant}</td>
     *       <td>Default V cache quantization</td>
     *       <td>{@code 16}</td>
     *     </tr>
     *     <tr>
     *       <td>{@code application.defaults.offload_kv}</td>
     *       <td>Default KV offload flag</td>
     *       <td>{@code true}</td>
     *     </tr>
     *     <tr>
     *       <td>{@code application.defaults.layers_upload}</td>
     *       <td>Default GPU layers to upload</td>
     *       <td>{@code -1}</td>
     *     </tr>
     * </table>
     * </p>
     *
     * @param model              Default model name
     * @param context            Default context length
     * @param cpuThreads         Default CPU thread count
     * @param port               Default port number
     * @param serveLocal         Default serve locally flag
     * @param disableVision      Default disable vision flag
     * @param apikey             Default API key
     * @param lockInRam          Default lock in RAM flag
     * @param kCacheQuant        Default KV cache quantization
     * @param vCacheQuant        Default V cache quantization
     * @param offloadKV          Default KV offload flag
     * @param layersUpload       Default GPU layers to upload
     * @return Configured {@link ModelConfig} bean with default values
     */
    @Bean
    ModelConfig defaultModelConfig(
            @Value("${application.defaults.model}") String model,
            @Value("${application.defaults.context}") Integer context,
            @Value("${application.defaults.cpu_threads}") Integer cpuThreads,
            @Value("${application.defaults.port}") Integer port,
            @Value("${application.defaults.serve_local}") Boolean serveLocal,
            @Value("${application.defaults.disable_vision}") Boolean disableVision,
            @Value("${application.defaults.api_key}") String apikey,
            @Value("${application.defaults.lock_in_ram}") Boolean lockInRam,
            @Value("${application.defaults.k_cache_quant}") Integer kCacheQuant,
            @Value("${application.defaults.v_cache_quant}") Integer vCacheQuant,
            @Value("${application.defaults.offload_kv}") Boolean offloadKV,
            @Value("${application.defaults.layers_upload}") Integer layersUpload
    ) {
        ModelConfig config = new ModelConfig();
        config.setModel(model);
        config.setContext(context);
        config.setCpuThreads(cpuThreads);
        config.setPort(port);
        config.setServeLocal(serveLocal);
        config.setDisableVision(disableVision);
        config.setApikey(apikey);
        config.setLockInRam(lockInRam);
        config.setKCacheQuant(kCacheQuant);
        config.setVCacheQuant(vCacheQuant);
        config.setOffloadKV(offloadKV);
        config.setLayersUpload(layersUpload);
        return config;
    }

    /**
     * Validates that the llama.cpp binary executable exists and is functional.
     * <p>
     * This bean performs the following validation checks:
     * <ol>
     *     <li>Checks if the binary file exists at the specified path</li>
     *     <li>Verifies the binary has execute permissions</li>
     *     <li>Executes the binary with {@code --help} flag</li>
     *     <li>Validates that the help output contains expected arguments
     *         ({@code --mlock} and {@code --numa})</li>
     * </ol>
     * </p>
     * <p>
     * If any validation fails, an {@link Exception} is thrown and the application
     * will fail to start. This ensures that the llama.cpp binary is properly
     * installed and configured before the application attempts to use it.
     * </p>
     *
     * @param binPath The path to the llama.cpp binary executable
     * @return {@code true} if validation passes (bean is registered as singleton)
     * @throws Exception if binary is not found, not executable, or missing expected arguments
     */
    @Bean("is_llama_cpp_bin_checked")
    Boolean checkLlamaCppBin(@Value("${application.llamacpp.bin}") String binPath) throws Exception {
        File binFile = new File(binPath);
        if (!binFile.exists() || !binFile.canExecute()) {
            throw new Exception("Executable not found or not executable at '" + binPath + "'");
        }

        ProcessBuilder pb = new ProcessBuilder(binPath, "--help");
        Process p = pb.start();
        String helpOutput = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (!helpOutput.contains("--mlock") || !helpOutput.contains("--numa")) {
            throw new Exception("Binary --help output missing typical llama.cpp args (--mlock, --numa).");
        }
        return true;
    }

    /**
     * Validates that the models directory exists and is accessible.
     * <p>
     * This bean performs the following validation checks:
     * <ol>
     *     <li>Checks if the directory exists at the specified path</li>
     *     <li>Verifies the path is actually a directory</li>
     * </ol>
     * </p>
     * <p>
     * If validation fails, an {@link Exception} is thrown and the application
     * will fail to start. This ensures that the models directory is properly
     * configured before the application attempts to scan for models.
     * </p>
     *
     * @param modelsDir The path to the models directory
     * @return {@code true} if validation passes (bean is registered as singleton)
     * @throws Exception if directory is not found or not a directory
     */
    @Bean("is_models_dir_checked")
    Boolean checkModelsDir(@Value("${application.models}") String modelsDir) throws Exception {
        File modelsDirFile = new File(modelsDir);
        if (!modelsDirFile.exists() || !modelsDirFile.isDirectory()) {
            throw new Exception("Models directory not found or not a directory at '" + modelsDir + "'");
        }
        return true;
    }

    /**
     * Validates that the default model file exists and is accessible.
     * <p>
     * This bean performs the following validation checks:
     * <ol>
     *     <li>Constructs the path to the default model file</li>
     *     <li>Checks if the model file exists at the specified path</li>
     *     <li>Verifies the path is actually a file</li>
     * </ol>
     * </p>
     * <p>
     * The default model file is expected to be named {@code model.gguf} within
     * the directory specified by {@code application.defaults.model}. If validation
     * fails, an {@link Exception} is thrown and the application will fail to start.
     * </p>
     *
     * @param modelsDir The path to the models directory
     * @param model     The default model name
     * @return {@code true} if validation passes (bean is registered as singleton)
     * @throws Exception if default model file is not found or not a file
     */
    @Bean("is_default_model_checked")
    Boolean checkDefaultModel(
            @Value("${application.models}") String modelsDir,
            @Value("${application.defaults.model}") String model
    ) throws Exception {
        File modelFile = new File(new File(modelsDir, model), "model.gguf");
        if (!modelFile.exists() || !modelFile.isFile()) {
            throw new Exception("Default model file not found at '" + modelFile.getAbsolutePath());
        }
        return true;
    }
}