package main

import (
	"errors"

	"gopkg.in/telegram-bot-api.v4"

	"github.com/frontendu/telegram-bot/services/core/internal/postgres"
	log "github.com/frontendu/telegram-bot/services/core/pkg/logger"
	"github.com/frontendu/telegram-bot/services/core/tg-bot-main/api"
	"os"
	"os/signal"
	"context"
	"time"
)

func main() {
	cfg := ParseFlags()
	props := log.Configure(
		cfg.Log.Format,
		cfg.Log.Level,
	)

	logger := log.GetLogrus(props)
	logger.Info("Starting...")

	//pgConfig := postgres.Config{
	//	MaxConnLifetime: time.Duration(cfg.DBMaxConnLifetime),
	//	MaxOpenConns:    cfg.DBMaxConnections,
	//	MaxIdleConns:    cfg.DBMaxIdleConns,
	//}
	//
	//db, err := initPostgres(cfg.DBUrl, logger, pgConfig)
	//if err != nil {
	//	panic("Cannot connect to database: " + err.Error())
	//}
	//defer db.Close()
	//runTelegram(logger)

	stopHttp := make(chan os.Signal, 1)
	signal.Notify(stopHttp, os.Interrupt)
	httpManager := api.NewHttpManager(logger, cfg.ListenAddr)
	go func() {
		if err := httpManager.Serve(); err != nil {
			logger.Fatalln("Failed to serve:", err)
		}
	}()

	<-stopHttp
	ctx, _ := context.WithTimeout(context.Background(), time.Second*5)
	httpManager.Shutdown(ctx)
}

func runTelegram(logger log.Logger) {
	bot, err := tgbotapi.NewBotAPI("MyAwesomeBotToken")
	if err != nil {
		logger.Panic(err)
	}

	bot.Debug = true

	logger.Printf("Authorized on account %s", bot.Self.UserName)

	u := tgbotapi.NewUpdate(0)
	u.Timeout = 60

	updates, err := bot.GetUpdatesChan(u)

	for update := range updates {
		if update.Message == nil {
			continue
		}

		logger.Printf("[%s] %s", update.Message.From.UserName, update.Message.Text)

		msg := tgbotapi.NewMessage(update.Message.Chat.ID, update.Message.Text)
		msg.ReplyToMessageID = update.Message.MessageID

		bot.Send(msg)
	}
}

func initPostgres(url string, logger log.Logger, config postgres.Config) (*postgres.DB, error) {
	db, err := postgres.New(url, logger, config)
	if err != nil {
		return nil, errors.New("couldn't open postgres")
	}

	if err := db.Connect(); err != nil {
		return nil, errors.New("couldn't connect to postgres")
	}

	return db, err
}
