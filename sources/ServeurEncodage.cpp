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
#define FichierNom "FConfigServeur.txt"


#define TAILLE_FILE_ATTENTE 20 
int socketsAcceptees[TAILLE_FILE_ATTENTE]; 
int indiceEcriture=0, indiceLecture=0; 

int sEcoute;
int PORT_ENCODING, NB_THREADS_POOL;

void HandlerSIGINT(int s); 
void TraitementConnexion(int sService); 
void* FctThreadClient(void* p); 

pthread_mutex_t mutexSocketsAcceptees; 
pthread_cond_t  condSocketsAcceptees; 

void* thread_function(void* arg);

int main() 
{
	char buffer[10];
	
	FILE *fp = fopen(FichierNom, "r");

	if (fp == NULL) {
	    perror("Erreur ouverture fichier");
	    exit(EXIT_FAILURE);
	}

	fscanf(fp, "%d", &PORT_ENCODING);
	fscanf(fp, "%d", &NB_THREADS_POOL);

	printf("Port = %d\n", PORT_ENCODING);
	printf("Autre valeur = %d\n", NB_THREADS_POOL);

	fclose(fp);


	pthread_mutex_init(&mutexSocketsAcceptees, NULL);
	pthread_cond_init(&condSocketsAcceptees, NULL);

	//on initialise le tableau qui va stocker les sockets de service pour chaque client accepter a -1 
	for (int i=0 ; i<TAILLE_FILE_ATTENTE ; i++) 
	{
		socketsAcceptees[i] = -1; 
	}


	// Armement des signaux 
	struct sigaction A; 
	A.sa_flags = 0; 
	sigemptyset(&A.sa_mask); 
	A.sa_handler = HandlerSIGINT; 

	if (sigaction(SIGINT,&A,NULL) == -1) 
	{ 
		perror("Erreur de sigaction"); 
		exit(1); 
	} 


	// Creation de la socket d'écoute 
	if ((sEcoute = ServerSocket(PORT_ENCODING))== -1) 
	{ 
		perror("Erreur de ServeurSocket"); 
		exit(1); 
	} 


	// Creation du pool de threads 
	printf("Création du pool de threads.\n"); 
	pthread_t th; 
	for (int i=0 ; i<NB_THREADS_POOL ; i++) 
	{
		pthread_create(&th,NULL,FctThreadClient,NULL); 
	}
	
	// Mise en boucle du serveur 
	int sService; 
	char ipClient[50]; 

	printf("Demarrage du serveur.\n"); 


	while(1) 
	{ 
		printf("Attente d'une connexion...\n"); 

		if ((sService = Accept(sEcoute,ipClient)) == -1) 
		{ 
			perror("Erreur de Accept"); 
			close(sEcoute); 
			//OBEP_Close(); 
			exit(1); 
		} 

		printf("Connexion acceptée : IP=%s socket=%d\n",ipClient,sService); 
		// Insertion en liste d'attente et réveil d'un thread du pool
		// (Production d'une tâche) 
		pthread_mutex_lock(&mutexSocketsAcceptees); 
		socketsAcceptees[indiceEcriture] = sService; // !!! 
		indiceEcriture++; 
		if (indiceEcriture == TAILLE_FILE_ATTENTE) 
		{
			indiceEcriture = 0; 
		}
		pthread_mutex_unlock(&mutexSocketsAcceptees); 
		pthread_cond_signal(&condSocketsAcceptees); 
	} 

	return 0;
}



void* FctThreadClient(void* p) 
{ 
	int sService; 

	while(1) 
	{ 
		printf("\t[THREAD %p] Attente socket...\n",pthread_self()); 

		// Attente d'une tâche 
		pthread_mutex_lock(&mutexSocketsAcceptees); 
		while (indiceEcriture == indiceLecture) 
		{
			pthread_cond_wait(&condSocketsAcceptees,&mutexSocketsAcceptees); 
		}

		sService = socketsAcceptees[indiceLecture]; 
		socketsAcceptees[indiceLecture] = -1; 
		indiceLecture++; 

		if (indiceLecture == TAILLE_FILE_ATTENTE)
		{
			indiceLecture = 0;
		}  
		pthread_mutex_unlock(&mutexSocketsAcceptees); 

		// Traitement de la connexion (consommation de la tâche) 
		printf("\t[THREAD %p] Je m'occupe de la socket %d\n", pthread_self(),sService); 

		//TraitementConnexion(sService); 
	} 
} 

void HandlerSIGINT(int s) 
{ 
	printf("\nArret du serveur.\n"); 
	close(sEcoute); 

	pthread_mutex_lock(&mutexSocketsAcceptees); 
	for (int i=0 ; i<TAILLE_FILE_ATTENTE ; i++) 
	{
		if (socketsAcceptees[i] != -1)
		{
			close(socketsAcceptees[i]); 
		}
	}
	 
	pthread_mutex_unlock(&mutexSocketsAcceptees); 
	//OBEP_Close(); 
	exit(0); 
} 