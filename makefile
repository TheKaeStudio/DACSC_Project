.SILENT:

all: CreationBD App Serveur Client

App:	main.o mainwindowclientbookencoder.o moc_mainwindowclientbookencoder.o
		g++ -o App main.o mainwindowclientbookencoder.o moc_mainwindowclientbookencoder.o /usr/lib64/libQt5Widgets.so /usr/lib64/libQt5Gui.so /usr/lib64/libQt5Core.so /usr/lib64/libGL.so -lpthread


main.o:	main.cpp
		g++ -c -pipe -g -std=gnu++11 -Wall -W -D_REENTRANT -fPIC -DQT_DEPRECATED_WARNINGS -DQT_QML_DEBUG -DQT_WIDGETS_LIB -DQT_GUI_LIB -DQT_CORE_LIB -I../Administrateur -I. -isystem /usr/include/qt5 -isystem /usr/include/qt5/QtWidgets -isystem /usr/include/qt5/QtGui -isystem /usr/include/qt5/QtCore -I. -I. -I/usr/lib64/qt5/mkspecs/linux-g++ -o main.o main.cpp

mainwindowclientbookencoder.o: mainwindowclientbookencoder.cpp
							   g++ -c -pipe -g -std=gnu++11 -Wall -W -D_REENTRANT -fPIC -DQT_DEPRECATED_WARNINGS -DQT_QML_DEBUG -DQT_WIDGETS_LIB -DQT_GUI_LIB -DQT_CORE_LIB -I../Administrateur -I. -isystem /usr/include/qt5 -isystem /usr/include/qt5/QtWidgets -isystem /usr/include/qt5/QtGui -isystem /usr/include/qt5/QtCore -I. -I. -I/usr/lib64/qt5/mkspecs/linux-g++ -o mainwindowclientbookencoder.o mainwindowclientbookencoder.cpp


moc_mainwindowclientbookencoder.o:	moc_mainwindowclientbookencoder.cpp
							   g++ -c -pipe -g -std=gnu++11 -Wall -W -D_REENTRANT -fPIC -DQT_DEPRECATED_WARNINGS -DQT_QML_DEBUG -DQT_WIDGETS_LIB -DQT_GUI_LIB -DQT_CORE_LIB -I../Administrateur -I. -isystem /usr/include/qt5 -isystem /usr/include/qt5/QtWidgets -isystem /usr/include/qt5/QtGui -isystem /usr/include/qt5/QtCore -I. -I. -I/usr/lib64/qt5/mkspecs/linux-g++ -o moc_mainwindowclientbookencoder.o moc_mainwindowclientbookencoder.cpp


CreationBD:	CreationBD.cpp
			g++ -o CreationBD CreationBD.cpp -I/usr/include/mysql -m64 -L/usr/lib64/mysql -lmysqlclient -lpthread -lz -lm -lrt -lssl -lcrypto -ldl

Serveur:	ServeurEncodage.cpp TCP.o
			g++ ServeurEncodage.cpp TCP.o -o Serveur -I/usr/include/mysql -m64 -L/usr/lib64/mysql -lmysqlclient -lpthread -lz -lm -lrt -lssl -lcrypto -ldl -fpermissive

TCP.o:	TCP.cpp TCP.h
		g++ -c TCP.cpp

Client:	ClientEncodage.cpp TCP.o
		g++ ClientEncodage.cpp TCP.o -o Client -I/usr/include/mysql -m64 -L/usr/lib64/mysql -lmysqlclient -lpthread -lz -lm -lrt -lssl -lcrypto -ldl -fpermissive

clean:	
	rm -r *.o  Serveur App Client

clobber: clean