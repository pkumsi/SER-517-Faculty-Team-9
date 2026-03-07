# Logger Guide

This guide explains how logging is set up in the application and how to use it effectively.

---

## Overview

The application uses Go's built-in `log` package for logging. Logs are written to both the console and a log file (`api.log`) for persistent storage. The log file is created in the same directory as the application.

---

## Features

- **Log Levels**: The application supports different log levels (e.g., `info`, `debug`, `warn`, `error`) based on the configuration.
- **Log File**: All logs are written to `api.log` in append mode.
- **Request Logging**: Each API request is logged with details such as HTTP method, path, status code, and duration.

---

## Log File Location

The log file is created in the same directory as the application: