package api

import "context"

type Server interface {
	Serve() error
	Shutdown(ctx context.Context) error
}
