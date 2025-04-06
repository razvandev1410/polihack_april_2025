import requests
import xml.etree.ElementTree as ET
import json
import time

def extract_gene_compound_relations(pathway_id):
    """
    For a given pathway_id (e.g. "path:hsa01522"), fetch the KGML XML,
    then extract all relations where entry1 is of type "gene" and entry2 is of type "compound".
    Returns a list of [gene_id, compound_id] pairs.
    """
    url = f"https://rest.kegg.jp/get/{pathway_id}/kgml"
    print(f"Processing {pathway_id} ...")
    response = requests.get(url)
    if response.status_code != 200:
        print(f"Failed to fetch {pathway_id}")
        return []
    
    # Parse the KGML XML content
    root = ET.fromstring(response.content)
    
    # Dictionaries to hold mapping: entry id -> list of gene IDs or compound IDs
    gene_entries = {}
    compound_entries = {}
    
    # Process all <entry> elements and keep only those with type "gene" or "compound"
    for entry in root.findall('entry'):
        entry_type = entry.attrib.get("type", "")
        entry_id = entry.attrib.get("id")
        if entry_type == "gene":
            # The "name" attribute can contain multiple gene IDs separated by spaces.
            names = entry.attrib.get("name", "")
            gene_entries[entry_id] = names.split()
        elif entry_type == "compound":
            names = entry.attrib.get("name", "")
            compound_entries[entry_id] = names.split()
    
    # Now extract relations where entry1 is a gene and entry2 is a compound.
    gene_drug_pairs = []
    for relation in root.findall('relation'):
        entry1 = relation.attrib.get("entry1")
        entry2 = relation.attrib.get("entry2")
        # Check that entry1 exists in gene_entries and entry2 in compound_entries
        if entry1 in gene_entries and entry2 in compound_entries:
            # Create a pair for each combination of gene from entry1 and compound from entry2.
            for gene in gene_entries[entry1]:
                for compound in compound_entries[entry2]:
                    gene_drug_pairs.append([gene, compound])
    
    return gene_drug_pairs

def main():
    # Load the pathways_output.json file
    with open("pathways_output.json", "r") as f:
        data = json.load(f)
    
    unique_pathways = data.get("unique_pathways", [])
    all_pairs = []

    # Process each pathway in the unique_pathways list
    for pathway in unique_pathways:
        pairs = extract_gene_compound_relations(pathway)
        all_pairs.extend(pairs)
        # Pause briefly to avoid overwhelming the KEGG server
        time.sleep(1)
    
    # Build the output structure
    output_data = {
        "gene_id_to_drug_id": all_pairs
    }
    
    # Save the results to gene_to_drug.json
    with open("gene_to_drug.json", "w") as f:
        json.dump(output_data, f, indent=2)
    
    print("Output written to gene_to_drug.json")

if __name__ == "__main__":
    main()