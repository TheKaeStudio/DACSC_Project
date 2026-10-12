#include "OBEP_Client.h"
#include "TCP.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#define TAILLE_REQUETE 500

static int sClient = -1;

/********************OBEP_Connect / OBEP_close*************************/
bool OBEP_Connect(char* ip, int port)
{
    OBEP_close();
    sClient = ClientSocket(ip, port);
    return sClient != -1;
}

void OBEP_close()
{
    if (sClient != -1)
    {
        close(sClient);
        sClient = -1;
    }
}

bool OBEP_estConnecte()
{
    return sClient != -1;
}

/*********************************Echange*****************************/
// Envoie la requete et attend la reponse. Retourne false si la connexion est
// perdue (la connexion est alors fermee). reponse doit faire TAILLE_MAX_DATA+1 octets.
static bool Echange(const char* requete, char* reponse)
{
    if (sClient == -1)
        return false;

    if (Send(sClient, requete, strlen(requete)) < 0 || Receive(sClient, reponse) <= 0)
    {
        OBEP_close();
        return false;
    }
    return true;
}

// Verifie que la reponse est "<commande>#ok#..." ; *save permet ensuite de lire la suite
static bool reponseOk(char* reponse, const char* commande, char** save)
{
    char* ptr = strtok_r(reponse, "#", save);
    if (ptr == NULL || strcmp(ptr, commande) != 0)
        return false;

    ptr = strtok_r(NULL, "#", save);
    return ptr != NULL && strcmp(ptr, "ok") == 0;
}

// Pour les commandes ADD_xxx : retourne l'id recu, ou -1
static int reponseId(char* reponse, const char* commande)
{
    char* save;
    if (!reponseOk(reponse, commande, &save))
        return -1;

    char* ptr = strtok_r(NULL, "#", &save);
    if (ptr == NULL)
        return -1;
    return atoi(ptr);
}

/********************OBEP_Login*************************/
bool OBEP_Login(const char* user, const char* password)
{
    char requete[TAILLE_REQUETE];
    char reponse[TAILLE_MAX_DATA + 1];
    char* save;

    if (user == NULL || password == NULL)
        return false;
    if (snprintf(requete, sizeof(requete), "LOGIN#%s#%s#", user, password) >= (int)sizeof(requete))
        return false;

    if (!Echange(requete, reponse))
        return false;

    return reponseOk(reponse, "LOGIN", &save);
}

/**************************OBEP_Logout************************************/
bool OBEP_Logout()
{
    char reponse[TAILLE_MAX_DATA + 1];
    char* save;

    return Echange("LOGOUT", reponse) && reponseOk(reponse, "LOGOUT", &save);
}

/**************************OBEP_Get_nb_authors / OBEP_Get_nb_subjects************************************/
int OBEP_Get_nb_authors()
{
    char reponse[TAILLE_MAX_DATA + 1];

    if (!Echange("GET_AUTHORS_nb#", reponse))
        return -1;
    return reponseId(reponse, "GET_AUTHORS_nb"); // "GET_AUTHORS_nb#ok#<nombre>"
}

int OBEP_Get_nb_subjects()
{
    char reponse[TAILLE_MAX_DATA + 1];

    if (!Echange("GET_SUBJECTS_nb#", reponse))
        return -1;
    return reponseId(reponse, "GET_SUBJECTS_nb"); // "GET_SUBJECTS_nb#ok#<nombre>"
}

/**************************OBEP_Get_authors************************************/
int OBEP_Get_authors(Author authors[], int maxAuthors)
{
    char reponse[TAILLE_MAX_DATA + 1];
    char* save;

    if (!Echange("GET_AUTHORS#", reponse) || !reponseOk(reponse, "GET_AUTHORS", &save))
        return -1;

    char* ptr = strtok_r(NULL, "#", &save);
    if (ptr == NULL)
        return -1;
    int nb = atoi(ptr);

    int i;
    for (i = 0; i < nb && i < maxAuthors; i++)
    {
        char* id        = strtok_r(NULL, "#", &save);
        char* lastName  = strtok_r(NULL, "#", &save);
        char* firstName = strtok_r(NULL, "#", &save);
        if (firstName == NULL)
            return -1;

        authors[i].id = atoi(id);
        snprintf(authors[i].lastName, sizeof(authors[i].lastName), "%s", lastName);
        snprintf(authors[i].firstName, sizeof(authors[i].firstName), "%s", firstName);
    }
    return i;
}

