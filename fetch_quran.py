import urllib.request
import json
import os

print("Fetching Arabic...")
req_ar = urllib.request.Request('http://api.alquran.cloud/v1/quran/quran-uthmani')
with urllib.request.urlopen(req_ar) as response:
    ar_data = json.loads(response.read().decode('utf-8'))['data']['surahs']

print("Fetching English...")
req_en = urllib.request.Request('http://api.alquran.cloud/v1/quran/en.sahih')
with urllib.request.urlopen(req_en) as response:
    en_data = json.loads(response.read().decode('utf-8'))['data']['surahs']

out_map = {}
bismillah_ar = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ"
bismillah_en = "In the name of Allah, the Entirely Merciful, the Especially Merciful."

global_verse_count = 1

for s in range(1, 115):
    s_str = str(s)
    out_map[s_str] = []
    
    # Bismillah insertion
    if s not in (1, 9):
        out_map[s_str].append({
            "surahNumber": s,
            "verseNumber": 0,
            "textArabic": bismillah_ar,
            "textEnglish": bismillah_en,
            "audioUrl": "https://cdn.islamic.network/quran/audio/128/ar.alafasy/1.mp3"
        })
    
    ar_verses = ar_data[s-1]['ayahs']
    en_verses = en_data[s-1]['ayahs']
    
    for i in range(len(ar_verses)):
        v_num = ar_verses[i]['numberInSurah']
        global_num = ar_verses[i]['number']
        
        ar_text = ar_verses[i]['text']
        en_text = en_verses[i]['text']
        
        # Remove Bismillah from the first verse of Surahs 2-114 (except 9) if api.alquran.cloud includes it
        # api.alquran.cloud actually DOES include "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ " at the start of verse 1 for surahs 2-114 (except 9).
        # We need to strip it so it doesn't duplicate our verse 0.
        if s not in (1, 9) and v_num == 1:
            if ar_text.startswith("بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ "):
                ar_text = ar_text.replace("بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ ", "")
            elif ar_text.startswith(bismillah_ar):
                ar_text = ar_text.replace(bismillah_ar, "").strip()
        
        out_map[s_str].append({
            "surahNumber": s,
            "verseNumber": v_num,
            "textArabic": ar_text,
            "textEnglish": en_text,
            "audioUrl": f"https://cdn.islamic.network/quran/audio/128/ar.alafasy/{global_num}.mp3"
        })

print("Writing to file...")
with open('app/src/main/assets/quran_complete.json', 'w', encoding='utf-8') as f:
    json.dump(out_map, f, ensure_ascii=False)

print("Done!")
