# llama.cpp-handler

A UI-wrapper to start LLM locally using llama.cpp server. This application provides a REST API and web interface for managing and running Llama.cpp model inference processes.

## Features

- **Model Management**: List, start, and stop LLM models
- **REST API**: Full-featured API for programmatic control
- **Web Interface**: User-friendly web UI for model management
- **Security**: API key authentication with Spring Security
- **Process Monitoring**: Track running model processes and their status
- **Configuration**: Flexible configuration with sensible defaults

## Architecture

The application follows a layered architecture with the following components:

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Web Layer (Spring MVC)                       │
│  ┌─────────────────┐  ┌───────────────────────┐  ┌───────────────┐  │
│  │ ViewController  │  │ ModelRunnerController │  │ ApiKeyFilter  │  |
│  └─────────────────┘  └───────────────────────┘  └───────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
│
▼
┌─────────────────────────────────────────────────────────────┐
│                    Service Layer                            │
│  ┌──────────────────────────────────────────────────────┐   │
│  │              ModelRunnerService                      │   │
│  │  - Process lifecycle management                      │   │
│  │  - Model validation                                  │   │
│  │  - Port availability checking                        │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
│
▼
┌─────────────────────────────────────────────────────────────┐
│                    Domain Layer                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │ ModelConfig │  │ ModelEntry  │  │     ModelRecord     │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

## Components

### Domain Layer

