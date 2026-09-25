#ifndef OBEP_H 
#define OBEP_H 
#define NB_MAX_CLIENTS 100 
bool OBEP(char* requete, char* reponse,int socket); 
bool OBEP_Login(const char* user,const char* password); 
int  OBEP_
void OBEP_Close(); 
#endif 