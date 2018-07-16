package registry

import (
	"github.com/frontendu/telegram-bot/services/core/pkg/logger"
	"context"
	"github.com/frontendu/telegram-bot/services/core/proto"
	"net"
	"google.golang.org/grpc"
	"google.golang.org/grpc/reflection"

	epb "google.golang.org/genproto/googleapis/rpc/errdetails"

	"log"
	"google.golang.org/grpc/status"
	"google.golang.org/grpc/codes"
	"regexp"
	"errors"
	"fmt"
	"gopkg.in/telegram-bot-api.v4"
	"time"
)

type Registry struct {
	subscribers map[string]*subscriberMeta
	logger      logger.Logger
	listenAddr  string
}

type subscriberMeta struct {
	conn    *grpc.ClientConn
	addr    *net.TCPAddr
	name    string
	command string
}

func NewRegistry(logger logger.Logger, listenAddr string) *Registry {
	r := &Registry{
		subscribers: make(map[string]*subscriberMeta),
		logger:      logger,
		listenAddr:  listenAddr,
	}

	return r
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
	proto.RegisterCommandsServer(s, &commands{})
	reflection.Register(s)
	r.logger.Infoln("Serving registry...")
	if err := s.Serve(listener); err != nil {
		log.Fatalf("failed to serve: %v", err)
	}
}

func (r *Registry) Process(command string, payload *tgbotapi.Update) error {
	if _, ok := r.subscribers[command]; ok {
		// Send payload to server
		// пинговать сервис, если не отвечает освобождать команду
		conn, err := grpc.Dial(r.listenAddr, grpc.WithInsecure())
		if err != nil {
			return errors.New("didn't connect: " + err.Error())
		}
		defer func() {
			if e := conn.Close(); e != nil {
				r.logger.Warnf("failed to close connection: %s", e)
			}
		}()

		c := proto.NewCommandsClient(conn)
		ctx, cancelFunc := context.WithTimeout(context.Background(), time.Second)
		defer cancelFunc()
		res, err := c.Command(ctx, &proto.TgUpdate{
			UpdateID: int32(payload.UpdateID),
		})
		if err != nil {
			return err
		}

		fmt.Println(res)

		return nil
	}

	return errors.New("unregistered command: " + command)
}

type registry struct {
	*Registry
}

type commands struct {

}

func (c *commands) Command(ctx context.Context, in *proto.TgUpdate) (*proto.Response, error) {
	return &proto.Response{Status: true}, nil
}

func (r *registry) Register(ctx context.Context, in *proto.RegisterRequest) (*proto.RegisterResponse, error) {
	var res *proto.RegisterResponse

	regExpCommand := regexp.MustCompile("^[a-zA-Z0-9_.-]*$")
	if !regExpCommand.MatchString(in.Command) {
		return nil, errors.New("invalid command: " + in.Command)
	}

	clientServerAddr, err := validateIp(in.ListenAddr)
	if err != nil {
		return nil, err
	}

	if _, ok := r.subscribers[in.Command]; !ok {
		r.subscribers[in.Command] = &subscriberMeta{
			addr:    clientServerAddr,
			name:    in.BotName,
			command: in.Command,
		}
		res = &proto.RegisterResponse{
			Message: "Command registered",
			Status:  true,
		}
	} else {
		return nil, status.New(codes.Aborted, "Command is already taken").Err()
	}

	r.logger.Infoln(fmt.Sprintf("Command %s registred by %s bot", in.Command, in.BotName))
	return res, nil
}

// Resolve tcp addr and send error, if something happened
func validateIp(addr string) (*net.TCPAddr, error) {
	tcpAddr, err := net.ResolveTCPAddr("tcp", addr)
	if err != nil {
		st := status.New(codes.Aborted, "cannot parse server address")
		ds, err := st.WithDetails(
			&epb.QuotaFailure{
				Violations: []*epb.QuotaFailure_Violation{{
					Subject:     addr,
					Description: "Bad ip address",
				}},
			},
		)
		if err != nil {
			return nil, st.Err()
		}
		return nil, ds.Err()
	}

	return tcpAddr, nil
}
