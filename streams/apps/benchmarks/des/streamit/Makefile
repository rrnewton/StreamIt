UNAME := $(shell uname)
CC = g++
CFLAGS = -O2 -vec-report0
INCLUDES = 
LIBS = -pthread -lstdc++
OBJS = main.o barrier.o dynamic_queue.o core0.o core8.o 

all: $(OBJS)
	$(CC) $(CFLAGS) $(LIBS) $(OBJS) -o smp2

clean:
	rm *.o smp2


realclean:
	rm *.o *.dot *.c *.h *.java smp2

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

