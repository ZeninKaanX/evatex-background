#!/usr/bin/env python3
"""Compile the online catalogue; media stays outside the mod jar."""
import argparse
import hashlib
import json
from pathlib import Path
from urllib.parse import urlsplit

ROOT = Path(__file__).resolve().parent.parent

def compile_catalog(directory):
    root = json.loads((directory / 'base.json').read_text(encoding='utf-8'))
    if root.get('schemaVersion') != 1 or root.get('catalogVersion', 0) < 3:
        raise ValueError('Unsupported catalogue version')
    entries = list(root['entries'])
    for path in sorted((directory / 'backgrounds').glob('*.json')):
        entries.append(json.loads(path.read_text(encoding='utf-8')))
    if not 1 <= len(entries) <= 1000:
        raise ValueError('Catalogue must contain 1–1000 entries')
    import re
    ids = set()
    for entry in entries:
        ident = entry['id']
        if not re.fullmatch('[a-z0-9-]{1,120}', ident) or ident in ids:
            raise ValueError(f'Invalid or duplicate id: {ident}')
        ids.add(ident)
        if entry['category'] not in {'anime', 'astral', 'nature', 'games', 'minimal'} or entry['kind'] not in {'video', 'image', 'gif'}:
            raise ValueError(f'Invalid category or kind: {ident}')
        for key in ['sourceUrl', 'thumbnailUrl', 'downloadUrl']:
            if value := entry.get(key):
                u = urlsplit(value)
                if u.scheme != 'https' or not u.hostname or u.username or u.port not in (None, 443) or u.hostname == 'localhost' or re.fullmatch('[0-9.:]+', u.hostname):
                    raise ValueError(f'Expected public HTTPS URL: {ident}/{key}')
        if any(isinstance(v, str) and len(v) > 2048 for v in entry.values()):
            raise ValueError(f'Text too long: {ident}')
        if not entry.get('builtin'):
            if not 0 < entry.get('bytes', 0) <= 512 * 1024 * 1024:
                raise ValueError(f'Missing or excessive download size: {ident}')
            if not entry.get('downloadUrl') or not entry.get('creator') or not entry.get('license'):
                raise ValueError(f'Missing download or credit: {ident}')
            if entry.get('redistributionAllowed'):
                if not re.fullmatch('[a-fA-F0-9]{64}', entry.get('sha256', '')):
                    raise ValueError(f'Missing SHA-256: {ident}')
            elif not (re.fullmatch('[a-fA-F0-9]{40}', entry.get('gitBlobSha1', '')) and re.fullmatch(r'https://raw.githubusercontent.com/[^/]+/[^/]+/[a-fA-F0-9]{40}/.+', entry['downloadUrl'])):
                raise ValueError(f'Unverified personal download: {ident}')
    # Interleave categories so discovery opens with a varied selection.
    buckets = [[e for e in entries if e['category'] == category] for category in ['anime', 'nature', 'astral', 'games', 'minimal']]
    entries = [bucket[i] for i in range(max(map(len, buckets))) for bucket in buckets if i < len(bucket)]
    root['entries'] = entries
    root['revision'] = hashlib.sha256(json.dumps(entries, sort_keys=True, ensure_ascii=False).encode()).hexdigest()[:20]
    output = json.dumps(root, ensure_ascii=False, indent=2) + '\n'
    if len(output.encode()) > 1024 * 1024:
        raise ValueError('Catalogue exceeds client limit')
    return output

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--bundled', action='store_true', help='Also update the offline seed for the next mod release')
    args = parser.parse_args()
    output = compile_catalog(ROOT / 'catalog')
    (ROOT / 'catalog/v1.json').write_text(output, encoding='utf-8')
    if args.bundled:
        (ROOT / 'assets/evatexbackgroundvideo/catalog/v1.json').write_text(output, encoding='utf-8')
    print(f'Validated {len(json.loads(output)["entries"])} backgrounds')

if __name__ == '__main__':
    main()
