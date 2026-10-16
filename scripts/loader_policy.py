"""Fabric runtime floors, shared by packaging and release verification.

Keep older game builds installable without inheriting a newer game's Loader
requirement. Floors must also satisfy every bundled Fabric module.
"""
def minimum_loader(minecraft):
    if minecraft == '26.3':
        return '0.19.3'
    if minecraft in ('26.1', '26.1.1', '26.1.2', '26.2'):
        return '0.18.4'
    if minecraft == '1.21.11':
        return '0.17.3'
    if minecraft in ('1.21.9', '1.21.10'):
        return '0.17.0'
    legacy = ('1.20', '1.20.1', '1.20.2', '1.20.3', '1.20.4', '1.20.5',
              '1.20.6', '1.21', '1.21.1', '1.21.2', '1.21.3', '1.21.4',
              '1.21.5', '1.21.6', '1.21.7', '1.21.8')
    if minecraft in legacy:
        return '0.16.14'
    raise ValueError('Review Fabric Loader requirements for Minecraft ' + minecraft)


def check_embedded_loader_requirement(metadata, minecraft):
    """Reject an advertised floor that a bundled module cannot actually use."""
    required = metadata.get('depends', {}).get('fabricloader', '*')
    if required == '*':
        return
    if not isinstance(required, str) or not required.startswith('>='):
        raise ValueError('Review unsupported embedded Loader predicate: ' + str(required))
    numeric = lambda version: tuple(int(part) for part in version.split('.'))
    if numeric(minimum_loader(minecraft)) < numeric(required[2:]):
        raise ValueError(f'{minecraft}: {metadata["id"]} requires Loader {required}')
