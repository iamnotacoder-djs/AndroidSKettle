var mraa = require('mraa');
var sensors = require('./sensors');
var relay = new mraa.Gpio(4);
relay.dir(mraa.DIR_OUT);

var WebSocketClient = require('websocket').client;

var client = new WebSocketClient();

var boiling = null;

client.on('connectFailed', function(error) {
    console.log('Connect Error: ' + error.toString());
});
 
client.on('connect', function(connection) {
    console.log('WebSocket Client Connected');
     connection.sendUTF(JSON.stringify({type: "clientTypeInfo", value: "kettle"}));
     setInterval(function(){connection.sendUTF(JSON.stringify({type: "info", temp: sensors.temp(), water: sensors.water() }));},5000);
    connection.on('error', function(error) {
        console.log("Connection Error: " + error.toString());
    });
    connection.on('close', function() {
        console.log('echo-protocol Connection Closed');
    });
    connection.on('message', function(message) {
        if (message.type === 'utf8') {
            console.log("Received: '" + message.utf8Data + "'");
            var msg = JSON.parse(message.utf8Data);
            console.log(msg.type);
            switch(msg.type){
                case "power":
                    switch(msg.value){
                        case "0":
                            relay.write(0);
                            clearInterval(boiling);
                            connection.sendUTF(JSON.stringify({type: "status", value: "1"}));
                            break;
                        case "1":
                            relay.write(1);
                            
                            connection.sendUTF(JSON.stringify({type: "status", value: "2"}));
                            boiling = setInterval(function(){
                                console.log("Heating..");
                                if(msg.temp <= sensors.temp()){
                                    relay.write(0);
                                    clearInterval(boiling);
                                }
                            },1000);
                            break;
                    }
                    break;
                    
            }
        }
    });
});
 
client.connect('ws://10.1.79.50:3001/');