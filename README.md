fully update GemmaForge-Systems

GemmaForge is an enterprise-grade AI migration engine that automates the translation of high-performance GPU kernels from NVIDIA’s proprietary CUDA to AMD’s open ROCm platform.Designed specifically for compute-heavy sectors like FinTech and RegTech, it eliminates the costly bottleneck of manual code porting and breaks vendor lock-in.
GemmaForge: Enterprise CUDA to ROCm Migration Engine

    
GemmaForge is an enterprise-grade AI DevOps pipeline that automates the translation of high-performance GPU kernels from NVIDIA’s proprietary CUDA to AMD’s open ROCm platform.
Designed for compute-heavy, highly regulated sectors like FinTech and RegTech, GemmaForge eliminates the massive engineering bottleneck of manual code porting, breaking vendor lock-in and democratizing access to high-performance compute infrastructure.
🎯 The Problem & The Solution

The Problem: Organizations locked into the CUDA ecosystem face immense technical debt when migrating to cost-effective AMD hardware. Manual porting is slow and prone to "silent architecture failures" (e.g., failing to account for NVIDIA's 32-thread warps vs. AMD's 64-thread wavefronts).
The Solution: GemmaForge acts as an intelligent migration bridge. It doesn't just replace syntax; it understands GPU architecture, recalculates thread mathematics, and embeds the refactored code directly into corporate deployment pipelines with full human-in-the-loop governance.
✨ Core Enterprise Features

Architectural AI Translation: Deep structural refactoring that automatically scales thread logic from 32-thread warps to 64-thread wavefronts, preventing silent runtime bugs.
Automated DevOps Pipeline: Zero-friction GitHub API integration. GemmaForge automatically branches, commits, and opens Pull Requests (PRs) for refactored kernels directly into corporate repositories.
Governance & Compliance (RBAC): Built-in Role-Based Access Control enforcing an Engineer (execution) and Reviewer (approval) workflow to meet FinTech/RegTech audit requirements.
Corporate ROI Dashboard: Tracks aggregate project migrations, token usage, and estimates capital saved per kernel.
ROCm Advisor Bot: A community-backed diagnostic engine that interprets raw compiler errors and provides plain-English, actionable fixes based on known ROCm implementation patterns.
🏗️ System Architecture

LayerTechnologyPurposeFrontendAngularCorporate dashboard, wavefront visualizer, and code review UI.BackendJava, Spring BootREST APIs, GitHub integration, and business/governance logic.DatabasePostgreSQLACID-compliant storage for audit logs, reports, and RBAC users.AI EngineGemma 2 (via Ollama/Fireworks API)LLM inference optimized for C++/HIP code generation.ValidationAMD Developer CloudDirect raw GPU access for validating compiled HIP/ROCm output.
🚀 Getting Started

Prerequisites

Java 17+ and Maven
Node.js and Angular CLI (npm i -g @angular/cli)
PostgreSQL running on port 5432
GitHub Personal Access Token (Fine-grained, with PR & Repo Read/Write access)
Ollama running locally (with gemma2 model installed), or Fireworks AI API key.
1. Database Setup

Create a PostgreSQL database named GemmaForgeDB. The system utilizes data.sql to automatically seed an Admin user and initial ROCm Advisor knowledge-base data upon first boot.
2. Environment Variables

Configure your application.properties or export the following environment variables:
export DB_USERNAME=postgresexport DB_PASSWORD=your_passwordexport GITHUB_TOKEN=github_pat_your_token_hereexport OLLAMA_URL=http://localhost:11434
