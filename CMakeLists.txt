cmake_minimum_required(VERSION 3.10)

# ============================================================
# PROJET
# ============================================================

project(AppGestionnaire)

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# ============================================================
# Qt5
# ============================================================

find_package(Qt5 COMPONENTS Core Gui Widgets REQUIRED)

# ============================================================
# RÉPERTOIRES DES HEADERS
# ============================================================

include_directories(
    ${PROJECT_SOURCE_DIR}/includes
    /usr/include/mysql
)

# ============================================================
# RÉPERTOIRE DES BIBLIOTHÈQUES
# ============================================================

link_directories(
    /usr/lib64/mysql
)

# ============================================================
# CLIENT QT
# ============================================================

add_executable(App

    # Programme principal Qt
    sources/main.cpp

    # Fenêtre principale
    sources/mainwindowclientbookencoder.cpp

    # MOC généré pour Qt
    sources/moc_mainwindowclientbookencoder.cpp

    # OBEP côté client
    sources/OBEP_Client.cpp

    # TCP
    sources/TCP.cpp
)

target_include_directories(App PRIVATE
    ${PROJECT_SOURCE_DIR}/includes
)

target_link_libraries(App
    Qt5::Widgets
    Qt5::Gui
    Qt5::Core
)

# ============================================================
# SERVEUR ENCODAGE
# ============================================================

add_executable(ServeurEncodage

    sources/ServeurEncodage.cpp
    sources/TCP.cpp
    sources/OBEP.cpp
)

target_include_directories(ServeurEncodage PRIVATE
    ${PROJECT_SOURCE_DIR}/includes
    /usr/include/mysql
)

target_link_libraries(ServeurEncodage

    mysqlclient
    pthread
    z
    m
    rt
    ssl
    crypto
    dl
)

# ============================================================
# CRÉATION DE LA BASE DE DONNÉES
# ============================================================

add_executable(CreationBD

    sources/CreationBD.cpp
)

target_include_directories(CreationBD PRIVATE
    ${PROJECT_SOURCE_DIR}/includes
    /usr/include/mysql
)

target_link_libraries(CreationBD

    mysqlclient
    pthread
    z
    m
    rt
    ssl
    crypto
    dl
)