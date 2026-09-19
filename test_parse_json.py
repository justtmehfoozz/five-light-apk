import json

with open("app/src/main/assets/quran_word_explorer.json", "r", encoding="utf-8") as f:
    d = json.load(f)

v_map = d["v"]
r_map = d["r"]
l_map = d["l"]

print("1:1 words count:", len(v_map["1:1"]))
print("Sample word from 1:1:", v_map["1:1"][0])
print("Root 'كتب' occ count:", len(r_map.get("كتب", [])))
print("Sample occ:", r_map.get("كتب", [])[:5])
