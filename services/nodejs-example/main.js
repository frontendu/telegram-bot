const request = require('request');
const express = require('express');
const app = express();

// Register bot
const regData = {
    "listen_addr": "localhost:5505",
    "bot_name": "pinger",
    // Commands for listening
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

app.get('/', (req, res) => {
    res.send('hello')
});

app.listen(3000);