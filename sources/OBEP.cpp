#include "OBEP.h"
#include <string.h> 
#include <stdio.h> 
#include <stdlib.h> 
#include <unistd.h> 
#include <pthread.h>
#include <mysql.h>

int clients[NB_MAX_CLIENTS];
int nbclients =0;

int  estPresent(int socket); 
void ajoute(int socket); 
void retire(int socket); 
pthread_mutex_t mutexClients = PTHREAD_MUTEX_INITIALIZER;
pthread_mutex_t mutexBD = PTHREAD_MUTEX_INITIALIZER;
extern MYSQL *connexion;

//***** Parsing de la requete et creation de la reponse ************* 

bool OBEP(char* requete, char* reponse,int socket) //serveur l'appel
{
	printf("%s\n", requete);
	// ***** Récupération nom de la requete ***************** 
	char *ptr = strtok(requete,"#"); 
	if (ptr == NULL)
    {
        sprintf(reponse, "ERROR#Requete vide");
        return false;
    }
	

	// ***** LOGIN ****************************************** 
	if (strcmp(ptr,"LOGIN") == 0)  
	{ 
		char *login = strtok(NULL, "#");
        char *password = strtok(NULL, "#");
        
		printf("\t[THREAD %p] LOGIN de %s\n",pthread_self(),login); 

		if (estPresent(socket) >= 0)  // client déjà loggé 
		{ 
			sprintf(reponse,"LOGIN#ko#Client déjà logge !"); 
			return false;  
		} 
		
		if (OBEP_Login(login,password)) 
		{ 
			sprintf(reponse,"LOGIN#ok"); 
			ajoute(socket);
			return true;
		}
		
		sprintf(reponse,"LOGIN#ko#Mauvais identifiants !");
		return false;
		
	}
	/****************LOGOUT ******************/
	else if(strcmp(ptr, "LOGOUT")==0)
	{
		
		if(OBEP_Logout(socket))
		{
			sprintf(reponse, "LOGOUT#ok");
			return true;
		}
		sprintf(reponse, "LOGOUT#ko#Client non logge");
    	return false;
	}
	/***************GET_AUTHEORS***********************/
	else if(strcmp(ptr, "GET_AUTHORS")==0)
	{
		
		
		Author authors[100];
		int nbAuthor= OBEP_GET_AUTHORS(authors,100);
		if (nbAuthor<=0)
		{ 
			sprintf(reponse,"GET_AUTHORS#ko#pas d'auteur!");
			return false;

		}
		sprintf(reponse, "GET_AUTHORS#ok#%d", nbAuthor);
		for(int i=0; i< nbAuthor; i++)
		{
			sprintf(reponse + strlen(reponse),"#%d#%s#%s", authors[i].id, authors[i].lastName,authors[i].firstName);
		}

			
		
	}
	/******************GET_SUBJECTS*************************/
		
	else if(strcmp(ptr, "GET_SUBJECTS")==0)
	{

		Subject sujets[100];
		int nbSujet= OBEP_GET_SUBJECTS(sujets,100);
		if (nbSujet<=0)
		{ 
			sprintf(reponse,"GET_SUBJECTS#ko#pas d'auteur!");
			return false;

		}
		sprintf(reponse, "GET_SUBJECTS#ok#%d", nbSujet);
		for(int i=0; i< nbSujet; i++)
		{
			sprintf(reponse + strlen(reponse),"#%d#%s", sujets[i].id, sujets[i].name);
		}
			
		return true;
		
	}
	/**********************ADD_AUTHOR******************************/
	else if(strcmp(ptr, "ADD_AUTHOR")==0)
	{

		char *lastName= strtok(NULL, "#");
        char *firstName = strtok(NULL, "#");
       

        int id = OBEP_ADD_AUTHOR(lastName, firstName);
        
        if(id <=0)
        {
        	sprintf(reponse, "ADD_AUTHOR#ko#%d", id);
            return false;
        }
        sprintf(reponse, "ADD_AUTHOR#ok#%d", id);
        return true ;

	}

	else if (strcmp(ptr, "ADD_SUBJECT") == 0)
	{
		char *name= strtok(NULL, "#");
 
        int id = OBEP_ADD_SUBJECT(name);
        sprintf(reponse, "ADD_SUBJECT#ok#%d", id);
        if(id <=0)
        {
            return false;
        }
        
        return true ;
	}

	else if(strcmp(ptr, "ADD_BOOK")==0)
	{
		int authorId = atoi(strtok(NULL, "#"));
		int subjectId = atoi(strtok(NULL, "#"));
		char *title= strtok(NULL, "#");
		char *isbn= strtok(NULL, "#");
		int pageCount= atoi(strtok(NULL, "#"));
		int stockQuantity = atoi(strtok(NULL, "#"));
		float price = strtof(strtok(NULL, "#"), NULL);
		int publishYear = atoi(strtok(NULL, "#"));
        if (title == NULL)
        {
            sprintf(reponse, "ADD_BOOK#ko#Parametres manquants");
            return false;
        }


        int id = OBEP_ADD_BOOK(authorId, subjectId, title, isbn, pageCount, stockQuantity, price, publishYear);
        sprintf(reponse, "ADD_BOOK#ok#%d", id);
        if(id <=0)
        {
            return false;
        }
        
        return true ;

	}
	 else
    {
        sprintf(reponse, "ERROR#Commande inconnue");
        return false;
    }

    return true;
		
} 
///////////////////////////////////////////////////////////////////////
bool OBEP_Login(const char* identifiant,const char* password)
{
	
	MYSQL_RES  *resultat;
	char requete[200];

	sprintf(requete,"select id from employees where lower(identifiant) like lower('%s') and lower(password) like lower('%s')", identifiant, password);

	pthread_mutex_lock(&mutexBD);
	if (mysql_query(connexion, requete))
    {
        fprintf(stderr, "Erreur mysql : %s\n",
        mysql_error(connexion));

        return false;
    }
    printf("\nrequete sql envoyé\n");

    resultat = mysql_store_result(connexion);


	if(resultat == NULL)
	{
		fprintf(stderr, "Erreur mysql %s\n", mysql_error(connexion));
		return false;
	}
	int nbTuple = mysql_num_rows(resultat);
	printf("nbTuple %d\n", nbTuple);

	if(nbTuple>0)
	{
		 mysql_free_result(resultat);
		 return true;
	}
	sprintf(requete, "INSERT INTO employees VALUES(NULL, '%s', '%s')", identifiant, password );

	if(mysql_query(connexion, requete))
	{
		 fprintf(stderr, "Erreur mysql : %s\n",
        mysql_error(connexion));

        return false;
	}
	mysql_free_result(resultat);
    pthread_mutex_unlock(&mutexBD);

	return true;
    
}
////////////////////////////////////////////////////////////
bool OBEP_Logout(int socket)
{
	
	if( estPresent(socket)>=0)
	{
		retire(socket);
		return true;
	}
	return false;

}
/////////////////////////////////////////////////////////
int OBEP_GET_AUTHORS(Author authors[], int maxAuthors)
{
	MYSQL_RES *resultat;
	MYSQL_ROW row;
	char requete[200];

	 sprintf(requete,"SELECT id, last_name, first_name FROM authors");

	pthread_mutex_lock(&mutexBD);
	if(mysql_query(connexion, requete))
	{
		fprintf(stderr, "Erreur mysql : %s\n",
        mysql_error(connexion));

        return -1;
	}
	resultat= mysql_store_result(connexion);
	if(resultat==NULL)
	{
		fprintf(stderr, "Erreur mysql %s\n", mysql_error(connexion));
		return -1;
	}


	
	int i=0;
	while ((row = mysql_fetch_row(resultat)) != NULL && i < maxAuthors)
	{
	    authors[i].id = atoi(row[0]);
	    strcpy(authors[i].lastName, row[1]);
	    strcpy(authors[i].firstName, row[2]);

	    i++;
	}

	mysql_free_result(resultat);
	pthread_mutex_unlock(&mutexBD);

	return i;
}
//////////////////////////////////////////////////////////////////

