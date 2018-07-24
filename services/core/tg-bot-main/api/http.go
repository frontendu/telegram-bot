package api

import (
	"net/http"
	"context"
	"github.com/frontendu/telegram-bot/services/core/pkg/logger"
	"path"
	"fmt"
	"strings"
	"github.com/frontendu/telegram-bot/services/core/tg-bot-main/registry"
)

type HttpEndpoint struct {
	endpoint http.Server
	logger   logger.Logger
}

func NewHttpEndpoint(registry *registry.Registry, logger logger.Logger, listenAddr string) Server {
	handler := newPathResolver(logger)
	registerRoutes(handler, registry)
	endpoint := &HttpEndpoint{
		endpoint: http.Server{
			Addr:    listenAddr,
			Handler: handler,
		},
		logger: logger,
	}

	return endpoint
}

func (h *HttpEndpoint) Serve() error {
	h.logger.Infof("Http served at %s", h.endpoint.Addr)
	return h.endpoint.ListenAndServe()
}

func (h *HttpEndpoint) Shutdown(ctx context.Context) error {
	h.logger.Infoln("Shutting down the server...")
	defer h.logger.Infoln("Server gracefully stopped")
	h.endpoint.SetKeepAlivesEnabled(false)
	err := h.endpoint.Shutdown(ctx)
	if err != nil {
		return err
	}

	return err
}

func registerRoutes(resolver *pathResolver, registry *registry.Registry) {
	h := handlers{
		registry: registry,
		logger:   resolver.logger,
	}
	resolver.AddIndex(h.indexHandler)
	resolver.Add("POST /register", h.registerCommandsHandler)
	resolver.Add("POST /commands/sendMessage", h.handleTgMessage)
}

type pathResolver struct {
	handlers map[string]http.HandlerFunc
	logger   logger.Logger
}

func newPathResolver(logger logger.Logger) *pathResolver {
	p := &pathResolver{
		handlers: make(map[string]http.HandlerFunc),
		logger:   logger,
	}

	return p
}

func (p *pathResolver) Add(path string, handler http.HandlerFunc) {
	const basePath = "/api/v1"
	s := strings.Split(path, " ")
	s = append(s, "")
	copy(s[2:], s[1:])
	s[1] = basePath
	url := s[0] + " " + strings.Join(s[1:], "")
	p.handlers[url] = handler
}

func (p *pathResolver) AddIndex(handler http.HandlerFunc) {
	p.handlers["GET /"] = handler
}

func (p *pathResolver) ServeHTTP(res http.ResponseWriter, req *http.Request) {
	check := req.Method + " " + req.URL.Path
	for pattern, handlerFunc := range p.handlers {
		if ok, err := path.Match(pattern, check); ok && err == nil {
			handlerFunc(res, req)
			return
		} else if err != nil {
			fmt.Fprint(res, err)
		}
	}

	http.NotFound(res, req)
}
