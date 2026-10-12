#ifndef OBEP_Client_H
#define OBEP_Client_H

#include <stdbool.h>
#include "Model.h"

void Echange(char* requete, char* reponse); 

bool OBEP_Connect(char* ip, int port);

bool OBEP_Login(const char* user,const char* password);

bool OBEP_Logout();

int OBEP_Get_nb_authors();

int OBEP_Get_nb_subjects();

int OBEP_Get_authors(Author authors[], int maxAuthors);

int OBEP_Get_subjects(Subject subjects[], int maxSubjects);

///////////////////////////////////////////////////////////////////
//recherche d'un auteur et recuperation de son id pour l'inseré dans la auteurId du book
int OBEP_Get_author(const char * lastName, const char* firstName);

//recherche d'un sujet
int OBEP_Get_subject(const char * name);

////////////////////////////////////////////////////////////////////

//int OBEP_add_author (const char* lastName, const char* firstName);

int OBEP_add_author (const char* lastName, const char* firstName, const char * birthday);

int OBEP_add_subject (const char* nom);

int OBEP_add_book (int authorId,int subjectId,const char* title, const char* isbn, int pageCount,int stockQuantity, float price,int publishYear);
void OBEP_close();

bool MessageArriver(char *message, int taille);


#endif