let PROTO_REG_PATH = '../core/proto/registry.proto';
let PROTO_COMMANDS_PATH = '../core/proto/tg.proto';
let grpc = require('grpc');
let registryChema = grpc.load(PROTO_REG_PATH).proto;
let commandsChema = grpc.load(PROTO_COMMANDS_PATH).proto;

function register() {
    // Bot registration
    let client = new registryChema.Registry('localhost:6661',
        grpc.credentials.createInsecure());
    // Register is a name of the command
    client.Register({botName: "pinger", listenAddr: '0.0.0.0:55051', command: "ping"}, (err, response) => {
        if (err != null) {
            console.log('Error! ' + err.details);
        } else {
            console.log('Got response:', response.message);
        }
    });
}

// unknown service
function startListenCommand() {
    let addr = "0.0.0.0:55051";
    console.log("starting server at " + addr);
    let server = new grpc.Server();
    server.addService(commandsChema.Commands.service, {command: command});
    server.bind(addr, grpc.ServerCredentials.createInsecure());
    server.start();
}

// Commands
function command(call, callback) {
    callback(null, {
        // message: 'Pong! From JS bot service 🤓'
        status: true,
    })
}

register();
startListenCommand();
