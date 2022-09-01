-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema floristeria
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema floristeria
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `floristeria` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci ;
USE `floristeria` ;

-- -----------------------------------------------------
-- Table `floristeria`.`products`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `floristeria`.`products` (
  `id_product` INT NOT NULL AUTO_INCREMENT,
  `type` VARCHAR(15) NOT NULL,
  `name` VARCHAR(45) NULL DEFAULT NULL,
  `price` DECIMAL(6,2) NULL DEFAULT NULL,
  `quantity` INT NULL DEFAULT NULL,
  `property` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`id_product`))
ENGINE = InnoDB
AUTO_INCREMENT = 14
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `floristeria`.`tickets`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `floristeria`.`tickets` (
  `id_ticket` INT NOT NULL AUTO_INCREMENT,
  `datetime` DATETIME NULL,
  PRIMARY KEY (`id_ticket`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `floristeria`.`products_tickets`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `floristeria`.`products_tickets` (
  `product_id` INT NOT NULL,
  `ticket_id` INT NOT NULL,
  `quantity` INT NULL,
  PRIMARY KEY (`product_id`, `ticket_id`),
  CONSTRAINT `fk_products_has_tickets_products`
    FOREIGN KEY (`product_id`)
    REFERENCES `floristeria`.`products` (`id_product`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_products_has_tickets_tickets1`
    FOREIGN KEY (`ticket_id`)
    REFERENCES `floristeria`.`tickets` (`id_ticket`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX `fk_products_has_tickets_tickets1_idx` ON `floristeria`.`products_tickets` (`ticket_id` ASC) VISIBLE;

CREATE INDEX `fk_products_has_tickets_products_idx` ON `floristeria`.`products_tickets` (`product_id` ASC) VISIBLE;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
