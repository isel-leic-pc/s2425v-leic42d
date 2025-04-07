import * as net from "net";

function newConnection(socket) {
    console.log('new connection', socket.remoteAddress, socket.remotePort);

    socket.on('end', () => {
        // FIN received. The connection will be closed automatically.
        console.log('EOF.');
    });
    socket.on('data', (data) => {

        console.log('data:', data);

        // close the connection if 'exit'
        if (data.includes('exit')) {
            console.log('closing.');
            socket.write("Bye, client" , (error) => {
                if (error)
                    console.log('error on write', error);
            }); // echo back the data.
            socket.destroy();
        }
        else {
            socket.write(data , (error) => {
                if (error)
                    console.log('error on write', error);
            }); // echo back the data.
        }

    });
}

let server = net.createServer();
server.on('error', (err) => { console.log("error: ", err); });
server.on('connection', newConnection);
server.listen({host: '0.0.0.0', port: 1234});