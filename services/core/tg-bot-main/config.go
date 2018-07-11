package main

import (
	"flag"
	"github.com/agalitsyn/flagenv"
)

type СliFlags struct {
	ListenAddr string

	Log struct {
		Format string
		Level  string
	}

	Logfile  string
	TgBotKey string

	DBUrl             string
	DBMaxConnLifetime int
	DBMaxConnections  int
	DBMaxIdleConns    int

	HttpApiVersion int
}

func ParseFlags() СliFlags {
	var cfg СliFlags

	flag.StringVar(&cfg.Log.Level, "log-level", "info", "Log level debug|info|warning|error|fatal|panic.")
	flag.StringVar(&cfg.Log.Format, "log-format", "text", "Log format text|json.")

	flag.StringVar(&cfg.ListenAddr, "listen-address", ":6661", "The address to listen on for HTTP requests.")

	flag.StringVar(&cfg.TgBotKey, "tg-key", "", "Telegram bot api key")
	flag.StringVar(&cfg.Logfile, "logfile", "tg-bot-main.log", "Logfile name")

	flag.StringVar(&cfg.DBUrl, "dbUrl", "", "db connection string")
	flag.IntVar(&cfg.DBMaxConnLifetime, "dbMaxConnLifeTime", 60, "db max connection life time")
	flag.IntVar(&cfg.DBMaxConnections, "dbMaxConnections", 2, "db max connections")
	flag.IntVar(&cfg.DBMaxIdleConns, "dbMaxIdleConns", 1, "db max idle connections")

	flag.IntVar(&cfg.HttpApiVersion, "httpApiVersion", 1, "version of http api")

	flagenv.Prefix = "frontendu_"
	flagenv.Parse()
	flag.Parse()

	return cfg
}
