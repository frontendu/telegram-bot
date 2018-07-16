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

	r := registry.NewRegistry(logger, cfg.ListenAddr)
	go r.Serve()
	c := api.NewRPCSCommander(r, logger)
	go runTelegram(cfg.TgBotKey, logger, c, r)

	time.Sleep(time.Second * 99999999)
}

func runTelegram(key string, logger log.Logger, commander api.Commander, registry *registry.Registry) {
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
			if err := registry.Process(command, &update); err != nil {
				logger.Warn(err)
			}
			//switch command {
			//case "ping":
			//	msg := tgbotapi.NewMessage(update.Message.Chat.ID, "pong")
			//	msg.ReplyToMessageID = update.Message.MessageID
			//	bot.Send(msg)
			//default:
			//	msg := tgbotapi.NewMessage(update.Message.Chat.ID, "Неизвестная команда")
			//	msg.ReplyToMessageID = update.Message.MessageID
			//	bot.Send(msg)
			//}
		}
	}
}
