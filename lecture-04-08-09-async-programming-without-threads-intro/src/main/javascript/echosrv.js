import * as net from "net";

function newConn(socket) {
    console.log('new connection', socket.remoteAddress, socket.remotePort);

    socket.on('end', () => {
        // FIN received. The connection will be closed automatically.
        console.log('EOF.');
    });
    socket.on('data', (data) => {
        if (data != ""){
            console.log('data:', data);
            socket.write(data); // echo back the data.

            // actively closed the connection if the data contains 'exit'
            if (data.includes('exit')) {
                console.log('closing.');
                socket.destroy();
            }
        }
    });
}

let server = net.createServer();
server.on('error', (err) => { console.log("error: ", err); });
server.on('connection', newConn);
server.listen({host: '0.0.0.0', port: 1234});