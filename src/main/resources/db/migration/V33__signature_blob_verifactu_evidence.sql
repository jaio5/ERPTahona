-- La firma digital RSA-2048 de los registros VeriFactu ocupa 256 bytes;
-- la columna se creó como VARBINARY(255) y truncaba la firma.
ALTER TABLE verifactu_evidence MODIFY COLUMN signature BLOB;
