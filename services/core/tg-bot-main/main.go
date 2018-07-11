package main

import (
	"gopkg.in/telegram-bot-api.v4"

	log "github.com/frontendu/telegram-bot/services/core/pkg/logger"
	"os"
	"os/signal"
	"time"
	"github.com/frontendu/telegram-bot/services/core/tg-bot-main/registry"
)

func main() {
	cfg := ParseFlags()
	props := log.Configure(
		cfg.Log.Format,
		cfg.Log.Level,
	)

	logger := log.GetLogrus(props)
	logger.Info("Starting...")

	go runTelegram(cfg.TgBotKey, logger)

	stopHttp := make(chan os.Signal, 1)
	signal.Notify(stopHttp, os.Interrupt)

	//streamer := api.NewgRPCStreamer(logger)
	go registry.NewRegistry(logger, cfg.ListenAddr).Serve()

	time.Sleep(time.Second * 99999999)
}

func runTelegram(key string, logger log.Logger) {
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
			switch command {
			case "ping":
				msg := tgbotapi.NewMessage(update.Message.Chat.ID, "pong")
				msg.ReplyToMessageID = update.Message.MessageID
				bot.Send(msg)
			default:
				msg := tgbotapi.NewMessage(update.Message.Chat.ID, "Неизвестная команда")
				msg.ReplyToMessageID = update.Message.MessageID
				bot.Send(msg)
			}
		}
	}
}
