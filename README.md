Rapport – Jeu de Morpion Multijoueur

1. Architecture du système

Serveur :

Gère toute la logique du jeu.

Héberge une grille partagée (3x3) avec états ('-', 'X', 'O').

Utilise des sockets TCP (ServerSocket) et un thread par client.

Assure l'alternance de tour et la validation des coups.

Clients :

Interface graphique Java Swing (JFrame + JButton).

Se connectent au serveur et envoient leurs coups.

Reçoivent en temps réel l’état du jeu et mettent à jour leur interface.


2. Représentation des données échangées

Les messages échangés via les sockets suivent un protocole texte simple :

Serveur → Client :
CONNECTED:x : identifiant joueur (0 pour X, 1 pour O).

STATE\nligne1\nligne2\nligne3\nTURN:x :

3 lignes de grille (ex: XOX, --O, ---)

TURN:x indique le joueur actif.

Client → Serveur :
PLAY:x:y : tentative de jouer en position (x, y).


3. Politique de gestion des conflits

Le serveur :

Refuse automatiquement les coups :

Joués hors de son tour

En dehors de la grille

Sur une case déjà occupée

Le client :

Désactive les boutons non valides selon l’état et le tour reçu.

Il n’y a pas de conflit entre joueurs, car le serveur garde autorité sur les règles.


4. Synchronisation de l’état de jeu

À chaque action valide :

Le serveur met à jour la grille

Il envoie un message STATE à tous les clients

Chaque client lit et affiche instantanément la nouvelle grille et le nouveau tour.

Le protocole est push + pull :

Le client “push” une commande (PLAY)

Le serveur “push” le nouvel état aux deux clients


5. Multithreading

Serveur :

Chaque joueur est géré dans un thread séparé (classe ClientHandler)

Synchronisation assurée via synchronized sur play() et broadcastState()

Clients :

Un thread principal gère l’interface

Un thread secondaire lit les messages serveur en continu


Stack technique
Langage : Java

Sockets : TCP

UI : Swing (client)

Multithreading : Thread, synchronized


6. Exécution

Lancement du serveur :
# javac Serveur.java Client.java
# java Serveur

Dans deux autres terminaux séparés, les clients :
# java Client
# java Client

Enfin, pour supprimer les class si nécessaire (facultatif puisque les class sont nécessaires à chaque lancement du jeu):
# rm *.class