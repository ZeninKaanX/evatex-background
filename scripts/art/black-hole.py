"""Generate EVATEX's original monochrome 64-frame black-hole glyph atlas."""
from pathlib import Path
import json,math,subprocess
ROOT=Path(__file__).resolve().parents[2]
frames=[]
for frame in range(64):
 phase=frame/64
 parts=['<g transform="rotate(-18 16 16)"><ellipse cx="16" cy="16" rx="13" ry="4.5" fill="none" stroke="white" stroke-opacity=".32" stroke-width="3"/>',f'<ellipse cx="16" cy="16" rx="13" ry="4.5" fill="none" stroke="white" stroke-width="1.6" stroke-dasharray="15 12 5 24" stroke-dashoffset="{-phase*56:.3f}"/></g>', '<circle cx="16" cy="15" r="6.5" fill="#050505" stroke="white" stroke-opacity=".25" stroke-width="3"/>','<circle cx="16" cy="15" r="6.5" fill="#050505" stroke="white" stroke-width="1.3"/>']
 for i in range(4):
  t=(phase+i/4)%1; angle=(i/4+t*1.3)*math.tau; radius=12.5-6*t
  x=16+math.cos(angle)*radius;y=15+math.sin(angle)*radius*.64
  alpha=math.sin(t*math.pi)*.8
  parts.append(f'<circle cx="{x:.3f}" cy="{y:.3f}" r="{.75-.25*t:.3f}" fill="white" fill-opacity="{alpha:.3f}"/>')
 parts += ['<g transform="rotate(-18 16 16)"><path d="M3 16a13 4.5 0 0 0 26 0" fill="none" stroke="white" stroke-width="1.8" stroke-linecap="round"/></g>']
 frames.append(''.join(parts))
svg='<svg xmlns="http://www.w3.org/2000/svg" width="256" height="256" viewBox="0 0 256 256">'+''.join(f'<g transform="translate({(i%8)*32} {(i//8)*32})">{s}</g>' for i,s in enumerate(frames))+'</svg>\n'
source=ROOT/'scripts/art/black-hole-atlas.svg';source.write_text(svg)
subprocess.run(['rsvg-convert',str(source),'-o',str(ROOT/'assets/evatexbackgroundvideo/textures/font/black_hole.png')],check=True)
font={'providers':[{'type':'bitmap','file':'evatexbackgroundvideo:font/black_hole.png','height':14,'ascent':7,'chars':[''.join(chr(0xe100+y*8+x) for x in range(8)) for y in range(8)]}]}
(ROOT/'assets/evatexbackgroundvideo/font/brand.json').write_text(json.dumps(font,indent=2)+'\n')