- **[`ModelConfig`](file:///home/valdisdot/Projects/java/NOW/LlamaCppHandler/src/main/java/com/valdisdot/llamacpphandler/domain/ModelConfig.java)**: Configuration for model inference with parameters like model name, context length, CPU threads, port, etc.
- **[`ModelEntry`](file:///home/valdisdot/Projects/java/NOW/LlamaCppHandler/src/main/java/com/valdisdot/llamacpphandler/domain/ModelEntry.java)**: Represents a running model with process ID, host address, and configuration.
- **[`ModelRecord`](file:///home/valdisdot/Projects/java/NOW/LlamaCppHandler/src/main/java/com/valdisdot/llamacpphandler/domain/ModelRecord.java)**: Metadata about available models including vision support and embeddings.

### Service Layer

- **[`ModelRunnerService`](file:///home/valdisdot/Projects/java/NOW/LlamaCppHandler/src/main/java/com/valdisdot/llamacpphandler/service/ModelRunnerService.java)**: Core service for managing model inference processes, including:
  - Listing available models
  - Starting/stopping models
  - Process monitoring
  - Configuration validation

### Controller Layer

- **[`ModelRunnerController`](file:///home/valdisdot/Projects/java/NOW/LlamaCppHandler/src/main/java/com/valdisdot/llamacpphandler/controller/ModelRunnerController.java)**: REST API endpoints for model management
- **[`ViewController`](file:///home/valdisdot/Projects/java/NOW/LlamaCppHandler/src/main/java/com/valdisdot/llamacpphandler/controller/ViewController.java)**: Web interface controller

### Security Layer

- **[`ApiKeyFilter`](file:///home/valdisdot/Projects/java/NOW/LlamaCppHandler/src/main/java/com/valdisdot/llamacpphandler/config/security/ApiKeyFilter.java)**: Validates API key authentication
- **[`WebSecurityConfig`](file:///home/valdisdot/Projects/java/NOW/LlamaCppHandler/src/main/java/com/valdisdot/llamacpphandler/config/security/WebSecurityConfig.java)**: Spring Security configuration with two security chains (API and UI)

### Configuration Layer

- **[`ApplicationConfig`](file:///home/valdisdot/Projects/java/NOW/LlamaCppHandler/src/main/java/com/valdisdot/llamacpphandler/config/app/ApplicationConfig.java)**: Application beans and validation checks

## API Documentation

### Base URL
```
http://localhost:10000/api/v1
```

### Endpoints

#### Check Service Availability
```http
GET /api/v1
```
Returns `200 OK` if service is available.

#### List Available Models
```http
GET /api/v1/models
```
Returns a list of available models with metadata.

**Response Example:**
```json
[
  {
    "model": "gemma3-4b-q4_0-qat",
    "vision": false,
    "embeddings": false
  }
]
```

#### Get Running Model
```http
GET /api/v1/model/{pid}
```
Returns metadata for a specific running model.

**Response Example:**
```json
{
  "pid": 12345,
  "host": "127.0.0.1",
  "model": "gemma3-4b-q4_0-qat",
  "context": 0,
  "cpu_threads": 4,
  "port": 10000,
  "serve_local": true,
  "disable_vision": false,
  "api_key": "an-api-key",
  "lock_in_ram": true,
  "k_cache_quant": 16,
  "v_cache_quant": 16,
  "offload_kv": true,
  "layers_upload": -1
}
```

#### Get All Running Models
```http
GET /api/v1/model/all
```
Returns metadata for all running models.

#### Start a Model
```http
POST /api/v1/model
Content-Type: application/json
api-key: an-api-key

{
  "model": "gemma3-4b-q4_0-qat",
  "context": 0,
  "cpu_threads": 4,
  "port": 10000,
  "serve_local": true,
  "disable_vision": false,
  "api_key": "an-api-key",
  "lock_in_ram": true,
  "k_cache_quant": 16,
  "v_cache_quant": 16,
  "offload_kv": true,
  "layers_upload": -1
}
```

**Response Example:**
```json
{
  "pid": 12345,
  "host": "127.0.0.1",
  "model": "gemma3-4b-q4_0-qat",
  "context": 0,
  "cpu_threads": 4,
  "port": 10000,
  "serve_local": true,
  "disable_vision": false,
  "api_key": "an-api-key",
  "lock_in_ram": true,
  "k_cache_quant": 16,
  "v_cache_quant": 16,
  "offload_kv": true,
  "layers_upload": -1
}
```

#### Stop a Model
```http
DELETE /api/v1/model/{pid}
api-key: an-api-key
```
Stops a running model by its process ID.

#### Stop All Models
```http
DELETE /api/v1/model/all
api-key: an-api-key
```
Stops all running models.

## Configuration

### Required Properties

| Property | Description | Example |
|----------|-------------|---------|
| `application.llamacpp.bin` | Path to llama.cpp binary | `/usr/local/bin/llama-cpp-server` |
| `application.models` | Path to models directory | `/models` |
| `server.api_key` | API key for authentication | `an-api-key` |

### Default Model Configuration

| Property | Description | Default Example |
|----------|-------------|-----------------|
| `application.defaults.model` | Default model name | `gemma3-4b-q4_0-qat` |
| `application.defaults.context` | Default context length | `0` |
| `application.defaults.cpu_threads` | Default CPU threads | `4` |
| `application.defaults.port` | Default port | `10000` |
| `application.defaults.serve_local` | Serve locally flag | `true` |
| `application.defaults.disable_vision` | Disable vision flag | `false` |
| `application.defaults.api_key` | Default API key | `an-api-key` |
| `application.defaults.lock_in_ram` | Lock in RAM flag | `true` |
| `application.defaults.k_cache_quant` | KV cache quantization | `16` |
| `application.defaults.v_cache_quant` | V cache quantization | `16` |
| `application.defaults.offload_kv` | KV offload flag | `true` |
| `application.defaults.layers_upload` | GPU layers to upload | `-1` |

### Example `application.properties`

```properties
# llama.cpp binary path
application.llamacpp.bin=/usr/local/bin/llama-cpp-server

# Models directory
application.models=/models

# API key for authentication
server.api_key=an-api-key

# Default model configuration
application.defaults.model=gemma3-4b-q4_0-qat
application.defaults.context=0
application.defaults.cpu_threads=4
application.defaults.port=10000
application.defaults.serve_local=true
application.defaults.disable_vision=false
application.defaults.api_key=an-api-key
application.defaults.lock_in_ram=true
application.defaults.k_cache_quant=16
application.defaults.v_cache_quant=16
application.defaults.offload_kv=true
application.defaults.layers_upload=-1
```

## Usage

### 1. Start the Application

```bash
mvn spring-boot:run
```

### 2. Check Service Availability

```bash
curl http://localhost:10000/api/v1
```

### 3. List Available Models

```bash
curl http://localhost:10000/api/v1/models
```

### 4. Start a Model

```bash
curl -X POST http://localhost:10000/api/v1/model \
  -H "Content-Type: application/json" \
  -H "api-key: an-api-key" \
  -d '{
    "model": "gemma3-4b-q4_0-qat",
    "context": 0,
    "cpu_threads": 4,
    "port": 10000,
    "serve_local": true,
    "disable_vision": false,
    "api_key": "an-api-key",
    "lock_in_ram": true,
    "k_cache_quant": 16,
    "v_cache_quant": 16,
    "offload_kv": true,
    "layers_upload": -1
  }'
```

### 5. Get Running Models

```bash
curl http://localhost:10000/api/v1/model/all \
  -H "api-key: an-api-key"
```

### 6. Stop a Model

```bash
curl -X DELETE http://localhost:10000/api/v1/model/12345 \
  -H "api-key: an-api-key"
```

### 7. Stop All Models

```bash
curl -X DELETE http://localhost:10000/api/v1/model/all \
  -H "api-key: an-api-key"
```

### 8. Access Web Interface

```bash
curl http://localhost:10000
```

## Security

The application uses API key authentication:

- All API requests must include the `api-key` header
- The API key is validated against the configured `server.api_key` property
- Invalid or missing API keys return `401 Unauthorized`
- The web interface allows public access to static resources

## Dependencies

- **Spring Boot**: Application framework
- **Spring Security**: Authentication and authorization
- **Spring MVC**: Web framework
- **Jackson**: JSON processing
- **Apache Log4j**: Logging

## Project Structure

```
LlamaCppHandler/
├── src/main/java/com/valdisdot/llamacpphandler/
│   ├── config/
│   │   ├── app/
│   │   │   └── ApplicationConfig.java
│   │   └── security/
│   │       ├── ApiKeyFilter.java
│   │       └── WebSecurityConfig.java
│   ├── controller/
│   │   ├── ModelRunnerController.java
│   │   └── ViewController.java
│   ├── domain/
│   │   ├── ModelConfig.java
│   │   ├── ModelEntry.java
│   │   └── ModelRecord.java
│   └── service/
│       └── ModelRunnerService.java
├── src/main/resources/
│   ├── application.properties
│   └── index.html
└── README.md
```
## UI Example

![UI Example](https://i.ibb.co/v4zy95qK/image.png)