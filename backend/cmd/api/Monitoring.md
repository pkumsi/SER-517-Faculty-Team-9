#### Metrics Endpoint
The API exposes internal performance data for Prometheus at:
GET http://localhost:8080/metrics

#### Health Check
To verify the server is alive without pulling full metrics:
GET http://localhost:8080/health
Response: {"status": "ok"}

#### Features
We use the ginprom middleware to automatically track:
Request Count: How many hits each endpoint gets.
Latency: How long the LLM takes to respond.
HTTP Errors: Tracking 4xx and 5xx status codes.
#### How to use
1. Start the backend.
2. Point a Prometheus instance to the /metrics path.
3. (Optional) Use Grafana to visualize the data.