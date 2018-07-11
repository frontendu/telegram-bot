package commander

import "github.com/frontendu/telegram-bot/services/core/pkg/logger"

type Router struct {
	Subscribers map[string]Subscriber
	Logger      logger.Logger
}

type Commander interface {
	Add(command string, sub Subscriber) error
}

func NewCommander() *Router {
	return &Router{
		Subscribers: make(map[string]Subscriber),
	}
}

func (c *Router) Add(command string, sub Subscriber) error {
	if _, isPresent := c.Subscribers[command]; !isPresent {
		c.Subscribers[command] = sub
		c.Logger.Infof("Registered %s command for %s subscriber", sub.Commands, sub.Name)
		return nil
	}
}

type Subscriber struct {
	Name           string
	GetAllMessages bool
	GetCommands    bool
	Commands       []string
}
