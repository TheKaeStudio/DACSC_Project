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
#include <mysql.h>
#include <signal.h> 
#include "TCP.h"
#include "OBEP.h"
#define FichierNom "FConfigServeur.txt"


#define TAILLE_FILE_ATTENTE 2 
int socketsAcceptees[TAILLE_FILE_ATTENTE]; 
int indiceEcriture=0, indiceLecture=0; 

int sEcoute;
int PORT_ENCODING, NB_THREADS_POOL;

void HandlerSIGINT(int s); 
void TraitementConnexion(int sService); 
void* FctThreadClient(void* p); 

pthread_mutex_t mutexSocketsAcceptees ; 
pthread_cond_t  condSocketsAcceptees; 

MYSQL* connexion;
void* thread_function(void* arg);

int main() 
{
	char buffer[10];
	// Connection à la BD
    connexion = mysql_init(NULL);
    if (mysql_real_connect(connexion,"localhost","Student","PassStudent1_","PourStudent",0,0,0) == NULL)
    {
      fprintf(stderr,"(SERVEUR) Erreur de connexion à la base de données...\n");
      exit(1);  
    }

	FILE *fp = fopen(FichierNom, "r");

	if (fp == NULL) {
	    perror("Erreur ouverture fichier");
	    exit(EXIT_FAILURE);
	}

	fscanf(fp, "%d", &PORT_ENCODING);
	fscanf(fp, "%d", &NB_THREADS_POOL);

	printf("Port = %d\n", PORT_ENCODING);
	printf("nombre de threads = %d\n", NB_THREADS_POOL);

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

		/*char messageListAttente[100] = "vous êtes en liste d'attend"; 
		int taille = strlen(messageListAttente);
		send(sService, messageListAttente,taille);*/


		//printf("messageListAttente = %s \t taille =  %d\n", messageListAttente, taille);
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

		TraitementConnexion(sService); 

	
		printf("serveur apres send()\n");
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
	OBEP_Close(); 
	exit(0); 
} 

void TraitementConnexion(int sService)
{
	char requete[200], reponse[200]; 
	int nbLus, nbEcrits; 
	bool onContinue = true;

//onContinue pour sortir de ma boucle de traitement
	while(onContinue)
	{

		if((nbLus = Receive(sService, requete))<0)
		{
			perror("Erreur Receive");
			close(sService);
			HandlerSIGINT(0);
		}


		// ***** Fin de connexion ? ***************** 
		if (nbLus == 0) 
		{ 
			printf("\t[THREAD %p] Fin de connexion du client.\n",pthread_self()); 
			close(sService); 
			return; 
		} 
		requete[nbLus] = 0; 

		printf("\t[THREAD %p] Requete recue = %s\n",pthread_self(),requete); 

		// ***** Traitement de la requete *********** 
		onContinue = OBEP(requete,reponse,sService); 
		if(onContinue)
		{
		 	printf("serveurTraitement onContinue : true\n");
		}
		else
		{
			printf("serveurTraitement onContinue : false\n");
		}
		
		// ***** Envoi de la reponse **************** 
		if ((nbEcrits = Send(sService,reponse,strlen(reponse))) < 0) 
		{ 
			perror("Erreur de Send"); 
			close(sService); 
			HandlerSIGINT(0); 
		} 
		printf("\t[THREAD %p] Reponse envoyee = %s\n",pthread_self(),reponse); 

		printf("nbLus : %d\n");
		if (!onContinue)  
			printf("\t[THREAD %p] Fin de connexion de la socket %d\n",pthread_self(),sService); 

	}
}