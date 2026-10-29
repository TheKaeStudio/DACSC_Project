-- ================================================================
-- Tables propres au serveur Achat (à exécuter après CreationBD)
-- Les tables books / authors / subjects / employees ne sont PAS touchées.
--   mysql -u Student -p PourStudent < queriesSQL.sql
-- ================================================================

-- l'ordre compte à cause des clés étrangères
DROP TABLE IF EXISTS caddy_items;
DROP TABLE IF EXISTS caddies;
DROP TABLE IF EXISTS clients;

CREATE TABLE clients (
    id      INT AUTO_INCREMENT PRIMARY KEY,
    nom     VARCHAR(100) NOT NULL,
    prenom  VARCHAR(100) NOT NULL,
    adresse VARCHAR(255)
);

CREATE TABLE caddies (
    id       INT AUTO_INCREMENT PRIMARY KEY,
    clientId INT NOT NULL,
    `date`   DATE NOT NULL,
    amount   DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    payed    BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_caddies_client FOREIGN KEY (clientId) REFERENCES clients(id)
);

CREATE TABLE caddy_items (
    id       INT AUTO_INCREMENT PRIMARY KEY,
    caddyId  INT NOT NULL,
    bookId   INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    CONSTRAINT fk_caddy_items_caddy FOREIGN KEY (caddyId) REFERENCES caddies(id),
    CONSTRAINT fk_caddy_items_book  FOREIGN KEY (bookId)  REFERENCES books(id),
    CONSTRAINT chk_caddy_items_quantity CHECK (quantity > 0)
);
