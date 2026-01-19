import pandas as pd
import numpy as np
import os
import random
import json
import sys

def get_city_country_from_filename(filename):
    """
    Extract city and country names from CSV filename.
    Expected format: 'city_Country.csv'.
    """
    name_without_ext = filename.replace('.csv', '')
    parts = name_without_ext.split('_')

    if len(parts) >= 2:
        city_name = '_'.join(parts[:-1])
        country_name = parts[-1]

        city_name = city_name.replace('_', ' ').title()
        country_name = country_name.replace('_', ' ').title()

        return city_name, country_name
    else:
        city_name = name_without_ext.replace('_', ' ').title()
        return city_name, 'Unknown'

def process_single_file(file_path, random_sampling=True):
    """
    Process a single Airbnb CSV file:
    - Extract city/country from filename
    - Sample data (random or fixed size)
    - Clean and transform columns
    - Add irregularity types for analysis
    """
    try:
        filename = os.path.basename(file_path)
        city_name, country_name = get_city_country_from_filename(filename)
        print(f"Processing: {file_path} → City: {city_name}, Country: {country_name}")

        # Read CSV file
        df = pd.read_csv(file_path, low_memory=False)
        
        # Apply sampling strategy
        if random_sampling:
            sample_size = random.randint(500, 1500)
            sampling_type = "random"
        else:
            sample_size = 1000
            sampling_type = "fixed"
        
        if len(df) > sample_size:
            df = df.sample(n=sample_size, random_state=42)
            print(f"  {sample_size} entries sampled ({sampling_type} size)")
        else:
            print(f"  Using all {len(df)} entries (file has less than {sample_size} records)")

        # Create result dataframe with cleaned data
        result_df = pd.DataFrame()
        result_df['host_name'] = df.get('host_name', '').fillna('')
        result_df['name'] = df.get('name', '').fillna('')
        result_df['latitude'] = pd.to_numeric(df.get('latitude', 0), errors='coerce').fillna(0)
        result_df['longitude'] = pd.to_numeric(df.get('longitude', 0), errors='coerce').fillna(0)

        # Convert price from USD to EUR
        price_column = df.get('price', '')
        result_df['price_eur'] = price_column.apply(
            lambda x: float(str(x).replace('$', '').replace(',', '')) * 0.86
            if pd.notna(x) and str(x).strip() and str(x) != '' else 0
        )

        # Add additional fields
        result_df['availability_365'] = pd.to_numeric(df.get('availability_365', 0), errors='coerce').fillna(0)
        result_df['room_type'] = df.get('room_type', '').fillna('')
        result_df['country'] = country_name
        result_df['municipality'] = df.get('neighbourhood_group_cleansed', '').fillna('')
        result_df['city'] = city_name
        result_df['reviews_count'] = pd.to_numeric(df.get('number_of_reviews', 0), errors='coerce').fillna(0)
        
        review_scores_rating = df.get('review_scores_rating', 0)
        result_df['review_rating'] = pd.to_numeric(review_scores_rating, errors='coerce').fillna(0)
        
        result_df['last_scraped'] = df.get('last_scraped', '').fillna('')
        result_df['host_listings_count'] = pd.to_numeric(df.get('host_total_listings_count', 1), errors='coerce').fillna(1)
        result_df['url'] = df.get('listing_url', '').fillna('')

        # Generate irregularity types for regulatory analysis
        irregularity_types = ['Compliant', 'Licensing', 'Zoning', 'Over-Occupation', 'Taxation']
        weights = [0.95, 0.02, 0.01, 0.01, 0.01]  # Mostly compliant, some irregularities
        result_df['irregularity_type'] = [random.choices(irregularity_types, weights=weights)[0]
                                        for _ in range(len(result_df))]

        print(f"  Processed {len(result_df)} records")
        return result_df

    except Exception as e:
        print(f"  Error: {str(e)}")
        return pd.DataFrame()

def process_users_file(users_file_path):
    """
    Process users CSV file to extract user data for the application.
    """
    try:
        print(f"Processing users file: {users_file_path}")
        
        users_df = pd.read_csv(users_file_path)
        
        users_data = []
        for _, row in users_df.iterrows():
            user_dict = {
                'email': row['email'],
                'password': row['password'],
                'name': row['name'],
                'pic': row['pic'],
                'role': row['role']
            }
            users_data.append(user_dict)
        
        print(f"  Processed {len(users_data)} users")
        return users_data
        
    except Exception as e:
        print(f"  Error processing users file: {str(e)}")
        return []

def main(data_folder="data", random_sampling=True):
    """
    Main function to process all data and generate db.json:
    - Processes users from users.csv
    - Processes all Airbnb listings from CSV files
    - Combines data into single JSON database
    """
    users_folder = os.path.join(data_folder, "users")
    listings_folder = os.path.join(data_folder, "listings")
    output_file = "db.json"

    # Validate folder structure
    if not os.path.exists(data_folder):
        print(f"Error: Folder '{data_folder}' not found!")
        print("Expected structure:")
        print(f"  {data_folder}/")
        print(f"  {data_folder}/users/users.csv")
        print(f"  {data_folder}/listings/ [Airbnb CSV files]")
        return

    db_data = {
        "users": [],
        "airbnb_listings": []
    }

    # Process users data
    users_file = os.path.join(users_folder, "users.csv")
    if os.path.exists(users_file):
        users_data = process_users_file(users_file)
        if users_data:
            db_data["users"] = users_data
    else:
        print(f"Users file {users_file} not found!")

    # Process Airbnb listings data
    if os.path.exists(listings_folder):
        csv_files = [os.path.join(listings_folder, f) for f in os.listdir(listings_folder) if f.endswith('.csv')]
        
        if not csv_files:
            print(f"No Airbnb CSV files found in {listings_folder}!")
        else:
            print(f"Found {len(csv_files)} Airbnb CSV files")
            print(f"Sampling mode: {'RANDOM' if random_sampling else 'FIXED (1000)'}")

            all_data = []
            for csv_file in csv_files:
                processed_df = process_single_file(csv_file, random_sampling)
                if not processed_df.empty:
                    all_data.append(processed_df)

            if all_data:
                # Combine all processed data
                consolidated_df = pd.concat(all_data, ignore_index=True)
                db_data["airbnb_listings"] = consolidated_df.replace({np.nan: None}).to_dict('records')
                
                print(f"Total Airbnb records: {len(consolidated_df)}")
            else:
                print("No Airbnb data processed!")
    else:
        print(f"Listings folder {listings_folder} not found!")

    # Save final JSON database
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(db_data, f, indent=2, ensure_ascii=False)
        f.write('\n')
    
    print(f"All data saved to: {output_file}")
    print(f"Final structure: {len(db_data['users'])} users, {len(db_data['airbnb_listings'])} Airbnb listings")
    print("Processing completed!")

if __name__ == "__main__":
    # Command line configuration
    data_folder = "data"
    random_sampling = True  
    
    if len(sys.argv) > 1:
        data_folder = sys.argv[1]
        print(f"Using data folder: {data_folder}")
    
    # Check for fixed sampling mode flag
    if "--fixed" in sys.argv or "-f" in sys.argv:
        random_sampling = False
        print("Using FIXED sampling mode (1000 per file)")
    else:
        print("Using RANDOM sampling mode (500-1500 per file)")
    
    main(data_folder, random_sampling)