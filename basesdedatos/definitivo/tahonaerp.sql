-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: localhost    Database: tahonaerp
-- ------------------------------------------------------
-- Server version	8.0.41

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `albaran_venta_lineas`
--

DROP TABLE IF EXISTS `albaran_venta_lineas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `albaran_venta_lineas` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `albaran_id` bigint DEFAULT NULL,
  `articulo_id` bigint DEFAULT NULL,
  `cantidad` decimal(10,2) DEFAULT NULL,
  `precio` decimal(10,2) DEFAULT NULL,
  `descuento` decimal(10,2) DEFAULT '0.00',
  `iva` decimal(5,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `albaran_id` (`albaran_id`),
  KEY `articulo_id` (`articulo_id`),
  CONSTRAINT `albaran_venta_lineas_ibfk_1` FOREIGN KEY (`albaran_id`) REFERENCES `albaranes_venta` (`id`) ON DELETE CASCADE,
  CONSTRAINT `albaran_venta_lineas_ibfk_2` FOREIGN KEY (`articulo_id`) REFERENCES `articulos` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `albaran_venta_lineas`
--

LOCK TABLES `albaran_venta_lineas` WRITE;
/*!40000 ALTER TABLE `albaran_venta_lineas` DISABLE KEYS */;
/*!40000 ALTER TABLE `albaran_venta_lineas` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `albaranes_venta`
--

DROP TABLE IF EXISTS `albaranes_venta`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `albaranes_venta` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `numero` varchar(50) NOT NULL,
  `fecha` date DEFAULT NULL,
  `cliente_id` bigint DEFAULT NULL,
  `almacen_id` bigint DEFAULT NULL,
  `observaciones` text,
  `total` decimal(10,2) DEFAULT '0.00',
  PRIMARY KEY (`id`),
  UNIQUE KEY `numero` (`numero`),
  KEY `cliente_id` (`cliente_id`),
  KEY `almacen_id` (`almacen_id`),
  CONSTRAINT `albaranes_venta_ibfk_1` FOREIGN KEY (`cliente_id`) REFERENCES `clientes` (`id`),
  CONSTRAINT `albaranes_venta_ibfk_2` FOREIGN KEY (`almacen_id`) REFERENCES `almacenes` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `albaranes_venta`
--

LOCK TABLES `albaranes_venta` WRITE;
/*!40000 ALTER TABLE `albaranes_venta` DISABLE KEYS */;
/*!40000 ALTER TABLE `albaranes_venta` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `almacenes`
--

DROP TABLE IF EXISTS `almacenes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `almacenes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `codigo` varchar(50) NOT NULL,
  `nombre` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `codigo` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `almacenes`
--

LOCK TABLES `almacenes` WRITE;
/*!40000 ALTER TABLE `almacenes` DISABLE KEYS */;
INSERT INTO `almacenes` VALUES (1,'1','ARMADA ESPAÑOLA');
/*!40000 ALTER TABLE `almacenes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `articulos`
--

DROP TABLE IF EXISTS `articulos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `articulos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `codigo` varchar(50) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `familia` varchar(100) DEFAULT NULL,
  `unidad` varchar(20) DEFAULT NULL,
  `iva` decimal(5,2) DEFAULT NULL,
  `pvp` decimal(10,2) DEFAULT '0.00',
  `coste` decimal(10,2) DEFAULT '0.00',
  PRIMARY KEY (`id`),
  UNIQUE KEY `codigo` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=128 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `articulos`
--

