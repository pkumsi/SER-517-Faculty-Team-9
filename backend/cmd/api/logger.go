package main

import (
    "log"
    "os"
)

var Logger *log.Logger

func InitLogger() {
    logFile, err := os.OpenFile("api.log", os.O_APPEND|os.O_CREATE|os.O_WRONLY, 0666)
    if err != nil {
        log.Fatalf("Failed to open log file: %v", err)
    }

    Logger = log.New(logFile, "[API] ", log.Ldate|log.Ltime|log.Lshortfile)
}