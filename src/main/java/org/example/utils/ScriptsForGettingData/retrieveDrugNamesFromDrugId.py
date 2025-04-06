import json
import requests
import time

def get_drug_name(drug_id):
    """
    Given a drug_id (e.g., "D01245"), query KEGG to get its name.
    The KEGG endpoint is: https://rest.kegg.jp/get/dr:{drug_id}
    Parses the returned text to extract the first drug name.
    """
    url = f"https://rest.kegg.jp/get/dr:{drug_id}"
    headers = {
        # Set a browser-like User-Agent to avoid HTTP 403 errors.
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0 Safari/537.36"
    }
    response = requests.get(url, headers=headers)
    count_attempts = 0
    while response.status_code != 200 and count_attempts < 10:
        print("Trying to get access...")
        response = requests.get(url, headers=headers)
        count_attempts += 1
        time.sleep(1)

    if count_attempts == 10:
        return ""
    print("Success")

    # The response text is plain text with lines like:
    # ENTRY       D01245                      DRUG
    # NAME        Aspirin; Acetylsalicylic acid
    lines = response.text.splitlines()
    for line in lines:
        if line.strip().startswith("NAME"):
            # Remove the "NAME" token and strip the rest
            drug_names = line.partition("NAME")[2].strip()
            # Take the first name if there are multiple separated by semicolons
            drug_name = drug_names.split(";")[0].strip()
            return drug_name
    return None

def main():
    # Load the gene_to_drug.json file
    with open("gene_to_drug.json", "r") as f:
        data = json.load(f)
    
    # Extract the list of pairs and determine unique drug IDs
    pairs = data.get("gene_id_to_drug_id", [])
    unique_drug_ids = set()
    for pair in pairs:
        # pair[1] is like "cpd:D01245", extract "D01245"
        compound_field = pair[1]
        if compound_field.startswith("cpd:"):
            drug_id = compound_field.split("cpd:")[1]
        elif compound_field.startswith("dr:"):
            drug_id = compound_field.split("dr:")[1]
        else:
            drug_id = compound_field
        unique_drug_ids.add(drug_id)
    
    # For each unique drug id, get the drug name from KEGG
    drug_id_to_name_pairs = []
    for drug_id in sorted(unique_drug_ids):
        print(f"Fetching drug {drug_id}...")
        drug_name = get_drug_name(drug_id)
        if drug_name is None:
            drug_name = ""
        drug_id_to_name_pairs.append([drug_id, drug_name])
        time.sleep(3)  # pause to be courteous to the KEGG server

    # Build output in the required format
    output = {
        "drug_id_to_drug_name": drug_id_to_name_pairs
    }
    
    # Save to a new JSON file
    with open("drug_id_to_drug_name.json", "w") as f:
        json.dump(output, f, indent=2)
    
    print("Output saved to drug_id_to_drug_name.json")

if __name__ == "__main__":
    main()