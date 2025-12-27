import os
from PIL import Image

path = r"assets\spritesPokemones\charizardBack.png"

try:
    img = Image.open(path)
    print(f"Format: {img.format}")
    print(f"Size: {img.size}")
    print(f"Mode: {img.mode}")
    print(f"Is Animated: {getattr(img, 'is_animated', False)}")
    if getattr(img, 'is_animated', False):
        print(f"Frames: {img.n_frames}")
except Exception as e:
    print(f"Error: {e}")
