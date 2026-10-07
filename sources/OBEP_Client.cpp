#include "OBEP_Client.h"
#include "TCP.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

int sClient;

/********************OBEP_Connect*************************/
bool OBEP_Connect(char* ip, int port)
{
    sClient = ClientSocket(ip, port);

    if (sClient == -1)
    {
        return false;
    }

    return true;
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

/**********************OBEP_close***********************************/
void OBEP_close()
{
    close(sClient);
}