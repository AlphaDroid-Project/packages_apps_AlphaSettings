# SPDX-FileCopyrightText: 2026 AlphaDroid
# SPDX-License-Identifier: Apache-2.0
"""
Batch-translate AlphaSettings `alpha_strings.xml` locale files.

For each `<string>` where `translatable != false` and the locale value is still
identical to `res/values/alpha_strings.xml`, this script replaces the locale
text using Google Translate (via `deep-translator`), with:

- placeholder protection for Android format tokens (`%s`, `%d`, `%1$s`, `%%`)
- a small per-language post-processor for common short-string mistranslations
- a JSON cache under `/tmp` so re-runs are fast and resumable

Usage:
  python3 scripts/batch_translate_alpha_strings.py
  python3 scripts/batch_translate_alpha_strings.py --only de-rDE fr-rFR
"""

from __future__ import annotations

import argparse
import json
import re
import time
from pathlib import Path
from typing import Dict, Iterable, List, Optional, Set, Tuple

import xml.etree.ElementTree as ET
from deep_translator import GoogleTranslator

ET.register_namespace("xliff", "urn:oasis:names:tc:xliff:document:1.2")

RES_DIR = Path(__file__).resolve().parents[1] / "res"
DEFAULTS_FILE = RES_DIR / "values" / "alpha_strings.xml"
CACHE_PATH = Path("/tmp/alpha_strings_translate_cache.json")

# `values-*` folder suffix -> deep-translator Google target code
FOLDER_TO_CODE: Dict[str, str] = {
    "ar-rSA": "ar",
    "az-rAZ": "az",
    "be-rBY": "be",
    "bn-rBD": "bn",
    "bg-rBG": "bg",
    "ca-rES": "ca",
    "cs-rCZ": "cs",
    "da-rDK": "da",
    "de-rDE": "de",
    "el-rGR": "el",
    "es-rES": "es",
    "es-rVE": "es",
    "et-rEE": "et",
    "fa-rIR": "fa",
    "fi-rFI": "fi",
    "fr-rFR": "fr",
    "hi-rIN": "hi",
    "hr-rHR": "hr",
    "hu-rHU": "hu",
    "in-rID": "id",
    "it-rIT": "it",
    "iw-rIL": "iw",
    "ja-rJP": "ja",
    "ko-rKR": "ko",
    "ku-rTR": "ku",
    "nl-rNL": "nl",
    "pl-rPL": "pl",
    "pt-rBR": "pt",
    "ro-rRO": "ro",
    "ru-rRU": "ru",
    "sk-rSK": "sk",
    "sl-rSI": "sl",
    "sq-rAL": "sq",
    "sr-rCS": "sr",
    "sv-rSE": "sv",
    "ta-rIN": "ta",
    "tr-rTR": "tr",
    "uk-rUA": "uk",
    "ur-rPK": "ur",
    "vi-rVN": "vi",
    "zh-rCN": "zh-CN",
    "zh-rTW": "zh-TW",
}

# Exact English values we intentionally keep (brands, symbols, shared UI tokens).
KEEP_VALUES: Set[str] = {
    "AlphaDroid",
    "AlphaVisuals",
    "Android",
    "Android %s",
    "GitHub",
    "Launcher3",
    "Lawnchair",
    "Bluetooth",
    "VPN",
    "CRT",
    "Squircle",
    "Hotspot",
    "iOS",
    "IOS",
    "Cyberpunk",
    "Ciberpunk",
    "QS",
    "DR",
    "Linear",
    "Matrix",
    "Sepia",
}

# Keys where the English value is intentionally global (do not MT).
KEEP_KEYS: Set[str] = {
    "about_device_rom_title",
    "category_alpha_info",
    "category_android_info",
    "source_title",
    "top_level_alpha_category_title",
}

_PLACEHOLDER_RE = re.compile(
    r"(?:%\d+\$[sdfsu]|\%(?:s|d|f|u|c|i|o|x|X|p|n|%|e|E|g|G|a|A|h|H|l|L|z|Z|t|j|m|b|B|q|Q|v|V|w|W|y|Y))"
)


