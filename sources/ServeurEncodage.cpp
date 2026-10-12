#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <signal.h>
#include <pthread.h>
#include <mysql.h>
#include "TCP.h"
#include "OBEP.h"

#define FichierNom "FConfigServeur.txt"

// File d'attente des sockets acceptees
#define TAILLE_FILE_ATTENTE 10
int socketsAcceptees[TAILLE_FILE_ATTENTE];
int indiceEcriture = 0, indiceLecture = 0, nbSocketsEnAttente = 0;

int sEcoute;
int PORT_ENCODING, NB_THREADS_POOL;

pthread_mutex_t mutexSocketsAcceptees = PTHREAD_MUTEX_INITIALIZER;
pthread_cond_t  condSocketsAcceptees = PTHREAD_COND_INITIALIZER;

MYSQL* connexion;

void HandlerSIGINT(int s);
void TraitementConnexion(int sService);
void* FctThreadClient(void* p);

int main()
{
	// Connection a la BD
	connexion = mysql_init(NULL);
	if (mysql_real_connect(connexion,"localhost","Student","PassStudent1_","PourStudent",0,0,0) == NULL)
	{
		fprintf(stderr,"(SERVEUR) Erreur de connexion a la base de donnees...\n");
		exit(1);
	}

	// Lecture du fichier de configuration : port puis nombre de threads
	FILE *fp = fopen(FichierNom, "r");
	if (fp == NULL)
	{
		perror("Erreur ouverture fichier de configuration");
		exit(1);
	}
	if (fscanf(fp, "%d", &PORT_ENCODING) != 1 || fscanf(fp, "%d", &NB_THREADS_POOL) != 1
	    || PORT_ENCODING <= 0 || NB_THREADS_POOL <= 0)
	{
		fprintf(stderr,"Fichier de configuration invalide (attendu : port puis nombre de threads)\n");
		exit(1);
	}
	fclose(fp);
	printf("Port = %d\n", PORT_ENCODING);
	printf("Nombre de threads = %d\n", NB_THREADS_POOL);

	// La file d'attente est vide
	for (int i=0 ; i<TAILLE_FILE_ATTENTE ; i++)
		socketsAcceptees[i] = -1;

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

	// Creation de la socket d'ecoute
	if ((sEcoute = ServerSocket(PORT_ENCODING)) == -1)
	{
		fprintf(stderr,"Erreur de ServerSocket\n");
		exit(1);
	}

	// Creation du pool de threads
	printf("Creation du pool de threads.\n");
	pthread_t th;
	for (int i=0 ; i<NB_THREADS_POOL ; i++)
	{
		if (pthread_create(&th,NULL,FctThreadClient,NULL) != 0)
		{
			fprintf(stderr,"Erreur de pthread_create\n");
			exit(1);
		}
	}

	// Mise en boucle du serveur
	int sService;
	char ipClient[50];
	printf("Demarrage du serveur.\n");

	while(1)
	{
		if ((sService = Accept(sEcoute,ipClient)) == -1)
		{
			usleep(100000); // pause avant de reessayer
			continue;
		}
		printf("Connexion acceptee : IP=%s socket=%d\n",ipClient,sService);

		// Mise de la socket dans la file d'attente (si il y a de la place)
		pthread_mutex_lock(&mutexSocketsAcceptees);
		if (nbSocketsEnAttente < TAILLE_FILE_ATTENTE)
		{
			socketsAcceptees[indiceEcriture] = sService;
			indiceEcriture = (indiceEcriture + 1) % TAILLE_FILE_ATTENTE;
			nbSocketsEnAttente++;
			printf("Socket %d ajoutee dans la file\n", sService);

			// Notification d'attente envoyee au client
			char message[] = "WAIT#Vous êtes en attente#";
			Send(sService, message, strlen(message));

			pthread_mutex_unlock(&mutexSocketsAcceptees);
			pthread_cond_signal(&condSocketsAcceptees);
		}
		else
		{
			pthread_mutex_unlock(&mutexSocketsAcceptees);

			char message[] = "BUSY#File pleine#";
			Send(sService, message, strlen(message));
			printf("File d'attente pleine : connexion refusee (socket %d)\n",sService);
			close(sService);
		}
	}
}

void* FctThreadClient(void* p)
{
	int sService;

	while(1)
	{
		printf("\t[THREAD %p] Attente socket...\n",(void*)pthread_self());

		// Attente d'une tache
		pthread_mutex_lock(&mutexSocketsAcceptees);
		while (nbSocketsEnAttente == 0)
			pthread_cond_wait(&condSocketsAcceptees,&mutexSocketsAcceptees);

		sService = socketsAcceptees[indiceLecture];
		socketsAcceptees[indiceLecture] = -1;
		indiceLecture = (indiceLecture + 1) % TAILLE_FILE_ATTENTE;
		nbSocketsEnAttente--;
		pthread_mutex_unlock(&mutexSocketsAcceptees);

		// Traitement de la connexion (consommation de la tache)
		printf("\t[THREAD %p] Je m'occupe de la socket %d\n",(void*)pthread_self(),sService);

		char message[] = "READY#Vous êtes pris en charge#";
		if (Send(sService, message, strlen(message)) < 0)
		{
			close(sService); // le client est deja parti
			continue;
		}

		TraitementConnexion(sService);
	}
	return NULL;
}

void HandlerSIGINT(int s)
{
	printf("\nArret du serveur.\n");
	close(sEcoute);

	// Fermeture des sockets en attente
	for (int i=0 ; i<TAILLE_FILE_ATTENTE ; i++)
		if (socketsAcceptees[i] != -1)
			close(socketsAcceptees[i]);

	OBEP_Close();
	exit(0);
}

void TraitementConnexion(int sService)
{
	// Un message fait au maximum TAILLE_MAX_DATA octets (+ le '\0' final)
	char requete[TAILLE_MAX_DATA + 1], reponse[TAILLE_MAX_DATA + 1];
	int nbLus;
	bool onContinue = true;

	while (onContinue)
	{
		// ***** Reception de la requete **************************
		nbLus = Receive(sService, requete);

		// ***** Fin de connexion ou erreur ? *********************
		if (nbLus == 0)
		{
			printf("\t[THREAD %p] Fin de connexion du client.\n",(void*)pthread_self());
			break;
		}
		if (nbLus < 0)
		{
			printf("\t[THREAD %p] Erreur de reception : connexion abandonnee.\n",(void*)pthread_self());
			break;
		}
		printf("\t[THREAD %p] Requete recue = %s\n",(void*)pthread_self(),requete);

		// ***** Traitement de la requete *************************
		onContinue = OBEP(requete,reponse,sService);

		// ***** Envoi de la reponse ******************************
		if (Send(sService,reponse,strlen(reponse)) < 0)
		{
			printf("\t[THREAD %p] Erreur d'envoi : connexion abandonnee.\n",(void*)pthread_self());
			break;
		}
		printf("\t[THREAD %p] Reponse envoyee = %s\n",(void*)pthread_self(),reponse);
	}

	// Fin de connexion : le client n'est plus logge et la socket est fermee
	OBEP_Logout(sService);
	close(sService);
	printf("\t[THREAD %p] Fin de connexion de la socket %d\n",(void*)pthread_self(),sService);
}
