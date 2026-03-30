package com.valdisdot.llamacpphandler.config.app;

import com.valdisdot.llamacpphandler.domain.ModelConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.nio.charset.StandardCharsets;

@Configuration
public class ApplicationConfig {
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

    @Bean("is_models_dir_checked")
    Boolean checkModelsDir(@Value("${application.models}") String modelsDir) throws Exception {
        File modelsDirFile = new File(modelsDir);
        if (!modelsDirFile.exists() || !modelsDirFile.isDirectory()) {
            throw new Exception("Models directory not found or not a directory at '" + modelsDir + "'");
        }
        return true;
    }

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
