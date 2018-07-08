package api

import "context"

// Each folder in services(except core) is recognized as a service
// Each service has endpoint to communicate with core

const (
	core = "core"
)

type Server interface {
	Serve() error
	Shutdown(ctx context.Context) error
}
