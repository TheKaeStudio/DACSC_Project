#ifndef OBEP_H
#define OBEP_H

#include <stdbool.h>
#define NB_MAX_CLIENTS 100

// Traite une requete et construit la reponse.
// Retourne false si la connexion avec le client doit etre fermee, true sinon.
bool OBEP(char* requete, char* reponse, int socket);

bool OBEP_Login(const char* login, const char* password);
bool OBEP_Logout(int socket);

// Construisent la reponse complete ("GET_AUTHORS#ok#nb#id#nom#prenom#...")
// Retournent false en cas d'erreur.
bool OBEP_GET_AUTHORS(char* reponse);
bool OBEP_GET_SUBJECTS(char* reponse);

// Nombre d'auteurs / de sujets en base, ou -1 en cas d'erreur
int OBEP_GET_NB_AUTHORS();
int OBEP_GET_NB_SUBJECTS();

// Recherche d'un auteur / d'un sujet : retournent son id, ou -1 s'il n'existe pas
int OBEP_GET_AUTHOR(const char* lastName, const char* firstName);
int OBEP_GET_SUBJECT(const char* name);

// Retournent l'id cree, ou -1 (deja existant / erreur)
// La date de naissance s'ecrit "jj-mm-aaaa" (ou "aaaa-mm-jj")
int OBEP_ADD_AUTHOR(const char* lastName, const char* firstName, const char* birthday);
int OBEP_ADD_SUBJECT(const char* name);
// Retourne l'id du livre (existant ou ajoute), ou -1 si l'auteur et/ou le sujet n'existent pas
int OBEP_ADD_BOOK(int authorId,int subjectId,const char* title,const char* isbn,int pageCount,int stockQuantity,float price,int publishYear);

void OBEP_Close();

#endif
