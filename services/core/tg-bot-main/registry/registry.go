package registry

import (
	"github.com/frontendu/telegram-bot/services/core/pkg/logger"
	"net"
	"gopkg.in/telegram-bot-api.v4"
	"regexp"
	"github.com/pkg/errors"
)

type Registry struct {
	Subscribers map[string]*SubscriberMeta
	logger      logger.Logger
}

type SubscriberMeta struct {
	addr     *net.TCPAddr
	commands []string
}

func CheckCommand(commands []string, meta *SubscriberMeta) (string, bool) {
	for _, newCommand := range commands {
		for _, command := range meta.commands {
			if newCommand == command {
				return command, false
			}
		}
	}

	return "", true
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
	regExpCommand := regexp.MustCompile("^[a-zA-Z0-9_.-]*$")
	for _, command := range payload.Commands {
		if !regExpCommand.MatchString(command) {
			return errors.New("bad command format: " + command)
		}
	}

	if _, ok := r.Subscribers[payload.BotName]; !ok {
		tcpAddr, err := net.ResolveTCPAddr("tcp", payload.ListenAddr)
		if err != nil {
			return errors.Wrap(err, "cannot parse tcp addr: "+payload.ListenAddr)
		}
		r.logger.Infof("===Start registration bot %s===", payload.BotName)
		r.Subscribers[payload.BotName] = &SubscriberMeta{
			addr:     tcpAddr,
			commands: payload.Commands,
		}
		r.logger.Infof("===Finish registration bot %s===", payload.BotName)
	} else {
		// already registered
	}

	return nil
}

func (r *Registry) RegisterAllMessages(meta RegistrationAllRequest) {

}
