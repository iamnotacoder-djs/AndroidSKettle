#!/usr/bin/env node
var WebSocketServer = require('websocket').server;
var http = require('http');

var kettle = {status: 1, temp: 0, water: 0},
    clients = new Array;
 
var server = http.createServer(function(request, response) {
    console.log((new Date()) + ' Received request for ' + request.url);
    response.writeHead(404);
    response.end();
});
server.listen(3001, function() {
    console.log((new Date()) + ' Server is listening on port 3001');
});
 
wsServer = new WebSocketServer({
    httpServer: server,
    autoAcceptConnections: false
});
 
function originIsAllowed(origin) {
  // put logic here to detect whether the specified origin is allowed. 
  return true;
}
 
wsServer.on('request', function(request) {
    if (!originIsAllowed(request.origin)) {
      // Make sure we only accept requests from an allowed origin 
      request.reject();
      console.log((new Date()) + ' Connection from origin ' + request.origin + ' rejected.');
      return;
    }
    
    var connection = request.accept(request.origin);
    console.log((new Date()) + ' Connection accepted.');
    connection.on('message', function(message) {
        if (message.type === 'utf8') {
            console.log('Received Message: ' + message.utf8Data);
            var msg = JSON.parse(message.utf8Data);
            switch(msg.type){
                case "clientTypeInfo":
                    connection.clientType = msg.value;
                    if(msg.value == "app"){
                        connection.sendUTF(JSON.stringify({type: "status", value: kettle.status}));
                        connection.sendUTF(JSON.stringify({type: "info", temp: kettle.temp, water: kettle.water }));
                        clients.push(connection);
                        connection.infoSender = setInterval(function(){connection.sendUTF(JSON.stringify({type: "info", temp: kettle.temp, water: kettle.water }));},4000);
                        
                    }
                    if(msg.value == "kettle"){
                        connection.temp = 0;
                        connection.water = 0;
                        connection.status = 1;
                        kettle = connection;
                    }
                    break;
                case "info":
                    kettle.temp = msg.temp;
                    kettle.water = msg.water;
                    break;
                case "power":
                    kettle.sendUTF(JSON.stringify(msg));
                    break;
                case "status":
                    kettle.status = msg.value;
                    clients.forEach(function(client,index,arr){
                       client.sendUTF(JSON.stringify(msg)); 
                    });
                    
                            
            }
        }
    });
    connection.on('close', function(reasonCode, description) {
        console.log((new Date()) + ' Peer ' + connection.remoteAddress + ' disconnected.');
    });
});