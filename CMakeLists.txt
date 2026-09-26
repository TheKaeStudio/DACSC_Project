cmake_minimum_required(VERSION 3.10)

# ============================================================
# Projet
# ============================================================

project(AppGestionnaire)

# Standard C++
set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# ============================================================
# Qt5
# ============================================================

find_package(Qt5 COMPONENTS Core Gui Widgets REQUIRED)

# ============================================================
# Répertoires des fichiers .h
# ============================================================

include_directories(
    ${PROJECT_SOURCE_DIR}/includes
    /usr/include/mysql
)

# ============================================================
# Répertoire des bibliothèques MySQL
# ============================================================

link_directories(
    /usr/lib64/mysql
)


# ============================================================
# INTERFACE GRAPHIQUE
# ============================================================

add_executable(App
    sources/main.cpp
    sources/mainwindowclientbookencoder.cpp
    sources/moc_mainwindowclientbookencoder.cpp)

target_include_directories(App PRIVATE
    ${PROJECT_SOURCE_DIR}/includes
)

target_link_libraries(App
    Qt5::Widgets
    Qt5::Gui
    Qt5::Core
)


# ============================================================
# CLIENT ENCODAGE
# ============================================================

add_executable(ClientEncodage
    sources/ClientEncodage.cpp
    sources/TCP.cpp
)

target_link_libraries(ClientEncodage
    Qt5::Core
    Qt5::Gui
    Qt5::Widgets
)

# ============================================================
# SERVEUR ENCODAGE
# ============================================================

add_executable(ServeurEncodage
    sources/ServeurEncodage.cpp
    sources/TCP.cpp
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
# CREATION DE LA BASE DE DONNEES
# ============================================================

add_executable(CreationBD
    sources/CreationBD.cpp
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