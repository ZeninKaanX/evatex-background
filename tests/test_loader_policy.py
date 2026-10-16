"""Guard the release's advertised floor against hidden embedded requirements."""
import sys
import unittest
from pathlib import Path
sys.path.insert(0, str(Path(__file__).resolve().parents[1] / 'scripts'))
from loader_policy import minimum_loader, check_embedded_loader_requirement


class EmbeddedLoaderRequirements(unittest.TestCase):
    def test_stricter_module_cannot_be_published(self):
        with self.assertRaisesRegex(ValueError, 'fabric-resource-loader'):
            check_embedded_loader_requirement({
                'id': 'fabric-resource-loader',
                'depends': {'fabricloader': '>=0.18.4'},
            }, '1.21.11')

    def test_unknown_predicate_requires_review(self):
        with self.assertRaisesRegex(ValueError, 'predicate'):
            check_embedded_loader_requirement({
                'id': 'future-module', 'depends': {'fabricloader': ['>=0.18.4']},
            }, '1.21.11')

    def test_new_game_cannot_silently_inherit_old_floor(self):
        with self.assertRaisesRegex(ValueError, 'Review'):
            minimum_loader('26.4')


if __name__ == '__main__':
    unittest.main()
