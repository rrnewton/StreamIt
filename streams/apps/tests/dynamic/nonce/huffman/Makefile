UNAME := $(shell uname)
CC = g++
CFLAGS = -O2 -lrt
ifeq ($(UNAME), Darwin)
CFLAGS = -O2 -vec-report0
endif
INCLUDES = 
LIBS = -pthread -lstdc++ -lrt
ifeq ($(UNAME), Darwin)
# do something for OSX
LIBS = -pthread -lstdc++
endif
OBJS = main.o barrier.o dynamic_queue.o core0.o core8.o core1.o core9.o core2.o core10.o core3.o core11.o 

all: $(OBJS)
	$(CC) $(CFLAGS) $(LIBS) $(OBJS) -o smp8

clean:
	rm *.o smp8


realclean:
	rm -rf *.o dotfiles *.c *.h *.java *.sir smp8

main.o: main.c
	$(CC) $(CFLAGS) $(INCLUDES) -c main.c

barrier.o: barrier.c
	$(CC) $(CFLAGS) $(INCLUDES) -c barrier.c

dynamic_queue.o: dynamic_queue.c
	$(CC) $(CFLAGS) $(INCLUDES) -c dynamic_queue.c

core0.o: core0.c
	$(CC) $(CFLAGS) $(INCLUDES) -c core0.c

core8.o: core8.c
	$(CC) $(CFLAGS) $(INCLUDES) -c core8.c

core1.o: core1.c
	$(CC) $(CFLAGS) $(INCLUDES) -c core1.c

core9.o: core9.c
	$(CC) $(CFLAGS) $(INCLUDES) -c core9.c

core2.o: core2.c
	$(CC) $(CFLAGS) $(INCLUDES) -c core2.c

core10.o: core10.c
	$(CC) $(CFLAGS) $(INCLUDES) -c core10.c

core3.o: core3.c
	$(CC) $(CFLAGS) $(INCLUDES) -c core3.c

core11.o: core11.c
	$(CC) $(CFLAGS) $(INCLUDES) -c core11.c

