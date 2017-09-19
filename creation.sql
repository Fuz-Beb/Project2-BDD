-- MySQL Script generated by MySQL Workbench
-- Tue Sep 19 17:19:31 2017
-- Model: New Model    Version: 1.0
-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `mydb` DEFAULT CHARACTER SET utf8 ;
USE `mydb` ;

-- -----------------------------------------------------
-- Table `mydb`.`Activite`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `mydb`.`Activite` ;

CREATE TABLE IF NOT EXISTS `mydb`.`Activite` (
  `id` INT NOT NULL,
  `name` VARCHAR(45) NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`Ligue`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `mydb`.`Ligue` ;

CREATE TABLE IF NOT EXISTS `mydb`.`Ligue` (
  `id` INT NOT NULL,
  `name` VARCHAR(45) NULL,
  `Activite_id` INT NOT NULL,
  PRIMARY KEY (`id`, `Activite_id`),
  INDEX `fk_Ligue_Activite1_idx` (`Activite_id` ASC),
  CONSTRAINT `fk_Ligue_Activite1`
    FOREIGN KEY (`Activite_id`)
    REFERENCES `mydb`.`Activite` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`Competition`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `mydb`.`Competition` ;

CREATE TABLE IF NOT EXISTS `mydb`.`Competition` (
  `id` INT NOT NULL,
  `Ligue_id` INT NOT NULL,
  `Ligue_Activite_id` INT NOT NULL,
  PRIMARY KEY (`id`, `Ligue_id`, `Ligue_Activite_id`),
  INDEX `fk_Competition_Ligue1_idx` (`Ligue_id` ASC, `Ligue_Activite_id` ASC),
  CONSTRAINT `fk_Competition_Ligue1`
    FOREIGN KEY (`Ligue_id` , `Ligue_Activite_id`)
    REFERENCES `mydb`.`Ligue` (`id` , `Activite_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`Equipe`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `mydb`.`Equipe` ;

CREATE TABLE IF NOT EXISTS `mydb`.`Equipe` (
  `id` INT NOT NULL,
  `nom` VARCHAR(45) NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`Participant`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `mydb`.`Participant` ;

CREATE TABLE IF NOT EXISTS `mydb`.`Participant` (
  `id` INT NOT NULL,
  `nom` VARCHAR(45) NULL,
  `prenom` VARCHAR(45) NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`Equipe_has_Ligue`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `mydb`.`Equipe_has_Ligue` ;

CREATE TABLE IF NOT EXISTS `mydb`.`Equipe_has_Ligue` (
  `Equipe_id` INT NOT NULL,
  `Ligue_id` INT NOT NULL,
  PRIMARY KEY (`Equipe_id`, `Ligue_id`),
  INDEX `fk_Equipe_has_Ligue_Ligue1_idx` (`Ligue_id` ASC),
  INDEX `fk_Equipe_has_Ligue_Equipe1_idx` (`Equipe_id` ASC),
  CONSTRAINT `fk_Equipe_has_Ligue_Equipe1`
    FOREIGN KEY (`Equipe_id`)
    REFERENCES `mydb`.`Equipe` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Equipe_has_Ligue_Ligue1`
    FOREIGN KEY (`Ligue_id`)
    REFERENCES `mydb`.`Ligue` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`Competition_has_Equipe`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `mydb`.`Competition_has_Equipe` ;

CREATE TABLE IF NOT EXISTS `mydb`.`Competition_has_Equipe` (
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
    REFERENCES `mydb`.`Competition` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Competition_has_Equipe_Equipe1`
    FOREIGN KEY (`Equipe_id2`)
    REFERENCES `mydb`.`Equipe` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Competition_has_Equipe_Equipe2`
    FOREIGN KEY (`Equipe_id1`)
    REFERENCES `mydb`.`Equipe` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`Competition_has_Activite`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `mydb`.`Competition_has_Activite` ;

CREATE TABLE IF NOT EXISTS `mydb`.`Competition_has_Activite` (
  `Competition_id` INT NOT NULL,
  `Activite_id` INT NOT NULL,
  PRIMARY KEY (`Competition_id`, `Activite_id`),
  INDEX `fk_Competition_has_Activite_Activite1_idx` (`Activite_id` ASC),
  INDEX `fk_Competition_has_Activite_Competition1_idx` (`Competition_id` ASC),
  CONSTRAINT `fk_Competition_has_Activite_Competition1`
    FOREIGN KEY (`Competition_id`)
    REFERENCES `mydb`.`Competition` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Competition_has_Activite_Activite1`
    FOREIGN KEY (`Activite_id`)
    REFERENCES `mydb`.`Activite` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`Competition_has_Resultat`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `mydb`.`Competition_has_Resultat` ;

CREATE TABLE IF NOT EXISTS `mydb`.`Competition_has_Resultat` (
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
    REFERENCES `mydb`.`Competition` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`Participant_has_Equipe`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `mydb`.`Participant_has_Equipe` ;

CREATE TABLE IF NOT EXISTS `mydb`.`Participant_has_Equipe` (
  `Participant_id` INT NOT NULL,
  `Equipe_id` INT NOT NULL,
  PRIMARY KEY (`Participant_id`, `Equipe_id`),
  INDEX `fk_Participant_has_Equipe_Equipe1_idx` (`Equipe_id` ASC),
  INDEX `fk_Participant_has_Equipe_Participant1_idx` (`Participant_id` ASC),
  CONSTRAINT `fk_Participant_has_Equipe_Participant1`
    FOREIGN KEY (`Participant_id`)
    REFERENCES `mydb`.`Participant` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Participant_has_Equipe_Equipe1`
    FOREIGN KEY (`Equipe_id`)
    REFERENCES `mydb`.`Equipe` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
