#include "OBEP.h"
#include "TCP.h"
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <pthread.h>
#include <mysql.h>

// Taille maximale acceptee pour un champ texte recu d'un client
#define TAILLE_MAX_CHAMP 100
#define TAILLE_REPONSE   (TAILLE_MAX_DATA + 1)

int clients[NB_MAX_CLIENTS];
int nbclients = 0;

static int  estPresent(int socket);
static bool ajoute(int socket);
static void retire(int socket);

pthread_mutex_t mutexClients = PTHREAD_MUTEX_INITIALIZER;
// Mutex protegeant l'acces a la connexion MySQL (partagee par tous les threads)
pthread_mutex_t mutexBD = PTHREAD_MUTEX_INITIALIZER;
extern MYSQL *connexion;

//***** Fonctions utilitaires *************************************************

static bool champOk(const char* s)
{
	return s != NULL && s[0] != '\0' && strlen(s) <= TAILLE_MAX_CHAMP;
}

// Echappe les caracteres speciaux SQL (dest : 2*TAILLE_MAX_CHAMP+1 octets). mutexBD verrouille par l'appelant
static void echappe(char* dest, const char* src)
{
	mysql_real_escape_string(connexion, dest, src, strlen(src));
}

// Execute un SELECT et retourne son resultat (NULL en cas d'erreur). mutexBD verrouille par l'appelant
static MYSQL_RES* selectSQL(const char* sql)
{
	if (mysql_query(connexion, sql))
	{
		fprintf(stderr, "Erreur mysql : %s\n", mysql_error(connexion));
		return NULL;
	}
	MYSQL_RES* resultat = mysql_store_result(connexion);
	if (resultat == NULL)
		fprintf(stderr, "Erreur mysql : %s\n", mysql_error(connexion));
	return resultat;
}

// Retourne la 1ere colonne de la 1ere ligne du resultat (entier), ou -1 si aucune ligne ou erreur.
// mutexBD verrouille par l'appelant
static int premierEntier(const char* sql)
{
	int valeur = -1;
	MYSQL_RES* resultat = selectSQL(sql);
	if (resultat != NULL)
	{
		MYSQL_ROW ligne = mysql_fetch_row(resultat);
		if (ligne != NULL && ligne[0] != NULL)
			valeur = atoi(ligne[0]);
		mysql_free_result(resultat);
	}
	return valeur;
}

//***** Parsing de la requete et creation de la reponse ***********************