def protect_placeholders(text: str) -> Tuple[str, List[str]]:
    tokens: List[str] = []

    def repl(m: re.Match[str]) -> str:
        tokens.append(m.group(0))
        return f"⟦{len(tokens) - 1}⟧"

    return _PLACEHOLDER_RE.sub(repl, text), tokens


def restore_placeholders(text: str, tokens: List[str]) -> str:
    out = text
    for i, tok in enumerate(tokens):
        out = out.replace(f"⟦{i}⟧", tok)
    return out


def android_escape_value(s: str) -> str:
    # Preserve existing entities while escaping raw ampersands for valid XML.
    s = s.replace("&amp;", "\x00AMP\x00")
    s = s.replace("&", "&amp;")
    s = s.replace("\x00AMP\x00", "&amp;")

    s = s.replace("\\", "\\\\")
    s = s.replace("\n", "\\n")
    s = s.replace("\t", "\\t")
    s = s.replace("'", "\\'")
    s = s.replace('"', '\\"')
    return s


def postprocess(lang: str, key: str, orig: str, tr: str) -> str:
    """Fix common Google mistranslations for very short UI labels."""
    tr = tr.strip()

    if lang == "de":
        if key == "about_title" and orig == "About" and tr in {"Um", "Um."}:
            return "Info"
        if orig == "Advanced" and "Fortschrittlich" in tr:
            return tr.replace("Fortschrittlich", "Erweitert")
        if orig == "No":
            return tr.replace("NEIN", "Nein")
        if orig == "Misc" and tr.lower() == "verschiedenes":
            return "Sonstiges"
        if orig == "search" and tr.lower().startswith("suche"):
            return "suchen"

    if lang == "it":
        if orig == "No" and tr.upper() == "NO":
            return "No"

    if lang == "es":
        if orig == "No" and tr.upper() == "NO":
            return "No"

    if lang == "fr":
        if orig == "No" and tr.upper() == "NON":
            return "Non"

    if lang == "pl":
        if orig == "No" and tr.upper() == "NIE":
            return "Nie"

    return tr


def load_cache() -> Dict[str, Dict[str, str]]:
    if CACHE_PATH.is_file():
        try:
            return json.loads(CACHE_PATH.read_text(encoding="utf-8"))
        except json.JSONDecodeError:
            return {}
    return {}


def save_cache(cache: Dict[str, Dict[str, str]]) -> None:
    CACHE_PATH.write_text(json.dumps(cache, ensure_ascii=False, indent=2), encoding="utf-8")


def load_strings(path: Path) -> Dict[str, str]:
    tree = ET.parse(path)
    root = tree.getroot()
    out: Dict[str, str] = {}
    for el in root:
        tag = el.tag.split("}", 1)[-1] if "}" in el.tag else el.tag
        if tag != "string":
            continue
        name = el.get("name")
        if not name:
            continue
        if el.get("translatable", "true").lower() == "false":
            continue
        out[name] = "".join(el.itertext()).strip()
    return out


def should_skip(name: Optional[str], text: str) -> bool:
    if not text:
        return True
    if text.startswith("@"):
        return True
    if name is not None and name in KEEP_KEYS:
        return True
    if text in KEEP_VALUES:
        return True
    return False


def apply_string_replacements(xml: str, updates: Dict[str, str]) -> str:
    out = xml
    for name, new_plain in updates.items():
        esc = android_escape_value(new_plain)
        pat = re.compile(
            rf'(<string\b[^>]*\bname="{re.escape(name)}"[^>]*>)(.*?)(</string>)',
            re.DOTALL,
        )
        new_out, n = pat.subn(lambda m: m.group(1) + esc + m.group(3), out, count=1)
        if n != 1:
            raise RuntimeError(f"Could not replace <string name={name!r}> (matches={n})")
        out = new_out
    return out


