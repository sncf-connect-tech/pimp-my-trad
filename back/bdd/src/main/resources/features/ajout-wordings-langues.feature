#language: fr
#encoding: utf-8

@ok
Fonctionnalité: Ajout de wordings et langues
  
  Scénario: Ajouter un fichier de wordings
    Soit le projet non paramétré "new-project"
    Quand j'ajoute le fichier "fr.json" pour la langue française à un nouveau keyset
    Alors je reçois un nouveau keyset avec les propriétés suivantes :
      | supportedLanguages |
      | name               |
      | keys               |
    Et la clé "wording.hello" a pour traduction française : Bonjour

  Scénario: Lire un wording
    Soit le projet paramétré "default"
    Et il comporte la clé "Clé toute neuve" identifiée par "totally.new.key"
    Et la clé est traduite par "Totally new key" en anglais
    Quand je récupère la clé identifiée par "totally.new.key"
    Alors je reçois une clé avec les propriétés suivantes :
      | translations |
      | state        |
    Et la clé a pour traduction anglaise : Totally new key

  Scénario: Ajouter un wording
    Soit le projet paramétré "default"
    Quand j'écris une clé "Clé toute neuve" identifiée par "totally.new.key"
    Alors je reçois une clé
    Et la clé a pour traduction française : Clé toute neuve

  Scénario: Modifier un wording existant
    Soit le projet paramétré "default"
    Et il comporte la clé "Coucou" identifiée par "wording.hello"
    Quand j'écris une clé "Salut" identifiée par "wording.hello"
    Alors je reçois une clé
    Et la clé a pour traduction française : Salut

  Scénario: Ajouter une langue
    Soit le projet paramétré "default"
    Quand j'ajoute le fichier "it.json" pour la langue italienne
    Alors je reçois un keyset
    Et le keyset prend en charge la langue italienne
    Et la clé "wording.hello" n'est pas traduite en italien