bool OBEP(char* requete, char* reponse, int socket)
{
	// Decoupage de la requete (strtok_r : version de strtok utilisable avec plusieurs threads)
	char *save;
	char *commande = strtok_r(requete, "#", &save);

	if (commande == NULL)
	{
		snprintf(reponse, TAILLE_REPONSE, "ERROR#ko#Requete vide");
		return true;
	}

	// ***** LOGIN ***********************************************************
	if (strcmp(commande, "LOGIN") == 0)
	{
		char *login = strtok_r(NULL, "#", &save);
		char *password = strtok_r(NULL, "#", &save);

		if (!champOk(login) || !champOk(password))
		{
			snprintf(reponse, TAILLE_REPONSE, "LOGIN#ko#Requete invalide");
			return true;
		}
		printf("\t[THREAD %p] LOGIN de %s\n", (void*)pthread_self(), login);

		if (estPresent(socket) >= 0)
		{
			snprintf(reponse, TAILLE_REPONSE, "LOGIN#ko#Client deja logge");
			return true;
		}
		if (OBEP_Login(login, password) && ajoute(socket))
		{
			snprintf(reponse, TAILLE_REPONSE, "LOGIN#ok");
			return true;
		}
		snprintf(reponse, TAILLE_REPONSE, "LOGIN#ko#Mauvais identifiants");
		return true;
	}

	// Toutes les autres commandes necessitent d'etre logge
	if (estPresent(socket) < 0)
	{
		snprintf(reponse, TAILLE_REPONSE, "ERROR#ko#Client non logge");
		return true;
	}

	// ***** LOGOUT **********************************************************
	if (strcmp(commande, "LOGOUT") == 0)
	{
		OBEP_Logout(socket);
		snprintf(reponse, TAILLE_REPONSE, "LOGOUT#ok");
		return true;
	}

	// ***** GET_AUTHORS / GET_SUBJECTS *************************************
	if (strcmp(commande, "GET_AUTHORS") == 0)
	{
		if (!OBEP_GET_AUTHORS(reponse))
			snprintf(reponse, TAILLE_REPONSE, "GET_AUTHORS#ko#Erreur");
		return true;
	}
	if (strcmp(commande, "GET_SUBJECTS") == 0)
	{
		if (!OBEP_GET_SUBJECTS(reponse))
			snprintf(reponse, TAILLE_REPONSE, "GET_SUBJECTS#ko#Erreur");
		return true;
	}

	// ***** GET_AUTHORS_nb / GET_SUBJECTS_nb ********************************
	if (strcmp(commande, "GET_AUTHORS_nb") == 0)
	{
		int nb = OBEP_GET_NB_AUTHORS();
		if (nb >= 0) snprintf(reponse, TAILLE_REPONSE, "GET_AUTHORS_nb#ok#%d", nb);
		else         snprintf(reponse, TAILLE_REPONSE, "GET_AUTHORS_nb#ko#Erreur");
		return true;
	}
	if (strcmp(commande, "GET_SUBJECTS_nb") == 0)
	{
		int nb = OBEP_GET_NB_SUBJECTS();
		if (nb >= 0) snprintf(reponse, TAILLE_REPONSE, "GET_SUBJECTS_nb#ok#%d", nb);
		else         snprintf(reponse, TAILLE_REPONSE, "GET_SUBJECTS_nb#ko#Erreur");
		return true;
	}

	// ***** GET_AUTHOR (un seul auteur) *************************************
	if (strcmp(commande, "GET_AUTHOR") == 0)
	{
		char *lastName = strtok_r(NULL, "#", &save);
		char *firstName = strtok_r(NULL, "#", &save);
		int id = -1;

		if (champOk(lastName) && champOk(firstName))
			id = OBEP_GET_AUTHOR(lastName, firstName);

		if (id > 0) snprintf(reponse, TAILLE_REPONSE, "GET_AUTHOR#ok#%d", id);
		else        snprintf(reponse, TAILLE_REPONSE, "GET_AUTHOR#ko#-1");
		return true;
	}

	// ***** GET_SUBJECT (un seul sujet) *************************************
	if (strcmp(commande, "GET_SUBJECT") == 0)
	{
		char *name = strtok_r(NULL, "#", &save);
		int id = -1;

		if (champOk(name))
			id = OBEP_GET_SUBJECT(name);

		if (id > 0) snprintf(reponse, TAILLE_REPONSE, "GET_SUBJECT#ok#%d", id);
		else        snprintf(reponse, TAILLE_REPONSE, "GET_SUBJECT#ko#-1");
		return true;
	}

	// ***** ADD_AUTHOR ******************************************************
	if (strcmp(commande, "ADD_AUTHOR") == 0)
	{
		char *lastName = strtok_r(NULL, "#", &save);
		char *firstName = strtok_r(NULL, "#", &save);
		char *birthday = strtok_r(NULL, "#", &save);
		int id = -1;

		if (champOk(lastName) && champOk(firstName) && champOk(birthday))
			id = OBEP_ADD_AUTHOR(lastName, firstName, birthday);

		if (id > 0) snprintf(reponse, TAILLE_REPONSE, "ADD_AUTHOR#ok#%d", id);
		else        snprintf(reponse, TAILLE_REPONSE, "ADD_AUTHOR#ko#-1");
		return true;
	}

	// ***** ADD_SUBJECT *****************************************************
	if (strcmp(commande, "ADD_SUBJECT") == 0)
	{
		char *name = strtok_r(NULL, "#", &save);
		int id = -1;

		if (champOk(name))
			id = OBEP_ADD_SUBJECT(name);

		if (id > 0) snprintf(reponse, TAILLE_REPONSE, "ADD_SUBJECT#ok#%d", id);
		else        snprintf(reponse, TAILLE_REPONSE, "ADD_SUBJECT#ko#-1");
		return true;
	}

	// ***** ADD_BOOK ********************************************************
	if (strcmp(commande, "ADD_BOOK") == 0)
	{
		// authorId, subjectId, title, isbn, pageCount, stockQuantity, price, publishYear
		char *champ[8];
		bool complet = true;
		for (int i = 0; i < 8; i++)
		{
			champ[i] = strtok_r(NULL, "#", &save);
			if (champ[i] == NULL) complet = false;
		}
		int id = -1;

		if (complet && champOk(champ[2]) && champOk(champ[3]))
			id = OBEP_ADD_BOOK(atoi(champ[0]), atoi(champ[1]), champ[2], champ[3],
			                   atoi(champ[4]), atoi(champ[5]), (float)atof(champ[6]), atoi(champ[7]));

		if (id > 0) snprintf(reponse, TAILLE_REPONSE, "ADD_BOOK#ok#%d", id);
		else        snprintf(reponse, TAILLE_REPONSE, "ADD_BOOK#ko#-1");
		return true;
	}

	snprintf(reponse, TAILLE_REPONSE, "ERROR#ko#Commande inconnue");
	return true;
}

