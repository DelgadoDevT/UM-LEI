# JBV Motors

## Requirements

- Python 3.7+
- PostgreSQL server (for source)
- MySQL server (for destination)

## Setup

1. **Install Python packages**

```bash
pip install -r requirements.txt
```

2. **Create `.env` file**

Create a `.env` file in the root directory to store database credentials:

```env
# PostgreSQL
PG_HOST=localhost
PG_PORT=5432
PG_DB="jbvmotors"
PG_USER="root"
PG_PASSWORD=""

# MySQL
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DB="jbvmotors"
MYSQL_USER="root"
MYSQL_PASSWORD=""
```

Do **not** commit your `.env` file to version control.

## Scripts

1. `populate.py`

Generates fake data and exports it to:

- SQL scripts (`populate_mysql.sql`, `populate_postgres.sql`)
- CSV files (`populate_output/csv/`)
- JSON files (`populate_output/json/`)

### Usage

```
python populate.py --all
python populate.py --mysql     # SQL for MySQL
python populate.py --postgres  # SQL for PostgreSQL
python populate.py --csv       # CSV files
python populate.py --json      # JSON files
```

2. `migrate.py`

Synchronizes data into a MySQL database from:

- PostgreSQL
- CSV
- JSON

### Usage

```
python migrate.py all       # Migrate from all sources
python migrate.py postgres  # Migrate from PostgreSQL
python migrate.py csv       # Migrate from CSV
python migrate.py json      # Migrate from JSON
```
