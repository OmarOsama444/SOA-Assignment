PROJECT DOCKER COMPOSE SETUP
============================

This project contains multiple microservices (Order, Customer, Inventory,
Notification, Pricing, Frontend) and a MySQL database, all orchestrated using
Docker Compose.

This file explains how to set up, run, and manage the entire environment.


------------------------------------------------------------
1. REQUIREMENTS
------------------------------------------------------------

Before running the project, make sure you have:

- Docker installed
- Docker Compose installed
- Ports 3306, 5001-5005, and 8080 available
- No need to install python or pip, you can see section 3 on how to run the application

------------------------------------------------------------
2. PROJECT STRUCTURE
------------------------------------------------------------

The docker-compose.yml defines the following services:

1. mysql
   - Runs MySQL 8.0
   - Initializes database using ./db/init.sql
   - Stores data in ./db/data

2. order-service
   - Flask microservice
   - Runs on port 5001

3. customer-service
   - Flask microservice
   - Runs on port 5002

4. inventory-service
   - Flask microservice
   - Runs on port 5003

5. notification-service
   - Flask microservice
   - Runs on port 5004

6. pricing-service
   - Flask microservice
   - Runs on port 5005

7. frontend
   - Runs on port 8080


------------------------------------------------------------
3. HOW TO START ALL SERVICES
------------------------------------------------------------

From the folder where docker-compose.yml is located, run:

    docker compose up --build

This will:
- Build all service images
- Start the MySQL database
- Start all microservices
- Start the frontend
- Install all required libraries
- Builds the venv folder


------------------------------------------------------------
4. RUNNING IN BACKGROUND (DETACHED MODE)
------------------------------------------------------------

If you want to run everything in the background:

    docker compose up --build -d


------------------------------------------------------------
5. STOPPING THE SERVICES
------------------------------------------------------------

To stop everything:

    docker compose down

To stop everything AND delete containers, networks, and volumes:

    docker compose down -v


------------------------------------------------------------
6. CHECKING LOGS
------------------------------------------------------------

To see logs for all services:

    docker compose logs -f

To see logs for a single service:

    docker compose logs -f service-name

Example:

    docker compose logs -f order-service


------------------------------------------------------------
7. ACCESSING CONTAINERS
------------------------------------------------------------

You can enter any running container using:

    docker exec -it container_name bash

Example:

    docker exec -it my_mysql bash


------------------------------------------------------------
8. DATABASE INFORMATION
------------------------------------------------------------

The MySQL container uses the following credentials (set in docker-compose.yml):

- HOST: mysql
- PORT: 3306
- DATABASE: ecommerce_system
- USER: myuser
- PASSWORD: mypassword
- ROOT PASSWORD: rootpassword

Your application should connect to "mysql" as the host,
not "localhost", since it runs inside Docker's network.


------------------------------------------------------------
9. INITIAL DATABASE SETUP
------------------------------------------------------------
./db/init.sql will run the first time the database initializes.
The database data will persist in ./db/data, so changes remain after restart.


------------------------------------------------------------
10. REBUILDING SERVICES
------------------------------------------------------------

If you change Python code or Dockerfiles, rebuild with:

    docker compose build

Or rebuild and start:

    docker compose up --build


