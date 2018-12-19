#language: fr
#encoding: utf-8

@ok
Fonctionnalité: Visualiser le reste à faire

  Scénario: Récupérer la liste des clés
    Soit le projet paramétré "default"
    Quand je veux consulter le projet
    Alors je reçois un projet avec les propriétés suivantes :
      | keysets |
      | name    |
    Et les clés ont un état

  Plan du Scénario: Utiliser les tags pour changer l'état
    Soit le projet paramétré "default"
    Et il comporte la clé "<fr>" identifiée par "some.new.key"
    Quand je traduis la clé "some.new.key" par "<en>" en anglais
    Alors je reçois une clé
    Et la clé a pour état "<state>"

    Exemples:
      | fr                | en                | state       |
      | Nouveau [TODO]    | New               | Todo        |
      | Nouveau [TODO]    | New [INPROGRESS]  | Todo        |
      | Nouveau           | New [INPROGRESS]  | InProgress  |