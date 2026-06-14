#!/usr/bin/env python3
"""Gera ícone PNG do SolanaForge usando apenas stdlib Python"""
import struct, zlib, os, math

SIZE = 256

def make_png(pixels):
    def chunk(name, data):
        c = struct.pack('>I', len(data)) + name + data
        return c + struct.pack('>I', zlib.crc32(name + data) & 0xffffffff)

    raw = b''
    for row in pixels:
        raw += b'\x00'
        for r,g,b,a in row:
            raw += bytes([r,g,b,a])

    ihdr = struct.pack('>IIBBBBB', SIZE, SIZE, 8, 6, 0, 0, 0)
    idat = zlib.compress(raw, 9)

    return (b'\x89PNG\r\n\x1a\n' +
            chunk(b'IHDR', ihdr) +
            chunk(b'IDAT', idat) +
            chunk(b'IEND', b''))

def lerp(a, b, t):
    return int(a + (b - a) * t)

def lerp_color(c1, c2, t):
    return tuple(lerp(c1[i], c2[i], t) for i in range(4))

# Cria grid de pixels
pixels = [[(0,0,0,0)]*SIZE for _ in range(SIZE)]

# Fundo dark purple
for y in range(SIZE):
    for x in range(SIZE):
        t = y / SIZE
        dx = x - SIZE//2
        dy = y - SIZE//2
        dist = math.sqrt(dx*dx + dy*dy)
        r_corner = 28
        # Cantos arredondados
        cx, cy = x, y
        in_corner = False
        corners = [(r_corner, r_corner), (SIZE-r_corner, r_corner),
                   (r_corner, SIZE-r_corner), (SIZE-r_corner, SIZE-r_corner)]
        near = False
        for ccx, ccy in corners:
            if abs(cx - ccx) <= r_corner and abs(cy - ccy) <= r_corner:
                near = True
                if math.sqrt((cx-ccx)**2 + (cy-ccy)**2) > r_corner:
                    in_corner = True
        if in_corner:
            continue

        # Gradiente fundo
        r = lerp(18, 30, t)
        g = lerp(8, 15, t)
        b = lerp(46, 70, t)
        pixels[y][x] = (r, g, b, 255)

# Borda neon
for i in range(3):
    col = [(120,40,220), (80,30,200), (60,20,180)][i]
    for y in range(SIZE):
        for x in range(SIZE):
            if pixels[y][x][3] == 0: continue
            on_edge = (x == i or x == SIZE-1-i or y == i or y == SIZE-1-i)
            if on_edge:
                pixels[y][x] = (*col, 200 - i*50)

def fill_rect(x0, y0, x1, y1, color):
    for y in range(max(0,y0), min(SIZE,y1+1)):
        for x in range(max(0,x0), min(SIZE,x1+1)):
            if pixels[y][x][3] > 0:
                pixels[y][x] = color

def draw_parallelogram(cx, cy, w, h, skew, color, glow_color=None):
    """Desenha um paralelogramo (barra diagonal do logo Solana)"""
    for dy in range(-h//2, h//2+1):
        offset = int(skew * (dy / (h/2))) if h > 0 else 0
        for dx in range(-w//2 + offset, w//2 + offset + 1):
            px, py = cx + dx, cy + dy
            if 0 <= px < SIZE and 0 <= py < SIZE and pixels[py][px][3] > 0:
                pixels[py][px] = color
    # Glow simples (borda)
    if glow_color:
        for dy in range(-h//2-3, h//2+4):
            offset = int(skew * (dy / (h/2))) if h > 0 else 0
            for dx in range(-w//2 + offset - 3, w//2 + offset + 4):
                px, py = cx + dx, cy + dy
                if 0 <= px < SIZE and 0 <= py < SIZE and pixels[py][px][3] > 0:
                    cur = pixels[py][px]
                    if cur != color:
                        pixels[py][px] = tuple(min(255, c + g//3) for c,g in zip(cur[:3], glow_color[:3])) + (255,)

# 3 barras do logo Solana (topo, meio, base)
bars = [
    (128, 82,  130, 20, -18, (153, 69, 255, 255),  (153, 69, 255)),   # roxo
    (128, 112, 130, 20, -18, (70, 160, 255, 255),   (70, 160, 255)),   # azul
    (128, 142, 130, 20, -18, (20, 241, 149, 255),   (20, 241, 149)),   # verde-ciano
]
for cx, cy, w, h, skew, color, gc in bars:
    draw_parallelogram(cx, cy, w, h, skew, color, gc)

# Bigorna (pixel art simples)
ANVIL = (55, 35, 75, 255)
GOLD  = (255, 185, 0, 255)

# Cabeça da bigorna
fill_rect(80, 164, 176, 182, ANVIL)
fill_rect(88, 156, 168, 167, ANVIL)
# Pescoço
fill_rect(96, 182, 160, 200, ANVIL)
# Base
fill_rect(78, 200, 178, 216, ANVIL)
# Detalhes dourados
fill_rect(80, 164, 176, 168, GOLD)
fill_rect(78, 200, 178, 204, GOLD)
fill_rect(96, 182, 101, 200, GOLD)
fill_rect(155, 182, 160, 200, GOLD)

# Salvar
out = os.path.join(os.path.dirname(os.path.abspath(__file__)),
                   "src/main/resources/solanaforge.png")
with open(out, 'wb') as f:
    f.write(make_png(pixels))
print(f"Ícone gerado: {out}")
