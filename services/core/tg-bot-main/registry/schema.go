package registry

import "gopkg.in/telegram-bot-api.v4"

type RegistrationCommandsRequest struct {
	ListenUrl      string   `json:"listen_url"`
	BotName        string   `json:"bot_name"`
	GetAllMessages bool     `json:"get_all_messages"`
	Commands       []string `json:"commands"`
}

type RegistrationAllRequest struct {
	ListenAddr string `json:"listen_addr"`
	BotName    string `json:"bot_name"`
}

type RegistrationResponse struct {
	Message string `json:"message"`
	Status  bool   `json:"status"`
}

type BotResponse struct {
	IsCommand bool             `json:"is_command"`
	Update    *tgbotapi.Update `json:"update"`
}