//***** Acces a la base de donnees ********************************************

bool OBEP_Login(const char* login, const char* password)
{
	char loginEsc[2*TAILLE_MAX_CHAMP+1], passwordEsc[2*TAILLE_MAX_CHAMP+1];
	char sql[600];
	bool trouve = false;

	pthread_mutex_lock(&mutexBD);

	echappe(loginEsc, login);
	echappe(passwordEsc, password);
	snprintf(sql, sizeof(sql), "SELECT id FROM employees WHERE login = '%s' AND password = '%s';",
	         loginEsc, passwordEsc);

	MYSQL_RES* resultat = selectSQL(sql);
	if (resultat != NULL)
	{
		trouve = (mysql_num_rows(resultat) > 0);
		mysql_free_result(resultat);
	}

	pthread_mutex_unlock(&mutexBD);
	return trouve;
}

bool OBEP_Logout(int socket)
{
	if (estPresent(socket) >= 0)
	{
		retire(socket);
		return true;
	}
	return false;
}

// Construit "<commande>#ok#<nb>#<col1>#<col2>#...#<col1>#<col2>..." pour toutes les lignes
static bool listeBD(const char* sql, const char* commande, int nbColonnes, char* reponse)
{
	bool ok = false;

	pthread_mutex_lock(&mutexBD);

	MYSQL_RES* resultat = selectSQL(sql);
	if (resultat != NULL)
	{
		int taille = snprintf(reponse, TAILLE_REPONSE, "%s#ok#%d", commande, (int)mysql_num_rows(resultat));
		ok = true;

		MYSQL_ROW ligne;
		while (ok && (ligne = mysql_fetch_row(resultat)) != NULL)
		{
			for (int c = 0; c < nbColonnes && ok; c++)
			{
				int n = snprintf(reponse + taille, TAILLE_REPONSE - taille, "#%s", ligne[c] ? ligne[c] : "");
				if (n < 0 || taille + n > TAILLE_MAX_DATA) ok = false; // ne rentre pas dans un message
				else taille += n;
			}
		}
		mysql_free_result(resultat);
	}

	pthread_mutex_unlock(&mutexBD);
	return ok;
}

bool OBEP_GET_AUTHORS(char* reponse)
{
	return listeBD("SELECT id, last_name, first_name FROM authors ORDER BY id;", "GET_AUTHORS", 3, reponse);
}

bool OBEP_GET_SUBJECTS(char* reponse)
{
	return listeBD("SELECT id, name FROM subjects ORDER BY id;", "GET_SUBJECTS", 2, reponse);
}

int OBEP_GET_NB_AUTHORS()
{
	pthread_mutex_lock(&mutexBD);
	int nb = premierEntier("SELECT count(*) FROM authors;");
	pthread_mutex_unlock(&mutexBD);
	return nb;
}

int OBEP_GET_NB_SUBJECTS()
{
	pthread_mutex_lock(&mutexBD);
	int nb = premierEntier("SELECT count(*) FROM subjects;");
	pthread_mutex_unlock(&mutexBD);
	return nb;
}

