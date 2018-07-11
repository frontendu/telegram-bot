package api

import (
	"io/ioutil"
	"github.com/pkg/errors"
	"os"
	"github.com/frontendu/telegram-bot/services/core/pkg/logger"
	"fmt"
	"google.golang.org/grpc"
)

type gRPCStreamer struct {
	router *Resolver
	logger logger.Logger
}

func NewgRPCStreamer(logger logger.Logger) *gRPCStreamer {
	folders, err := analyseFolders()
	fmt.Println(folders, err)

	g := &gRPCStreamer{
		router: &Resolver{
			table: make(map[string]*grpc.ClientConn),
		},
		logger: logger,
	}

	return g
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
