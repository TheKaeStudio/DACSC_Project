#include <stdio.h> 
#include <stdlib.h> 
#include <unistd.h> 
#include <string.h> 
#include <signal.h> 
#include "TCP.h" 

int sClient; 
void HandlerSIGINT(int s); 
void Echange(char* requete, char* reponse); 



int main(int argc,char* argv[]) 
{ 
	if (argc != 3) 
	{ 
		printf("Erreur...\n"); 
		printf("USAGE : Client ipServeur portServeur\n"); 
		exit(1); 
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


	// Connexion sur le serveur  
	if ((sClient = ClientSocket(argv[1],atoi(argv[2]))) == -1) 
	{ 
		perror("Erreur de ClientSocket"); 
		exit(1); 
	} 
	printf("Connecte sur le serveur.\n"); 
	// Phase de login 

	/*char user[50],password[50]; 

	printf("user: "); fgets(user,50,stdin); 
	user[strlen(user)-1] = 0; 
	printf("password: "); fgets(password,50,stdin); 
	password[strlen(password)-1] = 0; */
	
	while(1)
	{
		printf("client %d\n", sClient);
	}
	exit(1); 
	
}
void HandlerSIGINT(int s)
{
	//logout plutard
	close(sClient);

	exit(0);
}
