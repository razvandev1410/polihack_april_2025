import requests
import xml.etree.ElementTree as ET
import json
from collections import defaultdict

# Load input from pathways_output.json
with open("pathways_output.json", "r") as infile:
    input_data = json.load(infile)
    
# Get the gene_to_pathways mapping from the input file.
# This dictionary maps gene IDs to a list of pathway IDs.
gene_to_pathways = input_data.get("gene_to_pathways", {})

# Build the list of unique pathway IDs from the gene_to_pathways dictionary.
unique_pathways = list(set(
    p for pathways in gene_to_pathways.values() for p in pathways
))

def parse_kgml(pathway_id):
    """
    Retrieve and parse the KGML for a given pathway ID and extract gene-gene relationships.
    """
    url = f"https://rest.kegg.jp/get/{pathway_id}/kgml"
    response = requests.get(url)
    if response.status_code != 200:
        print(f"Failed to fetch {pathway_id}")
        return []
    
    # Parse the XML content using ElementTree
    root = ET.fromstring(response.content)
    
    # Build a mapping from KGML entry ID to a list of gene IDs
    id_to_genes = {}
    for entry in root.findall('entry'):
        if entry.attrib.get('type') != 'gene':
            continue
        entry_id = entry.attrib['id']
        # The attribute 'name' can contain one or more gene IDs separated by whitespace.
        gene_ids = entry.attrib['name'].split()
        id_to_genes[entry_id] = gene_ids

    # Extract gene-gene relationships from the 'relation' elements.
    relations = []
    for rel in root.findall('relation'):
        entry1 = rel.attrib['entry1']
        entry2 = rel.attrib['entry2']
        
        # Only consider relationships between entries that have gene IDs.
        if entry1 not in id_to_genes or entry2 not in id_to_genes:
            continue
        
        genes1 = id_to_genes[entry1]
        genes2 = id_to_genes[entry2]
        
        # Create a relation between every gene in entry1 and every gene in entry2.
        for g1 in genes1:
            for g2 in genes2:
                relations.append([g1, g2])
    
    return relations

# Final mapping that will hold gene->(pathway->relation list)
final_mapping = defaultdict(lambda: defaultdict(list))
# Cache for pathway relations (to avoid fetching the same pathway more than once)
pathway_to_relations = {}

# Loop through the unique pathways and fetch their KGML relations.
for pathway in unique_pathways:
    relations = parse_kgml(pathway)
    print("\n\n")
    print(f"pathway: {pathway}\n")
    print(relations)
    print("\n\n")
    pathway_to_relations[pathway] = relations

# Build the final mapping by combining the input gene_to_pathways and the parsed relations.
for gene_id, pathways in gene_to_pathways.items():
    for pathway in pathways:
        if pathway in pathway_to_relations:
            final_mapping[gene_id][pathway] = pathway_to_relations[pathway]

# Convert final_mapping (a defaultdict of defaultdicts) to a regular dict for JSON serialization.
final_output = {gene: dict(path_rel) for gene, path_rel in final_mapping.items()}

# Save the results to a new JSON file.
with open("gene_pathway_relations.json", "w") as outfile:
    json.dump(final_output, outfile, indent=2)

