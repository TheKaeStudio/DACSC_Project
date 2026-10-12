#ifndef TCP_H
#define TCP_H

// Taille maximale d'un message (sans l'entete de 4 caracteres).
// Les buffers passes a Receive doivent donc faire TAILLE_MAX_DATA + 1 octets.
#define TAILLE_MAX_DATA 10000

// Cree la socket d'ecoute sur le port donne. Retourne la socket ou -1.
int ServerSocket(int port);

// Attend un client. Retourne la socket de service ou -1.
// Si ipClient n'est pas NULL, il recoit l'adresse IP du client (>= 46 octets).
int Accept(int sEcoute,char *ipClient);

// Se connecte au serveur. Retourne la socket ou -1.
int ClientSocket(char* ipServeur,int portServeur);

// Envoie "taille" octets de data (1 <= taille <= TAILLE_MAX_DATA).
// Retourne taille, ou -1 en cas d'erreur.
int Send(int sSocket,const char* data,int taille);

// Recoit un message complet dans data (data est termine par '\0').
// Retourne la taille du message, 0 si le correspondant a ferme la connexion,
// -1 en cas d'erreur.
int Receive(int sSocket,char* data);

#endif
