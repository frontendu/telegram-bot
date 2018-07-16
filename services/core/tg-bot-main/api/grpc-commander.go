package api

import (
	"github.com/frontendu/telegram-bot/services/core/pkg/logger"
	"github.com/frontendu/telegram-bot/services/core/tg-bot-main/registry"
)

type gRPCSCommander struct {
	registry *registry.Registry
	logger   logger.Logger
}

func NewRPCSCommander(registry *registry.Registry, logger logger.Logger) *gRPCSCommander {
	g := &gRPCSCommander{
		registry: registry,
		logger:   logger,
	}

	return g
}

func (g *gRPCSCommander) Command(command string) error {


	return nil
}