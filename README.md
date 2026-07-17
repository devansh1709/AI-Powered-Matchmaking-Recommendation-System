# AI-Powered Matchmaking & Recommendation System

A full-stack matrimonial matchmaking platform that ranks candidate profiles using **semantic (RAG) retrieval over a vector database**, scores compatibility with a rule-based engine, and narrates the result with an LLM — all behind JWT-secured REST APIs with real-time chat over STOMP.

Built as a production-style backend project: JWT + BCrypt auth, Spring AI + Qdrant for retrieval-augmented matching, STOMP WebSocket messaging, Dockerized services, a Jenkins CI/CD pipeline, and JUnit 5 + Mockito test coverage.

---

## Table of contents

- [Architecture](#architecture)
- [Tech stack](#tech-stack)
- [Features](#features)
- [How matching works](#how-matching-works)
- [Getting started](#getting-started)
- [Environment variables](#environment-variables)
- [API overview](#api-overview)
- [Running tests](#running-tests)
- [CI/CD](#cicd)
- [Project structure](#project-structure)
- [Roadmap](#roadmap)

---

## Architecture

```
┌─────────────────┐        REST (JWT)        ┌──────────────────────┐
│  React Frontend │ ───────────────────────▶ │   Spring Boot API    │
│  (Vite + STOMP) │ ◀─────────────────────── │                       │
└─────────────────┘      STOMP / SockJS      │  ┌─────────────────┐ │
                          (live chat)          │  │ Spring Security │ │
                                                │  │   JWT filter    │ │
                                                │  └─────────────────┘ │
                                                │  ┌─────────────────┐ │
                                                │  │ Semantic Match  │─┼──▶  Qdrant (vector DB)
                                                │  │   (RAG layer)   │ │      profile embeddings
                                                │  └─────────────────┘ │
                                                │  ┌─────────────────┐ │
                                                │  │  Match Scoring  │ │
                                                │  │ (rule-based re- │ │
                                                │  │    ranking)     │ │
                                                │  └─────────────────┘ │
                                                │  ┌─────────────────┐ │
                                                │  │   AI Advisor    │─┼──▶  Ollama (local) / Gemini
                                                │  │ (narrative gen) │ │
                                                │  └─────────────────┘ │
                                                └───────────┬──────────┘
                                                             │
                                                             ▼
                                                        MySQL (profiles,
                                                     accounts, chats, etc.)
```

**Retrieval flow for a match request:** a profile's `about` / `lifeGoals` / `partnerExpectations` text is embedded and used to query Qdrant for the top-K semantically closest opposite-gender profiles → those candidates are re-ranked by a deterministic compatibility scorer (location, lifestyle, family plans, etc.) → the top result is optionally narrated by an LLM via Spring AI.

---

## Tech stack

**Backend**
- Java 21, Spring Boot 3.5
- Spring Security 6 + JWT (`jjwt`) + BCrypt
- Spring Data JPA + MySQL
- Spring AI 1.0 — Ollama (local) and Gemini chat models, Ollama embeddings
- Qdrant — vector store for semantic profile retrieval (RAG)
- Spring WebSocket — STOMP over SockJS for real-time chat
- springdoc-openapi — Swagger UI
- JUnit 5, Mockito, AssertJ

**Frontend**
- React 19 + Vite
- `@stomp/stompjs` + `sockjs-client` for live chat

**Infra**
- Docker / Docker Compose (app, MySQL, Qdrant)
- Jenkins pipeline (build → test → Docker image → push → deploy)

---

## Features

- **Semantic matchmaking (RAG)** — profiles are embedded and retrieved by vector similarity from Qdrant before being scored, instead of a full-table scan.
- **AI compatibility reports** — an LLM (Gemini or local Ollama) narrates the scored match into a human-readable compatibility report and answers follow-up questions about a specific pairing.
- **JWT authentication** — signup/login issue signed, expiring tokens; `BCryptPasswordEncoder` for password hashing; stateless sessions.
- **Ownership-scoped authorization** — every endpoint resolves the acting profile from the authenticated JWT principal (`@AuthenticationPrincipal`), not from client-supplied IDs.
- **Interest requests & real-time chat** — send/accept/decline interest requests; accepted requests open a conversation with live messaging over STOMP.
- **API documentation** — interactive Swagger UI generated from the live controllers.
- **Dockerized & CI/CD-ready** — `docker-compose` brings up the full stack; a `Jenkinsfile` builds, tests, and ships a container image.

---

## How matching works

1. **Index:** every profile is converted to a natural-language description (`ProfileDocumentMapper`) and embedded into Qdrant on save (`ProfileEmbeddingService`), keyed by profile ID so re-saves upsert rather than duplicate.
2. **Retrieve:** `SemanticMatchService` embeds a query built from the requester's stated preferences and does a filtered vector similarity search (opposite gender, top-K) against Qdrant.
3. **Score:** `MatchReportService` runs deterministic compatibility scoring (location, lifestyle, family type, children plans, etc.) over the retrieved candidates and ranks them.
4. **Narrate:** `AiAdvisorService` turns the top scored report into a natural-language summary via Spring AI, and can answer free-form follow-up questions about that specific match.

---

## Getting started

### Prerequisites
- JDK 21
- Node.js 18+
- Docker & Docker Compose
- (Optional) [Ollama](https://ollama.ai) running locally with `llama3.2` and `nomic-embed-text` pulled, if you don't want to use Gemini

### 1. Start infrastructure (MySQL + Qdrant)
```bash
cd Matchmaking-backend
docker compose up -d mysql qdrant
```

### 2. Configure environment
Create a `.env` file or export these before running the backend (see [Environment variables](#environment-variables) for the full list):
```bash
export DB_USERNAME=matchmaking_user
export DB_PASSWORD=matchmaking_password
export JWT_SECRET=$(openssl rand -base64 48)
export GEMINI_API_KEY=your-key-here   # required by default — app.ai.provider defaults to gemini
export AI_PROVIDER=ollama             # optional override to prefer the local Ollama model instead
```

### 3. Run the backend
```bash
./mvnw spring-boot:run
```
The API starts on `http://localhost:8080`. On first startup, `ProfileSeeder` and `ProfileEmbeddingSeeder` load and embed a set of demo profiles automatically.

### 4. Run the frontend
```bash
cd ../Matchmaking-frontend
npm install
npm run dev
```
Open `http://localhost:5173`. A demo account (`ananya@example.com` / `Password@123`) is seeded for quick login.

### 5. (Optional) Run everything in Docker
```bash
cd Matchmaking-backend
docker compose up -d
```

### Explore the API
Once running, open **`http://localhost:8080/swagger-ui.html`** for interactive API docs.

---

## Environment variables

| Variable | Default | Purpose |
|---|---|---|
| `DB_HOST` / `DB_PORT` / `DB_NAME` | `localhost` / `3306` / `matchmaking_db` | MySQL connection |
| `DB_USERNAME` / `DB_PASSWORD` | `root` / _(none)_ | MySQL credentials |
| `JWT_SECRET` | placeholder — **override in any real deployment** | HMAC signing key for JWTs (32+ chars) |
| `JWT_EXPIRATION_MS` | `86400000` (24h) | Token lifetime |
| `APP_CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://127.0.0.1:5173` | Allowed frontend origins |
| `AI_PROVIDER` | `gemini` | `ollama` or `gemini` — which model is preferred for AI advisor chats (the other is used as fallback on failure). **Note:** `docker-compose.yml` hardcodes `AI_PROVIDER: ollama` for the containerized `app` service, so running via Docker Compose prefers Ollama first even though the application's own default is Gemini — override it in `docker-compose.yml` if you want the same order in both environments. |
| `OLLAMA_BASE_URL` / `OLLAMA_MODEL` | `http://localhost:11434` / `llama3.2` | Local LLM config |
| `GEMINI_API_KEY` / `GEMINI_MODEL` | _(none)_ / `gemini-2.5-flash-lite` | Gemini config, if used |
| `QDRANT_HOST` / `QDRANT_PORT` | `localhost` / `6334` | Vector store connection (gRPC port) |

> **Never commit real secrets.** All of the above are read from environment variables with safe local defaults — see `application.properties`.

---

## API overview

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/auth/signup` | Public | Create an account + profile, returns a JWT |
| `POST` | `/api/auth/login` | Public | Authenticate, returns a JWT |
| `GET` | `/api/profiles` | Public | Search profiles by city/religion/gender |
| `GET` | `/api/profiles/{id}` | Public | Get a single profile |
| `POST` | `/api/profiles/reindex` | Admin | Re-embed all profiles into Qdrant |
| `GET` | `/api/matches` | JWT | Semantic + scored match recommendations for the caller |
| `POST` | `/api/match-reports` | JWT | Full compatibility report between two profiles |
| `POST` | `/api/ai/chat` | JWT | Ask the AI advisor about a specific match |
| `GET`/`POST` | `/api/interest-requests` | JWT | List / send interest requests |
| `POST` | `/api/interest-requests/{id}/accept` \| `/decline` | JWT | Respond to a request |
| `GET` | `/api/conversations` | JWT | List the caller's conversations |
| `GET`/`POST` | `/api/conversations/{id}/messages` | JWT | Read / send chat messages |
| STOMP | `/ws` → subscribe `/topic/conversations/{id}` | JWT (on CONNECT) | Live message delivery |

Full request/response schemas are available in Swagger UI at runtime.

---

## Running tests

```bash
cd Matchmaking-backend
./mvnw test
```
Covers authentication (signup/login success and failure paths), compatibility scoring logic, and interest-request/conversation authorization rules (e.g. only a request's receiver can accept it; only conversation participants can read its messages).

---

## CI/CD

The included `Jenkinsfile` runs: **checkout → build → test (with JUnit report publishing) → package → Docker build → Docker push**. It requires a `dockerhub-credentials` username/password credential in Jenkins for the push stage, and logs out of Docker Hub in a `post { always }` block regardless of outcome.

There is currently no deploy stage in the pipeline — pushing the image to Docker Hub is the last automated step. Rolling it out to EC2 (e.g. over SSH with an `ec2-ssh-key` credential) is a manual step today; see [Roadmap](#roadmap).

---

## Project structure

```
Matchmaking-backend/src/main/java/com/MatchmakingBackend/
├── controller/     AiController · AuthController · ConversationController ·
│                   InterestRequestController · MatchController ·
│                   MatchReportController · ProfileController  (16 REST endpoints total)
├── service/        AiAdvisorService, MatchReportService, SemanticMatchService,
│                   ProfileEmbeddingService, ProfileDocumentMapper,
│                   ConnectionService, ChatMessageBroadcaster
├── client/         AiClientRouter (Ollama/Gemini preference + fallback), OllamaClient,
│                   GeminiClient, AiTextClient
├── security/       JwtService, JwtAuthenticationFilter, StompAuthChannelInterceptor
│                   (validates the bearer token at STOMP CONNECT), CustomUserDetailsService
├── config/         SecurityConfig, WebSocketConfig, SpringAiConfig, ProfileSeeder,
│                   ProfileEmbeddingSeeder, AccountSeeder
├── entity/          Account, Profile, InterestRequest, Conversation, ChatMessage
├── repo/            Spring Data JPA repositories for the entities above
├── dto/             Request/response DTOs
└── enums/           Role, InterestRequestStatus

Matchmaking-frontend/
├── src/main.jsx           App shell (login, dashboard, profile detail, chat)
├── src/services/          apiClient (JWT-aware fetch), stompClient (live chat)
```

---

## Roadmap

- [ ] Move message sends onto STOMP `@MessageMapping` directly (currently REST-write / STOMP-broadcast split)
- [ ] Pagination for `/api/profiles` search results
- [ ] Rate limiting on `/api/auth/**`
- [ ] Refresh tokens (current JWTs are access-token-only with a fixed expiry)

---

## Author

**Devansh Gupta**  
B.Tech CSE 2027 · Pranveer Singh Institute of Technology, Kanpur  
[LinkedIn](https://www.linkedin.com/in/devansh-gupta-720960285/) · [GitHub](https://github.com/devansh1709) · [LeetCode](https://leetcode.com/devansh1709)
