package api

import (
	"net/http"
	"fmt"
	"encoding/json"
	"github.com/frontendu/telegram-bot/services/core/pkg/logger"
	"github.com/frontendu/telegram-bot/services/core/tg-bot-main/registry"
	"net"
)

type handlers struct {
	registry *registry.Registry
	logger   logger.Logger
}

func (h *handlers) indexHandler(res http.ResponseWriter, _ *http.Request) {
	message := `Frontend Youth ultimate, a little bit configurable bot
Developed by community
	
`
	fmt.Fprint(res, message)
}

func (h *handlers) registerCommandsHandler(res http.ResponseWriter, req *http.Request) {
	var regReq registry.RegistrationCommandsRequest
	var resReq registry.RegistrationResponse
	var httpError string

	if err := json.NewDecoder(req.Body).Decode(&regReq); err != nil {
		httpError = "cannot decode json"
		h.logger.WithError(err).Warnln(httpError)
		resReq = registry.RegistrationResponse{
			Message: "cannot decode json",
			Status:  false,
		}
		writeError(res, resReq)
		return
	}

	//httpError = fmt.Sprintf("command %s is already registered", command)
	//h.logger.Warnln(httpError)
	//resReq := registry.RegistrationResponse{
	//	Message: httpError,
	//	Status:  false,
	//}
	//writeError(res, resReq)

	if err := validateIp(regReq.ListenAddr); err != nil {
		httpError = "incorrect ip address"
		h.logger.WithError(err).Warnln(httpError)
		resReq = registry.RegistrationResponse{
			Message: "incorrect ip address",
			Status:  false,
		}
		writeError(res, resReq)
		return
	}

	if err := h.registry.RegisterCommands(regReq); err != nil {
		h.logger.Warnln(err)
		resReq = registry.RegistrationResponse{
			Message: "bad command format",
			Status:  false,
		}
		writeError(res, resReq)
		return
	}

	fmt.Println()
}

func writeError(res http.ResponseWriter, resReq registry.RegistrationResponse) {
	b, _ := json.Marshal(resReq)
	res.Header().Set("Content-type", "application/json")
	res.WriteHeader(http.StatusBadRequest)
	res.Write(b)
}

func validateIp(addr string) (error) {
	_, err := net.ResolveTCPAddr("tcp", addr)
	if err != nil {
		return err
	}

	return nil
}
