package api

import (
	"net/http"
	"io/ioutil"
	"github.com/pkg/errors"
	"os"
	"fmt"
	"github.com/frontendu/telegram-bot/services/core/pkg/logger"
	"context"
)

type HttpServer struct {
	endpoint *http.Server
	logger   logger.Logger
}

func NewHttpManager(logger logger.Logger, listenAddr string) *HttpServer {
	folders, err := analyseFolders()
	if err != nil {
		panic(err)
	}

	pr := newPathResolver()
	pr.Add("GET /", func(res http.ResponseWriter, req *http.Request) {
		fmt.Fprintf(res, "Frontend Youth ultimate modular-structured bot")
	})
	for _, f := range folders {
		pr.Add("GET /api/v1/services/"+f.Name(), stubHttpHandler)
	}

	manager := &HttpServer{
		endpoint: &http.Server{
			Addr:    listenAddr,
			Handler: pr,
		},
		logger: logger,
	}

	return manager
}

func (h *HttpServer) Serve() error {
	h.logger.Infof("Listening %s...", h.endpoint.Addr)
	for p := range h.endpoint.Handler.(*pathResolver).handlers {
		h.logger.Debugln(p)
	}

	return h.endpoint.ListenAndServe()
}

func (h *HttpServer) Shutdown(ctx context.Context) error {
	h.logger.Infoln("Shutting down the server...")
	h.endpoint.SetKeepAlivesEnabled(false)
	err := h.endpoint.Shutdown(ctx)
	if err != nil {
		return err
	}
	h.logger.Infoln("Server gracefully stopped")
	return err
}

func stubHttpHandler(res http.ResponseWriter, req *http.Request) {
	fmt.Fprint(res, "stub")
}

func analyseFolders() (servicesFolder []os.FileInfo, err error) {
	files, err := ioutil.ReadDir("./../../")
	if err != nil {
		return nil, errors.Wrap(err, "failed to get services folder list")
	}

	for _, f := range files {
		if (f.Name() == core && f.IsDir()) || !f.IsDir() {
			continue
		}

		servicesFolder = append(servicesFolder, f)
	}

	return servicesFolder, err
}
