# Структура репозитория

Вся логика работы с telegram находится в telegram-bot/services/core/

Каждая фича представляет из себя отдельную папку в services и должна реализовываться отдельным микросервисом, и общаться с core по rest api
Боты-клиенты работают в докере на одной машине

Пример простого бота можно найти в services/nodejs-example

# Описание

Логика построена на обмене json между ботом-клиентом и ядром.
Чтобы начать получать сообщения, бот-клиент должен зарегистрироваться.
Для этого нужно послать post запрос со следующим json:
```
    "listen_url": "http://127.0.0.1:5505/tg",
    "bot_name": "pinger",
    "get_all_messages": true,
    "commands": [
        "ping",
    ]
```

+ listen_url - endpoint, куда будут приходить сообщения.
+ bot_name - имя бота
+ get_all_messages - если нужно получать каждое сообщение. Проверить, пришло сообщение или команда боту
можно по ключу `is_command` в сообщении боту
+ массив commands содержит команды, которые резервирует бот. Сообщение будет отправляться каждый раз,
когда будет получена команда

Для приема команд бот дожен слушать POST запросы по адресу, указанному в `listen_url`

#### Пример сообщения
```
{ is_command: false,
  update_id: 702957316,
    message:
     { message_id: 9065,
       from:
        { id: 177925829,
          first_name: 'Kirill 🎼🎸',
          last_name: '',
          username: 'KirillQ',
          language_code: 'ru-RU',
          is_bot: false },
       date: 1532457012,
       chat:
        { id: -1001080005063,
          type: 'supergroup',
          title: 'Webology Talks',
          username: 'webpulse',
          first_name: '',
          last_name: '',
          all_members_are_administrators: false,
          photo: null },
       forward_from: null,
       forward_from_chat: null,
       forward_from_message_id: 0,
       forward_date: 0,
       reply_to_message: null,
       edit_date: 0,
       text: 'qwe',
       entities: null,
       audio: null,
       document: null,
       game: null,
       photo: null,
       sticker: null,
       video: null,
       video_note: null,
       voice: null,
       caption: '',
       contact: null,
       location: null,
       venue: null,
       new_chat_members: null,
       left_chat_member: null,
       new_chat_title: '',
       new_chat_photo: null,
       delete_chat_photo: false,
       group_chat_created: false,
       supergroup_chat_created: false,
       channel_chat_created: false,
       migrate_to_chat_id: 0,
       migrate_from_chat_id: 0,
       pinned_message: null,
       invoice: null,
       successful_payment: null },
    edited_message: null,
    channel_post: null,
    edited_channel_post: null,
    inline_query: null,
    chosen_inline_result: null,
    callback_query: null,
    shipping_query: null,
    pre_checkout_query: null }}
```

# Endpoint'ы ядра

+ `http://localhost:6661/api/v1/commands/sendMessage` </br>

Принимает json в формате
```
    "chat_id": update.message.chat.id,
    "reply_to_message_id": update.message.message_id,
    "text": command,
```

+ chat_id - id чата, содержится в сообщении
+ reply_to_message_id - если указан, отправляется как ответ на сообщение telegram
+ text - текст сообщения