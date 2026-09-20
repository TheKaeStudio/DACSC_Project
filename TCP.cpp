#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <netdb.h>

#include <sys/types.h>
#include <sys/socket.h>

#include "TCP.h"

int main() {
	return 0;
}

int ServerSocket(int port) {
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

	memset(&hints,0,sizeof(struct addrinfo));
	hints.ai_family = AF_INET;
	hints.ai_socktype = SOCK_STREAM;
	hints.ai_flags = AI_PASSIVE | AI_NUMERICSERV; // pour une connexion passive

	if (getaddrinfo(NULL,"50000",&hints,&results) != 0)
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

 	pause();

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

int ClientSocket(char* ipServeur,int portServeur) {
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

	 // Demande de connexion
	 if (connect(sClient,results->ai_addr,results->ai_addrlen) == -1)
	 {
		 perror("Erreur de connect()");
		 exit(1);
	 }
	 printf("connect() reussi !");

	return sClient;
}

int Send(int sSocket,char* data,int taille) {

	int nbEcrits; 

	if ((nbEcrits = write(sSocket,data,strlen(data))) ==-1) 
	{ 
		perror("Erreur de Send"); 
		close(sSocket);
		exit(1); 
	} 
		
	return nbEcrits; 

}

int Receive(int sSocket,char* data) {
	int nbLus;

	 if ((nbLus = read(sSocket, data, strlen(data)))==-1)
	 {
	 	perror("Erreur de receive"); 
	 	close(sSocket);
		exit(1);  
	 }
	return nbLus;
}