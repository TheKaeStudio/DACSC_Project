#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <errno.h>
#include <netdb.h>

#include <sys/types.h>
#include <sys/socket.h>

#include "TCP.h"

#define TAILLE_ENTETE 4

int ServerSocket(int port)
{
	int sServeur;

	if ((sServeur = socket(AF_INET, SOCK_STREAM, 0)) == -1)
	{
		perror("Erreur de socket()");
		return -1;
	}

	// Reutilisation de l'adresse (relance immediate du serveur)
	int opt = 1;
	setsockopt(sServeur, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt));

	// Construction de l'adresse d'ecoute
	struct addrinfo hints;
	struct addrinfo *results;
	char portToStr[10];
	sprintf(portToStr, "%d", port);

	memset(&hints,0,sizeof(struct addrinfo));
	hints.ai_family = AF_INET;
	hints.ai_socktype = SOCK_STREAM;
	hints.ai_flags = AI_PASSIVE | AI_NUMERICSERV; // pour une connexion passive

	if (getaddrinfo(NULL,portToStr,&hints,&results) != 0)
	{
		fprintf(stderr,"Erreur de getaddrinfo()\n");
		close(sServeur);
		return -1;
	}

	// Liaison de la socket a l'adresse
	if (bind(sServeur,results->ai_addr,results->ai_addrlen) < 0)
	{
		perror("Erreur de bind()");
		freeaddrinfo(results);
		close(sServeur);
		return -1;
	}
	freeaddrinfo(results);

	// Mise a l'ecoute de la socket
	if (listen(sServeur,SOMAXCONN) == -1)
	{
		perror("Erreur de listen()");
		close(sServeur);
		return -1;
	}

	return sServeur;
}

int Accept(int sEcoute,char *ipClient)
{
	int sService;
	struct sockaddr_storage adresse;
	socklen_t taille = sizeof(adresse);

	if ((sService = accept(sEcoute,(struct sockaddr*)&adresse,&taille)) == -1)
	{
		perror("Erreur de accept()");
		return -1;
	}

	// Recuperation de l'adresse IP du client (sous forme de texte)
	if (ipClient != NULL)
	{
		if (getnameinfo((struct sockaddr*)&adresse,taille,ipClient,46,NULL,0,NI_NUMERICHOST) != 0)
			strcpy(ipClient,"?");
	}

	return sService;
}

int ClientSocket(char* ipServeur,int portServeur)
{
	int sClient;

	if ((sClient = socket(AF_INET, SOCK_STREAM, 0)) == -1)
	{
		perror("Erreur de socket()");
		return -1;
	}

	// Construction de l'adresse du serveur
	struct addrinfo hints;
	struct addrinfo *results;
	char portToStr[10];
	sprintf(portToStr, "%d", portServeur);

	memset(&hints,0,sizeof(struct addrinfo));
	hints.ai_family = AF_INET;
	hints.ai_socktype = SOCK_STREAM;
	hints.ai_flags = AI_NUMERICSERV;

	if (getaddrinfo(ipServeur,portToStr,&hints,&results) != 0)
	{
		fprintf(stderr,"Erreur de getaddrinfo()\n");
		close(sClient);
		return -1;
	}

	// Demande de connexion
	if (connect(sClient,results->ai_addr,results->ai_addrlen) == -1)
	{
		perror("Erreur de connect()");
		freeaddrinfo(results);
		close(sClient);
		return -1;
	}
	freeaddrinfo(results);

	return sClient;
}

/* Trame : 4 caracteres (taille des donnees, ex. "0008") suivis des donnees. */
int Send(int sSocket,const char* data,int taille)
{
	if (taille <= 0 || taille > TAILLE_MAX_DATA)
		return -1;

	char trame[TAILLE_ENTETE + TAILLE_MAX_DATA + 1];
	sprintf(trame,"%04d",taille);
	memcpy(trame + TAILLE_ENTETE, data, taille);

	int tailleTrame = TAILLE_ENTETE + taille;
	int total = 0;

	while (total < tailleTrame)
	{
		// MSG_NOSIGNAL : pas de signal SIGPIPE si l'autre a ferme la connexion
		int nbEcrits = send(sSocket, trame + total, tailleTrame - total, MSG_NOSIGNAL);
		if (nbEcrits <= 0)
		{
			if (nbEcrits < 0 && errno == EINTR) continue;
			return -1;
		}
		total += nbEcrits;
	}

	return taille;
}

int Receive(int sSocket,char* data)
{
	char entete[TAILLE_ENTETE + 1];
	int total = 0;
	int nbLus;

	// Lecture de l'entete
	while (total < TAILLE_ENTETE)
	{
		nbLus = read(sSocket, entete + total, TAILLE_ENTETE - total);
		if (nbLus == 0)
			return (total == 0) ? 0 : -1; // 0 = connexion fermee proprement
		if (nbLus < 0)
		{
			if (errno == EINTR) continue;
			return -1;
		}
		total += nbLus;
	}
	entete[TAILLE_ENTETE] = '\0';

	int tailleData = atoi(entete);
	if (tailleData <= 0 || tailleData > TAILLE_MAX_DATA)
		return -1; // entete invalide

	// Lecture des donnees
	total = 0;
	while (total < tailleData)
	{
		nbLus = read(sSocket, data + total, tailleData - total);
		if (nbLus == 0) return -1; // connexion coupee en plein message
		if (nbLus < 0)
		{
			if (errno == EINTR) continue;
			return -1;
		}
		total += nbLus;
	}
	data[tailleData] = '\0';

	return tailleData;
}
