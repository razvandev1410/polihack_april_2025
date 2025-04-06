import json

def load_gene_map(json_file):
    """
    Load the gene_map.json file and build a dictionary mapping each gene name
    (extracted from the description before the semicolon) to its gene ID.
    """
    with open(json_file, "r") as f:
        gene_data = json.load(f)
    
    gene_name_to_id = {}
    
    for gene_id, description in gene_data.items():
        # Split the description at the semicolon and take the part before it.
        names_part = description.split(";")[0].strip()
        # There can be multiple gene names separated by commas.
        gene_names = [name.strip() for name in names_part.split(",")]
        for name in gene_names:
            gene_name_to_id[name] = gene_id
    
    return gene_name_to_id

def lookup_gene_id(gene_name, mapping):
    """
    Look up the gene ID for a given gene name.
    """
    return mapping.get(gene_name, "Gene name not found.")

def get_id_from_gene_name(gene_name):
    # Load the mapping from the gene_map.json file
    gene_map_file = "gene_map.json"
    gene_name_to_id = load_gene_map(gene_map_file)
    
    user_input = gene_name.strip()
    gene_id = lookup_gene_id(user_input, gene_name_to_id)
    
    return gene_id
    
if __name__ == "__main__":
    print(get_id_from_gene_name("1234"))