-- Isolated schemas following the Database-per-Service pattern
CREATE DATABASE IF NOT EXISTS user_db;
CREATE DATABASE IF NOT EXISTS product_db;
CREATE DATABASE IF NOT EXISTS inventory_db;
CREATE DATABASE IF NOT EXISTS order_db;
CREATE DATABASE IF NOT EXISTS payment_db;
CREATE DATABASE IF NOT EXISTS shipping_db;

-- Dedicated development user with access to ShopFlow schemas
CREATE USER IF NOT EXISTS 'shopflow'@'%' IDENTIFIED BY 'shopflow123';
GRANT ALL PRIVILEGES ON user_db.* TO 'shopflow'@'%';
GRANT ALL PRIVILEGES ON product_db.* TO 'shopflow'@'%';
GRANT ALL PRIVILEGES ON inventory_db.* TO 'shopflow'@'%';
GRANT ALL PRIVILEGES ON order_db.* TO 'shopflow'@'%';
GRANT ALL PRIVILEGES ON payment_db.* TO 'shopflow'@'%';
GRANT ALL PRIVILEGES ON shipping_db.* TO 'shopflow'@'%';
FLUSH PRIVILEGES;
