# JBVMotors

## Overview

**JBVMotors** is a complete database system designed for a car rental company with three operational branches. Developed for a university-level database course, this project includes everything from requirements analysis and modeling to SQL querying and data migration.

The system is built using **MySQL** for the final database implementation and **Python** for data generation and migration tasks.

## Contributors

* [DelgadoDevT](https://github.com/DelgadoDevT)
* [PaoComPlanta](https://github.com/paocomplanta)
* [xfn14](https://github.com/xfn14)
* [inesferribeiro](https://github.com/inesferribeiro)

## Features

- Entity-Relationship (ER) and Relational Models  
- Relational Algebra expressions  
- Normalized schema design  
- SQL scripts for schema creation and queries  
- Automated fake data generation  
- Data migration from PostgreSQL, CSV, and JSON to MySQL

## Requirements

- MySQL Server  
- Python 3.7+  
- PostgreSQL Server (for source data)

## Data Tools (Python Scripts)

In the `scripts_python/` folder, you'll find two main Python tools:

- **`populate.py`** – generates fake data and exports it to SQL, CSV, and JSON  
- **`migrate.py`** – migrates data into MySQL from PostgreSQL, CSV, or JSON  

### Example Commands

```bash
python populate.py --all       # Export to SQL, CSV, and JSON
python migrate.py all          # Import from PostgreSQL, CSV, and JSON into MySQL
