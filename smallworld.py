from random import *
from math import *

def get_free_cells(liste):
    res = []
    for i in range(len(liste)):
        if(not liste[i]):
            res.append(i)
    return res

k = 4
n = 10
B = 0.1
liste_noeuds = []

if n < k or k < log(n) or log(n) < 1:
    raise Exception("param = n noeuds, degre moyen k, et B tq : 0 <= B <= 1 et n >> k >> ln(n) >> 1")

#cree noeuds
for i in range(n):
    liste_noeuds.append([False]*n)

#cree liens anneau
#connecte chaque noeud a k/2 voisins de chaque cote
for i in range(n):
    for j in range(1, floor(k/2 +1)):
        liste_noeuds[i][(i+j) % n] = True
        liste_noeuds[(i+j) % n][i] = True


for i in range(n):
    for j in range(i+1, n):
        if liste_noeuds[i][j] and random() < B:
            liste_noeuds[i][j] = False
            liste_noeuds[j][i] = False

            free_cells = get_free_cells(liste_noeuds[i])
            free_cells.remove(i)
            shuffle(free_cells)
            target_cell = free_cells[0]
            liste_noeuds[i][target_cell] = True
            liste_noeuds[target_cell][i] = True

print(liste_noeuds)

for i in range(n):
    print(i, "===")
    for j in range(n):
        if(liste_noeuds[i][j]):
            print(j)
