import json

with open("app/src/main/assets/quran_word_explorer.json", "r", encoding="utf-8") as f:
    data = json.load(f)

print("Verses count in explorer:", len(data["verses"]))
print("Roots count:", len(data["rootOccurrences"]))
print("Lemmas count:", len(data["lemmaOccurrences"]))

# Check 1:1
print("\n--- 1:1 Words ---")
for w in data["verses"]["1:1"]:
    print(w)

# Check 2:2
print("\n--- 2:2 Words ---")
for w in data["verses"]["2:2"]:
    print(w)

# Check root 'كتب' occurrences
print("\n--- Occurrences of root كتب ---")
occ = data["rootOccurrences"].get("كتب", [])
print(f"Total occurrences: {len(occ)}")
print("First 10 occurrences:", occ[:10])

# Check a late surah: 114:1 (An-Nas)
print("\n--- 114:1 Words ---")
for w in data["verses"]["114:1"]:
    print(w)

# Check Surah 36:1 (Ya-Sin)
print("\n--- 36:1 Words ---")
for w in data["verses"]["36:1"]:
    print(w)

# Check Surah 67:1 (Al-Mulk)
print("\n--- 67:1 Words ---")
for w in data["verses"]["67:1"]:
    print(w)
