package logger

import (
	"io"
	"log"
	"os"
)

// Init opens api.log and sets the default log output. If mirrorToStdout is true
// (typical for development), logs are written to both the file and stderr so
// LLM and other errors show up in the terminal, not only in api.log.
func Init(path string, mirrorToStdout bool) (*os.File, error) {
	f, err := os.OpenFile(path, os.O_APPEND|os.O_CREATE|os.O_WRONLY, 0666)
	if err != nil {
		return nil, err
	}
	if mirrorToStdout {
		log.SetOutput(io.MultiWriter(f, os.Stderr))
	} else {
		log.SetOutput(f)
	}
	return f, nil
}
