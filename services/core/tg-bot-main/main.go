package main

import (
	"gopkg.in/telegram-bot-api.v4"

	log "github.com/frontendu/telegram-bot/services/core/pkg/logger"
	"time"
	"github.com/frontendu/telegram-bot/services/core/tg-bot-main/registry"
	"github.com/frontendu/telegram-bot/services/core/tg-bot-main/api"
)

func main() {
	cfg := ParseFlags()
	props := log.Configure(
		cfg.Log.Format,
		cfg.Log.Level,
	)

	logger := log.GetLogrus(props)
	logger.Info("Starting...")

	r := registry.NewRegistry(logger)
	endpoint := api.NewHttpEndpoint(r, logger, cfg.ListenAddr)
	go func() {
		if err := endpoint.Serve(); err != nil {
			logger.Fatalf("failed to serve http: %s", err.Error())
		}
	}()
	//go runTelegram(cfg.TgBotKey, logger, r)

	time.Sleep(time.Second * 99999999)
}

func runTelegram(key string, logger log.Logger, registry *registry.Registry) {
	bot, err := tgbotapi.NewBotAPI(key)
	if err != nil {
		logger.Panic(err)
	}

	bot.Debug = true

	logger.Printf("Authorized on account %s", bot.Self.UserName)

	u := tgbotapi.NewUpdate(0)
	u.Timeout = 60

	updates, err := bot.GetUpdatesChan(u)

	for update := range updates {
		msg := update.Message
		if msg == nil {
			continue
		}

		if msg.IsCommand() {
			logger.Debugf("[%s] %s", update.Message.From.UserName, update.Message.Text)
			command := msg.Command()
			if err := registry.Process(command, bot, &update); err != nil {
				logger.Warn(err)
			}
		}
	}
}
