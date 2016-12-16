# SCI -- TP Petit Monde et Négociations (JADE)
#### Tristan Camus & Arnaud Cojez

________________
## Instructions
* [Téléchargez Jade ici](http://jade.tilab.com/dl.php?file=JADE-bin-4.4.0.zip) ;
* Dézipper l'archive et placer le dossier ```lib``` obtenu à la racine du présent dossier;
* Pour lancer le programme : ```$ make execute```
* Un réseau petit monde de ```Neighbours``` est généré automatiquement, ainsi qu'un ```Observer```.
* Afin d'agir sur le réseau, il faut :
  * Créer un ```Dummy Agent``` ;
  * Ajouter l'agent ```Observer``` en tant que Receiver ;
  * Envoyer un message de type ```request```, contenant l'un des mots clés suivants :
    * pour lister les Neighbours et leur inventaire : ```info``` ;
    * pour lancer un tour de négociations entre Neighbours : ```trade``` ;
    * pour lancer une boucle de négociations : ```start```.


________________
## Explications

#### Réseau Petit Monde

Le réseau d'accointance généré suit les caractéristiques d'un Réseau Petit Monde.
Il est généré en suivant l'algorithme de [Watts et Strogatz](https://en.wikipedia.org/wiki/Watts_and_Strogatz_model#Algorithm).

Les propriétés choisies ici sont :
  * Nombre de nœuds n = 10 ;
  * Degré d'un nœeud k = 4 ;
  * Probabilité de recâblage B = 0.2.

Pour une lecture et vérification simplifiée, l'algorithme utilisé dans la classe ```GenerationPetitMonde``` est également implémenté dans le fichier ```smallworld.py```.

#### Neighbour

Le réseau est composé de n agents ```Neighbours```. Ces agents possèdent un comportement TradingBehaviour, dont le but est d'échanger des oranges et des tomates afin d'atteindre un nombre de tomates et d'oranges désirées.

Ces agents réagissent à différents types de messages :
  * PROPOSE :
    * ```orange``` permet de proposer une orange à un Neighbour ;
    * ```tomato``` permet de proposer une tomate à un Neighbour ;
  * ACCEPT_PROPOSAL :
    * ```orange``` est renvoyé si l'orange proposée est acceptée ;
    * ```tomato``` est renvoyé si la tomate proposée est acceptée ;
  * REJECT_PROPOSAL :
    * ```orange```  est renvoyé si l'orange proposée est refusée;
    * ```tomato```  est renvoyé si la tomate proposée est refusée ;
  * REQUEST :
    * ```start``` permet au Neighbour de démarrer ses négociations ;
    * ```trade``` permet au Neighbour de faire un tour de négociation ;
    * ```list``` permet de lister les voisins du Neighbour ;
    * ```inv``` permet de lister l'inventaire du Neighbour.

Lors d'un tour de négociation, les agents vont envoyer des PROPOSE orange et/ou tomato (en fonction de leurs stocks) à tous leurs voisins.
Quand un agent reçoit un PROPOSE [fruit], il va renvoyer un ACCEPT_PROPOSAL si il veut prendre ce fruit. Dans ce cas, il va incrémenter son compteur du fruit concerné.
L'agent "offreur" reçoit ensuite le ACCEPT_PROPOSAL, il décrémente alors son compteur.

Si le fruit proposé n'intéresse pas l'agent, il renvoie un REJECT_PROPOSAL, et rien ne se passe.

Un agent arrête de négocier :
* quand il est satisfait (autant de fruits en stock que de fruits désirés)
* quand il a essuyé 20 refus (valeur arbitraire)
