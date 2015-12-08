create database sample;
CREATE USER 'sampleadmin'@'localhost' IDENTIFIED BY '654321';
GRANT ALL PRIVILEGES ON sample . * TO 'sampleadmin'@'localhost';
use sample;
create table users (user_id varchar(100), username varchar(100), password varchar(100), primary key (user_id));
create table projects (project_id bigint, name varchar(100), owner_id varchar(100), primary key (project_id), FOREIGN KEY (owner_id) REFERENCES users(user_id));