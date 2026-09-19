with open("quranic-corpus-morphology-0.4.txt", "r", encoding="utf-8") as f:
    for line in f:
        if line.startswith("(37:130:"):
            print(line.strip())
