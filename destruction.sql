-- Suppression de l'ensemble des tables de la base de donnée centresportif --

-- Suppression de la table participant_has_equipe
DROP TABLE IF EXISTS `CentreSportif`.`participant_has_equipe`;

-- Suppression de la table equipe_has_ligue
DROP TABLE IF EXISTS `CentreSportif`.`equipe_has_ligue`;

-- Suppression de la table competition_has_equipe
DROP TABLE IF EXISTS `CentreSportif`.`competition_has_equipe`;

-- Suppression de la table equipe
DROP TABLE IF EXISTS `CentreSportif`.`equipe`;

-- Suppression de la table competition
DROP TABLE IF EXISTS `CentreSportif`.`competition`;

-- Suppression de la table ligue
DROP TABLE IF EXISTS `CentreSportif`.`ligue`;

-- Suppression de la table activite
DROP TABLE IF EXISTS `CentreSportif`.`activite`;

-- Suppression de la table participant
DROP TABLE IF EXISTS `CentreSportif`.`participant`;

-- Suppression de la base de donnée vide --
DROP DATABASE `centresportif`