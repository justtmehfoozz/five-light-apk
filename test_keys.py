import json
with open("app/src/main/assets/quran_complete.json", "r", encoding="utf-8") as f:
    quran = json.load(f)
print("Keys of first verse in surah 1:", quran["1"][0].keys())
print("First verse:", quran["1"][0])
