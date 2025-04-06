import requests
import json

url = "https://rest.kegg.jp/list/hsa"
response = requests.get(url)
lines = response.text.strip().split("\n")

gene_map = {}
for line in lines:
    # Extract the gene ID from the beginning (before the first tab)
    kegg_id = line.split("\t", 1)[0]
    # Extract only the text after the last tab
    symbol = line.rsplit("\t", 1)[-1].strip()
    gene_map[kegg_id] = symbol

# Write the gene_map dictionary to a JSON file with pretty formatting
with open("gene_map.json", "w") as outfile:
    json.dump(gene_map, outfile, indent=2)

print("JSON file 'gene_map.json' has been created.")
