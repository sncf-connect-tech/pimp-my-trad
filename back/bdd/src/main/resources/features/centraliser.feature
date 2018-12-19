#language: fr
#encoding: utf-8

@ok
Fonctionnalité: Centraliser les changements

  Scénario: Importer un projet depuis un repo git
    Quand je créée un projet depuis git
    Alors je reçois un nouveau projet avec les propriétés suivantes :
      | keysets |
      | name    |

  Scénario: Conflits
    Soit le projet paramétré "default"
    Et quelqu'un d'autre a modifié les traductions
    Et il comporte la clé "Salutations" identifiée par "wording.hello"
    Quand je synchronise le projet
    Alors il y a des conflits

  Scénario: Etat Conflict
    Soit le projet paramétré "default"
    Et quelqu'un d'autre a modifié les traductions
    Et le projet est en conflit
    Quand je récupère la clé identifiée par "wording.hello"
    Alors la clé a pour état "Conflict"

  Scénario: Résolution de conflits
    Soit le projet paramétré "default"
    Et quelqu'un d'autre a modifié les traductions
    Et le projet est en conflit
    Quand je traduis la clé "wording.hello" par "Salutations" en français
    Alors la clé a pour état "Done"

  Scénario: Portée de la résolution
    Soit le projet paramétré "default"
    Et quelqu'un d'autre a modifié les traductions
    Et le projet est en conflit
    Et je corrige "wording.hello" par "Salutations" en français
    Quand je récupère la clé identifiée par "wording.goodbye"
    Alors la clé a pour état "Conflict"