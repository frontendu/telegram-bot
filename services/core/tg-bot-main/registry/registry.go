package registry

import (
	"github.com/frontendu/telegram-bot/services/core/pkg/logger"
	"gopkg.in/telegram-bot-api.v4"
	"github.com/sirupsen/logrus"
	"net/url"
	"github.com/pkg/errors"
	"encoding/json"
	"net/http"
	"bytes"
)

type Registry struct {
	Subscribers map[string]*SubscriberMeta
	Bot         *tgbotapi.BotAPI
	logger      logger.Logger
}

type SubscriberMeta struct {
	endpoint       *url.URL
	getAllCommands bool
	commands       []string
}

type Streamer interface {
	Stream(bot *tgbotapi.BotAPI, payload *tgbotapi.Update) error
}

func NewRegistry(logger logger.Logger) *Registry {
	r := &Registry{
		Subscribers: make(map[string]*SubscriberMeta),
		logger:      logger,
	}

	return r
}

// Send payload to the service
func (r *Registry) Process(command string, bot *tgbotapi.BotAPI, payload *tgbotapi.Update) error {
	r.setBot(bot)
	meta, err := r.multiplexer(command)
	if err != nil {
		return err
	}

	botRes := BotResponse{
		Update:    payload,
		IsCommand: true,
	}

	res, err := json.Marshal(botRes)
	if err != nil {
		r.logger.Debugf("Cannot unmarshal payload %v", payload)
		return err
	}

	_, err = http.Post(meta.endpoint.String(), "application/json", bytes.NewReader(res))
	if err != nil {
		return err
	}

	r.logger.Infof("Data sent to the endpoint %s", meta.endpoint)

	return nil
}

func (r *Registry) Stream(bot *tgbotapi.BotAPI, payload *tgbotapi.Update) error {
	r.setBot(bot)
	subsForAllMessages := r.findStreamSubscribers()
	botRes := BotResponse{
		Update:    payload,
		IsCommand: false,
	}
	res, err := json.Marshal(botRes)
	if err != nil {
		r.logger.Debugf("Cannot unmarshal payload %v", payload)
		return err
	}

	for _, sub := range subsForAllMessages {
		go func() {
			_, err := http.Post(sub.endpoint.String(), "application/json", bytes.NewReader(res))
			if err != nil {
				r.logger.Warnln(err)
			}
		}()
	}

	return err
}

func (r *Registry) setBot(bot *tgbotapi.BotAPI) {
	r.Bot = bot
}

func (r *Registry) findStreamSubscribers() []*SubscriberMeta {
	var subs []*SubscriberMeta
	for _, sub := range r.Subscribers {
		if sub.getAllCommands {
			subs = append(subs, sub)
		}
	}

	return subs
}

func (r *Registry) multiplexer(command string) (*SubscriberMeta, error) {
	for _, sub := range r.Subscribers {
		for _, c := range sub.commands {
			if command == c {
				return sub, nil
			}
		}
	}

	return nil, errors.New("command not registered: " + command)
}

func (r *Registry) RegisterCommands(payload RegistrationCommandsRequest) error {
	clientUrl, _ := url.Parse(payload.ListenUrl)
	r.Subscribers[payload.BotName] = &SubscriberMeta{
		endpoint:       clientUrl,
		getAllCommands: payload.GetAllMessages,
		commands:       payload.Commands,
	}

	r.logger.WithFields(logrus.Fields{"payload": payload}).Infof("Bot %s has been registered", payload.BotName)

	return nil
}

func CheckCommand(commands []string, meta map[string]*SubscriberMeta) (string, bool) {
	for _, newCommand := range commands {
		for _, m := range meta {
			for _, command := range m.commands {
				if newCommand == command {
					return command, false
				}
			}
		}
	}

	return "", true
}
