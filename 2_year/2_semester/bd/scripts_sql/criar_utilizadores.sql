CREATE USER IF NOT EXISTS 'admin_sistema'@'localhost' IDENTIFIED BY 'SenhaForte123!';
GRANT ALL PRIVILEGES ON jbvmotors.* TO 'admin_sistema'@'localhost' WITH GRANT OPTION;

CREATE USER IF NOT EXISTS 'tecnico_sistema_braga'@'IP_do_stand_maia' IDENTIFIED BY 'SenhaSegura123!';
GRANT SELECT, INSERT, UPDATE ON jbvmotors.* TO 'tecnico_sistema_braga'@'localhost';

CREATE USER IF NOT EXISTS 'tecnico_sistema_maia'@'IP_do_stand_maia' IDENTIFIED BY 'SenhaSegura456!';
GRANT SELECT, INSERT, UPDATE ON jbvmotors.* TO 'tecnico_sistema_maia'@'IP_do_stand_maia';

CREATE USER IF NOT EXISTS 'tecnico_sistema_viana'@'IP_do_stand_viana' IDENTIFIED BY 'SenhaSegura789!';
GRANT SELECT, INSERT, UPDATE ON jbvmotors.* TO 'tecnico_sistema_viana'@'IP_do_stand_viana';