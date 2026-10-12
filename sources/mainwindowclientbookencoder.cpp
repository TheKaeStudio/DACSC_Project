#include "mainwindowclientbookencoder.h"
#include "ui_mainwindowclientbookencoder.h"
#include "unistd.h"
#include <QInputDialog>
#include <QMessageBox>
#include <iostream>
#include <cstring>
#include "OBEP_Client.h"
#include "Model.h"

using namespace std;

// A modifier pour la demonstration en reseau : adresse IP de la machine du serveur
#define IP_SERVEUR   "127.0.0.1"
#define PORT_SERVEUR 50000

// Tailles maximales des champs (largeur des colonnes en base de donnees)
#define TAILLE_MAX_NOM    20
#define TAILLE_MAX_TITRE  100

MainWindowClientBookEncoder::MainWindowClientBookEncoder(QWidget *parent)
    : QMainWindow(parent)
    , ui(new Ui::MainWindowClientBookEncoder)
    , connecte(false)
{
    ui->setupUi(this);

    // Redirection de stderr vers /dev/null
    freopen("/dev/null", "w", stderr);

    // Configuration de la table des employes (Personnel Garage)
    ui->tableWidgetEncodedBooks->setColumnCount(9);
    ui->tableWidgetEncodedBooks->setRowCount(0);
    QStringList labelsTableEmployes;
    labelsTableEmployes << "Id" << "Titre" << "Auteur" << "Sujet" << "ISBN" << "Pages" << "Année" << "Prix" << "Stock";
    ui->tableWidgetEncodedBooks->setHorizontalHeaderLabels(labelsTableEmployes);
    ui->tableWidgetEncodedBooks->horizontalHeader()->setVisible(true);
    ui->tableWidgetEncodedBooks->horizontalHeader()->setStretchLastSection(true);
    ui->tableWidgetEncodedBooks->verticalHeader()->setVisible(false);
    ui->tableWidgetEncodedBooks->horizontalHeader()->setStyleSheet("background-color: lightyellow");
    int columnWidths[] = {35, 250, 200, 200, 150, 50, 50, 50, 40};
    for (int col = 0; col < 9; ++col)
        ui->tableWidgetEncodedBooks->setColumnWidth(col, columnWidths[col]);

    this->logoutOk();

    // Connexion au serveur : la fenetre n'est affichee (voir main.cpp) qu'une fois le client pris en charge
    connecte = this->connecterAuServeur();
}

bool MainWindowClientBookEncoder::serveurPret() const {
    return connecte;
}

