#ifndef MODEL_H
#define MODEL_H

// Nombre maximal d'auteurs / de sujets recus du serveur
#define NB_MAX_AUTHORS  500
#define NB_MAX_SUBJECTS 500

typedef struct {
    int id;
    char lastName[100];
    char firstName[100];
} Author;

typedef struct {
    int id;
    char name[100];
} Subject;

typedef struct {
    int id;
    int authorId;
    int subjectId;
    char title[200];
    char isbn[30];
    int pageCount;
    int stockQuantity;
    float price;
    int publishYear;
} Book;

#endif
