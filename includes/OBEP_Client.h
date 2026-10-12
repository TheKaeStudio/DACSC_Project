#ifndef OBEP_Client_H
#define OBEP_Client_H

#include <stdbool.h>
#include "Model.h"

bool OBEP_Connect(char* ip, int port);
void OBEP_close();
bool OBEP_estConnecte();

bool OBEP_Login(const char* user,const char* password);
bool OBEP_Logout();

// Retournent le nombre d'auteurs / de sujets en base, ou -1 en cas d'erreur
int OBEP_Get_nb_authors();
int OBEP_Get_nb_subjects();

// Retournent le nombre d'elements recus, ou -1 en cas d'erreur
int OBEP_Get_authors(Author authors[], int maxAuthors);
int OBEP_Get_subjects(Subject subjects[], int maxSubjects);

// Recherche d'un auteur et recuperation de son id (pour l'auteurId d'un livre) ; -1 si inexistant
int OBEP_Get_author(const char* lastName, const char* firstName);

// Recherche d'un sujet et recuperation de son id ; -1 si inexistant
int OBEP_Get_subject(const char* name);

// Retournent l'id cree, ou -1 (deja existant / erreur)
// La date de naissance s'ecrit "jj-mm-aaaa"
int OBEP_add_author(const char* lastName, const char* firstName, const char* birthday);
int OBEP_add_subject(const char* name);
int OBEP_add_book(int authorId,int subjectId,const char* title,const char* isbn,int pageCount,int stockQuantity,float price,int publishYear);

// Lit une notification du serveur envoyee a la connexion :
//   "WAIT" (socket en file d'attente), "READY" (prise en charge par un thread)
//   ou "BUSY" (file pleine : "File pleine").
// Copie le texte de la notification dans message. Retourne false si rien de valide n'a ete recu.
bool MessageArriver(char *message, int taille);

#endif
