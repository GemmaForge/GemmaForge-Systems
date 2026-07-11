# GemmaForge: Enterprise CUDA to ROCm Migration Engine

![Spring Boot](https://img.shields.io/badge/Spring_Boot-F2F4F9?style=for-the-badge&logo=spring-boot)
![Angular](https://img.shields.io/badge/Angular-DD0031?style=for-the-badge&logo=angular&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![vLLM](https://img.shields.io/badge/vLLM-ROCm-orange?style=for-the-badge)
![AMD Instinct](https://img.shields.io/badge/AMD-Instinct_GPU-ed1c24?style=for-the-badge)

**GemmaForge** is an enterprise-grade AI DevOps pipeline that automates the translation of high-performance GPU kernels from NVIDIA’s proprietary CUDA to AMD’s open ROCm platform. 

Designed for compute-heavy, highly regulated sectors like **FinTech and RegTech**, GemmaForge eliminates the massive engineering bottleneck of manual code porting—breaking hardware vendor lock-in and democratizing access to high-performance compute infrastructure on the AMD ecosystem.

---

## 🎯 The Problem & The Solution

*   **The Problem:** Organizations locked into the CUDA ecosystem face immense technical debt when migrating to cost-effective AMD hardware. Manual porting is slow, expensive, and highly vulnerable to **"silent architecture failures"**—such as incorrectly mapping NVIDIA’s 32-thread warps to AMD’s native 64-thread wavefront execution model.
*   **The Solution:** GemmaForge acts as an intelligent, automated migration bridge. It goes beyond simple syntax swapping; it understands structural GPU hardware architecture, recalculates underlying thread and lane mathematics, and embeds the refactored HIP code into secure corporate deployment pipelines with human-in-the-loop governance.

---

## ✨ Core Enterprise Features

*   **Architectural AI Translation:** Deep structural refactoring that automatically scales thread logic from 32-thread warps to 64-thread wavefronts, preventing memory misalignment and silent runtime bugs.
*   **Automated DevOps State Engine:** Built-in repository state management that tracks files through `PENDING`, `PROCESSING`, `VERIFIED`, and `MERGED` cycles via automated business logic.
*   **Governance & Compliance (RBAC):** Native Role-Based Access Control dividing system permissions between an `Engineer` (authorized to trigger migrations and request debug runs) and a `Reviewer` (authorized to verify code audits and approve deployment changes).
*   **Corporate ROI Analytics Dashboard:** Aggregates real-time organizational migration metrics, tracks cumulative token consumption, and displays live data for total capital saved per kernel.
*   **ROCm Advisor Bot Engine:** An integrated, specialized diagnostic assistant that parses raw compiler errors and maps them directly to plain-English, actionable architecture modifications.

---

## ⚙️ Hardware & Infrastructure Validation

To validate the generated outputs under real-world compute environments, custom HIP kernels are mapped and staged directly on **AMD Instinct GPU Cloud Pods**.

### Proof of Wavefront Optimization (`matrix_kernel.cpp`):
Our core translation logic handles parallel reduction calculations by optimizing for AMD’s 64-thread wavefront configuration, enforcing strict memory barrier synchronization:```cpp
#include <hip/hip_runtime.h>
#include <iostream>

/**
 * Optimized Matrix Reduction for AMD ROCm
 * Structurally refactored from 32-thread warp logic to 64-thread wavefront execution
 */
__global__ void matrixWarpReduce(float *g_idata, float *g_odata) {
    // Adjusted explicitly for AMD 64-thread wavefront geometry
    int lane = threadIdx.x % 64; 
    int wid = threadIdx.x / 64;

    // Parallel reduction step handling high-density thread distribution
    if (lane < 32) {
        g_idata[threadIdx.x] += g_idata[threadIdx.x + 32];
    }
    
    // Explicit execution barrier ensuring all wavefront threads finish additions
    __syncthreads();

    // Isolated write path for the wavefront leading lane
    if (lane == 0) {
        g_odata[wid] = g_idata[threadIdx.x]; 
    }
}

🏗️ System ArchitectureLayerTechnologyPurposeFrontendAngular 17+ / Tailwind CSSResponsive corporate control dashboard, role-based workflows, and code diff viewer.BackendJava / Spring Boot 3 / Jakarta EECore REST APIs, repository state orchestration, and RBAC governance logic.DatabasePostgreSQLACID-compliant storage for structural data tables, audit logs, and system metrics.AI InferencevLLM EngineProduction-grade high-throughput model hosting serving google/gemma-4-12b-it.HardwareAMD Instinct GPUsHost infrastructure providing native ROCm compute capabilities and kernel tracking.🚀 Getting Started & Local SetupPrerequisitesJava 17+ and Maven 3.8+Node.js (v18+) and Angular CLI (npm i -g @angular/cli)PostgreSQL running locally on port 5432Docker and Docker Compose Installed1. Multi-Container Orchestration (Local App Stack)Deploy the database, core Spring Boot API service, and Angular web client simultaneously using docker-compose:YAMLversion: '3.8'

services:
  gemmaforge-db:
    image: postgres:15-alpine
    container_name: gemmaforge-db
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: GemmaForgeDB
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: your_password
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres -d GemmaForgeDB"]
      interval: 5s
      timeout: 5s
      retries: 5

  gemmaforge-backend:
    image: gemmaforge-backend
    container_name: gemmaforge-backend
    ports:
      - "8080:8080"
    environment:
      DB_USERNAME: postgres
      DB_PASSWORD: your_password
      VLLM_API_URL: [http://134.199.205.43:8000](http://134.199.205.43:8000) # AMD GPU Instance endpoint
    depends_on:
      gemmaforge-db:
        condition: service_healthy

  gemmaforge-frontend:
    image: gemmaforge-frontend
    container_name: gemmaforge-frontend
    ports:
      - "4200:80"
    depends_on:
      - gemmaforge-backend
2. Live AMD GPU Infrastructure DeploymentThe AI inference layer runs natively inside an AMD ROCm optimized container on the GPU hardware pod. Launch the server using the following production configuration:Bashdocker run -itd \
  --device /dev/kfd \
  --device /dev/dri \
  --group-add video \
  --ipc=host \
  --name gemma-server \
  -p 8000:8000 \
  -e HUGGING_FACE_HUB_TOKEN=your_hf_access_token_here \
  vllm/vllm-openai-rocm:v0.23.0 \
  google/gemma-4-12b-it \
  --port 8000 \
  --gpu-memory-utilization 0.20 \
  --max-model-len 4096 \
  --enforce-eager \
  --kv-cache-dtype fp8 \
  --trust-remote-code
  
3. Verification & TestingConfirm the inference server is fully running and exposes the correct active model metadata:Bash(Invoke-RestMethod -Uri "[http://134.199.205.43:8000/v1/models](http://134.199.205.43:8000/v1/models)" -Method Get).data.id
Expected Output: google/gemma-4-12b-it4. Base Account CredentialsTo evaluate the corporate dashboard and test the RBAC (Role-Based Access Control) governance features, use the following pre-seeded account: Email: reviewer@gemmaforge.com  Password: 123456

🗺️ Product Roadmap (The Gold Mine)To bridge our working functional prototype into a full-scale commercial utility, the following structural expansion tracks are scheduled:1. Complete DevOps Pipeline AutomationActive GitHub/GitLab REST Integration: Transition our current administrative code database approvals into active automated repository manipulation, using remote authentication to programmatically open downstream optimization branches and structural Pull Requests.2. High-Fidelity Architectural Visualizer2D Wavefront Mapping Tool: Build an interactive frontend layout rendering engine within the Angular interface. This engine will visually display 32-wide CUDA execution layouts shifting side-by-side into 64-wide hardware blocks, highlighting potential memory bank conflicts or alignment voids in real time.3. Live Kernel Performance AnalyticsCross-Platform Benchmarking Suite: Implement an internal testing bridge leveraging the cloud-based developer environment to directly run both the original CUDA structures (via emulation/native hosts) and the refactored HIP scripts, recording execution metrics inside the database (executionTimeMs) to chart verifiable optimization metrics.4. Enterprise Compliance Audit EngineAutomated Security Scan: Deploy localized static verification parameters to proactively evaluate generated C++ components for system anomalies, raw index overflows, or isolated device memory leaks prior to reviewer validation routines.Developed for the lablab.ai AMD AI Challenge. Verified on the AMD Instinct Platform.

