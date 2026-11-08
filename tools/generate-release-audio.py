"""Render distributable offline audio with the MIT-licensed MeloTTS model.

Requires sherpa-onnx in build/tts-runtime and the upstream model in build/materials.
The synthesis runtime and model weights are not embedded in the Android app.
"""
import array
import json
import pathlib
import shutil
import sys
import wave

ROOT = pathlib.Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "build/tts-runtime-v1"))
import sherpa_onnx
from opencc import OpenCC

model = ROOT / "build/materials/vits-melo-tts-zh_en"
lexicon = (model / "lexicon.txt").read_text(encoding="utf-8")
entries = {line.split(maxsplit=1)[0]: line.split(maxsplit=1)[1] for line in lexicon.splitlines() if len(line.split(maxsplit=1)) == 2}
for letter in "abcdefghijklmnopqrstuvwxyz":
    # English article 'a' has a different reading from the letter name.
    pronunciation = "ey 9" if letter == "a" else entries[letter]
    lexicon += f"\nsproutletter{letter} {pronunciation}\n"
release_lexicon = model / "sprout-lexicon.txt"
release_lexicon.write_text(lexicon, encoding="utf-8")
config = sherpa_onnx.OfflineTtsConfig(
    model=sherpa_onnx.OfflineTtsModelConfig(
        vits=sherpa_onnx.OfflineTtsVitsModelConfig(
            model=str(model / "model.onnx"), tokens=str(model / "tokens.txt"),
            lexicon=str(release_lexicon), dict_dir=str(model / "dict"),
            noise_scale=0.4, noise_scale_w=0.5,
        ), num_threads=4,
    ), rule_fsts=str(model / "number.fst"),
)
assert config.validate(), "Invalid offline synthesis configuration"
tts = sherpa_onnx.OfflineTts(config)
english_model = ROOT / "build/materials/vits-piper-en_US-ljspeech-medium-int8"
english_config = sherpa_onnx.OfflineTtsConfig(model=sherpa_onnx.OfflineTtsModelConfig(
    vits=sherpa_onnx.OfflineTtsVitsModelConfig(
        model=str(english_model / "en_US-ljspeech-medium.onnx"),
        tokens=str(english_model / "tokens.txt"), data_dir=str(english_model / "espeak-ng-data"),
        noise_scale=0.4, noise_scale_w=0.5,
    ), num_threads=4,
))
assert english_config.validate()
english_tts = sherpa_onnx.OfflineTts(english_config)
if "--probe" in sys.argv:
    for text in ["egg.", "apple.", "A.", "B.", "W."]:
        audio = english_tts.generate(text, sid=0, speed=0.85)
        print(repr(text), len(audio.samples), audio.sample_rate, flush=True)
    for text in ["馬。", "馬，馬。", "這是馬。馬。", "猫，猫。"]:
        audio = tts.generate(text, sid=0, speed=0.8)
        print(repr(text), len(audio.samples), audio.sample_rate, flush=True)
    raise SystemExit(0)
assets = ROOT / "app/src/main/assets"
manifest = json.loads((assets / "audio-manifest.json").read_text(encoding="utf-8"))
converter = OpenCC("t2s")
for index, item in enumerate(manifest["items"]):
    output = assets / "audio" / item["lang"] / (item["id"] + ".wav")
    if output.exists() and output.stat().st_size > 500:
        continue
    text = item["text"]
    if item["lang"] == "zh":
        text = converter.convert(text)
        if item["id"].startswith("word_"):
            text = text.split("（")[0]
            text = "这是" + text + "。" + text
    audio = (english_tts if item["lang"] == "en" else tts).generate(text + ".", sid=0, speed=0.85)
    if len(audio.samples) < audio.sample_rate * 0.12:
        raise RuntimeError(f"Empty or truncated speech: {item['lang']}/{item['id']}")
    samples = array.array("h", (int(max(-1, min(1, float(v))) * 32767) for v in audio.samples))
    with wave.open(str(output), "wb") as file:
        file.setnchannels(1)
        file.setsampwidth(2)
        file.setframerate(audio.sample_rate)
        file.writeframes(samples.tobytes())
    print(f"Rendered [{item['lang']}] {item['id']}: {item['text']}", flush=True)
shutil.copyfile(model / "LICENSE", assets / "legal/melotts-MIT.txt")
shutil.copyfile(english_model / "MODEL_CARD", assets / "legal/ljspeech-model-card.txt")
manifest["status"] = "offline-neural-speech-generated"
manifest["source"] = "Chinese: MeloTTS, MyShell.ai, MIT. English: Piper LJSpeech medium, trained from scratch on public-domain LJ Speech. Rendered offline with sherpa-onnx. Official Zhuyin: Taiwan MOE, CC BY 4.0."
manifest["model_source"] = "https://huggingface.co/myshell-ai/MeloTTS-Chinese"
(assets / "audio-manifest.json").write_text(json.dumps(manifest, ensure_ascii=False, indent=2), encoding="utf-8")
print("All release audio rendered; official Zhuyin files unchanged.", flush=True)
