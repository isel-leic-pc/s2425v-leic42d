
#include <sys/types.h>
#include <sys/epoll.h>
#include <stdbool.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include "uthread.h"

// NO TIMEOUT
#define	 INFINITE -1

// ZERO TIMEOUT
#define  POLLING   0

// MAXIMUM READY HANDLES
#define EPOOL_MAX_FDS 2048


// async handles


#define BACKLOG 5

struct async_handle_impl;

typedef struct async_handle_impl *async_handle_t;
 
struct  async_handle_impl {
	int fd; // associated file descriptor
	uthread_t *thread; // associated thread
};


// async operations

bool aio_in_use();

int aio_init();

uthread_t * aio_get_thread(async_handle_t ah);

void aio_set_thread(async_handle_t ah);

int aio_getfd(async_handle_t ah); 

async_handle_t aio_server_socket(int port);

async_handle_t  aio_accept(async_handle_t serv_sock, struct sockaddr * cliaddr, socklen_t *clilen);

int aio_read(async_handle_t ah, void *buf, int size);

int aio_write(async_handle_t ah, void *buf, int size);

typedef enum aio_wait_state { AIO_SYNCH_OPER, AIO_ASYNC_WAIT, AIO_INACTIVE, AIO_CLOSED } aio_wait_state_t;

aio_wait_state_t aio_wait(int timeout);

void aio_close(async_handle_t ah);
