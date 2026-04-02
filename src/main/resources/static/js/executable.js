/*
* Util functions.
* */
function isPlainObject(value) {
    return (
        value !== null &&
        typeof value === "object" &&
        !Array.isArray(value) &&
        Object.prototype.toString.call(value) === "[object Object]"
    );
}

function createElement(tag, textContent = null, classNames = null, attributes = null) {
    if (!tag) throw new Error("Tag is required");
    const element = document.createElement(tag);

    if (isPlainObject(attributes)) {
        for (let [attr, value] of Object.entries(attributes)) {
            if (value) element.setAttribute(attr, value.toString());
        }
    }

    if (classNames) {
        if (Array.isArray(classNames)) {
            for (let className of classNames) element.classList.add(className.toString());
        } else element.classList.add(classNames.toString());
    }

    if (textContent !== null) {
        element.textContent = textContent.toString();
    }

    return element;
}

function randomUUID() {
    return crypto.randomUUID ? crypto.randomUUID() : ([1e7]+-1e3+-4e3+-8e3+-1e11).replace(/[018]/g, c => (c ^ crypto.getRandomValues(new Uint8Array(1))[0] & 15 >> c / 4).toString(16));
}

function copyToClipboard(text) {
    if (navigator.clipboard && window.isSecureContext) {
        navigator.clipboard.writeText(text);
    } else {
        const textArea = document.createElement("textarea");
        textArea.value = text;

        textArea.style.position = "fixed";
        textArea.style.left = "-9999px";
        textArea.style.top = "0";
        document.body.appendChild(textArea);

        textArea.focus();
        textArea.select();
        document.execCommand('copy');
        document.body.removeChild(textArea);
    }
}

/*
* Server calls wrapper.
* */

class HandlerServerConnector {
    #apiPath = null;
    #apiKeyName = "llama.cpp-handler-api-key";

    constructor(host) {
        this.#apiPath = host + "/api/v1";
    }