int OBEP_GET_AUTHOR(const char* lastName, const char* firstName)
{
	char lastNameEsc[2*TAILLE_MAX_CHAMP+1], firstNameEsc[2*TAILLE_MAX_CHAMP+1];
	char sql[600];

	pthread_mutex_lock(&mutexBD);

	echappe(lastNameEsc, lastName);
	echappe(firstNameEsc, firstName);
	snprintf(sql, sizeof(sql), "SELECT id FROM authors WHERE last_name = '%s' AND first_name = '%s' ORDER BY id LIMIT 1;",
	         lastNameEsc, firstNameEsc);
	int id = premierEntier(sql);

	pthread_mutex_unlock(&mutexBD);
	return id;
}

int OBEP_GET_SUBJECT(const char* name)
{
	char nameEsc[2*TAILLE_MAX_CHAMP+1];
	char sql[400];

	pthread_mutex_lock(&mutexBD);

	echappe(nameEsc, name);
	snprintf(sql, sizeof(sql), "SELECT id FROM subjects WHERE name = '%s' ORDER BY id LIMIT 1;", nameEsc);
	int id = premierEntier(sql);

	pthread_mutex_unlock(&mutexBD);
	return id;
}

// Verifie que s[debut..fin-1] ne contient que des chiffres
static bool chiffres(const char* s, int debut, int fin)
{
	for (int i = debut; i < fin; i++)
		if (s[i] < '0' || s[i] > '9') return false;
	return true;
}

// Convertit une date "jj-mm-aaaa" (ou "aaaa-mm-jj") en "aaaa-mm-jj", le format attendu par MySQL.
// Retourne false si le format ou la date est invalide. dateSQL doit contenir 11 octets.
static bool dateVersSQL(const char* texte, char* dateSQL)
{
	int j, m, a;

	if (strlen(texte) != 10)
		return false;

	if (texte[2] == '-' && texte[5] == '-' && chiffres(texte, 0, 2) && chiffres(texte, 3, 5) && chiffres(texte, 6, 10))
	{
		// jj-mm-aaaa
		j = atoi(texte);
		m = atoi(texte + 3);
		a = atoi(texte + 6);
	}
	else if (texte[4] == '-' && texte[7] == '-' && chiffres(texte, 0, 4) && chiffres(texte, 5, 7) && chiffres(texte, 8, 10))
	{
		// aaaa-mm-jj
		a = atoi(texte);
		m = atoi(texte + 5);
		j = atoi(texte + 8);
	}
	else
		return false;

	int joursParMois[] = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
	bool bissextile = (a % 4 == 0 && a % 100 != 0) || a % 400 == 0;

	if (a < 1000 || m < 1 || m > 12)
		return false;
	int joursMax = joursParMois[m - 1] + ((m == 2 && bissextile) ? 1 : 0);
	if (j < 1 || j > joursMax)
		return false;

	sprintf(dateSQL, "%04d-%02d-%02d", a, m, j);
	return true;
}

int OBEP_ADD_AUTHOR(const char* lastName, const char* firstName, const char* birthday)
{
	char lastNameEsc[2*TAILLE_MAX_CHAMP+1], firstNameEsc[2*TAILLE_MAX_CHAMP+1];
	char dateSQL[11];
	char sql[600];
	int id = -1;

	if (!dateVersSQL(birthday, dateSQL))
		return -1;

	pthread_mutex_lock(&mutexBD);

	echappe(lastNameEsc, lastName);
	echappe(firstNameEsc, firstName);

	// L'auteur existe-t-il deja ?
	snprintf(sql, sizeof(sql), "SELECT id FROM authors WHERE last_name = '%s' AND first_name = '%s';",
	         lastNameEsc, firstNameEsc);
	MYSQL_RES* resultat = selectSQL(sql);
	if (resultat != NULL)
	{
		if (mysql_num_rows(resultat) == 0)
		{
			snprintf(sql, sizeof(sql), "INSERT INTO authors (last_name, first_name, birth_date) VALUES ('%s', '%s', '%s');",
			         lastNameEsc, firstNameEsc, dateSQL);
			if (mysql_query(connexion, sql) == 0)
				id = (int)mysql_insert_id(connexion);
			else
				fprintf(stderr, "Erreur mysql : %s\n", mysql_error(connexion));
		}
		mysql_free_result(resultat);
	}

	pthread_mutex_unlock(&mutexBD);
	return id;
}

