# Airbnb Data Processor - Instructions

## 1. Installation
Create a virtual environment and install dependencies:

```bash
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
pip install -r requirements.txt
```

## 2. Setup CSV Files
Create the folder `data/listings` inside `scripts/, put CSV files inside it and rename them with the respective city and country format:

```bash
city_Country.csv
```

## 3. Setup user account files
Create the folder `data/users` inside `scripts/`, put CSV with users inside it, follow the format:

```bash
email,password,name,avatar_picture_path,role
```

## 4. Run Script
```bash
python generate_db.py
```

The script will process all CSV files and generate a `db.json` file with the consolidated Airbnb data.
