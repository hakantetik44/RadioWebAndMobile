Feature:Radio France Uygulamasında Arama
@web
  Scenario: Radio France'da "histoire" araması yapma
    Given Uygulamayı başlatıyorum
    Then Ana sayfada olduğumu doğruluyorum
    When "Recherhe" düğmesine tıklarsam
    And Arama alanına "Histoire" yazarsam
    Then Histoire için sonuçlar gorunmeli
