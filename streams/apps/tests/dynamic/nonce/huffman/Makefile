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
OBJS = main.o barrier.o dynamic_queue.o core0.o core16.o core1.o core17.o core2.o core18.o core3.o core19.o core4.o core20.o core5.o core21.o core6.o core22.o core7.o core23.o core8.o core24.o core9.o core25.o core10.o core26.o core11.o core27.o core12.o core28.o core13.o core29.o core14.o core30.o core15.o core31.o 

all: $(OBJS)
	$(CC) $(CFLAGS) $(LIBS) $(OBJS) -o smp32

clean:
	rm *.o smp32


realclean:
	rm -rf *.o dotfiles *.c *.h *.java *.sir smp32

main.o: main.c
	$(CC) $(CFLAGS) $(INCLUDES) -c main.c

barrier.o: barrier.c
	$(CC) $(CFLAGS) $(INCLUDES) -c barrier.c

dynamic_queue.o: dynamic_queue.c
	$(CC) $(CFLAGS) $(INCLUDES) -c dynamic_queue.c

core0.o: core0.c
	$(CC) $(CFLAGS) $(INCLUDES) -c core0.c

core16.o: core16.c
	$(CC) $(CFLAGS) $(INCLUDES) -c core16.c

core1.o: core1.c
	$(CC) $(CFLAGS) $(INCLUDES) -c core1.c

core17.o: core17.c
	$(CC) $(CFLAGS) $(INCLUDES) -c core17.c

core2.o: core2.c
	$(CC) $(CFLAGS) $(INCLUDES) -c core2.c

core18.o: core18.c
	$(CC) $(CFLAGS) $(INCLUDES) -c core18.c

core3.o: core3.c
	$(CC) $(CFLAGS) $(INCLUDES) -c core3.c

core19.o: core19.c
	$(CC) $(CFLAGS) $(INCLUDES) -c core19.c

core4.o: core4.c
	$(CC) $(CFLAGS) $(INCLUDES) -c core4.c

core20.o: core20.c
	$(CC) $(CFLAGS) $(INCLUDES) -c core20.c

core5.o: core5.c
	$(CC) $(CFLAGS) $(INCLUDES) -c core5.c

core21.o: core21.c
	$(CC) $(CFLAGS) $(INCLUDES) -c core21.c

core6.o: core6.c
	$(CC) $(CFLAGS) $(INCLUDES) -c core6.c

core22.o: core22.c
	$(CC) $(CFLAGS) $(INCLUDES) -c core22.c

core7.o: core7.c
	$(CC) $(CFLAGS) $(INCLUDES) -c core7.c

core23.o: core23.c
	$(CC) $(CFLAGS) $(INCLUDES) -c core23.c

core8.o: core8.c
	$(CC) $(CFLAGS) $(INCLUDES) -c core8.c

core24.o: core24.c
	$(CC) $(CFLAGS) $(INCLUDES) -c core24.c

core9.o: core9.c
	$(CC) $(CFLAGS) $(INCLUDES) -c core9.c

core25.o: core25.c
	$(CC) $(CFLAGS) $(INCLUDES) -c core25.c

core10.o: core10.c
	$(CC) $(CFLAGS) $(INCLUDES) -c core10.c

core26.o: core26.c
	$(CC) $(CFLAGS) $(INCLUDES) -c core26.c

core11.o: core11.c
	$(CC) $(CFLAGS) $(INCLUDES) -c core11.c

core27.o: core27.c
	$(CC) $(CFLAGS) $(INCLUDES) -c core27.c

core12.o: core12.c
	$(CC) $(CFLAGS) $(INCLUDES) -c core12.c

core28.o: core28.c
	$(CC) $(CFLAGS) $(INCLUDES) -c core28.c

core13.o: core13.c
	$(CC) $(CFLAGS) $(INCLUDES) -c core13.c

core29.o: core29.c
	$(CC) $(CFLAGS) $(INCLUDES) -c core29.c

core14.o: core14.c
	$(CC) $(CFLAGS) $(INCLUDES) -c core14.c

core30.o: core30.c
	$(CC) $(CFLAGS) $(INCLUDES) -c core30.c

core15.o: core15.c
	$(CC) $(CFLAGS) $(INCLUDES) -c core15.c

core31.o: core31.c
	$(CC) $(CFLAGS) $(INCLUDES) -c core31.c