MainWindowClientBookEncoder::~MainWindowClientBookEncoder() {
    OBEP_close();
    delete ui;
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
///// Fonctions utiles Table des livres encodés (ne pas modifier) ////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////
void MainWindowClientBookEncoder::addTupleTableBooks(int id, string title, string author, string subject,
                                                     string isbn,
                                                     int pageCount,
                                                     int publishYear,
                                                     float price,
                                                     int stockQuantity)
{
    int nb = ui->tableWidgetEncodedBooks->rowCount();
    nb++;
    ui->tableWidgetEncodedBooks->setRowCount(nb);
    ui->tableWidgetEncodedBooks->setRowHeight(nb-1,10);

    // id
    QTableWidgetItem *item = new QTableWidgetItem;
    item->setFlags(item->flags() & ~Qt::ItemIsSelectable);
    item->setTextAlignment(Qt::AlignCenter);
    item->setText(QString::number(id));
    ui->tableWidgetEncodedBooks->setItem(nb-1,0,item);

    // title
    item = new QTableWidgetItem;
    item->setFlags(item->flags() & ~Qt::ItemIsSelectable);
    item->setText(QString::fromStdString(title));
    ui->tableWidgetEncodedBooks->setItem(nb-1,1,item);

    // author
    item = new QTableWidgetItem;
    item->setFlags(item->flags() & ~Qt::ItemIsSelectable);
    item->setTextAlignment(Qt::AlignCenter);
    item->setText(QString::fromStdString(author));
    ui->tableWidgetEncodedBooks->setItem(nb-1,2,item);

    // subject
    item = new QTableWidgetItem;
    item->setFlags(item->flags() & ~Qt::ItemIsSelectable);
    item->setTextAlignment(Qt::AlignCenter);
    item->setText(QString::fromStdString(subject));
    ui->tableWidgetEncodedBooks->setItem(nb-1,3,item);

    // isbn
    item = new QTableWidgetItem;
    item->setFlags(item->flags() & ~Qt::ItemIsSelectable);
    item->setTextAlignment(Qt::AlignCenter);
    item->setText(QString::fromStdString(isbn));
    ui->tableWidgetEncodedBooks->setItem(nb-1,4,item);

    // pageCount
    item = new QTableWidgetItem;
    item->setFlags(item->flags() & ~Qt::ItemIsSelectable);
    item->setTextAlignment(Qt::AlignCenter);
    item->setText(QString::number(pageCount));
    ui->tableWidgetEncodedBooks->setItem(nb-1,5,item);

    // publishYear
    item = new QTableWidgetItem;
    item->setFlags(item->flags() & ~Qt::ItemIsSelectable);
    item->setTextAlignment(Qt::AlignCenter);
    item->setText(QString::number(publishYear));
    ui->tableWidgetEncodedBooks->setItem(nb-1,6,item);

    // price
    item = new QTableWidgetItem;
    item->setFlags(item->flags() & ~Qt::ItemIsSelectable);
    item->setTextAlignment(Qt::AlignCenter);
    item->setText(QString::number(price));
    ui->tableWidgetEncodedBooks->setItem(nb-1,7,item);

    // stockQuantity
    item = new QTableWidgetItem;
    item->setFlags(item->flags() & ~Qt::ItemIsSelectable);
    item->setTextAlignment(Qt::AlignCenter);
    item->setText(QString::number(stockQuantity));
    ui->tableWidgetEncodedBooks->setItem(nb-1,8,item);

    
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
void MainWindowClientBookEncoder::clearTableBooks() {
    ui->tableWidgetEncodedBooks->setRowCount(0);
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
///// Fonctions utiles des comboboxes (ne pas modifier) //////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////
void MainWindowClientBookEncoder::addComboBoxAuthors(string author){

    ui->comboBoxAuthors->addItem(QString::fromStdString(author));
}

string MainWindowClientBookEncoder::getSelectionAuthor() const {
    return ui->comboBoxAuthors->currentText().toStdString();
}

void MainWindowClientBookEncoder::clearComboBoxAuthors() {
    ui->comboBoxAuthors->clear();
}

void MainWindowClientBookEncoder::addComboBoxSubjects(string subject){
    ui->comboBoxSubjects->addItem(QString::fromStdString(subject));
}

string MainWindowClientBookEncoder::getSelectionSubject() const {
    return ui->comboBoxSubjects->currentText().toStdString();
}

void MainWindowClientBookEncoder::clearComboBoxSubjects() {
    ui->comboBoxSubjects->clear();
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
///// Fonction utiles de la fenêtre (ne pas modifier) ////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////
string MainWindowClientBookEncoder::getTitle() const {
    return ui->lineEditTitle->text().toStdString();
}

string MainWindowClientBookEncoder::getIsbn() const {
    return ui->lineEditIsbn->text().toStdString();
}

int MainWindowClientBookEncoder::getPageCount() const {
    return ui->spinBoxPageCount->value();
}

float MainWindowClientBookEncoder::getPrice() const {
    return ui->doubleSpinBoxPrice->value();
}

int MainWindowClientBookEncoder::getPublishYear() const {
    return ui->spinBoxPublishYear->value();
}

int MainWindowClientBookEncoder::getStockQuantity() const {
    return ui->spinBoxStockQuantity->value();
}

void MainWindowClientBookEncoder::loginOk() {
    ui->pushButtonClear->setEnabled(true);
    ui->pushButtonAddBook->setEnabled(true);
    ui->pushButtonAddAuthor->setEnabled(true);
    ui->pushButtonAddSubject->setEnabled(true);
    ui->actionLogin->setEnabled(false);
    ui->actionLogout->setEnabled(true);
}

void MainWindowClientBookEncoder::logoutOk() {
    ui->pushButtonClear->setEnabled(false);
    ui->pushButtonAddBook->setEnabled(false);
    ui->pushButtonAddAuthor->setEnabled(false);
    ui->pushButtonAddSubject->setEnabled(false);
    ui->actionLogin->setEnabled(true);
    ui->actionLogout->setEnabled(false);
}
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
///// Fonctions permettant d'afficher des boites de dialogue (ne pas modifier) ///////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////
void MainWindowClientBookEncoder::dialogMessage(const string& title,const string& message) {
   QMessageBox::information(this,QString::fromStdString(title),QString::fromStdString(message));
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
void MainWindowClientBookEncoder::dialogError(const string& title,const string& message) {
   QMessageBox::critical(this,QString::fromStdString(title),QString::fromStdString(message));
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
string MainWindowClientBookEncoder::dialogInputText(const string& title,const string& question) {
    return QInputDialog::getText(this,QString::fromStdString(title),QString::fromStdString(question)).toStdString();
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
int MainWindowClientBookEncoder::dialogInputInt(const string& title,const string& question) {
    return QInputDialog::getInt(this,QString::fromStdString(title),QString::fromStdString(question));
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
///// Fonctions gestion des boutons et items de menu ///////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////

// Connexion au serveur puis attente de la prise en charge :
// notification WAIT (file d'attente), puis READY (pris en charge), ou BUSY (file pleine)
bool MainWindowClientBookEncoder::connecterAuServeur()
{
    if (!OBEP_Connect((char*)IP_SERVEUR, PORT_SERVEUR))
    {
        this->dialogError("Connexion", "Impossible de se connecter au serveur.");
        return false;
    }

    char message[200] = {0};
    if (!MessageArriver(message, sizeof(message)))
    {
        this->dialogError("Serveur", "Aucune notification valide reçue.");
        OBEP_close();
        return false;
    }
    if (strcmp(message, "File pleine") == 0)
    {
        this->dialogError("Serveur", "File pleine, revenez plus tard.");
        OBEP_close();
        return false;
    }
    this->dialogMessage("Serveur", message);

    message[0] = '\0';
    if (!MessageArriver(message, sizeof(message)))
    {
        this->dialogError("Serveur", "Aucune notification valide reçue.");
        OBEP_close();
        return false;
    }
    this->dialogMessage("Serveur", message);

    return true;
}

// Verification d'un champ avant envoi ('#' est le separateur du protocole OBEP)
bool MainWindowClientBookEncoder::champValide(const string& valeur, const string& nomChamp, size_t tailleMax)
{
    if (valeur.empty())
    {
        this->dialogError("Erreur", "Veuillez encoder : " + nomChamp);
        return false;
    }
    if (valeur.size() > tailleMax)
    {
        this->dialogError("Erreur", nomChamp + " trop long (maximum " + to_string(tailleMax) + " caracteres)");
        return false;
    }
    if (valeur.find('#') != string::npos)
    {
        this->dialogError("Erreur", nomChamp + " ne peut pas contenir le caractere #");
        return false;
    }
    return true;
}

// Recupere les auteurs aupres du serveur et remplit la combobox
bool MainWindowClientBookEncoder::chargerAuteurs()
{
    authors.assign(NB_MAX_AUTHORS, Author());
    int nb = OBEP_Get_authors(authors.data(), NB_MAX_AUTHORS);
    if (nb < 0)
    {
        authors.clear();
        return false;
    }
    authors.resize(nb);

    this->clearComboBoxAuthors();
    for (int i = 0; i < nb; i++)
        this->addComboBoxAuthors(string(authors[i].firstName) + " " + authors[i].lastName);
    return true;
}

// Recupere les sujets aupres du serveur et remplit la combobox
bool MainWindowClientBookEncoder::chargerSujets()
{
    subjects.assign(NB_MAX_SUBJECTS, Subject());
    int nb = OBEP_Get_subjects(subjects.data(), NB_MAX_SUBJECTS);
    if (nb < 0)
    {
        subjects.clear();
        return false;
    }
    subjects.resize(nb);

    this->clearComboBoxSubjects();
    for (int i = 0; i < nb; i++)
        this->addComboBoxSubjects(subjects[i].name);
    return true;
}

// Fin de session : retour a l'etat "non connecte" (la connexion avec le serveur reste ouverte)
void MainWindowClientBookEncoder::sessionTerminee()
{
    authors.clear();
    subjects.clear();
    this->clearComboBoxAuthors();
    this->clearComboBoxSubjects();
    this->clearTableBooks();
    this->logoutOk();
}

// Si la connexion avec le serveur est perdue, on le signale et on ferme l'application
bool MainWindowClientBookEncoder::connexionPerdue()
{
    if (OBEP_estConnecte())
        return false;

    this->dialogError("Connexion", "La connexion avec le serveur est perdue. L'application va se fermer.");
    QApplication::quit();
    return true;
}

void MainWindowClientBookEncoder::on_pushButtonAddAuthor_clicked() {
    string lastName = this->dialogInputText("Nouvel auteur","Nom ?");
    if (lastName.empty()) return; // Annuler
    string firstName = this->dialogInputText("Nouvel auteur","Prénom ?");
    if (firstName.empty()) return; // Annuler
    string birthDate = this->dialogInputText("Nouvel auteur","Date de naissance (jj-mm-aaaa) ?");
    if (birthDate.empty()) return; // Annuler

    if (!champValide(lastName, "Nom", TAILLE_MAX_NOM) || !champValide(firstName, "Prénom", TAILLE_MAX_NOM)
        || !champValide(birthDate, "Date de naissance", 10))
        return;

    if (OBEP_add_author(lastName.c_str(), firstName.c_str(), birthDate.c_str()) <= 0)
    {
        if (!connexionPerdue())
            this->dialogError("Ajout d'un auteur", "Auteur déjà existant, date de naissance invalide (jj-mm-aaaa) ou erreur du serveur.");
        return;
    }

    // Mise a jour de la combobox
    if (!chargerAuteurs())
        connexionPerdue();
}

void MainWindowClientBookEncoder::on_pushButtonAddSubject_clicked() {
    string name = this->dialogInputText("Nouveau sujet","Nom ?");
    if (name.empty()) return; // Annuler

    if (!champValide(name, "Nom du sujet", TAILLE_MAX_NOM))
        return;

    if (OBEP_add_subject(name.c_str()) <= 0)
    {
        if (!connexionPerdue())
            this->dialogError("Ajout d'un sujet", "Sujet déjà existant ou erreur du serveur.");
        return;
    }

    // Mise a jour de la combobox
    if (!chargerSujets())
        connexionPerdue();
}

void MainWindowClientBookEncoder::on_pushButtonAddBook_clicked() {
    string title = this->getTitle();
    string isbn = this->getIsbn();
    int pageCount = this->getPageCount();
    float price = this->getPrice();
    int publishYear = this->getPublishYear();
    int stockQuantity = this->getStockQuantity();

    if (!champValide(title, "Titre", TAILLE_MAX_TITRE) || !champValide(isbn, "ISBN", TAILLE_MAX_NOM))
        return;
    if (pageCount <= 0)
    {
        this->dialogError("Erreur", "Veuillez indiquer le nombre de pages.");
        return;
    }
    if (price <= 0)
    {
        this->dialogError("Erreur", "Veuillez indiquer le prix.");
        return;
    }

    // Les ids de l'auteur et du sujet sont ceux recus du serveur (meme position que dans la combobox)
    int iAuthor = ui->comboBoxAuthors->currentIndex();
    int iSubject = ui->comboBoxSubjects->currentIndex();
    if (iAuthor < 0 || iAuthor >= (int)authors.size() || iSubject < 0 || iSubject >= (int)subjects.size())
    {
        this->dialogError("Erreur", "Veuillez sélectionner un auteur et un sujet.");
        return;
    }

    int bookId = OBEP_add_book(authors[iAuthor].id, subjects[iSubject].id, title.c_str(), isbn.c_str(),
                               pageCount, stockQuantity, price, publishYear);
    if (bookId <= 0)
    {
        if (!connexionPerdue())
            this->dialogError("Ajout d'un livre", "Erreur lors de l'ajout du livre (auteur ou sujet inexistant ?).");
        return;
    }

    this->addTupleTableBooks(bookId, title, this->getSelectionAuthor(), this->getSelectionSubject(),
                             isbn, pageCount, publishYear, price, stockQuantity);
}

void MainWindowClientBookEncoder::on_pushButtonClear_clicked() {
    ui->lineEditTitle->clear();
    ui->lineEditIsbn->clear();
    ui->spinBoxPageCount->setValue(0);
    ui->doubleSpinBoxPrice->setValue(0);
    ui->spinBoxPublishYear->setValue(0);
    ui->spinBoxStockQuantity->setValue(0);
}

void MainWindowClientBookEncoder::on_actionLogin_triggered() {
    string login = this->dialogInputText("Entrée en session","Login ?");
    if (login.empty()) return; // Annuler
    string password = this->dialogInputText("Entrée en session","Password ?");
    if (password.empty()) return; // Annuler

    if (!champValide(login, "Login", TAILLE_MAX_NOM) || !champValide(password, "Password", TAILLE_MAX_NOM))
        return;

    if (!OBEP_Login(login.c_str(), password.c_str()))
    {
        if (!connexionPerdue())
            this->dialogError("Connexion", "Login ou mot de passe incorrect.");
        return;
    }

    this->loginOk();

    // Remplissage des deux comboboxes
    if (!chargerAuteurs() || !chargerSujets())
    {
        if (!connexionPerdue())
        {
            this->dialogError("Connexion", "Impossible de récupérer les auteurs et les sujets.");
            OBEP_Logout();
            this->sessionTerminee();
        }
    }
}

void MainWindowClientBookEncoder::on_actionLogout_triggered() {
    if (!OBEP_Logout())
    {
        if (connexionPerdue())
            return;
        this->dialogError("Logout", "Erreur lors de la déconnexion.");
    }
    this->sessionTerminee();
}

void MainWindowClientBookEncoder::on_actionQuitter_triggered(){
    OBEP_close();
    QApplication::exit(0);
}
