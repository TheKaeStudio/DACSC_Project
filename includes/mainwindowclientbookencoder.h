#ifndef MAINWINDOWCLIENTBOOKENCODER_H
#define MAINWINDOWCLIENTBOOKENCODER_H

#include <QMainWindow>
#include <string>
#include <vector>
#include "Model.h"
using namespace std;

QT_BEGIN_NAMESPACE
namespace Ui { class MainWindowClientBookEncoder; }
QT_END_NAMESPACE

class MainWindowClientBookEncoder : public QMainWindow
{
    Q_OBJECT

public:
    MainWindowClientBookEncoder(QWidget *parent = nullptr);
    ~MainWindowClientBookEncoder();

    void addTupleTableBooks(int id,string title,string author,string subject,string isbn,int pageCount,int publishYear,float price,int stockQuantity);
    void clearTableBooks();

    void addComboBoxAuthors(string author);
    string getSelectionAuthor() const;
    void clearComboBoxAuthors();
    void addComboBoxSubjects(string subject);
    string getSelectionSubject() const;
    void clearComboBoxSubjects();

    string getTitle() const;
    string getIsbn() const;
    int getPageCount() const;
    float getPrice() const;
    int getPublishYear() const;
    int getStockQuantity() const;

    void dialogMessage(const string& title,const string& message);
    void dialogError(const string& title,const string& message);
    string dialogInputText(const string& title,const string& question);
    int dialogInputInt(const string& title,const string& question);

    void loginOk();
    void logoutOk();

    bool serveurPret() const;

private slots:
    void on_pushButtonAddAuthor_clicked();
    void on_pushButtonAddSubject_clicked();
    void on_pushButtonAddBook_clicked();
    void on_pushButtonClear_clicked();
    void on_actionLogin_triggered();
    void on_actionLogout_triggered();
    void on_actionQuitter_triggered();

private:
    Ui::MainWindowClientBookEncoder *ui;

    // Auteurs et sujets recus du serveur (meme ordre que dans les comboboxes : on y retrouve les ids)
    vector<Author> authors;
    vector<Subject> subjects;

    bool connecte;   // true une fois le client pris en charge par le serveur

    bool connecterAuServeur();
    bool champValide(const string& valeur, const string& nomChamp, size_t tailleMax);
    bool chargerAuteurs();
    bool chargerSujets();
    bool connexionPerdue();
    void sessionTerminee();
};
#endif // MAINWINDOWCLIENTBOOKENCODER_H
