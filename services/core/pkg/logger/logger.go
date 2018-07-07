package logger

import (
	"strings"
	"github.com/sirupsen/logrus"
)

type Properties struct {
	Format string
	Level  logrus.Level
}

func Configure(format, level string) *Properties {
	l := strings.ToLower(level)
	lParsed, err := logrus.ParseLevel(l)
	if err != nil {
		logrus.Warnf("log: invalid level: %v, continue with info", l)
	}
	lFormat := strings.ToLower(format)

	p := &Properties{
		Format: lFormat,
		Level:  lParsed,
	}

	return p
}

func GetLogrus(p *Properties) *logrus.Logger {
	var logger *logrus.Logger
	if logger == nil {
		logrus.SetLevel(p.Level)
		switch p.Format {
		case "json":
			logrus.SetFormatter(&logrus.JSONFormatter{})
		case "text":
			logrus.SetFormatter(&logrus.TextFormatter{ForceColors: true})
		default:
			logrus.Warnf("log: invalid formatter: %v, continue with default", p.Format)
		}

		logger = logrus.StandardLogger()
	}
	return logger
}
