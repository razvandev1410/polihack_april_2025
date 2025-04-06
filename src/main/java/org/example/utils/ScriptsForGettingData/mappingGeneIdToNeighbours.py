import json
import time

def main():
    # Load gene_pathway_relations.json
    with open("gene_pathway_relations.json", "r") as infile:
        relations_data = json.load(infile)
    
    # Dictionary to collect direct connections (source gene -> set of target genes)
    direct_connections = {}
    
    # The gene_pathway_relations.json is assumed to be structured as:
    # {
    #   "some_key": {
    #       "path:XXXX": [
    #           [ "source_gene", "target_gene" ],
    #           ...
    #       ],
    #       ...
    #   },
    #   ...
    # }
    for outer_key, pathways in relations_data.items():
        for pathway, pairs in pathways.items():
            for pair in pairs:
                if len(pair) < 2:
                    continue
                src, tgt = pair[0], pair[1]
                if src not in direct_connections:
                    direct_connections[src] = set()
                direct_connections[src].add(tgt)
    
    # Convert sets to lists (sorting for consistency)
    direct_connections = { src: sorted(list(targets)) for src, targets in direct_connections.items() }
    
    # Write the output to a new JSON file
    with open("direct_connections.json", "w") as outfile:
        json.dump(direct_connections, outfile, indent=2)
    
    print("Output saved to direct_connections.json")

if __name__ == "__main__":
    main()