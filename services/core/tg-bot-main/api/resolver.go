package api

import (
	"google.golang.org/grpc"
	"github.com/pkg/errors"
)

type Resolver struct {
	table map[string]*grpc.ClientConn
}

func (r *Resolver) Add(command string) error {
	if _, ok := r.table[command]; !ok {
		r.table[command] = nil
 	} else {
 		return errors.New("command " + command + " is already taken")
	}

	return nil
}
