# GitTree  _(GitHub Repository Structure & Size Analyzer)_

A full-stack developer utility built with **Java, Spring Boot, React, and Docker**. Paste any public GitHub repository URL to inspect its full directory hierarchy with proper ASCII tree connectors (`├──`, `└──`, `│`), recursive folder sizes, individual file sizes, and total repository size—ready to copy as Markdown for documentation.

## Tech Stack

* **Backend:** Java 21, Spring Boot, Spring `RestClient`, Jakarta Bean Validation
* **Frontend:** React
* **External API:** GitHub REST API (Git Trees Recursive Endpoint)
* **Containerization:** Docker & Docker Compose

## Features

* **Recursive Size Calculation:** Computes total repository size and aggregates nested folder sizes upward from individual file blobs.
* **Clean ASCII Directory Tree:** Sorts folders before files alphabetically and renders proper `├──`, `└──`, and `│` hierarchy lines.
* **One-Click Markdown Export:** Copies the formatted directory tree with file and folder sizes directly to the clipboard for pasting into any `README.md`.
* **Validation & Error Handling:** Validates GitHub URLs and handles missing repositories or branches cleanly via `GlobalExceptionHandler` (`400 Bad Request`, `404 Not Found`).

## Quick Start (Docker Compose)

Make sure you have **Docker** and **Docker Compose** installed, then run:

```bash
git clone https://github.com/sachinz98/gittree.git
cd gittree
docker compose up --build
```

Once the containers start:

* **Frontend (React):** `http://localhost:3000`
* **Backend API (Spring Boot):** `http://localhost:8080`

## API Endpoint

### Generate Repository Tree
* **Endpoint:** `POST /api/tree`
* **Request Body:**
  ```json
  {
    "repoUrl": "https://github.com/sachinz98/tinylink",
    "branch": "main"
  }
  ```
* **Response (`200 OK`):**
  ```json
  {
    "repoName": "tinylink",
    "branch": "main",
    "totalFormattedSize": "718.45 KB",
    "treeContents": [
      {
        "id": 1,
        "prefix": "├── ",
        "text": "client/",
        "type": "tree",
        "formattedSize": "676.20 KB"
      }
    ],
    "markdown": "```text\ntinylink/ (718.45 KB)\n├── client/  [676.20 KB]\n```"
  }
  ```

<!--
## Project Structure

```text
gittree/
├── client/                  # React frontend
├── src/main/java/com/sachin/gittree/
│   ├── controller/          # REST endpoint (/api/tree)
│   ├── service/             # GitHub API client, N-ary tree builder & size aggregator
│   ├── dto/                 # Request, response & error DTOs
│   └── exception/           # Custom exceptions & GlobalExceptionHandler
├── Dockerfile               # Spring Boot multi-stage Docker build
└── docker-compose.yml       # Orchestrates Backend & Frontend containers
```

-->
