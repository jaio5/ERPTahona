-- Asegura que xml_generado sea LONGTEXT para almacenar XMLs de VeriFactu sin truncación.
-- Hibernate 6 puede crear esta columna como TINYTEXT/TEXT en entornos sin Flyway.
ALTER TABLE verifactu_evidence MODIFY COLUMN xml_generado LONGTEXT;
