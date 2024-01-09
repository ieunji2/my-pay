CREATE DATABASE IF NOT EXISTS `mypay_account`;
CREATE DATABASE IF NOT EXISTS `mypay_money`;

CREATE USER 'myuser'@'%' IDENTIFIED BY 'P@ssw0rd';

GRANT ALL ON mypay_account.* to 'myuser'@'%';
GRANT ALL ON mypay_money.* to 'myuser'@'%';
