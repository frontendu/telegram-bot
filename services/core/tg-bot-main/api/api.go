package api

// Each folder in services(except core) is recognized as a service
// Each service has gRPC server to communicate with core

const (
	core = "core"
)

type Commander interface {
	Command(command string) error
}
