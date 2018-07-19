package registry

import (
	"github.com/frontendu/telegram-bot/services/core/pkg/logger"
	"net"
	"gopkg.in/telegram-bot-api.v4"
	"github.com/sirupsen/logrus"
)

type Registry struct {
	Subscribers map[string]*SubscriberMeta
	logger      logger.Logger
}

type SubscriberMeta struct {
	addr     *net.TCPAddr
	commands []string
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


	return nil
}

func (r *Registry) RegisterCommands(payload RegistrationCommandsRequest) error {
	tcpAddr, _ := net.ResolveTCPAddr("tcp", payload.ListenAddr)
	r.Subscribers[payload.BotName] = &SubscriberMeta{
		addr:     tcpAddr,
		commands: payload.Commands,
	}

	r.logger.WithFields(logrus.Fields{"payload": payload}).Infof("Bot %s has been registered", payload.BotName)

	return nil
}

func (r *Registry) RegisterAllMessages(meta RegistrationAllRequest) {

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
