package api

import (
	"github.com/frontendu/telegram-bot/services/core/pkg/logger"
	"github.com/frontendu/telegram-bot/services/core/tg-bot-main/registry"
)

type gRPCSCommander struct {
	registry *registry.Registry
	logger   logger.Logger
}

func NewgRPCSCommander(registry *registry.Registry, logger logger.Logger) *gRPCSCommander {
	g := &gRPCSCommander{
		registry: registry,
		logger:   logger,
	}

	return g
}
