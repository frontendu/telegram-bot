package registry

import (
	"github.com/frontendu/telegram-bot/services/core/pkg/logger"
	"context"
	"github.com/frontendu/telegram-bot/services/core/proto"
	"net"
	"google.golang.org/grpc"
	"google.golang.org/grpc/reflection"

	"log"
)

type Registry struct {
	logger     logger.Logger
	listenAddr string
}

func NewRegistry(logger logger.Logger, listenAddr string) *Registry {
	r := &Registry{
		logger:     logger,
		listenAddr: listenAddr,
	}

	return r
}

func (r *Registry) Serve() {
	listener, err := net.Listen("tcp", r.listenAddr)
	if err != nil {
		r.logger.Fatalf("failed to listen: %v", err)
	}
	s := grpc.NewServer()
	proto.RegisterRegistryServer(s, &registry{})
	reflection.Register(s)
	r.logger.Infoln("Serving registry...")
	if err := s.Serve(listener); err != nil {
		log.Fatalf("failed to serve: %v", err)
	}
}

type registry struct{}

func (r *registry) Register(ctx context.Context, in *proto.RegisterRequest) (*proto.RegisterResponse, error) {
	return &proto.RegisterResponse{Message: "hello from core", Status: true}, nil
}
