#ifndef OBEP_Client_H
#define OBEP_Client_H

#include <stdbool.h>
#include "Model.h"

void Echange(char* requete, char* reponse); 
bool OBEP_Connect(char* ip, int port);
bool OBEP_Login(const char* user,const char* password);
bool OBEP_Logout();
int OBEP_Get_authors(Author authors[], int maxAuthors);

int OBEP_Get_subjets(Subject subjects[], int maxSubjects);

int OBEP_add_author (const char* lastName, const char* firstName);

int OBEP_add_subjet (const char* nom);

int OBEP_add_book (int authorId,int subjectId,const char* title, const char* isbn, int pageCount,int stockQuantity, float price,int publishYear);
void OBEP_close();


#endif