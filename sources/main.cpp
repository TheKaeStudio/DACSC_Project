#include "mainwindowclientbookencoder.h"

#include <QApplication>


int main(int argc, char *argv[])
{
    QApplication a(argc, argv);
    MainWindowClientBookEncoder w;

    // Pas d'interface tant que le serveur n'a pas pris le client en charge
    if (!w.serveurPret())
        return 1;

    w.show();
    return a.exec();


}
