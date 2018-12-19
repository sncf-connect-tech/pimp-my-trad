#language: fr
#encoding: utf-8

@ok
Fonctionnalité: Export et import de traductions

  Scénario: Exporter des traductions
    Soit le projet paramétré "default"
    Quand j'exporte ce projet
    Alors je reçois un export avec les propriétés suivantes :
      | metadata |
      | exported |
    Et l'export CSV vaut :
      """﻿﻿
      ﻿﻿﻿Une cle todo
      """
    
  Scénario: Importer des traductions
    Soit le projet paramétré "default"
    Et un export du projet
    Quand j'importe des traductions pour la langue anglaise
    Alors je reçois des keysets
    Et la clé "a.todo.key" a pour traduction en anglais : Imported translation