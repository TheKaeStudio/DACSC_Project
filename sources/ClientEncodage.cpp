#include <stdio.h> 
#include <stdlib.h> 
#include <unistd.h> 
#include <string.h> 
#include <signal.h> 
#include "TCP.h" 

int sClient; 
void HandlerSIGINT(int s); 
void Echange(char* requete, char* reponse); 
bool OBEP_Login(const char* user,const char* password);
void OBEP_Logout();

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
	
	
	char texte[80]; 
	sprintf(texte,"Hello, je suis ton ami virtual boby comment vas-tu?"); 
	int nbEcrits; 
	
	if ((nbEcrits = Send(sClient,texte,strlen(texte))) < 0) 
	{ 
		perror("Erreur de Send"); 
		close(sClient); 
	
		exit(1); 
	} 
	printf("NbEcrits = %d\n",nbEcrits); 
	printf("Ecrit    = --%s--\n",texte);

	char buffer[100]; 
	int nbLus; 
	int verif=1;
	while(verif)
	{
		if ((nbLus = Receive(sClient,buffer)) < 0) 
		{ 
			perror("Erreur de Receive dans Serveur"); 
			close(sClient); 
			
			exit(1); 
		} 
		printf("NbLus = %d\n",nbLus); 
		printf("Lu    = --%s--\n",buffer); 
		if(nbLus!=0)
		{
			verif=2;
		}
	}
	
	
	
	exit(1); 
	
}
void HandlerSIGINT(int s)
{
	//logout plutard
	close(sClient);

	exit(0);
}
/*********************************Echange*****************************/
void Echange(char* requete, char* reponse)
{
	int nbLus, nbLus;

	if ((nbEcrits = Send(sClient,requete,strlen(requete))) == -1) 
	{ 
		perror("Erreur de Send"); 
		close(sClient); 
		exit(1); 
	}

	// ***** Attente de la reponse ************************** 
	  
	if ((nbLus = Receive(sClient,reponse)) < 0) 
	{ 
	    perror("Erreur de Receive"); 
	    close(sClient); 
	    exit(1); 
	}

	if (nbLus == 0) 
	{ 
	    printf("Serveur arrete, pas de reponse reçue...\n"); 
	    close(sClient); 
	    exit(1); 
	} 

	reponse[nbLus] = 0;
}


/********************OBEP_Login*************************/

bool OBEP_Login(const char* user,const char* password)
{
	char requete[200];
	char reponse[200];
	if(user == NULL || password ==NULL)
	{
		return false;
	}

	sprintf(requete, "LOGIN#%s#%s", user, password);
	Echange(requete, reponse);

	// ***** Parsing de la réponse ************************** 
	char *ptr = strtok(reponse,"#"); // entête = LOGIN (normalement...) 
	ptr = strtok(NULL,"#"); // statut = ok ou ko 
	if (strcmp(ptr,"ok") == 0) printf("Login OK.\n"); 
	else 
	{ 
		ptr = strtok(NULL,"#"); // raison du ko 
		printf("Erreur de login: %s\n",ptr); 
		onContinue = false; 
	} 
	return onContinue;

}

/**************************OBEP_Logout************************************/
void OBEP_Logout()
{
	char requete[200];
	sprintf(requete, "LOGOUT#%D",ClientSocket);
	Echange(requete, reponse);
}