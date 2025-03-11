
dynamic: simpletest.c event_test.c echo_srv.c echo_cli.c
	gcc -o simpletest -Wall -g -I ../include simpletest.c ../uthread/libuthread.so
	gcc -o event_test -Wall -g -I ../include event_test.c ../uthread/libuthread.so
	
clean:
	rm -rf simpletest event_test  
