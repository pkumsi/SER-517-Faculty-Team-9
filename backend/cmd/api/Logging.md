#### Log File
- Filename: api.log
- Location: Root directory
- Format: [API] YYYY/MM/DD HH:MM:SS file.go:line: message

#### Setup

The logger is initialized in main.go. It opens the file in append mode, meaning it won't overwrite previous logs on restart.

#### Key Logs Collected
-Server start/stop events
-Environment and Config loading (LLM Model, Port, etc.)
-Endpoint hits (/, /health, /api/v1/response)
-Groq / LLM API key status (see main.go log lines)