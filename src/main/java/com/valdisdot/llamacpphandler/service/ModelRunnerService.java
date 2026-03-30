package com.valdisdot.llamacpphandler.service;

import com.valdisdot.llamacpphandler.domain.ModelConfig;
import com.valdisdot.llamacpphandler.domain.ModelEntry;
import com.valdisdot.llamacpphandler.domain.ModelRecord;
import jakarta.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class ModelRunnerService {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final ExecutorService logExecutor = Executors.newCachedThreadPool();
    private final Map<Long, ModelConfig> runningModels;
    private final ModelConfig defaultModelConfig;
    private final String binPath;
    private final String modelsDir;

    public ModelRunnerService(
            @Value("${application.llamacpp.bin}") String binPath,
            @Value("${application.models}") String modelsDir,
            @Autowired ModelConfig defaultModelConfig
    ) {
        this.binPath = binPath;
        this.modelsDir = modelsDir;
        this.defaultModelConfig = defaultModelConfig;
        this.runningModels = new ConcurrentHashMap<>();
    }

    public List<ModelRecord> listAvailableModels() {
        List<ModelRecord> modelsList = new ArrayList<>();
        Path modelsPath = Paths.get(modelsDir);

        if (Files.exists(modelsPath)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(modelsPath)) {
                for (Path entry : stream) {
                    if (Files.isDirectory(entry)) {
                        Path realPath = entry.toRealPath();
                        Path modelFile = realPath.resolve("model.gguf");

                        if (Files.exists(modelFile)) {
                            ModelRecord record = new ModelRecord();
                            record.setModel(entry.getFileName().toString());
                            record.setVision(Files.exists(realPath.resolve("mmproj.gguf")));
                            record.setEmbeddings(Files.exists(realPath.resolve("embeddings")));
                            modelsList.add(record);
                        }
                    }
                }
            } catch (IOException e) {
                logger.error("Error scanning models directory: {}", this.modelsDir, e);
            }
        }
        return modelsList;
    }

    public boolean isRunning(Long pid) {
        return runningModels.containsKey(pid);
    }

    public Optional<ModelEntry> getRunningModel(Long pid) {
        if (runningModels.containsKey(pid)) {
            ModelConfig config = runningModels.get(pid);
            return Optional.of(new ModelEntry(pid, this.getHostAddress(config.getServeLocal()), config));
        }
        return Optional.empty();
    }

    public List<ModelEntry> getAllRunningModels() {
        return runningModels.entrySet().stream()
                .map(entry -> new ModelEntry(entry.getKey(), this.getHostAddress(entry.getValue().getServeLocal()), entry.getValue()))
                .toList();
    }

    public ModelEntry runModel(ModelConfig config) {
        fulfillModelConfig(config);

        for (Map.Entry<Long, ModelConfig> runningModel : runningModels.entrySet()) {
            if (runningModel.getValue().equals(config)) {
                return new ModelEntry(runningModel.getKey(), this.getHostAddress(config.getServeLocal()), runningModel.getValue());
            }
        }

        Path rawModelDir = Paths.get(modelsDir, config.getModel());
        Path modelDir;
        try {
            modelDir = rawModelDir.toRealPath();
        } catch (IOException e) {
            logger.error("Model directory missing or invalid symlink: {}", rawModelDir);
            return new ModelEntry("Model directory missing or invalid symlink: " + rawModelDir, null);
        }

        Path modelFile = modelDir.resolve("model.gguf");
        if (!Files.exists(modelFile)) {
            logger.error("Model GGUF is missing in path: {}", modelFile);
            return new ModelEntry("model.gguf missing in path: " + modelFile, null);
        }

        if (!isPortAvailable(config.getPort())) {
            logger.error("Port {} is not available", config.getPort());
            return new ModelEntry("Port " + config.getPort() + " is not available", null);
        }

        List<String> cmdList = new ArrayList<>();
        cmdList.add(binPath);
        cmdList.add("-m");
        cmdList.add(modelFile.toAbsolutePath().toString());
        cmdList.add("-a");
        cmdList.add(config.getModel());
        cmdList.add("-c");
        cmdList.add(String.valueOf(config.getContext()));
        cmdList.add("-t");
        cmdList.add(String.valueOf(config.getCpuThreads()));
        cmdList.add("--port");
        cmdList.add(String.valueOf(config.getPort()));
        cmdList.add("--api-key");
        cmdList.add(config.getApikey());
        cmdList.add("--host");
        cmdList.add(Boolean.TRUE.equals(config.getServeLocal()) ? "127.0.0.1" : "0.0.0.0");

        if (Boolean.TRUE.equals(config.getLockInRam())) {
            cmdList.add("--mlock");
        }

        if (Boolean.FALSE.equals(config.getOffloadKV())) {
            cmdList.add("--no-kv-offload");
        }

        if (config.getKCacheQuant() != null) {
            cmdList.add("--cache-type-k");
            cmdList.add(convertQuant(config.getKCacheQuant()));
        }

        if (config.getVCacheQuant() != null) {
            cmdList.add("--cache-type-v");
            cmdList.add(convertQuant(config.getVCacheQuant()));
        }

        if (config.getLayersUpload() != null) {
            cmdList.add("--n-gpu-layers");
            cmdList.add(String.valueOf(config.getLayersUpload()));
        }

        Path mmprojFile = modelDir.resolve("mmproj.gguf");
        if (!Boolean.TRUE.equals(config.getDisableVision()) && Files.exists(mmprojFile)) {
            cmdList.add("--mmproj");
            cmdList.add(mmprojFile.toAbsolutePath().toString());
        }

        if (Files.exists(modelDir.resolve("embeddings"))) {
            cmdList.add("--embeddings");
            cmdList.add("--no-webui");
        }

        ProcessBuilder pb = new ProcessBuilder(cmdList);
        pb.redirectErrorStream(true);
        Process process;

        try {
            process = pb.start();
        } catch (IOException e) {
            logger.error("Failed to start process", e);
            return new ModelEntry("Failed to start process: " + e.getMessage(), null);
        }

        CountDownLatch readyLatch = new CountDownLatch(1);
        List<String> logCapture = Collections.synchronizedList(new LinkedList<>());
        AtomicBoolean started = new AtomicBoolean(false);

        logExecutor.submit(() -> {
            Logger logger = LogManager.getLogger(String.format("llama.cpp [%d]", process.pid()));
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.info(line);

                    synchronized (logCapture) {
                        logCapture.add(line);
                        if (logCapture.size() > 30) logCapture.remove(0);
                    }

                    if (line.contains("starting the main loop...")) {
                        started.set(true);
                        readyLatch.countDown();
                    }
                }
                logger.traceExit();
            } catch (IOException e) {
                logger.error("Error reading process output", e);
            }
            readyLatch.countDown();
        });

        try {
            readyLatch.await(120, TimeUnit.SECONDS);
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (!process.isAlive() && !started.get()) {
            logger.error("Process exited with code {} during startup.", process.exitValue());
            return new ModelEntry("Process exited with code " + process.exitValue() + " during startup.", logCapture);
        }

        if (started.get()) {
            long pid = process.pid();
            runningModels.put(pid, config);
            return new ModelEntry(pid, this.getHostAddress(config.getServeLocal()), config);
        } else {
            process.descendants().forEach(ProcessHandle::destroyForcibly);
            process.destroyForcibly();
            logger.error("Timeout waiting for model to load (exceeded 120s).");
            return new ModelEntry("Timeout waiting for model to load (exceeded 120s).", logCapture);
        }
    }

    public void stopModel(Long pid) {
        if (runningModels.containsKey(pid)) {
            ProcessHandle.of(pid).ifPresent(handle -> {
                handle.descendants().forEach(ProcessHandle::destroyForcibly);
                handle.destroyForcibly();
            });
            runningModels.remove(pid);
        }
    }

    public void stopAllModels() {
        new ArrayList<>(runningModels.keySet()).forEach(this::stopModel);
    }

    private void fulfillModelConfig(ModelConfig config) {
        if (config.getModel() == null || config.getModel().isBlank()) {
            config.setModel(defaultModelConfig.getModel());
        }

        String apiKey = sanitizeApiKey(config.getApikey());
        if (apiKey == null || apiKey.isBlank()) {
            config.setApikey(defaultModelConfig.getApikey());
        } else {
            config.setApikey(apiKey);
        }

        if (config.getKCacheQuant() == null || config.getKCacheQuant() <= 0) {
            config.setKCacheQuant(defaultModelConfig.getKCacheQuant());
        }

        if (config.getVCacheQuant() == null || config.getVCacheQuant() <= 0) {
            config.setVCacheQuant(defaultModelConfig.getVCacheQuant());
        }

        if (config.getContext() == null || config.getContext() < 0) {
            config.setContext(defaultModelConfig.getContext());
        }

        if (config.getCpuThreads() == null || config.getCpuThreads() <= 0) {
            config.setCpuThreads(defaultModelConfig.getCpuThreads());
        }

        if (config.getPort() == null || config.getPort() <= 0) {
            config.setPort(defaultModelConfig.getPort());
        }

        if (config.getLayersUpload() == null) {
            config.setLayersUpload(defaultModelConfig.getLayersUpload());
        }

        if (config.getServeLocal() == null) {
            config.setServeLocal(defaultModelConfig.getServeLocal());
        }

        if (config.getDisableVision() == null) {
            config.setDisableVision(defaultModelConfig.getDisableVision());
        }

        if (config.getLockInRam() == null) {
            config.setLockInRam(defaultModelConfig.getLockInRam());
        }

        if (config.getOffloadKV() == null) {
            config.setOffloadKV(defaultModelConfig.getOffloadKV());
        }
    }

    private String sanitizeApiKey(String apiKey) {
        if (apiKey == null) return null;
        return apiKey.replaceAll("[^a-zA-Z0-9_\\-]", "");
    }

    private String convertQuant(Integer quant) {
        return switch (quant) {
            case 1, 2, 3, 4, 5, 6, 7, 8 -> "q" + quant + "_0";
            case 16, 32 -> "f" + quant;
            default -> "f16";
        };
    }

    private String getHostAddress(Boolean serveLocal) {
        if (Boolean.FALSE.equals(serveLocal)) try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (!iface.isLoopback() && iface.isUp()) {
                    Enumeration<InetAddress> addresses = iface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress address = addresses.nextElement();

                        if (address instanceof java.net.Inet4Address) {
                            return address.getHostAddress();
                        }
                    }
                }

            }
        } catch (SocketException e) {
            logger.error("Error getting host address", e);
        }
        return "127.0.0.1";
    }

    private boolean isPortAvailable(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            logger.error("Port {} is not available", port, e);
            return false;
        }
    }

    @PreDestroy
    public void stopService() {
        this.stopAllModels();
        logExecutor.shutdownNow();
    }
}