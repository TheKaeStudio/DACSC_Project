#ifndef OBEP_H
#define OBEP_H

#include <stdbool.h>
#include "Model.h"
#define NB_MAX_CLIENTS 100
#define NB_MAX_AUTHORS 100
#define NB_MAX_SUBJECTS 100



bool OBEP(char* requete, char* reponse, int socket);

bool OBEP_Login(const char* identifiant, const char* password);

bool OBEP_Logout(int socket);

int OBEP_GET_AUTHORS(Author authors[], int maxAuthors);

int OBEP_GET_SUBJECTS(Subject subjects[], int maxSubjects);

int OBEP_ADD_AUTHOR(const char* lastName, const char* firstName);

int OBEP_ADD_SUBJECT(const char* nom);

int OBEP_ADD_BOOK(int authorId,int subjectId,const char* title, const char* isbn, int pageCount,int stockQuantity, float price,int publishYear);
void OBEP_Close();

#endif