import wave
import struct
import math
import random
import os

def make_wav(filename, duration_sec, generator_func):
    sample_rate = 44100
    n_samples = int(sample_rate * duration_sec)
    
    with wave.open(filename, 'wb') as wav:
        wav.setnchannels(1) # mono
        wav.setsampwidth(2) # 16-bit
        wav.setframerate(sample_rate)
        
        frames = bytearray()
        for i in range(n_samples):
            t = i / sample_rate
            norm_t = i / n_samples
            val = generator_func(t, norm_t)
            val = max(-1.0, min(1.0, val))
            sample = int(val * 32767)
            frames.extend(struct.pack('<h', sample))
        wav.writeframes(frames)

out_dir = 'app/src/main/res/raw'

def gen_soft_tick(t, nt):
    env = math.exp(-35.0 * nt)
    return math.sin(2.0 * math.pi * 2200 * t) * env * 0.45

def gen_gentle_tap(t, nt):
    env = math.exp(-25.0 * nt)
    body = math.sin(2.0 * math.pi * 520 * t) * 0.7 + math.sin(2.0 * math.pi * 260 * t) * 0.3
    return body * env * 0.55

def gen_wooden_tap(t, nt):
    env = math.exp(-22.0 * nt)
    body = math.sin(2.0 * math.pi * 380 * t) * 0.6 + math.sin(2.0 * math.pi * 760 * t) * 0.25 + (random.uniform(-1, 1) * 0.15 if nt < 0.1 else 0)
    return body * env * 0.6

def gen_soft_click(t, nt):
    env = math.exp(-40.0 * nt)
    click = math.sin(2.0 * math.pi * 3200 * t) * 0.5 + math.sin(2.0 * math.pi * 1600 * t) * 0.3 + (random.uniform(-1, 1) * 0.2 if nt < 0.2 else 0)
    return click * env * 0.4

def gen_digital(t, nt):
    env = math.exp(-20.0 * nt)
    return (math.sin(2.0 * math.pi * 1800 * t) * 0.6 + math.sin(2.0 * math.pi * 3600 * t) * 0.4) * env * 0.4

if __name__ == '__main__':
    make_wav(f'{out_dir}/soft_tick.wav', 0.04, gen_soft_tick)
    make_wav(f'{out_dir}/gentle_tap.wav', 0.06, gen_gentle_tap)
    make_wav(f'{out_dir}/wooden_tap.wav', 0.08, gen_wooden_tap)
    make_wav(f'{out_dir}/soft_click.wav', 0.03, gen_soft_click)
    make_wav(f'{out_dir}/digital.wav', 0.05, gen_digital)
    print('Generated audio successfully')