LOCK TABLES `articulos` WRITE;
/*!40000 ALTER TABLE `articulos` DISABLE KEYS */;
INSERT INTO `articulos` VALUES (1,'0100','PAN COMUN','01',NULL,1.00,0.00,0.00),(2,'0101','BARRA NORMAL 1/4','01',NULL,1.00,0.00,0.00),(3,'0102','BARRA CASERA LARGA 1/4','02',NULL,1.00,0.00,0.00),(4,'0103','BARRA CASERA 1/4','02',NULL,1.00,0.00,0.00),(5,'0104','BARRA CASERA 1/2 KG.','02',NULL,1.00,0.00,0.00),(6,'0105','PAN REDONDO CASERO 1/2','02',NULL,1.00,0.00,0.00),(7,'0106','PAN CASERO KILO.','02',NULL,1.00,0.00,0.00),(8,'0107','BARRA CASERA KILO.','02',NULL,1.00,0.00,0.00),(9,'0108','BARRA INTEGRAL','03',NULL,2.00,0.00,0.00),(10,'0109','BOCADILLO INTEGRAL','03',NULL,2.00,0.00,0.00),(11,'0110','BOCADILLO NORMAL','01',NULL,1.00,0.00,0.00),(12,'0111','BOCADILLO CASERO','02',NULL,1.00,0.00,0.00),(13,'0112','MEDIA DE BOMBON','03',NULL,2.00,0.00,0.00),(14,'0113','MONTADITO DE BOMBON','03',NULL,2.00,0.00,0.00),(15,'0114','PULGUITA DE BOMBON','03',NULL,2.00,0.00,0.00),(16,'0115','BOMBON BOC.','03',NULL,2.00,0.00,0.00),(17,'0116','PAN DE HAMBURGUESA','03',NULL,2.00,0.00,0.00),(18,'0117','CHURRO PAN','01',NULL,1.00,0.00,0.00),(19,'0118','CHURRO CASERO','02',NULL,1.00,0.00,0.00),(20,'0120','BARRA SOBADA 1/4','01',NULL,1.00,0.00,0.00),(21,'0121','PANECITO SOBADO 1/4','01',NULL,1.00,0.00,0.00),(22,'0122','ROLLO SOBADO 1/4','01',NULL,1.00,0.00,0.00),(23,'0123','BOCADILLO SOBADO','01',NULL,1.00,0.00,0.00),(24,'0124','PAN REDONDO SOBADO MEDIO','01',NULL,1.00,0.00,0.00),(25,'0125','BARRA SOBADO MEDIO','01',NULL,1.00,0.00,0.00),(26,'0126','PIEZA SOBADA CON FORMA €/Kg','01',NULL,1.00,0.00,0.00),(27,'0128','MEDIA DE PAN','01',NULL,1.00,0.00,0.00),(28,'0129','PULGUITA DE PAN','01',NULL,1.00,0.00,0.00),(29,'0130','BAGUETTINA','01',NULL,1.00,0.00,0.00),(30,'0131','MOLDE INTEGRAL','03',NULL,2.00,0.00,0.00),(31,'0132','MOLDE BOMBON','03',NULL,2.00,0.00,0.00),(32,'0135','BARRA ACEITE 1/4','03',NULL,2.00,0.00,0.00),(33,'0136','PAN ACEITE 1/2','03',NULL,2.00,0.00,0.00),(34,'0138','PANECITO CASERO COMEDOR','02',NULL,1.00,0.00,0.00),(35,'0139','PANECITO CASERO COMED.PEQUEÑO','02',NULL,1.00,0.00,0.00),(36,'0140','BARRA GALLEGA','02',NULL,1.00,0.00,0.00),(37,'0145','BARRA PAN CRISTAL','03',NULL,2.00,0.00,0.00),(38,'0146','CHAPATA CRISTAL PESO','03',NULL,2.00,0.00,0.00),(39,'0147','PAN REDONDO CRISTAL MEDIO','03',NULL,2.00,0.00,0.00),(40,'0148','PAN REDONDO CRISTAL KILO','03',NULL,2.00,0.00,0.00),(41,'0150','PAN DE MAIZ','03',NULL,2.00,0.00,0.00),(42,'0160','BARRA CENTENO','03',NULL,2.00,0.00,0.00),(43,'0161','BARRA CEREALES','03',NULL,2.00,0.00,0.00),(44,'0165','PAN ESPELTA','03',NULL,2.00,0.00,0.00),(45,'0166','PAN TRIGO SARRACENO','03',NULL,2.00,0.00,0.00),(46,'0170','BARRA INTEGRAL 1 KILO.','03',NULL,2.00,0.00,0.00),(47,'0200','BOLLERIA DULCE','04',NULL,2.00,0.00,0.00),(48,'0201','ENSEIMADA GRANDE','04',NULL,2.00,0.00,0.00),(49,'0202','CROISANT CURVO GRANDE','04',NULL,2.00,0.00,0.00),(50,'0203','CROISSANT RECTO MARGARINA 90','04',NULL,2.00,0.00,0.00),(51,'0204','CROISSANT RECTO CHOCO INYE. 90','04',NULL,2.00,0.00,0.00),(52,'0205','TORTELL','04',NULL,2.00,0.00,0.00),(53,'0206','FARTONS CASEROS','04',NULL,2.00,0.00,0.00),(54,'0207','MINI CROISANT MANTEQUILLA','04',NULL,2.00,0.00,0.00),(55,'0208','MINI ENSEIMADA','04',NULL,2.00,0.00,0.00),(56,'0209','NAPOLITANA CHOCO','04',NULL,2.00,0.00,0.00),(57,'0210','MINI CROISANT CHOCO','04',NULL,2.00,0.00,0.00),(58,'0211','KILO MADALENAS NORMALES','04',NULL,2.00,0.00,0.00),(59,'0212','KILO MADALENAS ALMENDRA','04',NULL,2.00,0.00,0.00),(60,'0213','KILO MADALENAS CHOCOLATE','04',NULL,2.00,0.00,0.00),(61,'0214','KILO MADALENAS MANZANA','04',NULL,2.00,0.00,0.00),(62,'0217','DONUT BLANCO','04',NULL,2.00,0.00,0.00),(63,'0218','DONUT CHOCO','04',NULL,2.00,0.00,0.00),(64,'0219','BOLLO SUIZO','04',NULL,2.00,0.00,0.00),(65,'0220','TOÑA GRANDE','04',NULL,2.00,0.00,0.00),(66,'0221','MONA GR. SIN/HUE.','04',NULL,2.00,0.00,0.00),(67,'0222','MONA GR. CON/HUE.','04',NULL,2.00,0.00,0.00),(68,'0223','MONA PEQ. SIN/HUE.','04',NULL,2.00,0.00,0.00),(69,'0224','MONA PEQ. CON/HUE.','04',NULL,2.00,0.00,0.00),(70,'0225','MONA CODORNIZ CON/HUE.','04',NULL,2.00,0.00,0.00),(71,'0230','PORCION PLANCHA CABELLO','04',NULL,2.00,0.00,0.00),(72,'0231','ROSCON PEQUEÑO','04',NULL,2.00,0.00,0.00),(73,'0232','ROSCON MEDIANO','04',NULL,2.00,0.00,0.00),(74,'0233','ROSCON GRANDE','04',NULL,2.00,0.00,0.00),(75,'0234','ROSCON PEQUEÑO RELLENO','04',NULL,2.00,0.00,0.00),(76,'0235','ROSCON MEDIANO RELLENO','04',NULL,2.00,0.00,0.00),(77,'0236','ROSCON GRANDE RELLENO','04',NULL,2.00,0.00,0.00),(78,'0300','BOLLERIA SALADA','05',NULL,2.00,0.00,0.00),(79,'0301','COCA MOLLITAS ENTERA','05',NULL,2.00,0.00,0.00),(80,'0302','PORCION COCA MOLLITAS','05',NULL,2.00,0.00,0.00),(81,'0303','COCA PISTO EMPANADA ENTERA','05',NULL,2.00,0.00,0.00),(82,'0304','PORCION COCA PISTO EMPANADA','05',NULL,2.00,0.00,0.00),(83,'0305','COCA GUISANTES ENTERA','05',NULL,2.00,0.00,0.00),(84,'0306','PORCION COCA GUISANTES','05',NULL,2.00,0.00,0.00),(85,'0307','COCA TOÑINA ENTERA','05',NULL,2.00,0.00,0.00),(86,'0308','PORCION COCA TOÑINA','05',NULL,2.00,0.00,0.00),(87,'0309','COCA VERDURA SARDINA ENTERA','05',NULL,2.00,0.00,0.00),(88,'0310','PORCION COCA VERDURA SARDINA','05',NULL,2.00,0.00,0.00),(89,'0311','COCA TOMATE ANCHOAS ENTERA','05',NULL,2.00,0.00,0.00),(90,'0312','PORCION COCA TOMATE ANCHOAS','05',NULL,2.00,0.00,0.00),(91,'0313','PIZZA JAMON QUESO ENTERA','05',NULL,2.00,0.00,0.00),(92,'0314','PORCION PIZZA JAMON QUESO','05',NULL,2.00,0.00,0.00),(93,'0315','PIZZA BERENJENA BACON ENTERA','05',NULL,2.00,0.00,0.00),(94,'0316','PORCION PIZZA BERENJENA BACON','05',NULL,2.00,0.00,0.00),(95,'0317','COQUITAS VERDURA','05',NULL,2.00,0.00,0.00),(96,'0320','EMPANADILLA GRANDE PISTO','05',NULL,2.00,0.00,0.00),(97,'0321','MINI EMPANADILLA ATUN/CEBOLLA','05',NULL,2.00,0.00,0.00),(98,'0322','MINI EMPANADILLA QUESO/BACON','05',NULL,2.00,0.00,0.00),(99,'0323','MINI EMPANADILLA Q.CABRA/CEB.CARAM.','05',NULL,2.00,0.00,0.00),(100,'0324','MINI EMPANADILLA ESPINACAS','05',NULL,2.00,0.00,0.00),(101,'0325','MINI EMPANADILLA QUESO','05',NULL,2.00,0.00,0.00),(102,'0326','MINI EMPANADILLA SOBRASADA','05',NULL,2.00,0.00,0.00),(103,'0327','MINI EMPANADILLA MORCILLA','05',NULL,2.00,0.00,0.00),(104,'0330','KILO SALADITOS VARIADOS','05',NULL,2.00,0.00,0.00),(105,'0331','NAPOLITANA YORK/QUESO','05',NULL,2.00,0.00,0.00),(106,'0332','REJILLAS POLLO','05',NULL,2.00,0.00,0.00),(107,'0333','REJILLAS ESPINACAS','05',NULL,2.00,0.00,0.00),(108,'0334','REJILLAS JAMON Y QUESO','05',NULL,2.00,0.00,0.00),(109,'0335','AGUJAS DE ATUN','05',NULL,2.00,0.00,0.00),(110,'0701','ROLLOS DE HUEVO KILO','07',NULL,2.00,0.00,0.00),(111,'0702','ROLLOS DE NARANJA KILO','07',NULL,2.00,0.00,0.00),(112,'0703','ROLLOS INTEGRALES KILO','07',NULL,2.00,0.00,0.00),(113,'0704','ROLLOS VINO-OLIVA KILO','07',NULL,2.00,0.00,0.00),(114,'0705','ROLLOS MORENOS 6 uds.','07',NULL,2.00,0.00,0.00),(115,'0706','ROLLOS MORENOS 9 uds.','07',NULL,2.00,0.00,0.00),(116,'0710','COOKIES CHOCOLATE KILO','07',NULL,2.00,0.00,0.00),(117,'0800','VARIOS PASTELERIA','06',NULL,2.00,0.00,0.00),(118,'0801','TARTA DE ELCHE KG.','06',NULL,2.00,0.00,0.00),(119,'0900','SERVICIO ESPECIAL','09',NULL,3.00,0.00,0.00),(120,'0901','ALQUILER DE UTILLAJE PROPIO','09',NULL,3.00,0.00,0.00),(121,'1001','HARINA SACO `BUFORT`','10',NULL,1.00,0.00,0.00),(122,'1002','LEVADURA PASTILLA 500G','10',NULL,2.00,0.00,0.00),(123,'1010','SACO PAN DURO/AYER','10',NULL,1.00,0.00,0.00);
/*!40000 ALTER TABLE `articulos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `clientes`
--

DROP TABLE IF EXISTS `clientes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `clientes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `codigo` varchar(50) NOT NULL,
  `nombre` varchar(255) DEFAULT NULL,
  `cif` varchar(50) DEFAULT NULL,
  `direccion` varchar(255) DEFAULT NULL,
  `poblacion` varchar(100) DEFAULT NULL,
  `codigo_postal` varchar(20) DEFAULT NULL,
  `provincia` varchar(50) DEFAULT NULL,
  `notas` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `codigo` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=128 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `clientes`
--

LOCK TABLES `clientes` WRITE;
/*!40000 ALTER TABLE `clientes` DISABLE KEYS */;
INSERT INTO `clientes` VALUES (1,'0001','**** FACTURA ANULADA ****',NULL,NULL,NULL,NULL,NULL,NULL),(2,'0002','CLIENTES CONTADO',NULL,NULL,NULL,NULL,NULL,NULL),(3,'0101','**baja**OCIO HOTELERO SANTA POLA S.L.','B-54747951','C/ HOLANDA 15','SANTA POLA','03130','03',NULL),(4,'0102','**baja**ALFONSA TROYA FERNANDEZ','21448520P','Ubanova - Local 5','Alicante',NULL,'03',NULL),(5,'0103','**baja**José Luis Herrera Payán','21999139F','Pza. San Crispín 6','TORRELLANO','03320','03',NULL),(6,'0104','RODRIGUEZ CHARCUTEROS C.B.','E-53472304','C/ LA IGLESIA 8','EL ALTET','03195','03',NULL),(7,'0105','**baja**ROSA Mª NAVARO RIERA','21433729-Y','C/ AN JUAN BOSCO 3, BAJO','ALICANTE','03005','03',NULL),(8,'0106','**baja**DAVID CERDA MARTINEZ','74239960-P','C/ LA TORRE 95','ELCHE','03204','03',NULL),(9,'0107','**baja**MARGARITA MOLLA AGULLO','34485334-W','C/ LIBERTAD 57','TORRELLANO',NULL,'03',NULL),(10,'0108','CARMELO ESCOLANO MENDEZ','21413415-R','ALICANTE 2','EL ALTET',NULL,'03',NULL),(11,'0109','**baja**MARIA DEL CARMEN TORRES','221151796D','C/ SAN BARTOLOME DE TIRAJANA 20','AENALES DEL SOL','03195','03',NULL),(12,'0110','**baja**VENTA VISTALEGRE S.C.','J-54746581','Ctra. Alicante-Cartagena N-332 Km. 88','Santa Pola',NULL,'03',NULL),(13,'0111','**baja**CANTINA AEROPUERTO ALICANTE','B53415162','C/ Jaime I s/n','Monforte','03670','03',NULL),(14,'0112','MECHEDA S.L.','B-54854914','Av Dama de Elche 17','El Altt',NULL,'03',NULL),(15,'0113','**baja**Juan Fanisco Snchez Campos','50528635G','Paseo Tomas Dura 17','Alicante',NULL,'03',NULL),(16,'0114','**BAJA**LIVIA DANUTA MECHITA','X3274500J','AV. CARTAGENA','EL ALTET',NULL,'03',NULL),(17,'0115','**baja**NOEMI FERNANDEZ TORRES','44766621-L','AV. SAN BARTOLOM DE TIRAJANA 20','ARENALES DEL SOL','03195','03',NULL),(18,'0116','Mª Del Mar Bautista Pelaez - R. de Pili','52523521P','Av. Elche 144','Alicante',NULL,'03',NULL),(19,'0117','**baja**BEATRIZ BERNÁRDEZ IZQUIERDO','47046881J','Av. Dama de Elche 19','El Altet','03195','03',NULL),(20,'0118','**baja**ANTONIA GARCIA MARTINEZ','22450750J','Rincon de Montemar','Gran Alacant - Santa Pola','03130','03',NULL),(21,'0119','**baja**ELOINA SELVAS FRIAS','21414585','Av. Ciudad Deportiva 2','El Altet',NULL,'03',NULL),(22,'0120','MARIA TERESA RODRIGUEZ BLASCO','22004031T','AV. CIUDAD DEPORTIVA S/N','EL ALTET','03195','03',NULL),(23,'0121','**baja**ELISABET MARTINEZ GARCIA','74006630J','Rte. Vista Alegre','Santa Pola','03130','03',NULL),(24,'0122','SANSEL C.V.','F54984141','Av. Cartagena 48','El Altet','03195','03',NULL),(25,'0123','RESTAURANTE VISTA ALEGRE SLU','B-42560375','Ptda. Valverde Bajo, 13','SANTA POLA','03130','03',NULL),(26,'0124','**baja**PEDRO ASENCIO AZNAR','21985860-E','C/ ALICANTE s/n','EL ALTET','03195','03',NULL),(27,'0126','**baja**KALIFRO, CB','E-42552000','CTRA. ELCHE-EL ALTET Km. 10','El Altet','03195','03',NULL),(28,'0127','**baja**BASILIO FERNANDEZ LATORRE','02078739-E','SAN BARTOLOME DE TIRAJANA 20, LOCAL 14','ARENALES DEL SOL',NULL,'03',NULL),(29,'0128','**baja**ANTONIA GARCIA','22450750-J','HOLANDA 5','GRAN ALACANT','03130','03',NULL),(30,'0129','VERONICA KUBICEK SILVEIRA `Nou Yoyos`','X-4584270-W','C/ Mar 25','El Altet','03195','03',NULL),(31,'0130','**BAJA**BRABEL RESTAURACION SL','B-54776844','REST. NOU ARCOS','ARENALES DEL SOL',NULL,'03',NULL),(32,'0131','CAPS  CUIDADORES S.L.','B-97320378','CEIP RODOLFO TOMAS i SAMPER - L´ALTET','PICANYA','46210','46',NULL),(33,'0132','RESTAURANTE LA BODEGA S.L.','B54989827','LIBERTAD 55','TORRELLANO',NULL,'03',NULL),(34,'0133','**baja**MONICA QUILACHAMIN -','51299831-X','Urbabanova Local 5','Alicante',NULL,'03',NULL),(35,'0134','CARDASI S,A,','A-53183901','C/ Galileo Galilei, 2','03203 ELCHE - Parque Industrial',NULL,'03',NULL),(36,'0135','PIKOLINO´S INTERCONTINENTAL, S.A.','A-53238713','C/ Galileo Galilei, 2','ELCHE','03203','03',NULL),(37,'0136','PIKOSTORE, S.L.','B-53906590','C/ GALILEO GALILEI, 2','TORRELLANO','03203','03',NULL),(38,'0137','MOLICOPI, S.L.','B-03875952','Galileo Galilei, 2','ELCHE','03320','03',NULL),(39,'0138','**baja*RESTAURANTE PRESTIGE SPORT S.L.U.','B-42606889','AV. CARTAGENA','EL ALTET',NULL,'03',NULL),(40,'0139','FUNDACION JUAN PERAN - PIKOLINOS','G-54265301','GALILEO GALILEI, 2','ELCHE','03203','03',NULL),(41,'0140','**baja**ROSA MARIA MONTEVERDE FERNANDEZ','21451754-E','AV. DAMA DE ELCHE','EL ALTET','03195','03',NULL),(42,'0141','SANTIAGO GUIJARRO JORNET, S.L.','B-42555953','`RINCON DE SANTI`','ARENALES DEL SOL','03195','03',NULL),(43,'0142','ASOCIACION DE VECINOS AGUA AMARGA','G-53299319','C/. TORMOS, 6 1A','ALICANTE','03008','03',NULL),(44,'0143','PASCUAL RUSO GIMENEZ `La Bodeguita`','74386240-P','Av. Mediterraneo 68, Local 1','Santa Pola','03133','03',NULL),(45,'0144','**baja**PETRONILA FELIZ RUIZ','74.386.240-P','Partida El Altet, Pol. 1º, nº 45','El Altet','03195','03',NULL),(46,'0145','**baja**JUANI HITA MESEGUER','21500386-D','Servido en Rte. `Maestral`','ALICANTE',NULL,'03','cliente particular - encargo -'),(47,'0146','**BAJA**PERLA ARENALES, S.L.','B-01628957','RESTAURANTE LA PERLA','ARENALES DEL SOL','03195','03','BAJA EN 2022  **DEJA A DEBER**'),(48,'0147','ALBA VIDAL GUILL - ICE & VICE','2168970-H','Paseo Tomás Durá, 5-6 Local 1','URBANOVA - ALICANTE','03008','03',NULL),(49,'0148','SERVICIOS INTEGRALES MRH, S.L.','B-04973079','Fragata Almansa, 6','El Altet','03195','03',NULL),(50,'0149','PIZZERIA URBANOVA, S.L.','B-72675549','Paseo Tomás Durá, 15 Urbanova','ALICANTE','03008','03','CLIENE NUEVO DESDE ENERO 2023'),(51,'0150','NICOLAS Y MORAGUES, ESPJ','E-42733030','LA TENDETA DE JUANI','TORRELLANO BAJO','03320','03',NULL),(52,'0151','ADA CILA RUSO ESPINOSA','20519123-H','GABRIEL MIRO, 67-1-4','SANTA POLA','03130','03',NULL),(53,'0152','JOSE M. MANCHON LLEDO','74230036-C','SAN BARTOLOME DE TIRAJANA, 67 BAJO','LOS ARENALES DEL SOL','03195','03',NULL),(54,'0153','BERNARDO PEREZ VALERA','48374021-F','`BURGUER BERNA`','ARENALES DEL SOL','03195','03',NULL),(55,'0154','HECTOR FABIAN GONZALEZ','X-3579041-B','`CAMINITO TAPAS BAR`','EL ALTET','03195','03','ANTES `SANSEL`, ROGELIO Y ELO'),(56,'0200','BEGOÑA PILAR BAEZA BONMATI','48350693-R','AV. SAN FRANCISCO DE ASIS 64','EL ALTET','03195','03',NULL),(57,'0201','ARENALES PLAYA - DESPACHO PROPIO','F-54059985','SAN BARTOLOME DE TIRAJANA','LOS ARENALES DEL SOL','03195','03',NULL),(58,'0202','TERRAMAR, S.A.','A-03284296','C/ ZARANDIETA 7, BAJO','ALICANTE','03010','03',NULL),(59,'0203','LIMENCOP, S.L.','B-53212619','Avda. Salamanca, 27','Alicante','03005','03',NULL),(60,'0207','**baja**HIJOS DE FABRIZZIO C.B.','53152138-E','ETELLA 6','ALICANTE',NULL,'03','****BAJA**** PASA A SER -----PIZZERIA PROVENZAL, S.L.'),(61,'0211','**baja**MARCO ESPIN S.L.','B-53540043','CONCEJAL LORENZO LLANERAS 9','ALICANTE',NULL,'03',NULL),(62,'0217','RAQUEL ORTOLA BONMATI','52765150-E','PASEO TOMAS DURA','URBANOVA - ALICANTE',NULL,'03',NULL),(63,'0218','** BAJA**PIZZERIA PROVENZAL, S.L.','B-02776797','PASEO TOMAS DURÁ','ALICANTE',NULL,'03','ALTA DESDE 2021 ------ANTES HIJOS DE FABRIZZIO'),(64,'0222','GRAN BAR POMARES S.L.','B-54547492','AV. CARTAGENA 17','EL ALTET','03195','03',NULL),(65,'0293','IES GRAN ALACANT','50300141-I','MUNTANYA DE SANTA POLA, S/N','SANTA POLA','03130','03','676243976-606515128'),(66,'0294','AMPA CEIP RODOLFO TOMAS SAMPER','G-03244464','AVDA. CIUDAD DEPORTIVA','EL ALTET','03195','03',NULL),(67,'0295','LOBO AGENCIA DIGITAL, S.L.','B-97845929','C/Travessia 15E La Marina Ed. BioHub',NULL,'46024','46','CLIENTE TRABAJO BEGOÑA BAEZA'),(68,'0296','**BAJA**TALLER DE EDITORES, S.A.','A-78509130','REVISTA MUJER HOY','MADRID','28027','28','CLIENTE DE BEGOÑA BAEZA    DIRECCION DE ARTE'),(69,'0297','**baja**JAVIER JOSÉ SEMPERE GOMIS','48365627-P','Ptda. L´Altet Pol.1 Nº48','EL ALTET','03195','03',NULL),(70,'0299','**baja**ANTONIO JESUS MARTINEZ','74229758-H','PLAZA MAYOR 12-13','GRN ALACANT','03130','03',NULL),(71,'0300','FUNDACION NORAY `Proyecto Hombre`','G-53363131','PTDA. AGUA AMARGA S/N','ALICANTE','03008','03',NULL);
/*!40000 ALTER TABLE `clientes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `direccionesenvio_new`
--

DROP TABLE IF EXISTS `direccionesenvio_new`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `direccionesenvio_new` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cliente_id` bigint DEFAULT NULL,
  `codigo_direccion` int DEFAULT NULL,
  `nombre` text,
  `direccion` text,
  `direccion2` text,
  `poblacion` text,
  `provincia` text,
  `cp` text,
  `telefono` text,
  `notas` text,
  PRIMARY KEY (`id`),
  KEY `cliente_id` (`cliente_id`),
  CONSTRAINT `direccionesenvio_new_ibfk_1` FOREIGN KEY (`cliente_id`) REFERENCES `clientes` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `direccionesenvio_new`
--

LOCK TABLES `direccionesenvio_new` WRITE;
/*!40000 ALTER TABLE `direccionesenvio_new` DISABLE KEYS */;
/*!40000 ALTER TABLE `direccionesenvio_new` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `factura_albaran`
--

DROP TABLE IF EXISTS `factura_albaran`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `factura_albaran` (
  `factura_id` bigint NOT NULL,
  `albaran_id` bigint NOT NULL,
  PRIMARY KEY (`factura_id`,`albaran_id`),
  KEY `albaran_id` (`albaran_id`),
  CONSTRAINT `factura_albaran_ibfk_1` FOREIGN KEY (`factura_id`) REFERENCES `facturas` (`id`) ON DELETE CASCADE,
  CONSTRAINT `factura_albaran_ibfk_2` FOREIGN KEY (`albaran_id`) REFERENCES `albaranes_venta` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `factura_albaran`
--

LOCK TABLES `factura_albaran` WRITE;
/*!40000 ALTER TABLE `factura_albaran` DISABLE KEYS */;
/*!40000 ALTER TABLE `factura_albaran` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `factura_lineas`
--

DROP TABLE IF EXISTS `factura_lineas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `factura_lineas` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `factura_id` bigint DEFAULT NULL,
  `articulo_id` bigint DEFAULT NULL,
  `cantidad` decimal(10,2) DEFAULT NULL,
  `precio` decimal(10,2) DEFAULT NULL,
  `iva` decimal(5,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `factura_id` (`factura_id`),
  KEY `articulo_id` (`articulo_id`),
  CONSTRAINT `factura_lineas_ibfk_1` FOREIGN KEY (`factura_id`) REFERENCES `facturas` (`id`) ON DELETE CASCADE,
  CONSTRAINT `factura_lineas_ibfk_2` FOREIGN KEY (`articulo_id`) REFERENCES `articulos` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `factura_lineas`
--

LOCK TABLES `factura_lineas` WRITE;
/*!40000 ALTER TABLE `factura_lineas` DISABLE KEYS */;
/*!40000 ALTER TABLE `factura_lineas` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `facturas`
--

DROP TABLE IF EXISTS `facturas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `facturas` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `numero` varchar(50) NOT NULL,
  `fecha` date DEFAULT NULL,
  `cliente_id` bigint DEFAULT NULL,
  `total` decimal(10,2) DEFAULT '0.00',
  `pagado` decimal(10,2) DEFAULT '0.00',
  PRIMARY KEY (`id`),
  UNIQUE KEY `numero` (`numero`),
  KEY `cliente_id` (`cliente_id`),
  CONSTRAINT `facturas_ibfk_1` FOREIGN KEY (`cliente_id`) REFERENCES `clientes` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `facturas`
--

LOCK TABLES `facturas` WRITE;
/*!40000 ALTER TABLE `facturas` DISABLE KEYS */;
/*!40000 ALTER TABLE `facturas` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pedido_lineas`
--

DROP TABLE IF EXISTS `pedido_lineas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pedido_lineas` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `pedido_id` bigint DEFAULT NULL,
  `articulo_id` bigint DEFAULT NULL,
  `cantidad` decimal(10,2) DEFAULT NULL,
  `precio` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `pedido_id` (`pedido_id`),
  KEY `articulo_id` (`articulo_id`),
  CONSTRAINT `pedido_lineas_ibfk_1` FOREIGN KEY (`pedido_id`) REFERENCES `pedidos` (`id`) ON DELETE CASCADE,
  CONSTRAINT `pedido_lineas_ibfk_2` FOREIGN KEY (`articulo_id`) REFERENCES `articulos` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pedido_lineas`
--

LOCK TABLES `pedido_lineas` WRITE;
/*!40000 ALTER TABLE `pedido_lineas` DISABLE KEYS */;
/*!40000 ALTER TABLE `pedido_lineas` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pedidos`
--

DROP TABLE IF EXISTS `pedidos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pedidos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `numero` varchar(50) NOT NULL,
  `fecha` date DEFAULT NULL,
  `cliente_id` bigint DEFAULT NULL,
  `estado` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `numero` (`numero`),
  KEY `cliente_id` (`cliente_id`),
  CONSTRAINT `pedidos_ibfk_1` FOREIGN KEY (`cliente_id`) REFERENCES `clientes` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pedidos`
--

LOCK TABLES `pedidos` WRITE;
/*!40000 ALTER TABLE `pedidos` DISABLE KEYS */;
/*!40000 ALTER TABLE `pedidos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `proveedores`
--

DROP TABLE IF EXISTS `proveedores`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `proveedores` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `codigo` varchar(50) NOT NULL,
  `nombre` varchar(255) DEFAULT NULL,
  `cif` varchar(50) DEFAULT NULL,
  `direccion` varchar(255) DEFAULT NULL,
  `poblacion` varchar(100) DEFAULT NULL,
  `codigo_postal` varchar(20) DEFAULT NULL,
  `provincia` varchar(50) DEFAULT NULL,
  `notas` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `codigo` (`codigo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `proveedores`
--

LOCK TABLES `proveedores` WRITE;
/*!40000 ALTER TABLE `proveedores` DISABLE KEYS */;
/*!40000 ALTER TABLE `proveedores` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-20 16:45:53
