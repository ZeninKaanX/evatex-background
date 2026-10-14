import importlib.util
import json
from pathlib import Path
import tempfile
import unittest

spec = importlib.util.spec_from_file_location('catalog', Path(__file__).parents[1] / 'scripts/compile_catalog.py')
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)

class CatalogueCompilerTests(unittest.TestCase):
    def test_live_addition_removal_and_validation(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            (root/'backgrounds').mkdir()
            base = {'schemaVersion': 1, 'catalogVersion': 3, 'entries': [dict(id='original', title='Original', category='astral', kind='image', builtin=True)]}
            (root/'base.json').write_text(json.dumps(base))
            original = module.compile_catalog(root)
            entry = dict(id='new-anime', title='New', category='anime', kind='video', downloadUrl='https://example.com/a.mp4', bytes=1024, sha256='a'*64, creator='Artist', license='CC0', redistributionAllowed=True)
            file = root/'backgrounds/new.json'
            file.write_text(json.dumps(entry))
            updated = module.compile_catalog(root)
            self.assertEqual(updated, module.compile_catalog(root))
            self.assertEqual(len(json.loads(updated)['entries']), 2)
            self.assertNotEqual(json.loads(original)['revision'], json.loads(updated)['revision'])
            for patch in [dict(id='original'), dict(downloadUrl='http://example.com/a'), dict(sha256='invalid'), dict(bytes=2**30)]:
                file.write_text(json.dumps(entry | patch))
                with self.assertRaises(ValueError): module.compile_catalog(root)
            file.unlink()
            self.assertEqual(original, module.compile_catalog(root))
