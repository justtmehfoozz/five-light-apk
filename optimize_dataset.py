import json
import gzip

with open("app/src/main/assets/quran_word_explorer.json", "r", encoding="utf-8") as f:
    data = json.load(f)

# Pack each word into compact tuple/list:
# [tokenIndex, exact, translit, meaning, lemma, root, pos]
compact_verses = {}
for vk, w_list in data["verses"].items():
    compact_verses[vk] = [
        [w["tokenIndex"], w["exact"], w["translit"], w["meaning"], w["lemma"], w["root"], w["pos"]]
        for w in w_list
    ]

# Sort occurrences numerically by surah and ayah (1:1, 1:2, 2:1, ...)
def sort_key(s_a):
    parts = s_a.split(":")
    return (int(parts[0]), int(parts[1]))

sorted_roots = {r: sorted(occs, key=sort_key) for r, occs in data["rootOccurrences"].items()}
sorted_lemmas = {l: sorted(occs, key=sort_key) for l, occs in data["lemmaOccurrences"].items()}

compact_data = {
    "v": compact_verses,
    "r": sorted_roots,
    "l": sorted_lemmas
}

with open("app/src/main/assets/quran_word_explorer.json", "w", encoding="utf-8") as f:
    json.dump(compact_data, f, separators=(',', ':'), ensure_ascii=False)

import os
size_mb = os.path.getsize("app/src/main/assets/quran_word_explorer.json") / (1024 * 1024)
print(f"Compact size: {size_mb:.2f} MB")