/**************************OBEP_Get_subjects************************************/
int OBEP_Get_subjects(Subject subjects[], int maxSubjects)
{
    char reponse[TAILLE_MAX_DATA + 1];
    char* save;

    if (!Echange("GET_SUBJECTS#", reponse) || !reponseOk(reponse, "GET_SUBJECTS", &save))
        return -1;

    char* ptr = strtok_r(NULL, "#", &save);
    if (ptr == NULL)
        return -1;
    int nb = atoi(ptr);

    int i;
    for (i = 0; i < nb && i < maxSubjects; i++)
    {
        char* id   = strtok_r(NULL, "#", &save);
        char* name = strtok_r(NULL, "#", &save);
        if (name == NULL)
            return -1;

        subjects[i].id = atoi(id);
        snprintf(subjects[i].name, sizeof(subjects[i].name), "%s", name);
    }
    return i;
}

/*********************OBEP_Get_author***************************/
// Recherche d'un auteur et recuperation de son id (pour l'authorId d'un livre)
int OBEP_Get_author(const char* lastName, const char* firstName)
{
    char requete[TAILLE_REQUETE];
    char reponse[TAILLE_MAX_DATA + 1];

    if (snprintf(requete, sizeof(requete), "GET_AUTHOR#%s#%s#", lastName, firstName) >= (int)sizeof(requete))
        return -1;
    if (!Echange(requete, reponse))
        return -1;
    return reponseId(reponse, "GET_AUTHOR");
}

/*********************OBEP_Get_subject***************************/
// Recherche d'un sujet
int OBEP_Get_subject(const char* name)
{
    char requete[TAILLE_REQUETE];
    char reponse[TAILLE_MAX_DATA + 1];

    if (snprintf(requete, sizeof(requete), "GET_SUBJECT#%s#", name) >= (int)sizeof(requete))
        return -1;
    if (!Echange(requete, reponse))
        return -1;
    return reponseId(reponse, "GET_SUBJECT");
}

/**************************OBEP_add_author************************************/
int OBEP_add_author(const char* lastName, const char* firstName, const char* birthday)
{
    char requete[TAILLE_REQUETE];
    char reponse[TAILLE_MAX_DATA + 1];

    if (snprintf(requete, sizeof(requete), "ADD_AUTHOR#%s#%s#%s#", lastName, firstName, birthday) >= (int)sizeof(requete))
        return -1;
    if (!Echange(requete, reponse))
        return -1;
    return reponseId(reponse, "ADD_AUTHOR");
}

/**************************OBEP_add_subject ************************************/
int OBEP_add_subject(const char* name)
{
    char requete[TAILLE_REQUETE];
    char reponse[TAILLE_MAX_DATA + 1];

    if (snprintf(requete, sizeof(requete), "ADD_SUBJECT#%s#", name) >= (int)sizeof(requete))
        return -1;
    if (!Echange(requete, reponse))
        return -1;
    return reponseId(reponse, "ADD_SUBJECT");
}

/**************************OBEP_add_book************************************/
int OBEP_add_book(int authorId,int subjectId,const char* title,const char* isbn,int pageCount,int stockQuantity,float price,int publishYear)
{
    char requete[TAILLE_REQUETE];
    char reponse[TAILLE_MAX_DATA + 1];

    if (snprintf(requete, sizeof(requete), "ADD_BOOK#%d#%d#%s#%s#%d#%d#%.2f#%d#",
                 authorId, subjectId, title, isbn, pageCount, stockQuantity, price, publishYear) >= (int)sizeof(requete))
        return -1;
    if (!Echange(requete, reponse))
        return -1;
    return reponseId(reponse, "ADD_BOOK");
}

/************************MessageArriver()*******************************/
// Notification envoyee spontanement par le serveur a la connexion :
// "WAIT#...#" (en file d'attente), "READY#...#" (pris en charge) ou "BUSY#...#" (file pleine)
bool MessageArriver(char *message, int taille)
{
    char reponse[TAILLE_MAX_DATA + 1];
    char* save;

    if (sClient == -1)
        return false;

    if (Receive(sClient, reponse) <= 0)
    {
        OBEP_close();
        return false;
    }

    char* type = strtok_r(reponse, "#", &save);
    char* contenu = strtok_r(NULL, "#", &save);
    if (type == NULL || contenu == NULL)
        return false;

    if (strcmp(type, "BUSY") == 0 || strcmp(type, "WAIT") == 0 || strcmp(type, "READY") == 0)
    {
        snprintf(message, taille, "%s", contenu);
        return true;
    }
    return false;
}