def translate_locale(
    folder: str,
    *,
    en: Dict[str, str],
    cache_root: Dict[str, Dict[str, str]],
    sleep_s: float,
    dry_run: bool,
) -> Tuple[int, int]:
    """Returns (updated_strings, unique_mt_calls)."""
    lang_code = FOLDER_TO_CODE.get(folder)
    if not lang_code:
        raise RuntimeError(f"No language mapping for {folder}")

    loc_path = RES_DIR / f"values-{folder}" / "alpha_strings.xml"
    loc = load_strings(loc_path)

    keys_to_fix: List[str] = []
    for k, ev in en.items():
        if should_skip(k, ev):
            continue
        lv = loc.get(k)
        if lv is None:
            continue
        if lv != ev:
            continue
        keys_to_fix.append(k)

    if not keys_to_fix:
        return 0, 0

    if dry_run:
        uniq = len({en[k] for k in keys_to_fix})
        print(f"[dry-run] {folder}: keys={len(keys_to_fix)} unique_texts={uniq}", flush=True)
        return len(keys_to_fix), 0

    print(f"==> {folder} ({lang_code}) …", flush=True)
    lang_cache = cache_root.setdefault(lang_code, {})
    translator = GoogleTranslator(source="en", target=lang_code)

    text_map: Dict[str, str] = {}
    mt_calls = 0
    unique_texts = sorted({en[k] for k in keys_to_fix}, key=lambda s: (-len(s), s))
    total_unique = len(unique_texts)
    for idx, text in enumerate(unique_texts, start=1):
        rep_key = next(k for k in keys_to_fix if en[k] == text)
        if should_skip(rep_key, text):
            text_map[text] = text
            continue

        if text in lang_cache:
            text_map[text] = lang_cache[text]
            continue

        if idx == 1 or idx % 25 == 0:
            print(f"{folder}: translating {idx}/{total_unique} …", flush=True)

        protected, tokens = protect_placeholders(text)
        try:
            tr = translator.translate(protected)
        except Exception:
            time.sleep(max(sleep_s, 0.5))
            tr = translator.translate(protected)
        mt_calls += 1
        time.sleep(sleep_s)

        tr = restore_placeholders(tr, tokens)
        tr = postprocess(lang_code, rep_key, text, tr)
        lang_cache[text] = tr
        text_map[text] = tr

    updates: Dict[str, str] = {}
    for k in keys_to_fix:
        ev = en[k]
        if ev in text_map:
            updates[k] = text_map[ev]
        else:
            updates[k] = ev

    xml = loc_path.read_text(encoding="utf-8")
    new_xml = apply_string_replacements(xml, updates)
    loc_path.write_text(new_xml, encoding="utf-8")
    save_cache(cache_root)
    print(f"{folder}: updated {len(updates)} strings ({mt_calls} MT calls)", flush=True)
    return len(updates), mt_calls


def discover_folders(only: Optional[Iterable[str]]) -> List[str]:
    folders: List[str] = []
    for p in sorted(RES_DIR.glob("values-*/alpha_strings.xml")):
        folder = p.parent.name.removeprefix("values-")
        if folder in {"pt-rPT"}:
            continue
        if only is not None and folder not in set(only):
            continue
        if folder not in FOLDER_TO_CODE:
            continue
        folders.append(folder)
    return folders


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument(
        "--only",
        nargs="*",
        help="Limit to specific `de-rDE`-style folder suffixes (values-<suffix>).",
    )
    ap.add_argument("--sleep", type=float, default=0.1, help="Sleep between MT calls.")
    ap.add_argument(
        "--smallest-first",
        action="store_true",
        help="Process locales with the fewest remaining English-identical strings first.",
    )
    ap.add_argument("--dry-run", action="store_true")
    args = ap.parse_args()

    only = args.only if args.only else None
    folders = discover_folders(only)
    if not folders:
        raise SystemExit("No matching locale folders found.")

    en = load_strings(DEFAULTS_FILE)
    if args.smallest_first:

        def identical_count(folder: str) -> int:
            loc_path = RES_DIR / f"values-{folder}" / "alpha_strings.xml"
            loc = load_strings(loc_path)
            return sum(
                1
                for k, v in en.items()
                if v and not v.startswith("@") and loc.get(k) == v
            )

        folders.sort(key=identical_count)

    cache_root = load_cache()

    total_updates = 0
    total_mt = 0
    for folder in folders:
        u, m = translate_locale(folder, en=en, cache_root=cache_root, sleep_s=args.sleep, dry_run=args.dry_run)
        total_updates += u
        total_mt += m

    if not args.dry_run:
        save_cache(cache_root)

    print(f"Done. updated_strings={total_updates} mt_calls={total_mt}", flush=True)


if __name__ == "__main__":
    main()
