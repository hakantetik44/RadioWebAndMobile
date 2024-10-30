Feature: Recherche dans l'application Radio France

  Scenario: Effectuer une recherche de "histoire" dans Radio France
    Given Je lance l'application
    Then Je vérifie que je suis sur la page d'accueil
    When Je clique sur le bouton "Recherche"
    And Je saisis "Histoire" dans le champ de recherche
    Then Les résultats pour "Histoire" doivent être affichés
