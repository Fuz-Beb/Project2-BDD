-- MySQL Script generated by MySQL Workbench
-- Tue Sep 19 17:19:31 2017
-- Model: New Model    Version: 1.0
-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

-- -----------------------------------------------------
-- DATABASE CentreSportif
-- -----------------------------------------------------
CREATE DATABASE IF NOT EXISTS `CentreSportif` DEFAULT CHARACTER SET utf8 ;
USE `CentreSportif` ;

-- -----------------------------------------------------
-- Table `CentreSportif`.`Activite`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `CentreSportif`.`Activite` ;

CREATE TABLE IF NOT EXISTS `CentreSportif`.`Activite` (
  `id` INT NOT NULL,
  `name` VARCHAR(45) NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `CentreSportif`.`Ligue`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `CentreSportif`.`Ligue` ;

CREATE TABLE IF NOT EXISTS `CentreSportif`.`Ligue` (
  `id` INT NOT NULL,
  `name` VARCHAR(45) NULL,
  `Activite_id` INT NOT NULL,
  PRIMARY KEY (`id`, `Activite_id`),
  INDEX `fk_Ligue_Activite1_idx` (`Activite_id` ASC),
  CONSTRAINT `fk_Ligue_Activite1`
    FOREIGN KEY (`Activite_id`)
    REFERENCES `CentreSportif`.`Activite` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `CentreSportif`.`Competition`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `CentreSportif`.`Competition` ;

CREATE TABLE IF NOT EXISTS `CentreSportif`.`Competition` (
  `id` INT NOT NULL,
  `Ligue_id` INT NOT NULL,
  `Ligue_Activite_id` INT NOT NULL,
  PRIMARY KEY (`id`, `Ligue_id`, `Ligue_Activite_id`),
  INDEX `fk_Competition_Ligue1_idx` (`Ligue_id` ASC, `Ligue_Activite_id` ASC),
  CONSTRAINT `fk_Competition_Ligue1`
    FOREIGN KEY (`Ligue_id` , `Ligue_Activite_id`)
    REFERENCES `CentreSportif`.`Ligue` (`id` , `Activite_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `CentreSportif`.`Equipe`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `CentreSportif`.`Equipe` ;

CREATE TABLE IF NOT EXISTS `CentreSportif`.`Equipe` (
  `id` INT NOT NULL,
  `nom` VARCHAR(45) NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `CentreSportif`.`Participant`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `CentreSportif`.`Participant` ;

CREATE TABLE IF NOT EXISTS `CentreSportif`.`Participant` (
  `id` INT NOT NULL,
  `nom` VARCHAR(45) NULL,
  `prenom` VARCHAR(45) NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `CentreSportif`.`Equipe_has_Ligue`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `CentreSportif`.`Equipe_has_Ligue` ;

CREATE TABLE IF NOT EXISTS `CentreSportif`.`Equipe_has_Ligue` (
  `Equipe_id` INT NOT NULL,
  `Ligue_id` INT NOT NULL,
  PRIMARY KEY (`Equipe_id`, `Ligue_id`),
  INDEX `fk_Equipe_has_Ligue_Ligue1_idx` (`Ligue_id` ASC),
  INDEX `fk_Equipe_has_Ligue_Equipe1_idx` (`Equipe_id` ASC),
  CONSTRAINT `fk_Equipe_has_Ligue_Equipe1`
    FOREIGN KEY (`Equipe_id`)
    REFERENCES `CentreSportif`.`Equipe` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Equipe_has_Ligue_Ligue1`
    FOREIGN KEY (`Ligue_id`)
    REFERENCES `CentreSportif`.`Ligue` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `CentreSportif`.`Competition_has_Equipe`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `CentreSportif`.`Competition_has_Equipe` ;

CREATE TABLE IF NOT EXISTS `CentreSportif`.`Competition_has_Equipe` (
  `Competition_id` INT NOT NULL,
  `Equipe_id1` INT NOT NULL,
  `score1` INT NULL,
  `Equipe_id2` INT NOT NULL,
  `score2` INT NULL,
  PRIMARY KEY (`Competition_id`, `Equipe_id1`, `Equipe_id2`),
  INDEX `fk_Competition_has_Equipe_Equipe1_idx` (`Equipe_id2` ASC),
  INDEX `fk_Competition_has_Equipe_Competition1_idx` (`Competition_id` ASC),
  INDEX `fk_Competition_has_Equipe_Equipe2_idx` (`Equipe_id1` ASC),
  CONSTRAINT `fk_Competition_has_Equipe_Competition1`
    FOREIGN KEY (`Competition_id`)
    REFERENCES `CentreSportif`.`Competition` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Competition_has_Equipe_Equipe1`
    FOREIGN KEY (`Equipe_id2`)
    REFERENCES `CentreSportif`.`Equipe` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Competition_has_Equipe_Equipe2`
    FOREIGN KEY (`Equipe_id1`)
    REFERENCES `CentreSportif`.`Equipe` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `CentreSportif`.`Competition_has_Activite`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `CentreSportif`.`Competition_has_Activite` ;

CREATE TABLE IF NOT EXISTS `CentreSportif`.`Competition_has_Activite` (
  `Competition_id` INT NOT NULL,
  `Activite_id` INT NOT NULL,
  PRIMARY KEY (`Competition_id`, `Activite_id`),
  INDEX `fk_Competition_has_Activite_Activite1_idx` (`Activite_id` ASC),
  INDEX `fk_Competition_has_Activite_Competition1_idx` (`Competition_id` ASC),
  CONSTRAINT `fk_Competition_has_Activite_Competition1`
    FOREIGN KEY (`Competition_id`)
    REFERENCES `CentreSportif`.`Competition` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Competition_has_Activite_Activite1`
    FOREIGN KEY (`Activite_id`)
    REFERENCES `CentreSportif`.`Activite` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `CentreSportif`.`Competition_has_Resultat`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `CentreSportif`.`Competition_has_Resultat` ;

CREATE TABLE IF NOT EXISTS `CentreSportif`.`Competition_has_Resultat` (
  `Competition_id` INT NOT NULL,
  `Resultat_id` INT NOT NULL,
  `Resultat_Activite_id` INT NOT NULL,
  `Resultat_Equipe_id1` INT NOT NULL,
  `Resultat_Equipe_id2` INT NOT NULL,
  `Resultat_Ligue_id` INT NOT NULL,
  PRIMARY KEY (`Competition_id`, `Resultat_id`, `Resultat_Activite_id`, `Resultat_Equipe_id1`, `Resultat_Equipe_id2`, `Resultat_Ligue_id`),
  INDEX `fk_Competition_has_Resultat_Competition1_idx` (`Competition_id` ASC),
  CONSTRAINT `fk_Competition_has_Resultat_Competition1`
    FOREIGN KEY (`Competition_id`)
    REFERENCES `CentreSportif`.`Competition` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `CentreSportif`.`Participant_has_Equipe`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `CentreSportif`.`Participant_has_Equipe` ;

CREATE TABLE IF NOT EXISTS `CentreSportif`.`Participant_has_Equipe` (
  `Participant_id` INT NOT NULL,
  `Equipe_id` INT NOT NULL,
  PRIMARY KEY (`Participant_id`, `Equipe_id`),
  INDEX `fk_Participant_has_Equipe_Equipe1_idx` (`Equipe_id` ASC),
  INDEX `fk_Participant_has_Equipe_Participant1_idx` (`Participant_id` ASC),
  CONSTRAINT `fk_Participant_has_Equipe_Participant1`
    FOREIGN KEY (`Participant_id`)
    REFERENCES `CentreSportif`.`Participant` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Participant_has_Equipe_Equipe1`
    FOREIGN KEY (`Equipe_id`)
    REFERENCES `CentreSportif`.`Equipe` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
