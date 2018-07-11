package registry

import (
	"github.com/frontendu/telegram-bot/services/core/pkg/logger"
	"context"
	"github.com/frontendu/telegram-bot/services/core/proto"
	"net"
	"google.golang.org/grpc"
	"google.golang.org/grpc/reflection"

	"log"
	"github.com/pkg/errors"
)

type Registry struct {
	subscribers map[string]*grpc.ClientConn
	logger      logger.Logger
	listenAddr  string
}

func NewRegistry(logger logger.Logger, listenAddr string) *Registry {
	r := &Registry{
		subscribers: make(map[string]*grpc.ClientConn),
		logger:      logger,
		listenAddr:  listenAddr,
	}

	return r
}

func (r *Registry) Add(command string) error {
	if _, ok := r.subscribers[command]; !ok {
		r.subscribers[command] = nil
	} else {
		return errors.New("command " + command + " is already taken")
	}

	return nil
}

func (r *Registry) Serve() {
	listener, err := net.Listen("tcp", r.listenAddr)
	if err != nil {
		r.logger.Fatalf("failed to listen: %v", err)
	}
	s := grpc.NewServer()
	proto.RegisterRegistryServer(s, &registry{
		Registry: r,
	})
	reflection.Register(s)
	r.logger.Infoln("Serving registry...")
	if err := s.Serve(listener); err != nil {
		log.Fatalf("failed to serve: %v", err)
	}
}

type registry struct {
	*Registry
}

func (r *registry) Register(ctx context.Context, in *proto.RegisterRequest) (*proto.RegisterResponse, error) {
	var res *proto.RegisterResponse
	//@TODO(Kirill) Check command
	if _, ok := r.subscribers[in.Command]; !ok {
		res = &proto.RegisterResponse{
			Message: "Command registered",
			Status:  true,
		}
	}

	//st := status.New(codes.Aborted, "Command is already taken")
	//return nil, st.Err()

	return res, nil
}