int OBEP_ADD_SUBJECT(const char* name)
{
	char nameEsc[2*TAILLE_MAX_CHAMP+1];
	char sql[400];
	int id = -1;

	pthread_mutex_lock(&mutexBD);

	echappe(nameEsc, name);

	// Le sujet existe-t-il deja ?
	snprintf(sql, sizeof(sql), "SELECT id FROM subjects WHERE name = '%s';", nameEsc);
	MYSQL_RES* resultat = selectSQL(sql);
	if (resultat != NULL)
	{
		if (mysql_num_rows(resultat) == 0)
		{
			snprintf(sql, sizeof(sql), "INSERT INTO subjects (name) VALUES ('%s');", nameEsc);
			if (mysql_query(connexion, sql) == 0)
				id = (int)mysql_insert_id(connexion);
			else
				fprintf(stderr, "Erreur mysql : %s\n", mysql_error(connexion));
		}
		mysql_free_result(resultat);
	}

	pthread_mutex_unlock(&mutexBD);
	return id;
}

int OBEP_ADD_BOOK(int authorId,int subjectId,const char* title,const char* isbn,int pageCount,int stockQuantity,float price,int publishYear)
{
	char titleEsc[2*TAILLE_MAX_CHAMP+1], isbnEsc[2*TAILLE_MAX_CHAMP+1];
	char sql[800];
	int id = -1;

	pthread_mutex_lock(&mutexBD);

	echappe(titleEsc, title);
	echappe(isbnEsc, isbn);

	// Le livre existe-t-il deja (meme titre, meme isbn, meme auteur) ? Si oui, on retourne son id
	snprintf(sql, sizeof(sql), "SELECT id FROM books WHERE title = '%s' AND isbn = '%s' AND author_id = %d ORDER BY id LIMIT 1;",
	         titleEsc, isbnEsc, authorId);
	id = premierEntier(sql);

	if (id == -1)
	{
		// Les cles etrangeres (author_id, subject_id) font echouer l'insertion
		// si l'auteur et/ou le sujet n'existent pas
		snprintf(sql, sizeof(sql),
		         "INSERT INTO books (author_id, subject_id, title, isbn, page_count, stock_quantity, price, publish_year) "
		         "VALUES (%d, %d, '%s', '%s', %d, %d, %.2f, %d);",
		         authorId, subjectId, titleEsc, isbnEsc, pageCount, stockQuantity, price, publishYear);

		if (mysql_query(connexion, sql) == 0)
			id = (int)mysql_insert_id(connexion);
		else
			fprintf(stderr, "Erreur mysql : %s\n", mysql_error(connexion));
	}

	pthread_mutex_unlock(&mutexBD);
	return id;
}

// Fermeture des sockets des clients connectes (arret du serveur)
void OBEP_Close()
{
	for (int i = 0; i < nbclients; i++)
		close(clients[i]);
}

//***** Gestion de l'etat du protocole (clients logges) ************************

// Position du client dans la liste (mutexClients verrouille par l'appelant)
static int indice(int socket)
{
	for (int i = 0; i < nbclients; i++)
		if (clients[i] == socket) return i;
	return -1;
}

static int estPresent(int socket)
{
	pthread_mutex_lock(&mutexClients);
	int pos = indice(socket);
	pthread_mutex_unlock(&mutexClients);
	return pos;
}

static bool ajoute(int socket)
{
	bool ok = false;
	pthread_mutex_lock(&mutexClients);
	if (nbclients < NB_MAX_CLIENTS)
	{
		clients[nbclients] = socket;
		nbclients++;
		ok = true;
	}
	pthread_mutex_unlock(&mutexClients);
	return ok;
}

static void retire(int socket)
{
	pthread_mutex_lock(&mutexClients);
	int pos = indice(socket);
	if (pos != -1)
	{
		for (int i = pos; i < nbclients - 1; i++)
			clients[i] = clients[i+1];
		nbclients--;
	}
	pthread_mutex_unlock(&mutexClients);
}
