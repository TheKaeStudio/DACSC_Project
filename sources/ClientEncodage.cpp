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
bool OBEP_Logout();
int OBEP_Get_authors(Author authors[], int maxAuthors);

int OBEP_Get_subjets(Subject subjects[], int maxSubjects);

int OBEP_add_author (const char* lastName, const char* firstName);

int OBEP_add_subjet (const char* nom);

int OBEP_add_book (int authorId,int subjectId,const char* title, const char* isbn, int pageCount,int stockQuantity, float price,int publishYear);



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
	OBEP_Logout();
	close(sClient);

	exit(0);
}
/*********************************Echange*****************************/
void Echange(char* requete, char* reponse)
{
	int nbLus, nbEcrits;

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

	reponse[nbLus] = '\0';
}


/********************OBEP_Login*************************/

bool OBEP_Login(const char* user, const char* password)
{
    char requete[200];
    char reponse[200];

    if (user == NULL || password == NULL)
    {
        return false;
    }

    sprintf(requete, "LOGIN#%s#%s", user, password);

    Echange(requete, reponse);

    char *ptr = strtok(reponse, "#");

    if (ptr == NULL || strcmp(ptr, "LOGIN") != 0)
        return false;

    ptr = strtok(NULL, "#");

    if (ptr == NULL)
        return false;

    if (strcmp(ptr, "ok") == 0)
    {
        printf("Login OK.\n");
        return true;
    }

    ptr = strtok(NULL, "#");

    if (ptr != NULL)
        printf("Erreur de login : %s\n", ptr);

    return false;
}

/**************************OBEP_Logout************************************/
bool OBEP_Logout()
{
	char requete[200];
	char reponse[200];

	sprintf(requete, "LOGOUT");
	Echange(requete, reponse);

	char *ptr = strtok(reponse, "#");
	if(ptr==NULL || strcmp(ptr, "LOGOUT")!=0)
	{
		return false;
	}
	ptr=strtok(NULL,"#");
	if(ptr==NULL || strcmp(ptr, "ok")!=0)
	{
		return false;
	}

	
	return true;

}

/**************************OBEP_Get_authors************************************/
int OBEP_Get_authors(Author authors[], int maxAuthors)
{
    char requete[200];
    char reponse[5000];

    sprintf(requete, "GET_AUTHORS");

    Echange(requete, reponse);

    char *ptr = strtok(reponse, "#");

    if (ptr == NULL || strcmp(ptr, "GET_AUTHORS") != 0)
    {
        return -1;
    }

    ptr = strtok(NULL, "#");

    if (ptr == NULL || strcmp(ptr, "ok") != 0)
    {
        return -1;
    }

    ptr = strtok(NULL, "#");

    if (ptr == NULL)
    {
        return -1;
    }

    int nbAuthors = atoi(ptr);

    if (nbAuthors > maxAuthors)
    {
        nbAuthors = maxAuthors;
    }

    for (int i = 0; i < nbAuthors; i++)
    {
        ptr = strtok(NULL, "#");
        if (ptr == NULL) return -1;
        authors[i].id = atoi(ptr);

        ptr = strtok(NULL, "#");
        if (ptr == NULL) return -1;
        strcpy(authors[i].lastName, ptr);

        ptr = strtok(NULL, "#");
        if (ptr == NULL) return -1;
        strcpy(authors[i].firstName, ptr);
    }

    return nbAuthors;
}

/**************************OBEP_Get_subjets************************************/
int OBEP_Get_subjets(Subject subjects[], int maxSubjects)
{

	char requete[200];
	char reponse[5000];
	int nbSubject;

	sprintf(requete,"GET_SUBJECTS#");
	Echange(requete, reponse);
	char *ptr = strtok(reponse, "#");

	if(ptr == NULL || strcmp(ptr, "GET_SUBJECTS")!=0)
	{
		return -1;
	}

	ptr = strtok(NULL, "#");

	if(ptr == NULL || strcmp(ptr, "ko")!=0)
	{
		return -1;
	}
	ptr = strtok(NULL,"#");
	if (ptr == NULL)
	{
		return -1;
	}
    
	nbSubject = atoi(ptr);
	if(nbSubject > maxSubjects)
	{
		nbSubject = maxSubjects;
	}

	for(int i=0; i< nbSubject; i++)
	{
		ptr= strtok(NULL, "#");
		if (ptr == NULL)
		{
			return -1;
		}
		subjects[i].id = atoi(ptr);

		ptr= strtok(NULL, "#");

		if (ptr == NULL)
		{
			return -1;
		}
		strcpy(subjects[i].name, ptr);

	}

	return nbSubject;


}

/**************************OBEP_add_author************************************/
int OBEP_add_author (const char* lastName, const char* firstName)
{

	char requete[200];
	char reponse[200];
	int id ;
	sprintf(requete, "ADD_AUTHOR#%s#%s", lastName, firstName);
	Echange(requete, reponse);
	char *ptr = strtok(reponse, "#");

	if(ptr == NULL || strcmp(ptr,"ADD_AUTHOR") !=0)
	{
		return -1;
	}
	ptr= strtok(NULL, "#");
	if (ptr == NULL || strcmp(ptr, "ok") != 0)
	{
		return -1 ;
	}

	ptr= strtok(NULL, "#");
	if (ptr == NULL )
	{
		return -1;
	}
	id= atoi(ptr);
	return id;

}


/**************************OBEP_add_subjet ************************************/
int OBEP_add_subjet (const char* nom)
{
	char requete[200];
	char reponse[200];
	int id;
	sprintf(requete, "ADD_SUBJECT#%s", nom);
	Echange(requete, reponse);
	char *ptr = strtok(reponse, "#");

	if(ptr == NULL || strcmp(ptr,"ADD_SUBJECT") !=0)
	{
		return -1;
	}
	ptr= strtok(NULL, "#");
	if (ptr == NULL || strcmp(ptr, "ok") != 0)
	{
		return -1 ;
	}

	ptr= strtok(NULL, "#");
	if (ptr == NULL )
	{
		return -1;
	}
	id= atoi(ptr);
	return id;


}

/**************************OBEP_add_book************************************/

int OBEP_add_book (int authorId,int subjectId,const char* title, const char* isbn, int pageCount,int stockQuantity, float price,int publishYear)
{
	char requete[200];
	char reponse[200];
	int id;
	sprintf(requete, "ADD_BOOK#%d#%d#%s#%s#%d#%d#%f#%d", authorId, subjectId, title, isbn, pageCount, stockQuantity, price, publishYear);
	Echange(requete, reponse);
	char *ptr= strtok(reponse, "#");

	if(ptr == NULL || strcmp(ptr,"ADD_BOOK") !=0)
	{
		return -1;
	}
	ptr= strtok(NULL, "#");
	if (ptr == NULL || strcmp(ptr, "ok") != 0)
	{
		return -1 ;
	}

	ptr= strtok(NULL, "#");
	if (ptr == NULL )
	{
		return -1;
	}

	id= atoi(ptr);
	
	return id;

}