package registry

type RegistrationCommandsRequest struct {
	ListenAddr string   `json:"listen_addr"`
	BotName    string   `json:"bot_name"`
	Commands   []string `json:"commands"`
}

type RegistrationAllRequest struct {
	ListenAddr string `json:"listen_addr"`
	BotName    string `json:"bot_name"`
}

type RegistrationResponse struct {
	Message string `json:"message"`
	Status  bool   `json:"status"`
}
