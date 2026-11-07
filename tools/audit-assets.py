"""Check the exact offline content shipped in the APK before packaging."""
import array
import hashlib
import json
import pathlib
import wave

root = pathlib.Path(__file__).resolve().parents[1]
assets = root / "app/src/main/assets"
words = json.loads((assets / "words.json").read_text(encoding="utf-8"))
assert len(words) == 104 and len({word["id"] for word in words}) == 104
for letter in "ABCDEFGHIJKLMNOPQRSTUVWXYZ":
    assert sum(word["letter"] == letter for word in words) == 4, letter
art = (root / "app/src/main/java/tw/littlesprout/island/IslandArt.kt").read_text(encoding="utf-8")
assert all('"' + word["id"] + '"' in art for word in words)
zhuyin = json.loads((assets / "zhuyin.json").read_text(encoding="utf-8"))
assert len(zhuyin) == 37
for symbol, data in zhuyin.items():
    assert data["strokes"] and len(data["strokes"]) == len(data["outlines"]), symbol
    assert all(0 <= value <= 1 for stroke in data["strokes"] for point in stroke for value in point)
manifest = json.loads((assets / "audio-manifest.json").read_text(encoding="utf-8"))
assert manifest["status"] == "offline-neural-speech-generated"
expected = {f"{item['lang']}/{item['id']}.wav" for item in manifest["items"]}
expected.update(f"zh/zhuyin_{ord(symbol):x}.wav" for symbol in zhuyin)
actual = {str(file.relative_to(assets / "audio")).replace("\\", "/") for file in (assets / "audio").rglob("*.wav")}
assert actual == expected, (expected - actual, actual - expected)
audios = []
for name in sorted(expected):
    path = assets / "audio" / name
    with wave.open(str(path), "rb") as file:
        assert file.getnchannels() in (1, 2) and file.getsampwidth() in (1, 2), name
        duration = file.getnframes() / file.getframerate()
        assert duration >= 0.1, (name, duration)
        raw = file.readframes(file.getnframes())
        if file.getsampwidth() == 2:
            samples = array.array("h", raw)
            peak = max(abs(value) for value in samples)
        else:
            peak = max(abs(value - 128) * 256 for value in raw)
        assert peak >= 50, (name, "silent")
    audios.append({"file": name, "seconds": round(duration, 3), "peak": peak, "sha256": hashlib.sha256(path.read_bytes()).hexdigest()})
report = {"word_cards": 104, "letters": 26, "official_zhuyin": 37, "audio_files": len(audios), "audio": audios}
destination = root / "delivery/asset-audit.json"
destination.parent.mkdir(exist_ok=True)
destination.write_text(json.dumps(report, indent=2), encoding="utf-8")
print(f"PASS: 104 cards, 26 letter groups, 37 official symbols, {len(audios)} non-silent offline recordings")
