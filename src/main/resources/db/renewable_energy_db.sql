-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1:3306
-- Generation Time: May 18, 2025 at 09:02 AM
-- Server version: 9.1.0
-- PHP Version: 8.3.14

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `renewable_energy_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `bin`
--

DROP TABLE IF EXISTS `bin`;
CREATE TABLE IF NOT EXISTS `bin` (
  `id` int NOT NULL AUTO_INCREMENT,
  `location` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `typeDechet` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `niveauRemplissage` float NOT NULL,
  `statut` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `blog`
--

DROP TABLE IF EXISTS `blog`;
CREATE TABLE IF NOT EXISTS `blog` (
  `id` int NOT NULL AUTO_INCREMENT,
  `titre` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `contenu` text COLLATE utf8mb4_general_ci,
  `image` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `datePublication` datetime NOT NULL,
  `auteurId` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `auteurId` (`auteurId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `categorie`
--

DROP TABLE IF EXISTS `categorie`;
CREATE TABLE IF NOT EXISTS `categorie` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nom` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `description` text COLLATE utf8mb4_general_ci,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `commande`
--

DROP TABLE IF EXISTS `commande`;
CREATE TABLE IF NOT EXISTS `commande` (
  `id` int NOT NULL AUTO_INCREMENT,
  `dateCommande` datetime NOT NULL,
  `statut` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `total` double NOT NULL,
  `utilisateurId` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `utilisateurId` (`utilisateurId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `commandeproduit`
--

