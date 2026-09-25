#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <sys/ipc.h>
#include <pthread.h>
#include <time.h>
#include <string.h>
#include <unistd.h>
#include <mysql.h>
#include <cerrno>
#include <setjmp.h>
#include <errno.h>

#include <signal.h> 
#include "TCP.h"


#define NB_THREADS 3

void* thread_function(void* arg);
int main()
{
	int sServeur;

	sServeur= ServerSocket(50000);

	pthread_t threads[NB_THREADS];

	printf("creation thread\n");
	for(int i =0; i< NB_THREADS; i++)
	{
		printf("je cree\n");
		pthread_create(&threads[i], NULL, thread_function, &sServeur);
	}



}

void* thread_function(void* arg)
{
	printf("je suis dans la fct de thread\n");
	int sService;
	int ServeurSocket;

	ServeurSocket= *(int *)arg;

	printf("socket serveur%d\n",*(int *)arg);

	while(1)
	{
		printf("socket serveur%d\n",*(int *)arg);
		sService= Accept(ServeurSocket, NULL);
	}
	
	printf("la socket de serice est %d\n", sService);



	return 0;

}