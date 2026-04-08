# SER-517 Faculty Team 9
A Go REST API that generates context-aware auto-responses for incoming messages. It reads sensor data from a mobile device, infers what the user is doing, and uses an LLM (via [Groq](https://groq.com)'s OpenAI-compatible API) to write a short, polite reply on their behalf.

---

## Tech Stack

- **Language:** Go 1.25
- **Framework:** Gin
- **LLM Provider:** Groq (OpenAI-compatible API)
- **Default Model:** `llama-3.3-70b-versatile`

---

## Project Structure

```
backend/
├── cmd/api/
│   ├── main.go          # Entry point, route setup
│   └── logger.go        # Logger setup
├── configs/
│   ├── config.go        # Config struct and loader
│   └── .env.example     # Environment variable template
├── internal/
│   ├── handlers/
│   │   └── llm_handler.go   # HTTP handler for /api/v1/response
│   ├── services/
│   │   ├── llm_service.go   # Main pipeline orchestrator
│   │   ├── inference.go     # Infers activity and context from sensor data
│   │   ├── prompt.go        # Builds the LLM prompt
│   │   └── llm.go           # Calls the LLM provider (Groq by default)
│   └── models/
│       ├── models_input.go  # Request and ContextSnapshot types
│       └── models_output.go # Response and feedback types
```

---
## Setup

### 1. Prerequisites

- Go 1.21+
- A [Groq](https://console.groq.com) API key (free tier available)

## Installing Go

### Mac

The easiest way is via [Homebrew](https://brew.sh):

```bash
brew install go
```

Or download the installer directly from [go.dev/dl](https://go.dev/dl/), pick the `.pkg` file for macOS, and follow the prompts.

Verify the installation:

```bash
go version
```

### Windows

Download the `.msi` installer from [go.dev/dl](https://go.dev/dl/) and run it. It will set up Go and update your `PATH` automatically.

Verify the installation by opening Command Prompt or PowerShell:

```powershell
go version
```

> This project requires **Go 1.21 or higher**.


### 2. Configure environment

```bash
cp configs/.env.example .env
```

Edit `.env` and set your API key:

```
GROQ_API_KEY=your_key_here
```

Optional: set `LLM_MODEL` to any model id your Groq account supports (see [Groq docs](https://console.groq.com/docs/models)). For another OpenAI-compatible API, set `LLM_BASE_URL` and put that provider’s API key in `GROQ_API_KEY`.

### 3. Run the server

```bash
cd backend
go run ./cmd/api
```

The server starts on `http://localhost:8080` by default.

---

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/` | Health check |
| `GET` | `/health` | Health check |
| `POST` | `/api/v1/response` | Generate an auto-response |
| `POST` | `/api/v1/feedback` | Submit feedback for responses |
| `GET` | `/api/v1/statistics/messages` | Get daily message statistics |

### POST `/api/v1/response`

**Request body:**
```json
{
  "request_id": "abc123",
  "context": {
    "uuid": "user-uuid",
    "HourOfDay": 14,
    "Calendar_Event": 1,
    "Event_Name": "Team Standup",
    "Event_TimeLeft": 900000,
    "Notif_App": "com.whatsapp",
    "Predicted_Availability": "0"
  }
}
```

**Response:**
```json
{
  "request_id": "abc123",
  "uuid": "user-uuid",
  "ar_enabled": true,
  "sent_ar": true,
  "responses": [
    "The user is currently in a team standup and will be available in approximately 15 minutes."
  ]
}
```

### GET `/api/v1/statistics/messages`

**Query Parameters:**
- `days` (optional): Number of days to retrieve statistics for (default: 7, max: 365)

**Response:**
```json
{
  "statistics": {
    "2024-01-15": 25,
    "2024-01-14": 32,
    "2024-01-13": 18
  },
  "days": 7
}
```

---

## How It Works

1. **Inference** — Raw sensor fields (calendar, motion, screen state, call status, etc.) are mapped to 5 context elements: Activity, Current Time, Sender Role, Urgency, and Expected Response Time.
2. **Prompt selection** — A prompt variant is chosen based on which elements are available (Standard → Rich → Minimal).
3. **LLM call** — The prompt is sent to Groq (or whatever host `LLM_BASE_URL` points to). The LLM returns a 1–3 sentence auto-response.
4. **Response** — The generated message is returned in the `responses` field.

---

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | `8080` | Server port |
| `HOST` | `localhost` | Server host |
| `ENVIRONMENT` | `development` | `development`, `staging`, or `production` |
| `LOG_LEVEL` | `info` | `debug`, `info`, `warn`, or `error` |
| `GROQ_API_KEY` | *(required)* | Groq API key (bearer token for the LLM request) |
| `LLM_BASE_URL` | `https://api.groq.com/openai/v1` | OpenAI-compatible API base URL |
| `LLM_MODEL` | `llama-3.3-70b-versatile` | Model id for the provider |
| `LLM_MAX_TOKENS` | `1000` | Max tokens per response |

