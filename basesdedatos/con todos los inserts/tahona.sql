-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: localhost    Database: tahona
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
-- Table structure for table `agentes`
--

DROP TABLE IF EXISTS `agentes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `agentes` (
  `CodigoAgente` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `NombreAgente` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Notas` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `_Comision` decimal(15,2) DEFAULT NULL,
  `Domicilio` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Poblacion` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `CodigoPostal` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Provincia` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `CIF` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`CodigoAgente`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `agentes`
--

LOCK TABLES `agentes` WRITE;
/*!40000 ALTER TABLE `agentes` DISABLE KEYS */;
/*!40000 ALTER TABLE `agentes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `albaranescompra`
--

DROP TABLE IF EXISTS `albaranescompra`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `albaranescompra` (
  `NumAlbaran` int DEFAULT NULL,
  `NumFactura` int DEFAULT NULL,
  `NumPedido` int DEFAULT NULL,
  `Fecha` text COLLATE utf8mb4_unicode_ci,
  `IVASN` text COLLATE utf8mb4_unicode_ci,
  `REQSN` text COLLATE utf8mb4_unicode_ci,
  `CodigoProveedor` text COLLATE utf8mb4_unicode_ci,
  `CodigoAlmacen` text COLLATE utf8mb4_unicode_ci,
  `Observaciones` text COLLATE utf8mb4_unicode_ci,
  `Sumadesglose` double DEFAULT NULL,
  `_DescuentoPP` double DEFAULT NULL,
  `Descuentopp` double DEFAULT NULL,
  `SumaIVA` double DEFAULT NULL,
  `SumaREQ` double DEFAULT NULL,
  `TotalFactura` double DEFAULT NULL,
  `BaseIVA1` double DEFAULT NULL,
  `IVA1` double DEFAULT NULL,
  `REQ1` double DEFAULT NULL,
  `ImporteIVA1` double DEFAULT NULL,
  `BaseIVA2` double DEFAULT NULL,
  `IVA2` double DEFAULT NULL,
  `REQ2` double DEFAULT NULL,
  `ImporteIVA2` double DEFAULT NULL,
  `BaseIVA3` double DEFAULT NULL,
  `IVA3` double DEFAULT NULL,
  `REQ3` double DEFAULT NULL,
  `ImporteIVA3` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `albaranescompra`
--

LOCK TABLES `albaranescompra` WRITE;
/*!40000 ALTER TABLE `albaranescompra` DISABLE KEYS */;
/*!40000 ALTER TABLE `albaranescompra` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `albaranescompradesglose`
--

DROP TABLE IF EXISTS `albaranescompradesglose`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `albaranescompradesglose` (
  `NumAlbaran` int DEFAULT NULL,
  `NumLinea` int DEFAULT NULL,
  `CodigoArticulo` text COLLATE utf8mb4_unicode_ci,
  `DescripcionArticulo` text COLLATE utf8mb4_unicode_ci,
  `Cantidad` double DEFAULT NULL,
  `PVP` double DEFAULT NULL,
  `Descuento` double DEFAULT NULL,
  `PrecioUnitario` double DEFAULT NULL,
  `Subtotal` double DEFAULT NULL,
  `TIVA` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `albaranescompradesglose`
--

LOCK TABLES `albaranescompradesglose` WRITE;
/*!40000 ALTER TABLE `albaranescompradesglose` DISABLE KEYS */;
/*!40000 ALTER TABLE `albaranescompradesglose` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `albaranesventa`
--

DROP TABLE IF EXISTS `albaranesventa`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `albaranesventa` (
  `NumAlbaran` int NOT NULL,
  `NumFactura` int DEFAULT NULL,
  `NumPedido` int DEFAULT NULL,
  `Fecha` text COLLATE utf8mb4_unicode_ci,
  `IVASN` text COLLATE utf8mb4_unicode_ci,
  `REQSN` text COLLATE utf8mb4_unicode_ci,
  `CodigoCliente` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `CodigoAlmacen` text COLLATE utf8mb4_unicode_ci,
  `Observaciones` text COLLATE utf8mb4_unicode_ci,
  `Sumadesglose` double DEFAULT NULL,
  `_DescuentoPP` double DEFAULT NULL,
  `Descuentopp` double DEFAULT NULL,
  `SumaIVA` double DEFAULT NULL,
  `SumaREQ` double DEFAULT NULL,
  `TotalFactura` double DEFAULT NULL,
  `BaseIVA1` double DEFAULT NULL,
  `IVA1` double DEFAULT NULL,
  `REQ1` double DEFAULT NULL,
  `ImporteIVA1` double DEFAULT NULL,
  `BaseIVA2` double DEFAULT NULL,
  `IVA2` double DEFAULT NULL,
  `REQ2` double DEFAULT NULL,
  `ImporteIVA2` double DEFAULT NULL,
  `BaseIVA3` double DEFAULT NULL,
  `IVA3` double DEFAULT NULL,
  `REQ3` double DEFAULT NULL,
  `ImporteIVA3` double DEFAULT NULL,
  `DireccionEnvio` int DEFAULT NULL,
  PRIMARY KEY (`NumAlbaran`),
  KEY `idx_albaranesventa_codigo_cliente` (`CodigoCliente`),
  CONSTRAINT `fk_albaranesventa_cliente` FOREIGN KEY (`CodigoCliente`) REFERENCES `clientes` (`Codigo`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `albaranesventa`
--

LOCK TABLES `albaranesventa` WRITE;
/*!40000 ALTER TABLE `albaranesventa` DISABLE KEYS */;
/*!40000 ALTER TABLE `albaranesventa` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `albaranesventadesglose`
--

DROP TABLE IF EXISTS `albaranesventadesglose`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `albaranesventadesglose` (
  `NumAlbaran` int NOT NULL,
  `NumLinea` int NOT NULL,
  `CodigoArticulo` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `DescripcionArticulo` text COLLATE utf8mb4_unicode_ci,
  `Cantidad` double DEFAULT NULL,
  `PVP` double DEFAULT NULL,
  `Descuento` double DEFAULT NULL,
  `PrecioUnitario` double DEFAULT NULL,
  `Subtotal` double DEFAULT NULL,
  `TIVA` int DEFAULT NULL,
  PRIMARY KEY (`NumAlbaran`,`NumLinea`),
  KEY `idx_avdesg_codigo_art` (`CodigoArticulo`),
  CONSTRAINT `fk_avdesg_articulo` FOREIGN KEY (`CodigoArticulo`) REFERENCES `articulos` (`CodigoArticulo`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `albaranesventadesglose`
--

LOCK TABLES `albaranesventadesglose` WRITE;
/*!40000 ALTER TABLE `albaranesventadesglose` DISABLE KEYS */;
/*!40000 ALTER TABLE `albaranesventadesglose` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `almacenes`
--

DROP TABLE IF EXISTS `almacenes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `almacenes` (
  `CodigoAlmacen` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `NombreAlmacen` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`CodigoAlmacen`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `almacenes`
--

LOCK TABLES `almacenes` WRITE;
/*!40000 ALTER TABLE `almacenes` DISABLE KEYS */;
INSERT INTO `almacenes` VALUES ('1','ARMADA ESPAÑOLA');
/*!40000 ALTER TABLE `almacenes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `apuntes`
--

DROP TABLE IF EXISTS `apuntes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `apuntes` (
  `Asiento` int DEFAULT NULL,
  `Fila` int DEFAULT NULL,
  `Fecha` text COLLATE utf8mb4_unicode_ci,
  `Subcuenta` int DEFAULT NULL,
  `Concepto` text COLLATE utf8mb4_unicode_ci,
  `DebePesetas` double DEFAULT NULL,
  `HaberPesetas` double DEFAULT NULL,
  `Documento` text COLLATE utf8mb4_unicode_ci,
  `Punteo` text COLLATE utf8mb4_unicode_ci,
  `Renumerado` int DEFAULT NULL,
  `Saldo` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `apuntes`
--

LOCK TABLES `apuntes` WRITE;
/*!40000 ALTER TABLE `apuntes` DISABLE KEYS */;
/*!40000 ALTER TABLE `apuntes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `articulos`
--

DROP TABLE IF EXISTS `articulos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `articulos` (
  `CodigoArticulo` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `DescripcionCorta` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `DescripcionArticulo` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Unidad` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `FamiliaArticulo` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Tipo_de_IVA` int DEFAULT NULL,
  `SubCuentaVentas` int DEFAULT NULL,
  `SubCuentaCompras` int DEFAULT NULL,
  `UsoInterno` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Compuesto` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Servicio` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `CosteArticulo` decimal(15,2) DEFAULT NULL,
  `CosteMedio` decimal(15,2) DEFAULT NULL,
  `UltimoCoste` decimal(15,2) DEFAULT NULL,
  `PVP1` decimal(15,2) DEFAULT NULL,
  `PVP2` decimal(15,2) DEFAULT NULL,
  `PVP3` decimal(15,2) DEFAULT NULL,
  `PVP4` decimal(15,2) DEFAULT NULL,
  `PVP5` decimal(15,2) DEFAULT NULL,
  `MinimoStock` int DEFAULT NULL,
  `MaximoStock` int DEFAULT NULL,
  `MinStockPorAlmacen` int DEFAULT NULL,
  `MaxStockPorAlmacen` int DEFAULT NULL,
  `Descuento1` decimal(15,2) DEFAULT NULL,
  `Descuento2` decimal(15,2) DEFAULT NULL,
  `Descuento3` decimal(15,2) DEFAULT NULL,
  `Descuento4` decimal(15,2) DEFAULT NULL,
  `Descuento5` decimal(15,2) DEFAULT NULL,
  `ProveedorDefecto` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `DiasFabricacion` decimal(15,2) DEFAULT NULL,
  `CodigoTablaArticulo` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ValorFila` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ValorColumna` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`CodigoArticulo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `articulos`
--

LOCK TABLES `articulos` WRITE;
/*!40000 ALTER TABLE `articulos` DISABLE KEYS */;
INSERT INTO `articulos` VALUES ('0100','PAN COMUN','PAN COMUN','','01',1,0,0,'0','0','0',0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0101','BARRA NORMAL 1/4','BARRA NORMAL 1/4','','01',1,0,0,'0','0','0',0.00,0.00,0.00,0.96,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0102','BARRA CASERA LARGA 1/4','BARRA CASERA LARGA 1/4','','02',1,0,0,'0','0','0',0.00,0.00,0.00,1.01,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0103','BARRA CASERA 1/4','BARRA CASERA 1/4','','02',1,0,0,'0','0','0',0.00,0.00,0.00,1.06,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0104','BARRA CASERA 1/2 KG.','BARRA CASERA 1/2 KG.','','02',1,0,0,'0','0','0',0.00,0.00,0.00,2.07,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0105','PAN REDONDO CASERO 1/2','PAN REDONDO CASERO 1/2','','02',1,0,0,'0','0','0',0.00,0.00,0.00,2.07,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0106','PAN CASERO KILO.','PAN CASERO KILO.','','02',1,0,0,'0','0','0',0.00,0.00,0.00,4.04,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0107','BARRA CASERA KILO.','BARRA CASERA KILO.','','02',1,0,0,'0','0','0',0.00,0.00,0.00,4.04,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0108','BARRA INTEGRAL','BARRA INTEGRAL','','03',2,0,0,'0','0','0',0.00,0.00,0.00,1.06,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0109','BOCADILLO INTEGRAL','BOCADILLO INTEGRAL','','03',2,0,0,'0','0','0',0.00,0.00,0.00,0.58,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0110','BOCADILLO NORMAL','BOCADILLO NORMAL','','01',1,0,0,'0','0','0',0.00,0.00,0.00,0.48,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0111','BOCADILLO CASERO','BOCADILLO CASERO','','02',1,0,0,'0','0','0',0.00,0.00,0.00,0.53,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0112','MEDIA DE BOMBON','MEDIA DE BOMBON','','03',2,0,0,'0','0','0',0.00,0.00,0.00,0.34,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0113','MONTADITO DE BOMBON','MONTADITO DE BOMBON','','03',2,0,0,'0','0','0',0.00,0.00,0.00,0.25,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0114','PULGUITA DE BOMBON','PULGUITA DE BOMBON','','03',2,0,0,'0','0','0',0.00,0.00,0.00,0.18,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0115','BOMBON BOC.','BOMBON BOC.','','03',2,0,0,'0','0','0',0.00,0.00,0.00,0.48,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0116','PAN DE HAMBURGUESA','PAN DE HAMBURGUESA','','03',2,0,0,'0','0','0',0.00,0.00,0.00,0.45,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0117','CHURRO PAN','CHURRO PAN','','01',1,0,0,'0','0','0',0.00,0.00,0.00,0.63,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0118','CHURRO CASERO','CHURRO CASERO','','02',1,0,0,'0','0','0',0.00,0.00,0.00,0.67,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0120','BARRA SOBADA 1/4','BARRA SOBADA 1/4','','01',1,0,0,'0','0','0',0.00,0.00,0.00,1.25,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0121','PANECITO SOBADO 1/4','PANECITO SOBADO 1/4','','01',1,0,0,'0','0','0',0.00,0.00,0.00,1.25,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0122','ROLLO SOBADO 1/4','ROLLO SOBADO 1/4','','01',1,0,0,'0','0','0',0.00,0.00,0.00,1.25,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0123','BOCADILLO SOBADO','BOCADILLO SOBADO','','01',1,0,0,'0','0','0',0.00,0.00,0.00,0.63,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0124','PAN REDONDO SOBADO MEDIO','PAN REDONDO SOBADO MEDIO','','01',1,0,0,'0','0','0',0.00,0.00,0.00,2.50,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0125','BARRA SOBADO MEDIO','BARRA SOBADO MEDIO','','01',1,0,0,'0','0','0',0.00,0.00,0.00,2.50,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0126','PIEZA SOBADA CON FORMA €/Kg','PIEZA SOBADA CON FORMA €/Kg','','01',1,0,0,'0','0','0',0.00,0.00,0.00,6.00,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0128','MEDIA DE PAN ','MEDIA DE PAN ','','01',1,0,0,'0','0','0',0.00,0.00,0.00,0.34,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0129','PULGUITA DE PAN ','PULGUITA DE PAN ','','01',1,0,0,'0','0','0',0.00,0.00,0.00,0.24,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0130','BAGUETTINA','BAGUETTINA','','01',1,0,0,'0','0','0',0.00,0.00,0.00,0.48,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0131','MOLDE INTEGRAL','MOLDE INTEGRAL','','03',2,0,0,'0','0','0',0.00,0.00,0.00,2.40,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0132','MOLDE BOMBON','MOLDE BOMBON','','03',2,0,0,'0','0','0',0.00,0.00,0.00,2.40,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0135','BARRA ACEITE 1/4','BARRA ACEITE 1/4','','03',2,0,0,'0','0','0',0.00,0.00,0.00,1.30,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0136','PAN ACEITE 1/2','PAN ACEITE 1/2','','03',2,0,0,'0','0','0',0.00,0.00,0.00,2.60,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0138','PANECITO CASERO COMEDOR','PANECITO CASERO COMEDOR','','02',1,0,0,'0','0','0',0.00,0.00,0.00,0.72,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0139','PANECITO CASERO COMED.PEQUEÑO','PANECITO CASERO COMED.PEQUEÑO','','02',1,0,0,'0','0','0',0.00,0.00,0.00,0.48,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0140','BARRA GALLEGA ','BARRA GALLEGA ','','02',1,0,0,'0','0','0',0.00,0.00,0.00,1.06,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0145','BARRA PAN CRISTAL','BARRA PAN CRISTAL','','03',2,0,0,'0','0','0',0.00,0.00,0.00,1.40,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0146','CHAPATA CRISTAL PESO','CHAPATA CRISTAL PESO','','03',2,0,0,'0','0','0',0.00,0.00,0.00,7.00,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0147','PAN REDONDO CRISTAL MEDIO','PAN REDONDO CRISTAL MEDIO','','03',2,0,0,'0','0','0',0.00,0.00,0.00,2.60,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0148','PAN REDONDO CRISTAL KILO','PAN REDONDO CRISTAL KILO','','03',2,0,0,'0','0','0',0.00,0.00,0.00,6.00,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0150','PAN DE MAIZ ','PAN DE MAIZ ','','03',2,0,0,'0','0','0',0.00,0.00,0.00,4.50,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0160','BARRA CENTENO','BARRA CENTENO','','03',2,0,0,'0','0','0',0.00,0.00,0.00,1.20,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0161','BARRA CEREALES','BARRA CEREALES','','03',2,0,0,'0','0','0',0.00,0.00,0.00,1.40,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0165','PAN ESPELTA','PAN ESPELTA','','03',2,0,0,'0','0','0',0.00,0.00,0.00,4.20,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0166','PAN TRIGO SARRACENO','PAN TRIGO SARRACENO','','03',2,0,0,'0','0','0',0.00,0.00,0.00,4.20,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0170','BARRA INTEGRAL 1 KILO.','BARRA INTEGRAL 1 KILO.','','03',2,0,0,'0','0','0',0.00,0.00,0.00,4.33,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0200','BOLLERIA DULCE','BOLLERIA DULCE','','04',2,0,0,'0','0','0',0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0201','ENSEIMADA GRANDE','ENSEIMADA GRANDE','','04',2,0,0,'0','0','0',0.00,0.00,0.00,0.91,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0202','CROISANT CURVO GRANDE','CROISANT CURVO GRANDE','','04',2,0,0,'0','0','0',0.00,0.00,0.00,1.09,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0203','CROISSANT RECTO MARGARINA 90','CROISSANT RECTO MARGARINA 90','','04',2,0,0,'0','0','0',0.00,0.00,0.00,0.91,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0204','CROISSANT RECTO CHOCO INYE. 90','CROISSANT RECTO CHOCO INYE. 90','','04',2,0,0,'0','0','0',0.00,0.00,0.00,1.09,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0205','TORTELL','TORTELL','','04',2,0,0,'0','0','0',0.00,0.00,0.00,1.36,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0206','FARTONS CASEROS','FARTONS CASEROS','','04',2,0,0,'0','0','0',0.00,0.00,0.00,0.45,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0207','MINI CROISANT MANTEQUILLA','MINI CROISANT MANTEQUILLA','','04',2,0,0,'0','0','0',0.00,0.00,0.00,0.54,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0208','MINI ENSEIMADA','MINI ENSEIMADA','','04',2,0,0,'0','0','0',0.00,0.00,0.00,0.45,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0209','NAPOLITANA CHOCO','NAPOLITANA CHOCO','','04',2,0,0,'0','0','0',0.00,0.00,0.00,1.09,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0210','MINI CROISANT CHOCO','MINI CROISANT CHOCO','','04',2,0,0,'0','0','0',0.00,0.00,0.00,0.54,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0211','KILO MADALENAS NORMALES','KILO MADALENAS NORMALES','','04',2,0,0,'0','0','0',0.00,0.00,0.00,7.00,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0212','KILO MADALENAS ALMENDRA','KILO MADALENAS ALMENDRA','','04',2,0,0,'0','0','0',0.00,0.00,0.00,8.00,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0213','KILO MADALENAS CHOCOLATE','KILO MADALENAS CHOCOLATE','','04',2,0,0,'0','0','0',0.00,0.00,0.00,7.50,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0214','KILO MADALENAS MANZANA','KILO MADALENAS MANZANA','','04',2,0,0,'0','0','0',0.00,0.00,0.00,7.50,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0217','DONUT BLANCO ','DONUT BLANCO ','','04',2,0,0,'0','0','0',0.00,0.00,0.00,0.80,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0218','DONUT CHOCO ','DONUT CHOCO ','','04',2,0,0,'0','0','0',0.00,0.00,0.00,1.10,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0219','BOLLO SUIZO','BOLLO SUIZO','','04',2,0,0,'0','0','0',0.00,0.00,0.00,0.75,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0220','TOÑA GRANDE','TOÑA GRANDE','','04',2,0,0,'0','0','0',0.00,0.00,0.00,3.32,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0221','MONA GR. SIN/HUE.','MONA GR. SIN/HUE.','','04',2,0,0,'0','0','0',0.00,0.00,0.00,2.55,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0222','MONA GR. CON/HUE.','MONA GR. CON/HUE.','','04',2,0,0,'0','0','0',0.00,0.00,0.00,2.77,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0223','MONA PEQ. SIN/HUE.','MONA PEQ. SIN/HUE.','','04',2,0,0,'0','0','0',0.00,0.00,0.00,1.82,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0224','MONA PEQ. CON/HUE.','MONA PEQ. CON/HUE.','','04',2,0,0,'0','0','0',0.00,0.00,0.00,2.14,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0225','MONA CODORNIZ CON/HUE.','MONA CODORNIZ CON/HUE.','','04',2,0,0,'0','0','0',0.00,0.00,0.00,1.45,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0230','PORCION PLANCHA CABELLO','PORCION PLANCHA CABELLO','','04',2,0,0,'0','0','0',0.00,0.00,0.00,1.20,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0231','ROSCON PEQUEÑO','ROSCON PEQUEÑO','','04',2,0,0,'0','0','0',0.00,0.00,0.00,11.82,13.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0232','ROSCON MEDIANO','ROSCON MEDIANO','','04',2,0,0,'0','0','0',0.00,0.00,0.00,15.45,17.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0233','ROSCON GRANDE','ROSCON GRANDE','','04',2,0,0,'0','0','0',0.00,0.00,0.00,20.45,22.50,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0234','ROSCON PEQUEÑO RELLENO','ROSCON PEQUEÑO RELLENO','','04',2,0,0,'0','0','0',0.00,0.00,0.00,13.64,15.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0235','ROSCON MEDIANO RELLENO','ROSCON MEDIANO RELLENO','','04',2,0,0,'0','0','0',0.00,0.00,0.00,20.00,22.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0236','ROSCON GRANDE RELLENO','ROSCON GRANDE RELLENO','','04',2,0,0,'0','0','0',0.00,0.00,0.00,24.55,27.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0300','BOLLERIA SALADA','BOLLERIA SALADA','','05',2,0,0,'0','0','0',0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0301','COCA MOLLITAS ENTERA','COCA MOLLITAS ENTERA','','05',2,0,0,'0','0','0',0.00,0.00,0.00,11.00,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0302','PORCION COCA MOLLITAS','PORCION COCA MOLLITAS','','05',2,0,0,'0','0','0',0.00,0.00,0.00,0.91,1.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0303','COCA PISTO EMPANADA ENTERA','COCA PISTO EMPANADA ENTERA','','05',2,0,0,'0','0','0',0.00,0.00,0.00,13.63,15.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0304','PORCION COCA PISTO EMPANADA','PORCION COCA PISTO EMPANADA','','05',2,0,0,'0','0','0',0.00,0.00,0.00,1.18,1.30,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0305','COCA GUISANTES ENTERA','COCA GUISANTES ENTERA','','05',2,0,0,'0','0','0',0.00,0.00,0.00,18.18,20.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0306','PORCION COCA GUISANTES','PORCION COCA GUISANTES','','05',2,0,0,'0','0','0',0.00,0.00,0.00,1.64,1.80,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0307','COCA TOÑINA ENTERA','COCA TOÑINA ENTERA','','05',2,0,0,'0','0','0',0.00,0.00,0.00,18.18,20.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0308','PORCION COCA TOÑINA','PORCION COCA TOÑINA','','05',2,0,0,'0','0','0',0.00,0.00,0.00,1.64,1.80,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0309','COCA VERDURA SARDINA ENTERA','COCA VERDURA SARDINA ENTERA','','05',2,0,0,'0','0','0',0.00,0.00,0.00,12.00,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0310','PORCION COCA VERDURA SARDINA','PORCION COCA VERDURA SARDINA','','05',2,0,0,'0','0','0',0.00,0.00,0.00,1.09,1.20,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0311','COCA TOMATE ANCHOAS ENTERA','COCA TOMATE ANCHOAS ENTERA','','05',2,0,0,'0','0','0',0.00,0.00,0.00,12.00,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0312','PORCION COCA TOMATE ANCHOAS','PORCION COCA TOMATE ANCHOAS','','05',2,0,0,'0','0','0',0.00,0.00,0.00,1.20,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0313','PIZZA JAMON QUESO ENTERA','PIZZA JAMON QUESO ENTERA','','05',2,0,0,'0','0','0',0.00,0.00,0.00,15.00,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0314','PORCION PIZZA JAMON QUESO','PORCION PIZZA JAMON QUESO','','05',2,0,0,'0','0','0',0.00,0.00,0.00,1.90,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0315','PIZZA BERENJENA BACON ENTERA','PIZZA BERENJENA BACON ENTERA','','05',2,0,0,'0','0','0',0.00,0.00,0.00,16.00,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0316','PORCION PIZZA BERENJENA BACON','PORCION PIZZA BERENJENA BACON','','05',2,0,0,'0','0','0',0.00,0.00,0.00,2.10,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0317','COQUITAS VERDURA','COQUITAS VERDURA','','05',2,0,0,'0','0','0',0.00,0.00,0.00,1.70,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0320','EMPANADILLA GRANDE PISTO','EMPANADILLA GRANDE PISTO','','05',2,0,0,'0','0','0',0.00,0.00,0.00,1.00,1.10,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0321','MINI EMPANADILLA ATUN/CEBOLLA','MINI EMPANADILLA ATUN/CEBOLLA','','05',2,0,0,'0','0','0',0.00,0.00,0.00,0.55,0.60,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0322','MINI EMPANADILLA QUESO/BACON','MINI EMPANADILLA QUESO/BACON','','05',2,0,0,'0','0','0',0.00,0.00,0.00,0.55,0.60,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0323','MINI EMPANADILLA Q.CABRA/CEB.CARAM.','MINI EMPANADILLA Q.CABRA/CEB.CARAM.','','05',2,0,0,'0','0','0',0.00,0.00,0.00,0.55,0.60,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0324','MINI EMPANADILLA ESPINACAS','MINI EMPANADILLA ESPINACAS','','05',2,0,0,'0','0','0',0.00,0.00,0.00,0.55,0.60,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0325','MINI EMPANADILLA QUESO','MINI EMPANADILLA QUESO','','05',2,0,0,'0','0','0',0.00,0.00,0.00,0.55,0.60,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0326','MINI EMPANADILLA SOBRASADA','MINI EMPANADILLA SOBRASADA','','05',2,0,0,'0','0','0',0.00,0.00,0.00,0.55,0.60,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0327','MINI EMPANADILLA MORCILLA','MINI EMPANADILLA MORCILLA','','05',2,0,0,'0','0','0',0.00,0.00,0.00,0.55,0.60,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0330','KILO SALADITOS VARIADOS','KILO SALADITOS VARIADOS','','05',2,0,0,'0','0','0',0.00,0.00,0.00,14.00,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0331','NAPOLITANA YORK/QUESO','NAPOLITANA YORK/QUESO','','05',2,0,0,'0','0','0',0.00,0.00,0.00,1.50,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0332','REJILLAS POLLO','REJILLAS POLLO','','05',2,0,0,'0','0','0',0.00,0.00,0.00,1.50,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0333','REJILLAS ESPINACAS','REJILLAS ESPINACAS','','05',2,0,0,'0','0','0',0.00,0.00,0.00,1.50,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0334','REJILLAS JAMON Y QUESO','REJILLAS JAMON Y QUESO','','05',2,0,0,'0','0','0',0.00,0.00,0.00,1.50,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0335','AGUJAS DE ATUN','AGUJAS DE ATUN','','05',2,0,0,'0','0','0',0.00,0.00,0.00,1.50,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0701','ROLLOS DE HUEVO KILO','ROLLOS DE HUEVO KILO','','07',2,0,0,'0','0','0',0.00,0.00,0.00,10.00,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0702','ROLLOS DE NARANJA KILO','ROLLOS DE NARANJA KILO','','07',2,0,0,'0','0','0',0.00,0.00,0.00,10.00,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0703','ROLLOS INTEGRALES KILO','ROLLOS INTEGRALES KILO','','07',2,0,0,'0','0','0',0.00,0.00,0.00,10.00,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0704','ROLLOS VINO-OLIVA KILO','ROLLOS VINO-OLIVA KILO','','07',2,0,0,'0','0','0',0.00,0.00,0.00,12.00,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0705','ROLLOS MORENOS 6 uds.','ROLLOS MORENOS 6 uds.','','07',2,0,0,'0','0','0',0.00,0.00,0.00,1.50,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0706','ROLLOS MORENOS 9 uds.','ROLLOS MORENOS 9 uds.','','07',2,0,0,'0','0','0',0.00,0.00,0.00,2.20,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0710','COOKIES CHOCOLATE KILO','COOKIES CHOCOLATE KILO','','07',2,0,0,'0','0','0',0.00,0.00,0.00,12.00,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0800','VARIOS PASTELERIA ','VARIOS PASTELERIA ','','06',2,0,0,'0','0','0',0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0801','TARTA DE ELCHE KG. ','TARTA DE ELCHE KG. ','','06',2,0,0,'0','0','0',0.00,0.00,0.00,18.20,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0900','SERVICIO ESPECIAL','SERVICIO ESPECIAL','','09',3,0,0,'0','0','1',0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('0901','ALQUILER DE UTILLAJE PROPIO','ALQUILER DE UTILLAJE PROPIO','','09',3,0,0,'0','0','1',0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('1001','HARINA SACO `BUFORT`','HARINA SACO `BUFORT`','','10',1,0,0,'0','0','0',0.00,0.00,0.00,24.16,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('1002','LEVADURA PASTILLA 500G','LEVADURA PASTILLA 500G','','10',2,0,0,'0','0','0',0.00,0.00,0.00,2.97,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','',''),('1010','SACO PAN DURO/AYER','SACO PAN DURO/AYER','','10',1,0,0,'0','0','0',0.00,0.00,0.00,2.10,0.00,0.00,0.00,0.00,0,0,0,0,0.00,0.00,0.00,0.00,0.00,'',0.00,'','','');
/*!40000 ALTER TABLE `articulos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `articulosprecio`
--

DROP TABLE IF EXISTS `articulosprecio`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `articulosprecio` (
  `CodigoArticulo` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `CodigoMoneda` int DEFAULT NULL,
  `PVP1` double DEFAULT NULL,
  `PVP2` double DEFAULT NULL,
  `PVP3` double DEFAULT NULL,
  `PVP4` double DEFAULT NULL,
  `PVP5` double DEFAULT NULL,
  `Coste` double DEFAULT NULL,
  KEY `idx_articulosprecio_codarticulo` (`CodigoArticulo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `articulosprecio`
--

LOCK TABLES `articulosprecio` WRITE;
/*!40000 ALTER TABLE `articulosprecio` DISABLE KEYS */;
/*!40000 ALTER TABLE `articulosprecio` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `asientos`
--

DROP TABLE IF EXISTS `asientos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `asientos` (
  `Asiento` int DEFAULT NULL,
  `Fila` int DEFAULT NULL,
  `Fecha` text COLLATE utf8mb4_unicode_ci,
  `Subcuenta` int DEFAULT NULL,
  `Concepto` text COLLATE utf8mb4_unicode_ci,
  `DebePesetas` double DEFAULT NULL,
  `HaberPesetas` double DEFAULT NULL,
  `Documento` text COLLATE utf8mb4_unicode_ci,
  `Punteo` text COLLATE utf8mb4_unicode_ci,
  `Renumerado` int DEFAULT NULL,
  `Saldo` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `asientos`
--

LOCK TABLES `asientos` WRITE;
/*!40000 ALTER TABLE `asientos` DISABLE KEYS */;
/*!40000 ALTER TABLE `asientos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `asientospatron`
--

DROP TABLE IF EXISTS `asientospatron`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `asientospatron` (
  `Codigo` int DEFAULT NULL,
  `Descripcion` text COLLATE utf8mb4_unicode_ci,
  `ProveedorOCliente` text COLLATE utf8mb4_unicode_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `asientospatron`
--

LOCK TABLES `asientospatron` WRITE;
/*!40000 ALTER TABLE `asientospatron` DISABLE KEYS */;
/*!40000 ALTER TABLE `asientospatron` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `balances`
--

DROP TABLE IF EXISTS `balances`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `balances` (
  `Agrupacion` int DEFAULT NULL,
  `Titulo` text COLLATE utf8mb4_unicode_ci,
  `SaldoN` double DEFAULT NULL,
  `SaldoN_1` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `balances`
--

LOCK TABLES `balances` WRITE;
/*!40000 ALTER TABLE `balances` DISABLE KEYS */;
INSERT INTO `balances` VALUES (100,'A) Accionistas por desembolsos no exigidos',0,0),(200,'B) Inmovilizado',0,0),(210,'    I. Gastos de establecimiento',0,0),(220,'    II. Inmovilizaciones inmateriales',0,0),(221,'        1. Gastos de investigación y desarrollo',0,0),(222,'        2. Concesiones, patentes, licencias, marcas y similares',0,0),(223,'        3. Fondo de comercio',0,0),(224,'        4. Derechos de traspaso',0,0),(225,'        5. Aplicaciones informáticas',0,0),(226,'        6. Anticipos',0,0),(227,'        7. Provisiones',0,0),(228,'        8. Amortizaciones',0,0),(230,'    III. Inmovilizaciones materiales',0,0),(231,'        1. Terrenos y construcciones',0,0),(232,'        2. Instalaciones técnicas y maquinaria',0,0),(233,'        3. Otras instalaciones, utillaje y mobiliario',0,0),(234,'        4. Anticipos e inmovilizaciones materiales en curso',0,0),(235,'        5. Otro inmovilizado',0,0),(236,'        6. Provisiones',0,0),(237,'        7. Amortizaciones',0,0),(240,'    IV. Inmovilizaciones financieras',0,0),(241,'        1. Participaciones en empresas del grupo',0,0),(242,'        2. Créditos a empresas del grupo',0,0),(243,'        3. Participaciones en empresas asociadas',0,0),(244,'        4. Créditos a empresas asociadas',0,0),(245,'        5. Valores que tengan carácter de inmovilizaciones',0,0),(246,'        6. Otros créditos',0,0),(247,'        7. Depósitos y fianzas entregados a largo plazo',0,0),(248,'        8. Provisiones',0,0),(250,'    V. Acciones propias',0,0),(300,'C) Gastos a distribuir en varios ejercicios',0,0),(400,'D) Activo circulante',0,0),(410,'    I. Accionistas por desembolsos exigidos',0,0),(420,'    II. Existencias',0,0),(421,'        1. Comerciales',0,0),(422,'        2. Materias primas y otros aprovisionamientos',0,0),(423,'        3. Productos en curso y semiterminados',0,0),(424,'        4. Productos terminados',0,0),(425,'        5. Subproductos, residuos y materiales recuperados',0,0),(426,'        6. Anticipos',0,0),(427,'        7. Provisiones',0,0),(430,'    III. Deudores',0,0),(431,'        1. Clientes por ventas y prestaciones de servicios',0,0),(432,'        2. Sociedades del grupo, deudores',0,0),(433,'        3. Sociedades asociadas, deudores',0,0),(434,'        4. Deudores varios',0,0),(435,'        5. Personal',0,0),(436,'        6. Administraciones Públicas',0,0),(437,'        7. Provisiones',0,0),(440,'    IV. Inversiones financieras temporales',0,0),(441,'        1. Participaciones en empresas del grupo',0,0),(442,'        2. Créditos a empresas del grupo',0,0),(443,'        3. Participaciones en empresas asociadas',0,0),(444,'        4. Créditos a empresas asociadas',0,0),(445,'        5. Cartera de valores a corto plazo',0,0),(446,'        6. Créditos',0,0),(447,'        7. Depósitos y fianzas entregados a corto plazo',0,0),(448,'        8. Provisiones',0,0),(450,'    V. Acciones propias a corto plazo',0,0),(460,'    VI. Tesorería',0,0),(470,'    VII. Ajustes por periodificación',0,0),(1100,'A) Fondos propios',0,0),(1110,'    I. Capital suscrito',0,0),(1120,'    II. Prima de emisión',0,0),(1130,'    III. Reserva de revalorización',0,0),(1140,'    IV. Reservas',0,0),(1141,'        1. Reserva legal',0,0),(1142,'        2. Reservas para acciones propias',0,0),(1143,'        3. Reservas para acciones de la sociedad dominante',0,0),(1144,'        4. Reservas estatutarias',0,0),(1145,'        5. Otras reservas',0,0),(1150,'    V. Resultados de ejercicios anteriores',0,0),(1151,'        1. Remanente',0,0),(1152,'        2. Resultados negatigos de ejercicios anteriores',0,0),(1153,'        3. Aportaciones socios para compensación pérdidas',0,0),(1160,'    VI.Pérdidas y ganancias (beneficio o pérdida)',0,0),(1170,'    VII. Dividendo a cuenta entregado en el ejercicio',0,0),(1200,'B) Ingresos a distribuir en varios ejercicios',0,0),(1201,'        1. Subvenciones de capital',0,0),(1202,'        2. Diferencias positivas de cambio',0,0),(1203,'        3.Otros ingresos a distribuir en varios ejercicios',0,0),(1300,'C) Provisiones para riesgos y gastos',0,0),(1301,'        1. Provisiones pensiones y obligaciones similares',0,0),(1302,'        2. Provisiones para impuestos',0,0),(1303,'        3. Otras provisiones',0,0),(1304,'        4. Fondos de reversión',0,0),(1400,'D) Acreedores a largo plazo',0,0),(1410,'    I. Emisiones de obligaciones',0,0),(1411,'        1. Obligaciones no convertibles',0,0),(1412,'        2. Obligaciones convertibles',0,0),(1413,'        3. Otras deudas representadas en valores negociab.',0,0),(1420,'    II. Deudas con entidades de crédito',0,0),(1430,'    III. Deudas con empresas del grupo y asociadas',0,0),(1431,'        1. Deudas con empresas del grupo',0,0),(1432,'        2. Deudas con empresas asociadas',0,0),(1440,'    IV.Otros acreedores',0,0),(1441,'        1. Deudas representadas por efectos a pagar',0,0),(1442,'        2. Otras deudas',0,0),(1443,'        3. Fianzas y depósitos recibidos a largo plazo',0,0),(1450,'    V. Desembolsos pendientes sobre acciones no exigidos',0,0),(1451,'        1. De empresas del grupo',0,0),(1452,'        2. De empresas asociadas',0,0),(1453,'        3. De otras empresas',0,0),(1500,'E) Acreedores a corto plazo',0,0),(1510,'    I. Emisiones de obligaciones',0,0),(1511,'        1. Obligaciones no convertibles',0,0),(1512,'        2. Obligaciones convertibles',0,0),(1513,'        3. Otras deudas representadas valores negociables',0,0),(1514,'        4. Intereses de obligaciones y otros valores',0,0),(1520,'    II. Deudas con entidades decrédito',0,0),(1521,'        1. Préstamos y otras deudas',0,0),(1522,'        2. Deudas por intereses',0,0),(1530,'    III. Deudas con empresas del grupo y asociadas a corto plazo',0,0),(1531,'        1. Deudas con empresas del grupo',0,0),(1532,'        2. Deudas con empresas asociadas',0,0),(1540,'    IV. Acreedores comerciales',0,0),(1541,'        1. Anticipos recibidos por pedidos',0,0),(1542,'        2. Deudas por compras o prestaciones de servicios',0,0),(1543,'        3. Deudas representadas por efectos a pagar',0,0),(1550,'    V. Otras deudas no comerciales',0,0),(1551,'        1. Administraciones Públicas',0,0),(1552,'        2. Deudas representadas por efectos a pagar',0,0),(1553,'        3. Otras deudas',0,0),(1554,'        4. Remuneraciones pendientes de pago',0,0),(1555,'        5. Fianzas y depósitos recibidos a corto plazo',0,0),(1560,'    VI. Provisiones para operaciones de tráfico',0,0),(1570,'    VII. Ajustes por periodificación',0,0),(2000,'A) GASTOS',0,0),(2010,'      1. Reducción de existencias productos terminados y en curso de fabricación',0,0),(2020,'      2. Aprovisionamientos',0,0),(2021,'          a) Consumo de mercaderías',0,0),(2022,'          b) Consumo de materias primas y otras materias consumibles',0,0),(2023,'          c) Otros gastos externos',0,0),(2030,'      3. Gastos de personal',0,0),(2031,'          a) Sueldos, salarios y asimilados',0,0),(2032,'          b) Cargas sociales',0,0),(2040,'      4. Dotaciones para amortizaciones de inmovilizado',0,0),(2050,'      5. Variación de las provisiones de tráfico',0,0),(2051,'          a) Variación de provisiones de existencias',0,0),(2052,'          b) Variación de provisiones y pérdidas de créditos incobrables',0,0),(2053,'          c) Variación de otras provisiones de tráfico',0,0),(2060,'      6. Otros gastos de explotación',0,0),(2061,'          a) Servicios exteriores',0,0),(2062,'          b) Tributos',0,0),(2063,'          c) Otros gastos de gestión corriente',0,0),(2064,'          d) Dotación al fondo de reversión',0,0),(2065,'    I. Beneficios de Explotación',0,0),(2070,'      7. Gastos financieros y gastos asimilados',0,0),(2071,'          a) Por deudas con empresas del grupo',0,0),(2072,'          b) Por deudas con empresas asociadas',0,0),(2073,'          c) Por deudas con terceros y gastos asimilados',0,0),(2074,'          d) Pérdidas de inversiones financieras',0,0),(2080,'      8. Variación de las provisiones de inversiones financieras',0,0),(2090,'      9. Diferencias negativas de cambio',0,0),(2091,'    II. Resultados financieros positivos',0,0),(2092,'    III. Beneficios actividades ordinarias',0,0),(2100,'      10. Var. previsiones inmovilizado inmaterial, material y cartera de control',0,0),(2110,'      11. Pérds.procedentes inmovilizado inmaterial,material y cartera de control',0,0),(2120,'      12. Pérdidas por operaciones con acciones y obligaciones propias',0,0),(2130,'      13. Gastos extraordinarios',0,0),(2140,'      14. Gastos y pérdidas de otros ejercicios',0,0),(2141,'    IV. Resultados extraordinarios positivos ',0,0),(2142,'    V. Beneficios antes de impuestos',0,0),(2150,'      15. Impuesto sobre sociedades',0,0),(2160,'      16. Otros impuestos',0,0),(2161,'    VI. Resultado del ejercicio (beneficios)',0,0),(3000,'B) INGRESOS',0,0),(3010,'      1. Importe neto de la cifra de negocio',0,0),(3011,'          a) Ventas',0,0),(3012,'          b) Prestaciones de servicios',0,0),(3013,'          c) Devoluciones y `rappels` sobre ventas',0,0),(3020,'      2.Aumento de las existencias productos terminados y en curso de fabricación',0,0),(3030,'      3. Trabajos efectuados por la empresa para el inmovilizado',0,0),(3040,'      4. Otros ingresos de explotación',0,0),(3041,'          a) Ingresos accesorios y otros de gestión corriente',0,0),(3042,'          b) Subvenciones',0,0),(3043,'          c) Exceso de provisiones de riesgos y gastos',0,0),(3044,'    I. Pérdidas de explotación',0,0),(3050,'      5. Ingresos de participaciones en capital',0,0),(3051,'          a) En empresas del grupo',0,0),(3052,'          b) En empresas asociadas',0,0),(3053,'          c) En empresas fuera del grupo',0,0),(3060,'      6. Ingresos de otros valores negociables y créditos del activo inmovilizado',0,0),(3061,'          a) De empresas del grupo',0,0),(3062,'          b) De empresas asociadas',0,0),(3063,'          c) De empresas del grupo',0,0),(3070,'      7. Otros intereses e ingresos asimilados',0,0),(3071,'          a) De empresas del grupo',0,0),(3072,'          b) De empresas asociadas',0,0),(3073,'          c) Otros intereses',0,0),(3074,'          d) Beneficios en inversiones financieras',0,0),(3080,'      8. Diferencias positivas de cambio',0,0),(3081,'    II. Resultados financieros negativos',0,0),(3082,'    III. Pérdidas actividades ordinarias',0,0),(3090,'      9. Beneficios enajenación inmovilizado inmaterial,material y cartera contro',0,0),(3100,'      10. Beneficios por operaciones con acciones y obligaciones propias',0,0),(3110,'      11. Subvenciones de capital transferidas al resultado del ejercicio',0,0),(3120,'      12. Ingresos extraordinarios',0,0),(3130,'      13. Ingresos y beneficios de otros ejercicios',0,0),(3160,'    IV. Resultados extraordinarios negativos',0,0),(3170,'    V. Pérdidas antes de impuestos',0,0),(3180,'    VI. Resultado del ejercicio (pérdidas)',0,0),(4000,'A) GASTOS',0,0),(4010,'         1.  Consumos de explotación',0,0),(4020,'         2.  Gastos de personal',0,0),(4021,'              a) Sueldos, salarios y asimilados',0,0),(4022,'              b) Cargas sociales',0,0),(4030,'         3.  Dotaciones para amortizaciones de inmovilizado',0,0),(4040,'         4.  Variación de las provisiones de tráfico y pérdidas de créditos incobrables',0,0),(4050,'         5.  Otros gastos de explotación',0,0),(4051,'    I.   BENEFICIOS DE EXPLOTACIÓN',0,0),(4060,'         6.  Gastos financieros y gastos asimilados',0,0),(4061,'              a) Por deudas con empresas del grupo',0,0),(4062,'              b) Por deudas con empresas asociadas',0,0),(4063,'              c) Por otras deudas',0,0),(4064,'              d) Pérdidas de inversiones financieras',0,0),(4070,'         7.  Variación de las provisiones de inversiones financieras',0,0),(4080,'         8.  Diferencias negativas de cambio',0,0),(4081,'    II.  RESULTADOS FINANCIEROS POSITIVOS',0,0),(4082,'    III. BENEFICIOS DE LAS ACTIVIDADES ORDINARIAS',0,0),(4090,'         9.  Variación de las provisiones de inmovilizado inmaterial, material y cartera de control',0,0),(4100,'        10. Pérdidas procedentes del inmovilizado inmaterial, material y cartera de control',0,0),(4110,'        11. Pérdidas por operaciones con acciones y obligaciones propias',0,0),(4120,'        12. Gastos extraordinarios',0,0),(4130,'        13. Gastos y pérdidas de otros ejercicios',0,0),(4131,'   IV. RESULTADOS EXTRAORDINARIOS POSITIVOS',0,0),(4132,'   V.  BENEFICIOS ANTES DE IMPUESTOS',0,0),(4140,'        14. Impuesto sobre sociedades',0,0),(4150,'        15. Otros impuestos',0,0),(4151,'   VI. RESULTADO DEL EJERCICIO (BENEFICIOS)',0,0),(5000,'B) INGRESOS',0,0),(5010,'        1. Ingresos de explotación',0,0),(5011,'            a) Importe neto de la cifra de negocios',0,0),(5012,'            b) Otros ingresos de explotación',0,0),(5013,'    I.  PERDIDAS DE EXPLOTACION',0,0),(5020,'        2. Ingresos financieros',0,0),(5021,'            a) En empresas del grupo',0,0),(5022,'            b) En empresas asociadas',0,0),(5023,'            c) Otros',0,0),(5024,'            d) Beneficios en inversiones financieras',0,0),(5030,'        3. Diferencias positivas de cambio',0,0),(5031,'   II.  RESULTADOS FINANCIEROS NEGATIVOS',0,0),(5032,'   III. PERDIDAS DE LAS ACTIVIDADES ORDINARIAS',0,0),(5040,'       4. Beneficios en enajenación de inmovilizado inmaterial, material y cartera de control',0,0),(5050,'       5. Beneficios por operaciones con acciones y obligaciones propias',0,0),(5060,'       6. Subvenciones de capital transferidas al resultado del ejercicio',0,0),(5070,'       7. Ingresos extraordinarios',0,0),(5080,'       8. Ingresos y beneficios de otros ejercicios',0,0),(5081,'   IV. RESULTADOS EXTRAORDINARIOS NEGATIVOS',0,0),(5082,'   V.  PÉRDIDAS ANTES DE IMPUESTOS',0,0),(5083,'   VI. RESULTADO DEL EJERCICIO (PÉRDIDAS)',0,0),(100,'A) Accionistas por desembolsos no exigidos',0,0),(200,'B) Inmovilizado',0,0),(210,'    I. Gastos de establecimiento',0,0),(220,'    II. Inmovilizaciones inmateriales',0,0),(221,'        1. Gastos de investigación y desarrollo',0,0),(222,'        2. Concesiones, patentes, licencias, marcas y similares',0,0),(223,'        3. Fondo de comercio',0,0),(224,'        4. Derechos de traspaso',0,0),(225,'        5. Aplicaciones informáticas',0,0),(226,'        6. Anticipos',0,0),(227,'        7. Provisiones',0,0),(228,'        8. Amortizaciones',0,0),(230,'    III. Inmovilizaciones materiales',0,0),(231,'        1. Terrenos y construcciones',0,0),(232,'        2. Instalaciones técnicas y maquinaria',0,0),(233,'        3. Otras instalaciones, utillaje y mobiliario',0,0),(234,'        4. Anticipos e inmovilizaciones materiales en curso',0,0),(235,'        5. Otro inmovilizado',0,0),(236,'        6. Provisiones',0,0),(237,'        7. Amortizaciones',0,0),(240,'    IV. Inmovilizaciones financieras',0,0),(241,'        1. Participaciones en empresas del grupo',0,0),(242,'        2. Créditos a empresas del grupo',0,0),(243,'        3. Participaciones en empresas asociadas',0,0),(244,'        4. Créditos a empresas asociadas',0,0),(245,'        5. Valores que tengan carácter de inmovilizaciones',0,0),(246,'        6. Otros créditos',0,0),(247,'        7. Depósitos y fianzas entregados a largo plazo',0,0),(248,'        8. Provisiones',0,0),(250,'    V. Acciones propias',0,0),(300,'C) Gastos a distribuir en varios ejercicios',0,0),(400,'D) Activo circulante',0,0),(410,'    I. Accionistas por desembolsos exigidos',0,0),(420,'    II. Existencias',0,0),(421,'        1. Comerciales',0,0),(422,'        2. Materias primas y otros aprovisionamientos',0,0),(423,'        3. Productos en curso y semiterminados',0,0),(424,'        4. Productos terminados',0,0),(425,'        5. Subproductos, residuos y materiales recuperados',0,0),(426,'        6. Anticipos',0,0),(427,'        7. Provisiones',0,0),(430,'    III. Deudores',0,0),(431,'        1. Clientes por ventas y prestaciones de servicios',0,0),(432,'        2. Sociedades del grupo, deudores',0,0),(433,'        3. Sociedades asociadas, deudores',0,0),(434,'        4. Deudores varios',0,0),(435,'        5. Personal',0,0),(436,'        6. Administraciones Públicas',0,0),(437,'        7. Provisiones',0,0),(440,'    IV. Inversiones financieras temporales',0,0),(441,'        1. Participaciones en empresas del grupo',0,0),(442,'        2. Créditos a empresas del grupo',0,0),(443,'        3. Participaciones en empresas asociadas',0,0),(444,'        4. Créditos a empresas asociadas',0,0),(445,'        5. Cartera de valores a corto plazo',0,0),(446,'        6. Créditos',0,0),(447,'        7. Depósitos y fianzas entregados a corto plazo',0,0),(448,'        8. Provisiones',0,0),(450,'    V. Acciones propias a corto plazo',0,0),(460,'    VI. Tesorería',0,0),(470,'    VII. Ajustes por periodificación',0,0),(1100,'A) Fondos propios',0,0),(1110,'    I. Capital suscrito',0,0),(1120,'    II. Prima de emisión',0,0),(1130,'    III. Reserva de revalorización',0,0),(1140,'    IV. Reservas',0,0),(1141,'        1. Reserva legal',0,0),(1142,'        2. Reservas para acciones propias',0,0),(1143,'        3. Reservas para acciones de la sociedad dominante',0,0),(1144,'        4. Reservas estatutarias',0,0),(1145,'        5. Otras reservas',0,0),(1150,'    V. Resultados de ejercicios anteriores',0,0),(1151,'        1. Remanente',0,0),(1152,'        2. Resultados negatigos de ejercicios anteriores',0,0),(1153,'        3. Aportaciones socios para compensación pérdidas',0,0),(1160,'    VI.Pérdidas y ganancias (beneficio o pérdida)',0,0),(1170,'    VII. Dividendo a cuenta entregado en el ejercicio',0,0),(1200,'B) Ingresos a distribuir en varios ejercicios',0,0),(1201,'        1. Subvenciones de capital',0,0),(1202,'        2. Diferencias positivas de cambio',0,0),(1203,'        3.Otros ingresos a distribuir en varios ejercicios',0,0),(1300,'C) Provisiones para riesgos y gastos',0,0),(1301,'        1. Provisiones pensiones y obligaciones similares',0,0),(1302,'        2. Provisiones para impuestos',0,0),(1303,'        3. Otras provisiones',0,0),(1304,'        4. Fondos de reversión',0,0),(1400,'D) Acreedores a largo plazo',0,0),(1410,'    I. Emisiones de obligaciones',0,0),(1411,'        1. Obligaciones no convertibles',0,0),(1412,'        2. Obligaciones convertibles',0,0),(1413,'        3. Otras deudas representadas en valores negociab.',0,0),(1420,'    II. Deudas con entidades de crédito',0,0),(1430,'    III. Deudas con empresas del grupo y asociadas',0,0),(1431,'        1. Deudas con empresas del grupo',0,0),(1432,'        2. Deudas con empresas asociadas',0,0),(1440,'    IV.Otros acreedores',0,0),(1441,'        1. Deudas representadas por efectos a pagar',0,0),(1442,'        2. Otras deudas',0,0),(1443,'        3. Fianzas y depósitos recibidos a largo plazo',0,0),(1450,'    V. Desembolsos pendientes sobre acciones no exigidos',0,0),(1451,'        1. De empresas del grupo',0,0),(1452,'        2. De empresas asociadas',0,0),(1453,'        3. De otras empresas',0,0),(1500,'E) Acreedores a corto plazo',0,0),(1510,'    I. Emisiones de obligaciones',0,0),(1511,'        1. Obligaciones no convertibles',0,0),(1512,'        2. Obligaciones convertibles',0,0),(1513,'        3. Otras deudas representadas valores negociables',0,0),(1514,'        4. Intereses de obligaciones y otros valores',0,0),(1520,'    II. Deudas con entidades decrédito',0,0),(1521,'        1. Préstamos y otras deudas',0,0),(1522,'        2. Deudas por intereses',0,0),(1530,'    III. Deudas con empresas del grupo y asociadas a corto plazo',0,0),(1531,'        1. Deudas con empresas del grupo',0,0),(1532,'        2. Deudas con empresas asociadas',0,0),(1540,'    IV. Acreedores comerciales',0,0),(1541,'        1. Anticipos recibidos por pedidos',0,0),(1542,'        2. Deudas por compras o prestaciones de servicios',0,0),(1543,'        3. Deudas representadas por efectos a pagar',0,0),(1550,'    V. Otras deudas no comerciales',0,0),(1551,'        1. Administraciones Públicas',0,0),(1552,'        2. Deudas representadas por efectos a pagar',0,0),(1553,'        3. Otras deudas',0,0),(1554,'        4. Remuneraciones pendientes de pago',0,0),(1555,'        5. Fianzas y depósitos recibidos a corto plazo',0,0),(1560,'    VI. Provisiones para operaciones de tráfico',0,0),(1570,'    VII. Ajustes por periodificación',0,0),(2000,'A) GASTOS',0,0),(2010,'      1. Reducción de existencias productos terminados y en curso de fabricación',0,0),(2020,'      2. Aprovisionamientos',0,0),(2021,'          a) Consumo de mercaderías',0,0),(2022,'          b) Consumo de materias primas y otras materias consumibles',0,0),(2023,'          c) Otros gastos externos',0,0),(2030,'      3. Gastos de personal',0,0),(2031,'          a) Sueldos, salarios y asimilados',0,0),(2032,'          b) Cargas sociales',0,0),(2040,'      4. Dotaciones para amortizaciones de inmovilizado',0,0),(2050,'      5. Variación de las provisiones de tráfico',0,0),(2051,'          a) Variación de provisiones de existencias',0,0),(2052,'          b) Variación de provisiones y pérdidas de créditos incobrables',0,0),(2053,'          c) Variación de otras provisiones de tráfico',0,0),(2060,'      6. Otros gastos de explotación',0,0),(2061,'          a) Servicios exteriores',0,0),(2062,'          b) Tributos',0,0),(2063,'          c) Otros gastos de gestión corriente',0,0),(2064,'          d) Dotación al fondo de reversión',0,0),(2065,'    I. Beneficios de Explotación',0,0),(2070,'      7. Gastos financieros y gastos asimilados',0,0),(2071,'          a) Por deudas con empresas del grupo',0,0),(2072,'          b) Por deudas con empresas asociadas',0,0),(2073,'          c) Por deudas con terceros y gastos asimilados',0,0),(2074,'          d) Pérdidas de inversiones financieras',0,0),(2080,'      8. Variación de las provisiones de inversiones financieras',0,0),(2090,'      9. Diferencias negativas de cambio',0,0),(2091,'    II. Resultados financieros positivos',0,0),(2092,'    III. Beneficios actividades ordinarias',0,0),(2100,'      10. Var. previsiones inmovilizado inmaterial, material y cartera de control',0,0),(2110,'      11. Pérds.procedentes inmovilizado inmaterial,material y cartera de control',0,0),(2120,'      12. Pérdidas por operaciones con acciones y obligaciones propias',0,0),(2130,'      13. Gastos extraordinarios',0,0),(2140,'      14. Gastos y pérdidas de otros ejercicios',0,0),(2141,'    IV. Resultados extraordinarios positivos ',0,0),(2142,'    V. Beneficios antes de impuestos',0,0),(2150,'      15. Impuesto sobre sociedades',0,0),(2160,'      16. Otros impuestos',0,0),(2161,'    VI. Resultado del ejercicio (beneficios)',0,0),(3000,'B) INGRESOS',0,0),(3010,'      1. Importe neto de la cifra de negocio',0,0),(3011,'          a) Ventas',0,0),(3012,'          b) Prestaciones de servicios',0,0),(3013,'          c) Devoluciones y `rappels` sobre ventas',0,0),(3020,'      2.Aumento de las existencias productos terminados y en curso de fabricación',0,0),(3030,'      3. Trabajos efectuados por la empresa para el inmovilizado',0,0),(3040,'      4. Otros ingresos de explotación',0,0),(3041,'          a) Ingresos accesorios y otros de gestión corriente',0,0),(3042,'          b) Subvenciones',0,0),(3043,'          c) Exceso de provisiones de riesgos y gastos',0,0),(3044,'    I. Pérdidas de explotación',0,0),(3050,'      5. Ingresos de participaciones en capital',0,0),(3051,'          a) En empresas del grupo',0,0),(3052,'          b) En empresas asociadas',0,0),(3053,'          c) En empresas fuera del grupo',0,0),(3060,'      6. Ingresos de otros valores negociables y créditos del activo inmovilizado',0,0),(3061,'          a) De empresas del grupo',0,0),(3062,'          b) De empresas asociadas',0,0),(3063,'          c) De empresas del grupo',0,0),(3070,'      7. Otros intereses e ingresos asimilados',0,0),(3071,'          a) De empresas del grupo',0,0),(3072,'          b) De empresas asociadas',0,0),(3073,'          c) Otros intereses',0,0),(3074,'          d) Beneficios en inversiones financieras',0,0),(3080,'      8. Diferencias positivas de cambio',0,0),(3081,'    II. Resultados financieros negativos',0,0),(3082,'    III. Pérdidas actividades ordinarias',0,0),(3090,'      9. Beneficios enajenación inmovilizado inmaterial,material y cartera contro',0,0),(3100,'      10. Beneficios por operaciones con acciones y obligaciones propias',0,0),(3110,'      11. Subvenciones de capital transferidas al resultado del ejercicio',0,0),(3120,'      12. Ingresos extraordinarios',0,0),(3130,'      13. Ingresos y beneficios de otros ejercicios',0,0),(3160,'    IV. Resultados extraordinarios negativos',0,0),(3170,'    V. Pérdidas antes de impuestos',0,0),(3180,'    VI. Resultado del ejercicio (pérdidas)',0,0),(4000,'A) GASTOS',0,0),(4010,'         1.  Consumos de explotación',0,0),(4020,'         2.  Gastos de personal',0,0),(4021,'              a) Sueldos, salarios y asimilados',0,0),(4022,'              b) Cargas sociales',0,0),(4030,'         3.  Dotaciones para amortizaciones de inmovilizado',0,0),(4040,'         4.  Variación de las provisiones de tráfico y pérdidas de créditos incobrables',0,0),(4050,'         5.  Otros gastos de explotación',0,0),(4051,'    I.   BENEFICIOS DE EXPLOTACIÓN',0,0),(4060,'         6.  Gastos financieros y gastos asimilados',0,0),(4061,'              a) Por deudas con empresas del grupo',0,0),(4062,'              b) Por deudas con empresas asociadas',0,0),(4063,'              c) Por otras deudas',0,0),(4064,'              d) Pérdidas de inversiones financieras',0,0),(4070,'         7.  Variación de las provisiones de inversiones financieras',0,0),(4080,'         8.  Diferencias negativas de cambio',0,0),(4081,'    II.  RESULTADOS FINANCIEROS POSITIVOS',0,0),(4082,'    III. BENEFICIOS DE LAS ACTIVIDADES ORDINARIAS',0,0),(4090,'         9.  Variación de las provisiones de inmovilizado inmaterial, material y cartera de control',0,0),(4100,'        10. Pérdidas procedentes del inmovilizado inmaterial, material y cartera de control',0,0),(4110,'        11. Pérdidas por operaciones con acciones y obligaciones propias',0,0),(4120,'        12. Gastos extraordinarios',0,0),(4130,'        13. Gastos y pérdidas de otros ejercicios',0,0),(4131,'   IV. RESULTADOS EXTRAORDINARIOS POSITIVOS',0,0),(4132,'   V.  BENEFICIOS ANTES DE IMPUESTOS',0,0),(4140,'        14. Impuesto sobre sociedades',0,0),(4150,'        15. Otros impuestos',0,0),(4151,'   VI. RESULTADO DEL EJERCICIO (BENEFICIOS)',0,0),(5000,'B) INGRESOS',0,0),(5010,'        1. Ingresos de explotación',0,0),(5011,'            a) Importe neto de la cifra de negocios',0,0),(5012,'            b) Otros ingresos de explotación',0,0),(5013,'    I.  PERDIDAS DE EXPLOTACION',0,0),(5020,'        2. Ingresos financieros',0,0),(5021,'            a) En empresas del grupo',0,0),(5022,'            b) En empresas asociadas',0,0),(5023,'            c) Otros',0,0),(5024,'            d) Beneficios en inversiones financieras',0,0),(5030,'        3. Diferencias positivas de cambio',0,0),(5031,'   II.  RESULTADOS FINANCIEROS NEGATIVOS',0,0),(5032,'   III. PERDIDAS DE LAS ACTIVIDADES ORDINARIAS',0,0),(5040,'       4. Beneficios en enajenación de inmovilizado inmaterial, material y cartera de control',0,0),(5050,'       5. Beneficios por operaciones con acciones y obligaciones propias',0,0),(5060,'       6. Subvenciones de capital transferidas al resultado del ejercicio',0,0),(5070,'       7. Ingresos extraordinarios',0,0),(5080,'       8. Ingresos y beneficios de otros ejercicios',0,0),(5081,'   IV. RESULTADOS EXTRAORDINARIOS NEGATIVOS',0,0),(5082,'   V.  PÉRDIDAS ANTES DE IMPUESTOS',0,0),(5083,'   VI. RESULTADO DEL EJERCICIO (PÉRDIDAS)',0,0);
/*!40000 ALTER TABLE `balances` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `balancesconimporte`
--

DROP TABLE IF EXISTS `balancesconimporte`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `balancesconimporte` (
  `Agrupacion` int DEFAULT NULL,
  `Titulo` text COLLATE utf8mb4_unicode_ci,
  `SumadeImporte` double DEFAULT NULL,
  `SaldoA_oAnterior` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `balancesconimporte`
--

LOCK TABLES `balancesconimporte` WRITE;
/*!40000 ALTER TABLE `balancesconimporte` DISABLE KEYS */;
INSERT INTO `balancesconimporte` VALUES (100,'A) Accionistas por desembolsos no exigidos',0,0),(200,'B) Inmovilizado',0,0),(210,'    I. Gastos de establecimiento',0,0),(220,'    II. Inmovilizaciones inmateriales',0,0),(221,'        1. Gastos de investigación y desarrollo',0,0),(222,'        2. Concesiones, patentes, licencias, marcas y similares',0,0),(223,'        3. Fondo de comercio',0,0),(224,'        4. Derechos de traspaso',0,0),(225,'        5. Aplicaciones informáticas',0,0),(226,'        6. Anticipos',0,0),(227,'        7. Provisiones',0,0),(228,'        8. Amortizaciones',0,0),(230,'    III. Inmovilizaciones materiales',0,0),(231,'        1. Terrenos y construcciones',0,0),(232,'        2. Instalaciones técnicas y maquinaria',0,0),(233,'        3. Otras instalaciones, utillaje y mobiliario',0,0),(234,'        4. Anticipos e inmovilizaciones materiales en curso',0,0),(235,'        5. Otro inmovilizado',0,0),(236,'        6. Provisiones',0,0),(237,'        7. Amortizaciones',0,0),(240,'    IV. Inmovilizaciones financieras',0,0),(241,'        1. Participaciones en empresas del grupo',0,0),(242,'        2. Créditos a empresas del grupo',0,0),(243,'        3. Participaciones en empresas asociadas',0,0),(244,'        4. Créditos a empresas asociadas',0,0),(245,'        5. Valores que tengan carácter de inmovilizaciones',0,0),(246,'        6. Otros créditos',0,0),(247,'        7. Depósitos y fianzas entregados a largo plazo',0,0),(248,'        8. Provisiones',0,0),(250,'    V. Acciones propias',0,0),(300,'C) Gastos a distribuir en varios ejercicios',0,0),(400,'D) Activo circulante',394,0),(410,'    I. Accionistas por desembolsos exigidos',0,0),(420,'    II. Existencias',0,0),(421,'        1. Comerciales',0,0),(422,'        2. Materias primas y otros aprovisionamientos',0,0),(423,'        3. Productos en curso y semiterminados',0,0),(424,'        4. Productos terminados',0,0),(425,'        5. Subproductos, residuos y materiales recuperados',0,0),(426,'        6. Anticipos',0,0),(427,'        7. Provisiones',0,0),(430,'    III. Deudores',394,0),(431,'        1. Clientes por ventas y prestaciones de servicios',394,0),(432,'        2. Sociedades del grupo, deudores',0,0),(433,'        3. Sociedades asociadas, deudores',0,0),(434,'        4. Deudores varios',0,0),(435,'        5. Personal',0,0),(436,'        6. Administraciones Públicas',0,0),(437,'        7. Provisiones',0,0),(440,'    IV. Inversiones financieras temporales',0,0),(441,'        1. Participaciones en empresas del grupo',0,0),(442,'        2. Créditos a empresas del grupo',0,0),(443,'        3. Participaciones en empresas asociadas',0,0),(444,'        4. Créditos a empresas asociadas',0,0),(445,'        5. Cartera de valores a corto plazo',0,0),(446,'        6. Créditos',0,0),(447,'        7. Depósitos y fianzas entregados a corto plazo',0,0),(448,'        8. Provisiones',0,0),(450,'    V. Acciones propias a corto plazo',0,0),(460,'    VI. Tesorería',0,0),(470,'    VII. Ajustes por periodificación',0,0),(1100,'A) Fondos propios',0,0),(1110,'    I. Capital suscrito',0,0),(1120,'    II. Prima de emisión',0,0),(1130,'    III. Reserva de revalorización',0,0),(1140,'    IV. Reservas',0,0),(1141,'        1. Reserva legal',0,0),(1142,'        2. Reservas para acciones propias',0,0),(1143,'        3. Reservas para acciones de la sociedad dominante',0,0),(1144,'        4. Reservas estatutarias',0,0),(1145,'        5. Otras reservas',0,0),(1150,'    V. Resultados de ejercicios anteriores',0,0),(1151,'        1. Remanente',0,0),(1152,'        2. Resultados negatigos de ejercicios anteriores',0,0),(1153,'        3. Aportaciones socios para compensación pérdidas',0,0),(1160,'    VI.Pérdidas y ganancias (beneficio o pérdida)',0,0),(1170,'    VII. Dividendo a cuenta entregado en el ejercicio',0,0),(1200,'B) Ingresos a distribuir en varios ejercicios',0,0),(1201,'        1. Subvenciones de capital',0,0),(1202,'        2. Diferencias positivas de cambio',0,0),(1203,'        3.Otros ingresos a distribuir en varios ejercicios',0,0),(1300,'C) Provisiones para riesgos y gastos',0,0),(1301,'        1. Provisiones pensiones y obligaciones similares',0,0),(1302,'        2. Provisiones para impuestos',0,0),(1303,'        3. Otras provisiones',0,0),(1304,'        4. Fondos de reversión',0,0),(1400,'D) Acreedores a largo plazo',0,0),(1410,'    I. Emisiones de obligaciones',0,0),(1411,'        1. Obligaciones no convertibles',0,0),(1412,'        2. Obligaciones convertibles',0,0),(1413,'        3. Otras deudas representadas en valores negociab.',0,0),(1420,'    II. Deudas con entidades de crédito',0,0),(1430,'    III. Deudas con empresas del grupo y asociadas',0,0),(1431,'        1. Deudas con empresas del grupo',0,0),(1432,'        2. Deudas con empresas asociadas',0,0),(1440,'    IV.Otros acreedores',0,0),(1441,'        1. Deudas representadas por efectos a pagar',0,0),(1442,'        2. Otras deudas',0,0),(1443,'        3. Fianzas y depósitos recibidos a largo plazo',0,0),(1450,'    V. Desembolsos pendientes sobre acciones no exigidos',0,0),(1451,'        1. De empresas del grupo',0,0),(1452,'        2. De empresas asociadas',0,0),(1453,'        3. De otras empresas',0,0),(1500,'E) Acreedores a corto plazo',54,0),(1510,'    I. Emisiones de obligaciones',0,0),(1511,'        1. Obligaciones no convertibles',0,0),(1512,'        2. Obligaciones convertibles',0,0),(1513,'        3. Otras deudas representadas valores negociables',0,0),(1514,'        4. Intereses de obligaciones y otros valores',0,0),(1520,'    II. Deudas con entidades decrédito',0,0),(1521,'        1. Préstamos y otras deudas',0,0),(1522,'        2. Deudas por intereses',0,0),(1530,'    III. Deudas con empresas del grupo y asociadas a corto plazo',0,0),(1531,'        1. Deudas con empresas del grupo',0,0),(1532,'        2. Deudas con empresas asociadas',0,0),(1540,'    IV. Acreedores comerciales',0,0),(1541,'        1. Anticipos recibidos por pedidos',0,0),(1542,'        2. Deudas por compras o prestaciones de servicios',0,0),(1543,'        3. Deudas representadas por efectos a pagar',0,0),(1550,'    V. Otras deudas no comerciales',54,0),(1551,'        1. Administraciones Públicas',54,0),(1552,'        2. Deudas representadas por efectos a pagar',0,0),(1553,'        3. Otras deudas',0,0),(1554,'        4. Remuneraciones pendientes de pago',0,0),(1555,'        5. Fianzas y depósitos recibidos a corto plazo',0,0),(1560,'    VI. Provisiones para operaciones de tráfico',0,0),(1570,'    VII. Ajustes por periodificación',0,0),(2000,'A) GASTOS',0,0),(2010,'      1. Reducción de existencias productos terminados y en curso de fabricación',0,0),(2020,'      2. Aprovisionamientos',0,0),(2021,'          a) Consumo de mercaderías',0,0),(2022,'          b) Consumo de materias primas y otras materias consumibles',0,0),(2023,'          c) Otros gastos externos',0,0),(2030,'      3. Gastos de personal',0,0),(2031,'          a) Sueldos, salarios y asimilados',0,0),(2032,'          b) Cargas sociales',0,0),(2040,'      4. Dotaciones para amortizaciones de inmovilizado',0,0),(2050,'      5. Variación de las provisiones de tráfico',0,0),(2051,'          a) Variación de provisiones de existencias',0,0),(2052,'          b) Variación de provisiones y pérdidas de créditos incobrables',0,0),(2053,'          c) Variación de otras provisiones de tráfico',0,0),(2060,'      6. Otros gastos de explotación',0,0),(2061,'          a) Servicios exteriores',0,0),(2062,'          b) Tributos',0,0),(2063,'          c) Otros gastos de gestión corriente',0,0),(2064,'          d) Dotación al fondo de reversión',0,0),(2065,'    I. Beneficios de Explotación',340,0),(2070,'      7. Gastos financieros y gastos asimilados',0,0),(2071,'          a) Por deudas con empresas del grupo',0,0),(2072,'          b) Por deudas con empresas asociadas',0,0),(2073,'          c) Por deudas con terceros y gastos asimilados',0,0),(2074,'          d) Pérdidas de inversiones financieras',0,0),(2080,'      8. Variación de las provisiones de inversiones financieras',0,0),(2090,'      9. Diferencias negativas de cambio',0,0),(2091,'    II. Resultados financieros positivos',0,0),(2092,'    III. Beneficios actividades ordinarias',340,0),(2100,'      10. Var. previsiones inmovilizado inmaterial, material y cartera de control',0,0),(2110,'      11. Pérds.procedentes inmovilizado inmaterial,material y cartera de control',0,0),(2120,'      12. Pérdidas por operaciones con acciones y obligaciones propias',0,0),(2130,'      13. Gastos extraordinarios',0,0),(2140,'      14. Gastos y pérdidas de otros ejercicios',0,0),(2141,'    IV. Resultados extraordinarios positivos ',0,0),(2142,'    V. Beneficios antes de impuestos',340,0),(2150,'      15. Impuesto sobre sociedades',0,0),(2160,'      16. Otros impuestos',0,0),(2161,'    VI. Resultado del ejercicio (beneficios)',340,0),(3000,'B) INGRESOS',340,0),(3010,'      1. Importe neto de la cifra de negocio',340,0),(3011,'          a) Ventas',340,0),(3012,'          b) Prestaciones de servicios',0,0),(3013,'          c) Devoluciones y `rappels` sobre ventas',0,0),(3020,'      2.Aumento de las existencias productos terminados y en curso de fabricación',0,0),(3030,'      3. Trabajos efectuados por la empresa para el inmovilizado',0,0),(3040,'      4. Otros ingresos de explotación',0,0),(3041,'          a) Ingresos accesorios y otros de gestión corriente',0,0),(3042,'          b) Subvenciones',0,0),(3043,'          c) Exceso de provisiones de riesgos y gastos',0,0),(3044,'    I. Pérdidas de explotación',0,0),(3050,'      5. Ingresos de participaciones en capital',0,0),(3051,'          a) En empresas del grupo',0,0),(3052,'          b) En empresas asociadas',0,0),(3053,'          c) En empresas fuera del grupo',0,0),(3060,'      6. Ingresos de otros valores negociables y créditos del activo inmovilizado',0,0),(3061,'          a) De empresas del grupo',0,0),(3062,'          b) De empresas asociadas',0,0),(3063,'          c) De empresas del grupo',0,0),(3070,'      7. Otros intereses e ingresos asimilados',0,0),(3071,'          a) De empresas del grupo',0,0),(3072,'          b) De empresas asociadas',0,0),(3073,'          c) Otros intereses',0,0),(3074,'          d) Beneficios en inversiones financieras',0,0),(3080,'      8. Diferencias positivas de cambio',0,0),(3081,'    II. Resultados financieros negativos',0,0),(3082,'    III. Pérdidas actividades ordinarias',0,0),(3090,'      9. Beneficios enajenación inmovilizado inmaterial,material y cartera contro',0,0),(3100,'      10. Beneficios por operaciones con acciones y obligaciones propias',0,0),(3110,'      11. Subvenciones de capital transferidas al resultado del ejercicio',0,0),(3120,'      12. Ingresos extraordinarios',0,0),(3130,'      13. Ingresos y beneficios de otros ejercicios',0,0),(3160,'    IV. Resultados extraordinarios negativos',0,0),(3170,'    V. Pérdidas antes de impuestos',0,0),(3180,'    VI. Resultado del ejercicio (pérdidas)',0,0),(4000,'A) GASTOS',0,0),(4010,'         1.  Consumos de explotación',0,0),(4020,'         2.  Gastos de personal',0,0),(4021,'              a) Sueldos, salarios y asimilados',0,0),(4022,'              b) Cargas sociales',0,0),(4030,'         3.  Dotaciones para amortizaciones de inmovilizado',0,0),(4040,'         4.  Variación de las provisiones de tráfico y pérdidas de créditos incobrables',0,0),(4050,'         5.  Otros gastos de explotación',0,0),(4051,'    I.   BENEFICIOS DE EXPLOTACIÓN',0,0),(4060,'         6.  Gastos financieros y gastos asimilados',0,0),(4061,'              a) Por deudas con empresas del grupo',0,0),(4062,'              b) Por deudas con empresas asociadas',0,0),(4063,'              c) Por otras deudas',0,0),(4064,'              d) Pérdidas de inversiones financieras',0,0),(4070,'         7.  Variación de las provisiones de inversiones financieras',0,0),(4080,'         8.  Diferencias negativas de cambio',0,0),(4081,'    II.  RESULTADOS FINANCIEROS POSITIVOS',0,0),(4082,'    III. BENEFICIOS DE LAS ACTIVIDADES ORDINARIAS',0,0),(4090,'         9.  Variación de las provisiones de inmovilizado inmaterial, material y cartera de control',0,0),(4100,'        10. Pérdidas procedentes del inmovilizado inmaterial, material y cartera de control',0,0),(4110,'        11. Pérdidas por operaciones con acciones y obligaciones propias',0,0),(4120,'        12. Gastos extraordinarios',0,0),(4130,'        13. Gastos y pérdidas de otros ejercicios',0,0),(4131,'   IV. RESULTADOS EXTRAORDINARIOS POSITIVOS',0,0),(4132,'   V.  BENEFICIOS ANTES DE IMPUESTOS',0,0),(4140,'        14. Impuesto sobre sociedades',0,0),(4150,'        15. Otros impuestos',0,0),(4151,'   VI. RESULTADO DEL EJERCICIO (BENEFICIOS)',0,0),(5000,'B) INGRESOS',340,0),(5010,'        1. Ingresos de explotación',340,0),(5011,'            a) Importe neto de la cifra de negocios',340,0),(5012,'            b) Otros ingresos de explotación',0,0),(5013,'    I.  PERDIDAS DE EXPLOTACION',0,0),(5020,'        2. Ingresos financieros',0,0),(5021,'            a) En empresas del grupo',0,0),(5022,'            b) En empresas asociadas',0,0),(5023,'            c) Otros',0,0),(5024,'            d) Beneficios en inversiones financieras',0,0),(5030,'        3. Diferencias positivas de cambio',0,0),(5031,'   II.  RESULTADOS FINANCIEROS NEGATIVOS',0,0),(5032,'   III. PERDIDAS DE LAS ACTIVIDADES ORDINARIAS',0,0),(5040,'       4. Beneficios en enajenación de inmovilizado inmaterial, material y cartera de control',0,0),(5050,'       5. Beneficios por operaciones con acciones y obligaciones propias',0,0),(5060,'       6. Subvenciones de capital transferidas al resultado del ejercicio',0,0),(5070,'       7. Ingresos extraordinarios',0,0),(5080,'       8. Ingresos y beneficios de otros ejercicios',0,0),(5081,'   IV. RESULTADOS EXTRAORDINARIOS NEGATIVOS',0,0),(5082,'   V.  PÉRDIDAS ANTES DE IMPUESTOS',0,0),(5083,'   VI. RESULTADO DEL EJERCICIO (PÉRDIDAS)',0,0),(100,'A) Accionistas por desembolsos no exigidos',0,0),(200,'B) Inmovilizado',0,0),(210,'    I. Gastos de establecimiento',0,0),(220,'    II. Inmovilizaciones inmateriales',0,0),(221,'        1. Gastos de investigación y desarrollo',0,0),(222,'        2. Concesiones, patentes, licencias, marcas y similares',0,0),(223,'        3. Fondo de comercio',0,0),(224,'        4. Derechos de traspaso',0,0),(225,'        5. Aplicaciones informáticas',0,0),(226,'        6. Anticipos',0,0),(227,'        7. Provisiones',0,0),(228,'        8. Amortizaciones',0,0),(230,'    III. Inmovilizaciones materiales',0,0),(231,'        1. Terrenos y construcciones',0,0),(232,'        2. Instalaciones técnicas y maquinaria',0,0),(233,'        3. Otras instalaciones, utillaje y mobiliario',0,0),(234,'        4. Anticipos e inmovilizaciones materiales en curso',0,0),(235,'        5. Otro inmovilizado',0,0),(236,'        6. Provisiones',0,0),(237,'        7. Amortizaciones',0,0),(240,'    IV. Inmovilizaciones financieras',0,0),(241,'        1. Participaciones en empresas del grupo',0,0),(242,'        2. Créditos a empresas del grupo',0,0),(243,'        3. Participaciones en empresas asociadas',0,0),(244,'        4. Créditos a empresas asociadas',0,0),(245,'        5. Valores que tengan carácter de inmovilizaciones',0,0),(246,'        6. Otros créditos',0,0),(247,'        7. Depósitos y fianzas entregados a largo plazo',0,0),(248,'        8. Provisiones',0,0),(250,'    V. Acciones propias',0,0),(300,'C) Gastos a distribuir en varios ejercicios',0,0),(400,'D) Activo circulante',394,0),(410,'    I. Accionistas por desembolsos exigidos',0,0),(420,'    II. Existencias',0,0),(421,'        1. Comerciales',0,0),(422,'        2. Materias primas y otros aprovisionamientos',0,0),(423,'        3. Productos en curso y semiterminados',0,0),(424,'        4. Productos terminados',0,0),(425,'        5. Subproductos, residuos y materiales recuperados',0,0),(426,'        6. Anticipos',0,0),(427,'        7. Provisiones',0,0),(430,'    III. Deudores',394,0),(431,'        1. Clientes por ventas y prestaciones de servicios',394,0),(432,'        2. Sociedades del grupo, deudores',0,0),(433,'        3. Sociedades asociadas, deudores',0,0),(434,'        4. Deudores varios',0,0),(435,'        5. Personal',0,0),(436,'        6. Administraciones Públicas',0,0),(437,'        7. Provisiones',0,0),(440,'    IV. Inversiones financieras temporales',0,0),(441,'        1. Participaciones en empresas del grupo',0,0),(442,'        2. Créditos a empresas del grupo',0,0),(443,'        3. Participaciones en empresas asociadas',0,0),(444,'        4. Créditos a empresas asociadas',0,0),(445,'        5. Cartera de valores a corto plazo',0,0),(446,'        6. Créditos',0,0),(447,'        7. Depósitos y fianzas entregados a corto plazo',0,0),(448,'        8. Provisiones',0,0),(450,'    V. Acciones propias a corto plazo',0,0),(460,'    VI. Tesorería',0,0),(470,'    VII. Ajustes por periodificación',0,0),(1100,'A) Fondos propios',0,0),(1110,'    I. Capital suscrito',0,0),(1120,'    II. Prima de emisión',0,0),(1130,'    III. Reserva de revalorización',0,0),(1140,'    IV. Reservas',0,0),(1141,'        1. Reserva legal',0,0),(1142,'        2. Reservas para acciones propias',0,0),(1143,'        3. Reservas para acciones de la sociedad dominante',0,0),(1144,'        4. Reservas estatutarias',0,0),(1145,'        5. Otras reservas',0,0),(1150,'    V. Resultados de ejercicios anteriores',0,0),(1151,'        1. Remanente',0,0),(1152,'        2. Resultados negatigos de ejercicios anteriores',0,0),(1153,'        3. Aportaciones socios para compensación pérdidas',0,0),(1160,'    VI.Pérdidas y ganancias (beneficio o pérdida)',0,0),(1170,'    VII. Dividendo a cuenta entregado en el ejercicio',0,0),(1200,'B) Ingresos a distribuir en varios ejercicios',0,0),(1201,'        1. Subvenciones de capital',0,0),(1202,'        2. Diferencias positivas de cambio',0,0),(1203,'        3.Otros ingresos a distribuir en varios ejercicios',0,0),(1300,'C) Provisiones para riesgos y gastos',0,0),(1301,'        1. Provisiones pensiones y obligaciones similares',0,0),(1302,'        2. Provisiones para impuestos',0,0),(1303,'        3. Otras provisiones',0,0),(1304,'        4. Fondos de reversión',0,0),(1400,'D) Acreedores a largo plazo',0,0),(1410,'    I. Emisiones de obligaciones',0,0),(1411,'        1. Obligaciones no convertibles',0,0),(1412,'        2. Obligaciones convertibles',0,0),(1413,'        3. Otras deudas representadas en valores negociab.',0,0),(1420,'    II. Deudas con entidades de crédito',0,0),(1430,'    III. Deudas con empresas del grupo y asociadas',0,0),(1431,'        1. Deudas con empresas del grupo',0,0),(1432,'        2. Deudas con empresas asociadas',0,0),(1440,'    IV.Otros acreedores',0,0),(1441,'        1. Deudas representadas por efectos a pagar',0,0),(1442,'        2. Otras deudas',0,0),(1443,'        3. Fianzas y depósitos recibidos a largo plazo',0,0),(1450,'    V. Desembolsos pendientes sobre acciones no exigidos',0,0),(1451,'        1. De empresas del grupo',0,0),(1452,'        2. De empresas asociadas',0,0),(1453,'        3. De otras empresas',0,0),(1500,'E) Acreedores a corto plazo',54,0),(1510,'    I. Emisiones de obligaciones',0,0),(1511,'        1. Obligaciones no convertibles',0,0),(1512,'        2. Obligaciones convertibles',0,0),(1513,'        3. Otras deudas representadas valores negociables',0,0),(1514,'        4. Intereses de obligaciones y otros valores',0,0),(1520,'    II. Deudas con entidades decrédito',0,0),(1521,'        1. Préstamos y otras deudas',0,0),(1522,'        2. Deudas por intereses',0,0),(1530,'    III. Deudas con empresas del grupo y asociadas a corto plazo',0,0),(1531,'        1. Deudas con empresas del grupo',0,0),(1532,'        2. Deudas con empresas asociadas',0,0),(1540,'    IV. Acreedores comerciales',0,0),(1541,'        1. Anticipos recibidos por pedidos',0,0),(1542,'        2. Deudas por compras o prestaciones de servicios',0,0),(1543,'        3. Deudas representadas por efectos a pagar',0,0),(1550,'    V. Otras deudas no comerciales',54,0),(1551,'        1. Administraciones Públicas',54,0),(1552,'        2. Deudas representadas por efectos a pagar',0,0),(1553,'        3. Otras deudas',0,0),(1554,'        4. Remuneraciones pendientes de pago',0,0),(1555,'        5. Fianzas y depósitos recibidos a corto plazo',0,0),(1560,'    VI. Provisiones para operaciones de tráfico',0,0),(1570,'    VII. Ajustes por periodificación',0,0),(2000,'A) GASTOS',0,0),(2010,'      1. Reducción de existencias productos terminados y en curso de fabricación',0,0),(2020,'      2. Aprovisionamientos',0,0),(2021,'          a) Consumo de mercaderías',0,0),(2022,'          b) Consumo de materias primas y otras materias consumibles',0,0),(2023,'          c) Otros gastos externos',0,0),(2030,'      3. Gastos de personal',0,0),(2031,'          a) Sueldos, salarios y asimilados',0,0),(2032,'          b) Cargas sociales',0,0),(2040,'      4. Dotaciones para amortizaciones de inmovilizado',0,0),(2050,'      5. Variación de las provisiones de tráfico',0,0),(2051,'          a) Variación de provisiones de existencias',0,0),(2052,'          b) Variación de provisiones y pérdidas de créditos incobrables',0,0),(2053,'          c) Variación de otras provisiones de tráfico',0,0),(2060,'      6. Otros gastos de explotación',0,0),(2061,'          a) Servicios exteriores',0,0),(2062,'          b) Tributos',0,0),(2063,'          c) Otros gastos de gestión corriente',0,0),(2064,'          d) Dotación al fondo de reversión',0,0),(2065,'    I. Beneficios de Explotación',340,0),(2070,'      7. Gastos financieros y gastos asimilados',0,0),(2071,'          a) Por deudas con empresas del grupo',0,0),(2072,'          b) Por deudas con empresas asociadas',0,0),(2073,'          c) Por deudas con terceros y gastos asimilados',0,0),(2074,'          d) Pérdidas de inversiones financieras',0,0),(2080,'      8. Variación de las provisiones de inversiones financieras',0,0),(2090,'      9. Diferencias negativas de cambio',0,0),(2091,'    II. Resultados financieros positivos',0,0),(2092,'    III. Beneficios actividades ordinarias',340,0),(2100,'      10. Var. previsiones inmovilizado inmaterial, material y cartera de control',0,0),(2110,'      11. Pérds.procedentes inmovilizado inmaterial,material y cartera de control',0,0),(2120,'      12. Pérdidas por operaciones con acciones y obligaciones propias',0,0),(2130,'      13. Gastos extraordinarios',0,0),(2140,'      14. Gastos y pérdidas de otros ejercicios',0,0),(2141,'    IV. Resultados extraordinarios positivos ',0,0),(2142,'    V. Beneficios antes de impuestos',340,0),(2150,'      15. Impuesto sobre sociedades',0,0),(2160,'      16. Otros impuestos',0,0),(2161,'    VI. Resultado del ejercicio (beneficios)',340,0),(3000,'B) INGRESOS',340,0),(3010,'      1. Importe neto de la cifra de negocio',340,0),(3011,'          a) Ventas',340,0),(3012,'          b) Prestaciones de servicios',0,0),(3013,'          c) Devoluciones y `rappels` sobre ventas',0,0),(3020,'      2.Aumento de las existencias productos terminados y en curso de fabricación',0,0),(3030,'      3. Trabajos efectuados por la empresa para el inmovilizado',0,0),(3040,'      4. Otros ingresos de explotación',0,0),(3041,'          a) Ingresos accesorios y otros de gestión corriente',0,0),(3042,'          b) Subvenciones',0,0),(3043,'          c) Exceso de provisiones de riesgos y gastos',0,0),(3044,'    I. Pérdidas de explotación',0,0),(3050,'      5. Ingresos de participaciones en capital',0,0),(3051,'          a) En empresas del grupo',0,0),(3052,'          b) En empresas asociadas',0,0),(3053,'          c) En empresas fuera del grupo',0,0),(3060,'      6. Ingresos de otros valores negociables y créditos del activo inmovilizado',0,0),(3061,'          a) De empresas del grupo',0,0),(3062,'          b) De empresas asociadas',0,0),(3063,'          c) De empresas del grupo',0,0),(3070,'      7. Otros intereses e ingresos asimilados',0,0),(3071,'          a) De empresas del grupo',0,0),(3072,'          b) De empresas asociadas',0,0),(3073,'          c) Otros intereses',0,0),(3074,'          d) Beneficios en inversiones financieras',0,0),(3080,'      8. Diferencias positivas de cambio',0,0),(3081,'    II. Resultados financieros negativos',0,0),(3082,'    III. Pérdidas actividades ordinarias',0,0),(3090,'      9. Beneficios enajenación inmovilizado inmaterial,material y cartera contro',0,0),(3100,'      10. Beneficios por operaciones con acciones y obligaciones propias',0,0),(3110,'      11. Subvenciones de capital transferidas al resultado del ejercicio',0,0),(3120,'      12. Ingresos extraordinarios',0,0),(3130,'      13. Ingresos y beneficios de otros ejercicios',0,0),(3160,'    IV. Resultados extraordinarios negativos',0,0),(3170,'    V. Pérdidas antes de impuestos',0,0),(3180,'    VI. Resultado del ejercicio (pérdidas)',0,0),(4000,'A) GASTOS',0,0),(4010,'         1.  Consumos de explotación',0,0),(4020,'         2.  Gastos de personal',0,0),(4021,'              a) Sueldos, salarios y asimilados',0,0),(4022,'              b) Cargas sociales',0,0),(4030,'         3.  Dotaciones para amortizaciones de inmovilizado',0,0),(4040,'         4.  Variación de las provisiones de tráfico y pérdidas de créditos incobrables',0,0),(4050,'         5.  Otros gastos de explotación',0,0),(4051,'    I.   BENEFICIOS DE EXPLOTACIÓN',0,0),(4060,'         6.  Gastos financieros y gastos asimilados',0,0),(4061,'              a) Por deudas con empresas del grupo',0,0),(4062,'              b) Por deudas con empresas asociadas',0,0),(4063,'              c) Por otras deudas',0,0),(4064,'              d) Pérdidas de inversiones financieras',0,0),(4070,'         7.  Variación de las provisiones de inversiones financieras',0,0),(4080,'         8.  Diferencias negativas de cambio',0,0),(4081,'    II.  RESULTADOS FINANCIEROS POSITIVOS',0,0),(4082,'    III. BENEFICIOS DE LAS ACTIVIDADES ORDINARIAS',0,0),(4090,'         9.  Variación de las provisiones de inmovilizado inmaterial, material y cartera de control',0,0),(4100,'        10. Pérdidas procedentes del inmovilizado inmaterial, material y cartera de control',0,0),(4110,'        11. Pérdidas por operaciones con acciones y obligaciones propias',0,0),(4120,'        12. Gastos extraordinarios',0,0),(4130,'        13. Gastos y pérdidas de otros ejercicios',0,0),(4131,'   IV. RESULTADOS EXTRAORDINARIOS POSITIVOS',0,0),(4132,'   V.  BENEFICIOS ANTES DE IMPUESTOS',0,0),(4140,'        14. Impuesto sobre sociedades',0,0),(4150,'        15. Otros impuestos',0,0),(4151,'   VI. RESULTADO DEL EJERCICIO (BENEFICIOS)',0,0),(5000,'B) INGRESOS',340,0),(5010,'        1. Ingresos de explotación',340,0),(5011,'            a) Importe neto de la cifra de negocios',340,0),(5012,'            b) Otros ingresos de explotación',0,0),(5013,'    I.  PERDIDAS DE EXPLOTACION',0,0),(5020,'        2. Ingresos financieros',0,0),(5021,'            a) En empresas del grupo',0,0),(5022,'            b) En empresas asociadas',0,0),(5023,'            c) Otros',0,0),(5024,'            d) Beneficios en inversiones financieras',0,0),(5030,'        3. Diferencias positivas de cambio',0,0),(5031,'   II.  RESULTADOS FINANCIEROS NEGATIVOS',0,0),(5032,'   III. PERDIDAS DE LAS ACTIVIDADES ORDINARIAS',0,0),(5040,'       4. Beneficios en enajenación de inmovilizado inmaterial, material y cartera de control',0,0),(5050,'       5. Beneficios por operaciones con acciones y obligaciones propias',0,0),(5060,'       6. Subvenciones de capital transferidas al resultado del ejercicio',0,0),(5070,'       7. Ingresos extraordinarios',0,0),(5080,'       8. Ingresos y beneficios de otros ejercicios',0,0),(5081,'   IV. RESULTADOS EXTRAORDINARIOS NEGATIVOS',0,0),(5082,'   V.  PÉRDIDAS ANTES DE IMPUESTOS',0,0),(5083,'   VI. RESULTADO DEL EJERCICIO (PÉRDIDAS)',0,0);
/*!40000 ALTER TABLE `balancesconimporte` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `balancescuentas`
--

DROP TABLE IF EXISTS `balancescuentas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `balancescuentas` (
  `Agrupacion` int DEFAULT NULL,
  `CodigoCuenta` int DEFAULT NULL,
  `Signo` int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `balancescuentas`
--

LOCK TABLES `balancescuentas` WRITE;
/*!40000 ALTER TABLE `balancescuentas` DISABLE KEYS */;
INSERT INTO `balancescuentas` VALUES (100,190,0),(100,191,0),(100,192,0),(100,193,0),(100,194,0),(100,195,0),(210,20,0),(221,210,0),(222,211,0),(222,212,0),(223,213,0),(224,214,0),(225,215,0),(226,219,0),(227,291,0),(228,281,0),(231,220,0),(231,221,0),(232,222,0),(232,223,0),(233,224,0),(233,225,0),(233,226,0),(234,23,0),(235,227,0),(235,228,0),(235,229,0),(236,292,0),(237,282,0),(241,240,0),(242,242,0),(242,244,0),(243,241,0),(244,243,0),(244,245,0),(244,247,0),(245,250,0),(245,251,0),(245,256,0),(246,252,0),(246,253,0),(246,254,0),(246,257,0),(246,258,0),(247,260,0),(247,265,0),(248,293,0),(248,294,0),(248,295,0),(248,296,0),(248,297,0),(248,298,0),(250,198,0),(300,27,0),(410,558,0),(421,30,0),(422,31,0),(422,32,0),(423,33,0),(423,34,0),(424,35,0),(425,36,0),(426,407,0),(427,39,0),(431,430,0),(431,431,0),(431,435,0),(431,436,0),(432,432,0),(432,551,1),(433,433,0),(433,552,1),(434,44,0),(434,553,1),(435,460,0),(435,544,0),(436,470,0),(436,471,0),(436,472,0),(436,474,0),(437,490,0),(437,493,0),(437,494,0),(441,530,0),(441,538,0),(442,532,0),(442,534,0),(442,536,0),(443,531,0),(443,539,0),(444,533,0),(444,535,0),(444,537,0),(445,540,0),(445,541,0),(445,546,0),(445,549,0),(446,542,0),(446,543,0),(446,545,0),(446,547,0),(446,548,0),(447,565,0),(447,566,0),(448,593,0),(448,594,0),(448,595,0),(448,596,0),(448,597,0),(448,598,0),(460,57,0),(470,480,0),(470,580,0),(1110,10,2),(1120,110,2),(1130,111,2),(1141,112,2),(1142,115,2),(1143,114,2),(1144,116,2),(1145,113,2),(1145,117,2),(1145,118,2),(1151,120,2),(1152,121,2),(1153,122,2),(420,30,0),(1160,129,2),(1170,557,2),(1201,130,2),(1201,131,2),(1202,136,2),(1203,135,2),(1301,140,2),(1302,141,2),(1303,142,2),(1303,143,2),(1304,144,2),(1411,150,2),(1412,151,2),(1413,155,2),(1420,170,2),(1431,160,2),(1431,162,2),(1431,164,2),(1432,161,2),(1432,163,2),(1432,165,2),(1441,174,2),(1442,171,2),(1442,172,2),(1442,173,2),(1443,180,2),(1443,185,2),(1451,248,2),(1452,249,2),(1453,259,2),(1511,500,2),(1512,501,2),(1513,505,2),(1514,506,2),(1521,520,2),(1522,526,2),(1531,402,2),(1531,510,2),(1531,512,2),(1531,514,2),(1531,516,2),(1531,551,3),(1532,403,2),(1532,511,2),(1532,513,2),(1532,515,2),(1532,517,2),(1532,552,3),(1541,437,2),(1542,400,2),(1542,406,2),(1542,410,2),(1542,419,2),(1543,401,2),(1543,411,2),(1551,475,2),(1551,476,2),(1551,477,2),(1551,479,2),(1552,524,2),(1553,509,2),(1553,521,2),(1553,523,2),(1553,525,2),(1553,527,2),(1553,553,3),(1553,555,2),(1553,556,2),(1554,465,2),(1555,560,2),(1555,561,2),(1560,499,2),(1570,485,2),(1570,585,2),(2010,71,1),(2021,600,0),(2021,6080,0),(2021,6090,0),(2021,610,0),(2022,601,0),(2022,602,0),(2022,6081,0),(2022,6082,0),(2022,6091,0),(2022,6092,0),(2022,611,0),(2022,612,0),(2023,607,0),(2031,640,0),(2031,641,0),(2032,642,0),(2032,643,0),(2032,649,0),(2040,68,0),(2051,693,0),(2051,793,0),(2052,650,0),(2052,694,0),(2052,794,0),(2053,695,0),(2053,795,0),(2061,62,0),(2062,631,0),(2062,634,0),(2062,636,0),(2062,639,0),(2063,651,0),(2063,659,0),(2064,690,0),(2071,6610,0),(2071,6615,0),(2071,6620,0),(2071,6630,0),(2071,6640,0),(2071,6650,0),(2072,6611,0),(2072,6616,0),(2072,6621,0),(2072,6631,0),(2072,6641,0),(2072,6651,0),(2073,6613,0),(2073,6618,0),(2073,6622,0),(2073,6623,0),(2073,6632,0),(2073,6633,0),(2073,6642,0),(2073,6653,0),(2073,669,0),(2074,666,0),(2074,667,0),(2080,6963,0),(2080,6965,0),(2080,6966,0),(2080,697,0),(2080,698,0),(2080,699,0),(2080,7963,0),(2080,7965,0),(2080,7966,0),(2080,797,0),(2080,798,0),(2080,799,0),(2090,668,0),(2100,691,0),(2100,692,0),(2100,6960,0),(2100,6961,0),(2100,791,0),(2100,792,0),(2100,7960,0),(2100,7961,0),(2110,670,0),(2110,671,0),(2110,672,0),(2110,673,0),(2120,674,0),(2130,678,0),(2140,679,0),(2150,630,0),(2150,633,0),(2150,638,0),(3011,700,2),(3011,701,2),(3011,702,2),(3011,703,2),(3011,704,2),(3012,705,2),(3013,708,2),(3013,709,2),(3020,71,3),(3030,73,2),(3041,75,2),(3042,74,2),(3043,790,2),(3051,7600,2),(3052,7601,2),(3053,7603,2),(3061,7610,2),(3061,7620,2),(3062,7611,2),(3062,7621,2),(3063,7613,2),(3063,7623,2),(3071,7630,2),(3071,7650,2),(3072,7631,2),(3072,7651,2),(3073,7633,2),(3073,7653,2),(3073,769,2),(3074,766,2),(3080,768,2),(3090,770,2),(3090,771,2),(3090,772,2),(3100,774,2),(3110,775,2),(3120,778,2),(3130,779,2),(100,196,0),(220,210,0),(220,211,0),(220,212,0),(220,213,0),(220,214,0),(220,215,0),(220,219,0),(220,291,0),(220,281,0),(200,20,0),(200,210,0),(200,211,0),(200,212,0),(200,213,0),(200,214,0),(200,215,0),(200,219,0),(200,291,0),(200,281,0),(230,220,0),(230,221,0),(230,222,0),(230,223,0),(230,224,0),(230,225,0),(230,226,0),(230,23,0),(230,227,0),(230,228,0),(230,229,0),(230,292,0),(230,282,0),(200,220,0),(200,221,0),(200,222,0),(200,223,0),(200,224,0),(200,225,0),(200,226,0),(200,23,0),(200,227,0),(200,228,0),(200,229,0),(200,292,0),(200,282,0),(242,246,0),(240,240,0),(240,242,0),(240,244,0),(240,246,0),(240,241,0),(240,243,0),(240,245,0),(240,247,0),(240,250,0),(240,251,0),(240,256,0),(240,252,0),(240,253,0),(240,254,0),(240,257,0),(240,258,0),(240,260,0),(240,265,0),(240,293,0),(240,294,0),(240,295,0),(240,296,0),(240,297,0),(240,298,0),(200,240,0),(200,242,0),(200,244,0),(200,246,0),(200,241,0),(200,243,0),(200,245,0),(200,247,0),(200,250,0),(200,251,0),(200,256,0),(200,252,0),(200,253,0),(200,254,0),(200,257,0),(200,258,0),(200,260,0),(200,265,0),(200,293,0),(200,294,0),(200,295,0),(200,296,0),(200,297,0),(200,298,0),(200,198,0),(420,31,0),(420,32,0),(420,33,0),(420,34,0),(420,35,0),(420,36,0),(420,407,0),(420,39,0),(400,558,0),(400,30,0),(400,31,0),(400,32,0),(400,33,0),(400,34,0),(400,35,0),(400,36,0),(400,407,0),(400,39,0),(430,430,0),(430,431,0),(430,435,0),(430,436,0),(430,432,0),(430,551,1),(430,433,0),(430,552,1),(430,44,0),(430,553,1),(430,460,0),(430,544,0),(430,470,0),(430,471,0),(430,472,0),(430,474,0),(430,490,0),(430,493,0),(430,494,0),(400,430,0),(400,431,0),(400,435,0),(400,436,0),(400,432,0),(400,551,1),(400,433,0),(400,552,1),(400,44,0),(400,553,1),(400,460,0),(400,544,0),(400,470,0),(400,471,0),(400,472,0),(400,474,0),(400,490,0),(400,493,0),(400,494,0),(440,530,0),(440,538,0),(440,532,0),(440,534,0),(440,536,0),(440,531,0),(440,539,0),(440,533,0),(440,535,0),(440,537,0),(440,540,0),(440,541,0),(440,546,0),(440,549,0),(440,542,0),(440,543,0),(440,545,0),(440,547,0),(440,548,0),(440,565,0),(440,566,0),(440,593,0),(440,594,0),(440,595,0),(440,596,0),(440,597,0),(440,598,0),(400,530,0),(400,538,0),(400,532,0),(400,534,0),(400,536,0),(400,531,0),(400,539,0),(400,533,0),(400,535,0),(400,537,0),(400,540,0),(400,541,0),(400,546,0),(400,549,0),(400,542,0),(400,543,0),(400,545,0),(400,547,0),(400,548,0),(400,565,0),(400,566,0),(400,593,0),(400,594,0),(400,595,0),(400,596,0),(400,597,0),(400,598,0),(400,57,0),(400,480,0),(400,580,0),(1140,112,2),(1140,115,2),(1140,114,2),(1140,116,2),(1140,113,2),(1140,117,2),(1140,118,2),(1150,120,2),(1150,121,2),(1150,122,2),(1100,10,2),(1100,110,2),(1100,111,2),(1100,112,2),(1100,115,2),(1100,114,2),(1100,116,2),(1100,113,2),(1100,117,2),(1100,118,2),(1100,120,2),(1100,121,2),(1100,122,2),(1100,129,2),(1100,557,2),(1200,130,2),(1200,131,2),(1200,136,2),(1200,135,2),(1300,140,2),(1300,141,2),(1300,142,2),(1300,143,2),(1300,144,2),(1410,150,2),(1410,151,2),(1410,155,2),(1430,160,2),(1430,162,2),(1430,164,2),(1430,161,2),(1430,163,2),(1430,165,2),(1440,174,2),(1440,171,2),(1440,172,2),(1440,173,2),(1440,180,2),(1440,185,2),(1450,248,2),(1450,249,2),(1450,259,2),(1400,150,2),(1400,151,2),(1400,155,2),(1400,170,2),(1400,160,2),(1400,162,2),(1400,164,2),(1400,161,2),(1400,163,2),(1400,165,2),(1400,174,2),(1400,171,2),(1400,172,2),(1400,173,2),(1400,180,2),(1400,185,2),(1400,248,2),(1400,249,2),(1400,259,0),(1510,500,2),(1510,501,2),(1510,505,2),(1510,506,2),(1520,520,2),(1520,526,2),(1530,402,2),(1530,510,2),(1530,512,2),(1530,514,2),(1530,516,2),(1530,551,3),(1530,403,2),(1530,511,2),(1530,513,2),(1530,515,2),(1530,517,2),(1530,552,3),(1540,437,2),(1540,400,2),(1540,406,2),(1540,410,2),(1540,419,2),(1540,401,2),(1540,411,2),(1550,475,2),(1550,476,2),(1550,477,2),(1550,479,2),(1550,524,2),(1550,509,2),(1550,521,2),(1550,523,2),(1550,525,2),(1550,527,2),(1550,553,3),(1550,555,2),(1550,556,2),(1550,465,2),(1550,560,2),(1550,561,2),(1500,500,2),(1500,501,2),(1500,505,2),(1500,506,2),(1500,520,2),(1500,526,2),(1500,402,2),(1500,510,2),(1500,512,2),(1500,514,2),(1500,516,2),(1500,551,3),(1500,403,2),(1500,511,2),(1500,513,2),(1500,515,2),(1500,517,2),(1500,552,3),(1500,437,2),(1500,400,2),(1500,406,2),(1500,410,2),(1500,419,2),(1500,401,2),(1500,411,2),(1500,475,2),(1500,476,2),(1500,477,2),(1500,479,2),(1500,524,2),(1500,509,2),(1500,521,2),(1500,523,2),(1500,525,2),(1500,527,2),(1500,553,3),(1500,555,2),(1500,556,2),(1500,465,2),(1500,560,2),(1500,561,2),(1500,499,2),(1500,485,2),(1500,585,2),(2020,600,0),(2020,6080,0),(2020,6090,0),(2020,610,0),(2020,601,0),(2020,602,0),(2020,6081,0),(2020,6082,0),(2020,6091,0),(2020,6092,0),(2020,611,0),(2020,612,0),(2020,607,0),(2030,640,0),(2030,641,0),(2030,642,0),(2030,643,0),(2030,649,0),(2050,693,0),(2050,793,0),(2050,650,0),(2050,694,0),(2050,794,0),(2050,695,0),(2050,795,0),(2060,62,0),(2060,631,0),(2060,634,0),(2060,636,0),(2060,639,0),(2060,651,0),(2060,659,0),(2060,690,0),(2070,6610,0),(2070,6615,0),(2070,6620,0),(2070,6630,0),(2070,6640,0),(2070,6650,0),(2070,6611,0),(2070,6616,0),(2070,6621,0),(2070,6631,0),(2070,6641,0),(2070,6651,0),(2070,6613,0),(2070,6618,0),(2070,6622,0),(2070,6623,0),(2070,6632,0),(2070,6633,0),(2070,6642,0),(2070,6653,0),(2070,669,0),(2070,666,0),(2070,667,0),(2000,71,1),(2000,600,0),(2000,6080,0),(2000,6090,0),(2000,610,0),(2000,601,0),(2000,602,0),(2000,6081,0),(2000,6082,0),(2000,6091,0),(2000,6092,0),(2000,611,0),(2000,612,0),(2000,607,0),(2000,640,0),(2000,641,0),(2000,642,0),(2000,643,0),(2000,649,0),(2000,68,0),(2000,693,0),(2000,793,0),(2000,650,0),(2000,694,0),(2000,794,0),(2000,695,0),(2000,795,0),(2000,62,0),(2000,631,0),(2000,634,0),(2000,636,0),(2000,639,0),(2000,651,0),(2000,659,0),(2000,690,0),(2000,6610,0),(2000,6615,0),(2000,6620,0),(2000,6630,0),(2000,6640,0),(2000,6650,0),(2000,6611,0),(2000,6616,0),(2000,6621,0),(2000,6631,0),(2000,6641,0),(2000,6651,0),(2000,6613,0),(2000,6618,0),(2000,6622,0),(2000,6623,0),(2000,6632,0),(2000,6633,0),(2000,6642,0),(2000,6653,0),(2000,669,0),(2000,666,0),(2000,667,0),(2000,6963,0),(2000,6965,0),(2000,6966,0),(2000,697,0),(2000,698,0),(2000,699,0),(2000,7963,0),(2000,7965,0),(2000,7966,0),(2000,797,0),(2000,798,0),(2000,799,0),(2000,668,0),(2000,691,0),(2000,692,0),(2000,6960,0),(2000,6961,0),(2000,791,0),(2000,792,0),(2000,7960,0),(2000,7961,0),(2000,670,0),(2000,671,0),(2000,672,0),(2000,673,0),(2000,674,0),(2000,678,0),(2000,679,0),(2000,630,0),(2000,633,0),(2000,638,0),(3010,700,2),(3010,701,2),(3010,702,2),(3010,703,2),(3010,704,2),(3010,705,2),(3010,708,2),(3010,709,2),(3040,75,2),(3040,74,2),(3040,790,2),(3050,7600,2),(3050,7601,2),(3050,7603,2),(3060,7610,2),(3060,7620,2),(3060,7611,2),(3060,7621,2),(3060,7613,2),(3060,7623,2),(3070,7630,2),(3070,7650,2),(3070,7631,2),(3070,7651,2),(3070,7633,2),(3070,7653,2),(3070,769,2),(3070,766,2),(3000,700,2),(3000,701,2),(3000,702,2),(3000,703,2),(3000,704,2),(3000,705,2),(3000,708,2),(3000,709,2),(3000,71,3),(3000,73,2),(3000,75,2),(3000,74,2),(3000,790,2),(3000,7600,2),(3000,7601,2),(3000,7603,2),(3000,7610,2),(3000,7620,2),(3000,7611,2),(3000,7621,2),(3000,7613,2),(3000,7623,2),(3000,7630,2),(3000,7650,2),(3000,7631,2),(3000,7651,2),(3000,7633,2),(3000,7653,2),(3000,769,2),(3000,766,2),(3000,768,2),(3000,770,2),(3000,771,2),(3000,772,2),(3000,774,2),(3000,775,2),(3000,778,2),(3000,779,2),(224,217,0),(4000,60,0),(4000,61,0),(4000,71,0),(4000,640,0),(4000,641,0),(4000,642,0),(4000,643,0),(4000,649,0),(4000,68,0),(4000,650,0),(4000,693,0),(4000,694,0),(4000,695,0),(4000,793,0),(4000,794,0),(4000,795,0),(4000,62,0),(4000,631,0),(4000,634,0),(4000,636,0),(4000,639,0),(4000,651,0),(4000,659,0),(4000,690,0),(4000,6610,0),(4000,6615,0),(4000,6620,0),(4000,6630,0),(4000,6640,0),(4000,6650,0),(4000,6611,0),(4000,6616,0),(4000,6621,0),(4000,6631,0),(4000,6641,0),(4000,6651,0),(4000,6613,0),(4000,6618,0),(4000,6622,0),(4000,6623,0),(4000,6632,0),(4000,6633,0),(4000,6642,0),(4000,6653,0),(4000,669,0),(4000,666,0),(4000,667,0),(4000,6963,0),(4000,6965,0),(4000,6966,0),(4000,697,0),(4000,698,0),(4000,699,0),(4000,7963,0),(4000,7965,0),(4000,7966,0),(4000,797,0),(4000,798,0),(4000,799,0),(4000,668,0),(4000,691,0),(4000,692,0),(4000,6960,0),(4000,6961,0),(4000,791,0),(4000,792,0),(4000,7960,0),(4000,7961,0),(4000,670,0),(4000,671,0),(4000,672,0),(4000,673,0),(4000,674,0),(4000,678,0),(4000,679,0),(4000,630,0),(4000,633,0),(4000,638,0),(4010,60,0),(4010,61,0),(4010,71,0),(4020,640,0),(4020,641,0),(4020,642,0),(4020,643,0),(4020,649,0),(4021,640,0),(4021,641,0),(4022,642,0),(4022,643,0),(4022,649,0),(4030,68,0),(4040,650,0),(4040,693,0),(4040,694,0),(4040,695,0),(4040,793,0),(4040,794,0),(4040,795,0),(4050,62,0),(4050,631,0),(4050,634,0),(4050,636,0),(4050,639,0),(4050,651,0),(4050,659,0),(4050,690,0),(4060,6610,0),(4060,6615,0),(4060,6620,0),(4060,6630,0),(4060,6640,0),(4060,6650,0),(4060,6611,0),(4060,6616,0),(4060,6621,0),(4060,6631,0),(4060,6641,0),(4060,6651,0),(4060,6613,0),(4060,6618,0),(4060,6622,0),(4060,6623,0),(4060,6632,0),(4060,6633,0),(4060,6642,0),(4060,6653,0),(4060,669,0),(4060,666,0),(4060,667,0),(4061,6610,0),(4061,6615,0),(4061,6620,0),(4061,6630,0),(4061,6640,0),(4061,6650,0),(4062,6611,0),(4062,6616,0),(4062,6621,0),(4062,6631,0),(4062,6641,0),(4062,6651,0),(4063,6613,0),(4064,6618,0),(4063,6622,0),(4063,6623,0),(4063,6632,0),(4063,6633,0),(4063,6642,0),(4063,6653,0),(4063,669,0),(4064,666,0),(4064,667,0),(4070,6963,0),(4070,6965,0),(4070,6966,0),(4070,697,0),(4070,698,0),(4070,699,0),(4070,7963,0),(4070,7965,0),(4070,7966,0),(4070,797,0),(4070,798,0),(4070,799,0),(4080,668,0),(4090,691,0),(4090,692,0),(4090,6960,0),(4090,6961,0),(4090,791,0),(4090,792,0),(4090,7960,0),(4090,7961,0),(4100,670,0),(4100,671,0),(4100,672,0),(4100,673,0),(4110,674,0),(4120,678,0),(4130,679,0),(4140,630,0),(4140,633,0),(4140,638,0),(5000,70,2),(5000,73,2),(5000,74,2),(5000,75,2),(5000,790,2),(5000,7600,2),(5000,7610,2),(5000,7620,2),(5000,7630,2),(5000,7650,2),(5000,7601,2),(5000,7611,2),(5000,7621,2),(5000,7631,2),(5000,7651,2),(5000,7603,2),(5000,7613,2),(5000,7623,2),(5000,7633,2),(5000,7653,2),(5000,769,2),(5000,766,2),(5000,768,2),(5000,770,2),(5000,771,2),(5000,772,2),(5000,774,2),(5000,775,2),(5000,778,2),(5000,779,2),(5010,70,2),(5010,73,2),(5010,74,2),(5010,75,2),(5010,790,2),(5020,7600,2),(5020,7610,2),(5020,7620,2),(5020,7630,2),(5020,7650,2),(5020,7601,2),(5020,7611,2),(5020,7621,2),(5020,7631,2),(5020,7651,2),(5020,7603,2),(5020,7613,2),(5020,7623,2),(5020,7633,2),(5020,7653,2),(5020,769,2),(5020,766,2),(5030,768,2),(5040,770,2),(5040,771,2),(5040,772,2),(5050,774,2),(5060,775,2),(5070,778,2),(5080,779,2),(5011,70,2),(5012,73,2),(5012,74,2),(5012,75,2),(5012,790,2),(5021,7600,2),(5021,7610,2),(5021,7620,2),(5021,7630,2),(5021,7650,2),(5022,7601,2),(5022,7611,2),(5022,7621,2),(5022,7631,2),(5022,7651,2),(5023,7603,2),(5023,7613,2),(5023,7623,2),(5023,7633,2),(5023,7653,2),(5023,769,2),(5024,766,2),(220,217,0),(200,217,0),(436,473,0),(434,550,1),(1553,550,3),(400,550,1),(1500,550,3),(1550,550,3),(430,550,1),(430,473,0),(400,473,0),(100,190,0),(100,191,0),(100,192,0),(100,193,0),(100,194,0),(100,195,0),(210,20,0),(221,210,0),(222,211,0),(222,212,0),(223,213,0),(224,214,0),(225,215,0),(226,219,0),(227,291,0),(228,281,0),(231,220,0),(231,221,0),(232,222,0),(232,223,0),(233,224,0),(233,225,0),(233,226,0),(234,23,0),(235,227,0),(235,228,0),(235,229,0),(236,292,0),(237,282,0),(241,240,0),(242,242,0),(242,244,0),(243,241,0),(244,243,0),(244,245,0),(244,247,0),(245,250,0),(245,251,0),(245,256,0),(246,252,0),(246,253,0),(246,254,0),(246,257,0),(246,258,0),(247,260,0),(247,265,0),(248,293,0),(248,294,0),(248,295,0),(248,296,0),(248,297,0),(248,298,0),(250,198,0),(300,27,0),(410,558,0),(421,30,0),(422,31,0),(422,32,0),(423,33,0),(423,34,0),(424,35,0),(425,36,0),(426,407,0),(427,39,0),(431,430,0),(431,431,0),(431,435,0),(431,436,0),(432,432,0),(432,551,1),(433,433,0),(433,552,1),(434,44,0),(434,553,1),(435,460,0),(435,544,0),(436,470,0),(436,471,0),(436,472,0),(436,474,0),(437,490,0),(437,493,0),(437,494,0),(441,530,0),(441,538,0),(442,532,0),(442,534,0),(442,536,0),(443,531,0),(443,539,0),(444,533,0),(444,535,0),(444,537,0),(445,540,0),(445,541,0),(445,546,0),(445,549,0),(446,542,0),(446,543,0),(446,545,0),(446,547,0),(446,548,0),(447,565,0),(447,566,0),(448,593,0),(448,594,0),(448,595,0),(448,596,0),(448,597,0),(448,598,0),(460,57,0),(470,480,0),(470,580,0),(1110,10,2),(1120,110,2),(1130,111,2),(1141,112,2),(1142,115,2),(1143,114,2),(1144,116,2),(1145,113,2),(1145,117,2),(1145,118,2),(1151,120,2),(1152,121,2),(1153,122,2),(420,30,0),(1160,129,2),(1170,557,2),(1201,130,2),(1201,131,2),(1202,136,2),(1203,135,2),(1301,140,2),(1302,141,2),(1303,142,2),(1303,143,2),(1304,144,2),(1411,150,2),(1412,151,2),(1413,155,2),(1420,170,2),(1431,160,2),(1431,162,2),(1431,164,2),(1432,161,2),(1432,163,2),(1432,165,2),(1441,174,2),(1442,171,2),(1442,172,2),(1442,173,2),(1443,180,2),(1443,185,2),(1451,248,2),(1452,249,2),(1453,259,2),(1511,500,2),(1512,501,2),(1513,505,2),(1514,506,2),(1521,520,2),(1522,526,2),(1531,402,2),(1531,510,2),(1531,512,2),(1531,514,2),(1531,516,2),(1531,551,3),(1532,403,2),(1532,511,2),(1532,513,2),(1532,515,2),(1532,517,2),(1532,552,3),(1541,437,2),(1542,400,2),(1542,406,2),(1542,410,2),(1542,419,2),(1543,401,2),(1543,411,2),(1551,475,2),(1551,476,2),(1551,477,2),(1551,479,2),(1552,524,2),(1553,509,2),(1553,521,2),(1553,523,2),(1553,525,2),(1553,527,2),(1553,553,3),(1553,555,2),(1553,556,2),(1554,465,2),(1555,560,2),(1555,561,2),(1560,499,2),(1570,485,2),(1570,585,2),(2010,71,1),(2021,600,0),(2021,6080,0),(2021,6090,0),(2021,610,0),(2022,601,0),(2022,602,0),(2022,6081,0),(2022,6082,0),(2022,6091,0),(2022,6092,0),(2022,611,0),(2022,612,0),(2023,607,0),(2031,640,0),(2031,641,0),(2032,642,0),(2032,643,0),(2032,649,0),(2040,68,0),(2051,693,0),(2051,793,0),(2052,650,0),(2052,694,0),(2052,794,0),(2053,695,0),(2053,795,0),(2061,62,0),(2062,631,0),(2062,634,0),(2062,636,0),(2062,639,0),(2063,651,0),(2063,659,0),(2064,690,0),(2071,6610,0),(2071,6615,0),(2071,6620,0),(2071,6630,0),(2071,6640,0),(2071,6650,0),(2072,6611,0),(2072,6616,0),(2072,6621,0),(2072,6631,0),(2072,6641,0),(2072,6651,0),(2073,6613,0),(2073,6618,0),(2073,6622,0),(2073,6623,0),(2073,6632,0),(2073,6633,0),(2073,6642,0),(2073,6653,0),(2073,669,0),(2074,666,0),(2074,667,0),(2080,6963,0),(2080,6965,0),(2080,6966,0),(2080,697,0),(2080,698,0),(2080,699,0),(2080,7963,0),(2080,7965,0),(2080,7966,0),(2080,797,0),(2080,798,0),(2080,799,0),(2090,668,0),(2100,691,0),(2100,692,0),(2100,6960,0),(2100,6961,0),(2100,791,0),(2100,792,0),(2100,7960,0),(2100,7961,0),(2110,670,0),(2110,671,0),(2110,672,0),(2110,673,0),(2120,674,0),(2130,678,0),(2140,679,0),(2150,630,0),(2150,633,0),(2150,638,0),(3011,700,2),(3011,701,2),(3011,702,2),(3011,703,2),(3011,704,2),(3012,705,2),(3013,708,2),(3013,709,2),(3020,71,3),(3030,73,2),(3041,75,2),(3042,74,2),(3043,790,2),(3051,7600,2),(3052,7601,2),(3053,7603,2),(3061,7610,2),(3061,7620,2),(3062,7611,2),(3062,7621,2),(3063,7613,2),(3063,7623,2),(3071,7630,2),(3071,7650,2),(3072,7631,2),(3072,7651,2),(3073,7633,2),(3073,7653,2),(3073,769,2),(3074,766,2),(3080,768,2),(3090,770,2),(3090,771,2),(3090,772,2),(3100,774,2),(3110,775,2),(3120,778,2),(3130,779,2),(100,196,0),(220,210,0),(220,211,0),(220,212,0),(220,213,0),(220,214,0),(220,215,0),(220,219,0),(220,291,0),(220,281,0),(200,20,0),(200,210,0),(200,211,0),(200,212,0),(200,213,0),(200,214,0),(200,215,0),(200,219,0),(200,291,0),(200,281,0),(230,220,0),(230,221,0),(230,222,0),(230,223,0),(230,224,0),(230,225,0),(230,226,0),(230,23,0),(230,227,0),(230,228,0),(230,229,0),(230,292,0),(230,282,0),(200,220,0),(200,221,0),(200,222,0),(200,223,0),(200,224,0),(200,225,0),(200,226,0),(200,23,0),(200,227,0),(200,228,0),(200,229,0),(200,292,0),(200,282,0),(242,246,0),(240,240,0),(240,242,0),(240,244,0),(240,246,0),(240,241,0),(240,243,0),(240,245,0),(240,247,0),(240,250,0),(240,251,0),(240,256,0),(240,252,0),(240,253,0),(240,254,0),(240,257,0),(240,258,0),(240,260,0),(240,265,0),(240,293,0),(240,294,0),(240,295,0),(240,296,0),(240,297,0),(240,298,0),(200,240,0),(200,242,0),(200,244,0),(200,246,0),(200,241,0),(200,243,0),(200,245,0),(200,247,0),(200,250,0),(200,251,0),(200,256,0),(200,252,0),(200,253,0),(200,254,0),(200,257,0),(200,258,0),(200,260,0),(200,265,0),(200,293,0),(200,294,0),(200,295,0),(200,296,0),(200,297,0),(200,298,0),(200,198,0),(420,31,0),(420,32,0),(420,33,0),(420,34,0),(420,35,0),(420,36,0),(420,407,0),(420,39,0),(400,558,0),(400,30,0),(400,31,0),(400,32,0),(400,33,0),(400,34,0),(400,35,0),(400,36,0),(400,407,0),(400,39,0),(430,430,0),(430,431,0),(430,435,0),(430,436,0),(430,432,0),(430,551,1),(430,433,0),(430,552,1),(430,44,0),(430,553,1),(430,460,0),(430,544,0),(430,470,0),(430,471,0),(430,472,0),(430,474,0),(430,490,0),(430,493,0),(430,494,0),(400,430,0),(400,431,0),(400,435,0),(400,436,0),(400,432,0),(400,551,1),(400,433,0),(400,552,1),(400,44,0),(400,553,1),(400,460,0),(400,544,0),(400,470,0),(400,471,0),(400,472,0),(400,474,0),(400,490,0),(400,493,0),(400,494,0),(440,530,0),(440,538,0),(440,532,0),(440,534,0),(440,536,0),(440,531,0),(440,539,0),(440,533,0),(440,535,0),(440,537,0),(440,540,0),(440,541,0),(440,546,0),(440,549,0),(440,542,0),(440,543,0),(440,545,0),(440,547,0),(440,548,0),(440,565,0),(440,566,0),(440,593,0),(440,594,0),(440,595,0),(440,596,0),(440,597,0),(440,598,0),(400,530,0),(400,538,0),(400,532,0),(400,534,0),(400,536,0),(400,531,0),(400,539,0),(400,533,0),(400,535,0),(400,537,0),(400,540,0),(400,541,0),(400,546,0),(400,549,0),(400,542,0),(400,543,0),(400,545,0),(400,547,0),(400,548,0),(400,565,0),(400,566,0),(400,593,0),(400,594,0),(400,595,0),(400,596,0),(400,597,0),(400,598,0),(400,57,0),(400,480,0),(400,580,0),(1140,112,2),(1140,115,2),(1140,114,2),(1140,116,2),(1140,113,2),(1140,117,2),(1140,118,2),(1150,120,2),(1150,121,2),(1150,122,2),(1100,10,2),(1100,110,2),(1100,111,2),(1100,112,2),(1100,115,2),(1100,114,2),(1100,116,2),(1100,113,2),(1100,117,2),(1100,118,2),(1100,120,2),(1100,121,2),(1100,122,2),(1100,129,2),(1100,557,2),(1200,130,2),(1200,131,2),(1200,136,2),(1200,135,2),(1300,140,2),(1300,141,2),(1300,142,2),(1300,143,2),(1300,144,2),(1410,150,2),(1410,151,2),(1410,155,2),(1430,160,2),(1430,162,2),(1430,164,2),(1430,161,2),(1430,163,2),(1430,165,2),(1440,174,2),(1440,171,2),(1440,172,2),(1440,173,2),(1440,180,2),(1440,185,2),(1450,248,2),(1450,249,2),(1450,259,2),(1400,150,2),(1400,151,2),(1400,155,2),(1400,170,2),(1400,160,2),(1400,162,2),(1400,164,2),(1400,161,2),(1400,163,2),(1400,165,2),(1400,174,2),(1400,171,2),(1400,172,2),(1400,173,2),(1400,180,2),(1400,185,2),(1400,248,2),(1400,249,2),(1400,259,0),(1510,500,2),(1510,501,2),(1510,505,2),(1510,506,2),(1520,520,2),(1520,526,2),(1530,402,2),(1530,510,2),(1530,512,2),(1530,514,2),(1530,516,2),(1530,551,3),(1530,403,2),(1530,511,2),(1530,513,2),(1530,515,2),(1530,517,2),(1530,552,3),(1540,437,2),(1540,400,2),(1540,406,2),(1540,410,2),(1540,419,2),(1540,401,2),(1540,411,2),(1550,475,2),(1550,476,2),(1550,477,2),(1550,479,2),(1550,524,2),(1550,509,2),(1550,521,2),(1550,523,2),(1550,525,2),(1550,527,2),(1550,553,3),(1550,555,2),(1550,556,2),(1550,465,2),(1550,560,2),(1550,561,2),(1500,500,2),(1500,501,2),(1500,505,2),(1500,506,2),(1500,520,2),(1500,526,2),(1500,402,2),(1500,510,2),(1500,512,2),(1500,514,2),(1500,516,2),(1500,551,3),(1500,403,2),(1500,511,2),(1500,513,2),(1500,515,2),(1500,517,2),(1500,552,3),(1500,437,2),(1500,400,2),(1500,406,2),(1500,410,2),(1500,419,2),(1500,401,2),(1500,411,2),(1500,475,2),(1500,476,2),(1500,477,2),(1500,479,2),(1500,524,2),(1500,509,2),(1500,521,2),(1500,523,2),(1500,525,2),(1500,527,2),(1500,553,3),(1500,555,2),(1500,556,2),(1500,465,2),(1500,560,2),(1500,561,2),(1500,499,2),(1500,485,2),(1500,585,2),(2020,600,0),(2020,6080,0),(2020,6090,0),(2020,610,0),(2020,601,0),(2020,602,0),(2020,6081,0),(2020,6082,0),(2020,6091,0),(2020,6092,0),(2020,611,0),(2020,612,0),(2020,607,0),(2030,640,0),(2030,641,0),(2030,642,0),(2030,643,0),(2030,649,0),(2050,693,0),(2050,793,0),(2050,650,0),(2050,694,0),(2050,794,0),(2050,695,0),(2050,795,0),(2060,62,0),(2060,631,0),(2060,634,0),(2060,636,0),(2060,639,0),(2060,651,0),(2060,659,0),(2060,690,0),(2070,6610,0),(2070,6615,0),(2070,6620,0),(2070,6630,0),(2070,6640,0),(2070,6650,0),(2070,6611,0),(2070,6616,0),(2070,6621,0),(2070,6631,0),(2070,6641,0),(2070,6651,0),(2070,6613,0),(2070,6618,0),(2070,6622,0),(2070,6623,0),(2070,6632,0),(2070,6633,0),(2070,6642,0),(2070,6653,0),(2070,669,0),(2070,666,0),(2070,667,0),(2000,71,1),(2000,600,0),(2000,6080,0),(2000,6090,0),(2000,610,0),(2000,601,0),(2000,602,0),(2000,6081,0),(2000,6082,0),(2000,6091,0),(2000,6092,0),(2000,611,0),(2000,612,0),(2000,607,0),(2000,640,0),(2000,641,0),(2000,642,0),(2000,643,0),(2000,649,0),(2000,68,0),(2000,693,0),(2000,793,0),(2000,650,0),(2000,694,0),(2000,794,0),(2000,695,0),(2000,795,0),(2000,62,0),(2000,631,0),(2000,634,0),(2000,636,0),(2000,639,0),(2000,651,0),(2000,659,0),(2000,690,0),(2000,6610,0),(2000,6615,0),(2000,6620,0),(2000,6630,0),(2000,6640,0),(2000,6650,0),(2000,6611,0),(2000,6616,0),(2000,6621,0),(2000,6631,0),(2000,6641,0),(2000,6651,0),(2000,6613,0),(2000,6618,0),(2000,6622,0),(2000,6623,0),(2000,6632,0),(2000,6633,0),(2000,6642,0),(2000,6653,0),(2000,669,0),(2000,666,0),(2000,667,0),(2000,6963,0),(2000,6965,0),(2000,6966,0),(2000,697,0),(2000,698,0),(2000,699,0),(2000,7963,0),(2000,7965,0),(2000,7966,0),(2000,797,0),(2000,798,0),(2000,799,0),(2000,668,0),(2000,691,0),(2000,692,0),(2000,6960,0),(2000,6961,0),(2000,791,0),(2000,792,0),(2000,7960,0),(2000,7961,0),(2000,670,0),(2000,671,0),(2000,672,0),(2000,673,0),(2000,674,0),(2000,678,0),(2000,679,0),(2000,630,0),(2000,633,0),(2000,638,0),(3010,700,2),(3010,701,2),(3010,702,2),(3010,703,2),(3010,704,2),(3010,705,2),(3010,708,2),(3010,709,2),(3040,75,2),(3040,74,2),(3040,790,2),(3050,7600,2),(3050,7601,2),(3050,7603,2),(3060,7610,2),(3060,7620,2),(3060,7611,2),(3060,7621,2),(3060,7613,2),(3060,7623,2),(3070,7630,2),(3070,7650,2),(3070,7631,2),(3070,7651,2),(3070,7633,2),(3070,7653,2),(3070,769,2),(3070,766,2),(3000,700,2),(3000,701,2),(3000,702,2),(3000,703,2),(3000,704,2),(3000,705,2),(3000,708,2),(3000,709,2),(3000,71,3),(3000,73,2),(3000,75,2),(3000,74,2),(3000,790,2),(3000,7600,2),(3000,7601,2),(3000,7603,2),(3000,7610,2),(3000,7620,2),(3000,7611,2),(3000,7621,2),(3000,7613,2),(3000,7623,2),(3000,7630,2),(3000,7650,2),(3000,7631,2),(3000,7651,2),(3000,7633,2),(3000,7653,2),(3000,769,2),(3000,766,2),(3000,768,2),(3000,770,2),(3000,771,2),(3000,772,2),(3000,774,2),(3000,775,2),(3000,778,2),(3000,779,2),(224,217,0),(4000,60,0),(4000,61,0),(4000,71,0),(4000,640,0),(4000,641,0),(4000,642,0),(4000,643,0),(4000,649,0),(4000,68,0),(4000,650,0),(4000,693,0),(4000,694,0),(4000,695,0),(4000,793,0),(4000,794,0),(4000,795,0),(4000,62,0),(4000,631,0),(4000,634,0),(4000,636,0),(4000,639,0),(4000,651,0),(4000,659,0),(4000,690,0),(4000,6610,0),(4000,6615,0),(4000,6620,0),(4000,6630,0),(4000,6640,0),(4000,6650,0),(4000,6611,0),(4000,6616,0),(4000,6621,0),(4000,6631,0),(4000,6641,0),(4000,6651,0),(4000,6613,0),(4000,6618,0),(4000,6622,0),(4000,6623,0),(4000,6632,0),(4000,6633,0),(4000,6642,0),(4000,6653,0),(4000,669,0),(4000,666,0),(4000,667,0),(4000,6963,0),(4000,6965,0),(4000,6966,0),(4000,697,0),(4000,698,0),(4000,699,0),(4000,7963,0),(4000,7965,0),(4000,7966,0),(4000,797,0),(4000,798,0),(4000,799,0),(4000,668,0),(4000,691,0),(4000,692,0),(4000,6960,0),(4000,6961,0),(4000,791,0),(4000,792,0),(4000,7960,0),(4000,7961,0),(4000,670,0),(4000,671,0),(4000,672,0),(4000,673,0),(4000,674,0),(4000,678,0),(4000,679,0),(4000,630,0),(4000,633,0),(4000,638,0),(4010,60,0),(4010,61,0),(4010,71,0),(4020,640,0),(4020,641,0),(4020,642,0),(4020,643,0),(4020,649,0),(4021,640,0),(4021,641,0),(4022,642,0),(4022,643,0),(4022,649,0),(4030,68,0),(4040,650,0),(4040,693,0),(4040,694,0),(4040,695,0),(4040,793,0),(4040,794,0),(4040,795,0),(4050,62,0),(4050,631,0),(4050,634,0),(4050,636,0),(4050,639,0),(4050,651,0),(4050,659,0),(4050,690,0),(4060,6610,0),(4060,6615,0),(4060,6620,0),(4060,6630,0),(4060,6640,0),(4060,6650,0),(4060,6611,0),(4060,6616,0),(4060,6621,0),(4060,6631,0),(4060,6641,0),(4060,6651,0),(4060,6613,0),(4060,6618,0),(4060,6622,0),(4060,6623,0),(4060,6632,0),(4060,6633,0),(4060,6642,0),(4060,6653,0),(4060,669,0),(4060,666,0),(4060,667,0),(4061,6610,0),(4061,6615,0),(4061,6620,0),(4061,6630,0),(4061,6640,0),(4061,6650,0),(4062,6611,0),(4062,6616,0),(4062,6621,0),(4062,6631,0),(4062,6641,0),(4062,6651,0),(4063,6613,0),(4064,6618,0),(4063,6622,0),(4063,6623,0),(4063,6632,0),(4063,6633,0),(4063,6642,0),(4063,6653,0),(4063,669,0),(4064,666,0),(4064,667,0),(4070,6963,0),(4070,6965,0),(4070,6966,0),(4070,697,0),(4070,698,0),(4070,699,0),(4070,7963,0),(4070,7965,0),(4070,7966,0),(4070,797,0),(4070,798,0),(4070,799,0),(4080,668,0),(4090,691,0),(4090,692,0),(4090,6960,0),(4090,6961,0),(4090,791,0),(4090,792,0),(4090,7960,0),(4090,7961,0),(4100,670,0),(4100,671,0),(4100,672,0),(4100,673,0),(4110,674,0),(4120,678,0),(4130,679,0),(4140,630,0),(4140,633,0),(4140,638,0),(5000,70,2),(5000,73,2),(5000,74,2),(5000,75,2),(5000,790,2),(5000,7600,2),(5000,7610,2),(5000,7620,2),(5000,7630,2),(5000,7650,2),(5000,7601,2),(5000,7611,2),(5000,7621,2),(5000,7631,2),(5000,7651,2),(5000,7603,2),(5000,7613,2),(5000,7623,2),(5000,7633,2),(5000,7653,2),(5000,769,2),(5000,766,2),(5000,768,2),(5000,770,2),(5000,771,2),(5000,772,2),(5000,774,2),(5000,775,2),(5000,778,2),(5000,779,2),(5010,70,2),(5010,73,2),(5010,74,2),(5010,75,2),(5010,790,2),(5020,7600,2),(5020,7610,2),(5020,7620,2),(5020,7630,2),(5020,7650,2),(5020,7601,2),(5020,7611,2),(5020,7621,2),(5020,7631,2),(5020,7651,2),(5020,7603,2),(5020,7613,2),(5020,7623,2),(5020,7633,2),(5020,7653,2),(5020,769,2),(5020,766,2),(5030,768,2),(5040,770,2),(5040,771,2),(5040,772,2),(5050,774,2),(5060,775,2),(5070,778,2),(5080,779,2),(5011,70,2),(5012,73,2),(5012,74,2),(5012,75,2),(5012,790,2),(5021,7600,2),(5021,7610,2),(5021,7620,2),(5021,7630,2),(5021,7650,2),(5022,7601,2),(5022,7611,2),(5022,7621,2),(5022,7631,2),(5022,7651,2),(5023,7603,2),(5023,7613,2),(5023,7623,2),(5023,7633,2),(5023,7653,2),(5023,769,2),(5024,766,2),(220,217,0),(200,217,0),(436,473,0),(434,550,1),(1553,550,3),(400,550,1),(1500,550,3),(1550,550,3),(430,550,1),(430,473,0),(400,473,0);
/*!40000 ALTER TABLE `balancescuentas` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bancos`
--

DROP TABLE IF EXISTS `bancos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bancos` (
  `CodigoBanco` int DEFAULT NULL,
  `Nombre` text COLLATE utf8mb4_unicode_ci,
  `Direccion` text COLLATE utf8mb4_unicode_ci,
  `Poblacion` text COLLATE utf8mb4_unicode_ci,
  `CP` text COLLATE utf8mb4_unicode_ci,
  `Telefono` text COLLATE utf8mb4_unicode_ci,
  `FAX` text COLLATE utf8mb4_unicode_ci,
  `Director` text COLLATE utf8mb4_unicode_ci,
  `Banco` text COLLATE utf8mb4_unicode_ci,
  `Oficina` text COLLATE utf8mb4_unicode_ci,
  `DC` text COLLATE utf8mb4_unicode_ci,
  `Cuenta` text COLLATE utf8mb4_unicode_ci,
  `CuentaContable` int DEFAULT NULL,
  `Provincia` text COLLATE utf8mb4_unicode_ci,
  `SufijoOrdenante` text COLLATE utf8mb4_unicode_ci,
  `SufijoPresentador` text COLLATE utf8mb4_unicode_ci,
  `CodigoCedente` text COLLATE utf8mb4_unicode_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bancos`
--

LOCK TABLES `bancos` WRITE;
/*!40000 ALTER TABLE `bancos` DISABLE KEYS */;
/*!40000 ALTER TABLE `bancos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cashcobros`
--

DROP TABLE IF EXISTS `cashcobros`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cashcobros` (
  `NumCobro` int NOT NULL,
  `CodigoCliente` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `FechaExpedicion` text COLLATE utf8mb4_unicode_ci,
  `FechaVencimiento` text COLLATE utf8mb4_unicode_ci,
  `FechaCobro` text COLLATE utf8mb4_unicode_ci,
  `Concepto` text COLLATE utf8mb4_unicode_ci,
  `Importe` double DEFAULT NULL,
  `Presentado` text COLLATE utf8mb4_unicode_ci,
  `Cobrado` text COLLATE utf8mb4_unicode_ci,
  `Impagado` text COLLATE utf8mb4_unicode_ci,
  `BancoCliente` text COLLATE utf8mb4_unicode_ci,
  `PoblacionBanco` text COLLATE utf8mb4_unicode_ci,
  `DireccionBanco` text COLLATE utf8mb4_unicode_ci,
  `Banco` text COLLATE utf8mb4_unicode_ci,
  `AgenciaBanco` text COLLATE utf8mb4_unicode_ci,
  `CuentaBanco` text COLLATE utf8mb4_unicode_ci,
  `DC` text COLLATE utf8mb4_unicode_ci,
  `Asiento` int DEFAULT NULL,
  `Factura` int DEFAULT NULL,
  `Devuelto` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`NumCobro`),
  KEY `idx_cashcobros_codigo_cliente` (`CodigoCliente`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cashcobros`
--

LOCK TABLES `cashcobros` WRITE;
/*!40000 ALTER TABLE `cashcobros` DISABLE KEYS */;
/*!40000 ALTER TABLE `cashcobros` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cashpagos`
--

DROP TABLE IF EXISTS `cashpagos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cashpagos` (
  `NumPago` int NOT NULL,
  `CodigoProveedor` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Vencimiento` text COLLATE utf8mb4_unicode_ci,
  `Concepto` text COLLATE utf8mb4_unicode_ci,
  `Importe` double DEFAULT NULL,
  `CodigoBanco` int DEFAULT NULL,
  `Pagado` text COLLATE utf8mb4_unicode_ci,
  `Asiento` int DEFAULT NULL,
  `Factura` int DEFAULT NULL,
  PRIMARY KEY (`NumPago`),
  KEY `idx_cashpagos_codigo_proveedor` (`CodigoProveedor`),
  CONSTRAINT `fk_cashpagos_proveedor` FOREIGN KEY (`CodigoProveedor`) REFERENCES `proveedores` (`Codigo`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cashpagos`
--

LOCK TABLES `cashpagos` WRITE;
/*!40000 ALTER TABLE `cashpagos` DISABLE KEYS */;
/*!40000 ALTER TABLE `cashpagos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cashremesas`
--

DROP TABLE IF EXISTS `cashremesas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cashremesas` (
  `NumRemesa` int NOT NULL,
  `Fecha_Ingreso` text COLLATE utf8mb4_unicode_ci,
  `Banco` int DEFAULT NULL,
  `NumCobros` int DEFAULT NULL,
  `ImporteTotal` double DEFAULT NULL,
  `NumAsiento` int DEFAULT NULL,
  `SubcuentaEfectos` int DEFAULT NULL,
  `Norma` int DEFAULT NULL,
  PRIMARY KEY (`NumRemesa`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cashremesas`
--

LOCK TABLES `cashremesas` WRITE;
/*!40000 ALTER TABLE `cashremesas` DISABLE KEYS */;
/*!40000 ALTER TABLE `cashremesas` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cashremesasdesglose`
--

DROP TABLE IF EXISTS `cashremesasdesglose`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cashremesasdesglose` (
  `NumRemesa` int DEFAULT NULL,
  `NumCobro` int DEFAULT NULL,
  `Linea` int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cashremesasdesglose`
--

LOCK TABLES `cashremesasdesglose` WRITE;
/*!40000 ALTER TABLE `cashremesasdesglose` DISABLE KEYS */;
/*!40000 ALTER TABLE `cashremesasdesglose` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `clientes`
--

DROP TABLE IF EXISTS `clientes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `clientes` (
  `Codigo` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `CodigoDeReferencia` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Nombre` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Domicilio` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Domicilio2` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Poblacion` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `CodigoPostal` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Provincia` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `PersonaContacto` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Telefono` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Fax` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `CIF` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `IVASN` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `REQSN` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `SubcuentaCliente` int DEFAULT NULL,
  `SubcuentaProveedor` int DEFAULT NULL,
  `TipoDescuento` int DEFAULT NULL,
  `Tarifa` int DEFAULT NULL,
  `SectoresActividad` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `FormaPago` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Agente` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Riesgo` decimal(15,2) DEFAULT NULL,
  `Banco` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `DireccionBanco` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `PoblacionBanco` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ProvinciaBanco` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `AgenciaBanco` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `CuentaBanco` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Notas` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`Codigo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `clientes`
--

LOCK TABLES `clientes` WRITE;
/*!40000 ALTER TABLE `clientes` DISABLE KEYS */;
INSERT INTO `clientes` VALUES ('0001','','**** FACTURA ANULADA ****','','','','','','','','','','1','0',0,0,0,0,'','','',0.00,'','','','','','',''),('0002','','CLIENTES CONTADO','','','','','','','','','','1','0',0,0,0,0,'','','',0.00,'','','','','','',''),('0101','','**baja**OCIO HOTELERO SANTA POLA S.L.','C/ HOLANDA 15','','SANTA POLA','03130','03','','966697897','','B-54747951','1','0',0,0,0,0,'','CONTADO','',0.00,'','','','','','',''),('0102','','**baja**ALFONSA TROYA FERNANDEZ','Ubanova - Local 5','  `LA RUEDA`','Alicante','','03','Jose','','','21448520P','1','0',0,0,0,0,'','','',0.00,'','','','','','',''),('0103','','**baja**José Luis Herrera Payán ','Pza. San Crispín 6','','TORRELLANO','03320','03','José Luis','965680098','','21999139F','1','0',0,0,0,0,'','CONTADO','',0.00,'','','','','','',''),('0104','','RODRIGUEZ CHARCUTEROS C.B.','C/ LA IGLESIA 8','','EL ALTET','03195','03','','','','E-53472304','1','0',0,0,0,0,'','CONTADO','',0.00,'','','','','','',''),('0105','','**baja**ROSA Mª NAVARO RIERA','C/ AN JUAN BOSCO 3, BAJO',' `El Pollo Frutero`','ALICANTE','03005','03','','','','21433729-Y','1','0',0,0,0,0,'','CONTADO','',0.00,'','','','','','',''),('0106','','**baja**DAVID CERDA MARTINEZ','C/ LA TORRE 95','','ELCHE','03204','03','','','','74239960-P','1','0',0,0,0,0,'','CONTADO','',0.00,'','','','','','',''),('0107','','**baja**MARGARITA MOLLA AGULLO ','C/ LIBERTAD 57','`El Chambao`','TORRELLANO','','03','','','','34485334-W','1','0',0,0,0,0,'','CONTADO','',0.00,'','','','','','',''),('0108','','CARMELO ESCOLANO MENDEZ','ALICANTE 2','','EL ALTET','','03','','','','21413415-R','1','0',0,0,0,0,'','CONTADO','',0.00,'','','','','','',''),('0109','','**baja**MARIA DEL CARMEN TORRES','C/ SAN BARTOLOME DE TIRAJANA 20','','AENALES DEL SOL','03195','03','','','','221151796D','1','0',0,0,0,0,'','','',0.00,'','','','','','',''),('0110','','**baja**VENTA VISTALEGRE S.C.','Ctra. Alicante-Cartagena N-332 Km. 88','','Santa Pola','','03','','','','J-54746581','1','0',0,0,0,0,'','','',0.00,'','','','','','',''),('0111','','**baja**CANTINA AEROPUERTO ALICANTE','C/ Jaime I s/n','','Monforte','03670','03','','','','B53415162','1','0',0,0,0,0,'','','',0.00,'','','','','','',''),('0112','','MECHEDA S.L. ','Av Dama de Elche 17','','El Altt','','03','','965689218','º','B-54854914','1','0',0,0,0,0,'','CONTADO','',0.00,'','','','','','',''),('0113','','**baja**Juan Fanisco Snchez Campos','Paseo Tomas Dura 17','Ubanova `Don Arroz`','Alicante','','03','','965188527','','50528635G','1','0',0,0,0,0,'','CONTADO','',0.00,'','','','','','',''),('0114','','**BAJA**LIVIA DANUTA MECHITA','AV. CARTAGENA','','EL ALTET','','03','','','','X3274500J','1','0',0,0,0,0,'','CONTADO','',0.00,'','','','','','',''),('0115','','**baja**NOEMI FERNANDEZ TORRES','AV. SAN BARTOLOM DE TIRAJANA 20','','ARENALES DEL SOL','03195','03','','','','44766621-L','1','0',0,0,0,0,'','CONTADO','',0.00,'','','','','','',''),('0116','','Mª Del Mar Bautista Pelaez - R. de Pili','Av. Elche 144','','Alicante','','03','','965113125','','52523521P','1','0',0,0,0,0,'','CONTADO','',0.00,'','','','','','',''),('0117','','**baja**BEATRIZ BERNÁRDEZ IZQUIERDO','Av. Dama de Elche 19',' `Botijo`','El Altet','03195','03','','','','47046881J','1','0',0,0,0,0,'','CONTADO','',0.00,'','','','','','',''),('0118','','**baja**ANTONIA GARCIA MARTINEZ','Rincon de Montemar','C/ Holanda 13','Gran Alacant - Santa Pola','03130','03','','','','22450750J','1','0',0,0,0,0,'','CONTADO','',0.00,'','','','','','',''),('0119','','**baja**ELOINA SELVAS FRIAS ','Av. Ciudad Deportiva 2','','El Altet','','03','','','','21414585','1','0',0,0,0,0,'','','',0.00,'','','','','','',''),('0120','','MARIA TERESA RODRIGUEZ BLASCO','AV. CIUDAD DEPORTIVA S/N','','EL ALTET','03195','03','','','','22004031T','1','0',0,0,0,0,'','CONTADO','',0.00,'','','','','','',''),('0121','','**baja**ELISABET MARTINEZ GARCIA','Rte. Vista Alegre','Ctra. Alicante-Cartagena Km. 17','Santa Pola','03130','03','','','','74006630J','1','0',0,0,0,0,'','CONTADO','',0.00,'','','','','','',''),('0122','','SANSEL C.V.','Av. Cartagena 48','','El Altet','03195','03','','','','F54984141','1','0',0,0,0,0,'','CONTADO','',0.00,'','','','','','',''),('0123','','RESTAURANTE VISTA ALEGRE SLU','Ptda. Valverde Bajo, 13','','SANTA POLA','03130','03','','','','B-42560375','1','0',0,0,0,0,'','','',0.00,'','','','','','',''),('0124','','**baja**PEDRO ASENCIO AZNAR','C/ ALICANTE s/n',' `Centro Social`','EL ALTET','03195','03','','','','21985860-E','1','0',0,0,0,0,'','','',0.00,'','','','','','',''),('0126','','**baja**KALIFRO, CB  ','CTRA. ELCHE-EL ALTET Km. 10','Pol. 1 Nº 48 Letra A   --- `BRASERIA LOS','El Altet','03195','03','Rogelio Juan','965 26 70 74','','E-42552000','1','0',0,0,0,0,'','','',0.00,'','','','','','',''),('0127','','**baja**BASILIO FERNANDEZ LATORRE','SAN BARTOLOME DE TIRAJANA 20, LOCAL 14','','ARENALES DEL SOL','','03','','','','02078739-E','1','0',0,0,0,0,'','','',0.00,'','','','','','',''),('0128','','**baja**ANTONIA GARCIA','HOLANDA 5','','GRAN ALACANT','03130','03','','','','22450750-J','1','0',0,0,0,0,'','','',0.00,'','','','','','',''),('0129','','VERONICA KUBICEK SILVEIRA `Nou Yoyos`','C/ Mar 25','','El Altet','03195','03','','','','X-4584270-W','1','0',0,0,0,0,'','','',0.00,'','','','','','',''),('0130','','**BAJA**BRABEL RESTAURACION SL','REST. NOU ARCOS','ISLAS CANARIAS 2','ARENALES DEL SOL ','','03','','','','B-54776844','1','0',0,0,0,0,'','','',0.00,'','','','','','',''),('0131','','CAPS  CUIDADORES S.L.','CEIP RODOLFO TOMAS i SAMPER - L´ALTET ','C/ SEQUIA DE MESTALLA, 2','PICANYA','46210','46','Mª Jose','963976520','','B-97320378','1','0',0,0,0,0,'','','',0.00,'','','','','','',''),('0132','','RESTAURANTE LA BODEGA S.L.','LIBERTAD 55','','TORRELLANO','','03','','965680395','','B54989827','1','0',0,0,0,0,'','CONTADO','',0.00,'','','','','','',''),('0133','','**baja**MONICA QUILACHAMIN - ','Urbabanova Local 5','`El Mar Restaurante','Alicante','','03','','','','51299831-X','1','0',0,0,0,0,'','CONTADO','',0.00,'','','','','','',''),('0134','','CARDASI S,A,','C/ Galileo Galilei, 2','','03203 ELCHE - Parque Industrial','','03','','','','A-53183901','1','0',0,0,0,0,'','CONTADO','',0.00,'','','','','','',''),('0135','','PIKOLINO´S INTERCONTINENTAL, S.A.','C/ Galileo Galilei, 2','Elche Parque Industrial','ELCHE','03203','03','Miguel Angel Justicia','966915150','','A-53238713','1','0',0,0,0,0,'','90DIAS','',0.00,'','','','','','',''),('0136','','PIKOSTORE, S.L.','C/ GALILEO GALILEI, 2','ELCHE PARQUE INDUSTRIAL','TORRELLANO','03203','03','','','','B-53906590','1','0',0,0,0,0,'','90DIAS','',0.00,'','','','','','',''),('0137','','MOLICOPI, S.L.','Galileo Galilei, 2','Elche Parque Industrial','ELCHE','03320','03','M.A. JUSTICIA - MARIOLA','965681234','','B-03875952','1','0',0,0,0,0,'','','',0.00,'','','','','','',''),('0138','','**baja*RESTAURANTE PRESTIGE SPORT S.L.U.','AV. CARTAGENA','','EL ALTET','','03','','','','B-42606889','1','0',0,0,0,0,'','CONTADO','',0.00,'','','','','','',''),('0139','','FUNDACION JUAN PERAN - PIKOLINOS','GALILEO GALILEI, 2','ELCHE PARQUE EMPRESARIAL','ELCHE','03203','03','','965681234','','G-54265301','1','0',0,0,0,0,'','','',0.00,'','','','','','',''),('0140','','**baja**ROSA MARIA MONTEVERDE FERNANDEZ','AV. DAMA DE ELCHE','','EL ALTET','03195','03','ROSA','','','21451754-E','1','0',0,0,0,0,'','','',0.00,'','','','','','',''),('0141','','SANTIAGO GUIJARRO JORNET, S.L.','`RINCON DE SANTI`','AVDA. San Bartolome de Tirajana, 45','ARENALES DEL SOL','03195','03','YOLANDA Y SANTI','','','B-42555953','1','0',0,0,0,0,'','','',0.00,'','','','','','',''),('0142','','ASOCIACION DE VECINOS AGUA AMARGA','C/. TORMOS, 6 1A','','ALICANTE','03008','03','PILI  615 097 192','','','G-53299319','1','0',0,0,0,0,'','CONTADO','',0.00,'','','','','','',''),('0143','','PASCUAL RUSO GIMENEZ `La Bodeguita`','Av. Mediterraneo 68, Local 1','','Santa Pola','03133','03','','','','74386240-P','1','0',0,0,0,0,'','','',0.00,'','','','','','',''),('0144','','**baja**PETRONILA FELIZ RUIZ ','Partida El Altet, Pol. 1º, nº 45','RTE. LOS OLIVOS','El Altet','03195','03','','','','74.386.240-P','1','0',0,0,0,0,'','','',0.00,'','','','','','',''),('0145','','**baja**JUANI HITA MESEGUER','Servido en Rte. `Maestral`','','ALICANTE','','03','','','','21500386-D','1','0',0,0,0,0,'','CONTADO','',0.00,'','','','','','','cliente particular - encargo -'),('0146','','**BAJA**PERLA ARENALES, S.L.','RESTAURANTE LA PERLA','SAN BARTOLOME DE TIRAJANA, 24','ARENALES DEL SOL','03195','03','ANTONIO Y PEDRO','','','B-01628957','1','0',0,0,0,0,'','','',0.00,'','','','','','','BAJA EN 2022  **DEJA A DEBER**'),('0147','','ALBA VIDAL GUILL - ICE & VICE','Paseo Tomás Durá, 5-6 Local 1','','URBANOVA - ALICANTE','03008','03','','','','2168970-H','1','0',0,0,0,0,'','CONTADO','',0.00,'','','','','','',''),('0148','','SERVICIOS INTEGRALES MRH, S.L.','Fragata Almansa, 6','','El Altet','03195','03','JOSE','','','B-04973079','1','0',0,0,0,0,'','','',0.00,'','','','','','',''),('0149','','PIZZERIA URBANOVA, S.L.','Paseo Tomás Durá, 15 Urbanova','**PROVENZAL**','ALICANTE','03008','03','SERGIO','','','B-72675549','1','0',0,0,0,0,'','','',0.00,'','','','','','','CLIENE NUEVO DESDE ENERO 2023'),('0150','','NICOLAS Y MORAGUES, ESPJ','LA TENDETA DE JUANI','PTDA. TORRELLANO BAJO, POL.1 NUM.25','TORRELLANO BAJO','03320','03','JUANFRAN Y JUANI - SEVE','622301065','','E-42733030','1','0',0,0,0,0,'','30DIAS','',0.00,'CAIXABANC','ALICANTE','ALICANTE','03','ES18','21008843220200032551',''),('0151','','ADA CILA RUSO ESPINOSA','GABRIEL MIRO, 67-1-4','La Tahona de Ada','SANTA POLA','03130','03','ADA','633684034','','20519123-H','1','0',0,0,0,1,'','','',0.00,'','','','','','',''),('0152','','JOSE M. MANCHON LLEDO','SAN BARTOLOME DE TIRAJANA, 67 BAJO','`RESTAURANTE ESTRELLA DE MAR`','LOS ARENALES DEL SOL','03195','03','JOSE-MERCE','966 911 331','','74230036-C','1','0',0,0,0,2,'','CONTADO','',0.00,'','','','','','',''),('0153','','BERNARDO PEREZ VALERA',' `BURGUER BERNA`','SAN BARTOLOME DE TIRAJANA, 11','ARENALES DEL SOL','03195','03','BERNA Y DANI','','','48374021-F','1','0',0,0,0,0,'','','',0.00,'','','','','','',''),('0154','','HECTOR FABIAN GONZALEZ','`CAMINITO TAPAS BAR`','Avda. Cartagena, 48','EL ALTET','03195','03','FABIAN','','','X-3579041-B','1','0',0,0,0,0,'','CONTADO','',0.00,'','','','','','','ANTES `SANSEL`, ROGELIO Y ELO'),('0200','','BEGOÑA PILAR BAEZA BONMATI','AV. SAN FRANCISCO DE ASIS 64','','EL ALTET','03195','03','','','','48350693-R','1','1',0,0,0,0,'','','',0.00,'','','','','','',''),('0201','','ARENALES PLAYA - DESPACHO PROPIO','SAN BARTOLOME DE TIRAJANA','','LOS ARENALES DEL SOL','03195','03','','','','F-54059985','1','0',0,0,0,0,'','','',0.00,'','','','','','',''),('0202','','TERRAMAR, S.A.','C/ ZARANDIETA 7, BAJO','','ALICANTE','03010','03','','','','A-03284296','1','0',0,0,0,0,'','','',0.00,'','','','','','',''),('0203','','LIMENCOP, S.L.','Avda. Salamanca, 27','','Alicante','03005','03','SANTOS','','','B-53212619','1','0',0,0,0,0,'','','',0.00,'','','','','','',''),('0207','','**baja**HIJOS DE FABRIZZIO C.B.','ETELLA 6',' `Pizz. Provenzal','ALICANTE','','03','','','','53152138-E','1','0',0,0,0,0,'','','',0.00,'','','','','','','****BAJA**** PASA A SER -----PIZZERIA PROVENZAL, S.L.'),('0211','','**baja**MARCO ESPIN S.L.','CONCEJAL LORENZO LLANERAS 9',' `Pollo Salsero`','ALICANTE','','03','','','','B-53540043','1','0',0,0,0,0,'','','',0.00,'','','','','','',''),('0217','','RAQUEL ORTOLA BONMATI','PASEO TOMAS DURA','','URBANOVA - ALICANTE','','03','','','','52765150-E','1','0',0,0,0,0,'','','',0.00,'','','','','','',''),('0218','','** BAJA**PIZZERIA PROVENZAL, S.L.','PASEO TOMAS DURÁ','URBANOVA','ALICANTE','','03','ALAN','','','B-02776797','1','0',0,0,0,0,'','','',0.00,'','','','','','','ALTA DESDE 2021 ------ANTES HIJOS DE FABRIZZIO'),('0222','','GRAN BAR POMARES S.L.','AV. CARTAGENA 17','','EL ALTET','03195','03','','','','B-54547492','1','0',0,0,0,0,'','','',0.00,'','','','','','',''),('0293','','IES GRAN ALACANT','MUNTANYA DE SANTA POLA, S/N','GRAN ALACANT','SANTA POLA','03130','03','','','','50300141-I','1','0',0,0,0,0,'','CONTADO','',0.00,'','','','','','','676243976-606515128'),('0294','','AMPA CEIP RODOLFO TOMAS SAMPER','AVDA. CIUDAD DEPORTIVA','','EL ALTET','03195','03','','','','G-03244464','1','0',0,0,0,0,'','CONTADO','',0.00,'','','','','','',''),('0295','','LOBO AGENCIA DIGITAL, S.L.','C/Travessia 15E La Marina Ed. BioHub','','','46024','46','','','','B-97845929','1','0',0,0,0,0,'','','',0.00,'','','','','','','CLIENTE TRABAJO BEGOÑA BAEZA'),('0296','','**BAJA**TALLER DE EDITORES, S.A. ','REVISTA MUJER HOY','C/.JOSEFA VALCARCEL, 40 BIS','MADRID','28027','28','BEGOÑA BAEZA BONMATI','','','A-78509130','1','0',0,0,0,0,'','','',0.00,'','','','','','','CLIENTE DE BEGOÑA BAEZA    DIRECCION DE ARTE'),('0297','','**baja**JAVIER JOSÉ SEMPERE GOMIS','Ptda. L´Altet Pol.1 Nº48','Rte. LOS OLIVOS','EL ALTET','03195','03','JAVI','966366910','','48365627-P','1','0',0,0,0,0,'','','',0.00,'','','','','','',''),('0299','','**baja**ANTONIO JESUS MARTINEZ','PLAZA MAYOR 12-13',' `Cafet. Montemar`','GRN ALACANT','03130','03','','','','74229758-H','1','0',0,0,0,0,'','','',0.00,'','','','','','',''),('0300','','FUNDACION NORAY `Proyecto Hombre`','PTDA. AGUA AMARGA S/N','','ALICANTE','03008','03','','','','G-53363131','1','0',0,0,0,0,'','','',0.00,'','','','','','','');
/*!40000 ALTER TABLE `clientes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `clientesdiapago`
--

DROP TABLE IF EXISTS `clientesdiapago`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `clientesdiapago` (
  `CodigoCliente` text COLLATE utf8mb4_unicode_ci,
  `DiaPago` int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `clientesdiapago`
--

LOCK TABLES `clientesdiapago` WRITE;
/*!40000 ALTER TABLE `clientesdiapago` DISABLE KEYS */;
/*!40000 ALTER TABLE `clientesdiapago` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cobrospagospendientes`
--

DROP TABLE IF EXISTS `cobrospagospendientes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cobrospagospendientes` (
  `Fecha` text COLLATE utf8mb4_unicode_ci,
  `Numero` int DEFAULT NULL,
  `Nombre` text COLLATE utf8mb4_unicode_ci,
  `Codigo` text COLLATE utf8mb4_unicode_ci,
  `Cobro` double DEFAULT NULL,
  `Pago` double DEFAULT NULL,
  `Saldo` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cobrospagospendientes`
--

LOCK TABLES `cobrospagospendientes` WRITE;
/*!40000 ALTER TABLE `cobrospagospendientes` DISABLE KEYS */;
/*!40000 ALTER TABLE `cobrospagospendientes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `composicion`
--

DROP TABLE IF EXISTS `composicion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `composicion` (
  `ArticuloCompuesto` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `Componente` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `Cantidad` double DEFAULT NULL,
  PRIMARY KEY (`ArticuloCompuesto`,`Componente`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `composicion`
--

LOCK TABLES `composicion` WRITE;
/*!40000 ALTER TABLE `composicion` DISABLE KEYS */;
/*!40000 ALTER TABLE `composicion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cuentas`
--

DROP TABLE IF EXISTS `cuentas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cuentas` (
  `CodigoCuenta` int NOT NULL,
  `Titulo` text COLLATE utf8mb4_unicode_ci,
  `SaldoDebe` double DEFAULT NULL,
  `SaldoHaber` double DEFAULT NULL,
  `SaldoDebe2` double DEFAULT NULL,
  `SaldoHaber2` double DEFAULT NULL,
  `Filtro0` int DEFAULT NULL,
  `Filtro9` int DEFAULT NULL,
  PRIMARY KEY (`CodigoCuenta`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cuentas`
--

LOCK TABLES `cuentas` WRITE;
/*!40000 ALTER TABLE `cuentas` DISABLE KEYS */;
INSERT INTO `cuentas` VALUES (10,'Capital',0,0,0,0,100000000,109999999),(11,'Reservas',0,0,0,0,110000000,119999999),(12,'Resultados pendientes de aplicación',0,0,0,0,120000000,129999999),(13,'Ingresos a distribuir en varios ejercicios',0,0,0,0,130000000,139999999),(14,'Provisiones para riesgos y gastos',0,0,0,0,140000000,149999999),(15,'Empréstitos y otras obligaciones análogas',0,0,0,0,150000000,159999999),(16,'Deudas largo plazo con empresas grupo y asociadas',0,0,0,0,160000000,169999999),(17,'Deudas largo plazo préstamos recibidos y otros',0,0,0,0,170000000,179999999),(18,'Fianzas y depósitos recibidos a largo plazo',0,0,0,0,180000000,189999999),(19,'Situaciones transitorias de financiación',0,0,0,0,190000000,199999999),(20,'Gastos de establecimiento',0,0,0,0,200000000,209999999),(21,'Inmovilizaciones inmateriales',0,0,0,0,210000000,219999999),(22,'Inmovilizaciones materiales',0,0,0,0,220000000,229999999),(23,'Inmovilizaciones materiales en curso',0,0,0,0,230000000,239999999),(24,'Inversiones financieras en empresas grupo y asoc.',0,0,0,0,240000000,249999999),(25,'Otras inversiones financieras permanentes',0,0,0,0,250000000,259999999),(26,'Fianzas y depósitos constituidos a largo plazo',0,0,0,0,260000000,269999999),(27,'Gastos a distribuir en varios ejercicios',0,0,0,0,270000000,279999999),(28,'Amortización acumulada del inmovilizado',0,0,0,0,280000000,289999999),(29,'Provisiones de inmovilizado',0,0,0,0,290000000,299999999),(30,'Comerciales',0,0,0,0,300000000,309999999),(31,'Materias primas',0,0,0,0,310000000,319999999),(32,'Otros aprovisionamientos',0,0,0,0,320000000,329999999),(33,'Productos en curso',0,0,0,0,330000000,339999999),(34,'Productos semiterminados',0,0,0,0,340000000,349999999),(35,'Productos terminados',0,0,0,0,350000000,359999999),(36,'Subproductos, residuos y materiales recuperados',0,0,0,0,360000000,369999999),(39,'Provisiones por depreciación de existencias',0,0,0,0,390000000,399999999),(40,'Proveedores',0,0,0,0,400000000,409999999),(41,'Acreedores varios',0,0,0,0,410000000,419999999),(43,'Clientes',0,0,0,0,430000000,439999999),(44,'Deudores varios',0,0,0,0,440000000,449999999),(46,'Personal',0,0,0,0,460000000,469999999),(47,'Administraciones Públicas',0,0,0,0,470000000,479999999),(48,'Ajustes por periodificación',0,0,0,0,480000000,489999999),(49,'Provisiones por operaciones de tráfico',0,0,0,0,490000000,499999999),(50,'Empréstitos y emisiones análogas a corto plazo',0,0,0,0,500000000,509999999),(51,'Deudas a corto plazo empresas grupo y asociadas',0,0,0,0,510000000,519999999),(52,'Deudas corto plazo préstamos recibidos',0,0,0,0,520000000,529999999),(53,'Inversiones financieras corto plazo emp. grupo',0,0,0,0,530000000,539999999),(54,'Otras inversiones financieras temporales',0,0,0,0,540000000,549999999),(55,'Otras cuentas no bancarias',0,0,0,0,550000000,559999999),(56,'Fianzas y depósitos a corto plazo',0,0,0,0,560000000,569999999),(57,'Tesorería',0,0,0,0,570000000,579999999),(58,'Ajustes por periodificación',0,0,0,0,580000000,589999999),(59,'Provisiones financieras',0,0,0,0,590000000,599999999),(60,'Compras',0,0,0,0,600000000,609999999),(61,'Variación de existencias',0,0,0,0,610000000,619999999),(62,'Servicios exteriores',0,0,0,0,620000000,629999999),(63,'Tributos',0,0,0,0,630000000,639999999),(64,'Gastos de personal',0,0,0,0,640000000,649999999),(65,'Otros gastos de gestión',0,0,0,0,650000000,659999999),(66,'Gastos financieros',0,0,0,0,660000000,669999999),(67,'Pérdidas procedentes del inmovilizado y gastos exc',0,0,0,0,670000000,679999999),(68,'Dotaciones para amortizaciones',0,0,0,0,680000000,689999999),(69,'Dotaciones a las provisiones',0,0,0,0,690000000,699999999),(70,'Venta mercaderías producción propia, servicios...',0,0,0,0,700000000,709999999),(71,'Variación de existencias',0,0,0,0,710000000,719999999),(73,'Trabajos realizados para la empresa',0,0,0,0,730000000,739999999),(74,'Subvenciones a la explotación',0,0,0,0,740000000,749999999),(75,'Otros ingresos de gestión',0,0,0,0,750000000,759999999),(76,'Ingresos financieros',0,0,0,0,760000000,769999999),(77,'Beneficios procedentes del inmovilizado e ingresos',0,0,0,0,770000000,779999999),(79,'Excesos y aplicaciones de provisiones',0,0,0,0,790000000,799999999),(100,'Capital social',0,0,0,0,100000000,100999999),(101,'Fondo social',0,0,0,0,101000000,101999999),(102,'Capital',0,0,0,0,102000000,102999999),(110,'Prima de emisión de acciones',0,0,0,0,110000000,110999999),(111,'Reservas de revalorización',0,0,0,0,111000000,111999999),(112,'Reserva legal',0,0,0,0,112000000,112999999),(113,'Reservas especiales',0,0,0,0,113000000,113999999),(114,'Reservas para acciones de la sociedad dominante',0,0,0,0,114000000,114999999),(115,'Reservas para acciones propias',0,0,0,0,115000000,115999999),(116,'Reservas estatutarias',0,0,0,0,116000000,116999999),(117,'Reservas voluntarias',0,0,0,0,117000000,117999999),(118,'Reserva por capital amortizado',0,0,0,0,118000000,118999999),(120,'Remanente',0,0,0,0,120000000,120999999),(121,'Resultados negativos de ejercicios anteriores',0,0,0,0,121000000,121999999),(122,'Aportaciones de socios para compensación pérdidas',0,0,0,0,122000000,122999999),(129,'Pérdidas y ganancias',0,0,0,0,129000000,129999999),(130,'Subvenciones oficiales de capital',0,0,0,0,130000000,130999999),(131,'Subvenciones de capital',0,0,0,0,131000000,131999999),(135,'Ingresos por intereses diferidos',0,0,0,0,135000000,135999999),(136,'Diferencias positivas en moneda extranjera',0,0,0,0,136000000,136999999),(140,'Provisión para pensiones y obligaciones similares',0,0,0,0,140000000,140999999),(141,'Provisión para impuestos',0,0,0,0,141000000,141999999),(142,'Provisión para responsabilidades',0,0,0,0,142000000,142999999),(143,'Provisión para grandes reparaciones',0,0,0,0,143000000,143999999),(144,'Fondo de reversión',0,0,0,0,144000000,144999999),(150,'Obligaciones y bonos',0,0,0,0,150000000,150999999),(151,'Obligaciones y bonos convertibles',0,0,0,0,151000000,151999999),(155,'Deudas representadas en otros valores negociables',0,0,0,0,155000000,155999999),(160,'Deudas a largo plazo con empresas del grupo',0,0,0,0,160000000,160999999),(161,'Deudas a largo plazo con empresas asociadas',0,0,0,0,161000000,161999999),(162,'Deudas largo plazo con entidades crédito del grupo',0,0,0,0,162000000,162999999),(163,'Deudas a largo plazo entidades de crédito asoc.',0,0,0,0,163000000,163999999),(164,'Proveedores de inmov. largo plazo empresas grupo',0,0,0,0,164000000,164999999),(165,'Proveedores de inmov. largo plazo empresas asoc.',0,0,0,0,165000000,165999999),(170,'Deudas largo plazo con entidades de crédito',0,0,0,0,170000000,170999999),(171,'Deudas largo plazo',0,0,0,0,171000000,171999999),(172,'Deudas largo plazo transformables en subvenciones',0,0,0,0,172000000,172999999),(173,'Proveedores de inmovilizado a largo plazo',0,0,0,0,173000000,173999999),(174,'Efectos a pagar a largo plazo',0,0,0,0,174000000,174999999),(180,'Fianzas recibidas a largo plazo',0,0,0,0,180000000,180999999),(185,'Depósitos recibidos a largo plazo',0,0,0,0,185000000,185999999),(190,'Accionistas por desembolsos no exigidos',0,0,0,0,190000000,190999999),(191,'Accionistas desembolsos no exigidos emp.grupo',0,0,0,0,191000000,191999999),(192,'Accionistas desembolsos no exigidos emp. asoc.',0,0,0,0,192000000,192999999),(193,'Accionistas aportaciones no dinerarias pendientes',0,0,0,0,193000000,193999999),(194,'Accionistas aport. no dinerarias ptes. emp. grupo',0,0,0,0,194000000,194999999),(195,'Accionistas aport. no dinerarias ptes. emp. asoc.',0,0,0,0,195000000,195999999),(196,'Socios parte no desembolsada',0,0,0,0,196000000,196999999),(198,'Acciones propias en situaciones especiales',0,0,0,0,198000000,198999999),(200,'Gastos de constitución',0,0,0,0,200000000,200999999),(201,'Gastos de primer establecimiento',0,0,0,0,201000000,201999999),(202,'Gastos ampliación de capital',0,0,0,0,202000000,202999999),(210,'Gastos de investigación y desarrollo',0,0,0,0,210000000,210999999),(211,'Concesiones administrativas',0,0,0,0,211000000,211999999),(212,'Propiedad industrial',0,0,0,0,212000000,212999999),(213,'Fondo de comercio',0,0,0,0,213000000,213999999),(214,'Derechos de transpaso',0,0,0,0,214000000,214999999),(215,'Aplicaciones informáticas',0,0,0,0,215000000,215999999),(217,'Derechos sobre bienes arrendamientos financieros',0,0,0,0,217000000,217999999),(219,'Anticipos de inmovilizaciones inmateriales',0,0,0,0,219000000,219999999),(220,'Terrenos y bienes naturales',0,0,0,0,220000000,220999999),(221,'Construcciones',0,0,0,0,221000000,221999999),(222,'Instalaciones técnicas',0,0,0,0,222000000,222999999),(223,'Maquinaria',0,0,0,0,223000000,223999999),(224,'Utillaje',0,0,0,0,224000000,224999999),(225,'Otras instalaciones',0,0,0,0,225000000,225999999),(226,'Mobiliario',0,0,0,0,226000000,226999999),(227,'Equipos para proceso de información',0,0,0,0,227000000,227999999),(228,'Elementos de transporte',0,0,0,0,228000000,228999999),(229,'Otro inmovilizado material',0,0,0,0,229000000,229999999),(230,'Adaptación de terrenos y bienes naturales',0,0,0,0,230000000,230999999),(231,'Construcciones en curso',0,0,0,0,231000000,231999999),(232,'Instalaciones técnicas en montaje',0,0,0,0,232000000,232999999),(233,'Maquinaria en montaje',0,0,0,0,233000000,233999999),(237,'Equipos para procesos de información en montaje',0,0,0,0,237000000,237999999),(239,'Anticipos para inmovilizaciones materiales',0,0,0,0,239000000,239999999),(240,'Participación en empresas del grupo',0,0,0,0,240000000,240999999),(241,'Participación en empresas asociadas',0,0,0,0,241000000,241999999),(242,'Valores de renta fija empresas del grupo',0,0,0,0,242000000,242999999),(243,'Valores de renta fija empresas asociadas',0,0,0,0,243000000,243999999),(244,'Créditos largo plazo empresas grupo',0,0,0,0,244000000,244999999),(245,'Créditos largo plazo empresas asociadas',0,0,0,0,245000000,245999999),(246,'Intereses largo plazo inversiones financ.emp.grupo',0,0,0,0,246000000,246999999),(247,'Intereses largo plazo inversiones financ.emp.asoc.',0,0,0,0,247000000,247999999),(248,'Desembolsos pendientes sobre acciones emp. grupo',0,0,0,0,248000000,248999999),(249,'Desembolsos pendientes sobre acciones emp.asoc.',0,0,0,0,249000000,249999999),(250,'Inversiones financieras permanentes en capital',0,0,0,0,250000000,250999999),(251,'Valores de renta fija',0,0,0,0,251000000,251999999),(252,'Créditos a largo plazo',0,0,0,0,252000000,252999999),(253,'Créditos largo plazo enajenacion de inmovilizado',0,0,0,0,253000000,253999999),(254,'Créditos a largo plazo al personal',0,0,0,0,254000000,254999999),(256,'Intereses a largo plazo valores de renta fija',0,0,0,0,256000000,256999999),(257,'Intereses largo plazo créditos',0,0,0,0,257000000,257999999),(258,'Imposiciones a largo plazo',0,0,0,0,258000000,258999999),(259,'Desembolsos pendientes sobre acciones',0,0,0,0,259000000,259999999),(260,'Fianzas constituidas a largo plazo',0,0,0,0,260000000,260999999),(265,'Depósitos constituidos a largo plazo',0,0,0,0,265000000,265999999),(270,'Gastos de formalización de deudas',0,0,0,0,270000000,270999999),(271,'Gastos por intereses diferidos valores negociables',0,0,0,0,271000000,271999999),(272,'Gastos por intereses diferidos',0,0,0,0,272000000,272999999),(281,'Amortización acumulada del inmovilizado inmaterial',0,0,0,0,281000000,281999999),(282,'Amortización acumulada del inmovilizado material',0,0,0,0,282000000,282999999),(291,'Provisión depreciación inmovilizado inmaterial',0,0,0,0,291000000,291999999),(292,'Provisión depreciación inmovilizado material',0,0,0,0,292000000,292999999),(293,'Provisión deprec. valores neg. a largo emp.grupo',0,0,0,0,293000000,293999999),(294,'Provisión deprec. valores neg. a largo emp.asoc.',0,0,0,0,294000000,294999999),(295,'Provisión insolvencias créditos a largo emp. grupo',0,0,0,0,295000000,295999999),(296,'Provisión insolvencias créditos a largo emp.asoc.',0,0,0,0,296000000,296999999),(297,'Provisión depreciación valores neg. largo plazo',0,0,0,0,297000000,297999999),(298,'Provisión insolvencias créditos a largo plazo',0,0,0,0,298000000,298999999),(300,'Mercaderías A',0,0,0,0,300000000,300999999),(301,'Mercaderías B',0,0,0,0,301000000,301999999),(310,'Materias primas A',0,0,0,0,310000000,310999999),(311,'Materias primas B',0,0,0,0,311000000,311999999),(320,'Elementos y conjuntos incorporables',0,0,0,0,320000000,320999999),(321,'Combustibles',0,0,0,0,321000000,321999999),(322,'Repuestos',0,0,0,0,322000000,322999999),(325,'Materiales diversos',0,0,0,0,325000000,325999999),(326,'Embalajes',0,0,0,0,326000000,326999999),(327,'Envases',0,0,0,0,327000000,327999999),(328,'Material de oficina',0,0,0,0,328000000,328999999),(330,'Productos en curso A',0,0,0,0,330000000,330999999),(331,'Productos en curso B',0,0,0,0,331000000,331999999),(340,'Productos semiterminados A',0,0,0,0,340000000,340999999),(341,'Productos semiterminados B',0,0,0,0,341000000,341999999),(350,'Productos terminados A',0,0,0,0,350000000,350999999),(351,'Productos terminados B',0,0,0,0,351000000,351999999),(360,'Subproductos A',0,0,0,0,360000000,360999999),(361,'Subproductos B',0,0,0,0,361000000,361999999),(365,'Residuos A',0,0,0,0,365000000,365999999),(366,'Residuos B',0,0,0,0,366000000,366999999),(368,'Materiales recuperados A',0,0,0,0,368000000,368999999),(369,'Materiales recuperados B',0,0,0,0,369000000,369999999),(390,'Provisión depreciación de mercaderias',0,0,0,0,390000000,390999999),(391,'Provisión depreciación de materias primas',0,0,0,0,391000000,391999999),(392,'Provisión depreciación otros aprovisionamientos',0,0,0,0,392000000,392999999),(393,'Provisión depreciación productos en curso',0,0,0,0,393000000,393999999),(394,'Provisión depreciación productos semiterminados',0,0,0,0,394000000,394999999),(395,'Provisión depreciación productos terminados',0,0,0,0,395000000,395999999),(396,'Provisión deprec. subproductos residuos mat.recup.',0,0,0,0,396000000,396999999),(400,'Proveedores',0,0,0,0,400000000,400999999),(401,'Proveedores efectos comerciales a pagar',0,0,0,0,401000000,401999999),(402,'Proveedores empresas del grupo',0,0,0,0,402000000,402999999),(403,'Proveedores empresas asociadas',0,0,0,0,403000000,403999999),(406,'Envases y embalajes a devolver a proveedores',0,0,0,0,406000000,406999999),(407,'Anticipos a proveedores',0,0,0,0,407000000,407999999),(410,'Acreedores por prestaciones de servicios',0,0,0,0,410000000,410999999),(411,'Acreedores efectos comerciales a pagar',0,0,0,0,411000000,411999999),(419,'Acreedores por operaciones en común',0,0,0,0,419000000,419999999),(430,'Clientes',0,0,0,0,430000000,430999999),(431,'Clientes efectos comerciales a cobrar',0,0,0,0,431000000,431999999),(432,'Clientes empresas del grupo',0,0,0,0,432000000,432999999),(433,'Clientes empresas asociadas',0,0,0,0,433000000,433999999),(435,'Clientes de dudoso cobro',0,0,0,0,435000000,435999999),(436,'Envases y embalajes a devolver por clientes',0,0,0,0,436000000,436999999),(437,'Anticipos de clientes',0,0,0,0,437000000,437999999),(440,'Deudores',0,0,0,0,440000000,440999999),(441,'Deudores efectos comerciales a cobrar',0,0,0,0,441000000,441999999),(445,'Deudores de dudoso cobro',0,0,0,0,445000000,445999999),(449,'Deudores por operaciones en común',0,0,0,0,449000000,449999999),(460,'Anticipos de remuneraciones',0,0,0,0,460000000,460999999),(465,'Remuneraciones pendientes de pago',0,0,0,0,465000000,465999999),(470,'Hacienda pública deudor por diversos conceptos',0,0,0,0,470000000,470999999),(471,'Organismos de la seguridad social, deudores',0,0,0,0,471000000,471999999),(472,'Hacienda pública iva soportado',0,0,0,0,472000000,472999999),(473,'Hacienda pública, retenciones y pagos a cuenta',0,0,0,0,473000000,473999999),(474,'Impuesto beneficios anticipados y comp. pérdidas',0,0,0,0,474000000,474999999),(475,'Hacienda pública acreedor por conceptos fiscales',0,0,0,0,475000000,475999999),(476,'Organismos de la seguridad social acreedores',0,0,0,0,476000000,476999999),(477,'Hacienda pública iva repercutido',0,0,0,0,477000000,477999999),(479,'Impuesto sobre beneficios diferido',0,0,0,0,479000000,479999999),(480,'Gastos anticipados',0,0,0,0,480000000,480999999),(485,'Ingresos anticipados',0,0,0,0,485000000,485999999),(490,'Provisión para insolvencias de tráfico',0,0,0,0,490000000,490999999),(493,'Provisión insolvencias tráfico empresas del grupo',0,0,0,0,493000000,493999999),(494,'Provisión insolvencias tráfico empresas asociadas',0,0,0,0,494000000,494999999),(499,'Provisión para otras operaciones de tráfico',0,0,0,0,499000000,499999999),(500,'Obligaciones y bonos a corto plazo',0,0,0,0,500000000,500999999),(501,'Obligaciones y bonos convertibles a corto plazo',0,0,0,0,501000000,501999999),(505,'Deudas representadas en otros valores neg. a corto',0,0,0,0,505000000,505999999),(506,'Intereses empréstitos y otras emisiones análogas',0,0,0,0,506000000,506999999),(509,'Valores negociables amortizados',0,0,0,0,509000000,509999999),(510,'Deudas a corto plazo con empresas del grupo',0,0,0,0,510000000,510999999),(511,'Deudas a corto plazo con empresas asociadas',0,0,0,0,511000000,511999999),(512,'Deudas a corto plazo entidades crédito del grupo',0,0,0,0,512000000,512999999),(513,'Deudas a corto plazo entidades crédito asociadas',0,0,0,0,513000000,513999999),(514,'Proveedores de inmovilizado a corto emp. grupo',0,0,0,0,514000000,514999999),(515,'Proveedores de inmovilizado a corto emp. asociadas',0,0,0,0,515000000,515999999),(516,'Intereses a corto plazo de deudas com emp. grupo',0,0,0,0,516000000,516999999),(517,'Intereses a corto plazo de deudas con emp. asoc.',0,0,0,0,517000000,517999999),(520,'Deudas a corto plazo con entidades de crédito',0,0,0,0,520000000,520999999),(521,'Deudas a corto plazo',0,0,0,0,521000000,521999999),(523,'Proveedores de inmovilizado a corto plazo',0,0,0,0,523000000,523999999),(524,'Efectos comerciales a pagar a corto plazo',0,0,0,0,524000000,524999999),(525,'Dividendo activo a pagar',0,0,0,0,525000000,525999999),(526,'Intereses a corto plazo deudas entidades crédito',0,0,0,0,526000000,526999999),(527,'Intereses a corto plazo de deudas',0,0,0,0,527000000,527999999),(530,'Participaciones a corto plazo en empresas grupo',0,0,0,0,530000000,530999999),(531,'Participaciones a corto plazo en empresas asoc.',0,0,0,0,531000000,531999999),(532,'Valores de renta fija a corto plazo empresas grupo',0,0,0,0,532000000,532999999),(533,'Valores de renta fija corto plazo empresas asoc.',0,0,0,0,533000000,533999999),(534,'Créditos a corto plazo a empresas del grupo',0,0,0,0,534000000,534999999),(535,'Créditos a corto plazo a empresas asociadas',0,0,0,0,535000000,535999999),(536,'Intereses a corto inversiones financ. emp. grupo',0,0,0,0,536000000,536999999),(537,'Intereses a corto inversiones financ. emp. asoc.',0,0,0,0,537000000,537999999),(538,'Desembolsos pendientes acciones a corto emp.grup.',0,0,0,0,538000000,538999999),(539,'Desembolsos pendientes.acciones a corto emp.asoc.',0,0,0,0,539000000,539999999),(540,'Inversiones financieras temporales en capital',0,0,0,0,540000000,540999999),(541,'Valores de renta fija a corto plazo',0,0,0,0,541000000,541999999),(542,'Créditos a corto plazo',0,0,0,0,542000000,542999999),(543,'Créditos a corto por enajenación del inmovilizado',0,0,0,0,543000000,543999999),(544,'Créditos a corto plazo al personal',0,0,0,0,544000000,544999999),(545,'Dividendo a cobrar',0,0,0,0,545000000,545999999),(546,'Intereses a corto plazo de valores renta fija',0,0,0,0,546000000,546999999),(547,'Intereses a corto plazo de créditos',0,0,0,0,547000000,547999999),(548,'Imposiciones a corto plazo',0,0,0,0,548000000,548999999),(549,'Desembolsos pendientes sobre acciones a corto',0,0,0,0,549000000,549999999),(550,'Titular de la explotación',0,0,0,0,550000000,550999999),(551,'Cuenta corriente con empresas del grupo',0,0,0,0,551000000,551999999),(552,'Cuenta corriente con empresas asociadas',0,0,0,0,552000000,552999999),(553,'Cuenta corriente con socios y administradores',0,0,0,0,553000000,553999999),(555,'Partidas pendientes de aplicación',0,0,0,0,555000000,555999999),(556,'Desembolsos exigidos sobre acciones',0,0,0,0,556000000,556999999),(557,'Dividendo activo a cuenta',0,0,0,0,557000000,557999999),(558,'Accionistas por desembolsos exigidos',0,0,0,0,558000000,558999999),(560,'Fianzas recibidas a corto plazo',0,0,0,0,560000000,560999999),(561,'Depósitos recibidos a corto plazo',0,0,0,0,561000000,561999999),(565,'Fianzas constituidas a corto plazo',0,0,0,0,565000000,565999999),(566,'Depósitos constituidos a corto plazo',0,0,0,0,566000000,566999999),(570,'Caja pesetas',0,0,0,0,570000000,570999999),(571,'Caja moneda extranjera',0,0,0,0,571000000,571999999),(572,'Bancos e inst. crédito c/c vista, pesetas',0,0,0,0,572000000,572999999),(573,'Bancos e inst. crédito c/c vista, moneda extr.',0,0,0,0,573000000,573999999),(574,'Bancos e inst. crédito cuentas ahorro pesetas',0,0,0,0,574000000,574999999),(575,'Bancos e inst. crédito cuentas ahorro moneda extr.',0,0,0,0,575000000,575999999),(580,'Intereses pagados por anticipado',0,0,0,0,580000000,580999999),(585,'Intereses cobrados por anticipado',0,0,0,0,585000000,585999999),(593,'Provisión deprec. valores neg. a corto emp. grupo',0,0,0,0,593000000,593999999),(594,'Provisión deprec. valores neg. a corto emp. asoc.',0,0,0,0,594000000,594999999),(595,'Provisión insolvencias créditos a corto emp. grupo',0,0,0,0,595000000,595999999),(596,'Provisión insolvencias créditos corto emp. asoc.',0,0,0,0,596000000,596999999),(597,'Provisión depreciación valores negociables a corto',0,0,0,0,597000000,597999999),(598,'Provisión insolvencias créditos a corto plazo',0,0,0,0,598000000,598999999),(600,'Compras de mercaderias',0,0,0,0,600000000,600999999),(601,'Compras de materias primas',0,0,0,0,601000000,601999999),(602,'Compras de otros aprovisionamientos',0,0,0,0,602000000,602999999),(607,'Trabajos realizados por otras empresas',0,0,0,0,607000000,607999999),(608,'Devoluciones de compras y operaciones similares',0,0,0,0,608000000,608999999),(609,'`rappels` por compras',0,0,0,0,609000000,609999999),(610,'Variación de existencias de mercaderias',0,0,0,0,610000000,610999999),(611,'Variación de existencias de materias primas',0,0,0,0,611000000,611999999),(612,'Variación de existencias otros aprovisionamientos',0,0,0,0,612000000,612999999),(620,'Investigación y desarrollo',0,0,0,0,620000000,620999999),(621,'Arrendamientos y canones',0,0,0,0,621000000,621999999),(622,'Reparaciones y conservación',0,0,0,0,622000000,622999999),(623,'Servicios de profesionales independientes',0,0,0,0,623000000,623999999),(624,'Transportes',0,0,0,0,624000000,624999999),(625,'Primas de seguros',0,0,0,0,625000000,625999999),(626,'Servicios bancarios y similares',0,0,0,0,626000000,626999999),(627,'Publicidad, propaganda y relaciones públicas',0,0,0,0,627000000,627999999),(628,'Suministros',0,0,0,0,628000000,628999999),(629,'Otros servicios',0,0,0,0,629000000,629999999),(630,'Impuesto sobre beneficios',0,0,0,0,630000000,630999999),(631,'Otros tributos',0,0,0,0,631000000,631999999),(633,'Ajustes negativos en la imposición sobre benefic.',0,0,0,0,633000000,633999999),(634,'Ajustes negativos en la imposición indirecta',0,0,0,0,634000000,634999999),(636,'Devolución de impuestos',0,0,0,0,636000000,636999999),(638,'Ajustes positivos en la imposición sobre benefic.',0,0,0,0,638000000,638999999),(639,'Ajustes positivos en la imposición indirecta',0,0,0,0,639000000,639999999),(640,'Sueldos y salarios',0,0,0,0,640000000,640999999),(641,'Indemnizaciones',0,0,0,0,641000000,641999999),(642,'Seguridad social a cargo de la empresa',0,0,0,0,642000000,642999999),(643,'Aportaciones a sistemas complement.de pensiones',0,0,0,0,643000000,643999999),(649,'Otros gastos sociales',0,0,0,0,649000000,649999999),(650,'Pérdidas de créditos comerciales incobrables',0,0,0,0,650000000,650999999),(651,'Resultados de operaciones en común',0,0,0,0,651000000,651999999),(659,'Otras pérdidas en gestión corriente',0,0,0,0,659000000,659999999),(661,'Intereses de obligaciones y bonos',0,0,0,0,661000000,661999999),(662,'Intereses de deudas a largo plazo',0,0,0,0,662000000,662999999),(663,'Intereses de deudas a corto plazo',0,0,0,0,663000000,663999999),(664,'Intereses por descuento de efectos',0,0,0,0,664000000,664999999),(665,'Descuentos sobre ventas por pronto pago',0,0,0,0,665000000,665999999),(666,'Pérdidas en valores mobiliarios',0,0,0,0,666000000,666999999),(667,'Pérdidas de créditos',0,0,0,0,667000000,667999999),(668,'Diferencias negativas de cambio',0,0,0,0,668000000,668999999),(669,'Otros gastos financieros',0,0,0,0,669000000,669999999),(670,'Pérdidas procedentes de inmovilizado inmaterial',0,0,0,0,670000000,670999999),(671,'Pérdidas procedentes del inmovilizado material',0,0,0,0,671000000,671999999),(672,'Pérdidas procedentes de valores a largo plazo',0,0,0,0,672000000,672999999),(673,'Pérdidas de créditos a largo plazo',0,0,0,0,673000000,673999999),(674,'Pérdidas operaciones acciones y oblig. propias',0,0,0,0,674000000,674999999),(678,'Gastos extraordinarios',0,0,0,0,678000000,678999999),(679,'Gastos y pérdidas de ejercicios anteriores',0,0,0,0,679000000,679999999),(680,'Amortización de gastos de establecimiento',0,0,0,0,680000000,680999999),(681,'Amortización del inmovilizado inmaterial',0,0,0,0,681000000,681999999),(682,'Amortización del inmovilizado material',0,0,0,0,682000000,682999999),(690,'Dotación al fondo de reversión',0,0,0,0,690000000,690999999),(691,'Dotación provisión del inmovilizado inmaterial',0,0,0,0,691000000,691999999),(692,'Dotación provisión del inmovilizado material',0,0,0,0,692000000,692999999),(693,'Dotación provisión de existencias',0,0,0,0,693000000,693999999),(694,'Dotación provisión para insolvencias de tráfico',0,0,0,0,694000000,694999999),(695,'Dotación provisión otras operaciones de tráfico',0,0,0,0,695000000,695999999),(696,'Dotación provisión valores negociables a largo',0,0,0,0,696000000,696999999),(697,'Dotación provisión insolvencias créditos a largo',0,0,0,0,697000000,697999999),(698,'Dotacion provisión valores negociables a corto',0,0,0,0,698000000,698999999),(699,'Dtoacion provisión insolvencias créditos a corto',0,0,0,0,699000000,699999999),(700,'Ventas de mercaderias',0,0,0,0,700000000,700999999),(701,'Ventas de productos terminados',0,0,0,0,701000000,701999999),(702,'Ventas de productos semiterminados',0,0,0,0,702000000,702999999),(703,'Ventas de subproductos y residuos',0,0,0,0,703000000,703999999),(704,'Ventas de envases y embalajes',0,0,0,0,704000000,704999999),(705,'Prestaciones de servicios',0,0,0,0,705000000,705999999),(708,'Devoluciones de ventas y operaciones similares',0,0,0,0,708000000,708999999),(709,'`rappels` sobre ventas',0,0,0,0,709000000,709999999),(710,'Variación de existencias de productos en curso',0,0,0,0,710000000,710999999),(711,'Variación de existencias de productos semiterminad',0,0,0,0,711000000,711999999),(712,'Variación existencias de productos terminados',0,0,0,0,712000000,712999999),(713,'Variación exist.subprod.residuos y mat.recuperados',0,0,0,0,713000000,713999999),(730,'Incorporacion al activo de gastos establecimiento',0,0,0,0,730000000,730999999),(731,'Trabajos realizados inmovilizado inmaterial',0,0,0,0,731000000,731999999),(732,'Trabajos realizados inmovilizado material',0,0,0,0,732000000,732999999),(733,'Trabajos realizados inmovilizado material en curso',0,0,0,0,733000000,733999999),(737,'Incorporación activo gastos formalización deudas',0,0,0,0,737000000,737999999),(740,'Subvenciones oficiales a la explotación',0,0,0,0,740000000,740999999),(741,'Otras subvenciones a la explotación',0,0,0,0,741000000,741999999),(751,'Resultados de operaciones en común',0,0,0,0,751000000,751999999),(752,'Ingresos por arrendamientos',0,0,0,0,752000000,752999999),(753,'Ingresos propiedad industrial cedida explotación',0,0,0,0,753000000,753999999),(754,'Ingresos por comisiones',0,0,0,0,754000000,754999999),(755,'Ingresos por servicios al personal',0,0,0,0,755000000,755999999),(759,'Ingresos por servicios diversos',0,0,0,0,759000000,759999999),(760,'Ingresos en participaciones de capital',0,0,0,0,760000000,760999999),(761,'Ingresos de valores de renta fija',0,0,0,0,761000000,761999999),(762,'Ingresos de créditos a largo plazo',0,0,0,0,762000000,762999999),(763,'Ingresos de créditos a corto plazo',0,0,0,0,763000000,763999999),(765,'Descuento sobre compras por pronto pago',0,0,0,0,765000000,765999999),(766,'Beneficios en valores negociables',0,0,0,0,766000000,766999999),(768,'Diferencias positivas de cambio',0,0,0,0,768000000,768999999),(769,'Otros ingresos financieros',0,0,0,0,769000000,769999999),(770,'Beneficios procedentes de inmovilizado inmaterial',0,0,0,0,770000000,770999999),(771,'Beneficios procedentes de inmovilizado material',0,0,0,0,771000000,771999999),(772,'Benef. procedentes part. capital a largo emp.grupo',0,0,0,0,772000000,772999999),(774,'Benef. operaciones acciones y obligaciones propias',0,0,0,0,774000000,774999999),(775,'Subvenciones de capital transp.resultado ejercicio',0,0,0,0,775000000,775999999),(778,'Ingresos extraordinarios',0,0,0,0,778000000,778999999),(779,'Ingresos y beneficios de ejercicios anteriores',0,0,0,0,779000000,779999999),(790,'Excesos de provisión para riesgos y gastos',0,0,0,0,790000000,790999999),(791,'Exceso de provisión del inmovilizado inmaterial',0,0,0,0,791000000,791999999),(792,'Exceso de provisión del inmovilizado material',0,0,0,0,792000000,792999999),(793,'Provisión de existencias aplicadas',0,0,0,0,793000000,793999999),(794,'Provisión para insolvencias de trafico aplicadas',0,0,0,0,794000000,794999999),(795,'Provisión para otras operaciones de tráfico aplic.',0,0,0,0,795000000,795999999),(796,'Exceso provisión valores negociables a largo',0,0,0,0,796000000,796999999),(797,'Exceso provisión insolvencias créditos a largo',0,0,0,0,797000000,797999999),(798,'Exceso provisión valores negociables a corto',0,0,0,0,798000000,798999999),(799,'Exceso provisión insolvencias créditos a corto',0,0,0,0,799000000,799999999),(1000,'Capital ordinario',0,0,0,0,100000000,100099999),(1001,'Capital privilegiado',0,0,0,0,100100000,100199999),(1002,'Capital sin derecho a voto',0,0,0,0,100200000,100299999),(1003,'Capital con derechos restringidos',0,0,0,0,100300000,100399999),(1300,'Subvenciones del Estado',0,0,0,0,130000000,130099999),(1301,'Subvenciones de otras Administraciones Públicas',0,0,0,0,130100000,130199999),(1500,'Obligaciones y bonos simples',0,0,0,0,150000000,150099999),(1501,'Obligaciones y bonos garantizados',0,0,0,0,150100000,150199999),(1502,'Obligaciones y bonos subordinados',0,0,0,0,150200000,150299999),(1503,'Obligaciones y bonos cupón cero',0,0,0,0,150300000,150399999),(1504,'Obligaciones y bonos c/opción suscripción acciones',0,0,0,0,150400000,150499999),(1505,'Obligaciones y bonos c/participación en beneficios',0,0,0,0,150500000,150599999),(1600,'Préstamos a largo plazo de empresas del grupo',0,0,0,0,160000000,160099999),(1609,'Otras deudas a largo plazo con empresas del grupo',0,0,0,0,160900000,160999999),(1700,'Préstamos a largo plazo de entidades de crédito',0,0,0,0,170000000,170099999),(1709,'Otras deudas a largo plazo de entidades de crédito',0,0,0,0,170900000,170999999),(2100,'Gastos de I + D en proyectos no terminados',0,0,0,0,210000000,210099999),(2101,'Gastos de I + D en proyectos terminados',0,0,0,0,210100000,210199999),(2500,'Inv. finan. perm. acciones con cotización mercado',0,0,0,0,250000000,250099999),(2501,'Inv. finan. perm. acciones sin cotización mercado',0,0,0,0,250100000,250199999),(2502,'Otras inversiones financieras en capital',0,0,0,0,250200000,250299999),(2810,'Amortización acumulada de gastos de I + D',0,0,0,0,281000000,281099999),(2811,'Amortización acumulada concesiones administrativas',0,0,0,0,281100000,281199999),(2812,'Amortización acumulada de propiedad industrial',0,0,0,0,281200000,281299999),(2813,'Amortización acumulada de fondo de comercio',0,0,0,0,281300000,281399999),(2814,'Amortización acumulada de derechos de traspaso',0,0,0,0,281400000,281499999),(2815,'Amortización acumulada aplicaciones informáticas',0,0,0,0,281500000,281599999),(2817,'Amt. acum. derechos bienes régimen arrend. financ.',0,0,0,0,281700000,281799999),(2821,'Amortización acumulada de construcciones',0,0,0,0,282100000,282199999),(2822,'Amortización acumulada de instalaciones técnicas',0,0,0,0,282200000,282299999),(2823,'Amortización acumulada de maquinaria',0,0,0,0,282300000,282399999),(2824,'Amortización acumulada de utillaje',0,0,0,0,282400000,282499999),(2825,'Amortización acumulada de otras instalaciones',0,0,0,0,282500000,282599999),(2826,'Amortización acumulada de mobiliario',0,0,0,0,282600000,282699999),(2827,'Amt. acum. de equipos para proceso de información',0,0,0,0,282700000,282799999),(2828,'Amortización acumulada de elementos de transporte',0,0,0,0,282800000,282899999),(2829,'Amortización acumulada otro inmovilizado material',0,0,0,0,282900000,282999999),(2930,'Provisión deprec. partic.capital a largo emp.grupo',0,0,0,0,293000000,293099999),(2935,'Provisión deprec. val renta fija a largo emp.grupo',0,0,0,0,293500000,293599999),(2941,'Provisión deprec. partic.capital a largo emp.asoc.',0,0,0,0,294100000,294199999),(2946,'Provisión deprec. val renta fija a largo emp.asoc.',0,0,0,0,294600000,294699999),(4000,'Proveedores (Pesetas)',0,0,0,0,400000000,400099999),(4004,'Proveedores (Moneda extranjera)',0,0,0,0,400400000,400499999),(4009,'Proveedores facturas pendientes de formalizar',0,0,0,0,400900000,400999999),(4020,'Proveedores empresas del grupo (pesetas)',0,0,0,0,402000000,402099999),(4021,'Efectos comerciales a pagar, empresas del grupo',0,0,0,0,402100000,402199999),(4024,'Proveedores, empresas del grupo(moneda extranjera)',0,0,0,0,402400000,402499999),(4026,'Envases y embalajes a devolver a prov. emp.grupo',0,0,0,0,402600000,402699999),(4029,'Proveedores emp.grupo facturas pendientes',0,0,0,0,402900000,402999999),(4100,'Acreedores prestaciones de servicios (pesetas)',0,0,0,0,410000000,410099999),(4104,'Acreedores prestaciones de servicios (moneda ext.)',0,0,0,0,410400000,410499999),(4109,'Acreedores prestaciones de servicios, facturas',0,0,0,0,410900000,410999999),(4300,'Clientes (pesetas)',0,0,0,0,430000000,430099999),(4304,'Clientes (moneda extranjera)',0,0,0,0,430400000,430499999),(4309,'Clientes, facturas pendientes de formalizar',0,0,0,0,430900000,430999999),(4310,'Efectos comerciales en cartera',0,0,0,0,431000000,431099999),(4311,'Efectos comerciales descontados',0,0,0,0,431100000,431199999),(4312,'Efectos comerciales en gestión de cobro',0,0,0,0,431200000,431299999),(4315,'Efectos comerciales impagados',0,0,0,0,431500000,431599999),(4320,'Clientes, empresas del grupo (pesetas)',0,0,0,0,432000000,432099999),(4321,'Efectos comerciales a cobrar, empresas del grupo',0,0,0,0,432100000,432199999),(4324,'Clientes, empresas del grupo (moneda extranjera)',0,0,0,0,432400000,432499999),(4326,'Envases y embalajes a devolver clientes, emp.grupo',0,0,0,0,432600000,432699999),(4329,'Clientes, empresas del grupo, facturas pendientes ',0,0,0,0,432900000,432999999),(4400,'Deudores (pesetas)',0,0,0,0,440000000,440099999),(4404,'Deudores (moneda extranjera)',0,0,0,0,440400000,440499999),(4409,'Deudores, facturas pendientes de formalizar',0,0,0,0,440900000,440999999),(4410,'Deudores, efectos comerciales en cartera',0,0,0,0,441000000,441099999),(4411,'Deudores, efectos comerciales descontados',0,0,0,0,441100000,441199999),(4412,'Deudores, efectos comerciales en gestión de cobro',0,0,0,0,441200000,441299999),(4415,'Deudores, efectos comerciales impagados',0,0,0,0,441500000,441599999),(4700,'Hacienda Pública, deudor por IVA',0,0,0,0,470000000,470099999),(4708,'Hacienda Pública, deudor subvenciones concedidas',0,0,0,0,470800000,470899999),(4709,'Hacienda Pública, deudor devolución de impuestos',0,0,0,0,470900000,470999999),(4740,'Impuesto sobre beneficios anticipado',0,0,0,0,474000000,474099999),(4745,'Crédito por pérdidas a compensar del ejercicio...',0,0,0,0,474500000,474599999),(4750,'Hacienda Pública, acreedor por IVA.',0,0,0,0,475000000,475099999),(4751,'Hacienda Pública, acreedor retenciones practicadas',0,0,0,0,475100000,475199999),(4752,'Hacienda Pública, acreedor impuesto sociedades',0,0,0,0,475200000,475299999),(4758,'Hacienda Pública, acreedor subvenciones reintegrar',0,0,0,0,475800000,475899999),(5090,'Obligaciones y bonos amortizados',0,0,0,0,509000000,509099999),(5091,'Obligaciones y bonos convertibles amortizados',0,0,0,0,509100000,509199999),(5095,'Otros valores negociables amortizados',0,0,0,0,509500000,509599999),(5100,'Préstamos a corto plazo de empresas del grupo',0,0,0,0,510000000,510099999),(5109,'Otras deudas a corto plazo con empresas del grupo',0,0,0,0,510900000,510999999),(5120,'Préstamos a corto de entidades crédito del grupo',0,0,0,0,512000000,512099999),(5128,'Deudas efectos descontados entidades crédito grupo',0,0,0,0,512800000,512899999),(5129,'Otras deudas a corto entidades crédito del grupo',0,0,0,0,512900000,512999999),(5200,'Préstamos a corto plazo de entidades de crédito',0,0,0,0,520000000,520099999),(5201,'Deudas a corto plazo por crédito dispuesto',0,0,0,0,520100000,520199999),(5208,'Deudas por efectos descontados',0,0,0,0,520800000,520899999),(5360,'Intereses a corto plazo de valores de renta fija',0,0,0,0,536000000,536099999),(5361,'Intereses a corto plazo de créditos a emp. grupo',0,0,0,0,536100000,536199999),(5400,'Inversiones finan. temp. acciones con cotización',0,0,0,0,540000000,540099999),(5401,'Inversiones finan. temp. acciones sin cotización',0,0,0,0,540100000,540199999),(5409,'Otras inversiones finan. temporales en capital',0,0,0,0,540900000,540999999),(5560,'Desembolsos exigidos sobre acciones de emp. grupo',0,0,0,0,556000000,556099999),(5561,'Desembolsos exigidos sobre acciones de emp. asoc.',0,0,0,0,556100000,556199999),(5562,'Desembolsos exigidos sobre acciones de otras emp.',0,0,0,0,556200000,556299999),(6080,'Devoluciones de compras de mercaderías',0,0,0,0,608000000,608099999),(6081,'Devoluciones de compras de materias primas',0,0,0,0,608100000,608199999),(6082,'Devoluciones de compras otros aprovisionamientos',0,0,0,0,608200000,608299999),(6090,'`Rappels` por compras de mercaderías',0,0,0,0,609000000,609099999),(6091,'`Rappels` por compras de materias primas',0,0,0,0,609100000,609199999),(6092,'`Rappels` por compras de otros aprovisionamientos',0,0,0,0,609200000,609299999),(6341,'Ajustes negativos en I.V.A. de circulante',0,0,0,0,634100000,634199999),(6342,'Ajustes negativos  en I.V.A. de inversiones',0,0,0,0,634200000,634299999),(6391,'Ajustes positivos en I.V.A. de circulante',0,0,0,0,639100000,639199999),(6392,'Ajustes positivos en I.V.A. de inversiones',0,0,0,0,639200000,639299999),(6510,'Beneficio transferido (gestor)',0,0,0,0,651000000,651099999),(6511,'Pérdida soportada (partícipe o asociado no gestor)',0,0,0,0,651100000,651199999),(6610,'Intereses obligaciones y bonos a largo emp.grupo',0,0,0,0,661000000,661099999),(6611,'Intereses obligaciones y bonos a largo emp.asoc.',0,0,0,0,661100000,661199999),(6613,'Intereses obligaciones y bonos a largo otras emp.',0,0,0,0,661300000,661399999),(6615,'Intereses obligaciones y bonos a corto emp.grupo',0,0,0,0,661500000,661599999),(6616,'Intereses obligaciones y bonos a corto emp.asoc.',0,0,0,0,661600000,661699999),(6618,'Intereses obligaciones y bonos a corto otras emp.',0,0,0,0,661800000,661899999),(6620,'Intereses deudas a largo plazo con emp. grupo',0,0,0,0,662000000,662099999),(6621,'Intereses deudas a largo plazo con emp. asoc.',0,0,0,0,662100000,662199999),(6622,'Intereses deudas a largo con entidades de crédito',0,0,0,0,662200000,662299999),(6623,'Intereses deudas largo con otras empresas',0,0,0,0,662300000,662399999),(6630,'Intereses deudas a corto plazo con emp. grupo',0,0,0,0,663000000,663099999),(6631,'Intereses deudas a corto plazo con emp. asoc.',0,0,0,0,663100000,663199999),(6632,'Intereses deudas a corto con entidades de crédito',0,0,0,0,663200000,663299999),(6633,'Intereses de deudas a corto con otras empresas',0,0,0,0,663300000,663399999),(6640,'Intereses descuento efectos ent. crédito grupo',0,0,0,0,664000000,664099999),(6641,'Intereses descuento efectos ent. crédito asoc.',0,0,0,0,664100000,664199999),(6642,'Intereses descuento efectos en otras ent. crédito',0,0,0,0,664200000,664299999),(6650,'Descuentos ventas por pronto pago emp. grupo',0,0,0,0,665000000,665099999),(6651,'Descuentos ventas por pronto pago emp. asoc.',0,0,0,0,665100000,665199999),(6653,'Descuentos ventas por pronto pago otras emp.',0,0,0,0,665300000,665399999),(6660,'Pérdidas valores negociables a largo emp. grupo',0,0,0,0,666000000,666099999),(6661,'Pérdidas valores negociables a largo emp. asoc.',0,0,0,0,666100000,666199999),(6663,'Pérdidas valores negociables a largo otras emp.',0,0,0,0,666300000,666399999),(6665,'Pérdidas valores negociables a corto emp. grupo',0,0,0,0,666500000,666599999),(6666,'Pérdidas valores negociables a corto emp. asoc.',0,0,0,0,666600000,666699999),(6668,'Pérdidas valores negociables a corto otras emp.',0,0,0,0,666800000,666899999),(6670,'Pérdidas créditos a largo plazo a emp. grupo',0,0,0,0,667000000,667099999),(6671,'Pérdidas créditos a largo plazo a emp. asociadas',0,0,0,0,667100000,667199999),(6673,'Pérdidas créditos a largo plazo a otras empresas',0,0,0,0,667300000,667399999),(6675,'Pérdidas créditos a corto plazo a emp. grupo',0,0,0,0,667500000,667599999),(6676,'Pérdidas créditos a corto plazo a emp. asociadas',0,0,0,0,667600000,667699999),(6678,'Pérdidas créditos a corto plazo a otras empresas',0,0,0,0,667800000,667899999),(6960,'Dotac.provis.particip.capital a largo emp.grupo',0,0,0,0,696000000,696099999),(6961,'Dotac.provis.particip.capital a largo emp.asoc.',0,0,0,0,696100000,696199999),(6963,'Dotac.provis. valores r.fija a largo otras emp',0,0,0,0,696300000,696399999),(6965,'Dotac.provis. valores r.fija a largo emp.grupo',0,0,0,0,696500000,696599999),(6966,'Dotac.provis. valores r.fija a largo emp asoc.',0,0,0,0,696600000,696699999),(6970,'Dotac.provis. insolv. crédito a largo emp.grupo',0,0,0,0,697000000,697099999),(6971,'Dotac.provis. insolv. crédito a largo emp.asoc.',0,0,0,0,697100000,697199999),(6973,'Dotac.provis. insolv. crédito a largo otras emp.',0,0,0,0,697300000,697399999),(6980,'Dotac.provis. valores neg. a corto emp. grupo',0,0,0,0,698000000,698099999),(6981,'Dotac.provis. valores neg. a corto emp. asoc.',0,0,0,0,698100000,698199999),(6983,'Dotac.provis. valores neg. a corto otras emp.',0,0,0,0,698300000,698399999),(6990,'Dotac.provis. insolv. crédito a corto emp. grupo',0,0,0,0,699000000,699099999),(6991,'Dotac.provis. insolv. crédito a corto emp. asoc.',0,0,0,0,699100000,699199999),(6993,'Dotac.provis. insolv. crédito a corto otras emp.',0,0,0,0,699300000,699399999),(7080,'Devoluciones de ventas de mercaderías',0,0,0,0,708000000,708099999),(7081,'Devoluciones de ventas de productos terminados',0,0,0,0,708100000,708199999),(7082,'Devoluciones de ventas de productos semiterminados',0,0,0,0,708200000,708299999),(7083,'Devoluciones de ventas de subproductos y residuos',0,0,0,0,708300000,708399999),(7084,'Devoluciones de ventas de envases y embalajes',0,0,0,0,708400000,708499999),(7090,'Rappels sobre ventas de mercaderías',0,0,0,0,709000000,709099999),(7091,'Rappels sobre ventas de productos terminados',0,0,0,0,709100000,709199999),(7092,'Rappels sobre ventas de productos semiterminados',0,0,0,0,709200000,709299999),(7093,'Rappels sobre ventas de subproductos y residuos',0,0,0,0,709300000,709399999),(7094,'Rappels sobre ventas de envases y embalajes',0,0,0,0,709400000,709499999),(7510,'Pérdida transferencia (gestor)',0,0,0,0,751000000,751099999),(7511,'Beneficio atribuido (partícipe asociado no gestor)',0,0,0,0,751100000,751199999),(7600,'Ingresos participaciones en capital emp.grupo',0,0,0,0,760000000,760099999),(7601,'Ingresos participaciones en capital emp.asociadas',0,0,0,0,760100000,760199999),(7603,'Ingresos participaciones capital otras empresas',0,0,0,0,760300000,760399999),(7610,'Ingresos en valores renta fija empresas del grupo',0,0,0,0,761000000,761099999),(7611,'Ingresos en valores renta fija empresas asociadas',0,0,0,0,761100000,761199999),(7613,'Ingresos en valores renta fija otras empresas',0,0,0,0,761300000,761399999),(7620,'Ingresos créditos a largo plazo a emp. grupo',0,0,0,0,762000000,762099999),(7621,'Ingresos créditos a largo plazo emp.asociadas',0,0,0,0,762100000,762199999),(7623,'Ingresos créditos a argo plazo a otras empresas',0,0,0,0,762300000,762399999),(7630,'Ingresos créditos a corto plazo a emp. grupo',0,0,0,0,763000000,763099999),(7631,'Ingresos créditos a corto plazo a emp. asociadas',0,0,0,0,763100000,763199999),(7633,'Ingresos créditos a corto plazo a otras empresas',0,0,0,0,763300000,763399999),(7650,'Descuentos sobre compras por pronto pago emp.grupo',0,0,0,0,765000000,765099999),(7651,'Descuentos sobre compras por pronto pago emp.asoc.',0,0,0,0,765100000,765199999),(7653,'Descuentos sobre compras por pronto pago otras emp',0,0,0,0,765300000,765399999),(7660,'Beneficios valores negociables a largo emp. grupo',0,0,0,0,766000000,766099999),(7661,'Beneficios valores negociables a largo emp. asoc.',0,0,0,0,766100000,766199999),(7663,'Beneficios valores negociables a largo otras emp.',0,0,0,0,766300000,766399999),(7665,'Beneficios valores negociables a corto emp. grupo',0,0,0,0,766500000,766599999),(7666,'Beneficios valores negociables a corto emp. asoc.',0,0,0,0,766600000,766699999),(7668,'Beneficios valores negociables a corto otras emp.',0,0,0,0,766800000,766899999),(7960,'Exceso provis.particip.capital a largo emp.grupo',0,0,0,0,796000000,796099999),(7961,'Exceso provis.particip.capital a largo emp.asoc.',0,0,0,0,796100000,796199999),(7963,'Exceso provis.valores negociables largo otras emp.',0,0,0,0,796300000,796399999),(7965,'Exceso provis.valores r.fija a largo emp.grupo',0,0,0,0,796500000,796599999),(7966,'Exceso provis.valores r.fija a largo emp asoc.',0,0,0,0,796600000,796699999),(7970,'Exceso provis.insolv. créditos a largo emp. grupo',0,0,0,0,797000000,797099999),(7971,'Exceso provis.insolv. créditos a largo emp. asoc.',0,0,0,0,797100000,797199999),(7973,'Exceso provis.insolv. créditos a largo otras emp.',0,0,0,0,797300000,797399999),(7980,'Exceso provis.valores neg. a corto emp. grupo',0,0,0,0,798000000,798099999),(7981,'Exceso provis.valores neg. a corto emp. asoc.',0,0,0,0,798100000,798199999),(7983,'Exceso provis.valores neg. a corto otras emp.',0,0,0,0,798300000,798399999),(7990,'Exceso provis.insolv. créditos a corto emp. grupo',0,0,0,0,799000000,799099999),(7991,'Exceso provis.insolv. créditos a corto emp. asoc.',0,0,0,0,799100000,799199999),(7993,'Exceso provis.insolv. créditos a corto otras emp.',0,0,0,0,799300000,799399999);
/*!40000 ALTER TABLE `cuentas` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `datosempresa`
--

DROP TABLE IF EXISTS `datosempresa`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `datosempresa` (
  `Codigo` int DEFAULT NULL,
  `Nombre` text COLLATE utf8mb4_unicode_ci,
  `Direcci_n` text COLLATE utf8mb4_unicode_ci,
  `Poblaci_n` text COLLATE utf8mb4_unicode_ci,
  `Prov_ncia` text COLLATE utf8mb4_unicode_ci,
  `CP` text COLLATE utf8mb4_unicode_ci,
  `CIF` text COLLATE utf8mb4_unicode_ci,
  `SufijoClienteOrdenante` text COLLATE utf8mb4_unicode_ci,
  `SufijoClientePresentador` text COLLATE utf8mb4_unicode_ci,
  `Directorio` text COLLATE utf8mb4_unicode_ci,
  `DirectorioEA` int DEFAULT NULL,
  `Defecto` text COLLATE utf8mb4_unicode_ci,
  `CodigoImpPed` int DEFAULT NULL,
  `CodigoImpAlb` int DEFAULT NULL,
  `CodigoImpFac` int DEFAULT NULL,
  `CodigoImpRec` double DEFAULT NULL,
  `CodigoImpPago` int DEFAULT NULL,
  `CodigoImpPresupuesto` int DEFAULT NULL,
  `VentasIngresoFin` int DEFAULT NULL,
  `VentasGastoFin` int DEFAULT NULL,
  `ComprasIngresoFin` int DEFAULT NULL,
  `ComprasGastoFin` int DEFAULT NULL,
  `DevolucionVentas` int DEFAULT NULL,
  `DevolucionCompras` int DEFAULT NULL,
  `Vigilar_stock` text COLLATE utf8mb4_unicode_ci,
  `FechaUltimoInventario` text COLLATE utf8mb4_unicode_ci,
  `TipoValoracion` int DEFAULT NULL,
  `Moneda` int DEFAULT NULL,
  `DecimalesPrecios` int DEFAULT NULL,
  `DecimalesCantidad` int DEFAULT NULL,
  `DecimalesCantidadPresentes` text COLLATE utf8mb4_unicode_ci,
  `DecimalesPreciosPresentes` text COLLATE utf8mb4_unicode_ci,
  `CabListado` text COLLATE utf8mb4_unicode_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `datosempresa`
--

LOCK TABLES `datosempresa` WRITE;
/*!40000 ALTER TABLE `datosempresa` DISABLE KEYS */;
INSERT INTO `datosempresa` VALUES (18,'GRUPO BABO, S.Coop.V.L. 2025','ARMADA ESPAÑOLA, P.2 Nº23','EL ALTET','ALICANTE','03195','F54059985','','','C:Documents and SettingsMamenMy DocumentsPC FUJITSU SIEMENS Panaderia oct2024GTELITE',17,'0',1,1,1,1,1,1,769000000,665000000,765000000,669000000,708000000,608000000,'0','',0,1,2,2,'1','1','0'),(18,'GRUPO BABO, S.Coop.V.L. 2025','ARMADA ESPAÑOLA, P.2 Nº23','EL ALTET','ALICANTE','03195','F54059985','','','C:Documents and SettingsMamenMy DocumentsPC FUJITSU SIEMENS Panaderia oct2024GTELITE',17,'0',1,1,1,1,1,1,769000000,665000000,765000000,669000000,708000000,608000000,'0','',0,1,2,2,'1','1','0');
/*!40000 ALTER TABLE `datosempresa` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `direccionesenvio`
--

DROP TABLE IF EXISTS `direccionesenvio`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `direccionesenvio` (
  `CodigoCliente` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `CodigoDireccion` int NOT NULL,
  `Nombre` text COLLATE utf8mb4_unicode_ci,
  `Direccion` text COLLATE utf8mb4_unicode_ci,
  `Direccion2` text COLLATE utf8mb4_unicode_ci,
  `Poblacion` text COLLATE utf8mb4_unicode_ci,
  `Provincia` text COLLATE utf8mb4_unicode_ci,
  `CP` text COLLATE utf8mb4_unicode_ci,
  `Telefono` text COLLATE utf8mb4_unicode_ci,
  `Notas` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`CodigoCliente`,`CodigoDireccion`),
  CONSTRAINT `fk_direccion_cliente` FOREIGN KEY (`CodigoCliente`) REFERENCES `clientes` (`Codigo`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `direccionesenvio`
--

LOCK TABLES `direccionesenvio` WRITE;
/*!40000 ALTER TABLE `direccionesenvio` DISABLE KEYS */;
/*!40000 ALTER TABLE `direccionesenvio` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `facturascompra`
--

DROP TABLE IF EXISTS `facturascompra`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `facturascompra` (
  `NumFactura` int NOT NULL,
  `NumAsiento` int DEFAULT NULL,
  `NumIVA` int DEFAULT NULL,
  `Fecha` text COLLATE utf8mb4_unicode_ci,
  `IVASN` text COLLATE utf8mb4_unicode_ci,
  `REQSN` text COLLATE utf8mb4_unicode_ci,
  `CodigoProveedor` text COLLATE utf8mb4_unicode_ci,
  `FormaPago` text COLLATE utf8mb4_unicode_ci,
  `Observaciones` text COLLATE utf8mb4_unicode_ci,
  `Sumadesglose` double DEFAULT NULL,
  `_DescuentoPP` double DEFAULT NULL,
  `Descuentopp` double DEFAULT NULL,
  `SumaIVA` double DEFAULT NULL,
  `SumaREQ` double DEFAULT NULL,
  `TotalFactura` double DEFAULT NULL,
  `Agente` text COLLATE utf8mb4_unicode_ci,
  `_Comision` double DEFAULT NULL,
  `Importe_Comision` double DEFAULT NULL,
  `BaseIVA1` double DEFAULT NULL,
  `IVA1` double DEFAULT NULL,
  `REQ1` double DEFAULT NULL,
  `ImporteIVA1` double DEFAULT NULL,
  `BaseIVA2` double DEFAULT NULL,
  `IVA2` double DEFAULT NULL,
  `REQ2` double DEFAULT NULL,
  `ImporteIVA2` double DEFAULT NULL,
  `BaseIVA3` double DEFAULT NULL,
  `IVA3` double DEFAULT NULL,
  `REQ3` double DEFAULT NULL,
  `ImporteIVA3` double DEFAULT NULL,
  `Referencia` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`NumFactura`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `facturascompra`
--

LOCK TABLES `facturascompra` WRITE;
/*!40000 ALTER TABLE `facturascompra` DISABLE KEYS */;
/*!40000 ALTER TABLE `facturascompra` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `facturascompradesglose`
--

DROP TABLE IF EXISTS `facturascompradesglose`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `facturascompradesglose` (
  `NumFactura` int NOT NULL,
  `NumLinea` int NOT NULL,
  `CodigoArticulo` text COLLATE utf8mb4_unicode_ci,
  `DescripcionArticulo` text COLLATE utf8mb4_unicode_ci,
  `Cantidad` double DEFAULT NULL,
  `PVP` double DEFAULT NULL,
  `Descuento` double DEFAULT NULL,
  `PrecioUnitario` double DEFAULT NULL,
  `Subtotal` double DEFAULT NULL,
  `TIVA` int DEFAULT NULL,
  `NumAlbaran` int DEFAULT NULL,
  `Fecha` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`NumFactura`,`NumLinea`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `facturascompradesglose`
--

LOCK TABLES `facturascompradesglose` WRITE;
/*!40000 ALTER TABLE `facturascompradesglose` DISABLE KEYS */;
/*!40000 ALTER TABLE `facturascompradesglose` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `facturasventa`
--

DROP TABLE IF EXISTS `facturasventa`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `facturasventa` (
  `NumFactura` int NOT NULL,
  `NumAsiento` int DEFAULT NULL,
  `NumIVA` int DEFAULT NULL,
  `Fecha` text COLLATE utf8mb4_unicode_ci,
  `IVASN` text COLLATE utf8mb4_unicode_ci,
  `REQSN` text COLLATE utf8mb4_unicode_ci,
  `CodigoCliente` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `FormaPago` text COLLATE utf8mb4_unicode_ci,
  `Observaciones` text COLLATE utf8mb4_unicode_ci,
  `Sumadesglose` double DEFAULT NULL,
  `_DescuentoPP` double DEFAULT NULL,
  `Descuentopp` double DEFAULT NULL,
  `SumaIVA` double DEFAULT NULL,
  `SumaREQ` double DEFAULT NULL,
  `TotalFactura` double DEFAULT NULL,
  `Agente` text COLLATE utf8mb4_unicode_ci,
  `_Comision` double DEFAULT NULL,
  `Importe_Comision` double DEFAULT NULL,
  `BaseIVA1` double DEFAULT NULL,
  `IVA1` double DEFAULT NULL,
  `REQ1` double DEFAULT NULL,
  `ImporteIVA1` double DEFAULT NULL,
  `BaseIVA2` double DEFAULT NULL,
  `IVA2` double DEFAULT NULL,
  `REQ2` double DEFAULT NULL,
  `ImporteIVA2` double DEFAULT NULL,
  `BaseIVA3` double DEFAULT NULL,
  `IVA3` double DEFAULT NULL,
  `REQ3` double DEFAULT NULL,
  `ImporteIVA3` double DEFAULT NULL,
  `Referencia` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`NumFactura`),
  KEY `idx_facturasventa_codigo_cliente` (`CodigoCliente`),
  CONSTRAINT `fk_facturasventa_cliente` FOREIGN KEY (`CodigoCliente`) REFERENCES `clientes` (`Codigo`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `facturasventa`
--

LOCK TABLES `facturasventa` WRITE;
/*!40000 ALTER TABLE `facturasventa` DISABLE KEYS */;
/*!40000 ALTER TABLE `facturasventa` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `facturasventadesglose`
--

DROP TABLE IF EXISTS `facturasventadesglose`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `facturasventadesglose` (
  `NumFactura` int NOT NULL,
  `NumLinea` int NOT NULL,
  `CodigoArticulo` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `DescripcionArticulo` text COLLATE utf8mb4_unicode_ci,
  `Cantidad` double DEFAULT NULL,
  `PVP` double DEFAULT NULL,
  `Descuento` double DEFAULT NULL,
  `PrecioUnitario` double DEFAULT NULL,
  `Subtotal` double DEFAULT NULL,
  `TIVA` int DEFAULT NULL,
  `NumAlbaran` int DEFAULT NULL,
  `Fecha` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`NumFactura`,`NumLinea`),
  KEY `idx_fvdesg_codigo_art` (`CodigoArticulo`),
  CONSTRAINT `fk_fvdesg_articulo` FOREIGN KEY (`CodigoArticulo`) REFERENCES `articulos` (`CodigoArticulo`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `facturasventadesglose`
--

LOCK TABLES `facturasventadesglose` WRITE;
/*!40000 ALTER TABLE `facturasventadesglose` DISABLE KEYS */;
/*!40000 ALTER TABLE `facturasventadesglose` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `familias`
--

DROP TABLE IF EXISTS `familias`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `familias` (
  `CodigoFamilia` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `DescripcionFamilia` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`CodigoFamilia`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `familias`
--

LOCK TABLES `familias` WRITE;
/*!40000 ALTER TABLE `familias` DISABLE KEYS */;
INSERT INTO `familias` VALUES ('01','PAN COMUN'),('02','PAN CASERO'),('03','PAN ESPECIAL'),('04','BOLLERIA DULCE'),('05','BOLLERIA SALADA'),('06','PASTELERIA'),('07','PASTAS Y ROLLOS'),('08','BEBIDAS Y SNACKS'),('09','SERVICIOS ESPECIALES'),('10','MATERIAS PRIMAS Y ENVASES');
/*!40000 ALTER TABLE `familias` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `formasdepago`
--

DROP TABLE IF EXISTS `formasdepago`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `formasdepago` (
  `CodigoFormaPago` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `Descripcion` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `formasdepago`
--

LOCK TABLES `formasdepago` WRITE;
/*!40000 ALTER TABLE `formasdepago` DISABLE KEYS */;
INSERT INTO `formasdepago` VALUES ('30DIAS','A 30 días'),('60DIAS','A 60 días'),('90DIAS','A 90 días'),('CONTADO','Al contado'),('30DIAS','A 30 días'),('60DIAS','A 60 días'),('90DIAS','A 90 días'),('CONTADO','Al contado');
/*!40000 ALTER TABLE `formasdepago` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `formasdepagodesglose`
--

DROP TABLE IF EXISTS `formasdepagodesglose`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `formasdepagodesglose` (
  `CodigoFormaPago` text COLLATE utf8mb4_unicode_ci,
  `D_asFechaFactura` int DEFAULT NULL,
  `D_adePago` int DEFAULT NULL,
  `PorcentajeFactura` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `formasdepagodesglose`
--

LOCK TABLES `formasdepagodesglose` WRITE;
/*!40000 ALTER TABLE `formasdepagodesglose` DISABLE KEYS */;
INSERT INTO `formasdepagodesglose` VALUES ('CONTADO',0,0,100),('30DIAS',30,0,100),('60DIAS',60,0,100),('90DIAS',90,0,100),('CONTADO',0,0,100),('30DIAS',30,0,100),('60DIAS',60,0,100),('90DIAS',90,0,100);
/*!40000 ALTER TABLE `formasdepagodesglose` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ivarep`
--

DROP TABLE IF EXISTS `ivarep`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ivarep` (
  `NumFactura` int DEFAULT NULL,
  `TipoIVA` int DEFAULT NULL,
  `Documento` text COLLATE utf8mb4_unicode_ci,
  `CodigoEmpresa` text COLLATE utf8mb4_unicode_ci,
  `Fecha` text COLLATE utf8mb4_unicode_ci,
  `BaseImponible` double DEFAULT NULL,
  `IVA_` double DEFAULT NULL,
  `CuotaIVA` double DEFAULT NULL,
  `REQ_` double DEFAULT NULL,
  `CuotaREQ` double DEFAULT NULL,
  `ImporteIVA` double DEFAULT NULL,
  `Total_Factura` double DEFAULT NULL,
  `UINumFact` int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ivarep`
--

LOCK TABLES `ivarep` WRITE;
/*!40000 ALTER TABLE `ivarep` DISABLE KEYS */;
/*!40000 ALTER TABLE `ivarep` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ivasop`
--

DROP TABLE IF EXISTS `ivasop`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ivasop` (
  `NumFactura` int DEFAULT NULL,
  `TipoIVA` int DEFAULT NULL,
  `Documento` text COLLATE utf8mb4_unicode_ci,
  `CodigoEmpresa` text COLLATE utf8mb4_unicode_ci,
  `Fecha` text COLLATE utf8mb4_unicode_ci,
  `BaseImponible` double DEFAULT NULL,
  `IVA_` double DEFAULT NULL,
  `CuotaIVA` double DEFAULT NULL,
  `REQ_` double DEFAULT NULL,
  `CuotaREQ` double DEFAULT NULL,
  `ImporteIVA` double DEFAULT NULL,
  `Total_Factura` double DEFAULT NULL,
  `UINumFact` int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ivasop`
--

LOCK TABLES `ivasop` WRITE;
/*!40000 ALTER TABLE `ivasop` DISABLE KEYS */;
/*!40000 ALTER TABLE `ivasop` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ordenesproduccion`
--

DROP TABLE IF EXISTS `ordenesproduccion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ordenesproduccion` (
  `NumOrden` int DEFAULT NULL,
  `Articulo` text COLLATE utf8mb4_unicode_ci,
  `Cantidad` double DEFAULT NULL,
  `Almacen` text COLLATE utf8mb4_unicode_ci,
  `FechaInicioPrevista` text COLLATE utf8mb4_unicode_ci,
  `FechaFinPrevista` text COLLATE utf8mb4_unicode_ci,
  `Notas` text COLLATE utf8mb4_unicode_ci,
  `Coste` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ordenesproduccion`
--

LOCK TABLES `ordenesproduccion` WRITE;
/*!40000 ALTER TABLE `ordenesproduccion` DISABLE KEYS */;
/*!40000 ALTER TABLE `ordenesproduccion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ordenesproducciondesglose`
--

DROP TABLE IF EXISTS `ordenesproducciondesglose`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ordenesproducciondesglose` (
  `NumOrden` int DEFAULT NULL,
  `NumLinea` int DEFAULT NULL,
  `CodigoArticulo` text COLLATE utf8mb4_unicode_ci,
  `DescripcionArticulo` text COLLATE utf8mb4_unicode_ci,
  `Cantidad` double DEFAULT NULL,
  `Almacen` text COLLATE utf8mb4_unicode_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ordenesproducciondesglose`
--

LOCK TABLES `ordenesproducciondesglose` WRITE;
/*!40000 ALTER TABLE `ordenesproducciondesglose` DISABLE KEYS */;
/*!40000 ALTER TABLE `ordenesproducciondesglose` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pedidoscompra`
--

DROP TABLE IF EXISTS `pedidoscompra`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pedidoscompra` (
  `NumPedido` int DEFAULT NULL,
  `Fecha` text COLLATE utf8mb4_unicode_ci,
  `CodigoProveedor` text COLLATE utf8mb4_unicode_ci,
  `Observaciones` text COLLATE utf8mb4_unicode_ci,
  `Servido` text COLLATE utf8mb4_unicode_ci,
  `FechaEntrega` text COLLATE utf8mb4_unicode_ci,
  `IVASN` text COLLATE utf8mb4_unicode_ci,
  `REQSN` text COLLATE utf8mb4_unicode_ci,
  `Sumadesglose` double DEFAULT NULL,
  `_DescuentoPP` double DEFAULT NULL,
  `Descuentopp` double DEFAULT NULL,
  `SumaIVA` double DEFAULT NULL,
  `SumaREQ` double DEFAULT NULL,
  `TotalFactura` double DEFAULT NULL,
  `BaseIVA1` double DEFAULT NULL,
  `IVA1` double DEFAULT NULL,
  `REQ1` double DEFAULT NULL,
  `ImporteIVA1` double DEFAULT NULL,
  `BaseIVA2` double DEFAULT NULL,
  `IVA2` double DEFAULT NULL,
  `REQ2` double DEFAULT NULL,
  `ImporteIVA2` double DEFAULT NULL,
  `BaseIVA3` double DEFAULT NULL,
  `IVA3` double DEFAULT NULL,
  `REQ3` double DEFAULT NULL,
  `ImporteIVA3` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pedidoscompra`
--

LOCK TABLES `pedidoscompra` WRITE;
/*!40000 ALTER TABLE `pedidoscompra` DISABLE KEYS */;
/*!40000 ALTER TABLE `pedidoscompra` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pedidoscompradesglose`
--

DROP TABLE IF EXISTS `pedidoscompradesglose`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pedidoscompradesglose` (
  `NumPedido` int DEFAULT NULL,
  `Numlinea` int DEFAULT NULL,
  `CodigoArticulo` text COLLATE utf8mb4_unicode_ci,
  `DescripcionArticulo` text COLLATE utf8mb4_unicode_ci,
  `Cantidad` double DEFAULT NULL,
  `PVP` double DEFAULT NULL,
  `Descuento` double DEFAULT NULL,
  `PrecioUnitario` double DEFAULT NULL,
  `Subtotal` double DEFAULT NULL,
  `TIVA` int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pedidoscompradesglose`
--

LOCK TABLES `pedidoscompradesglose` WRITE;
/*!40000 ALTER TABLE `pedidoscompradesglose` DISABLE KEYS */;
/*!40000 ALTER TABLE `pedidoscompradesglose` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pedidosventa`
--

DROP TABLE IF EXISTS `pedidosventa`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pedidosventa` (
  `NumPedido` int NOT NULL,
  `Fecha` text COLLATE utf8mb4_unicode_ci,
  `CodigoCliente` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Observaciones` text COLLATE utf8mb4_unicode_ci,
  `Servido` text COLLATE utf8mb4_unicode_ci,
  `FechaEntrega` text COLLATE utf8mb4_unicode_ci,
  `NumPresupuesto` text COLLATE utf8mb4_unicode_ci,
  `IVASN` text COLLATE utf8mb4_unicode_ci,
  `REQSN` text COLLATE utf8mb4_unicode_ci,
  `Sumadesglose` text COLLATE utf8mb4_unicode_ci,
  `_DescuentoPP` text COLLATE utf8mb4_unicode_ci,
  `Descuentopp` text COLLATE utf8mb4_unicode_ci,
  `SumaIVA` text COLLATE utf8mb4_unicode_ci,
  `SumaREQ` text COLLATE utf8mb4_unicode_ci,
  `TotalFactura` text COLLATE utf8mb4_unicode_ci,
  `BaseIVA1` text COLLATE utf8mb4_unicode_ci,
  `IVA1` text COLLATE utf8mb4_unicode_ci,
  `REQ1` text COLLATE utf8mb4_unicode_ci,
  `ImporteIVA1` text COLLATE utf8mb4_unicode_ci,
  `BaseIVA2` text COLLATE utf8mb4_unicode_ci,
  `IVA2` text COLLATE utf8mb4_unicode_ci,
  `REQ2` text COLLATE utf8mb4_unicode_ci,
  `ImporteIVA2` text COLLATE utf8mb4_unicode_ci,
  `BaseIVA3` text COLLATE utf8mb4_unicode_ci,
  `IVA3` text COLLATE utf8mb4_unicode_ci,
  `REQ3` text COLLATE utf8mb4_unicode_ci,
  `ImporteIVA3` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`NumPedido`),
  KEY `idx_pedidosventa_codigo_cliente` (`CodigoCliente`),
  CONSTRAINT `fk_pedidosventa_cliente` FOREIGN KEY (`CodigoCliente`) REFERENCES `clientes` (`Codigo`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pedidosventa`
--

LOCK TABLES `pedidosventa` WRITE;
/*!40000 ALTER TABLE `pedidosventa` DISABLE KEYS */;
/*!40000 ALTER TABLE `pedidosventa` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pedidosventadesglose`
--

DROP TABLE IF EXISTS `pedidosventadesglose`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pedidosventadesglose` (
  `NumPedido` int NOT NULL,
  `Numlinea` int NOT NULL,
  `CodigoArticulo` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `DescripcionArticulo` text COLLATE utf8mb4_unicode_ci,
  `Cantidad` double DEFAULT NULL,
  `PVP` double DEFAULT NULL,
  `Descuento` double DEFAULT NULL,
  `PrecioUnitario` double DEFAULT NULL,
  `Subtotal` double DEFAULT NULL,
  `TIVA` int DEFAULT NULL,
  PRIMARY KEY (`NumPedido`,`Numlinea`),
  KEY `idx_pvdesg_codigo_art` (`CodigoArticulo`),
  CONSTRAINT `fk_pvdesg_articulo` FOREIGN KEY (`CodigoArticulo`) REFERENCES `articulos` (`CodigoArticulo`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pedidosventadesglose`
--

LOCK TABLES `pedidosventadesglose` WRITE;
/*!40000 ALTER TABLE `pedidosventadesglose` DISABLE KEYS */;
/*!40000 ALTER TABLE `pedidosventadesglose` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `presupuestos`
--

DROP TABLE IF EXISTS `presupuestos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `presupuestos` (
  `NumPresupuesto` int DEFAULT NULL,
  `Fecha` text COLLATE utf8mb4_unicode_ci,
  `IVASN` text COLLATE utf8mb4_unicode_ci,
  `REQSN` text COLLATE utf8mb4_unicode_ci,
  `Observaciones` text COLLATE utf8mb4_unicode_ci,
  `Sumadesglose` double DEFAULT NULL,
  `_DescuentoPP` double DEFAULT NULL,
  `Descuentopp` double DEFAULT NULL,
  `SumaIVA` double DEFAULT NULL,
  `SumaREQ` double DEFAULT NULL,
  `TotalPresupuesto` double DEFAULT NULL,
  `Agente` text COLLATE utf8mb4_unicode_ci,
  `BaseIVA1` double DEFAULT NULL,
  `IVA1` double DEFAULT NULL,
  `REQ1` double DEFAULT NULL,
  `ImporteIVA1` double DEFAULT NULL,
  `BaseIVA2` double DEFAULT NULL,
  `IVA2` double DEFAULT NULL,
  `REQ2` double DEFAULT NULL,
  `ImporteIVA2` double DEFAULT NULL,
  `BaseIVA3` double DEFAULT NULL,
  `IVA3` double DEFAULT NULL,
  `REQ3` double DEFAULT NULL,
  `ImporteIVA3` double DEFAULT NULL,
  `Aceptado` text COLLATE utf8mb4_unicode_ci,
  `CodigoCliente` text COLLATE utf8mb4_unicode_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `presupuestos`
--

LOCK TABLES `presupuestos` WRITE;
/*!40000 ALTER TABLE `presupuestos` DISABLE KEYS */;
/*!40000 ALTER TABLE `presupuestos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `presupuestosdesglose`
--

DROP TABLE IF EXISTS `presupuestosdesglose`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `presupuestosdesglose` (
  `NumPresupuesto` int DEFAULT NULL,
  `NumLinea` int DEFAULT NULL,
  `DescripcionArticulo` text COLLATE utf8mb4_unicode_ci,
  `Cantidad` double DEFAULT NULL,
  `PVP` double DEFAULT NULL,
  `Descuento` double DEFAULT NULL,
  `PrecioUnitario` double DEFAULT NULL,
  `Subtotal` double DEFAULT NULL,
  `TIVA` int DEFAULT NULL,
  `CodigoArticulo` text COLLATE utf8mb4_unicode_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `presupuestosdesglose`
--

LOCK TABLES `presupuestosdesglose` WRITE;
/*!40000 ALTER TABLE `presupuestosdesglose` DISABLE KEYS */;
/*!40000 ALTER TABLE `presupuestosdesglose` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `proveedores`
--

DROP TABLE IF EXISTS `proveedores`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `proveedores` (
  `Codigo` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `CodigoDeReferencia` text COLLATE utf8mb4_unicode_ci,
  `Nombre` text COLLATE utf8mb4_unicode_ci,
  `Domicilio` text COLLATE utf8mb4_unicode_ci,
  `Domicilio2` text COLLATE utf8mb4_unicode_ci,
  `Poblacion` text COLLATE utf8mb4_unicode_ci,
  `CodigoPostal` text COLLATE utf8mb4_unicode_ci,
  `Provincia` text COLLATE utf8mb4_unicode_ci,
  `PersonaContacto` text COLLATE utf8mb4_unicode_ci,
  `Telefono` text COLLATE utf8mb4_unicode_ci,
  `Fax` text COLLATE utf8mb4_unicode_ci,
  `CIF` text COLLATE utf8mb4_unicode_ci,
  `IVASN` text COLLATE utf8mb4_unicode_ci,
  `REQSN` text COLLATE utf8mb4_unicode_ci,
  `SubcuentaCliente` int DEFAULT NULL,
  `SubcuentaProveedor` int DEFAULT NULL,
  `TipoDescuento` int DEFAULT NULL,
  `Tarifa` int DEFAULT NULL,
  `SectoresActividad` text COLLATE utf8mb4_unicode_ci,
  `FormaPago` text COLLATE utf8mb4_unicode_ci,
  `Agente` text COLLATE utf8mb4_unicode_ci,
  `Riesgo` double DEFAULT NULL,
  `Banco` text COLLATE utf8mb4_unicode_ci,
  `DireccionBanco` text COLLATE utf8mb4_unicode_ci,
  `PoblacionBanco` text COLLATE utf8mb4_unicode_ci,
  `ProvinciaBanco` text COLLATE utf8mb4_unicode_ci,
  `AgenciaBanco` text COLLATE utf8mb4_unicode_ci,
  `CuentaBanco` text COLLATE utf8mb4_unicode_ci,
  `Notas` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`Codigo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `proveedores`
--

LOCK TABLES `proveedores` WRITE;
/*!40000 ALTER TABLE `proveedores` DISABLE KEYS */;
/*!40000 ALTER TABLE `proveedores` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `provincias`
--

DROP TABLE IF EXISTS `provincias`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `provincias` (
  `CodigoProvincia` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `NombreProvincia` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Zona` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `provincias`
--

LOCK TABLES `provincias` WRITE;
/*!40000 ALTER TABLE `provincias` DISABLE KEYS */;
INSERT INTO `provincias` VALUES ('01','ALAVA','PAV'),('02','ALBACETE','CAM'),('03','ALICANTE','VAL'),('04','ALMERIA','AND'),('05','AVILA','CAL'),('06','BADAJOZ','EXT'),('07','BALEARES','BAL'),('08','BARCELONA','CAT'),('09','BURGOS','CAL'),('10','CACERES','EXT'),('11','CADIZ','AND'),('12','CASTELLON','VAL'),('13','CIUDAD DECIMAL(15,2)','CAM'),('14','CORDOBA','AND'),('15','LA CORUÑA','GAL'),('16','CUENCA','CAM'),('17','GERONA','CAT'),('18','GRANADA','AND'),('19','GUADALAJARA','CAM'),('20','GUIPUZCOA','PAV'),('21','HUELVA','AND'),('22','HUESCA','ARA'),('23','JAEN','AND'),('24','LEON','CAL'),('25','LERIDA','CAT'),('26','LOGROÑO','RIO'),('27','LUGO','GAL'),('28','MADRID','MAD'),('29','MALAGA','AND'),('30','MURCIA','MUR'),('31','NAVARRA','NAV'),('32','ORENSE','GAL'),('33','ASTURIAS','AST'),('34','PALENCIA','CAL'),('35','LAS PALMAS','CAN'),('36','PONTEVEDRA','GAL'),('37','SALAMANCA','CAL'),('38','SANTA CRUZ DE TENERIFE','CAN'),('39','SANTANDER','CANT'),('40','SEGOVIA','CAL'),('41','SEVILLA','AND'),('42','SORIA','CAL'),('43','TARRAGONA','CAT'),('44','TERUEL','ARA'),('45','TOLEDO','CAM'),('46','VALENCIA','VAL'),('47','VALLADOLID','CAL'),('48','VIZCAYA','PAV'),('49','ZAMORA','CAL'),('50','ZARAGOZA','ARA'),('01','ALAVA','PAV'),('02','ALBACETE','CAM'),('03','ALICANTE','VAL'),('04','ALMERIA','AND'),('05','AVILA','CAL'),('06','BADAJOZ','EXT'),('07','BALEARES','BAL'),('08','BARCELONA','CAT'),('09','BURGOS','CAL'),('10','CACERES','EXT'),('11','CADIZ','AND'),('12','CASTELLON','VAL'),('13','CIUDAD DECIMAL(15,2)','CAM'),('14','CORDOBA','AND'),('15','LA CORUÑA','GAL'),('16','CUENCA','CAM'),('17','GERONA','CAT'),('18','GRANADA','AND'),('19','GUADALAJARA','CAM'),('20','GUIPUZCOA','PAV'),('21','HUELVA','AND'),('22','HUESCA','ARA'),('23','JAEN','AND'),('24','LEON','CAL'),('25','LERIDA','CAT'),('26','LOGROÑO','RIO'),('27','LUGO','GAL'),('28','MADRID','MAD'),('29','MALAGA','AND'),('30','MURCIA','MUR'),('31','NAVARRA','NAV'),('32','ORENSE','GAL'),('33','ASTURIAS','AST'),('34','PALENCIA','CAL'),('35','LAS PALMAS','CAN'),('36','PONTEVEDRA','GAL'),('37','SALAMANCA','CAL'),('38','SANTA CRUZ DE TENERIFE','CAN'),('39','SANTANDER','CANT'),('40','SEGOVIA','CAL'),('41','SEVILLA','AND'),('42','SORIA','CAL'),('43','TARRAGONA','CAT'),('44','TERUEL','ARA'),('45','TOLEDO','CAM'),('46','VALENCIA','VAL'),('47','VALLADOLID','CAL'),('48','VIZCAYA','PAV'),('49','ZAMORA','CAL'),('50','ZARAGOZA','ARA');
/*!40000 ALTER TABLE `provincias` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `querysaldos`
--

DROP TABLE IF EXISTS `querysaldos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `querysaldos` (
  `Subcuenta` int DEFAULT NULL,
  `SumaDebe` double DEFAULT NULL,
  `SumaHaber` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `querysaldos`
--

LOCK TABLES `querysaldos` WRITE;
/*!40000 ALTER TABLE `querysaldos` DISABLE KEYS */;
/*!40000 ALTER TABLE `querysaldos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sectores`
--

DROP TABLE IF EXISTS `sectores`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sectores` (
  `CodigoSector` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `DescripcionSector` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sectores`
--

LOCK TABLES `sectores` WRITE;
/*!40000 ALTER TABLE `sectores` DISABLE KEYS */;
/*!40000 ALTER TABLE `sectores` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stockmovimientos`
--

DROP TABLE IF EXISTS `stockmovimientos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stockmovimientos` (
  `Codigo` int NOT NULL,
  `Fecha` text COLLATE utf8mb4_unicode_ci,
  `CodigoArticulo` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Cantidad` double DEFAULT NULL,
  `AlmacenOrigen` text COLLATE utf8mb4_unicode_ci,
  `AlmacenDestino` text COLLATE utf8mb4_unicode_ci,
  `Observaciones` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`Codigo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stockmovimientos`
--

LOCK TABLES `stockmovimientos` WRITE;
/*!40000 ALTER TABLE `stockmovimientos` DISABLE KEYS */;
/*!40000 ALTER TABLE `stockmovimientos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stockregularizaciones`
--

DROP TABLE IF EXISTS `stockregularizaciones`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stockregularizaciones` (
  `Codigo` int NOT NULL,
  `Fecha` text COLLATE utf8mb4_unicode_ci,
  `CodigoArticulo` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Cantidad` double DEFAULT NULL,
  `Almacen` text COLLATE utf8mb4_unicode_ci,
  `Observaciones` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`Codigo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stockregularizaciones`
--

LOCK TABLES `stockregularizaciones` WRITE;
/*!40000 ALTER TABLE `stockregularizaciones` DISABLE KEYS */;
/*!40000 ALTER TABLE `stockregularizaciones` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subcuentas`
--

DROP TABLE IF EXISTS `subcuentas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subcuentas` (
  `Codigosubcuenta` int NOT NULL,
  `TITULO` text COLLATE utf8mb4_unicode_ci,
  `SaldoDebe` double DEFAULT NULL,
  `SaldoHaber` double DEFAULT NULL,
  `SaldoDebe2` double DEFAULT NULL,
  `SaldoHaber2` double DEFAULT NULL,
  PRIMARY KEY (`Codigosubcuenta`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subcuentas`
--

LOCK TABLES `subcuentas` WRITE;
/*!40000 ALTER TABLE `subcuentas` DISABLE KEYS */;
INSERT INTO `subcuentas` VALUES (100000000,'Capital social',0,0,0,0),(129000000,'Pérdidas y ganancias',0,0,0,0),(200000000,'Gastos de establecimiento',0,0,0,0),(210000000,'Inmovilizado inmaterial',0,0,0,0),(217000000,'Derechos s/bienes en régimen arrendamiento financ.',0,0,0,0),(221000000,'Inmovilizado material, construcciones',0,0,0,0),(223000000,'Inmovilizado material, maquinaria',0,0,0,0),(226000000,'Inmovilizado material, mobiliario',0,0,0,0),(228000000,'Inmovilizado material, elementos de transporte',0,0,0,0),(260000000,'Fianzas y depósitos constituidos a largo plazo',0,0,0,0),(272000000,'Gastos por intereses diferidos',0,0,0,0),(281700000,'Amort.acum. derechos s/bienes arrendam. financ.',0,0,0,0),(282100000,'Amortización acumulada de construcciones',0,0,0,0),(282300000,'Amortización acumulada de maquinaria',0,0,0,0),(282600000,'Amortización acumulada de mobiliario',0,0,0,0),(282800000,'Amortización acumulada de elementos de transporte',0,0,0,0),(300000000,'Mercaderías',0,0,0,0),(400000000,'Proveedores varios',0,0,0,0),(401000000,'Efectos comerciales a pagar, proveedores',0,0,0,0),(410000000,'Acreedores por prestación de servicios',0,0,0,0),(430000000,'Clientes varios',0,0,0,0),(431000000,'Efectos comerciales a cobrar, clientes',0,0,0,0),(470000000,'Hacienda Pública, deudor por IVA',0,0,0,0),(471000000,'Seguridad Social deudora',0,0,0,0),(472000004,'IVA soportado 4%',0,0,0,0),(472000007,'IVA soportado 7%',0,0,0,0),(472000016,'IVA soportado 16%',0,0,0,0),(473000000,'Hacienda Pública, retenciones y pagos a cuenta',0,0,0,0),(475000000,'Hacienda Pública, acreedor por IVA',0,0,0,0),(476000000,'Seguridad Social, acreedor',0,0,0,0),(477000004,'IVA repercutido 4%',0,0,0,0),(477000007,'IVA repercutido 7%',0,0,0,0),(477000016,'IVA repercutido 16%',0,0,0,0),(479000000,'Hacienda pública IVA provisional',0,0,0,0),(487000000,'Hacienda pública IVA soportado diferido',0,0,0,0),(570000000,'Caja',0,0,0,0),(572000000,'Bancos',0,0,0,0),(600000000,'Compras mercaderías',0,0,0,0),(608000000,'Devolución de compras',0,0,0,0),(610000000,'Variación de existencias',0,0,0,0),(621000000,'Arrendamientos y cánones',0,0,0,0),(622000000,'Reparaciones y conservación',0,0,0,0),(623000001,'Gastos profesionales independientes',0,0,0,0),(624000000,'Transportes varios',0,0,0,0),(625000000,'Primas de seguros',0,0,0,0),(626000000,'Comisiones bancarias',0,0,0,0),(627000000,'Publicidad y relaciones públicas',0,0,0,0),(628000000,'Suministros',0,0,0,0),(629000000,'Gastos varios',0,0,0,0),(630000000,'Impuestos sobre beneficios',0,0,0,0),(631000000,'Otros tributos',0,0,0,0),(640000000,'Sueldos y salarios',0,0,0,0),(642000000,'Seguridad Social a cargo de la empresa',0,0,0,0),(650000000,'Pérdidas de créditos comerciales incobrables',0,0,0,0),(662000000,'Intereses a largo plazo',0,0,0,0),(665000000,'Descuentos sobre ventas de pronto pago',0,0,0,0),(668000000,'Diferencias negativas de cambio',0,0,0,0),(669000000,'Gastos financieros',0,0,0,0),(678000000,'Gastos extraordinarios',0,0,0,0),(681000000,'Amortización inmovilizado inmaterial',0,0,0,0),(682000000,'Amortización inmovilizado material',0,0,0,0),(700000000,'Ventas mercaderías',0,0,0,0),(705000000,'Prestación de servicios',0,0,0,0),(708000000,'Devolución ventas de mercaderías',0,0,0,0),(759000000,'Ingresos por servicios diversos',0,0,0,0),(765000000,'Descuentos sobre compras por pronto pago',0,0,0,0),(768000000,'Diferencias positivas de cambio',0,0,0,0),(769000000,'Ingresos financieros',0,0,0,0),(778000000,'Ingresos extraordinarios',0,0,0,0);
/*!40000 ALTER TABLE `subcuentas` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `suministros`
--

DROP TABLE IF EXISTS `suministros`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `suministros` (
  `Proveedor` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Articulo` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Coste` double DEFAULT NULL,
  `Descuento` double DEFAULT NULL,
  `PlazoEntrega` int DEFAULT NULL,
  KEY `fk_suministros_proveedor` (`Proveedor`),
  KEY `fk_suministros_articulo` (`Articulo`),
  CONSTRAINT `fk_suministros_articulo` FOREIGN KEY (`Articulo`) REFERENCES `articulos` (`CodigoArticulo`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_suministros_proveedor` FOREIGN KEY (`Proveedor`) REFERENCES `proveedores` (`Codigo`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `suministros`
--

LOCK TABLES `suministros` WRITE;
/*!40000 ALTER TABLE `suministros` DISABLE KEYS */;
/*!40000 ALTER TABLE `suministros` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tablaauxiliar`
--

DROP TABLE IF EXISTS `tablaauxiliar`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tablaauxiliar` (
  `CodigoTabla` text COLLATE utf8mb4_unicode_ci,
  `Descripcion` text COLLATE utf8mb4_unicode_ci,
  `DescripcionFilas` text COLLATE utf8mb4_unicode_ci,
  `DescripcionColumnas` text COLLATE utf8mb4_unicode_ci,
  `CodigoArticulo` text COLLATE utf8mb4_unicode_ci,
  `DescripcionArticulo` text COLLATE utf8mb4_unicode_ci,
  `Unidad` text COLLATE utf8mb4_unicode_ci,
  `FamiliaArticulo` text COLLATE utf8mb4_unicode_ci,
  `TipodeIVA` int DEFAULT NULL,
  `SubCuentaVentas` int DEFAULT NULL,
  `SubCuentaCompras` int DEFAULT NULL,
  `UsoInterno` text COLLATE utf8mb4_unicode_ci,
  `Compuesto` text COLLATE utf8mb4_unicode_ci,
  `Servicio` text COLLATE utf8mb4_unicode_ci,
  `CosteArticulo` double DEFAULT NULL,
  `PVP1` double DEFAULT NULL,
  `PVP2` double DEFAULT NULL,
  `PVP3` double DEFAULT NULL,
  `PVP4` double DEFAULT NULL,
  `PVP5` double DEFAULT NULL,
  `MinimoStock` int DEFAULT NULL,
  `MaximoStock` int DEFAULT NULL,
  `MinStockPorAlmacen` int DEFAULT NULL,
  `MaxStockPorAlmacen` int DEFAULT NULL,
  `Descuento1` double DEFAULT NULL,
  `Descuento2` double DEFAULT NULL,
  `Descuento3` double DEFAULT NULL,
  `Descuento4` double DEFAULT NULL,
  `Descuento5` double DEFAULT NULL,
  `ProveedorDefecto` text COLLATE utf8mb4_unicode_ci,
  `DiasFabricacion` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tablaauxiliar`
--

LOCK TABLES `tablaauxiliar` WRITE;
/*!40000 ALTER TABLE `tablaauxiliar` DISABLE KEYS */;
/*!40000 ALTER TABLE `tablaauxiliar` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tablaauxiliarcolumna`
--

DROP TABLE IF EXISTS `tablaauxiliarcolumna`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tablaauxiliarcolumna` (
  `CodigoTabla` text COLLATE utf8mb4_unicode_ci,
  `NumeroColumna` int DEFAULT NULL,
  `Descripcion` text COLLATE utf8mb4_unicode_ci,
  `DigitosGeneracion` text COLLATE utf8mb4_unicode_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tablaauxiliarcolumna`
--

LOCK TABLES `tablaauxiliarcolumna` WRITE;
/*!40000 ALTER TABLE `tablaauxiliarcolumna` DISABLE KEYS */;
/*!40000 ALTER TABLE `tablaauxiliarcolumna` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tablaauxiliarfila`
--

DROP TABLE IF EXISTS `tablaauxiliarfila`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tablaauxiliarfila` (
  `CodigoTabla` text COLLATE utf8mb4_unicode_ci,
  `NumeroFila` int DEFAULT NULL,
  `Descripcion` text COLLATE utf8mb4_unicode_ci,
  `DigitosGeneracion` text COLLATE utf8mb4_unicode_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tablaauxiliarfila`
--

LOCK TABLES `tablaauxiliarfila` WRITE;
/*!40000 ALTER TABLE `tablaauxiliarfila` DISABLE KEYS */;
/*!40000 ALTER TABLE `tablaauxiliarfila` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tablasaldosagrupaciones`
--

DROP TABLE IF EXISTS `tablasaldosagrupaciones`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tablasaldosagrupaciones` (
  `Agrupacion` int DEFAULT NULL,
  `Signo` int DEFAULT NULL,
  `Importe` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tablasaldosagrupaciones`
--

LOCK TABLES `tablasaldosagrupaciones` WRITE;
/*!40000 ALTER TABLE `tablasaldosagrupaciones` DISABLE KEYS */;
/*!40000 ALTER TABLE `tablasaldosagrupaciones` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tiposdeiva`
--

DROP TABLE IF EXISTS `tiposdeiva`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tiposdeiva` (
  `Codigo_Tipo_de_IVA` int NOT NULL,
  `Porcentaje_de_IVA` decimal(15,2) DEFAULT NULL,
  `Porcentaje_de_REQ` decimal(15,2) DEFAULT NULL,
  `SubcuentaIVASoportado` int DEFAULT NULL,
  `SubcuentaIVARepercutido` int DEFAULT NULL,
  `SubcuentaREQRepercutido` int DEFAULT NULL,
  PRIMARY KEY (`Codigo_Tipo_de_IVA`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tiposdeiva`
--

LOCK TABLES `tiposdeiva` WRITE;
/*!40000 ALTER TABLE `tiposdeiva` DISABLE KEYS */;
INSERT INTO `tiposdeiva` VALUES (0,0.00,0.00,0,0,0),(1,2.00,0.26,472000004,477000004,475000000),(2,10.00,1.40,472000007,477000007,475000000),(3,21.00,5.20,472000016,477000016,475000000);
/*!40000 ALTER TABLE `tiposdeiva` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wkalbaranescompra`
--

DROP TABLE IF EXISTS `wkalbaranescompra`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wkalbaranescompra` (
  `NumAlbaran` int DEFAULT NULL,
  `NumLinea` int DEFAULT NULL,
  `CodigoArticulo` text COLLATE utf8mb4_unicode_ci,
  `DescripcionArticulo` text COLLATE utf8mb4_unicode_ci,
  `Cantidad` double DEFAULT NULL,
  `PVP` double DEFAULT NULL,
  `Descuento` double DEFAULT NULL,
  `PrecioUnitario` double DEFAULT NULL,
  `Subtotal` double DEFAULT NULL,
  `TIVA` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wkalbaranescompra`
--

LOCK TABLES `wkalbaranescompra` WRITE;
/*!40000 ALTER TABLE `wkalbaranescompra` DISABLE KEYS */;
/*!40000 ALTER TABLE `wkalbaranescompra` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wkalbaranescomprafacturar`
--

DROP TABLE IF EXISTS `wkalbaranescomprafacturar`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wkalbaranescomprafacturar` (
  `NumAlbaran` int DEFAULT NULL,
  `NumFactura` int DEFAULT NULL,
  `NumPedido` int DEFAULT NULL,
  `Fecha` text COLLATE utf8mb4_unicode_ci,
  `IVASN` text COLLATE utf8mb4_unicode_ci,
  `REQSN` text COLLATE utf8mb4_unicode_ci,
  `CodigoProveedor` text COLLATE utf8mb4_unicode_ci,
  `CodigoAlmacen` text COLLATE utf8mb4_unicode_ci,
  `Observaciones` text COLLATE utf8mb4_unicode_ci,
  `Sumadesglose` double DEFAULT NULL,
  `_DescuentoPP` double DEFAULT NULL,
  `Descuentopp` double DEFAULT NULL,
  `SumaIVA` double DEFAULT NULL,
  `SumaREQ` double DEFAULT NULL,
  `TotalFactura` double DEFAULT NULL,
  `BaseIVA1` double DEFAULT NULL,
  `IVA1` double DEFAULT NULL,
  `REQ1` double DEFAULT NULL,
  `ImporteIVA1` double DEFAULT NULL,
  `BaseIVA2` double DEFAULT NULL,
  `IVA2` double DEFAULT NULL,
  `REQ2` double DEFAULT NULL,
  `ImporteIVA2` double DEFAULT NULL,
  `BaseIVA3` double DEFAULT NULL,
  `IVA3` double DEFAULT NULL,
  `REQ3` double DEFAULT NULL,
  `ImporteIVA3` double DEFAULT NULL,
  `DireccionEnvio` int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wkalbaranescomprafacturar`
--

LOCK TABLES `wkalbaranescomprafacturar` WRITE;
/*!40000 ALTER TABLE `wkalbaranescomprafacturar` DISABLE KEYS */;
/*!40000 ALTER TABLE `wkalbaranescomprafacturar` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wkalbaranescompraotramoneda`
--

DROP TABLE IF EXISTS `wkalbaranescompraotramoneda`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wkalbaranescompraotramoneda` (
  `NumAlbaran` int DEFAULT NULL,
  `NumFactura` int DEFAULT NULL,
  `NumPedido` int DEFAULT NULL,
  `Fecha` text COLLATE utf8mb4_unicode_ci,
  `IVASN` text COLLATE utf8mb4_unicode_ci,
  `REQSN` text COLLATE utf8mb4_unicode_ci,
  `CodigoProveedor` text COLLATE utf8mb4_unicode_ci,
  `CodigoAlmacen` text COLLATE utf8mb4_unicode_ci,
  `Observaciones` text COLLATE utf8mb4_unicode_ci,
  `Sumadesglose` double DEFAULT NULL,
  `_DescuentoPP` double DEFAULT NULL,
  `Descuentopp` double DEFAULT NULL,
  `SumaIVA` double DEFAULT NULL,
  `SumaREQ` double DEFAULT NULL,
  `TotalFactura` double DEFAULT NULL,
  `BaseIVA1` double DEFAULT NULL,
  `IVA1` double DEFAULT NULL,
  `REQ1` double DEFAULT NULL,
  `ImporteIVA1` double DEFAULT NULL,
  `BaseIVA2` double DEFAULT NULL,
  `IVA2` double DEFAULT NULL,
  `REQ2` double DEFAULT NULL,
  `ImporteIVA2` double DEFAULT NULL,
  `BaseIVA3` double DEFAULT NULL,
  `IVA3` double DEFAULT NULL,
  `REQ3` double DEFAULT NULL,
  `ImporteIVA3` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wkalbaranescompraotramoneda`
--

LOCK TABLES `wkalbaranescompraotramoneda` WRITE;
/*!40000 ALTER TABLE `wkalbaranescompraotramoneda` DISABLE KEYS */;
/*!40000 ALTER TABLE `wkalbaranescompraotramoneda` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wkalbaranescomprasinfacturar`
--

DROP TABLE IF EXISTS `wkalbaranescomprasinfacturar`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wkalbaranescomprasinfacturar` (
  `NumAlbaran` int DEFAULT NULL,
  `NumFactura` int DEFAULT NULL,
  `NumPedido` int DEFAULT NULL,
  `Fecha` text COLLATE utf8mb4_unicode_ci,
  `IVASN` text COLLATE utf8mb4_unicode_ci,
  `REQSN` text COLLATE utf8mb4_unicode_ci,
  `CodigoProveedor` text COLLATE utf8mb4_unicode_ci,
  `CodigoAlmacen` text COLLATE utf8mb4_unicode_ci,
  `Observaciones` text COLLATE utf8mb4_unicode_ci,
  `Sumadesglose` double DEFAULT NULL,
  `_DescuentoPP` int DEFAULT NULL,
  `Descuentopp` double DEFAULT NULL,
  `SumaIVA` double DEFAULT NULL,
  `SumaREQ` double DEFAULT NULL,
  `TotalFactura` double DEFAULT NULL,
  `BaseIVA1` double DEFAULT NULL,
  `IVA1` double DEFAULT NULL,
  `REQ1` double DEFAULT NULL,
  `ImporteIVA1` double DEFAULT NULL,
  `BaseIVA2` double DEFAULT NULL,
  `IVA2` double DEFAULT NULL,
  `REQ2` double DEFAULT NULL,
  `ImporteIVA2` double DEFAULT NULL,
  `BaseIVA3` double DEFAULT NULL,
  `IVA3` double DEFAULT NULL,
  `REQ3` double DEFAULT NULL,
  `ImporteIVA3` double DEFAULT NULL,
  `DireccionEnvio` int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wkalbaranescomprasinfacturar`
--

LOCK TABLES `wkalbaranescomprasinfacturar` WRITE;
/*!40000 ALTER TABLE `wkalbaranescomprasinfacturar` DISABLE KEYS */;
/*!40000 ALTER TABLE `wkalbaranescomprasinfacturar` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wkalbaranesventa`
--

DROP TABLE IF EXISTS `wkalbaranesventa`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wkalbaranesventa` (
  `NumAlbaran` int DEFAULT NULL,
  `NumLinea` int DEFAULT NULL,
  `CodigoArticulo` text COLLATE utf8mb4_unicode_ci,
  `DescripcionArticulo` text COLLATE utf8mb4_unicode_ci,
  `Cantidad` double DEFAULT NULL,
  `PVP` double DEFAULT NULL,
  `Descuento` double DEFAULT NULL,
  `PrecioUnitario` double DEFAULT NULL,
  `Subtotal` double DEFAULT NULL,
  `TIVA` int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wkalbaranesventa`
--

LOCK TABLES `wkalbaranesventa` WRITE;
/*!40000 ALTER TABLE `wkalbaranesventa` DISABLE KEYS */;
/*!40000 ALTER TABLE `wkalbaranesventa` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wkalbaranesventafacturar`
--

DROP TABLE IF EXISTS `wkalbaranesventafacturar`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wkalbaranesventafacturar` (
  `NumAlbaran` int DEFAULT NULL,
  `NumFactura` int DEFAULT NULL,
  `NumPedido` int DEFAULT NULL,
  `Fecha` text COLLATE utf8mb4_unicode_ci,
  `IVASN` text COLLATE utf8mb4_unicode_ci,
  `REQSN` text COLLATE utf8mb4_unicode_ci,
  `CodigoCliente` text COLLATE utf8mb4_unicode_ci,
  `CodigoAlmacen` text COLLATE utf8mb4_unicode_ci,
  `Observaciones` text COLLATE utf8mb4_unicode_ci,
  `Sumadesglose` double DEFAULT NULL,
  `_DescuentoPP` double DEFAULT NULL,
  `Descuentopp` double DEFAULT NULL,
  `SumaIVA` double DEFAULT NULL,
  `SumaREQ` double DEFAULT NULL,
  `TotalFactura` double DEFAULT NULL,
  `BaseIVA1` double DEFAULT NULL,
  `IVA1` double DEFAULT NULL,
  `REQ1` double DEFAULT NULL,
  `ImporteIVA1` double DEFAULT NULL,
  `BaseIVA2` double DEFAULT NULL,
  `IVA2` double DEFAULT NULL,
  `REQ2` double DEFAULT NULL,
  `ImporteIVA2` double DEFAULT NULL,
  `BaseIVA3` double DEFAULT NULL,
  `IVA3` double DEFAULT NULL,
  `REQ3` double DEFAULT NULL,
  `ImporteIVA3` double DEFAULT NULL,
  `DireccionEnvio` int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wkalbaranesventafacturar`
--

LOCK TABLES `wkalbaranesventafacturar` WRITE;
/*!40000 ALTER TABLE `wkalbaranesventafacturar` DISABLE KEYS */;
/*!40000 ALTER TABLE `wkalbaranesventafacturar` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wkalbaranesventaotramoneda`
--

DROP TABLE IF EXISTS `wkalbaranesventaotramoneda`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wkalbaranesventaotramoneda` (
  `NumAlbaran` int DEFAULT NULL,
  `NumFactura` int DEFAULT NULL,
  `NumPedido` int DEFAULT NULL,
  `Fecha` text COLLATE utf8mb4_unicode_ci,
  `IVASN` text COLLATE utf8mb4_unicode_ci,
  `REQSN` text COLLATE utf8mb4_unicode_ci,
  `CodigoCliente` text COLLATE utf8mb4_unicode_ci,
  `CodigoAlmacen` text COLLATE utf8mb4_unicode_ci,
  `Observaciones` text COLLATE utf8mb4_unicode_ci,
  `Sumadesglose` double DEFAULT NULL,
  `_DescuentoPP` double DEFAULT NULL,
  `Descuentopp` double DEFAULT NULL,
  `SumaIVA` double DEFAULT NULL,
  `SumaREQ` double DEFAULT NULL,
  `TotalFactura` double DEFAULT NULL,
  `BaseIVA1` double DEFAULT NULL,
  `IVA1` double DEFAULT NULL,
  `REQ1` double DEFAULT NULL,
  `ImporteIVA1` double DEFAULT NULL,
  `BaseIVA2` double DEFAULT NULL,
  `IVA2` double DEFAULT NULL,
  `REQ2` double DEFAULT NULL,
  `ImporteIVA2` double DEFAULT NULL,
  `BaseIVA3` double DEFAULT NULL,
  `IVA3` double DEFAULT NULL,
  `REQ3` double DEFAULT NULL,
  `ImporteIVA3` double DEFAULT NULL,
  `DireccionEnvio` int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wkalbaranesventaotramoneda`
--

LOCK TABLES `wkalbaranesventaotramoneda` WRITE;
/*!40000 ALTER TABLE `wkalbaranesventaotramoneda` DISABLE KEYS */;
/*!40000 ALTER TABLE `wkalbaranesventaotramoneda` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wkalbaranesventasinfacturar`
--

DROP TABLE IF EXISTS `wkalbaranesventasinfacturar`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wkalbaranesventasinfacturar` (
  `NumAlbaran` int DEFAULT NULL,
  `NumFactura` int DEFAULT NULL,
  `NumPedido` int DEFAULT NULL,
  `Fecha` text COLLATE utf8mb4_unicode_ci,
  `IVASN` text COLLATE utf8mb4_unicode_ci,
  `REQSN` text COLLATE utf8mb4_unicode_ci,
  `CodigoCliente` text COLLATE utf8mb4_unicode_ci,
  `CodigoAlmacen` text COLLATE utf8mb4_unicode_ci,
  `Observaciones` text COLLATE utf8mb4_unicode_ci,
  `Sumadesglose` double DEFAULT NULL,
  `_DescuentoPP` int DEFAULT NULL,
  `Descuentopp` double DEFAULT NULL,
  `SumaIVA` double DEFAULT NULL,
  `SumaREQ` double DEFAULT NULL,
  `TotalFactura` double DEFAULT NULL,
  `BaseIVA1` double DEFAULT NULL,
  `IVA1` double DEFAULT NULL,
  `REQ1` double DEFAULT NULL,
  `ImporteIVA1` double DEFAULT NULL,
  `BaseIVA2` double DEFAULT NULL,
  `IVA2` double DEFAULT NULL,
  `REQ2` double DEFAULT NULL,
  `ImporteIVA2` double DEFAULT NULL,
  `BaseIVA3` double DEFAULT NULL,
  `IVA3` double DEFAULT NULL,
  `REQ3` double DEFAULT NULL,
  `ImporteIVA3` double DEFAULT NULL,
  `DireccionEnvio` int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wkalbaranesventasinfacturar`
--

LOCK TABLES `wkalbaranesventasinfacturar` WRITE;
/*!40000 ALTER TABLE `wkalbaranesventasinfacturar` DISABLE KEYS */;
/*!40000 ALTER TABLE `wkalbaranesventasinfacturar` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wkcobrosaremesar`
--

DROP TABLE IF EXISTS `wkcobrosaremesar`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wkcobrosaremesar` (
  `NumCobro` int DEFAULT NULL,
  `CodigoCliente` text COLLATE utf8mb4_unicode_ci,
  `FechaVencimiento` text COLLATE utf8mb4_unicode_ci,
  `Importe` double DEFAULT NULL,
  `Devuelto` text COLLATE utf8mb4_unicode_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wkcobrosaremesar`
--

LOCK TABLES `wkcobrosaremesar` WRITE;
/*!40000 ALTER TABLE `wkcobrosaremesar` DISABLE KEYS */;
/*!40000 ALTER TABLE `wkcobrosaremesar` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wkcobrossinremesar`
--

DROP TABLE IF EXISTS `wkcobrossinremesar`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wkcobrossinremesar` (
  `NumCobro` int DEFAULT NULL,
  `CodigoCliente` text COLLATE utf8mb4_unicode_ci,
  `FechaVencimiento` text COLLATE utf8mb4_unicode_ci,
  `Importe` double DEFAULT NULL,
  `Devuelto` text COLLATE utf8mb4_unicode_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wkcobrossinremesar`
--

LOCK TABLES `wkcobrossinremesar` WRITE;
/*!40000 ALTER TABLE `wkcobrossinremesar` DISABLE KEYS */;
/*!40000 ALTER TABLE `wkcobrossinremesar` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wkdevoluciones`
--

DROP TABLE IF EXISTS `wkdevoluciones`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wkdevoluciones` (
  `NumLote` int DEFAULT NULL,
  `TipoOperacion` text COLLATE utf8mb4_unicode_ci,
  `FechaSoporte` text COLLATE utf8mb4_unicode_ci,
  `FechaDevolucion` text COLLATE utf8mb4_unicode_ci,
  `NumEfecto` double DEFAULT NULL,
  `NumCobro` double DEFAULT NULL,
  `FechaPresentacion` text COLLATE utf8mb4_unicode_ci,
  `NumRemesa` double DEFAULT NULL,
  `ImporteImpagado` double DEFAULT NULL,
  `Importe` double DEFAULT NULL,
  `Vencimiento` text COLLATE utf8mb4_unicode_ci,
  `IdentificativoEfecto` int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wkdevoluciones`
--

LOCK TABLES `wkdevoluciones` WRITE;
/*!40000 ALTER TABLE `wkdevoluciones` DISABLE KEYS */;
INSERT INTO `wkdevoluciones` VALUES (0,'Efectos devueltos impagados','1999-07-14 00:00:00','1999-07-15 00:00:00',123456789123456,15,'1999-07-09 00:00:00',1,1040,1040,'1999-07-09 00:00:00',0),(0,'Efectos reclamados','1999-07-14 00:00:00','1999-07-15 00:00:00',123456789123456,15,'1999-07-09 00:00:00',1,1160,1160,'1999-07-09 00:00:00',1),(0,'Efectos devueltos impagados','1999-07-14 00:00:00','1999-07-15 00:00:00',123456789123456,15,'1999-07-09 00:00:00',1,1040,1040,'1999-07-09 00:00:00',0),(0,'Efectos reclamados','1999-07-14 00:00:00','1999-07-15 00:00:00',123456789123456,15,'1999-07-09 00:00:00',1,1160,1160,'1999-07-09 00:00:00',1);
/*!40000 ALTER TABLE `wkdevoluciones` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wkdevoluciones19`
--

DROP TABLE IF EXISTS `wkdevoluciones19`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wkdevoluciones19` (
  `NumOrden` int DEFAULT NULL,
  `CodigoCliente` text COLLATE utf8mb4_unicode_ci,
  `NombreCliente` text COLLATE utf8mb4_unicode_ci,
  `Importe` double DEFAULT NULL,
  `CodigoRemesa` int DEFAULT NULL,
  `NumCobro` int DEFAULT NULL,
  `Concepto` text COLLATE utf8mb4_unicode_ci,
  `Motivo` text COLLATE utf8mb4_unicode_ci,
  `Entidad` text COLLATE utf8mb4_unicode_ci,
  `Oficina` text COLLATE utf8mb4_unicode_ci,
  `DC` text COLLATE utf8mb4_unicode_ci,
  `Cuenta` text COLLATE utf8mb4_unicode_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wkdevoluciones19`
--

LOCK TABLES `wkdevoluciones19` WRITE;
/*!40000 ALTER TABLE `wkdevoluciones19` DISABLE KEYS */;
/*!40000 ALTER TABLE `wkdevoluciones19` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wkdevoluciones58`
--

DROP TABLE IF EXISTS `wkdevoluciones58`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wkdevoluciones58` (
  `NumOrden` int DEFAULT NULL,
  `CodigoCliente` text COLLATE utf8mb4_unicode_ci,
  `NombreCliente` text COLLATE utf8mb4_unicode_ci,
  `Entidad` text COLLATE utf8mb4_unicode_ci,
  `Oficina` text COLLATE utf8mb4_unicode_ci,
  `DC` text COLLATE utf8mb4_unicode_ci,
  `Cuenta` text COLLATE utf8mb4_unicode_ci,
  `Importe` double DEFAULT NULL,
  `CodigoRemesa` int DEFAULT NULL,
  `NumCobro` int DEFAULT NULL,
  `Concepto` text COLLATE utf8mb4_unicode_ci,
  `Motivo` text COLLATE utf8mb4_unicode_ci,
  `Vencimiento` text COLLATE utf8mb4_unicode_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wkdevoluciones58`
--

LOCK TABLES `wkdevoluciones58` WRITE;
/*!40000 ALTER TABLE `wkdevoluciones58` DISABLE KEYS */;
/*!40000 ALTER TABLE `wkdevoluciones58` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wkentregasyrecepcionespendientes`
--

DROP TABLE IF EXISTS `wkentregasyrecepcionespendientes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wkentregasyrecepcionespendientes` (
  `NumPedido` int DEFAULT NULL,
  `Fecha` text COLLATE utf8mb4_unicode_ci,
  `Empresa` text COLLATE utf8mb4_unicode_ci,
  `FechaEntrega` text COLLATE utf8mb4_unicode_ci,
  `CodigoArticulo` text COLLATE utf8mb4_unicode_ci,
  `CantidadPedido` double DEFAULT NULL,
  `CantidadRecibir` double DEFAULT NULL,
  `CantidadEntregar` double DEFAULT NULL,
  `Stock` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wkentregasyrecepcionespendientes`
--

LOCK TABLES `wkentregasyrecepcionespendientes` WRITE;
/*!40000 ALTER TABLE `wkentregasyrecepcionespendientes` DISABLE KEYS */;
/*!40000 ALTER TABLE `wkentregasyrecepcionespendientes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wkfacturascompra`
--

DROP TABLE IF EXISTS `wkfacturascompra`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wkfacturascompra` (
  `NumFactura` int DEFAULT NULL,
  `NumLinea` int DEFAULT NULL,
  `CodigoArticulo` text COLLATE utf8mb4_unicode_ci,
  `DescripcionArticulo` text COLLATE utf8mb4_unicode_ci,
  `Cantidad` double DEFAULT NULL,
  `PVP` double DEFAULT NULL,
  `Descuento` double DEFAULT NULL,
  `PrecioUnitario` double DEFAULT NULL,
  `Subtotal` double DEFAULT NULL,
  `TIVA` int DEFAULT NULL,
  `NumAlbaran` int DEFAULT NULL,
  `Fecha` text COLLATE utf8mb4_unicode_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wkfacturascompra`
--

LOCK TABLES `wkfacturascompra` WRITE;
/*!40000 ALTER TABLE `wkfacturascompra` DISABLE KEYS */;
/*!40000 ALTER TABLE `wkfacturascompra` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wkfacturascompraotramoneda`
--

DROP TABLE IF EXISTS `wkfacturascompraotramoneda`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wkfacturascompraotramoneda` (
  `NumFactura` int DEFAULT NULL,
  `NumAsiento` int DEFAULT NULL,
  `NumIVA` int DEFAULT NULL,
  `Fecha` text COLLATE utf8mb4_unicode_ci,
  `IVASN` text COLLATE utf8mb4_unicode_ci,
  `REQSN` text COLLATE utf8mb4_unicode_ci,
  `CodigoProveedor` text COLLATE utf8mb4_unicode_ci,
  `FormaPago` text COLLATE utf8mb4_unicode_ci,
  `Observaciones` text COLLATE utf8mb4_unicode_ci,
  `Sumadesglose` double DEFAULT NULL,
  `_DescuentoPP` double DEFAULT NULL,
  `Descuentopp` double DEFAULT NULL,
  `SumaIVA` double DEFAULT NULL,
  `SumaREQ` double DEFAULT NULL,
  `TotalFactura` double DEFAULT NULL,
  `Agente` text COLLATE utf8mb4_unicode_ci,
  `_Comision` double DEFAULT NULL,
  `Importe_Comision` double DEFAULT NULL,
  `BaseIVA1` double DEFAULT NULL,
  `IVA1` double DEFAULT NULL,
  `REQ1` double DEFAULT NULL,
  `ImporteIVA1` double DEFAULT NULL,
  `BaseIVA2` double DEFAULT NULL,
  `IVA2` double DEFAULT NULL,
  `REQ2` double DEFAULT NULL,
  `ImporteIVA2` double DEFAULT NULL,
  `BaseIVA3` double DEFAULT NULL,
  `IVA3` double DEFAULT NULL,
  `REQ3` double DEFAULT NULL,
  `ImporteIVA3` double DEFAULT NULL,
  `Referencia` text COLLATE utf8mb4_unicode_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wkfacturascompraotramoneda`
--

LOCK TABLES `wkfacturascompraotramoneda` WRITE;
/*!40000 ALTER TABLE `wkfacturascompraotramoneda` DISABLE KEYS */;
/*!40000 ALTER TABLE `wkfacturascompraotramoneda` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wkfacturasventa`
--

DROP TABLE IF EXISTS `wkfacturasventa`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wkfacturasventa` (
  `NumFactura` int DEFAULT NULL,
  `NumLinea` int DEFAULT NULL,
  `CodigoArticulo` text COLLATE utf8mb4_unicode_ci,
  `DescripcionArticulo` text COLLATE utf8mb4_unicode_ci,
  `Cantidad` double DEFAULT NULL,
  `PVP` double DEFAULT NULL,
  `Descuento` double DEFAULT NULL,
  `PrecioUnitario` double DEFAULT NULL,
  `Subtotal` double DEFAULT NULL,
  `TIVA` int DEFAULT NULL,
  `NumAlbaran` int DEFAULT NULL,
  `Fecha` text COLLATE utf8mb4_unicode_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wkfacturasventa`
--

LOCK TABLES `wkfacturasventa` WRITE;
/*!40000 ALTER TABLE `wkfacturasventa` DISABLE KEYS */;
/*!40000 ALTER TABLE `wkfacturasventa` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wkfacturasventaotramoneda`
--

DROP TABLE IF EXISTS `wkfacturasventaotramoneda`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wkfacturasventaotramoneda` (
  `NumFactura` int DEFAULT NULL,
  `NumAsiento` int DEFAULT NULL,
  `NumIVA` int DEFAULT NULL,
  `Fecha` text COLLATE utf8mb4_unicode_ci,
  `IVASN` text COLLATE utf8mb4_unicode_ci,
  `REQSN` text COLLATE utf8mb4_unicode_ci,
  `CodigoCliente` text COLLATE utf8mb4_unicode_ci,
  `FormaPago` text COLLATE utf8mb4_unicode_ci,
  `Observaciones` text COLLATE utf8mb4_unicode_ci,
  `Sumadesglose` double DEFAULT NULL,
  `_DescuentoPP` double DEFAULT NULL,
  `Descuentopp` double DEFAULT NULL,
  `SumaIVA` double DEFAULT NULL,
  `SumaREQ` double DEFAULT NULL,
  `TotalFactura` double DEFAULT NULL,
  `Agente` text COLLATE utf8mb4_unicode_ci,
  `_Comision` double DEFAULT NULL,
  `Importe_Comision` double DEFAULT NULL,
  `BaseIVA1` double DEFAULT NULL,
  `IVA1` double DEFAULT NULL,
  `REQ1` double DEFAULT NULL,
  `ImporteIVA1` double DEFAULT NULL,
  `BaseIVA2` double DEFAULT NULL,
  `IVA2` double DEFAULT NULL,
  `REQ2` double DEFAULT NULL,
  `ImporteIVA2` double DEFAULT NULL,
  `BaseIVA3` double DEFAULT NULL,
  `IVA3` double DEFAULT NULL,
  `REQ3` double DEFAULT NULL,
  `ImporteIVA3` double DEFAULT NULL,
  `Referencia` text COLLATE utf8mb4_unicode_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wkfacturasventaotramoneda`
--

LOCK TABLES `wkfacturasventaotramoneda` WRITE;
/*!40000 ALTER TABLE `wkfacturasventaotramoneda` DISABLE KEYS */;
/*!40000 ALTER TABLE `wkfacturasventaotramoneda` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wkformasdepago`
--

DROP TABLE IF EXISTS `wkformasdepago`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wkformasdepago` (
  `CodigoFormaPago` text COLLATE utf8mb4_unicode_ci,
  `D_asFechaFactura` int DEFAULT NULL,
  `D_adePago` int DEFAULT NULL,
  `PorcentajeFactura` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wkformasdepago`
--

LOCK TABLES `wkformasdepago` WRITE;
/*!40000 ALTER TABLE `wkformasdepago` DISABLE KEYS */;
/*!40000 ALTER TABLE `wkformasdepago` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wkmayorcuentas`
--

DROP TABLE IF EXISTS `wkmayorcuentas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wkmayorcuentas` (
  `Asiento` int DEFAULT NULL,
  `Fila` int DEFAULT NULL,
  `Fecha` text COLLATE utf8mb4_unicode_ci,
  `Subcuenta` int DEFAULT NULL,
  `Concepto` text COLLATE utf8mb4_unicode_ci,
  `DebePesetas` double DEFAULT NULL,
  `HaberPesetas` double DEFAULT NULL,
  `Documento` text COLLATE utf8mb4_unicode_ci,
  `Punteo` text COLLATE utf8mb4_unicode_ci,
  `Renumerado` int DEFAULT NULL,
  `Saldo` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wkmayorcuentas`
--

LOCK TABLES `wkmayorcuentas` WRITE;
/*!40000 ALTER TABLE `wkmayorcuentas` DISABLE KEYS */;
/*!40000 ALTER TABLE `wkmayorcuentas` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wkopcomponentessinstock`
--

DROP TABLE IF EXISTS `wkopcomponentessinstock`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wkopcomponentessinstock` (
  `CodigoArticulo` text COLLATE utf8mb4_unicode_ci,
  `DescripcionArticulo` text COLLATE utf8mb4_unicode_ci,
  `CantidadNecesaria` double DEFAULT NULL,
  `Almacen` text COLLATE utf8mb4_unicode_ci,
  `Producir` text COLLATE utf8mb4_unicode_ci,
  `Proveedor` text COLLATE utf8mb4_unicode_ci,
  `CantidadPedir` double DEFAULT NULL,
  `MinimoStock` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wkopcomponentessinstock`
--

LOCK TABLES `wkopcomponentessinstock` WRITE;
/*!40000 ALTER TABLE `wkopcomponentessinstock` DISABLE KEYS */;
/*!40000 ALTER TABLE `wkopcomponentessinstock` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wkopestadomateriales`
--

DROP TABLE IF EXISTS `wkopestadomateriales`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wkopestadomateriales` (
  `CodigoArticulo` text COLLATE utf8mb4_unicode_ci,
  `DescripcionArticulo` text COLLATE utf8mb4_unicode_ci,
  `Cantidad` double DEFAULT NULL,
  `Almacen` text COLLATE utf8mb4_unicode_ci,
  `StockAlmacen` double DEFAULT NULL,
  `StockTotal` double DEFAULT NULL,
  `MinimoStock` double DEFAULT NULL,
  `CantidadRecibir` double DEFAULT NULL,
  `CantidadEntregar` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wkopestadomateriales`
--

LOCK TABLES `wkopestadomateriales` WRITE;
/*!40000 ALTER TABLE `wkopestadomateriales` DISABLE KEYS */;
/*!40000 ALTER TABLE `wkopestadomateriales` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wkordenantes`
--

DROP TABLE IF EXISTS `wkordenantes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wkordenantes` (
  `NumLote` int DEFAULT NULL,
  `FechaSoporte` text COLLATE utf8mb4_unicode_ci,
  `IdentificacionCedente` text COLLATE utf8mb4_unicode_ci,
  `ImportesDevueltos` double DEFAULT NULL,
  `ImportesNominales` double DEFAULT NULL,
  `NumeroRegistros` double DEFAULT NULL,
  `NumeroRecibos` double DEFAULT NULL,
  `EntidadAdeudo` text COLLATE utf8mb4_unicode_ci,
  `OficinaAdeudo` text COLLATE utf8mb4_unicode_ci,
  `DCAdeudo` text COLLATE utf8mb4_unicode_ci,
  `CuentaAdeudo` text COLLATE utf8mb4_unicode_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wkordenantes`
--

LOCK TABLES `wkordenantes` WRITE;
/*!40000 ALTER TABLE `wkordenantes` DISABLE KEYS */;
INSERT INTO `wkordenantes` VALUES (1,'1999-07-14 00:00:00','123456789123456',2200,2200,4,2,'1234','4321','20','1234567890'),(1,'1999-07-14 00:00:00','123456789123456',2200,2200,4,2,'1234','4321','20','1234567890');
/*!40000 ALTER TABLE `wkordenantes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wkordenantes19`
--

DROP TABLE IF EXISTS `wkordenantes19`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wkordenantes19` (
  `NumOrden` int DEFAULT NULL,
  `FechaAdeudo` text COLLATE utf8mb4_unicode_ci,
  `NombreCliente` text COLLATE utf8mb4_unicode_ci,
  `SumaImportes` double DEFAULT NULL,
  `TotalDevoluciones` int DEFAULT NULL,
  `CodigoOrdenante` text COLLATE utf8mb4_unicode_ci,
  `Entidad` text COLLATE utf8mb4_unicode_ci,
  `Oficina` text COLLATE utf8mb4_unicode_ci,
  `DC` text COLLATE utf8mb4_unicode_ci,
  `Cuenta` text COLLATE utf8mb4_unicode_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wkordenantes19`
--

LOCK TABLES `wkordenantes19` WRITE;
/*!40000 ALTER TABLE `wkordenantes19` DISABLE KEYS */;
/*!40000 ALTER TABLE `wkordenantes19` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wkordenantes58`
--

DROP TABLE IF EXISTS `wkordenantes58`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wkordenantes58` (
  `NumOrden` int DEFAULT NULL,
  `CodigoOrdenante` text COLLATE utf8mb4_unicode_ci,
  `NombreCliente` text COLLATE utf8mb4_unicode_ci,
  `Entidad` text COLLATE utf8mb4_unicode_ci,
  `Oficina` text COLLATE utf8mb4_unicode_ci,
  `DC` text COLLATE utf8mb4_unicode_ci,
  `Cuenta` text COLLATE utf8mb4_unicode_ci,
  `SumaImportes` double DEFAULT NULL,
  `TotalDevoluciones` int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wkordenantes58`
--

LOCK TABLES `wkordenantes58` WRITE;
/*!40000 ALTER TABLE `wkordenantes58` DISABLE KEYS */;
/*!40000 ALTER TABLE `wkordenantes58` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wkordenesproduccion`
--

DROP TABLE IF EXISTS `wkordenesproduccion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wkordenesproduccion` (
  `NumOrden` int DEFAULT NULL,
  `NumLinea` int DEFAULT NULL,
  `CodigoArticulo` text COLLATE utf8mb4_unicode_ci,
  `DescripcionArticulo` text COLLATE utf8mb4_unicode_ci,
  `Cantidad` double DEFAULT NULL,
  `Almacen` text COLLATE utf8mb4_unicode_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wkordenesproduccion`
--

LOCK TABLES `wkordenesproduccion` WRITE;
/*!40000 ALTER TABLE `wkordenesproduccion` DISABLE KEYS */;
/*!40000 ALTER TABLE `wkordenesproduccion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wkpedidoscompra`
--

DROP TABLE IF EXISTS `wkpedidoscompra`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wkpedidoscompra` (
  `NumPedido` int DEFAULT NULL,
  `Numlinea` int DEFAULT NULL,
  `CodigoArticulo` text COLLATE utf8mb4_unicode_ci,
  `DescripcionArticulo` text COLLATE utf8mb4_unicode_ci,
  `Cantidad` double DEFAULT NULL,
  `PVP` double DEFAULT NULL,
  `Descuento` double DEFAULT NULL,
  `PrecioUnitario` double DEFAULT NULL,
  `Subtotal` double DEFAULT NULL,
  `TIVA` int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wkpedidoscompra`
--

LOCK TABLES `wkpedidoscompra` WRITE;
/*!40000 ALTER TABLE `wkpedidoscompra` DISABLE KEYS */;
/*!40000 ALTER TABLE `wkpedidoscompra` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wkpedidoscompraotramoneda`
--

DROP TABLE IF EXISTS `wkpedidoscompraotramoneda`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wkpedidoscompraotramoneda` (
  `NumPedido` int DEFAULT NULL,
  `Fecha` text COLLATE utf8mb4_unicode_ci,
  `CodigoProveedor` text COLLATE utf8mb4_unicode_ci,
  `Observaciones` text COLLATE utf8mb4_unicode_ci,
  `Servido` text COLLATE utf8mb4_unicode_ci,
  `FechaEntrega` text COLLATE utf8mb4_unicode_ci,
  `IVASN` text COLLATE utf8mb4_unicode_ci,
  `REQSN` text COLLATE utf8mb4_unicode_ci,
  `Sumadesglose` double DEFAULT NULL,
  `_DescuentoPP` double DEFAULT NULL,
  `Descuentopp` double DEFAULT NULL,
  `SumaIVA` double DEFAULT NULL,
  `SumaREQ` double DEFAULT NULL,
  `TotalFactura` double DEFAULT NULL,
  `BaseIVA1` double DEFAULT NULL,
  `IVA1` double DEFAULT NULL,
  `REQ1` double DEFAULT NULL,
  `ImporteIVA1` double DEFAULT NULL,
  `BaseIVA2` double DEFAULT NULL,
  `IVA2` double DEFAULT NULL,
  `REQ2` double DEFAULT NULL,
  `ImporteIVA2` double DEFAULT NULL,
  `BaseIVA3` double DEFAULT NULL,
  `IVA3` double DEFAULT NULL,
  `REQ3` double DEFAULT NULL,
  `ImporteIVA3` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wkpedidoscompraotramoneda`
--

LOCK TABLES `wkpedidoscompraotramoneda` WRITE;
/*!40000 ALTER TABLE `wkpedidoscompraotramoneda` DISABLE KEYS */;
/*!40000 ALTER TABLE `wkpedidoscompraotramoneda` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wkpedidoscompraservidos`
--

DROP TABLE IF EXISTS `wkpedidoscompraservidos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wkpedidoscompraservidos` (
  `NumPedido` int DEFAULT NULL,
  `CodigoArticulo` text COLLATE utf8mb4_unicode_ci,
  `DescripcionArticulo` text COLLATE utf8mb4_unicode_ci,
  `CantidadPedido` double DEFAULT NULL,
  `PVP` double DEFAULT NULL,
  `Descuento` double DEFAULT NULL,
  `CantidadEntregada` double DEFAULT NULL,
  `CantidadPendiente` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wkpedidoscompraservidos`
--

LOCK TABLES `wkpedidoscompraservidos` WRITE;
/*!40000 ALTER TABLE `wkpedidoscompraservidos` DISABLE KEYS */;
/*!40000 ALTER TABLE `wkpedidoscompraservidos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wkpedidoscompraservir`
--

DROP TABLE IF EXISTS `wkpedidoscompraservir`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wkpedidoscompraservir` (
  `NumPedido` int DEFAULT NULL,
  `CodigoArticulo` text COLLATE utf8mb4_unicode_ci,
  `DescripcionArticulo` text COLLATE utf8mb4_unicode_ci,
  `Cantidad` double DEFAULT NULL,
  `PVP` double DEFAULT NULL,
  `Descuento` double DEFAULT NULL,
  `PrecioUnitario` double DEFAULT NULL,
  `Subtotal` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wkpedidoscompraservir`
--

LOCK TABLES `wkpedidoscompraservir` WRITE;
/*!40000 ALTER TABLE `wkpedidoscompraservir` DISABLE KEYS */;
/*!40000 ALTER TABLE `wkpedidoscompraservir` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wkpedidosventa`
--

DROP TABLE IF EXISTS `wkpedidosventa`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wkpedidosventa` (
  `NumPedido` int DEFAULT NULL,
  `Numlinea` text COLLATE utf8mb4_unicode_ci,
  `CodigoArticulo` text COLLATE utf8mb4_unicode_ci,
  `DescripcionArticulo` text COLLATE utf8mb4_unicode_ci,
  `Cantidad` text COLLATE utf8mb4_unicode_ci,
  `PVP` double DEFAULT NULL,
  `Descuento` double DEFAULT NULL,
  `PrecioUnitario` text COLLATE utf8mb4_unicode_ci,
  `Subtotal` text COLLATE utf8mb4_unicode_ci,
  `TIVA` text COLLATE utf8mb4_unicode_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wkpedidosventa`
--

LOCK TABLES `wkpedidosventa` WRITE;
/*!40000 ALTER TABLE `wkpedidosventa` DISABLE KEYS */;
/*!40000 ALTER TABLE `wkpedidosventa` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wkpedidosventaotramoneda`
--

DROP TABLE IF EXISTS `wkpedidosventaotramoneda`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wkpedidosventaotramoneda` (
  `NumPedido` int DEFAULT NULL,
  `Fecha` text COLLATE utf8mb4_unicode_ci,
  `CodigoCliente` text COLLATE utf8mb4_unicode_ci,
  `Observaciones` text COLLATE utf8mb4_unicode_ci,
  `Servido` text COLLATE utf8mb4_unicode_ci,
  `FechaEntrega` text COLLATE utf8mb4_unicode_ci,
  `NumPresupuesto` int DEFAULT NULL,
  `IVASN` text COLLATE utf8mb4_unicode_ci,
  `REQSN` text COLLATE utf8mb4_unicode_ci,
  `Sumadesglose` double DEFAULT NULL,
  `_DescuentoPP` double DEFAULT NULL,
  `Descuentopp` double DEFAULT NULL,
  `SumaIVA` double DEFAULT NULL,
  `SumaREQ` double DEFAULT NULL,
  `TotalFactura` double DEFAULT NULL,
  `BaseIVA1` double DEFAULT NULL,
  `IVA1` double DEFAULT NULL,
  `REQ1` double DEFAULT NULL,
  `ImporteIVA1` double DEFAULT NULL,
  `BaseIVA2` double DEFAULT NULL,
  `IVA2` double DEFAULT NULL,
  `REQ2` double DEFAULT NULL,
  `ImporteIVA2` double DEFAULT NULL,
  `BaseIVA3` double DEFAULT NULL,
  `IVA3` double DEFAULT NULL,
  `REQ3` double DEFAULT NULL,
  `ImporteIVA3` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wkpedidosventaotramoneda`
--

LOCK TABLES `wkpedidosventaotramoneda` WRITE;
/*!40000 ALTER TABLE `wkpedidosventaotramoneda` DISABLE KEYS */;
/*!40000 ALTER TABLE `wkpedidosventaotramoneda` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wkpedidosventaservidos`
--

DROP TABLE IF EXISTS `wkpedidosventaservidos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wkpedidosventaservidos` (
  `NumPedido` int DEFAULT NULL,
  `CodigoArticulo` text COLLATE utf8mb4_unicode_ci,
  `DescripcionArticulo` text COLLATE utf8mb4_unicode_ci,
  `CantidadPedido` double DEFAULT NULL,
  `PVP` double DEFAULT NULL,
  `Descuento` double DEFAULT NULL,
  `CantidadEntregada` double DEFAULT NULL,
  `CantidadPendiente` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wkpedidosventaservidos`
--

LOCK TABLES `wkpedidosventaservidos` WRITE;
/*!40000 ALTER TABLE `wkpedidosventaservidos` DISABLE KEYS */;
/*!40000 ALTER TABLE `wkpedidosventaservidos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wkpedidosventaservir`
--

DROP TABLE IF EXISTS `wkpedidosventaservir`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wkpedidosventaservir` (
  `NumPedido` int DEFAULT NULL,
  `CodigoArticulo` text COLLATE utf8mb4_unicode_ci,
  `DescripcionArticulo` text COLLATE utf8mb4_unicode_ci,
  `Cantidad` double DEFAULT NULL,
  `PVP` double DEFAULT NULL,
  `Descuento` double DEFAULT NULL,
  `PrecioUnitario` double DEFAULT NULL,
  `Subtotal` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wkpedidosventaservir`
--

LOCK TABLES `wkpedidosventaservir` WRITE;
/*!40000 ALTER TABLE `wkpedidosventaservir` DISABLE KEYS */;
/*!40000 ALTER TABLE `wkpedidosventaservir` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wkpresupuestos`
--

DROP TABLE IF EXISTS `wkpresupuestos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wkpresupuestos` (
  `NumPresupuesto` int DEFAULT NULL,
  `NumLinea` int DEFAULT NULL,
  `DescripcionArticulo` text COLLATE utf8mb4_unicode_ci,
  `Cantidad` double DEFAULT NULL,
  `PVP` double DEFAULT NULL,
  `Descuento` double DEFAULT NULL,
  `PrecioUnitario` double DEFAULT NULL,
  `Subtotal` double DEFAULT NULL,
  `TIVA` int DEFAULT NULL,
  `CodigoArticulo` text COLLATE utf8mb4_unicode_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wkpresupuestos`
--

LOCK TABLES `wkpresupuestos` WRITE;
/*!40000 ALTER TABLE `wkpresupuestos` DISABLE KEYS */;
/*!40000 ALTER TABLE `wkpresupuestos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wkpresupuestosotramoneda`
--

DROP TABLE IF EXISTS `wkpresupuestosotramoneda`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wkpresupuestosotramoneda` (
  `NumPresupuesto` int DEFAULT NULL,
  `Fecha` text COLLATE utf8mb4_unicode_ci,
  `IVASN` text COLLATE utf8mb4_unicode_ci,
  `REQSN` text COLLATE utf8mb4_unicode_ci,
  `CodigoCliente` text COLLATE utf8mb4_unicode_ci,
  `Observaciones` text COLLATE utf8mb4_unicode_ci,
  `Sumadesglose` double DEFAULT NULL,
  `_DescuentoPP` double DEFAULT NULL,
  `Descuentopp` double DEFAULT NULL,
  `SumaIVA` double DEFAULT NULL,
  `SumaREQ` double DEFAULT NULL,
  `TotalPresupuesto` double DEFAULT NULL,
  `Agente` text COLLATE utf8mb4_unicode_ci,
  `BaseIVA1` double DEFAULT NULL,
  `IVA1` double DEFAULT NULL,
  `REQ1` double DEFAULT NULL,
  `ImporteIVA1` double DEFAULT NULL,
  `BaseIVA2` double DEFAULT NULL,
  `IVA2` double DEFAULT NULL,
  `REQ2` double DEFAULT NULL,
  `ImporteIVA2` double DEFAULT NULL,
  `BaseIVA3` double DEFAULT NULL,
  `IVA3` double DEFAULT NULL,
  `REQ3` double DEFAULT NULL,
  `ImporteIVA3` double DEFAULT NULL,
  `Aceptado` text COLLATE utf8mb4_unicode_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wkpresupuestosotramoneda`
--

LOCK TABLES `wkpresupuestosotramoneda` WRITE;
/*!40000 ALTER TABLE `wkpresupuestosotramoneda` DISABLE KEYS */;
/*!40000 ALTER TABLE `wkpresupuestosotramoneda` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wkrechazados`
--

DROP TABLE IF EXISTS `wkrechazados`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wkrechazados` (
  `NumCobro` int DEFAULT NULL,
  `FechaSoporte` text COLLATE utf8mb4_unicode_ci,
  `NumRemesa` int DEFAULT NULL,
  `NumProvinciaLibramiento` text COLLATE utf8mb4_unicode_ci,
  `CodigoINEPlazaLibramiento` text COLLATE utf8mb4_unicode_ci,
  `PlazaLibramiento` text COLLATE utf8mb4_unicode_ci,
  `Importe` double DEFAULT NULL,
  `Vencimiento` text COLLATE utf8mb4_unicode_ci,
  `TipoDocumento` int DEFAULT NULL,
  `FechaExpedicion` text COLLATE utf8mb4_unicode_ci,
  `Nombre` text COLLATE utf8mb4_unicode_ci,
  `CodigoAcepto` int DEFAULT NULL,
  `ClausulaGastos` int DEFAULT NULL,
  `Entidad` text COLLATE utf8mb4_unicode_ci,
  `Oficina` text COLLATE utf8mb4_unicode_ci,
  `DC` text COLLATE utf8mb4_unicode_ci,
  `Cuenta` text COLLATE utf8mb4_unicode_ci,
  `TipoError` text COLLATE utf8mb4_unicode_ci,
  `Domicilio` text COLLATE utf8mb4_unicode_ci,
  `CPPlazaLibrada` text COLLATE utf8mb4_unicode_ci,
  `PlazaLibrada` text COLLATE utf8mb4_unicode_ci,
  `NumProvinciaPlazaLibrada` text COLLATE utf8mb4_unicode_ci,
  `CodigoINEPlazaLibrada` text COLLATE utf8mb4_unicode_ci,
  `Motivo` text COLLATE utf8mb4_unicode_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wkrechazados`
--

LOCK TABLES `wkrechazados` WRITE;
/*!40000 ALTER TABLE `wkrechazados` DISABLE KEYS */;
INSERT INTO `wkrechazados` VALUES (19,'1999-07-15 00:00:00',1,'08','','BARCELONA',23200,'1999-08-14 00:00:00',2,'1999-07-15 00:00:00','CLIENTE 1',2,0,'IBER','4654','65','4654654654','Formal','DOMICILIO 1','01231','POBLACION 1','02','','CUENTA INCORRECTA'),(20,'1999-07-15 00:00:00',1,'08','','BARCELONA',23200,'1999-09-13 00:00:00',2,'1999-07-15 00:00:00','CLIENTE 1',2,0,'IBER','4654','65','4654654654','Informático','DOMICILIO 1','01231','POBLACION 1','02','','DISCO DEFECTUOSO'),(19,'1999-07-15 00:00:00',1,'08','','BARCELONA',23200,'1999-08-14 00:00:00',2,'1999-07-15 00:00:00','CLIENTE 1',2,0,'IBER','4654','65','4654654654','Formal','DOMICILIO 1','01231','POBLACION 1','02','','CUENTA INCORRECTA'),(20,'1999-07-15 00:00:00',1,'08','','BARCELONA',23200,'1999-09-13 00:00:00',2,'1999-07-15 00:00:00','CLIENTE 1',2,0,'IBER','4654','65','4654654654','Informático','DOMICILIO 1','01231','POBLACION 1','02','','DISCO DEFECTUOSO');
/*!40000 ALTER TABLE `wkrechazados` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wkstockdetallado`
--

DROP TABLE IF EXISTS `wkstockdetallado`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wkstockdetallado` (
  `Fecha` text COLLATE utf8mb4_unicode_ci,
  `CodigoArticulo` text COLLATE utf8mb4_unicode_ci,
  `Coste` double DEFAULT NULL,
  `Cantidad` double DEFAULT NULL,
  `CodigoAlmacen` text COLLATE utf8mb4_unicode_ci,
  `Tipo` text COLLATE utf8mb4_unicode_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wkstockdetallado`
--

LOCK TABLES `wkstockdetallado` WRITE;
/*!40000 ALTER TABLE `wkstockdetallado` DISABLE KEYS */;
/*!40000 ALTER TABLE `wkstockdetallado` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `zonas`
--

DROP TABLE IF EXISTS `zonas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `zonas` (
  `Codigo_de_zona` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `Descripcion_Zona` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `zonas`
--

LOCK TABLES `zonas` WRITE;
/*!40000 ALTER TABLE `zonas` DISABLE KEYS */;
INSERT INTO `zonas` VALUES ('AND','Andalucía'),('ARA','Aragón'),('AST','Asturias'),('BAL','Baleares'),('CAL','Castilla León'),('CAM','Castilla La Mancha'),('CAN','Canarias'),('CANT','Cantabria'),('CAT','Cataluña'),('EXT','Extremadura'),('GAL','Galicia'),('MAD','Madrid'),('MUR','Murcia'),('NAV','Navarra'),('PAV','País Vasco'),('RIO','La Rioja'),('VAL','Comunidad Valenciana'),('AND','Andalucía'),('ARA','Aragón'),('AST','Asturias'),('BAL','Baleares'),('CAL','Castilla León'),('CAM','Castilla La Mancha'),('CAN','Canarias'),('CANT','Cantabria'),('CAT','Cataluña'),('EXT','Extremadura'),('GAL','Galicia'),('MAD','Madrid'),('MUR','Murcia'),('NAV','Navarra'),('PAV','País Vasco'),('RIO','La Rioja'),('VAL','Comunidad Valenciana');
/*!40000 ALTER TABLE `zonas` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-18 18:23:17
