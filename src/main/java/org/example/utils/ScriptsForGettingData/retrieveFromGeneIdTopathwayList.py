import json
import requests

def get_pathways_for_gene(gene_id):
    """Retrieve KEGG pathway IDs for a given gene ID."""
    url = f"https://rest.kegg.jp/link/pathway/{gene_id}"
    response = requests.get(url)
    pathways = []
    if response.ok:
        # Each line is tab-separated: gene_id <tab> pathway:XXXXX
        for line in response.text.strip().splitlines():
            parts = line.split("\t")
            if len(parts) == 2:
                # parts[1] is the pathway ID, e.g. pathway:hsa04110
                pathways.append(parts[1])
    else:
        print(f"Request failed for {gene_id} with status code: {response.status_code}")
    return pathways

def main():
    # Load the JSON file (adjust the file name if needed)
    with open('gene_map.json', 'r') as file:
        gene_data = json.load(file)
    
    # Create a dictionary to store pathways per gene
    gene_to_pathways = {}
    all_pathways = set()
    
    # Iterate over each gene id (keys of the JSON)
    for gene_id in gene_data.keys():
        pathways = get_pathways_for_gene(gene_id)
        print(f"{gene_id}: {pathways}")
        gene_to_pathways[gene_id] = pathways
        all_pathways.update(pathways)
    
    # Convert the set of all pathways to a list
    unique_pathways = list(all_pathways)
    
    # Output the results to the console
    print("\nPathways per gene:")
    for gene, pathways in gene_to_pathways.items():
        print(f"{gene}: {pathways}")
    
    print("\nUnique pathways found across all genes:")
    print(unique_pathways)
    
    # Save the results into a JSON file
    output_data = {
        "gene_to_pathways": gene_to_pathways,
        "unique_pathways": unique_pathways
    }
    with open("pathways_output.json", "w") as outfile:
        json.dump(output_data, outfile, indent=4)

if __name__ == "__main__":
    main()
