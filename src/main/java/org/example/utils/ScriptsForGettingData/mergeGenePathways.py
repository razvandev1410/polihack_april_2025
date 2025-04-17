import json
import glob
import os

def main():
    # Prepare a dictionary to hold the merged data
    merged_data = {
        "gene_to_pathways": {},
        "unique_pathways": set()
    }

    # Look for all JSON files that match the naming pattern pathways_output_batch_*.json
    json_files = ['data/pathways_output_batch_0.json',
                  'data/pathways_output_batch_1.json',
                  'data/pathways_output_batch_2.json',
                  'data/pathways_output_batch_3.json',
                  'data/pathways_output_batch_4.json',
                  'data/pathways_output_batch_5.json',
                  'data/pathways_output_batch_6.json',
                  'data/pathways_output_batch_7.json',
                  'data/pathways_output_batch_8.json',
                  'data/pathways_output_batch_9.json',
                  'data/pathways_output_batch_10.json',
                  'data/pathways_output_batch_11.json',
                  'data/pathways_output_batch_12.json'
                  ]

    # Process each file
    for json_file in json_files:
        print(f"Merging data from {json_file}...")
        with open(json_file, "r") as f:
            data = json.load(f)

        # Each file should have the structure:
        # {
        #   "gene_to_pathways": { "hsa:1234": [...], "hsa:5678": [...] },
        #   "unique_pathways": ["pathway:hsa04110", "pathway:hsa04520", ...]
        # }

        gene_to_pathways = data.get("gene_to_pathways", {})
        unique_pathways = data.get("unique_pathways", [])

        # Merge gene_to_pathways
        for gene_id, pathways in gene_to_pathways.items():
            # If this gene ID hasn't been seen, initialize a set
            if gene_id not in merged_data["gene_to_pathways"]:
                merged_data["gene_to_pathways"][gene_id] = set()

            # Add the pathways for this gene (use a set to avoid duplicates)
            merged_data["gene_to_pathways"][gene_id].update(pathways)

        # Merge unique_pathways
        merged_data["unique_pathways"].update(unique_pathways)

    # Convert each set in gene_to_pathways back to a list
    for gene_id, pathway_set in merged_data["gene_to_pathways"].items():
        merged_data["gene_to_pathways"][gene_id] = list(pathway_set)

    # Convert the unique_pathways set to a list
    merged_data["unique_pathways"] = list(merged_data["unique_pathways"])

    # Save the merged results to a new JSON file
    output_filename = "pathways_output.json"
    with open(output_filename, "w") as outfile:
        json.dump(merged_data, outfile, indent=4)
    
    print(f"\nMerged output saved to: {output_filename}")

if __name__ == "__main__":
    main()
