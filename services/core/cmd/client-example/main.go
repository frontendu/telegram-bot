package main

import (
	"google.golang.org/grpc"
	"github.com/frontendu/telegram-bot/services/core/proto"
	"context"
	"time"
	"fmt"
)

func main() {
	conn, err := grpc.Dial("localhost:6661", grpc.WithInsecure())
	if err != nil {
		panic(err)
	}
	defer func() {
		if e := conn.Close(); e != nil {
			panic(e)
		}
	}()

	c := proto.NewCommandsClient(conn)
	ctx, cancel := context.WithTimeout(context.Background(), time.Second)
	defer cancel()
	r, err := c.Command(ctx, &proto.TgUpdate{
		UpdateID: int32(123),
	})
	if err != nil {
		panic(err)
	}

	fmt.Println(r)
}