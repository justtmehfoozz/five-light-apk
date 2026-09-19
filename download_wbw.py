import urllib.request
import json
import time

all_words = {} # (s, a, w) -> {"translit": ..., "meaning": ...}

# Let's test downloading a few chapters or see how fast
print("Testing chapter 1...")
t0 = time.time()
url = "https://api.quran.com/api/v4/verses/by_chapter/1?language=en&words=true&word_fields=translation,transliteration,location&per_page=50"
req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
with urllib.request.urlopen(req, timeout=10) as resp:
    d = json.loads(resp.read().decode('utf-8'))
    print(f"Surah 1 fetched in {time.time() - t0:.2f}s, verses count: {len(d['verses'])}")