int OBEP_GET_SUBJECTS(Subject subjects[], int maxSubjects)
{
	MYSQL_RES *resultat;
	MYSQL_ROW row;
	char requete[200];

	sprintf(requete, "select * from subjects");
	pthread_mutex_lock(&mutexBD);
	if(mysql_query(connexion, requete))
	{
		fprintf(stderr, "Erreur mysql : %s\n",
        mysql_error(connexion));

        return -1;
	}
	resultat= mysql_store_result(connexion);
	if(resultat==NULL)
	{
		fprintf(stderr, "Erreur mysql %s\n", mysql_error(connexion));
		return -1;
	}
	

	
	int i=0;
	while ((row = mysql_fetch_row(resultat)) != NULL && i < maxSubjects)
	{
	    subjects[i].id = atoi(row[0]);
	    strcpy(subjects[i].name, row[1]);
	   
	    i++;
	}

	mysql_free_result(resultat);
	pthread_mutex_unlock(&mutexBD);
	return i;
}

///////////////////////////////////////////////////////////////////////////////////////////////

int OBEP_ADD_AUTHOR(const char* lastName, const char* firstName)
{
	MYSQL_RES *resultat;
	MYSQL_ROW row;
	char requete[200];
	int id;
	 sprintf(requete, "SELECT id FROM authors WHERE lower(last_name) like lower('%s') AND lower(first_name) = lower('%s')",
            lastName, firstName);
	 pthread_mutex_lock(&mutexBD);
	if(mysql_query(connexion, requete))
	{
		fprintf(stderr, "Erreur mysql : %s\n",
        mysql_error(connexion));

        return -1;
	}

	resultat = mysql_store_result(connexion);

	 if (resultat == NULL)
    {
        fprintf(stderr, "Erreur mysql : %s\n", mysql_error(connexion));
        return -1;
    }

    // L'auteur existe déjà
    if (mysql_num_rows(resultat) > 0)
    {
        row = mysql_fetch_row(resultat);

         id = atoi(row[0]);

        mysql_free_result(resultat);

        return id;
    }

    // L'auteur n'existe pas → insertion
    sprintf(requete,"INSERT INTO authors (last_name, first_name) VALUES ('%s', '%s')",
            lastName, firstName);

	if(mysql_query(connexion, requete))
	{
		fprintf(stderr, "Erreur mysql : %s\n",
        mysql_error(connexion));

        return -1;
	}
	id=(int)mysql_insert_id(connexion);

	mysql_free_result(resultat);
	pthread_mutex_unlock(&mutexBD);
	return id;

}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

