-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jun 24, 2025 at 11:06 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

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

CREATE TABLE `bin` (
  `id` int(11) NOT NULL,
  `location` varchar(255) NOT NULL,
  `typeDechet` varchar(100) NOT NULL,
  `niveauRemplissage` float NOT NULL,
  `statut` varchar(50) NOT NULL,
  `isSolarPowered` tinyint(1) DEFAULT 0,
  `energyStatus` varchar(50) DEFAULT 'N/A',
  `batteryLevel` double DEFAULT 0,
  `solarPanelOutput` double DEFAULT 0,
  `lastMaintenanceDate` date DEFAULT NULL,
  `nextScheduledMaintenance` date DEFAULT NULL,
  `maintenanceNotes` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `blog`
--

CREATE TABLE `blog` (
  `id` int(11) NOT NULL,
  `titre` varchar(255) NOT NULL,
  `resume` varchar(5000) DEFAULT NULL,
  `contenu` text DEFAULT NULL,
  `image` varchar(500) DEFAULT NULL,
  `datePublication` datetime NOT NULL,
  `auteurId` int(11) NOT NULL,
  `views` int(11) NOT NULL DEFAULT 0,
  `status` varchar(255) NOT NULL,
  `like_count` int(11) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `blog`
--

INSERT INTO `blog` (`id`, `titre`, `resume`, `contenu`, `image`, `datePublication`, `auteurId`, `views`, `status`, `like_count`) VALUES
(3, 'The Efficiency of LED Bulbs: Save Energy, Brighten Your Future', 'LED bulbs offer a modern, cost-effective, and eco-friendly lighting solution. With their low energy consumption and long lifespan, they are a smart choice for reducing electricity bills and protecting the environment.', '1. What is an LED bulb?\nLED (Light Emitting Diode) bulbs produce light using a very small amount of electric current. Unlike incandescent or compact fluorescent bulbs, LEDs waste very little energy as heat.\n\n2. Advantages of LED bulbs:\n\nEnergy Efficiency: Up to 90% less energy used compared to traditional bulbs.\n\nLong Lifespan: An LED bulb can last between 15,000 and 50,000 hours.\n\nLow Maintenance: Fewer replacements mean convenience and savings.\n\nEnvironmentally Friendly: LEDs contain no mercury and help reduce CO₂ emissions.\n\n3. Where can they be used?\nLEDs are versatile — perfect for homes, offices, schools, and public spaces. They come in various colors and brightness levels to suit any atmosphere.\n\n4. Why switch to LEDs today?\nWith rising energy costs and environmental challenges, switching to LED bulbs is a simple yet powerful step toward a sustainable future. It’s an easy action with a big impact — for your wallet and for the planet.\n\n', '1750629569742_generated_9a94a57a-7433-4b0b-acea-f431e203d579.png', '2025-06-22 10:00:42', 1, 94, 'Published', 1),
(10, 'Little Hands, Big Impact: Teaching Kids About Recycling', 'This post explores the importance of teaching children about recycling and offers practical tips for parents and educators on how to make recycling fun and engaging for young learners.  We\'ll discuss the benefits of early environmental education and provide creative ways to instill responsible waste management habits.', '## Little Hands, Big Impact: Teaching Kids About Recycling\nOur planet\'s future depends on the actions we take today, and instilling environmentally responsible habits in children is crucial.  Recycling, a seemingly simple act, plays a significant role in reducing waste and protecting our environment. But how do we effectively teach kids about the importance of recycling, making it more than just a chore?\nThe image above perfectly captures the spirit of engaging children in recycling.  Two young children, brimming with enthusiasm, stand proudly beside a large recycling bin, ready to contribute. This visual represents the power of positive reinforcement and the joy that can be found in making a difference.\n**Why Start Young?**\nTeaching children about recycling from a young age offers several key benefits:\n* **Habit Formation:**  Early exposure fosters the development of lifelong habits.  What children learn young often becomes ingrained behavior.\n* **Environmental Awareness:**  It cultivates an understanding of the impact of waste on the planet and the importance of conservation.\n* **Responsibility and Ownership:**  Recycling empowers children, giving them a sense of responsibility and ownership in protecting their environment.\n* **Problem-Solving Skills:**  It encourages critical thinking about waste reduction and resource management.\n**Making Recycling Fun and Engaging:**\nTurning recycling into a fun activity is key to its success:\n* **Interactive Games:** Create games that involve sorting different types of waste into designated bins.\n* **Storytelling:** Use stories and characters to illustrate the importance of recycling and its positive impact.\n* **Visual Aids:**  Use colorful charts and diagrams to explain the recycling process.\n* **Hands-on Activities:**  Involve children in creating recycled crafts from used materials.\n* **Family Involvement:**  Make recycling a family affair.  Involve children in the process at home, making it a shared responsibility.\n* **School Initiatives:**  Support school recycling programs and encourage participation.\n**Beyond the Bin:**\nTeaching children about recycling goes beyond simply tossing items into a bin.  It\'s about understanding:\n* **The Recycling Process:**  Explain where recycled materials go and how they are transformed into new products.\n* **Reduce, Reuse, Recycle:**  Emphasize the importance of reducing consumption and reusing items before recycling.\n* **Proper Sorting:**  Teach children how to correctly sort different materials for recycling.\nBy making recycling a fun, engaging, and educational experience, we can empower the next generation to become responsible environmental stewards.  Let\'s nurture their enthusiasm and help them understand that even small actions can have a big impact on the health of our planet.', '1750629587494_generated_d31cb492-6103-4ce8-a320-4a7ebfe59741.png', '2025-05-30 02:16:41', 1, 9, 'Published', 1),
(11, 'Harnessing the Power of the Sun: Solar Energy for Your Home', 'This post explores the benefits of installing solar panels on your home, highlighting the environmental advantages, cost savings, and the increasing accessibility of this clean energy source.  We\'ll cover the basics of solar energy, the installation process, and factors to consider before making the switch.', '## Harnessing the Power of the Sun: Solar Energy for Your Home\nThe sun—a boundless source of clean, renewable energy—is shining down on us every day.  For too long, we\'ve overlooked its potential, relying instead on fossil fuels that harm our planet. But times are changing.  More and more homeowners are discovering the benefits of harnessing solar power, and it\'s easier than ever to join the movement.\nThis post will guide you through the world of residential solar energy, helping you understand its advantages and how it can power your home.\n**Why Choose Solar?**\nThe benefits of solar energy are numerous:\n* **Environmental Friendliness:** Solar power is a clean energy source, producing no greenhouse gas emissions during operation.  By switching to solar, you\'re significantly reducing your carbon footprint and contributing to a healthier planet.\n* **Cost Savings:** While the initial investment can seem significant, solar panels drastically reduce your reliance on the power grid, leading to lower electricity bills over the long term.  Many governments also offer incentives and tax credits to encourage solar adoption.\n* **Increased Home Value:**  Studies show that homes with solar panels often sell for a higher price than comparable homes without them.  It\'s a smart investment that adds value to your property.\n* **Energy Independence:**  Solar panels give you a degree of energy independence, reducing your vulnerability to fluctuating electricity prices and potential power outages.\n**How Does it Work?**\nSolar panels, made of photovoltaic (PV) cells, convert sunlight directly into electricity. This electricity powers your home, and any excess energy is often fed back into the grid, earning you credits on your electricity bill (depending on your location and energy provider).\n**The Installation Process:**\nThe installation process typically involves:\n1. **Assessment:** A professional will assess your roof and energy consumption to determine the optimal system size.\n2. **Permitting:**  Necessary permits need to be obtained from your local authorities.\n3. **Installation:**  Experienced installers will mount the panels on your roof and connect them to your electrical system.\n4. **Monitoring:**  Many systems come with monitoring software that allows you to track your energy production and consumption.\n**Things to Consider:**\n* **Roof suitability:**  Your roof\'s orientation, shading, and structural integrity will impact the effectiveness of your solar system.\n* **System size:**  The size of your system depends on your energy needs and available roof space.\n* **Financing options:**  Various financing options are available, including loans, leases, and power purchase agreements (PPAs).\n* **Local regulations and incentives:**  Check with your local government for any available incentives or regulations regarding solar installations.\n**Conclusion:**\nSwitching to solar energy is a significant step towards a sustainable future.  While the initial investment requires careful consideration, the long-term benefits—environmental, financial, and personal—make it a worthwhile endeavor for many homeowners.  Do your research, consult with professionals, and take the first step towards powering your home with the clean energy of the sun.', '1750688045845_generated_03d2c6fb-7310-4bd1-b218-bb2f03a16ad6.png', '2025-06-18 09:48:58', 1, 4, 'Published', 0),
(140, 'Building a Sustainable Future: The Smart City of Tomorrow', 'This blog post explores the concept of a sustainable smart city, showcasing how renewable energy sources, innovative building design, and technological integration can create environmentally friendly and efficient urban environments.  We\'ll look at key elements like wind turbines, solar panels, and energy-efficient buildings, highlighting their roles in building a greener future.', '## Building a Sustainable Future: The Smart City of Tomorrow\nOur cities are at a crossroads.  Rapid urbanization presents significant challenges, primarily environmental concerns like pollution and resource depletion.  But within these challenges lies an opportunity: to build smarter, more sustainable cities that prioritize both human well-being and environmental responsibility.  The image above depicts a vision of this future, a vibrant and eco-conscious urban landscape powered by innovation.\nThis \"smart city\" isn\'t just a pipe dream; it\'s a tangible goal achievable through a combination of technological advancements and a shift in urban planning priorities. Let\'s examine the key elements illustrated:\n**1. Renewable Energy Integration:** The prominent wind turbines and solar panels are not mere aesthetic additions; they represent the backbone of a sustainable energy infrastructure.  Wind energy harnesses the power of the wind to generate electricity, while solar panels convert sunlight into clean, renewable energy.  This reliance on renewable sources significantly reduces the city\'s carbon footprint and dependence on fossil fuels.\n**2. Energy-Efficient Buildings:** The buildings themselves are designed with sustainability in mind.  Their architectural styles likely incorporate features that maximize natural light and ventilation, minimizing the need for artificial lighting and climate control.  Smart building technologies could further optimize energy consumption, adjusting lighting and temperature based on occupancy and external conditions.\n**3. Green Spaces and Urban Greenery:** Notice the trees and rooftop gardens integrated into the city\'s design.  These green spaces play a vital role in improving air quality, reducing the urban heat island effect, and enhancing the overall quality of life for residents.\n**4. Technological Integration:**  The lightbulb icon symbolizes the smart city\'s reliance on technology to manage resources efficiently.  This could include smart grids that optimize energy distribution, intelligent transportation systems that reduce traffic congestion, and data-driven approaches to waste management.\n**The Path Forward:**\nBuilding a sustainable smart city requires collaboration between governments, businesses, and citizens.  It necessitates investment in renewable energy infrastructure, the development of energy-efficient building codes, and the adoption of smart technologies.  But the rewards are immense: cleaner air, reduced pollution, improved quality of life, and a more resilient urban environment capable of withstanding the challenges of climate change.\nThe image is a powerful visual representation of a possible future.  It\'s a reminder that sustainable urban development isn\'t just about reducing our environmental impact; it\'s about creating thriving, equitable, and resilient cities for generations to come.  The journey towards this future requires commitment, innovation, and a shared vision of a more sustainable world.', '1750632593465_1750632589953_generated_11817f73-e155-4cf2-a2b1-26eb593f0eef.png', '2025-06-22 23:49:53', 92, 0, 'Published', 1),
(143, 'Harnessing the Sun: The Rise of Solar Power in Residential Homes', 'This post explores the increasing popularity of solar panels on residential homes, highlighting their environmental benefits, cost savings, and the aesthetic integration into modern homes. We\'ll discuss the factors driving this trend and what the future holds for solar energy in the residential sector.', '**Harnessing the Sun: The Rise of Solar Power in Residential Homes**\nThe sun, our planet\'s ultimate energy source, is increasingly powering our homes.  Solar panels, once a niche technology, are becoming a common sight on rooftops across the globe.  This shift reflects a growing awareness of environmental responsibility and the economic advantages of renewable energy.\nThe image above perfectly captures this trend.  It depicts a charming neighborhood where one home stands out – not because it\'s extravagant, but because it\'s equipped with a solar panel system.  This simple visual communicates the growing accessibility and normalcy of residential solar power.\n**Why the Solar Boom?**\nSeveral factors contribute to the surge in residential solar adoption:\n* **Environmental Concerns:**  Climate change is a pressing global issue, and individuals are seeking ways to reduce their carbon footprint.  Solar energy offers a clean and sustainable alternative to fossil fuels, significantly decreasing greenhouse gas emissions.\n* **Cost Savings:** While the initial investment can be substantial, solar panels ultimately reduce or even eliminate electricity bills.  Government incentives, such as tax credits and rebates, further lessen the financial burden, making solar power more affordable than ever.\n* **Technological Advancements:** Solar panel technology has advanced significantly, resulting in increased efficiency and durability.  Panels are now more aesthetically pleasing and seamlessly integrate into home designs.\n* **Energy Independence:**  Solar panels provide a degree of energy independence, reducing reliance on the traditional power grid.  This is particularly appealing in areas prone to power outages or those seeking greater self-sufficiency.\n**The Future is Bright (and Solar Powered)**\nThe trend toward residential solar power is expected to continue.  As technology improves and costs decrease, solar energy will become even more accessible and attractive to homeowners.  Furthermore, advancements in energy storage solutions, such as battery systems, will further enhance the reliability and appeal of solar power.\nThe image serves as a powerful symbol of this positive change. It shows that embracing renewable energy doesn\'t require sacrificing aesthetic appeal; it\'s a practical and visually pleasing way to build a sustainable future.  Consider exploring the possibilities of solar power for your own home – it\'s an investment in a cleaner planet and a brighter financial future.', '1750633605099_1750633596270_generated_f965b8aa-77dd-44e6-9219-20cc67cc2366.png', '2025-06-23 00:06:44', 92, 0, 'Published', 0);

-- --------------------------------------------------------

--
-- Table structure for table `blog_likes`
--

CREATE TABLE `blog_likes` (
  `id` int(11) NOT NULL,
  `blog_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `blog_likes`
--

INSERT INTO `blog_likes` (`id`, `blog_id`, `user_id`, `created_at`) VALUES
(13, 4, 1, '2025-05-29 09:25:16'),
(14, 9, 1, '2025-05-29 10:31:22'),
(18, 2, 96, '2025-05-30 21:56:58'),
(20, 10, 92, '2025-06-19 09:57:38'),
(22, 3, 92, '2025-06-19 10:07:54'),
(24, 9, 92, '2025-06-19 10:19:25'),
(25, 12, 92, '2025-06-19 12:25:44'),
(26, 140, 92, '2025-06-20 01:59:11');

-- --------------------------------------------------------

--
-- Table structure for table `categorie`
--

CREATE TABLE `categorie` (
  `id` int(11) NOT NULL,
  `nom` varchar(255) NOT NULL,
  `description` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `categorie`
--

INSERT INTO `categorie` (`id`, `nom`, `description`) VALUES
(1, 'Solar Energy', 'Products related to solar energy generation and storage.'),
(3, 'Energy Storage', 'Battery packs and energy storage solutions.'),
(4, 'Lighting', 'Energy-efficient lighting products like LED bulbs.'),
(5, 'Electric Vehicles', 'Accessories and chargers for electric vehicles.'),
(6, 'Smart Home', 'Smart home devices focused on energy efficiency.'),
(7, 'Hydropower', 'Products for small-scale hydroelectric power generation.'),
(8, 'Recycled Materials', 'Products made from recycled materials.'),
(9, 'Agriculture', 'Products for sustainable and eco-friendly agriculture.'),
(10, 'Office Supplies', 'Eco-friendly office supplies and stationery.'),
(14, 'solair', ''),
(27, 'Personal Electronics', ''),
(28, 'Waste Management', '');

-- --------------------------------------------------------

--
-- Table structure for table `commande`
--

CREATE TABLE `commande` (
  `id` int(11) NOT NULL,
  `dateCommande` datetime NOT NULL,
  `statut` varchar(50) NOT NULL,
  `total` double NOT NULL,
  `clientId` int(11) NOT NULL,
  `shipmentAddress` varchar(255) DEFAULT NULL,
  `estimatedDeliveryDate` date DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `commande`
--

INSERT INTO `commande` (`id`, `dateCommande`, `statut`, `total`, `clientId`, `shipmentAddress`, `estimatedDeliveryDate`) VALUES
(83, '2025-05-30 03:59:47', 'Completed', 11.5, 95, 'Zaghouan', '2025-06-19'),
(84, '2025-05-30 04:02:02', 'Pending', 11.5, 95, 'Zaghouan', '2025-06-03'),
(85, '2025-05-30 12:42:09', 'Paid', 688.62, 95, 'Zaghouan', '2025-06-03'),
(86, '2025-05-30 13:27:39', 'Paid', 65, 95, 'Zaghouan', '2025-06-03'),
(87, '2025-05-30 15:45:29', 'Pending', 76.5, 95, 'Zaghouan', '2025-06-03'),
(88, '2025-05-31 14:02:18', 'Completed', 65, 92, 'Ariana, Gafsa, 1150', '2025-06-04'),
(89, '2025-05-31 15:13:26', 'Completed', 180, 92, 'Ariana, Tunis, 1141', '2025-06-04'),
(90, '2025-06-17 19:56:28', 'Paid', 2799.99, 92, 'tunisia', '2025-06-21'),
(91, '2025-06-17 19:58:42', 'Paid', 167.01999999999998, 92, 'tunisia', '2025-06-21'),
(92, '2025-06-18 09:20:50', 'Paid', 340.56, 92, 'tunisia', '2025-06-22'),
(93, '2025-06-18 09:26:15', 'Paid', 65, 92, 'tunisia', '2025-06-22'),
(94, '2025-06-20 02:55:09', 'Paid', 65, 92, 'fahs, Bledi, 1141', '2025-06-24'),
(95, '2025-06-22 11:43:13', 'Paid', 1629.98, 92, 'Bir mcherga, Zaghouan, 1141', '2025-06-26'),
(96, '2025-06-23 00:19:54', 'Paid', 1639.99, 92, 'Zaghouan', '2025-06-27');

-- --------------------------------------------------------

--
-- Table structure for table `commandeproduit`
--

CREATE TABLE `commandeproduit` (
  `id` int(11) NOT NULL,
  `quantite` int(11) NOT NULL,
  `sousTotal` double NOT NULL,
  `commandeId` int(11) NOT NULL,
  `produitId` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `commandeproduit`
--

INSERT INTO `commandeproduit` (`id`, `quantite`, `sousTotal`, `commandeId`, `produitId`) VALUES
(115, 1, 11.5, 83, 22),
(116, 1, 11.5, 84, 22),
(117, 1, 65, 85, 23),
(118, 6, 612.12, 85, 16),
(119, 1, 11.5, 85, 22),
(120, 1, 65, 86, 23),
(121, 1, 65, 87, 23),
(122, 1, 11.5, 87, 22),
(123, 1, 65, 88, 23),
(124, 2, 50, 89, 24),
(125, 2, 130, 89, 23),
(126, 1, 1299.99, 90, 37),
(127, 1, 1500, 90, 39),
(128, 1, 65, 91, 23),
(129, 1, 102.02, 91, 16),
(130, 3, 34.5, 92, 22),
(131, 3, 306.06, 92, 16),
(132, 1, 65, 93, 23),
(133, 1, 65, 94, 23),
(134, 2, 130, 95, 23),
(135, 1, 599.99, 95, 35),
(136, 1, 899.99, 95, 36),
(137, 16, 1040, 96, 23),
(138, 1, 599.99, 96, 35);

-- --------------------------------------------------------

--
-- Table structure for table `commentaire`
--

CREATE TABLE `commentaire` (
  `id` int(11) NOT NULL,
  `contenu` text NOT NULL,
  `date` datetime NOT NULL,
  `blogId` int(11) NOT NULL,
  `auteurId` int(11) NOT NULL,
  `gif_url` varchar(255) DEFAULT NULL,
  `media_type` varchar(50) DEFAULT 'TEXT'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `commentaire`
--

INSERT INTO `commentaire` (`id`, `contenu`, `date`, `blogId`, `auteurId`, `gif_url`, `media_type`) VALUES
(6, 'hiihihi', '2025-05-14 23:41:02', 2, 1, NULL, 'TEXT'),
(7, 'hi', '2025-05-15 10:19:50', 3, 1, NULL, 'TEXT'),
(8, 'lolm', '2025-05-15 10:23:15', 3, 1, NULL, 'TEXT'),
(9, 'li', '2025-05-15 10:28:40', 3, 1, NULL, 'TEXT'),
(10, 'hihih', '2025-05-15 11:24:54', 2, 1, NULL, 'TEXT'),
(11, 'hi', '2025-05-15 13:04:24', 2, 1, NULL, 'TEXT'),
(12, '😒', '2025-05-22 10:09:49', 2, 1, NULL, 'TEXT'),
(13, '👍', '2025-05-22 10:10:06', 2, 1, NULL, 'TEXT'),
(14, ':smile', '2025-05-28 09:56:00', 2, 1, NULL, 'TEXT'),
(15, '<p>😊😄😆😆 hi</p>', '2025-05-28 11:56:06', 2, 1, NULL, 'TEXT'),
(16, 'https://media3.giphy.com/media/v1.Y2lkPWQ0YWY3NDA2a211YTJ2NjQxYXhucTRxcXc3dzQwdGoxbTFpOGpyd2tvY3NzaXB6byZlcD12MV9naWZzX3RyZW5kaW5nJmN0PWc/JksahSdnH6BX1WEtlc/100.gif', '2025-05-28 21:04:37', 2, 1, NULL, 'TEXT'),
(17, 'https://media3.giphy.com/media/v1.Y2lkPWQ0YWY3NDA2bTlxd242aDM0MGtuZjdyNHhmZG54NXBxaGFteHl6NmRydzgxaDE0NCZlcD12MV9naWZzX3NlYXJjaCZjdD1n/5K7ngCtszoxxbaBieC/giphy-preview.gif', '2025-05-28 21:24:17', 2, 1, NULL, 'TEXT'),
(18, 'lol https://media2.giphy.com/media/v1.Y2lkPWQ0YWY3NDA2ZjhnOG9nZzEzNng2enN1b3dxOTl6bnZwMHNlN2oyMjB5OXhrZTl6dCZlcD12MV9naWZzX3NlYXJjaCZjdD1n/ZqlvCTNHpqrio/giphy-preview.gif', '2025-05-28 21:31:42', 2, 1, NULL, 'TEXT'),
(19, 'https://media1.giphy.com/media/v1.Y2lkPWQ0YWY3NDA2OXQ4aXRkcWJyc2FxMW84bDI5NHNsa2gyam50eXNuYXdpc2lmcmc1NCZlcD12MV9naWZzX3NlYXJjaCZjdD1n/gw3IWyGkC0rsazTi/100w.gif', '2025-05-28 21:45:40', 9, 1, NULL, 'TEXT'),
(20, 'https://media1.giphy.com/media/v1.Y2lkPWQ0YWY3NDA2eno2eDA0ZTQ2eXNnYmxxeDVnMDZoemVneW04YXlsbG9iYmV4bzh6YSZlcD12MV9naWZzX3NlYXJjaCZjdD1n/26ufdipQqU2lhNA4g/giphy-preview.gif', '2025-05-29 10:25:07', 4, 1, NULL, 'TEXT'),
(21, 'https://media4.giphy.com/media/v1.Y2lkPWQ0YWY3NDA2ejc1ZDQwajIxNWN2eXQ3am8zMzN2dnVzOHV3MGF2dHMwY28yendlayZlcD12MV9naWZzX3NlYXJjaCZjdD1n/gj0QdZ9FgqGhOBNlFS/giphy-preview.gif', '2025-05-29 11:56:10', 3, 1, NULL, 'TEXT'),
(27, 'HELLO', '2025-05-30 01:38:08', 3, 95, NULL, 'TEXT'),
(28, 'ho', '2025-05-30 01:45:24', 3, 95, NULL, 'TEXT'),
(29, 'test', '2025-05-30 01:49:57', 3, 95, NULL, 'TEXT'),
(30, 'ok', '2025-05-30 01:51:42', 3, 95, NULL, 'TEXT'),
(31, 'hi', '2025-05-30 01:57:40', 3, 95, NULL, 'TEXT'),
(32, 'OMG', '2025-05-30 22:56:29', 2, 96, NULL, 'TEXT'),
(33, 'omg', '2025-05-30 22:57:11', 2, 96, NULL, 'TEXT'),
(34, 'yesssssss https://media0.giphy.com/media/v1.Y2lkPWQ0YWY3NDA2anFnZHlibml0NWtqcXBlbG94NjUxYjRwMXNhejh6N2RkazBzNm5mdiZlcD12MV9naWZzX3NlYXJjaCZjdD1n/sr8jYZVVsCmxddga8w/giphy-preview.gif', '2025-05-31 13:58:15', 2, 92, NULL, 'TEXT'),
(35, 'heheh', '2025-05-31 14:00:09', 2, 92, NULL, 'TEXT'),
(41, 'https://media0.giphy.com/media/v1.Y2lkPWQ0YWY3NDA2MzF1b2xuaDg1NjdhZXU3OHZpdGc1emJ2Y2oxZHpwYWs5N3VqbjJyNSZlcD12MV9naWZzX3RyZW5kaW5nJmN0PWc/l0Exf8PCkLThVaUUM/giphy-preview.gif', '2025-06-20 02:59:16', 140, 92, NULL, 'TEXT');

-- --------------------------------------------------------

--
-- Table structure for table `comment_likes`
--

CREATE TABLE `comment_likes` (
  `id` int(11) NOT NULL,
  `comment_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `comment_likes`
--

INSERT INTO `comment_likes` (`id`, `comment_id`, `user_id`, `created_at`) VALUES
(1, 9, 1, '2025-05-29 10:55:55'),
(2, 8, 1, '2025-05-29 10:55:58'),
(3, 7, 1, '2025-05-29 10:55:59'),
(5, 34, 92, '2025-05-31 12:58:26'),
(6, 7, 92, '2025-06-18 10:41:36'),
(8, 27, 92, '2025-06-18 10:41:38'),
(10, 29, 92, '2025-06-19 10:07:58'),
(11, 21, 92, '2025-06-19 10:08:00'),
(14, 19, 92, '2025-06-19 10:24:53'),
(15, 31, 92, '2025-06-19 10:27:12'),
(17, 41, 92, '2025-06-20 01:59:19'),
(18, 40, 92, '2025-06-22 23:12:07'),
(19, 8, 92, '2025-06-22 23:12:09');

-- --------------------------------------------------------

--
-- Table structure for table `comment_replies`
--

CREATE TABLE `comment_replies` (
  `id` int(11) NOT NULL,
  `contenu` text NOT NULL,
  `date_reply` timestamp NOT NULL DEFAULT current_timestamp(),
  `auteur_id` int(11) NOT NULL,
  `comment_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `comment_replies`
--

INSERT INTO `comment_replies` (`id`, `contenu`, `date_reply`, `auteur_id`, `comment_id`) VALUES
(1, 'hello', '2025-05-28 11:25:30', 1, 15),
(2, 'dret', '2025-05-28 11:25:47', 1, 15),
(3, 'hi', '2025-05-28 11:26:46', 1, 6),
(8, 'test', '2025-06-19 10:29:43', 92, 19),
(11, 'ok', '2025-06-19 10:40:20', 92, 37),
(14, 'https://media3.giphy.com/media/v1.Y2lkPWQ0YWY3NDA2bzE1OHRwcHF1ejVwc2o5OGlvNzFwbjB4aXBhcjF1cHh3bm1vdzNqOSZlcD12MV9naWZzX3RyZW5kaW5nJmN0PWc/r2BUOQEHvbaYFaCx8Q/giphy-preview.gif', '2025-06-19 12:25:34', 92, 39),
(15, 'https://media4.giphy.com/media/v1.Y2lkPWQ0YWY3NDA2cnNleTR4NHRscnoyM3JmbDkyMzQycDV3bm4wNmhzNW93Mm9ocGZndyZlcD12MV9naWZzX3RyZW5kaW5nJmN0PWc/ayPDNL3unkmXrbnCGB/100.gif', '2025-06-19 19:25:38', 92, 19);

-- --------------------------------------------------------

--
-- Table structure for table `contract`
--

CREATE TABLE `contract` (
  `id` int(11) NOT NULL,
  `startDate` datetime NOT NULL,
  `duration` int(11) NOT NULL,
  `utilisateurId` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `dechet`
--

CREATE TABLE `dechet` (
  `id` int(11) NOT NULL,
  `type` varchar(100) NOT NULL,
  `quantite` float NOT NULL,
  `date` datetime NOT NULL,
  `utilisateurId` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `email_verification_tokens`
--

CREATE TABLE `email_verification_tokens` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `token` varchar(191) NOT NULL,
  `expiry_date` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `email_verification_tokens`
--

INSERT INTO `email_verification_tokens` (`id`, `user_id`, `token`, `expiry_date`) VALUES
(5, 86, '701967', '2025-05-10 15:03:20'),
(6, 87, '939477', '2025-05-10 19:14:47'),
(14, 1, '901045', '2025-05-17 18:01:08'),
(18, 93, '147258', '2025-05-27 22:51:56'),
(12, 89, '720402', '2025-05-14 22:59:48'),
(15, 90, '664906', '2025-05-17 20:10:09'),
(16, 91, '701182', '2025-05-19 19:55:01'),
(25, 92, '301202', '2025-06-01 12:27:32'),
(24, 97, '950059', '2025-06-01 11:14:38'),
(20, 96, '118972', '2025-05-31 15:29:07');

-- --------------------------------------------------------

--
-- Table structure for table `evenements`
--

CREATE TABLE `evenements` (
  `id` int(11) NOT NULL,
  `titre` varchar(255) NOT NULL,
  `description` text DEFAULT NULL,
  `date` datetime DEFAULT NULL,
  `adresse` varchar(255) DEFAULT NULL,
  `organisateur` int(11) DEFAULT NULL,
  `posterImageUrl` text DEFAULT NULL,
  `reservationLimit` int(11) DEFAULT 0,
  `priceCents` bigint(20) DEFAULT 0
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `evenements`
--

INSERT INTO `evenements` (`id`, `titre`, `description`, `date`, `adresse`, `organisateur`, `posterImageUrl`, `reservationLimit`, `priceCents`) VALUES
(2, 'test 2', 'test2', '2025-05-30 08:12:00', 'test', 92, 'file:/C:/Users/Nasri/Desktop/dfgdfg/New%20folder/for%20me/src/main/resources/images/default_renewable.png', 20, 500),
(3, 'test ', 'party for dog owners', '2025-05-28 10:10:00', 'centre ville', 92, 'file:///C:/Users/Nasri/AppData/Local/Temp/poster_evt3_1862076211131365861/poster_7f0b7862-8377-49e7-a721-9f23753f9544.png', 10, 500),
(4, 'test3', 'zaertyuiop', '2025-05-29 12:00:00', 'zerdtfyguhij', 92, 'file:/C:/Users/yosrn/Downloads/téléchargement.jpg', 0, 100),
(5, 'café creatif', 'atelier café creatif , ceramique , peinture avec un bon café', '2025-05-30 12:10:00', 'centre ville', 92, 'file:/C:/Users/yosrn/Downloads/White%20Minimalist%20Profile%20LinkedIn%20Banner.png', 10, 150);

-- --------------------------------------------------------

--
-- Table structure for table `evenement_participants`
--

CREATE TABLE `evenement_participants` (
  `Evenement_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `events`
--

CREATE TABLE `events` (
  `id` int(11) NOT NULL,
  `titre` varchar(255) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `date` datetime DEFAULT NULL,
  `adresse` varchar(255) DEFAULT NULL,
  `organisateur_id` int(11) DEFAULT NULL,
  `poster_image_url` varchar(255) DEFAULT NULL,
  `reservation_limit` int(11) DEFAULT NULL,
  `price_cents` bigint(20) DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `feedbacks`
--

CREATE TABLE `feedbacks` (
  `id` int(11) NOT NULL,
  `commentaire` text DEFAULT NULL,
  `note` int(11) DEFAULT NULL,
  `date` datetime DEFAULT NULL,
  `utilisateur_id` int(11) DEFAULT NULL,
  `evenement_id` int(11) DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `feedbacks`
--

INSERT INTO `feedbacks` (`id`, `commentaire`, `note`, `date`, `utilisateur_id`, `evenement_id`) VALUES
(2, 'good', 5, '2025-05-26 23:30:17', 92, 1),
(4, 'hi', 5, '2025-05-27 00:05:40', 92, 1),
(5, 'hhhhhhhhhh', 5, '2025-05-27 00:06:58', 92, 1),
(9, 'test update feedbak', 4, '2025-05-27 14:56:11', 92, 5),
(7, 'awsome', 4, '2025-05-27 12:06:28', 92, 3),
(10, 'hello', 2, '2025-05-27 23:31:40', 92, 3);

-- --------------------------------------------------------

--
-- Table structure for table `mfa_persistent_tokens`
--

CREATE TABLE `mfa_persistent_tokens` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `device_identifier` varchar(191) NOT NULL,
  `token` varchar(255) NOT NULL,
  `expiry_date` datetime NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `mfa_persistent_tokens`
--

INSERT INTO `mfa_persistent_tokens` (`id`, `user_id`, `device_identifier`, `token`, `expiry_date`) VALUES
(1, 0, 'DEVICE_IDENTIFIER', '28b78a3a-3280-11f0-886f-c85b76e52cea', '2025-06-15 19:04:06'),
(2, 11, 'your_device_identifier_here', '56bb71f4-3281-11f0-886f-c85b76e52cea', '2025-06-15 19:12:33'),
(3, 90, '765ee54d-692d-40cf-93e2-4f475ae6d5aa', 'jqi3mpmdf4e5k53ckd83ufqjsu', '2025-06-15 21:11:30'),
(4, 90, '64a43e15-bccb-4e9c-92f4-5346a07f9d04', 's628ntqsbt3rq55vkha8j63mqo', '2025-06-15 22:24:59'),
(5, 11, '64a43e15-bccb-4e9c-92f4-5346a07f9d04', '3dgkc5n826q4decu7rb0l91tl2', '2025-06-16 10:17:51'),
(6, 91, '64a43e15-bccb-4e9c-92f4-5346a07f9d04', 'utqq6juce5q3ogn4sv0km241ka', '2025-06-17 20:56:28'),
(7, 92, '2d48effc-5b25-4792-9cfa-0bb5591e5f03', 'i1gltcd4b5r13krimgca1nui3p', '2025-06-25 21:42:14'),
(8, 93, '2d48effc-5b25-4792-9cfa-0bb5591e5f03', 'vfuj6a1t80oobohortq7vn89f8', '2025-06-25 22:53:23'),
(9, 92, '64a43e15-bccb-4e9c-92f4-5346a07f9d04', 'emg47q3m05qs2i5c40r096hto8', '2025-06-26 23:11:11'),
(10, 95, 'a6325bf8-18e6-4a57-84ac-2bfcaae5ca50', '9o1l2baqg2lskpod923jils5a1', '2025-06-28 21:34:03'),
(11, 96, '64a43e15-bccb-4e9c-92f4-5346a07f9d04', 'c7h2pch8qgb0p7dkaq1l3av0br', '2025-06-29 22:56:05'),
(12, 95, '64a43e15-bccb-4e9c-92f4-5346a07f9d04', '95te20hkvu0pp6arrrumf4pcmm', '2025-06-30 08:21:02'),
(14, 92, 'a6325bf8-18e6-4a57-84ac-2bfcaae5ca50', '4uo8ve0iskk4vbogm2bcevnkkh', '2025-06-30 13:28:05');

-- --------------------------------------------------------

--
-- Table structure for table `panier`
--

CREATE TABLE `panier` (
  `id` int(11) NOT NULL,
  `utilisateurId` int(11) NOT NULL,
  `total` double NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `panier`
--

INSERT INTO `panier` (`id`, `utilisateurId`, `total`) VALUES
(12, 95, 0),
(41, 96, 0),
(42, 92, 0);

-- --------------------------------------------------------

--
-- Table structure for table `panierproduit`
--

CREATE TABLE `panierproduit` (
  `id` int(11) NOT NULL,
  `quantite` int(11) NOT NULL,
  `sousTotal` double NOT NULL,
  `panierId` int(11) NOT NULL,
  `produitId` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `posts`
--

CREATE TABLE `posts` (
  `id` int(11) NOT NULL,
  `title` varchar(255) NOT NULL,
  `content` text NOT NULL,
  `user_id` int(11) NOT NULL,
  `created_at` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `produit`
--

CREATE TABLE `produit` (
  `id` int(11) NOT NULL,
  `nom` varchar(255) NOT NULL,
  `description` text DEFAULT NULL,
  `prix` double NOT NULL,
  `quantiteStock` int(11) NOT NULL,
  `image` varchar(500) DEFAULT NULL,
  `recyclable` tinyint(1) NOT NULL,
  `categorieId` int(11) NOT NULL,
  `companyName` varchar(255) DEFAULT NULL,
  `ownerId` int(11) DEFAULT NULL,
  `companyLogo` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `produit`
--

INSERT INTO `produit` (`id`, `nom`, `description`, `prix`, `quantiteStock`, `image`, `recyclable`, `categorieId`, `companyName`, `ownerId`, `companyLogo`) VALUES
(35, 'Solar Water Pump for Urban Farms', 'Low-voltage water pump for vertical & rooftop farming', 135, 40, 'solar_water_pump_for_urban_farms_1750687533296.jpg', 1, 1, 'AgroSmart', 0, 'logo_agrosmart_1750687533293.jpg'),
(36, 'Transparent Solar Window Panel', 'Glass-like solar panels for smart buildings', 520, 30, 'transparent_solar_window_panel_1750687760955.jpg', 1, 1, 'ClearVolt', 0, 'logo_clearvolt_1750687760952.jpg'),
(41, 'Portable Foldable Solar Charger', 'Lightweight, waterproof solar charger for electronics', 79, 50, 'portable_foldable_solar_charger_1750687045116.jpg', 1, 27, 'EcoCharge', 0, 'logo_ecocharge_1750687220322.jpg'),
(42, 'Smart Solar Trash Bin', 'Automatically compresses waste and alerts collection teams', 1450, 15, 'smart_solar_trash_bin_1750687183209.jpg', 1, 28, 'CleanCity Systems', 0, 'logo_cleancity_systems_1750687183206.jpg'),
(43, 'Home Solar Panel Kit', 'Complete plug-and-play solar panel kit with app monitoring', 4500, 12, 'home_solar_panel_kit_1750687923315.jpg', 1, 6, 'ClearVolt', 0, 'logo_clearvolt_1750687923313.jpg');

-- --------------------------------------------------------

--
-- Table structure for table `reclamation`
--

CREATE TABLE `reclamation` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `report` text NOT NULL,
  `status` varchar(50) DEFAULT 'Pending',
  `created_at` timestamp NULL DEFAULT current_timestamp()
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `reclamation`
--

INSERT INTO `reclamation` (`id`, `user_id`, `report`, `status`, `created_at`) VALUES
(1, 1, 'Example claim report: The product arrived damaged.', 'Pending', '2025-05-03 23:32:20'),
(2, 1, 'Example claim report: The product arrived damaged.', 'Pending', '2025-05-03 23:37:44'),
(3, 2, 'Late delivery of the order.', 'Pending', '2025-05-03 23:37:44');

-- --------------------------------------------------------

--
-- Table structure for table `reports`
--

CREATE TABLE `reports` (
  `id` int(11) NOT NULL,
  `title` varchar(255) NOT NULL,
  `description` text DEFAULT NULL,
  `generated_date` timestamp NULL DEFAULT current_timestamp(),
  `report_type` varchar(50) NOT NULL,
  `file_path` varchar(255) DEFAULT NULL,
  `report_data` longblob DEFAULT NULL,
  `created_by` int(11) DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `reservations`
--

CREATE TABLE `reservations` (
  `id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `event_id` int(11) DEFAULT NULL,
  `date` datetime DEFAULT NULL,
  `price_cents` bigint(20) DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `reservations`
--

INSERT INTO `reservations` (`id`, `user_id`, `event_id`, `date`, `price_cents`) VALUES
(4, 92, 4, '2025-05-27 10:06:21', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `sms_verification_tokens`
--

CREATE TABLE `sms_verification_tokens` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `token` varchar(191) NOT NULL,
  `expiry_date` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `sms_verification_tokens`
--

INSERT INTO `sms_verification_tokens` (`id`, `user_id`, `token`, `expiry_date`) VALUES
(10, 88, '990854', '2025-06-01 07:36:23'),
(17, 97, '677606', '2025-06-01 08:50:39');

-- --------------------------------------------------------

--
-- Table structure for table `tickets`
--

CREATE TABLE `tickets` (
  `id` varchar(100) NOT NULL,
  `reservation_id` int(11) DEFAULT NULL,
  `pdf_path` varchar(255) DEFAULT NULL,
  `qr_code_data` text DEFAULT NULL,
  `generated_at` datetime DEFAULT NULL,
  `used` tinyint(1) DEFAULT 0
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `truck`
--

CREATE TABLE `truck` (
  `id` int(11) NOT NULL,
  `capaciteMax` float NOT NULL,
  `niveauRemplissageActuel` float NOT NULL,
  `section` varchar(50) NOT NULL,
  `statut` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `nom` varchar(100) NOT NULL,
  `prenom` varchar(100) NOT NULL,
  `birth_date` datetime NOT NULL,
  `email` varchar(100) NOT NULL,
  `adresse` varchar(25) NOT NULL,
  `telephone` varchar(25) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` varchar(50) NOT NULL,
  `status` varchar(25) NOT NULL,
  `description` text DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `last_login` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `nom`, `prenom`, `birth_date`, `email`, `adresse`, `telephone`, `password`, `role`, `status`, `description`, `created_at`, `last_login`) VALUES
(11, 'Admin', 'User', '1980-01-01 00:00:00', 'admin@example.com', '123 Admin St', '1234567890', '$2a$10$Dow1Q6Xq6Xq6Xq6Xq6Xq6u6Xq6Xq6Xq6Xq6Xq6Xq6Xq6Xq6Xq6Xq6', 'ADMIN', 'confirmed', 'Administrator account', '2025-05-29 20:11:55', '2025-05-29 20:11:55'),
(57, 'Maaloul', 'Yasmine', '1993-09-18 00:00:00', 'yasmine.maaloul@example.com', '99 Rue Ibn Khaldoun, Sous', '24345678', 'yasmine93@', 'CLIENT', 'banned', '', '2025-05-19 02:22:30', NULL),
(58, 'Kefi', 'Houssem', '1986-02-25 00:00:00', 'houssem.kefi@example.com', 'Rue de l’Indépendance, Ka', '25543210', 'kefiH86#', 'CLIENT', 'CONFIRMED', '', '2025-05-19 02:22:30', NULL),
(59, 'Bouazizi', 'Rim', '1997-06-05 00:00:00', 'rim.bouazizi@example.com', 'Rue de la République, Gab', '26112233', 'rimBoua97', 'PRODUCT_OWNER', 'CONFIRMED', '', '2025-05-19 02:22:30', NULL),
(60, 'Zribi', 'Fares', '1988-03-14 00:00:00', 'fares.zribi@example.com', 'Avenue Hédi Chaker, Bizer', '27334455', 'faresZ88!', 'CLIENT', 'NON_CONFIRMED', '', '2025-05-19 02:22:30', NULL),
(61, 'Ben Rejeb', 'Nour', '1992-10-01 00:00:00', 'nour.benrejeb@example.com', 'Rue 14 Janvier, Nabeul', '29123344', 'nourR92$', 'EVENT_PLANNER', 'CONFIRMED', '', '2025-05-19 02:22:30', NULL),
(62, 'Chaabane', 'Walid', '1984-01-17 00:00:00', 'walid.chaabane@example.com', 'Rue Farhat Hached, Mahdia', '29887766', 'walidC84@', 'CLIENT', 'BANNED', '', '2025-05-19 02:22:30', NULL),
(63, 'Brahmi', 'Lina', '1990-08-21 00:00:00', 'lina.brahmi@example.com', 'Avenue Bourguiba, Monasti', '30224466', 'linaB90$', 'CLIENT', 'CONFIRMED', '', '2025-05-19 02:22:30', NULL),
(64, 'Jaziri', 'Ahmed', '1982-05-10 00:00:00', 'ahmed.jaziri@example.com', 'Rue Mongi Slim, Tunis', '31334422', 'ahmedJ82', 'ADMIN', 'CONFIRMED', '', '2025-05-19 02:22:30', NULL),
(65, 'Ben Mbarek', 'Syrine', '1996-11-02 00:00:00', 'syrine.benmbarek@example.com', 'Rue Taieb Mhiri, Sfax', '32112288', 'syrineBM96', 'CLIENT', 'CONFIRMED', '', '2025-05-19 02:22:30', NULL),
(66, 'Feki', 'Mehdi', '1985-12-19 00:00:00', 'mehdi.feki@example.com', 'Rue de Sidi Bou Said, La ', '33223311', 'fekiM85!', 'PRODUCT_OWNER', 'banned', '', '2025-05-19 02:22:30', NULL),
(67, 'Khalfallah', 'Ines', '1998-07-07 00:00:00', 'ines.khalfallah@example.com', 'Rue Ennasr, Ariana', '34119900', 'inesK98@', 'CLIENT', 'CONFIRMED', '', '2025-05-19 02:22:30', NULL),
(68, 'Ayari', 'Tarek', '1991-03-03 00:00:00', 'tarek.ayari@example.com', 'Rue El Menzah 6, Tunis', '35118877', 'tarekA91$', 'CLIENT', 'BANNED', '', '2025-05-19 02:22:30', NULL),
(69, 'Masmoudi', 'Dorsaf', '1994-09-25 00:00:00', 'dorsaf.masmoudi@example.com', 'Avenue Habib Thameur, Sou', '36224411', 'dorsafM94#', 'EVENT_PLANNER', 'CONFIRMED', '', '2025-05-19 02:22:30', NULL),
(70, 'Karray', 'Omar', '1989-06-16 00:00:00', 'omar.karray@example.com', 'Rue de Ras Jebel, Bizerte', '37112233', 'omarK89@', 'CLIENT', 'confirmed', '', '2025-05-19 02:22:30', NULL),
(71, 'Sliti', 'Rania', '1993-01-09 00:00:00', 'rania.sliti@example.com', 'Rue de l’Université, Gabè', '38991100', 'raniaS93!', 'PRODUCT_OWNER', 'CONFIRMED', '', '2025-05-19 02:22:30', NULL),
(72, 'Cherif', 'Adel', '1983-04-04 00:00:00', 'adel.cherif@example.com', 'Rue Ibn Sina, Monastir', '39445577', 'adelC83$', 'CLIENT', 'CONFIRMED', '', '2025-05-19 02:22:30', NULL),
(73, 'Hajri', 'Nadia', '1999-12-12 00:00:00', 'nadia.hajri@example.com', 'Rue de l’Aéroport, Djerba', '40112244', 'nadiaH99!', 'CLIENT', 'CONFIRMED', '', '2025-05-19 02:22:30', NULL),
(84, 'lola', 'lola', '1999-05-12 00:00:00', 'wael.chenoufi@esprit.tn', 'tunis', '23803535', '$2a$10$12KBZZyjvIJfy.4z9gJLe.wz5pmJY9LCGAl3mRXZvjyzWlp0HCUPG', 'USER', 'non_confirmed', '', '2025-05-19 02:22:30', NULL),
(85, 'xxxxxx', 'xxxxxxxx', '2000-05-31 00:00:00', 'chenoufiwael2@gmail.com', 'tunis', '23803535', '$2a$10$DpTehxNjsQnEgDS2e6wRTuXaZJ2dgHrOU.uGp3ML.6MQz8GKyOmJm', 'USER', 'non_confirmed', '', '2025-05-19 02:22:30', NULL),
(88, 'lola', 'lola', '1999-05-07 00:00:00', 'waelchenoufi2@gmail.com', 'tunis', '23803535', '$2a$10$EhxamviqoAtfih6gi2GLue0y5RKyXXpXNzUmrbVD3oIG1hqjK0yn2', 'CLIENT', 'verified', '', '2025-05-19 02:22:30', NULL),
(91, 'nasri', 'nasri', '1999-05-13 00:00:00', 'nasri213a@gmail.com', 'tunis', '+21623803535', '$2a$10$/5F/lRMrxLWeg4vHHQe/VuqjDGcWtfT0ZY/tVM1d8/RvPKP2awCci', 'UNKNOWN', 'verified', NULL, '2025-05-19 02:22:30', NULL),
(92, 'Nasri', 'Ayari', '2002-05-15 00:00:00', 'houdadalar123@gmail.com', 'Zaghouan', '+21624031727', '$2a$10$CZo89Ts2R/E8isDueJFEi.RsJ5m6rPsZFGuRKWYuVdv.a.YrrD1P2', 'CLIENT', 'confirmed', NULL, '2025-05-25 20:01:38', NULL),
(93, 'test', 'user', '2000-01-01 00:00:00', 'test.user@example.com', '123 rue principale', '+21612345678', '$2a$10$tB16WG7b8f1CbCOfsG4yWONqZ6JF8QP4btPvZBp43KPhPio80lY/C', 'CLIENT', 'confirmed', 'Ceci est un utilisateur de test.', '2025-05-26 22:49:08', '2025-05-26 22:49:08'),
(94, 'add user test', 'update test', '2005-05-20 00:00:00', 'zaer@zedrf.com', 'sqdzerfty', '98708758', '$2a$10$jp.M5Hl5W3oJKhbGzpPW8uhvg6KvDMCdPNVz73S6xLc8UJeWshQP2', 'CLIENT', 'confirmed', NULL, '2025-05-27 20:44:04', NULL),
(95, 'Nasri', 'Ayari', '2004-05-19 00:00:00', 'rinkes16@gmail.com', 'Zaghouan', '+2162403172', '$2a$10$2kEdquhpHX6FVMSNgGzvleGaJPOiIYN3fGzoMS9e8gSl1.yBPo7Fm', 'ADMIN', 'confirmed', NULL, '2025-05-29 21:32:52', NULL),
(97, 'wael', 'chenoufi', '2000-05-04 00:00:00', 'wael2chenoufi@gmail.com', 'tunis', '+21623803535', '$2a$10$CZo89Ts2R/E8isDueJFEi.RsJ5m6rPsZFGuRKWYuVdv.a.YrrD1P2', 'USER', 'non_confirmed', NULL, '2025-05-31 08:24:19', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `user_profiles`
--

CREATE TABLE `user_profiles` (
  `id` int(11) NOT NULL COMMENT 'Primary key',
  `user_id` int(11) NOT NULL COMMENT 'Foreign key reference to users table',
  `profile_photo_url` varchar(255) DEFAULT NULL COMMENT 'URL or path to user profile photo',
  `description` text DEFAULT NULL COMMENT 'User profile description',
  `created_at` datetime DEFAULT current_timestamp() COMMENT 'Record creation timestamp',
  `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp() COMMENT 'Record last update timestamp'
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `user_profiles`
--

INSERT INTO `user_profiles` (`id`, `user_id`, `profile_photo_url`, `description`, `created_at`, `updated_at`) VALUES
(8, 92, 'file:/C:/Users/Nasri/Desktop/HELLO/Final/pi-java/pi-java/src/main/resources/images/profile_photo/houdadalar123_gmail.com.jpg', '', '2025-06-22 22:35:31', '2025-06-23 00:04:33'),
(5, 1, '', '', '2025-05-29 20:40:01', '2025-05-29 20:40:01'),
(6, 95, 'file:/C:/Users/Nasri/Desktop/Desktop/CIN/AyariNasrEddine.jpg', '', '2025-05-29 21:38:50', '2025-05-29 21:39:07'),
(7, 96, '', '', '2025-05-30 16:30:00', '2025-05-30 16:30:00');

-- --------------------------------------------------------

--
-- Table structure for table `wishlist`
--

CREATE TABLE `wishlist` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `product_id` int(11) NOT NULL,
  `date_added` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `bin`
--
ALTER TABLE `bin`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `blog`
--
ALTER TABLE `blog`
  ADD PRIMARY KEY (`id`),
  ADD KEY `auteurId` (`auteurId`);

--
-- Indexes for table `blog_likes`
--
ALTER TABLE `blog_likes`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_blog_user` (`blog_id`,`user_id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `categorie`
--
ALTER TABLE `categorie`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `commande`
--
ALTER TABLE `commande`
  ADD PRIMARY KEY (`id`),
  ADD KEY `utilisateurId` (`clientId`);

--
-- Indexes for table `commandeproduit`
--
ALTER TABLE `commandeproduit`
  ADD PRIMARY KEY (`id`),
  ADD KEY `commandeId` (`commandeId`),
  ADD KEY `produitId` (`produitId`);

--
-- Indexes for table `commentaire`
--
ALTER TABLE `commentaire`
  ADD PRIMARY KEY (`id`),
  ADD KEY `auteurId` (`auteurId`),
  ADD KEY `fk_blog_id` (`blogId`);

--
-- Indexes for table `comment_likes`
--
ALTER TABLE `comment_likes`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_comment_user` (`comment_id`,`user_id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `comment_replies`
--
ALTER TABLE `comment_replies`
  ADD PRIMARY KEY (`id`),
  ADD KEY `auteur_id` (`auteur_id`),
  ADD KEY `comment_id` (`comment_id`);

--
-- Indexes for table `contract`
--
ALTER TABLE `contract`
  ADD PRIMARY KEY (`id`),
  ADD KEY `utilisateurId` (`utilisateurId`);

--
-- Indexes for table `dechet`
--
ALTER TABLE `dechet`
  ADD PRIMARY KEY (`id`),
  ADD KEY `utilisateurId` (`utilisateurId`);

--
-- Indexes for table `email_verification_tokens`
--
ALTER TABLE `email_verification_tokens`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `token` (`token`),
  ADD KEY `fk_user` (`user_id`);

--
-- Indexes for table `evenements`
--
ALTER TABLE `evenements`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_organisateur` (`organisateur`);

--
-- Indexes for table `evenement_participants`
--
ALTER TABLE `evenement_participants`
  ADD PRIMARY KEY (`Evenement_id`,`user_id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `events`
--
ALTER TABLE `events`
  ADD PRIMARY KEY (`id`),
  ADD KEY `organisateur_id` (`organisateur_id`);

--
-- Indexes for table `feedbacks`
--
ALTER TABLE `feedbacks`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`utilisateur_id`),
  ADD KEY `event_id` (`evenement_id`);

--
-- Indexes for table `mfa_persistent_tokens`
--
ALTER TABLE `mfa_persistent_tokens`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_device_user` (`user_id`,`device_identifier`);

--
-- Indexes for table `panier`
--
ALTER TABLE `panier`
  ADD PRIMARY KEY (`id`),
  ADD KEY `utilisateurId` (`utilisateurId`);

--
-- Indexes for table `panierproduit`
--
ALTER TABLE `panierproduit`
  ADD PRIMARY KEY (`id`),
  ADD KEY `panierId` (`panierId`),
  ADD KEY `produitId` (`produitId`);

--
-- Indexes for table `posts`
--
ALTER TABLE `posts`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `produit`
--
ALTER TABLE `produit`
  ADD PRIMARY KEY (`id`),
  ADD KEY `categorieId` (`categorieId`);

--
-- Indexes for table `reclamation`
--
ALTER TABLE `reclamation`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_user` (`user_id`);

--
-- Indexes for table `reports`
--
ALTER TABLE `reports`
  ADD PRIMARY KEY (`id`),
  ADD KEY `created_by` (`created_by`);

--
-- Indexes for table `reservations`
--
ALTER TABLE `reservations`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `event_id` (`event_id`);

--
-- Indexes for table `sms_verification_tokens`
--
ALTER TABLE `sms_verification_tokens`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `token` (`token`),
  ADD KEY `fk_user_sms` (`user_id`);

--
-- Indexes for table `tickets`
--
ALTER TABLE `tickets`
  ADD PRIMARY KEY (`id`),
  ADD KEY `reservation_id` (`reservation_id`);

--
-- Indexes for table `truck`
--
ALTER TABLE `truck`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- Indexes for table `user_profiles`
--
ALTER TABLE `user_profiles`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `id` (`id`),
  ADD KEY `idx_user_profiles_user_id` (`user_id`);

--
-- Indexes for table `wishlist`
--
ALTER TABLE `wishlist`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_wishlist_user` (`user_id`),
  ADD KEY `fk_wishlist_product` (`product_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `bin`
--
ALTER TABLE `bin`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `blog`
--
ALTER TABLE `blog`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=144;

--
-- AUTO_INCREMENT for table `blog_likes`
--
ALTER TABLE `blog_likes`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=27;

--
-- AUTO_INCREMENT for table `categorie`
--
ALTER TABLE `categorie`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=29;

--
-- AUTO_INCREMENT for table `commande`
--
ALTER TABLE `commande`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=97;

--
-- AUTO_INCREMENT for table `commandeproduit`
--
ALTER TABLE `commandeproduit`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=139;

--
-- AUTO_INCREMENT for table `commentaire`
--
ALTER TABLE `commentaire`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=42;

--
-- AUTO_INCREMENT for table `comment_likes`
--
ALTER TABLE `comment_likes`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=20;

--
-- AUTO_INCREMENT for table `comment_replies`
--
ALTER TABLE `comment_replies`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;

--
-- AUTO_INCREMENT for table `contract`
--
ALTER TABLE `contract`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `dechet`
--
ALTER TABLE `dechet`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `email_verification_tokens`
--
ALTER TABLE `email_verification_tokens`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=26;

--
-- AUTO_INCREMENT for table `evenements`
--
ALTER TABLE `evenements`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `events`
--
ALTER TABLE `events`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `feedbacks`
--
ALTER TABLE `feedbacks`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `mfa_persistent_tokens`
--
ALTER TABLE `mfa_persistent_tokens`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;

--
-- AUTO_INCREMENT for table `panier`
--
ALTER TABLE `panier`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=43;

--
-- AUTO_INCREMENT for table `panierproduit`
--
ALTER TABLE `panierproduit`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=171;

--
-- AUTO_INCREMENT for table `posts`
--
ALTER TABLE `posts`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `produit`
--
ALTER TABLE `produit`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=44;

--
-- AUTO_INCREMENT for table `reclamation`
--
ALTER TABLE `reclamation`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `reports`
--
ALTER TABLE `reports`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `reservations`
--
ALTER TABLE `reservations`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `sms_verification_tokens`
--
ALTER TABLE `sms_verification_tokens`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=18;

--
-- AUTO_INCREMENT for table `truck`
--
ALTER TABLE `truck`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=98;

--
-- AUTO_INCREMENT for table `user_profiles`
--
ALTER TABLE `user_profiles`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Primary key', AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `wishlist`
--
ALTER TABLE `wishlist`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=69;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
