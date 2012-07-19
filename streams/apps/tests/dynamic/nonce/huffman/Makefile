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
OBJS = main.o barrier.o dynamic_queue.o core0.o core8.o 

all: $(OBJS)
	$(CC) $(CFLAGS) $(LIBS) $(OBJS) -o smp2

clean:
	rm *.o smp2


realclean:
	rm -rf *.o dotfiles *.c *.h *.java *.sir smp2

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

