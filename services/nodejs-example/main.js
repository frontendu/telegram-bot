let grpc = require('grpc');

let PROTO_REG_PATH = '../core/proto/registry.proto';
let PROTO_COMMANDS_PATH = '../core/proto/tg.proto';

let registryChema = grpc.load(PROTO_REG_PATH).proto;
let commandsChema = grpc.load(PROTO_COMMANDS_PATH).proto;

// Bot registration
let client = new registryChema.Registry('localhost:6661', grpc.credentials.createInsecure());

function register() {
    // Register is a name of the command
    client.Register({botName: "pinger", listenAddr: '0.0.0.0:55051', command: "ping"}, (err, response) => {
        if (err != null) {
            console.log('Error! ' + err.details);
        } else {
            console.log('Got response:', response.message);
        }
    });
}

function startListenCommand() {
    let addr = "0.0.0.0:55051";
    console.log("starting server at " + addr);
    let server = new grpc.Server();
    server.addService(commandsChema.Commands.service, {command: commandResponse});
    server.bind(addr, grpc.ServerCredentials.createInsecure());
    server.start();
}


// Commands
function commandResponse(call, callback) {
    let allOk = true;
    console.log("Got command from " + call.request.Message.From.FirstName);
    if (allOk) {
        callback(null, {
            status: true,
        })
    } else {
        callback(null, {
            status: false,
        })
    }
}

register();
startListenCommand();
