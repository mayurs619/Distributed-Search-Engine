# Distributed Search Engine

**Live Demo:** [distributed-search-engine-beta.vercel.app](https://distributed-search-engine-beta.vercel.app)

A full-stack, distributed search engine simulating core Big Tech infrastructure. This project features an asynchronous web crawler, a mathematically ranked inverted index, and a horizontally sharded database architecture with a scatter-gather query router.

## System Architecture

```mermaid
graph TD
    subgraph Data Ingestion
        A[Python Crawler Seeds] -->|Async Fetch| B[Web Sources]
        B -->|HTML Response| C[aiohttp Workers]
        C -->|Extract URLs| D[(Redis Task Queue)]
        D -->|Pop Tasks| C
    end

    subgraph Spring Boot Microservice
        C -->|POST JSON| E[API Gateway Controller]
        E -->|Process & Tokenize| F[Indexing Service]
        F -->|Hash URL % 2| G{DataSource Router}
        J[Next.js Client] -->|GET /search| E
        E -->|Scatter-Gather| G
    end

    subgraph Distributed PostgreSQL
        G -->|Result 0| H[(Shard 1: Neon Cloud)]
        G -->|Result 1| I[(Shard 2: Neon Cloud)]
    end
```

## Core Engineering Highlights

* **Ingestion Engine (Python):** Engineered an asynchronous web crawler utilizing `aiohttp` and `asyncio`. It is orchestrated by a distributed Redis task queue (Upstash) to prevent duplicate crawling, manage deep pagination, and handle rate-limiting.
* **API Gateway & Routing (Java/Spring Boot):** Designed a custom scatter-gather search API. The engine dynamically hashes incoming URLs using a custom `AbstractRoutingDataSource` and `ThreadLocal` context, routing traffic seamlessly across multiple isolated, serverless PostgreSQL database shards (Neon).
* **Algorithmic Ranking:** Implemented a custom TF-IDF (Term Frequency-Inverse Document Frequency) algorithm from scratch.
* **Frontend UI (Next.js/React):** Built a responsive, minimalist search interface.

## Testing & Observability

* **Unit Testing (JUnit 5 & Mockito):** Full coverage of TF-IDF ranking logic.
* **Concurrency Testing:** Thread-local database context routing tested under multi-threaded conditions.
* **Application Metrics:** Spring Boot Actuator metrics for monitoring.

## Tech Stack

* Frontend: Next.js, React, Tailwind CSS
* Backend: Java 17, Spring Boot, Spring Data JPA
* Ingestion: Python 3.12, BeautifulSoup4, Redis, aiohttp
* Cloud: Render, Vercel, Neon, Upstash
* Testing: JUnit 5, Mockito, Maven

## Performance Metrics

* 100% backend test suite pass rate (3/3).
* Distributed ingestion of 180+ documents.
* Optimized scatter-gather query execution.

## Local Setup & Execution

1. Configure Redis/Postgres.
2. Run backend with `./mvnw spring-boot:run`.
3. Run crawler with `python crawler.py`.
4. Run frontend with `npm run dev`.
