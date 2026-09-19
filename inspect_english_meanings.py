import urllib.request
import json

url = 'https://api.quran.com/api/v4/verses/by_chapter/1?language=en&words=true&word_fields=text_uthmani,translation,transliteration,location'
req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
with urllib.request.urlopen(req, timeout=10) as resp:
    d = json.loads(resp.read().decode('utf-8'))

for v in d['verses']:
    print(f"Verse {v['verse_key']}:")
    for w in v['words']:
        if w.get('char_type_name') == 'word':
            print(f"  {w['location']}: {w.get('text_uthmani') or w.get('text')} | {w.get('transliteration', {}).get('text')} | {w.get('translation', {}).get('text')}")
