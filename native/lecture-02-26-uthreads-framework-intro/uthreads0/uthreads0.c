#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>

#define STACK_SIZE 4000

typedef void (*thread_func)(void);

typedef struct {
    uint64_t r15;
    uint64_t r14;
    uint64_t r13;
    uint64_t r12;
    uint64_t rbx;
    uint64_t rbp;
    thread_func ret;
} context_t;

typedef struct {
    context_t *context;
    uint8_t stack[STACK_SIZE];
} thread_t;

void context_switch(thread_t *from, thread_t *to);

thread_t t1, t2, t_main;

void func1() {
    printf("start thread 1\n");
    context_switch(&t1, &t2);
    printf("again in thread 1\n");
    exit(1);
}

void func2() {
    printf("start thread 2\n");
    context_switch(&t2, &t1);
}

void thread_init(thread_t *thread, thread_func func) {
    context_t *ctx =  ((context_t*) (thread->stack + STACK_SIZE)) -1;
    ctx->rbp = 0;
    ctx->ret = func;
    // what was missed in the lecture!
    thread->context= ctx;
}

int main() {
    thread_init(&t1, func1);
    thread_init(&t2, func2);
    context_switch(&t_main, &t1);
}