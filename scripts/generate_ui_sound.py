#!/usr/bin/env python3
"""Generate EVATEX's original, quiet Astral UI click. No sampled third-party audio."""
import math, random, struct, subprocess, tempfile, wave
from pathlib import Path
ROOT=Path(__file__).resolve().parent.parent

def samples():
    rate=48000;rng=random.Random(86415);previous=0.0;out=[]
    for n in range(int(rate*.032)):
        t=n/rate;attack=1-math.exp(-t/.0007)
        # Short dry actuation: no pitch sweep or ringing tail.
        body=.20*math.sin(2*math.pi*(420*t))*math.exp(-t/.003)
        glass=.025*math.sin(2*math.pi*(180*t))*math.exp(-t/.004)
        noise=rng.uniform(-1,1);edge=.055*(noise-previous)*math.exp(-t/.0015);previous=noise
        tail=min(1,max(0,(.032-t)/.004))
        out.append((body+glass+edge)*attack*tail)
    return rate,out

def write(path,rate,values):
    with wave.open(str(path),'wb') as f:
        f.setparams((1,2,rate,0,'NONE','not compressed'))
        f.writeframes(b''.join(struct.pack('<h',round(max(-1,min(1,v))*32767)) for v in values))

if __name__=='__main__':
    target=ROOT/'assets/evatexbackgroundvideo/sounds/ui/astral_click.ogg';target.parent.mkdir(parents=True,exist_ok=True)
    rate,data=samples()
    with tempfile.TemporaryDirectory() as temp:
        source=Path(temp)/'click.wav';write(source,rate,data)
        subprocess.run(['ffmpeg','-nostdin','-hide_banner','-loglevel','error','-y','-i',str(source),'-c:a','libvorbis','-q:a','5',str(target)],check=True)
    print(f'Generated original Astral click: {len(data)/rate:.3f}s, peak {20*math.log10(max(map(abs,data))):.1f} dBFS')
