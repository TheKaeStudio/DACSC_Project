#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <netdb.h>

#include <sys/types.h>
#include <sys/socket.h>

#include "TCP.h"


int ServerSocket(int port)
 {
	int sServeur;


	printf("pid = %d\n",getpid());

	if ((sServeur = socket(AF_INET, SOCK_STREAM, 0)) == -1)
	{
		perror("Erreur de socket()");
		exit(1);
	}

	printf("socket creee = %d\n",sServeur);
	
	// Pour la recherche
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
		close(sServeur);
		exit(1);
	} 

	// Liaison de la socket à l'adresse
	if (bind(sServeur,results->ai_addr,results->ai_addrlen) < 0)
	{
		perror("Erreur de bind()");
		exit(1);
	}
	freeaddrinfo(results);
	printf("bind() reussi !\n");

	// Mise à l'écoute de la socket
	if (listen(sServeur,SOMAXCONN) == -1)
	{
		perror("Erreur de listen()");
		exit(1);
	}
	printf("listen() reussi !\n");


 	return sServeur;
}

int Accept(int sEcoute,char *ipClient) {
	int sService;

	if ((sService = accept(sEcoute,NULL,NULL)) == -1)
	{
		perror("Erreur de accept()");
		exit(1);
	}

	printf("accept() reussi !");
	printf("socket de service = %d\n",sService);

	return sService;
}

int ClientSocket(char* ipServeur,int portServeur) 
{
	int sClient;
	printf("pid = %d\n",getpid());

	// Creation de la socket
	if ((sClient = socket(AF_INET, SOCK_STREAM, 0)) == -1)
	{
		perror("Erreur de socket()");
		exit(1);
	}

	printf("socket creee = %d\n",sClient);

	// Construction de l'adresse du serveur 
	struct addrinfo hints; 
	struct addrinfo *results; 
	memset(&hints,0,sizeof(struct addrinfo)); 
	hints.ai_family = AF_INET; 
	hints.ai_socktype = SOCK_STREAM; 
	hints.ai_flags = AI_NUMERICSERV; 
	char portToStr[10];
	sprintf(portToStr, "%d", portServeur);

	if (getaddrinfo(ipServeur,portToStr,&hints,&results) != 0) 
	{
		exit(1); 
	}

	  // Affichage du contenu de l'adresse obtenue 
	char host[NI_MAXHOST]; 
	char port[NI_MAXSERV]; 
	getnameinfo(results->ai_addr,results->ai_addrlen, 
	host,NI_MAXHOST,port,NI_MAXSERV, 
	NI_NUMERICSERV | NI_NUMERICHOST); 
	printf("Mon Adresse IP: %s -- Mon Port: %s\n",host,port); 


	
	 // Demande de connexion
	 if (connect(sClient,results->ai_addr,results->ai_addrlen) == -1)
	 {
		 perror("Erreur de connect()");
		 exit(1);
	 }
	 printf("connect() reussi !");

	return sClient;
}

int Send(int sSocket, char *data, int taille)
{
    int nbEcrits;
    int total = 0;

    char messagePlusEntete[taille + 5];

    sprintf(messagePlusEntete, "%04d", taille);
    strcat(messagePlusEntete, data);

    printf("messagePlusEntete = %s\n", messagePlusEntete);

    int tailleMessage = taille + 4;

    while (total < tailleMessage)
    {
        nbEcrits = write(sSocket,
                         messagePlusEntete + total,
                         tailleMessage - total);

        if (nbEcrits <= 0)
        {
            perror("Erreur de Send");
            close(sSocket);
            exit(1);
        }

        total += nbEcrits;
    }

    return total;
}

int Receive(int sSocket, char *data)
{
    int nbLus;
    int total = 0;
    int tailleData;

    char bufferEntete[5];

    // Lire  les 4 caractères de l'entête
    while (total < 4)
    {
        nbLus = read(sSocket, bufferEntete + total, 4 - total);

        if (nbLus <= 0)
        {
            perror("Erreur de receive");
            close(sSocket);
            exit(1);
        }

        total += nbLus;
    }

    bufferEntete[4] = '\0';

    tailleData = atoi(bufferEntete);

    total = 0;

    // Lire  tailleData caractères
    while (total < tailleData)
    {
        nbLus = read(sSocket,
                     data + total,
                     tailleData - total);

        if (nbLus <= 0)
        {
            perror("Erreur de receive");
            close(sSocket);
            exit(1);
        }

        total += nbLus;
    }

    data[tailleData] = '\0';

    return tailleData;
}