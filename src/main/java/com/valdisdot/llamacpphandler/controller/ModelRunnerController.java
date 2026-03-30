package com.valdisdot.llamacpphandler.controller;

import com.valdisdot.llamacpphandler.domain.ModelConfig;
import com.valdisdot.llamacpphandler.service.ModelRunnerService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/*
 * get                   /api/v1                200, 403               checks availability
 * get                   /api/v1/models         200 [ModelRecord]      lists all available models
 * get                   /api/v1/model/{pid}    404, 200 ModelEntry    returns a model meta-data with the given pid
 * get                   /api/v1/model/all      200 [ModelEntry]       returns all running models meta-data
 * post ModelConfig      /api/v1/model          200 [ModelEntry]       runs a model
 * delete                /api/v1/model/{pid}    404, 200               stops a model with the given pid
 * delete                /api/v1/model/all      200                    stops all models
 */

@RestController
@RequestMapping("/api/v1")
@Validated
public class ModelRunnerController {
    private final ModelRunnerService modelRunnerService;

    ModelRunnerController(ModelRunnerService modelRunnerService) {
        this.modelRunnerService = modelRunnerService;
    }

    @GetMapping
    ResponseEntity<?> checkAvailability() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/models")
    ResponseEntity<?> listAvailableModels() {
        return ResponseEntity.ok(modelRunnerService.listAvailableModels());
    }

    @RequestMapping(method = RequestMethod.OPTIONS, path = "/model/{pid}")
    ResponseEntity<?> isRunning(@PathVariable Long pid) {
        return modelRunnerService.isRunning(pid) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/model/{pid}")
    ResponseEntity<?> getRunningModel(@PathVariable Long pid) {
        return modelRunnerService.getRunningModel(pid).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/model/all")
    ResponseEntity<?> getAllRunningModels() {
        return ResponseEntity.ok(modelRunnerService.getAllRunningModels());
    }

    @PostMapping("/model")
    ResponseEntity<?> runModel(@RequestBody ModelConfig config) {
        return ResponseEntity.ok(modelRunnerService.runModel(config));
    }

    @DeleteMapping("/model/{pid}")
    ResponseEntity<?> stopModel(@PathVariable Long pid) {
        if (modelRunnerService.isRunning(pid)) {
            modelRunnerService.stopModel(pid);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/model/all")
    ResponseEntity<?> stopAllModels() {
        modelRunnerService.stopAllModels();
        return ResponseEntity.ok().build();
    }
}
