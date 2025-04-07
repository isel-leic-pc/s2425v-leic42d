import * as net from "net";

function Connection0(socket) {
    return {
        socket : socket,
        pending : null,
        error : false,
        ended : false
    }
}

function sockWrite(connection, data)  {
    return new Promise( (resolve, reject) => {
        if (connection.error) {
            reject(connection.error);
            return;
        }
        connection.socket.write(data, (error) => {
            if (error) {
                reject(error)
            }
            else {
                resolve()
            }
        })
    })
}

function sockRead(connection) {
    return new Promise((resolve,reject) => {
        connection.reader = { resolve: resolve, reject: reject}
        connection.socket.resume()
    })
}

function processConnection(connection) {
    sockRead(connection)
    .then(data => {
        if (data.length === 0) {
            console.log('end connection');
            connection.socket.destroy()
        } else if (data.includes('exit')) {
            console.log('closing.');
            sockWrite(connection, "Bye, client")
            .then(() => connection.socket.destroy())
        } else {
            console.log('data: ', data.toString());
            sockWrite(connection, data)
            .then(processConnection(connection))
        }
    },
    (exc) => {
        console.error('exception:', exc);
        connection.socket.destroy()
    });
}

function newConnection(socket ) {
    console.log('new connection', socket.remoteAddress, socket.remotePort);

    const connection = Connection(socket);

    processConnection(connection)

}

async function newConnectionAsync(socket ) {
    console.log('new connection', socket.remoteAddress, socket.remotePort);
    try {
        const connection = Connection(socket);
        while (true) {
            const data = await sockRead(connection);
            console.log('data: ', data.toString());
            if (data.length === 0) {
                console.log('end connection');
                break;
            }
            if (data.includes('exit')) {
                console.log('closing.');
                await sockWrite(connection, "Bye, client");
                break
            }
            else {
                await sockWrite(connection, data);
            }
        }
    } catch (exc) {
        console.error('exception:', exc);
    } finally {
        socket.destroy();
    }
}

function Connection(socket) {
    console.log('new connection', socket.remoteAddress, socket.remotePort);

    const connection =  {
        socket : socket,
        pending : null,
        error : false,
        ended : false
    }

    socket.on('end', () => {
        // also fulfills the current read.
        console.log("Process end")
        connection.ended = true;
        if (connection.reader) {
            connection.reader.resolve('');   // EOF
            connection.reader = null;
        }
    });

    socket.on('data', (data) => {
        console.assert(connection.reader)
        connection.socket.pause()
        connection.reader.resolve(data)
        connection.reader = null
    });
    socket.on('error', (error) => {
        // errors are also delivered to the current read.
        connection.error = error;
        if (connection.reader) {
            connection.reader.reject(error);
            connection.reader = null;
        }
    });
    return connection
}


let server = net.createServer({pauseOnConnect : true});
server.on('error', (err) => { console.log("error: ", err); });
server.on('connection', newConnection);
server.listen({host: '0.0.0.0', port: 1234});