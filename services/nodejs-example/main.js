const request = require('request');
const express = require('express');
const bodyParser = require('body-parser');
const app = express();

app.use(bodyParser.json()); // support json encoded bodies
app.use(bodyParser.urlencoded({extended: true})); // support encoded bodies

// Register bot
const regData = {
    "listen_url": "http://127.0.0.1:5505/tg",
    "bot_name": "pinger",
    // Commands for listening
    "get_all_messages": true,
    "commands": [
        "ping",
    ]
};

request.post({
    url: 'http://localhost:6661/api/v1/register',
    headers: {
        "Content-Type": "application/json"
    },
    body: regData,
    json: true,
}, (err, res, body) => {
    if (err != null) {
        console.log(err)
    }
    console.log(body)
});
// End register bot

// Replies
function NewMessage(payload) {
    const command = payload.message.text ? payload.message.text : "Привет";
    console.log(payload);
    let message = {
        "chat_id": payload.message.chat.id,
        // "reply_to_message_id": payload.message.message_id,
        "text": command,
    };

    request.post({
        url: "http://localhost:6661/api/v1/commands/sendMessage",
        headers: {
            "Content-Type": "application/json"
        },
        body: message,
        json: true,
    }, (err, res, body) => {
        if (err != null) {
            console.log(err)
        }
        console.log("body " + body)
    })
}

// End replies

app.post('/tg', (req, res) => {
    NewMessage(req.body);
    // Без этого приложение зависнет! WTF
    res.send('done');
});

app.listen(5505);