    async validateApiKey(apiKey = null) {
        apiKey = apiKey ? apiKey : localStorage.getItem(this.#apiKeyName);
        return fetch(this.#apiPath, {
            method: "GET",
            headers: {"api-key": apiKey}
        })
            .then(response => {
                if (response.status === 200) {
                    localStorage.setItem(this.#apiKeyName, apiKey);
                    return true;
                }
                return false;
            })
    }

    async fetchModels() {
        return fetch(`${this.#apiPath}/models`, {
            method: "GET",
            headers: {"api-key": localStorage.getItem(this.#apiKeyName)}
        })
            .then(response => {
                if (response.status === 200) return response.json();
                if (response.status === 403) return false;
                throw new Error(`Server error, status code: ${response.status}`);
            })
    }

    async fetchRunningModel(pid) {
        return fetch(`${this.#apiPath}/model/${pid}`, {
            method: "GET",
            headers: {"api-key": localStorage.getItem(this.#apiKeyName)}
        })
            .then(response => {
                if (response.status === 200) return response.json();
                if (response.status === 403) return false;
                throw new Error(`Server error, status code: ${response.status}`);
            })
    }

    async fetchRunningModels() {
        return fetch(`${this.#apiPath}/model/all`, {
            method: "GET",
            headers: {"api-key": localStorage.getItem(this.#apiKeyName)}
        })
            .then(response => {
                if (response.status === 200) return response.json();
                if (response.status === 403) return false;
                throw new Error(`Server error, status code: ${response.status}`);
            })
    }

    async runModel(modelConfig) {
        return fetch(`${this.#apiPath}/model`, {
            method: "POST",
            headers: {
                "api-key": localStorage.getItem(this.#apiKeyName),
                "Content-Type": "application/json"
            },
            body: JSON.stringify(modelConfig)
        })
            .then(response => {
                if (response.status === 200) return response.json();
                if (response.status === 403) return false;
                throw new Error(`Server error, status code: ${response.status}`);
            })
            .then(json => {
                if (json) {
                    if (json["error_message"]) {
                        let error = json["error_message"];
                        if (json["logs"]) {
                            error = error + "\n" + json["logs"].join("\n");
                        }
                        throw new Error(error);
                    }
                    return json;
                }
                return false;
            })
    }

    async stopRunningModel(pid) {
        return fetch(`${this.#apiPath}/model/${pid}`, {
            method: "DELETE",
            headers: {"api-key": localStorage.getItem(this.#apiKeyName)}
        })
            .then(response => {
                if (response.status === 200) return true;
                if (response.status === 403) return false;
                throw new Error(`Server error, status code: ${response.status}`);
            })
    }

    async stopRunningModels() {
        return fetch(`${this.#apiPath}/model/all`, {
            method: "DELETE",
            headers: {"api-key": localStorage.getItem(this.#apiKeyName)}
        })
            .then(response => {
                if (response.status === 200) return true;
                if (response.status === 403) return false;
                throw new Error(`Server error, status code: ${response.status}`);
            })
    }
}

/*
* Model editor form controller.
* */
class RunningModelEditor {
    #handlerServerConnector = null;

    constructor(handlerServerConnector) {
        this.#handlerServerConnector = handlerServerConnector;
    }

    editRunningModel(pid, modelConfig, onSuccess) {
        this.#buildModelEditorComponent(
            `Edit model (PID: ${pid})`,
            modelConfig,
            "Edit",
            "btn-edit",
            (editedModelConfig) => {
                return this.#handlerServerConnector.stopRunningModel(pid)
                    .then(stopResult => {
                        if (stopResult) {
                            return this.#handlerServerConnector.runModel(editedModelConfig)
                                .then(runResult => {
                                    if (runResult) {
                                        onSuccess();
                                        return true;
                                    } else throw new Error(`Model is not running, config:\n${JSON.stringify(editedModelConfig)}`);
                                })
                        } else throw new Error(`Running model with pid: ${pid} is not stopped`);
                    });
            }
        );
    }

    runNewModel(modelName, onSuccess) {
        this.runNewModelFromConfig(
            {
                "model": modelName,
                "context": 4096,
                "cpu_threads": 2,
                "serve_local": false,
                "disable_vision": false,
                "api_key": randomUUID(),
                "lock_in_ram": false,
                "k_cache_quant": 16,
                "v_cache_quant": 16,
                "offload_kv": true,
                "layers_upload": -1
            },
            onSuccess
        );
    }

    runNewModelFromConfig(modelConfig, onSuccess) {
        this.#buildModelEditorComponent(
            "Run a model",
            modelConfig,
            "Run",
            "btn-start",
            (editedModelConfig) => {
                return this.#handlerServerConnector.runModel(editedModelConfig)
                    .then(runResult => {
                        if (runResult) {
                            onSuccess();
                            return true;
                        } else throw new Error(`Model is not running, config:\n${JSON.stringify(editedModelConfig)}`);
                    })
            }
        );

    }

    #buildModelEditorComponent(actionTitle, modelConfig, acceptButtonName, acceptButtonClasses, modelConfigProcessingCallback) {
        document.getElementById("running-model-editor-name").textContent = actionTitle;
        const runningModelEditorComponent = document.getElementById("running-model-editor-component");
        runningModelEditorComponent.replaceChildren();
        let inputs = {};

        let configRow = createElement("div", null, "config-row");
        let label = createElement("label", "Model Name:", null, {"for": "model"});
        let input = createElement("input", null, null, {
            "type": "text",
            "id": "model",
            "readonly": "",
            "value": modelConfig["model"]
        });
        configRow.appendChild(label);
        configRow.appendChild(input);
        runningModelEditorComponent.append(configRow);

        configRow = createElement("div", null, "config-row");
        label = createElement("label", "Context Length:", null, {"for": "context"});
        input = createElement("input", null, null, {
            "type": "number",
            "id": "context",
            "value": modelConfig["context"]
        });
        configRow.appendChild(label);
        configRow.appendChild(input);
        runningModelEditorComponent.append(configRow);
        inputs["context"] = input;

        configRow = createElement("div", null, "config-row");
        label = createElement("label", "CPU Threads:", null, {"for": "cpu_threads"});
        input = createElement("input", null, null, {
            "type": "number",
            "id": "cpu_threads",
            "value": modelConfig["cpu_threads"]
        });
        configRow.appendChild(label);
        configRow.appendChild(input);
        runningModelEditorComponent.append(configRow);
        inputs["cpu_threads"] = input;

        configRow = createElement("div", null, "config-row");
        label = createElement("label", "Port:", null, {"for": "port"});
        input = createElement("input", null, null, {"type": "number", "id": "port", "value": modelConfig["port"]});
        configRow.appendChild(label);
        configRow.appendChild(input);
        runningModelEditorComponent.append(configRow);
        inputs["port"] = input;

        configRow = createElement("div", null, "config-row");
        label = createElement("label", "API Key:", null, {"for": "api_key"});
        input = createElement("input", null, null, {"type": "text", "id": "api_key", "value": modelConfig["api_key"]});
        configRow.appendChild(label);
        configRow.appendChild(input);
        runningModelEditorComponent.append(configRow);
        inputs["api_key"] = input;

        configRow = createElement("div", null, "config-row");
        label = createElement("label", "K Cache Quant:", null, {"for": "k_cache_quant"});
        input = createElement("input", null, null, {
            "type": "number",
            "id": "k_cache_quant",
            "value": modelConfig["k_cache_quant"]
        });
        configRow.appendChild(label);
        configRow.appendChild(input);
        runningModelEditorComponent.append(configRow);
        inputs["k_cache_quant"] = input;

        configRow = createElement("div", null, "config-row");
        label = createElement("label", "V Cache Quant:", null, {"for": "v_cache_quant"});
        input = createElement("input", null, null, {
            "type": "number",
            "id": "v_cache_quant",
            "value": modelConfig["v_cache_quant"]
        });
        configRow.appendChild(label);
        configRow.appendChild(input);
        runningModelEditorComponent.append(configRow);
        inputs["v_cache_quant"] = input;

        configRow = createElement("div", null, "config-row");
        label = createElement("label", "Layers Upload:", null, {"for": "layers_upload"});
        input = createElement("input", null, null, {
            "type": "number",
            "id": "layers_upload",
            "value": modelConfig["layers_upload"]
        });
        configRow.appendChild(label);
        configRow.appendChild(input);
        runningModelEditorComponent.append(configRow);
        inputs["layers_upload"] = input;

        configRow = createElement("div", null, "config-row");
        label = createElement("label", "Offload KV (RAM):", null, {"for": "offload_kv"});
        input = createElement("input", null, null, {
            "type": "checkbox",
            "id": "offload_kv",
            "checked": modelConfig["offload_kv"] ? "" : null
        });
        configRow.appendChild(label);
        configRow.appendChild(input);
        runningModelEditorComponent.append(configRow);
        inputs["offload_kv"] = input;

        configRow = createElement("div", null, "config-row");
        label = createElement("label", "Lock in RAM:", null, {"for": "lock_in_ram"});
        input = createElement("input", null, null, {
            "type": "checkbox",
            "id": "lock_in_ram",
            "checked": modelConfig["lock_in_ram"] ? "" : null
        });
        configRow.appendChild(label);
        configRow.appendChild(input);
        runningModelEditorComponent.append(configRow);
        inputs["lock_in_ram"] = input;

        configRow = createElement("div", null, "config-row");
        label = createElement("label", "Serve Local:", null, {"for": "serve_local"});
        input = createElement("input", null, null, {
            "type": "checkbox",
            "id": "serve_local",
            "checked": modelConfig["serve_local"] ? "" : null
        });
        configRow.appendChild(label);
        configRow.appendChild(input);
        runningModelEditorComponent.append(configRow);
        inputs["serve_local"] = input;

        configRow = createElement("div", null, "config-row");
        label = createElement("label", "Disable Vision:", null, {"for": "disable_vision"});
        input = createElement("input", null, null, {
            "type": "checkbox",
            "id": "disable_vision",
            "checked": modelConfig["disable_vision"] ? "" : null
        });
        configRow.appendChild(label);
        configRow.appendChild(input);
        runningModelEditorComponent.append(configRow);
        inputs["disable_vision"] = input;

        const controllerRow = createElement("div", null, "control-row");
        const cancelButton = createElement("button", "Cancel", "btn-cancel");
        cancelButton.addEventListener("click", () => {
            this.#hide();
        });
        controllerRow.append(cancelButton);
        const acceptButton = createElement("button", acceptButtonName, acceptButtonClasses);
        acceptButton.addEventListener("click", () => {
            let modelConfigClone = {...modelConfig};

            if (inputs["context"].value.trim() === "" || Number(inputs["context"].value) < -1) delete modelConfigClone["context"];
            else modelConfigClone["context"] = inputs["context"].value;

            if (inputs["cpu_threads"].value.trim() === "" || Number(inputs["cpu_threads"].value) < 0) delete modelConfigClone["cpu_threads"];
            else modelConfigClone["cpu_threads"] = inputs["cpu_threads"].value;

            if (inputs["port"].value.trim() === "" || Number(inputs["port"].value) < 0) delete modelConfigClone["port"];
            else modelConfigClone["port"] = inputs["port"].value;

            if (inputs["k_cache_quant"].value.trim() === "" || Number(inputs["k_cache_quant"].value) < 0) delete modelConfigClone["k_cache_quant"];
            else modelConfigClone["k_cache_quant"] = inputs["k_cache_quant"].value;

            if (inputs["v_cache_quant"].value.trim() === "" || Number(inputs["v_cache_quant"].value) < 0) delete modelConfigClone["v_cache_quant"];
            else modelConfigClone["v_cache_quant"] = inputs["v_cache_quant"].value;

            if (inputs["layers_upload"].value.trim() === "" || Number(inputs["layers_upload"].value) < -1) delete modelConfigClone["layers_upload"];
            else modelConfigClone["layers_upload"] = inputs["layers_upload"].value;

            if (inputs["api_key"].value.trim() === "") delete modelConfigClone["api_key"];
            else modelConfigClone["api_key"] = inputs["api_key"].value;

            modelConfigClone["serve_local"] = inputs["serve_local"].checked;
            modelConfigClone["disable_vision"] = inputs["disable_vision"].checked;
            modelConfigClone["lock_in_ram"] = inputs["lock_in_ram"].checked;
            modelConfigClone["offload_kv"] = inputs["offload_kv"].checked;

            modelConfigProcessingCallback(modelConfigClone)
                .then(res => {
                    if (res) {
                        this.#hide();
                    }
                })
        });
        controllerRow.append(acceptButton);
        runningModelEditorComponent.append(controllerRow);

        Array.from(document.getElementsByClassName("right-hidden")).forEach(el => el.className = "right");
    }

    #hide() {
        document.getElementById("running-model-editor-name").textContent = "";
        document.getElementById("running-model-editor-component").replaceChildren();
        Array.from(document.getElementsByClassName("right")).forEach(el => el.className = "right-hidden");
    }
}

/*
* Available models table element controller.
* */
class AvailableModelsController {
    #handlerServerConnector = null;
    #runningModelEditor = null;
    #runningModelsController = null;

    constructor(handlerServerConnector, runningModelEditor, runningModelsController) {
        this.#handlerServerConnector = handlerServerConnector;
        this.#runningModelEditor = runningModelEditor;
        this.#runningModelsController = runningModelsController;
    }

    displayAvailableModels() {
        const table = createElement("table");

        const tableHead = createElement("thead");
        const tr = createElement("tr");
        let th = createElement("th", "Model", "col-model");
        tr.append(th);
        for (let label of ["Vision", "Embeddings"]) {
            const th = createElement("th", label, "col-small");
            tr.appendChild(th);
        }

        th = createElement("th", "Action", "col-action");
        tr.append(th);

        tableHead.appendChild(tr);
        table.append(tableHead);

        const tableBody = createElement("tbody");
        table.append(tableBody);

        document.getElementById("available-models").replaceChildren(table);

        this.#handlerServerConnector.fetchModels()
            .then(modelRecords => {
                if (modelRecords.length === 0) {
                    const tr = createElement("tr");
                    const spannedRow = createElement("td", "No models available");
                    spannedRow.colSpan = 4;
                    tr.append(spannedRow);
                    tableBody.append(tr);
                } else {
                    for (let modelRecord of modelRecords) {
                        const tr = createElement("tr");
                        let td = createElement("td", modelRecord["model"], "col-model");
                        tr.append(td);
                        td = createElement("td", modelRecord["vision"], "col-small");
                        tr.append(td);
                        td = createElement("td", modelRecord["embeddings"], "col-small");
                        tr.append(td);
                        td = createElement("td", null, "col-action");
                        const runButton = createElement("button", "Run", "btn-run");
                        runButton.addEventListener("click", () => {
                            this.#runModel(modelRecord["model"]);
                        });
                        td.append(runButton);
                        tr.append(td);
                        tableBody.append(tr);
                    }
                }
            });
    }

    #runModel(modelName) {
        this.#runningModelEditor.runNewModel(modelName, () => this.#runningModelsController.displayRunningModels());
    }
}

/*
* Running models table element controller.
* */
class RunningModelsController {
    #handlerServerConnector = null;
    #runningModelEditor = null;

    constructor(handlerServerConnector, runningModelEditor) {
        this.#handlerServerConnector = handlerServerConnector;
        this.#runningModelEditor = runningModelEditor;
    }

    displayRunningModels() {
        const table = createElement("table");

        const tableHead = createElement("thead");
        const tr = createElement("tr");

        let th = createElement("th", "PID", "col-small");
        tr.append(th);
        th = createElement("th", "Model", "col-model");
        tr.append(th);
        for (let label of ["Context", "Vision", "K/V Cache (type)", "K/V Cache (offloaded)", "Layers (offloaded)", "Runs local", "Threads"]) {
            th = createElement("th", label, "col-small");
            tr.appendChild(th);
        }
        th = createElement("th", "Action", "col-action");
        tr.append(th);

        tableHead.appendChild(tr);
        table.append(tableHead);

        const tableBody = createElement("tbody");
        table.append(tableBody);

        document.getElementById("currently-running").replaceChildren(table);

        this.#handlerServerConnector.fetchRunningModels()
            .then(moderRunningRecords => {
                if (moderRunningRecords.length === 0) {
                    const tr = createElement("tr");
                    const spannedRow = createElement("td", "No running models");
                    spannedRow.colSpan = 10;
                    tr.append(spannedRow);
                    tableBody.append(tr);
                } else {
                    for (let modelEntry of moderRunningRecords) {
                        const modelConfig = modelEntry["model"];
                        const tr = createElement("tr");
                        let td = createElement("td", modelEntry["pid"], "col-small");
                        tr.append(td);
                        td = createElement("td", modelConfig["model"], "col-model");
                        tr.append(td);
                        td = createElement("td", modelConfig["context"] == 0 ? "full" : (modelConfig["context"] * 1.0 / 1000) + "k", "col-small");
                        tr.append(td);
                        td = createElement("td", !modelConfig["disable_vision"], "col-small");
                        tr.append(td);
                        td = createElement("td", `${modelConfig["k_cache_quant"]}/${modelConfig["v_cache_quant"]}`, "col-small");
                        tr.append(td);
                        td = createElement("td", modelConfig["offload_kv"], "col-small");
                        tr.append(td);
                        td = createElement("td", modelConfig["layers_upload"], "col-small");
                        tr.append(td);
                        td = createElement("td", modelConfig["serve_local"], "col-small");
                        tr.append(td);
                        td = createElement("td", modelConfig["cpu_threads"], "col-small");
                        tr.append(td);
                        td = createElement("td", null, "col-action");

                        const actionWrapper = createElement("div", null, "actions-wrapper");

                        const actionButton = createElement("button", "Actions", "btn-actions");
                        actionWrapper.appendChild(actionButton);
                        const dropDown = createElement("div", null, "dropdown-menu");

                        const copyApiKeyButton = createElement("button", "Copy API Key");
                        copyApiKeyButton.addEventListener("click", () => {
                            copyToClipboard(modelConfig["api_key"]);
                        });
                        dropDown.append(copyApiKeyButton);

                        const visitLlamaCppUIButton = createElement("button", "Visit UI");
                        visitLlamaCppUIButton.addEventListener("click", () => {
                            this.#visitLlamaServerUI(modelEntry["host"], modelConfig["port"]);
                        });
                        dropDown.append(visitLlamaCppUIButton);

                        const editRunningModelButton = createElement("button", "Edit");
                        editRunningModelButton.addEventListener("click", () => {
                            this.#editRunningModel(modelEntry["pid"], modelConfig);
                        });
                        dropDown.append(editRunningModelButton);

                        const cloneRunningModelButton = createElement("button", "Start new");
                        cloneRunningModelButton.addEventListener("click", () => {
                            this.#cloneRunningModel(modelConfig);
                        });
                        dropDown.append(cloneRunningModelButton);

                        const stopRunningModelButton = createElement("button", "Stop");
                        stopRunningModelButton.addEventListener("click", () => {
                            this.#stopRunningModel(modelEntry["pid"]);
                        });
                        dropDown.append(stopRunningModelButton);

                        actionWrapper.append(dropDown);
                        td.append(actionWrapper);

                        tr.append(td);
                        tableBody.append(tr);
                    }
                }
            });
    }

    #visitLlamaServerUI(url, port, secure = false) {
        window.open(`${secure ? "https" : "http"}://${url}:${port}`, "_blank");
    }

    #editRunningModel(pid, modelConfig) {
        this.#runningModelEditor.editRunningModel(pid, modelConfig, () => this.displayRunningModels());
    }

    #cloneRunningModel(modelConfig) {
        this.#runningModelEditor.runNewModelFromConfig(modelConfig, () => this.displayRunningModels())
    }

    #stopRunningModel(pid) {
        this.#handlerServerConnector.stopRunningModel(pid)
            .then(result => {
                if (result) this.displayRunningModels();
            })
    }
}

/*
* Init on document loaded.
* */
document.addEventListener("DOMContentLoaded", async () => {
    const handlerServerConnector = new HandlerServerConnector("");
    let isValid = await handlerServerConnector.validateApiKey();
    let attempts = 5;
    while (!isValid && attempts-- > 0) {
        isValid = await handlerServerConnector.validateApiKey(
            prompt("Input the API key for the service")
        );
    }

    if (!isValid && !attempts) alert("Failed to validate API key! Reload page and try again!");
    else {
        const runningModelEditor = new RunningModelEditor(handlerServerConnector);
        const runningModelsController = new RunningModelsController(handlerServerConnector, runningModelEditor);
        const availableModelsController = new AvailableModelsController(handlerServerConnector, runningModelEditor, runningModelsController);
        availableModelsController.displayAvailableModels();
        runningModelsController.displayRunningModels();
    }
});