int OBEP_ADD_SUBJECT(const char* nom)
{
	MYSQL_RES *resultat;
	MYSQL_ROW row;
	char requete[200];
	int id;
	 sprintf(requete, "SELECT id FROM subjects WHERE lower(name) like lower('%s')",
            nom);
	 pthread_mutex_lock(&mutexBD);
	if(mysql_query(connexion, requete))
	{
		fprintf(stderr, "Erreur mysql : %s\n",
        mysql_error(connexion));

        return -1;
	}

	resultat = mysql_store_result(connexion);

	 if (resultat == NULL)
    {
        fprintf(stderr, "Erreur mysql : %s\n", mysql_error(connexion));
        return -1;
    }

    // L'auteur existe déjà
    if (mysql_num_rows(resultat) > 0)
    {
        row = mysql_fetch_row(resultat);

         id = atoi(row[0]);

        mysql_free_result(resultat);

        return id;
    }

    // L'auteur n'existe pas → insertion
    sprintf(requete,"INSERT INTO subjects (name) VALUES ('%s')",
            nom);

	if(mysql_query(connexion, requete))
	{
		fprintf(stderr, "Erreur mysql : %s\n",
        mysql_error(connexion));

        return -1;
	}
	id=(int)mysql_insert_id(connexion);

	mysql_free_result(resultat);
	pthread_mutex_lock(&mutexBD);

	return id;
}


int OBEP_ADD_BOOK(int authorId,int subjectId,const char* title, const char* isbn, int pageCount,int stockQuantity, float price,int publishYear)
{
	MYSQL_RES *resultat;
	MYSQL_ROW row;
	char requete[200];
	int id;


	sprintf(requete,"select id from books where lower(title) like lower(%s) and lower(isbn) like lower(%s) and author_id = (%d)", title, isbn, authorId);
	pthread_mutex_lock(&mutexBD);
	if(mysql_query(connexion, requete))
	{
		fprintf(stderr, "Erreur mysql : %s\n",
        mysql_error(connexion));

        return -1;
	}

	if (mysql_num_rows(resultat) > 0)
    {
        row = mysql_fetch_row(resultat);

         id = atoi(row[0]);

        mysql_free_result(resultat);

        return id;
    }

	sprintf(requete,
    "INSERT INTO books (author_id, subject_id, title, isbn, page_count, stock_quantity, price, publish_year) "
    "VALUES (%d, %d, '%s', '%s', %d, %d, %.2f, %d)",
    authorId, subjectId,title,isbn,pageCount,
    stockQuantity,
    price,
    publishYear);

     if (mysql_query(connexion, requete))
    {
        fprintf(stderr, "Erreur mysql : %s\n",
                mysql_error(connexion));
        return -1;
    }
    id = (int)mysql_insert_id(connexion);
    

	mysql_free_result(resultat);
	pthread_mutex_unlock(&mutexBD);
	return id;
}

void OBEP_Close()
{
	pthread_mutex_lock(&mutexClients); 
	for (int i=0 ; i<nbclients ; i++) 
	close(clients[i]); 
	pthread_mutex_unlock(&mutexClients);
}



//***** Gestion de l'état du protocole ****************************** 
int estPresent(int socket) 
{ 
	int indice = -1; 
	pthread_mutex_lock(&mutexClients); 
	for(int i=0 ; i<nbclients ; i++) 
		if (clients[i] == socket)
		{ 
		 	indice  = i; 
		 	break; 
		} 
	pthread_mutex_unlock(&mutexClients); 
	return indice; 
} 

void ajoute(int socket) 
{ 
	pthread_mutex_lock(&mutexClients); 
	clients[nbclients] = socket; 
	nbclients++; 
	pthread_mutex_unlock(&mutexClients); 
} 
void retire(int socket) 
{ 
	int pos = estPresent(socket); 
	if (pos == -1) return; 
	pthread_mutex_lock(&mutexClients); 
	for (int i=pos ; i<=nbclients-2 ; i++) 
		clients[i] = clients[i+1]; 
		nbclients--; 
	pthread_mutex_unlock(&mutexClients); 
}