DROP TABLE IF EXISTS `commandeproduit`;
CREATE TABLE IF NOT EXISTS `commandeproduit` (
  `id` int NOT NULL AUTO_INCREMENT,
  `quantite` int NOT NULL,
  `sousTotal` double NOT NULL,
  `statut` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `commandeId` int NOT NULL,
  `produitId` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `commandeId` (`commandeId`),
  KEY `produitId` (`produitId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `commentaire`
--

DROP TABLE IF EXISTS `commentaire`;
CREATE TABLE IF NOT EXISTS `commentaire` (
  `id` int NOT NULL AUTO_INCREMENT,
  `contenu` text COLLATE utf8mb4_general_ci NOT NULL,
  `date` datetime NOT NULL,
  `utilisateurId` int NOT NULL,
  `blogId` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `utilisateurId` (`utilisateurId`),
  KEY `blogId` (`blogId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `comments`
--

DROP TABLE IF EXISTS `comments`;
CREATE TABLE IF NOT EXISTS `comments` (
  `id` int NOT NULL AUTO_INCREMENT,
  `content` text COLLATE utf8mb4_general_ci NOT NULL,
  `user_id` int NOT NULL,
  `post_id` int NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  KEY `post_id` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `contract`
--

DROP TABLE IF EXISTS `contract`;
CREATE TABLE IF NOT EXISTS `contract` (
  `id` int NOT NULL AUTO_INCREMENT,
  `startDate` datetime NOT NULL,
  `duration` int NOT NULL,
  `utilisateurId` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `utilisateurId` (`utilisateurId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `dechet`
--

DROP TABLE IF EXISTS `dechet`;
CREATE TABLE IF NOT EXISTS `dechet` (
  `id` int NOT NULL AUTO_INCREMENT,
  `type` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `quantite` float NOT NULL,
  `date` datetime NOT NULL,
  `utilisateurId` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `utilisateurId` (`utilisateurId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `email_verification_tokens`
--

DROP TABLE IF EXISTS `email_verification_tokens`;
CREATE TABLE IF NOT EXISTS `email_verification_tokens` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `token` varchar(191) NOT NULL,
  `expiry_date` timestamp NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `token` (`token`),
  KEY `fk_user` (`user_id`)
) ENGINE=MyISAM AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `email_verification_tokens`
--

INSERT INTO `email_verification_tokens` (`id`, `user_id`, `token`, `expiry_date`) VALUES
(5, 86, '701967', '2025-05-10 15:03:20'),
(6, 87, '939477', '2025-05-10 19:14:47'),
(14, 1, '901045', '2025-05-17 18:01:08'),
(11, 88, '227483', '2025-05-11 14:15:27'),
(12, 89, '720402', '2025-05-14 22:59:48'),
(15, 90, '664906', '2025-05-17 20:10:09');

-- --------------------------------------------------------

--
-- Table structure for table `evenement`
--

DROP TABLE IF EXISTS `evenement`;
CREATE TABLE IF NOT EXISTS `evenement` (
  `id` int NOT NULL AUTO_INCREMENT,
  `titre` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `description` text COLLATE utf8mb4_general_ci,
  `date` datetime NOT NULL,
  `adresse` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `organisateurId` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `organisateurId` (`organisateurId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `likes`
--

DROP TABLE IF EXISTS `likes`;
CREATE TABLE IF NOT EXISTS `likes` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `post_id` int NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_post_unique` (`user_id`,`post_id`),
  KEY `post_id` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `mfa_persistent_tokens`
--

DROP TABLE IF EXISTS `mfa_persistent_tokens`;
CREATE TABLE IF NOT EXISTS `mfa_persistent_tokens` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `device_identifier` varchar(191) NOT NULL,
  `token` varchar(255) NOT NULL,
  `expiry_date` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_device_user` (`user_id`,`device_identifier`)
) ENGINE=MyISAM AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `mfa_persistent_tokens`
--

INSERT INTO `mfa_persistent_tokens` (`id`, `user_id`, `device_identifier`, `token`, `expiry_date`) VALUES
(1, 0, 'DEVICE_IDENTIFIER', '28b78a3a-3280-11f0-886f-c85b76e52cea', '2025-06-15 19:04:06'),
(2, 11, 'your_device_identifier_here', '56bb71f4-3281-11f0-886f-c85b76e52cea', '2025-06-15 19:12:33'),
(3, 90, '765ee54d-692d-40cf-93e2-4f475ae6d5aa', 'jqi3mpmdf4e5k53ckd83ufqjsu', '2025-06-15 21:11:30'),
(4, 90, '64a43e15-bccb-4e9c-92f4-5346a07f9d04', 's628ntqsbt3rq55vkha8j63mqo', '2025-06-15 22:24:59'),
(5, 11, '64a43e15-bccb-4e9c-92f4-5346a07f9d04', '3dgkc5n826q4decu7rb0l91tl2', '2025-06-16 10:17:51');

-- --------------------------------------------------------

--
-- Table structure for table `panier`
--

DROP TABLE IF EXISTS `panier`;
CREATE TABLE IF NOT EXISTS `panier` (
  `id` int NOT NULL AUTO_INCREMENT,
  `utilisateurId` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `utilisateurId` (`utilisateurId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `panierproduit`
--

DROP TABLE IF EXISTS `panierproduit`;
CREATE TABLE IF NOT EXISTS `panierproduit` (
  `id` int NOT NULL AUTO_INCREMENT,
  `quantite` int NOT NULL,
  `panierId` int NOT NULL,
  `produitId` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `panierId` (`panierId`),
  KEY `produitId` (`produitId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `poster`
--

DROP TABLE IF EXISTS `poster`;
CREATE TABLE IF NOT EXISTS `poster` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `description` text COLLATE utf8mb4_general_ci NOT NULL,
  `imageUrl` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `createdAt` datetime NOT NULL,
  `evenementId` int NOT NULL,
  `createdById` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `evenementId` (`evenementId`),
  KEY `createdById` (`createdById`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `posts`
--

DROP TABLE IF EXISTS `posts`;
CREATE TABLE IF NOT EXISTS `posts` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `content` text COLLATE utf8mb4_general_ci NOT NULL,
  `user_id` int NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `produit`
--

DROP TABLE IF EXISTS `produit`;
CREATE TABLE IF NOT EXISTS `produit` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nom` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `description` text COLLATE utf8mb4_general_ci,
  `prix` double NOT NULL,
  `quantiteStock` int NOT NULL,
  `image` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `recyclable` tinyint(1) NOT NULL,
  `categorie_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `categorie_id` (`categorie_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `reclamation`
--

DROP TABLE IF EXISTS `reclamation`;
CREATE TABLE IF NOT EXISTS `reclamation` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `report` text NOT NULL,
  `status` varchar(50) DEFAULT 'Pending',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_user` (`user_id`)
) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `reclamation`
--

INSERT INTO `reclamation` (`id`, `user_id`, `report`, `status`, `created_at`) VALUES
(1, 1, 'Example claim report: The product arrived damaged.', 'Pending', '2025-05-03 23:32:20'),
(2, 1, 'Example claim report: The product arrived damaged.', 'Pending', '2025-05-03 23:37:44'),
(3, 2, 'Late delivery of the order.', 'Pending', '2025-05-03 23:37:44');

-- --------------------------------------------------------

--
-- Table structure for table `sms_verification_tokens`
--

DROP TABLE IF EXISTS `sms_verification_tokens`;
CREATE TABLE IF NOT EXISTS `sms_verification_tokens` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `token` varchar(191) NOT NULL,
  `expiry_date` timestamp NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `token` (`token`),
  KEY `fk_user_sms` (`user_id`)
) ENGINE=MyISAM AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `sms_verification_tokens`
--

INSERT INTO `sms_verification_tokens` (`id`, `user_id`, `token`, `expiry_date`) VALUES
(8, 88, '763353', '2025-05-16 12:30:19');

-- --------------------------------------------------------

--
-- Table structure for table `truck`
--

DROP TABLE IF EXISTS `truck`;
CREATE TABLE IF NOT EXISTS `truck` (
  `id` int NOT NULL AUTO_INCREMENT,
  `capaciteMax` float NOT NULL,
  `niveauRemplissageActuel` float NOT NULL,
  `section` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `statut` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
CREATE TABLE IF NOT EXISTS `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nom` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `prenom` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `birth_date` datetime NOT NULL,
  `email` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `adresse` varchar(25) COLLATE utf8mb4_general_ci NOT NULL,
  `telephone` varchar(25) COLLATE utf8mb4_general_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `role` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `status` varchar(25) COLLATE utf8mb4_general_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=91 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `nom`, `prenom`, `birth_date`, `email`, `adresse`, `telephone`, `password`, `role`, `status`, `description`) VALUES
(1, 'Doe', 'John', '1980-01-01 00:00:00', 'john.doe@example.com', '123 Main St', '1234567890', 'password123', 'user', 'confirmed', ''),
(11, 'admin', 'admin', '2025-04-08 00:00:00', 'admin', 'sdqsdqsdqsdqsdqssdqs', '00000000', '$2a$10$QXjLBSCh5DcNy6oYfneUruQ/ccEcwyCW1pH4T7qijfzumReBmJDrS', 'ADMIN', 'confirmed', ''),
(49, 'Smith', 'John', '1985-06-15 00:00:00', 'john.smith@example.com', '123 Main St', '555-1234', 'password123', 'ADMIN', 'CONFIRMED', ''),
(50, 'Doe', 'Jane', '1990-09-20 00:00:00', 'jane.doe@example.com', '456 Oak Ave', '555-5678', 'password123', 'CLIENT', 'NON_CONFIRMED', ''),
(51, 'Brown', 'Charlie', '1988-12-05 00:00:00', 'charlie.brown@example.com', '789 Pine Rd', '555-9012', 'password123', 'CLIENT', 'BANNED', ''),
(52, 'Johnson', 'Emily', '1992-03-10 00:00:00', 'emily.johnson@example.com', '321 Elm St', '00000000', 'password123', 'EVENT_PLANNER', 'confirmed', ''),
(53, 'Davis', 'Michael', '1980-11-25 00:00:00', 'michael.davis@example.com', '654 Maple Dr', '555-7890', 'password123', 'PRODUCT_OWNER', 'CONFIRMED', ''),
(55, 'Trabelsi', 'Sana', '1995-07-08 00:00:00', 'sana.trabelsi@example.com', '45 Avenue de France, Sfax', '22987654', 'sanaT95!', 'EVENT_PLANNER', 'NON_CONFIRMED', ''),
(56, 'Gharbi', 'Karim', '1987-11-30 00:00:00', 'karim.gharbi@example.com', '78 Rue de la Liberté, Ari', '23111222', 'karimG87$', 'PRODUCT_OWNER', 'CONFIRMED', ''),
(57, 'Maaloul', 'Yasmine', '1993-09-18 00:00:00', 'yasmine.maaloul@example.com', '99 Rue Ibn Khaldoun, Sous', '24345678', 'yasmine93@', 'CLIENT', 'BANNED', ''),
(58, 'Kefi', 'Houssem', '1986-02-25 00:00:00', 'houssem.kefi@example.com', 'Rue de l'Indépendance, Ka', '25543210', 'kefiH86#', 'CLIENT', 'CONFIRMED', ''),
(59, 'Bouazizi', 'Rim', '1997-06-05 00:00:00', 'rim.bouazizi@example.com', 'Rue de la République, Gab', '26112233', 'rimBoua97', 'PRODUCT_OWNER', 'CONFIRMED', ''),
(60, 'Zribi', 'Fares', '1988-03-14 00:00:00', 'fares.zribi@example.com', 'Avenue Hédi Chaker, Bizer', '27334455', 'faresZ88!', 'CLIENT', 'NON_CONFIRMED', ''),
(61, 'Ben Rejeb', 'Nour', '1992-10-01 00:00:00', 'nour.benrejeb@example.com', 'Rue 14 Janvier, Nabeul', '29123344', 'nourR92$', 'EVENT_PLANNER', 'CONFIRMED', ''),
(62, 'Chaabane', 'Walid', '1984-01-17 00:00:00', 'walid.chaabane@example.com', 'Rue Farhat Hached, Mahdia', '29887766', 'walidC84@', 'CLIENT', 'BANNED', ''),
(63, 'Brahmi', 'Lina', '1990-08-21 00:00:00', 'lina.brahmi@example.com', 'Avenue Bourguiba, Monasti', '30224466', 'linaB90$', 'CLIENT', 'CONFIRMED', ''),
(64, 'Jaziri', 'Ahmed', '1982-05-10 00:00:00', 'ahmed.jaziri@example.com', 'Rue Mongi Slim, Tunis', '31334422', 'ahmedJ82', 'ADMIN', 'CONFIRMED', ''),
(65, 'Ben Mbarek', 'Syrine', '1996-11-02 00:00:00', 'syrine.benmbarek@example.com', 'Rue Taieb Mhiri, Sfax', '32112288', 'syrineBM96', 'CLIENT', 'CONFIRMED', ''),
(66, 'Feki', 'Mehdi', '1985-12-19 00:00:00', 'mehdi.feki@example.com', 'Rue de Sidi Bou Said, La ', '33223311', 'fekiM85!', 'PRODUCT_OWNER', 'NON_CONFIRMED', ''),
(67, 'Khalfallah', 'Ines', '1998-07-07 00:00:00', 'ines.khalfallah@example.com', 'Rue Ennasr, Ariana', '34119900', 'inesK98@', 'CLIENT', 'CONFIRMED', ''),
(68, 'Ayari', 'Tarek', '1991-03-03 00:00:00', 'tarek.ayari@example.com', 'Rue El Menzah 6, Tunis', '35118877', 'tarekA91$', 'CLIENT', 'BANNED', ''),
(69, 'Masmoudi', 'Dorsaf', '1994-09-25 00:00:00', 'dorsaf.masmoudi@example.com', 'Avenue Habib Thameur, Sou', '36224411', 'dorsafM94#', 'EVENT_PLANNER', 'CONFIRMED', ''),
(70, 'Karray', 'Omar', '1989-06-16 00:00:00', 'omar.karray@example.com', 'Rue de Ras Jebel, Bizerte', '37112233', 'omarK89@', 'CLIENT', 'confirmed', ''),
(71, 'Sliti', 'Rania', '1993-01-09 00:00:00', 'rania.sliti@example.com', 'Rue de l'Université, Gabè', '38991100', 'raniaS93!', 'PRODUCT_OWNER', 'CONFIRMED', ''),
(72, 'Cherif', 'Adel', '1983-04-04 00:00:00', 'adel.cherif@example.com', 'Rue Ibn Sina, Monastir', '39445577', 'adelC83$', 'CLIENT', 'CONFIRMED', ''),
(73, 'Hajri', 'Nadia', '1999-12-12 00:00:00', 'nadia.hajri@example.com', 'Rue de l'Aéroport, Djerba', '40112244', 'nadiaH99!', 'CLIENT', 'CONFIRMED', ''),
(79, 'mareim', 'mareim', '1998-04-15 00:00:00', 'demo4@example.com', 'sqdqsdq', '00000000', '$2a$10$73PRMZAHgxEcJ3sf6UlBWuztKvwga5LeRWR.mdaCmD41xbh/x2KZm', 'USER', 'non_confirmed', ''),
(80, 'houda', 'houda', '2000-04-13 00:00:00', 'demo2@example.com', 'qsdqsdqsdqsdq', '00000000', '$2a$10$D9l7sr6bNh0RjlrtJsVMauN/gRrBYx4KRF1t/b6XlptJn44CkJ9d.', 'USER', 'non_confirmed', ''),
(81, 'wael', 'wael', '1998-04-22 00:00:00', 'demo6@example.com', 'qsdqsdqsdqsd', '00000000', '$2a$10$5TEwmX1ucWey9rFz/VxUvuafPrUKH1N80b//56zS6On3kneTXo5AC', 'BLOGER_ADMIN', 'confirmed', ''),
(82, 'sirin', 'kchaw', '2002-04-15 00:00:00', 'demo3@example.com', 'qsdqsdqsdqsdqsdq', '00000000', '$2a$10$3UJfq/rYN8XZxW2IpbJ6ve4P0sYFyziVCt7xadq9868XQuIHYolee', 'PRODUCT_OWNER', 'banned', ''),
(84, 'lola', 'lola', '1999-05-12 00:00:00', 'wael.chenoufi@esprit.tn', 'tunis', '00000000', '$2a$10$12KBZZyjvIJfy.4z9gJLe.wz5pmJY9LCGAl3mRXZvjyzWlp0HCUPG', 'USER', 'non_confirmed', ''),
(85, 'xxxxxx', 'xxxxxxxx', '2000-05-31 00:00:00', 'demo1@example.com', 'tunis', '00000000', '$2a$10$DpTehxNjsQnEgDS2e6wRTuXaZJ2dgHrOU.uGp3ML.6MQz8GKyOmJm', 'USER', 'non_confirmed', ''),
(87, 'moez', 'moez', '1998-12-07 00:00:00', 'demo5@example.com', 'tunis', '00000000', '$2a$10$Vo3bxHh4sm3nsG3zJCVmaua8yHsUp242/QFz/c26r6yosCdDmzpoy', 'USER', 'non_confirmed', ''),
(88, 'lola', 'lola', '1999-05-07 00:00:00', 'demo8@example.com', 'tunis', '00000000', '$2a$10$EhxamviqoAtfih6gi2GLue0y5RKyXXpXNzUmrbVD3oIG1hqjK0yn2', 'CLIENT', 'verified', ''),
(90, 'wael', 'chenoufi', '1999-09-13 00:00:00', 'demo7@example.com', 'tunis', '00000000', '$2a$10$sfuOLZpFd26ev6pxciDjC.UCDXaEGzfHRQ1sX0KPX0k7M/msHXsua', 'UNKNOWN', 'verified', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `user_profiles`
--

DROP TABLE IF EXISTS `user_profiles`;
CREATE TABLE IF NOT EXISTS `user_profiles` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `user_id` int NOT NULL COMMENT 'Foreign key reference to users table',
  `profile_photo_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'URL or path to user profile photo',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT 'User profile description',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Record last update timestamp',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `idx_user_profiles_user_id` (`user_id`)
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `user_profiles`
--

INSERT INTO `user_profiles` (`id`, `user_id`, `profile_photo_url`, `description`, `created_at`, `updated_at`) VALUES
(1, 11, NULL, '', '2025-05-17 21:05:48', '2025-05-17 21:20:26');

--
-- Constraints for dumped tables
--

--
-- Constraints for table `blog`
--
ALTER TABLE `blog`
  ADD CONSTRAINT `blog_ibfk_1` FOREIGN KEY (`auteurId`) REFERENCES `users` (`id`);

--
-- Constraints for table `commande`
--
ALTER TABLE `commande`
  ADD CONSTRAINT `commande_ibfk_1` FOREIGN KEY (`utilisateurId`) REFERENCES `users` (`id`);

--
-- Constraints for table `commandeproduit`
--
ALTER TABLE `commandeproduit`
  ADD CONSTRAINT `commandeproduit_ibfk_1` FOREIGN KEY (`commandeId`) REFERENCES `commande` (`id`),
  ADD CONSTRAINT `commandeproduit_ibfk_2` FOREIGN KEY (`produitId`) REFERENCES `produit` (`id`);

--
-- Constraints for table `commentaire`
--
ALTER TABLE `commentaire`
  ADD CONSTRAINT `commentaire_ibfk_1` FOREIGN KEY (`utilisateurId`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `commentaire_ibfk_2` FOREIGN KEY (`blogId`) REFERENCES `blog` (`id`);

--
-- Constraints for table `comments`
--
ALTER TABLE `comments`
  ADD CONSTRAINT `comments_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `comments_ibfk_2` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`);

--
-- Constraints for table `contract`
--
ALTER TABLE `contract`
  ADD CONSTRAINT `contract_ibfk_1` FOREIGN KEY (`utilisateurId`) REFERENCES `users` (`id`);

--
-- Constraints for table `dechet`
--
ALTER TABLE `dechet`
  ADD CONSTRAINT `dechet_ibfk_1` FOREIGN KEY (`utilisateurId`) REFERENCES `users` (`id`);

--
-- Constraints for table `evenement`
--
ALTER TABLE `evenement`
  ADD CONSTRAINT `evenement_ibfk_1` FOREIGN KEY (`organisateurId`) REFERENCES `users` (`id`);

--
-- Constraints for table `likes`
--
ALTER TABLE `likes`
  ADD CONSTRAINT `likes_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `likes_ibfk_2` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`);

--
-- Constraints for table `panier`
--
ALTER TABLE `panier`
  ADD CONSTRAINT `panier_ibfk_1` FOREIGN KEY (`utilisateurId`) REFERENCES `users` (`id`);

--
-- Constraints for table `panierproduit`
--
ALTER TABLE `panierproduit`
  ADD CONSTRAINT `panierproduit_ibfk_1` FOREIGN KEY (`panierId`) REFERENCES `panier` (`id`),
  ADD CONSTRAINT `panierproduit_ibfk_2` FOREIGN KEY (`produitId`) REFERENCES `produit` (`id`);

--
-- Constraints for table `poster`
--
ALTER TABLE `poster`
  ADD CONSTRAINT `poster_ibfk_1` FOREIGN KEY (`evenementId`) REFERENCES `evenement` (`id`),
  ADD CONSTRAINT `poster_ibfk_2` FOREIGN KEY (`createdById`) REFERENCES `users` (`id`);

--
-- Constraints for table `posts`
--
ALTER TABLE `posts`
  ADD CONSTRAINT `posts_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Constraints for table `produit`
--
ALTER TABLE `produit`
  ADD CONSTRAINT `produit_ibfk_1` FOREIGN KEY (`categorie_id`) REFERENCES `categorie` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
