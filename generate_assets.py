#!/usr/bin/env python3
"""
Generate all JSON and PNG assets for new material bases in blocky13.
Run from the project root.
"""
import os
import sys
import json

ROOT = os.path.dirname(os.path.abspath(__file__))
ASSETS   = os.path.join(ROOT, "src/main/resources/assets/blocky13")
DATA     = os.path.join(ROOT, "src/main/resources/data/blocky13")

REF_CHAIN_BLOCK = os.path.join(ASSETS, "textures/block/iron_block_chain.png")
REF_BARS_BLOCK  = os.path.join(ASSETS, "textures/block/iron_block_bars.png")
REF_SLAB_ITEM   = os.path.join(ASSETS, "textures/item/iron_block_slab.png")
REF_DOOR_ITEM   = os.path.join(ASSETS, "textures/item/iron_block_door.png")
REF_CHAIN_ITEM  = os.path.join(ASSETS, "textures/item/iron_block_chain.png")
REF_BRICKS_BLOCK = os.path.join(ASSETS, "textures/block/reference_bricks.png")

# (base_id, mc_block_texture_suffix, avg_rgb, is_transparent)
MATERIALS = [
    # Concrete (16 colors)
    ("white_concrete",        "white_concrete",        (207, 213, 214), False),
    ("orange_concrete",       "orange_concrete",       (224,  97,   0), False),
    ("magenta_concrete",      "magenta_concrete",      (169,  48, 159), False),
    ("light_blue_concrete",   "light_blue_concrete",   ( 58, 175, 217), False),
    ("yellow_concrete",       "yellow_concrete",       (240, 175,  21), False),
    ("lime_concrete",         "lime_concrete",         ( 94, 168,  24), False),
    ("pink_concrete",         "pink_concrete",         (213, 101, 142), False),
    ("gray_concrete",         "gray_concrete",         ( 54,  57,  61), False),
    ("light_gray_concrete",   "light_gray_concrete",   (125, 125, 115), False),
    ("cyan_concrete",         "cyan_concrete",         ( 21, 119, 136), False),
    ("purple_concrete",       "purple_concrete",       (100,  31, 156), False),
    ("blue_concrete",         "blue_concrete",         ( 44,  46, 143), False),
    ("brown_concrete",        "brown_concrete",        ( 96,  59,  31), False),
    ("green_concrete",        "green_concrete",        ( 73,  91,  36), False),
    ("red_concrete",          "red_concrete",          (142,  33,  33), False),
    ("black_concrete",        "black_concrete",        (  8,  10,  15), False),
    # Terracotta (plain + 16 colors)
    ("terracotta",            "terracotta",            (150,  88,  67), False),
    ("white_terracotta",      "white_terracotta",      (209, 177, 161), False),
    ("orange_terracotta",     "orange_terracotta",     (162,  84,  38), False),
    ("magenta_terracotta",    "magenta_terracotta",    (149,  88, 108), False),
    ("light_blue_terracotta", "light_blue_terracotta", (113, 108, 137), False),
    ("yellow_terracotta",     "yellow_terracotta",     (186, 133,  35), False),
    ("lime_terracotta",       "lime_terracotta",       (103, 117,  52), False),
    ("pink_terracotta",       "pink_terracotta",       (161,  78,  78), False),
    ("gray_terracotta",       "gray_terracotta",       ( 57,  42,  35), False),
    ("light_gray_terracotta", "light_gray_terracotta", (135, 107,  98), False),
    ("cyan_terracotta",       "cyan_terracotta",       ( 86,  91,  91), False),
    ("purple_terracotta",     "purple_terracotta",     (118,  70,  86), False),
    ("blue_terracotta",       "blue_terracotta",       ( 74,  59,  91), False),
    ("brown_terracotta",      "brown_terracotta",      ( 77,  51,  35), False),
    ("green_terracotta",      "green_terracotta",      ( 76,  83,  42), False),
    ("red_terracotta",        "red_terracotta",        (143,  61,  46), False),
    ("black_terracotta",      "black_terracotta",      ( 37,  22,  16), False),
    # Glass (plain + 16 stained)
    ("glass",                 "glass",                 (196, 232, 255),  True),
    ("white_stained_glass",   "white_stained_glass",   (255, 255, 255),  True),
    ("orange_stained_glass",  "orange_stained_glass",  (216, 127,  51),  True),
    ("magenta_stained_glass", "magenta_stained_glass", (178,  76, 216),  True),
    ("light_blue_stained_glass","light_blue_stained_glass",(102,153,216),True),
    ("yellow_stained_glass",  "yellow_stained_glass",  (229, 229,  51),  True),
    ("lime_stained_glass",    "lime_stained_glass",    (127, 204,  25),  True),
    ("pink_stained_glass",    "pink_stained_glass",    (242, 127, 165),  True),
    ("gray_stained_glass",    "gray_stained_glass",    ( 76,  76,  76),  True),
    ("light_gray_stained_glass","light_gray_stained_glass",(153,153,153),True),
    ("cyan_stained_glass",    "cyan_stained_glass",    ( 76, 127, 153),  True),
    ("purple_stained_glass",  "purple_stained_glass",  (127,  63, 178),  True),
    ("blue_stained_glass",    "blue_stained_glass",    ( 51,  76, 178),  True),
    ("brown_stained_glass",   "brown_stained_glass",   (102,  76,  51),  True),
    ("green_stained_glass",   "green_stained_glass",   (102, 127,  51),  True),
    ("red_stained_glass",     "red_stained_glass",     (153,  51,  51),  True),
    ("black_stained_glass",   "black_stained_glass",   ( 25,  25,  25),  True),
]

VARIANTS = ["slab", "stairs", "fence", "fence_gate", "door", "trapdoor",
            "pressure_plate", "button", "chain", "bars"]

# DyeColor ordinal order: white=0, orange=1, magenta=2, light_blue=3, yellow=4, lime=5,
#   pink=6, gray=7, light_gray=8, cyan=9, purple=10, blue=11, brown=12, green=13, red=14, black=15
BRICKS_MATERIALS = [
    ("white_bricks",       (207, 213, 214)),
    ("orange_bricks",      (224,  97,   0)),
    ("magenta_bricks",     (169,  48, 159)),
    ("light_blue_bricks",  ( 58, 175, 217)),
    ("yellow_bricks",      (240, 175,  21)),
    ("lime_bricks",        ( 94, 168,  24)),
    ("pink_bricks",        (213, 101, 142)),
    ("gray_bricks",        ( 54,  57,  61)),
    ("light_gray_bricks",  (125, 125, 115)),
    ("cyan_bricks",        ( 21, 119, 136)),
    ("purple_bricks",      (100,  31, 156)),
    ("blue_bricks",        ( 44,  46, 143)),
    ("brown_bricks",       ( 96,  59,  31)),
    ("green_bricks",       ( 73,  91,  36)),
    ("red_bricks",         (142,  33,  33)),
    ("black_bricks",       (  8,  10,  15)),
]


def create_brick_reference(path):
    """Create a 16x16 grayscale brick pattern PNG at the given path."""
    from PIL import Image
    size = 16
    mortar = (80, 80, 80, 255)
    brick  = (200, 200, 200, 255)
    img = Image.new("RGBA", (size, size))
    pixels = []
    for y in range(size):
        row_offset = 4 if (y // 4) % 2 == 1 else 0
        for x in range(size):
            if y % 4 == 3:
                # horizontal mortar line
                pixels.append(mortar)
            elif (x + row_offset) % 8 == 0:
                # vertical mortar line (1px wide)
                pixels.append(mortar)
            else:
                pixels.append(brick)
    img.putdata(pixels)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)


def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(data, f, indent=2)


def title_name(base_id):
    return " ".join(w.capitalize() for w in base_id.split("_"))


def recolor_texture(ref_path, rgb, output_path, alpha=255):
    from PIL import Image
    ref = Image.open(ref_path).convert("RGBA")
    w, h = ref.size
    ref_pixels = list(ref.getdata())

    # Compute max luminance of reference (reference is grayscale: R≈G≈B)
    max_lum = max(((r + g + b) / 3) for r, g, b, a in ref_pixels if a > 0)
    if max_lum == 0:
        max_lum = 1

    nr, ng, nb = rgb
    new_pixels = []
    for r, g, b, a in ref_pixels:
        if a == 0:
            new_pixels.append((0, 0, 0, 0))
        else:
            lum = (r + g + b) / 3 / max_lum
            new_pixels.append((
                min(255, int(nr * lum)),
                min(255, int(ng * lum)),
                min(255, int(nb * lum)),
                alpha,
            ))

    out = Image.new("RGBA", (w, h))
    out.putdata(new_pixels)
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    out.save(output_path)


# --------------------------------------------------------------------------- #
# Blockstate generators                                                         #
# --------------------------------------------------------------------------- #
def bs_slab(base):
    return {
        "variants": {
            "type=bottom": {"model": f"blocky13:block/{base}_slab"},
            "type=top":    {"model": f"blocky13:block/{base}_slab_top"},
            "type=double": {"model": f"blocky13:block/{base}_slab_double"},
        }
    }


def bs_stairs(base):
    variants = {}
    for facing in ["east", "north", "south", "west"]:
        for half in ["bottom", "top"]:
            for shape in ["inner_left", "inner_right", "outer_left", "outer_right", "straight"]:
                key = f"facing={facing},half={half},shape={shape}"
                straight = shape == "straight"
                inner    = "inner" in shape
                outer    = "outer" in shape
                left     = "left" in shape

                if straight:
                    model = f"blocky13:block/{base}_stairs"
                elif inner:
                    model = f"blocky13:block/{base}_stairs_inner"
                else:
                    model = f"blocky13:block/{base}_stairs_outer"

                y_map_bottom = {"east": 0, "south": 90, "west": 180, "north": 270}
                y_map_inner_right  = {"east": 0,   "south": 90,  "west": 180, "north": 270}
                y_map_inner_left   = {"east": 270, "south": 0,   "west": 90,  "north": 180}
                y_map_outer_right  = {"east": 0,   "south": 90,  "west": 180, "north": 270}
                y_map_outer_left   = {"east": 270, "south": 0,   "west": 90,  "north": 180}

                if straight:
                    y = y_map_bottom[facing]
                elif inner and not left:
                    y = y_map_inner_right[facing]
                elif inner and left:
                    y = y_map_inner_left[facing]
                elif outer and not left:
                    y = y_map_outer_right[facing]
                else:
                    y = y_map_outer_left[facing]

                entry = {"model": model, "uvlock": True}
                if half == "top":
                    entry["x"] = 180
                    # for top, y rotation shifts by 90 for inner/outer
                    if not straight:
                        top_y_shift = {"inner_right": 90, "inner_left": 0,
                                       "outer_right": 90, "outer_left": 0}
                        y = (y + top_y_shift[shape]) % 360
                if y != 0:
                    entry["y"] = y
                elif "x" not in entry:
                    entry.pop("uvlock", None)
                    if straight and half == "bottom" and y == 0 and facing == "east":
                        entry = {"model": model}
                    else:
                        entry = {"model": model, "uvlock": True}
                        if y != 0:
                            entry["y"] = y
                variants[key] = entry
    return {"variants": variants}


def bs_fence(base):
    return {
        "multipart": [
            {"apply": {"model": f"blocky13:block/{base}_fence_post"}},
            {"apply": {"model": f"blocky13:block/{base}_fence_side", "uvlock": True},
             "when": {"north": "true"}},
            {"apply": {"model": f"blocky13:block/{base}_fence_side", "uvlock": True, "y": 90},
             "when": {"east": "true"}},
            {"apply": {"model": f"blocky13:block/{base}_fence_side", "uvlock": True, "y": 180},
             "when": {"south": "true"}},
            {"apply": {"model": f"blocky13:block/{base}_fence_side", "uvlock": True, "y": 270},
             "when": {"west": "true"}},
        ]
    }


def bs_fence_gate(base):
    variants = {}
    y_map = {"south": 0, "west": 90, "north": 180, "east": 270}
    for facing in ["east", "north", "south", "west"]:
        for in_wall in ["false", "true"]:
            for open_ in ["false", "true"]:
                key = f"facing={facing},in_wall={in_wall},open={open_}"
                if in_wall == "false":
                    model = f"blocky13:block/{base}_fence_gate" if open_ == "false" else f"blocky13:block/{base}_fence_gate_open"
                else:
                    model = f"blocky13:block/{base}_fence_gate_wall" if open_ == "false" else f"blocky13:block/{base}_fence_gate_wall_open"
                entry = {"model": model, "uvlock": True}
                y = y_map[facing]
                if y:
                    entry["y"] = y
                variants[key] = entry
    return {"variants": variants}


def bs_door(base):
    variants = {}
    face_y = {"east": 0, "north": 270, "west": 180, "south": 90}
    open_offset = {"left": 90, "right": -90}
    for facing in ["east", "north", "south", "west"]:
        for half in ["lower", "upper"]:
            for hinge in ["left", "right"]:
                for open_ in ["false", "true"]:
                    key = f"facing={facing},half={half},hinge={hinge},open={open_}"
                    h_str = "left" if hinge == "left" else "right"
                    o_str = "open" if open_ == "true" else ""
                    if o_str:
                        model_name = f"{base}_door_{half[:3]}_{h_str}_open"
                    else:
                        model_name = f"{base}_door_{half[:3]}_{h_str}"
                    entry = {"model": f"blocky13:block/{model_name}"}
                    base_y = face_y[facing]
                    if open_ == "true":
                        if hinge == "left":
                            y = (base_y + 90) % 360
                        else:
                            y = (base_y - 90) % 360
                    else:
                        y = base_y
                    if y:
                        entry["y"] = y
                    variants[key] = entry
    return {"variants": variants}


def bs_trapdoor(base):
    variants = {}
    y_map = {"north": 0, "south": 180, "east": 90, "west": 270}
    for facing in ["east", "north", "south", "west"]:
        for half in ["bottom", "top"]:
            for open_ in ["false", "true"]:
                key = f"facing={facing},half={half},open={open_}"
                if open_ == "true":
                    model = f"blocky13:block/{base}_trapdoor_open"
                    entry = {"model": model}
                    y = y_map[facing]
                    if y:
                        entry["y"] = y
                elif half == "top":
                    entry = {"model": f"blocky13:block/{base}_trapdoor_top"}
                else:
                    entry = {"model": f"blocky13:block/{base}_trapdoor_bottom"}
                variants[key] = entry
    return {"variants": variants}


def bs_pressure_plate(base):
    return {
        "variants": {
            "powered=false": {"model": f"blocky13:block/{base}_pressure_plate"},
            "powered=true":  {"model": f"blocky13:block/{base}_pressure_plate_down"},
        }
    }


def bs_button(base):
    variants = {}
    faces = {"floor": (0, None), "ceiling": (180, None), "wall": (90, None)}
    dirs  = {"north": 0, "east": 90, "south": 180, "west": 270}
    for face, (x, _) in faces.items():
        for facing, dy in dirs.items():
            for powered in ["false", "true"]:
                key = f"face={face},facing={facing},powered={powered}"
                model_suf = "_pressed" if powered == "true" else ""
                model = f"blocky13:block/{base}_button{model_suf}"
                entry = {"model": model}
                if face == "wall":
                    entry["uvlock"] = True
                    entry["x"] = 90
                elif face == "ceiling":
                    entry["x"] = 180
                if dy:
                    entry["y"] = dy
                variants[key] = entry
    return {"variants": variants}


def bs_chain(base):
    return {
        "variants": {
            "axis=x": {"model": f"blocky13:block/{base}_chain", "x": 90, "y": 90},
            "axis=y": {"model": f"blocky13:block/{base}_chain"},
            "axis=z": {"model": f"blocky13:block/{base}_chain", "x": 90},
        }
    }


def bs_bars(base):
    return {
        "multipart": [
            {"apply": {"model": f"blocky13:block/{base}_bars_post_ends"}},
            {"apply": {"model": f"blocky13:block/{base}_bars_post"},
             "when": {"east": "false", "north": "false", "south": "false", "west": "false"}},
            {"apply": {"model": f"blocky13:block/{base}_bars_cap"},
             "when": {"east": "false", "north": "true", "south": "false", "west": "false"}},
            {"apply": {"model": f"blocky13:block/{base}_bars_cap", "y": 90},
             "when": {"east": "true", "north": "false", "south": "false", "west": "false"}},
            {"apply": {"model": f"blocky13:block/{base}_bars_cap_alt"},
             "when": {"east": "false", "north": "false", "south": "true", "west": "false"}},
            {"apply": {"model": f"blocky13:block/{base}_bars_cap_alt", "y": 90},
             "when": {"east": "false", "north": "false", "south": "false", "west": "true"}},
            {"apply": {"model": f"blocky13:block/{base}_bars_side"},
             "when": {"north": "true"}},
            {"apply": {"model": f"blocky13:block/{base}_bars_side", "y": 90},
             "when": {"east": "true"}},
            {"apply": {"model": f"blocky13:block/{base}_bars_side_alt"},
             "when": {"south": "true"}},
            {"apply": {"model": f"blocky13:block/{base}_bars_side_alt", "y": 90},
             "when": {"west": "true"}},
        ]
    }


# --------------------------------------------------------------------------- #
# Block model generators                                                        #
# --------------------------------------------------------------------------- #
def model_slab(base, tex):
    return {"parent": "minecraft:block/slab",
            "textures": {"bottom": tex, "top": tex, "side": tex}}

def model_slab_top(base, tex):
    return {"parent": "minecraft:block/slab_top",
            "textures": {"bottom": tex, "top": tex, "side": tex}}

def model_slab_double(base, tex):
    return {"parent": "minecraft:block/cube_all", "textures": {"all": tex}}

def model_stairs(base, tex):
    return {"parent": "minecraft:block/stairs",
            "textures": {"bottom": tex, "top": tex, "side": tex}}

def model_stairs_inner(base, tex):
    return {"parent": "minecraft:block/inner_stairs",
            "textures": {"bottom": tex, "top": tex, "side": tex}}

def model_stairs_outer(base, tex):
    return {"parent": "minecraft:block/outer_stairs",
            "textures": {"bottom": tex, "top": tex, "side": tex}}

def model_fence_post(base, tex):
    return {"parent": "minecraft:block/fence_post", "textures": {"texture": tex}}

def model_fence_side(base, tex):
    return {"parent": "minecraft:block/fence_side", "textures": {"texture": tex}}

def model_fence_inventory(base, tex):
    return {"parent": "minecraft:block/fence_inventory", "textures": {"texture": tex}}

def model_fence_gate(base, tex):
    return {"parent": "minecraft:block/template_fence_gate", "textures": {"texture": tex}}

def model_fence_gate_open(base, tex):
    return {"parent": "minecraft:block/template_fence_gate_open", "textures": {"texture": tex}}

def model_fence_gate_wall(base, tex):
    return {"parent": "minecraft:block/template_fence_gate_wall", "textures": {"texture": tex}}

def model_fence_gate_wall_open(base, tex):
    return {"parent": "minecraft:block/template_fence_gate_wall_open", "textures": {"texture": tex}}

def model_door_bottom_left(base, tex):
    return {"parent": "minecraft:block/door_bottom_left",
            "textures": {"top": tex, "bottom": tex}}

def model_door_bottom_left_open(base, tex):
    return {"parent": "minecraft:block/door_bottom_left_open",
            "textures": {"top": tex, "bottom": tex}}

def model_door_bottom_right(base, tex):
    return {"parent": "minecraft:block/door_bottom_right",
            "textures": {"top": tex, "bottom": tex}}

def model_door_bottom_right_open(base, tex):
    return {"parent": "minecraft:block/door_bottom_right_open",
            "textures": {"top": tex, "bottom": tex}}

def model_door_top_left(base, tex):
    return {"parent": "minecraft:block/door_top_left",
            "textures": {"top": tex, "bottom": tex}}

def model_door_top_left_open(base, tex):
    return {"parent": "minecraft:block/door_top_left_open",
            "textures": {"top": tex, "bottom": tex}}

def model_door_top_right(base, tex):
    return {"parent": "minecraft:block/door_top_right",
            "textures": {"top": tex, "bottom": tex}}

def model_door_top_right_open(base, tex):
    return {"parent": "minecraft:block/door_top_right_open",
            "textures": {"top": tex, "bottom": tex}}

def model_trapdoor_bottom(base, tex):
    return {"parent": "minecraft:block/template_trapdoor_bottom", "textures": {"texture": tex}}

def model_trapdoor_top(base, tex):
    return {"parent": "minecraft:block/template_trapdoor_top", "textures": {"texture": tex}}

def model_trapdoor_open(base, tex):
    return {"parent": "minecraft:block/template_trapdoor_open", "textures": {"texture": tex}}

def model_pressure_plate(base, tex):
    return {"parent": "minecraft:block/pressure_plate_up", "textures": {"texture": tex}}

def model_pressure_plate_down(base, tex):
    return {"parent": "minecraft:block/pressure_plate_down", "textures": {"texture": tex}}

def model_button(base, tex):
    return {"parent": "minecraft:block/button", "textures": {"texture": tex}}

def model_button_pressed(base, tex):
    return {"parent": "minecraft:block/button_pressed", "textures": {"texture": tex}}

def model_button_inventory(base, tex):
    return {"parent": "minecraft:block/button_inventory", "textures": {"texture": tex}}

def model_chain(base):
    ct = f"blocky13:block/{base}_chain"
    return {"parent": "minecraft:block/template_chain",
            "textures": {"texture": ct, "particle": ct}}

def model_bars_post(base):
    bt = f"blocky13:block/{base}_bars"
    return {"parent": "minecraft:block/template_bars_post",
            "textures": {"bars": bt, "edge": bt, "particle": bt}}

def model_bars_post_ends(base):
    bt = f"blocky13:block/{base}_bars"
    return {"parent": "minecraft:block/template_bars_post_ends",
            "textures": {"bars": bt, "edge": bt, "particle": bt}}

def model_bars_side(base):
    bt = f"blocky13:block/{base}_bars"
    return {"parent": "minecraft:block/template_bars_side",
            "textures": {"bars": bt, "edge": bt, "particle": bt}}

def model_bars_side_alt(base):
    bt = f"blocky13:block/{base}_bars"
    return {"parent": "minecraft:block/template_bars_side_alt",
            "textures": {"bars": bt, "edge": bt, "particle": bt}}

def model_bars_cap(base):
    bt = f"blocky13:block/{base}_bars"
    return {"parent": "minecraft:block/template_bars_cap",
            "textures": {"bars": bt, "edge": bt, "particle": bt}}

def model_bars_cap_alt(base):
    bt = f"blocky13:block/{base}_bars"
    return {"parent": "minecraft:block/template_bars_cap_alt",
            "textures": {"bars": bt, "edge": bt, "particle": bt}}


# --------------------------------------------------------------------------- #
# Item model generators                                                         #
# --------------------------------------------------------------------------- #
def item_slab(base):
    return {"parent": "minecraft:item/generated",
            "textures": {"layer0": f"blocky13:item/{base}_slab"}}

def item_stairs(base):
    return {"parent": f"blocky13:block/{base}_stairs"}

def item_fence(base):
    return {"parent": f"blocky13:block/{base}_fence_inventory"}

def item_fence_gate(base):
    return {"parent": f"blocky13:block/{base}_fence_gate"}

def item_door(base):
    return {"parent": "minecraft:item/generated",
            "textures": {"layer0": f"blocky13:item/{base}_door"}}

def item_trapdoor(base):
    return {"parent": f"blocky13:block/{base}_trapdoor_bottom"}

def item_pressure_plate(base):
    return {"parent": f"blocky13:block/{base}_pressure_plate"}

def item_button(base):
    return {"parent": f"blocky13:block/{base}_button_inventory"}

def item_chain(base):
    return {"parent": "minecraft:item/generated",
            "textures": {"layer0": f"blocky13:item/{base}_chain"}}

def item_bars(base):
    return {"parent": "minecraft:item/generated",
            "textures": {"layer0": f"blocky13:block/{base}_bars"}}


# --------------------------------------------------------------------------- #
# Recipe generators                                                             #
# --------------------------------------------------------------------------- #
def recipe_slab(base, ing):
    return {"type": "minecraft:crafting_shaped", "category": "building",
            "key": {"#": ing},
            "pattern": ["###"],
            "result": {"count": 6, "id": f"blocky13:{base}_slab"}}

def recipe_stairs(base, ing):
    return {"type": "minecraft:crafting_shaped", "category": "building",
            "key": {"#": ing},
            "pattern": ["#  ", "## ", "###"],
            "result": {"count": 4, "id": f"blocky13:{base}_stairs"}}

def recipe_fence(base, ing):
    return {"type": "minecraft:crafting_shaped", "category": "building",
            "key": {"#": ing, "/": "minecraft:stick"},
            "pattern": ["#/#", "#/#"],
            "result": {"count": 3, "id": f"blocky13:{base}_fence"}}

def recipe_fence_gate(base, ing):
    return {"type": "minecraft:crafting_shaped", "category": "redstone",
            "key": {"#": ing, "/": "minecraft:stick"},
            "pattern": ["/#/", "/#/"],
            "result": {"count": 1, "id": f"blocky13:{base}_fence_gate"}}

def recipe_door(base, ing):
    return {"type": "minecraft:crafting_shaped", "category": "redstone",
            "key": {"#": ing},
            "pattern": ["##", "##", "##"],
            "result": {"count": 3, "id": f"blocky13:{base}_door"}}

def recipe_trapdoor(base, ing):
    return {"type": "minecraft:crafting_shaped", "category": "redstone",
            "key": {"#": ing},
            "pattern": ["###", "###"],
            "result": {"count": 2, "id": f"blocky13:{base}_trapdoor"}}

def recipe_pressure_plate(base, ing):
    return {"type": "minecraft:crafting_shaped", "category": "redstone",
            "key": {"#": ing},
            "pattern": ["##"],
            "result": {"count": 1, "id": f"blocky13:{base}_pressure_plate"}}

def recipe_button(base, ing):
    return {"type": "minecraft:crafting_shapeless", "category": "redstone",
            "ingredients": [ing],
            "result": {"count": 1, "id": f"blocky13:{base}_button"}}

def recipe_chain(base, ing):
    return {"type": "minecraft:crafting_shaped", "category": "misc",
            "key": {"#": ing},
            "pattern": ["#", "#"],
            "result": {"count": 4, "id": f"blocky13:{base}_chain"}}

def recipe_bars(base, ing):
    return {"type": "minecraft:crafting_shaped", "category": "misc",
            "key": {"#": ing},
            "pattern": ["##", "##"],
            "result": {"count": 16, "id": f"blocky13:{base}_bars"}}


# --------------------------------------------------------------------------- #
# Loot table generators                                                         #
# --------------------------------------------------------------------------- #
def loot_slab(base):
    return {"type": "minecraft:block", "pools": [{"rolls": 1.0, "bonus_rolls": 0.0,
        "entries": [{"type": "minecraft:item", "name": f"blocky13:{base}_slab",
            "functions": [
                {"function": "minecraft:set_count", "add": False, "count": 2.0,
                 "conditions": [{"condition": "minecraft:block_state_property",
                                 "block": f"blocky13:{base}_slab",
                                 "properties": {"type": "double"}}]},
                {"function": "minecraft:explosion_decay"}
            ]}]}]}

def loot_simple(block_id):
    return {"type": "minecraft:block", "pools": [{"rolls": 1.0, "bonus_rolls": 0.0,
        "entries": [{"type": "minecraft:item", "name": f"blocky13:{block_id}",
            "functions": [{"function": "minecraft:explosion_decay"}]}]}]}


# --------------------------------------------------------------------------- #
# Advancement generator                                                         #
# --------------------------------------------------------------------------- #
def advancement(base, variant, mc_ing):
    block_id = f"{base}_{variant}"
    return {
        "parent": "minecraft:recipes/root",
        "criteria": {
            "has_ingredient": {
                "trigger": "minecraft:inventory_changed",
                "conditions": {"items": [{"items": mc_ing}]}
            },
            "has_the_recipe": {
                "trigger": "minecraft:recipe_unlocked",
                "conditions": {"recipe": f"blocky13:{block_id}"}
            }
        },
        "requirements": [["has_the_recipe", "has_ingredient"]],
        "rewards": {"recipes": [f"blocky13:{block_id}"]}
    }


# --------------------------------------------------------------------------- #
# Main generation loop                                                          #
# --------------------------------------------------------------------------- #
def generate_for_material(base_id, mc_tex_suffix, rgb, is_transparent):
    tex = f"minecraft:block/{mc_tex_suffix}"
    mc_ing = f"minecraft:{base_id}"
    alpha = 128 if is_transparent else 255

    bs_dir  = os.path.join(ASSETS, "blockstates")
    mb_dir  = os.path.join(ASSETS, "models/block")
    mi_dir  = os.path.join(ASSETS, "models/item")
    tx_b    = os.path.join(ASSETS, "textures/block")
    tx_i    = os.path.join(ASSETS, "textures/item")
    rec_dir = os.path.join(DATA,   "recipe")
    lt_dir  = os.path.join(DATA,   "loot_table/blocks")
    adv_dir = os.path.join(DATA,   "advancement/recipes/blocky13")

    # ---- blockstates ----
    write_json(f"{bs_dir}/{base_id}_slab.json",           bs_slab(base_id))
    write_json(f"{bs_dir}/{base_id}_stairs.json",         bs_stairs(base_id))
    write_json(f"{bs_dir}/{base_id}_fence.json",          bs_fence(base_id))
    write_json(f"{bs_dir}/{base_id}_fence_gate.json",     bs_fence_gate(base_id))
    write_json(f"{bs_dir}/{base_id}_door.json",           bs_door(base_id))
    write_json(f"{bs_dir}/{base_id}_trapdoor.json",       bs_trapdoor(base_id))
    write_json(f"{bs_dir}/{base_id}_pressure_plate.json", bs_pressure_plate(base_id))
    write_json(f"{bs_dir}/{base_id}_button.json",         bs_button(base_id))
    write_json(f"{bs_dir}/{base_id}_chain.json",          bs_chain(base_id))
    write_json(f"{bs_dir}/{base_id}_bars.json",           bs_bars(base_id))

    # ---- block models ----
    write_json(f"{mb_dir}/{base_id}_slab.json",                  model_slab(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_slab_top.json",              model_slab_top(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_slab_double.json",           model_slab_double(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_stairs.json",                model_stairs(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_stairs_inner.json",          model_stairs_inner(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_stairs_outer.json",          model_stairs_outer(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_fence_post.json",            model_fence_post(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_fence_side.json",            model_fence_side(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_fence_inventory.json",       model_fence_inventory(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_fence_gate.json",            model_fence_gate(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_fence_gate_open.json",       model_fence_gate_open(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_fence_gate_wall.json",       model_fence_gate_wall(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_fence_gate_wall_open.json",  model_fence_gate_wall_open(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_door_bottom_left.json",      model_door_bottom_left(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_door_bottom_left_open.json", model_door_bottom_left_open(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_door_bottom_right.json",     model_door_bottom_right(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_door_bottom_right_open.json",model_door_bottom_right_open(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_door_top_left.json",         model_door_top_left(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_door_top_left_open.json",    model_door_top_left_open(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_door_top_right.json",        model_door_top_right(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_door_top_right_open.json",   model_door_top_right_open(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_trapdoor_bottom.json",       model_trapdoor_bottom(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_trapdoor_top.json",          model_trapdoor_top(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_trapdoor_open.json",         model_trapdoor_open(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_pressure_plate.json",        model_pressure_plate(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_pressure_plate_down.json",   model_pressure_plate_down(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_button.json",                model_button(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_button_pressed.json",        model_button_pressed(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_button_inventory.json",      model_button_inventory(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_chain.json",                 model_chain(base_id))
    write_json(f"{mb_dir}/{base_id}_bars_post.json",             model_bars_post(base_id))
    write_json(f"{mb_dir}/{base_id}_bars_post_ends.json",        model_bars_post_ends(base_id))
    write_json(f"{mb_dir}/{base_id}_bars_side.json",             model_bars_side(base_id))
    write_json(f"{mb_dir}/{base_id}_bars_side_alt.json",         model_bars_side_alt(base_id))
    write_json(f"{mb_dir}/{base_id}_bars_cap.json",              model_bars_cap(base_id))
    write_json(f"{mb_dir}/{base_id}_bars_cap_alt.json",          model_bars_cap_alt(base_id))

    # ---- item models ----
    write_json(f"{mi_dir}/{base_id}_slab.json",           item_slab(base_id))
    write_json(f"{mi_dir}/{base_id}_stairs.json",         item_stairs(base_id))
    write_json(f"{mi_dir}/{base_id}_fence.json",          item_fence(base_id))
    write_json(f"{mi_dir}/{base_id}_fence_gate.json",     item_fence_gate(base_id))
    write_json(f"{mi_dir}/{base_id}_door.json",           item_door(base_id))
    write_json(f"{mi_dir}/{base_id}_trapdoor.json",       item_trapdoor(base_id))
    write_json(f"{mi_dir}/{base_id}_pressure_plate.json", item_pressure_plate(base_id))
    write_json(f"{mi_dir}/{base_id}_button.json",         item_button(base_id))
    write_json(f"{mi_dir}/{base_id}_chain.json",          item_chain(base_id))
    write_json(f"{mi_dir}/{base_id}_bars.json",           item_bars(base_id))

    # ---- recipes ----
    write_json(f"{rec_dir}/{base_id}_slab.json",           recipe_slab(base_id, mc_ing))
    write_json(f"{rec_dir}/{base_id}_stairs.json",         recipe_stairs(base_id, mc_ing))
    write_json(f"{rec_dir}/{base_id}_fence.json",          recipe_fence(base_id, mc_ing))
    write_json(f"{rec_dir}/{base_id}_fence_gate.json",     recipe_fence_gate(base_id, mc_ing))
    write_json(f"{rec_dir}/{base_id}_door.json",           recipe_door(base_id, mc_ing))
    write_json(f"{rec_dir}/{base_id}_trapdoor.json",       recipe_trapdoor(base_id, mc_ing))
    write_json(f"{rec_dir}/{base_id}_pressure_plate.json", recipe_pressure_plate(base_id, mc_ing))
    write_json(f"{rec_dir}/{base_id}_button.json",         recipe_button(base_id, mc_ing))
    write_json(f"{rec_dir}/{base_id}_chain.json",          recipe_chain(base_id, mc_ing))
    write_json(f"{rec_dir}/{base_id}_bars.json",           recipe_bars(base_id, mc_ing))

    # ---- loot tables ----
    write_json(f"{lt_dir}/{base_id}_slab.json",           loot_slab(base_id))
    for v in ["stairs", "fence", "fence_gate", "door", "trapdoor",
              "pressure_plate", "button", "chain", "bars"]:
        write_json(f"{lt_dir}/{base_id}_{v}.json", loot_simple(f"{base_id}_{v}"))

    # ---- advancements ----
    for v in VARIANTS:
        write_json(f"{adv_dir}/{base_id}_{v}.json", advancement(base_id, v, mc_ing))

    # ---- textures (PNG) ----
    recolor_texture(REF_CHAIN_BLOCK, rgb, f"{tx_b}/{base_id}_chain.png", alpha)
    recolor_texture(REF_BARS_BLOCK,  rgb, f"{tx_b}/{base_id}_bars.png",  alpha)
    recolor_texture(REF_SLAB_ITEM,   rgb, f"{tx_i}/{base_id}_slab.png",  alpha)
    recolor_texture(REF_DOOR_ITEM,   rgb, f"{tx_i}/{base_id}_door.png",  alpha)
    recolor_texture(REF_CHAIN_ITEM,  rgb, f"{tx_i}/{base_id}_chain.png", alpha)


def generate_for_bricks(base_id, rgb):
    tex = f"blocky13:block/{base_id}"
    ing = f"blocky13:{base_id}"
    # Derive color name for dye (strip "_bricks" suffix)
    color_name = base_id[:-len("_bricks")]
    dye_ing = f"minecraft:{color_name}_dye"

    bs_dir  = os.path.join(ASSETS, "blockstates")
    mb_dir  = os.path.join(ASSETS, "models/block")
    mi_dir  = os.path.join(ASSETS, "models/item")
    it_dir  = os.path.join(ASSETS, "items")
    tx_b    = os.path.join(ASSETS, "textures/block")
    tx_i    = os.path.join(ASSETS, "textures/item")
    rec_dir = os.path.join(DATA,   "recipe")
    lt_dir  = os.path.join(DATA,   "loot_table/blocks")
    adv_dir = os.path.join(DATA,   "advancement/recipes/blocky13")

    # ---- base brick block assets ----
    write_json(f"{bs_dir}/{base_id}.json",
               {"variants": {"": {"model": f"blocky13:block/{base_id}_block"}}})
    write_json(f"{mb_dir}/{base_id}_block.json",
               {"parent": "minecraft:block/cube_all", "textures": {"all": tex}})
    write_json(f"{mi_dir}/{base_id}.json",
               {"parent": f"blocky13:block/{base_id}_block"})
    write_json(f"{rec_dir}/{base_id}.json",
               {"type": "minecraft:crafting_shapeless", "category": "building",
                "ingredients": ["minecraft:bricks", dye_ing],
                "result": {"count": 1, "id": ing}})
    write_json(f"{lt_dir}/{base_id}.json", loot_simple(base_id))
    write_json(f"{adv_dir}/{base_id}.json", advancement(base_id, "", ing))

    # ---- variant blockstates ----
    write_json(f"{bs_dir}/{base_id}_slab.json",           bs_slab(base_id))
    write_json(f"{bs_dir}/{base_id}_stairs.json",         bs_stairs(base_id))
    write_json(f"{bs_dir}/{base_id}_fence.json",          bs_fence(base_id))
    write_json(f"{bs_dir}/{base_id}_fence_gate.json",     bs_fence_gate(base_id))
    write_json(f"{bs_dir}/{base_id}_door.json",           bs_door(base_id))
    write_json(f"{bs_dir}/{base_id}_trapdoor.json",       bs_trapdoor(base_id))
    write_json(f"{bs_dir}/{base_id}_pressure_plate.json", bs_pressure_plate(base_id))
    write_json(f"{bs_dir}/{base_id}_button.json",         bs_button(base_id))
    write_json(f"{bs_dir}/{base_id}_chain.json",          bs_chain(base_id))
    write_json(f"{bs_dir}/{base_id}_bars.json",           bs_bars(base_id))

    # ---- variant block models ----
    write_json(f"{mb_dir}/{base_id}_slab.json",                  model_slab(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_slab_top.json",              model_slab_top(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_slab_double.json",           model_slab_double(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_stairs.json",                model_stairs(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_stairs_inner.json",          model_stairs_inner(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_stairs_outer.json",          model_stairs_outer(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_fence_post.json",            model_fence_post(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_fence_side.json",            model_fence_side(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_fence_inventory.json",       model_fence_inventory(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_fence_gate.json",            model_fence_gate(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_fence_gate_open.json",       model_fence_gate_open(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_fence_gate_wall.json",       model_fence_gate_wall(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_fence_gate_wall_open.json",  model_fence_gate_wall_open(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_door_bottom_left.json",      model_door_bottom_left(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_door_bottom_left_open.json", model_door_bottom_left_open(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_door_bottom_right.json",     model_door_bottom_right(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_door_bottom_right_open.json",model_door_bottom_right_open(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_door_top_left.json",         model_door_top_left(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_door_top_left_open.json",    model_door_top_left_open(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_door_top_right.json",        model_door_top_right(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_door_top_right_open.json",   model_door_top_right_open(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_trapdoor_bottom.json",       model_trapdoor_bottom(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_trapdoor_top.json",          model_trapdoor_top(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_trapdoor_open.json",         model_trapdoor_open(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_pressure_plate.json",        model_pressure_plate(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_pressure_plate_down.json",   model_pressure_plate_down(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_button.json",                model_button(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_button_pressed.json",        model_button_pressed(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_button_inventory.json",      model_button_inventory(base_id, tex))
    write_json(f"{mb_dir}/{base_id}_chain.json",                 model_chain(base_id))
    write_json(f"{mb_dir}/{base_id}_bars_post.json",             model_bars_post(base_id))
    write_json(f"{mb_dir}/{base_id}_bars_post_ends.json",        model_bars_post_ends(base_id))
    write_json(f"{mb_dir}/{base_id}_bars_side.json",             model_bars_side(base_id))
    write_json(f"{mb_dir}/{base_id}_bars_side_alt.json",         model_bars_side_alt(base_id))
    write_json(f"{mb_dir}/{base_id}_bars_cap.json",              model_bars_cap(base_id))
    write_json(f"{mb_dir}/{base_id}_bars_cap_alt.json",          model_bars_cap_alt(base_id))

    # ---- variant item models ----
    write_json(f"{mi_dir}/{base_id}_slab.json",           item_slab(base_id))
    write_json(f"{mi_dir}/{base_id}_stairs.json",         item_stairs(base_id))
    write_json(f"{mi_dir}/{base_id}_fence.json",          item_fence(base_id))
    write_json(f"{mi_dir}/{base_id}_fence_gate.json",     item_fence_gate(base_id))
    write_json(f"{mi_dir}/{base_id}_door.json",           item_door(base_id))
    write_json(f"{mi_dir}/{base_id}_trapdoor.json",       item_trapdoor(base_id))
    write_json(f"{mi_dir}/{base_id}_pressure_plate.json", item_pressure_plate(base_id))
    write_json(f"{mi_dir}/{base_id}_button.json",         item_button(base_id))
    write_json(f"{mi_dir}/{base_id}_chain.json",          item_chain(base_id))
    write_json(f"{mi_dir}/{base_id}_bars.json",           item_bars(base_id))

    # ---- variant recipes ----
    write_json(f"{rec_dir}/{base_id}_slab.json",           recipe_slab(base_id, ing))
    write_json(f"{rec_dir}/{base_id}_stairs.json",         recipe_stairs(base_id, ing))
    write_json(f"{rec_dir}/{base_id}_fence.json",          recipe_fence(base_id, ing))
    write_json(f"{rec_dir}/{base_id}_fence_gate.json",     recipe_fence_gate(base_id, ing))
    write_json(f"{rec_dir}/{base_id}_door.json",           recipe_door(base_id, ing))
    write_json(f"{rec_dir}/{base_id}_trapdoor.json",       recipe_trapdoor(base_id, ing))
    write_json(f"{rec_dir}/{base_id}_pressure_plate.json", recipe_pressure_plate(base_id, ing))
    write_json(f"{rec_dir}/{base_id}_button.json",         recipe_button(base_id, ing))
    write_json(f"{rec_dir}/{base_id}_chain.json",          recipe_chain(base_id, ing))
    write_json(f"{rec_dir}/{base_id}_bars.json",           recipe_bars(base_id, ing))

    # ---- variant loot tables ----
    write_json(f"{lt_dir}/{base_id}_slab.json",           loot_slab(base_id))
    for v in ["stairs", "fence", "fence_gate", "door", "trapdoor",
              "pressure_plate", "button", "chain", "bars"]:
        write_json(f"{lt_dir}/{base_id}_{v}.json", loot_simple(f"{base_id}_{v}"))

    # ---- variant advancements ----
    for v in VARIANTS:
        write_json(f"{adv_dir}/{base_id}_{v}.json", advancement(base_id, v, ing))

    # ---- textures (PNG) ----
    recolor_texture(REF_BRICKS_BLOCK, rgb, f"{tx_b}/{base_id}.png")
    recolor_texture(REF_CHAIN_BLOCK,  rgb, f"{tx_b}/{base_id}_chain.png")
    recolor_texture(REF_BARS_BLOCK,   rgb, f"{tx_b}/{base_id}_bars.png")
    recolor_texture(REF_SLAB_ITEM,    rgb, f"{tx_i}/{base_id}_slab.png")
    recolor_texture(REF_DOOR_ITEM,    rgb, f"{tx_i}/{base_id}_door.png")
    recolor_texture(REF_CHAIN_ITEM,   rgb, f"{tx_i}/{base_id}_chain.png")


def generate_brush_assets():
    mi_dir = os.path.join(ASSETS, "models/item")
    it_dir = os.path.join(ASSETS, "items")
    write_json(f"{mi_dir}/dye_brush.json",
               {"parent": "minecraft:item/handheld",
                "textures": {"layer0": "minecraft:item/brush"}})
    write_json(f"{it_dir}/dye_brush.json",
               {"model": {"type": "minecraft:model", "model": "blocky13:item/dye_brush"}})


def generate_lang_entries():
    lang_path = os.path.join(ASSETS, "lang/en_us.json")
    with open(lang_path) as f:
        lang = json.load(f)

    variant_labels = {
        "slab": "Slab", "stairs": "Stairs", "fence": "Fence",
        "fence_gate": "Fence Gate", "door": "Door", "trapdoor": "Trapdoor",
        "pressure_plate": "Pressure Plate", "button": "Button",
        "chain": "Chain", "bars": "Bars",
    }

    for base_id, _, _, _ in MATERIALS:
        base_label = title_name(base_id)
        for v, v_label in variant_labels.items():
            full_name = f"{base_label} {v_label}"
            block_key = f"block.blocky13.{base_id}_{v}"
            item_key  = f"item.blocky13.{base_id}_{v}"
            if block_key not in lang:
                lang[block_key] = full_name
                lang[item_key]  = full_name

    for base_id, _ in BRICKS_MATERIALS:
        base_label = title_name(base_id)
        # Base brick block entries
        base_block_key = f"block.blocky13.{base_id}"
        base_item_key  = f"item.blocky13.{base_id}"
        if base_block_key not in lang:
            lang[base_block_key] = base_label
            lang[base_item_key]  = base_label
        # Variant entries
        for v, v_label in variant_labels.items():
            full_name = f"{base_label} {v_label}"
            block_key = f"block.blocky13.{base_id}_{v}"
            item_key  = f"item.blocky13.{base_id}_{v}"
            if block_key not in lang:
                lang[block_key] = full_name
                lang[item_key]  = full_name

    # Dye brush
    if "item.blocky13.dye_brush" not in lang:
        lang["item.blocky13.dye_brush"] = "Dye Brush"

    with open(lang_path, "w") as f:
        json.dump(lang, f, indent=2, ensure_ascii=False)
    print(f"Updated lang file with {len(MATERIALS)} vanilla materials and {len(BRICKS_MATERIALS)} brick sets")


# --------------------------------------------------------------------------- #
# Walls (issue #11) + connection / mining tags (issues #15, #16)                #
#                                                                               #
# Walls and the block tags cover *every* registered base — including the        #
# original hand-authored metal/dirt bases that are not part of MATERIALS /      #
# BRICKS_MATERIALS. To stay in sync with whatever is actually registered, the   #
# base list and each base's texture are discovered from the existing            #
# `<base>_slab` block model rather than hard-coded here.                         #
# --------------------------------------------------------------------------- #

# Bases whose blocks are mined with a shovel rather than a pickaxe.
SHOVEL_BASES = {"dirt"}
# Pickaxe tier required to harvest drops, mirroring the vanilla source block.
NEEDS_STONE_BASES = {"iron_block", "raw_iron_block", "copper_block",
                     "raw_copper_block", "lapis_block"}
NEEDS_IRON_BASES = {"gold_block", "raw_gold_block", "redstone_block",
                    "emerald_block", "diamond_block"}
NEEDS_DIAMOND_BASES = {"netherite_block"}


def _discover_bases():
    """Return every registered variant base, discovered from existing slab models."""
    bs_dir = os.path.join(ASSETS, "blockstates")
    bases = [f[:-len("_slab.json")] for f in os.listdir(bs_dir)
             if f.endswith("_slab.json") and not f.endswith("_vertical_slab.json")]
    return sorted(bases)


def _base_side_texture(base):
    """(texture, render_type) used by a base, read from its slab block model."""
    path = os.path.join(ASSETS, "models/block", f"{base}_slab.json")
    with open(path) as f:
        model = json.load(f)
    return model["textures"]["side"], model.get("render_type")


def _base_ingredient(base):
    return f"blocky13:{base}" if base.endswith("_bricks") else f"minecraft:{base}"


def _mining_tool(base):
    """('shovel'|'pickaxe'|'none', tier) for a base, matching vanilla behaviour."""
    if base in SHOVEL_BASES:
        return "shovel", None
    if "glass" in base:            # glass is broken by hand, no tool tier
        return "none", None
    if base in NEEDS_STONE_BASES:
        return "pickaxe", "stone"
    if base in NEEDS_IRON_BASES:
        return "pickaxe", "iron"
    if base in NEEDS_DIAMOND_BASES:
        return "pickaxe", "diamond"
    return "pickaxe", None


def bs_wall(base):
    side = f"blocky13:block/{base}_wall_side"
    side_tall = f"blocky13:block/{base}_wall_side_tall"
    return {
        "multipart": [
            {"apply": {"model": f"blocky13:block/{base}_wall_post"}, "when": {"up": "true"}},
            {"apply": {"model": side, "uvlock": True},            "when": {"north": "low"}},
            {"apply": {"model": side, "uvlock": True, "y": 90},   "when": {"east": "low"}},
            {"apply": {"model": side, "uvlock": True, "y": 180},  "when": {"south": "low"}},
            {"apply": {"model": side, "uvlock": True, "y": 270},  "when": {"west": "low"}},
            {"apply": {"model": side_tall, "uvlock": True},           "when": {"north": "tall"}},
            {"apply": {"model": side_tall, "uvlock": True, "y": 90},  "when": {"east": "tall"}},
            {"apply": {"model": side_tall, "uvlock": True, "y": 180}, "when": {"south": "tall"}},
            {"apply": {"model": side_tall, "uvlock": True, "y": 270}, "when": {"west": "tall"}},
        ]
    }


def _wall_model(parent, tex, render_type):
    # Bind the texture under every variable name the vanilla wall templates have
    # used across versions ("wall" today; "texture"/"particle" as safe aliases).
    # Unused texture variables are ignored by the model loader, so listing all
    # of them guarantees the sprite binds regardless of the template's variable.
    model = {"parent": parent, "textures": {"wall": tex, "texture": tex, "particle": tex}}
    if render_type:
        model["render_type"] = render_type
    return model


def wall_inventory_model(tex, render_type):
    # Self-contained inventory model: the geometry and texture references live
    # here (using "#wall"), so the sprite binds without depending on the texture
    # variable name used inside minecraft:block/wall_inventory. The block/block
    # parent only supplies the standard inventory display transforms.
    model = {
        "parent": "minecraft:block/block",
        "textures": {"wall": tex, "particle": tex},
        "elements": [
            {"from": [4, 0, 0], "to": [12, 16, 16],
             "faces": {
                 "down":  {"uv": [4, 0, 12, 16], "texture": "#wall", "cullface": "down"},
                 "up":    {"uv": [4, 0, 12, 16], "texture": "#wall", "cullface": "up"},
                 "north": {"uv": [4, 0, 12, 16], "texture": "#wall", "cullface": "north"},
                 "south": {"uv": [4, 0, 12, 16], "texture": "#wall", "cullface": "south"},
                 "west":  {"uv": [0, 0, 16, 16], "texture": "#wall"},
                 "east":  {"uv": [0, 0, 16, 16], "texture": "#wall"},
             }},
        ],
    }
    if render_type:
        model["render_type"] = render_type
    return model


def recipe_wall(base, ing):
    return {"type": "minecraft:crafting_shaped", "category": "building",
            "key": {"#": ing},
            "pattern": ["###", "###"],
            "result": {"count": 6, "id": f"blocky13:{base}_wall"}}


def generate_walls_and_tags():
    bs_dir  = os.path.join(ASSETS, "blockstates")
    mb_dir  = os.path.join(ASSETS, "models/block")
    mi_dir  = os.path.join(ASSETS, "models/item")
    it_dir  = os.path.join(ASSETS, "items")
    rec_dir = os.path.join(DATA,   "recipe")
    lt_dir  = os.path.join(DATA,   "loot_table/blocks")
    adv_dir = os.path.join(DATA,   "advancement/recipes/blocky13")
    mc_tags = os.path.join(ROOT, "src/main/resources/data/minecraft/tags/block")

    bases = _discover_bases()

    walls_tag, fences_tag = [], []
    pickaxe, shovel = [], []
    needs_stone, needs_iron, needs_diamond = [], [], []

    # Variants that share the base's mining behaviour (everything we register).
    minted_variants = VARIANTS + ["wall", "vertical_slab", "layer"]

    for base in bases:
        tex, render_type = _base_side_texture(base)
        ing = _base_ingredient(base)

        # ---- wall assets ----
        write_json(f"{bs_dir}/{base}_wall.json", bs_wall(base))
        write_json(f"{mb_dir}/{base}_wall_post.json",
                   _wall_model("minecraft:block/template_wall_post", tex, render_type))
        write_json(f"{mb_dir}/{base}_wall_side.json",
                   _wall_model("minecraft:block/template_wall_side", tex, render_type))
        write_json(f"{mb_dir}/{base}_wall_side_tall.json",
                   _wall_model("minecraft:block/template_wall_side_tall", tex, render_type))
        write_json(f"{mb_dir}/{base}_wall_inventory.json",
                   wall_inventory_model(tex, render_type))
        write_json(f"{mi_dir}/{base}_wall.json",
                   {"parent": f"blocky13:block/{base}_wall_inventory"})
        write_json(f"{it_dir}/{base}_wall.json",
                   {"model": {"type": "minecraft:model",
                              "model": f"blocky13:item/{base}_wall"}})
        write_json(f"{rec_dir}/{base}_wall.json", recipe_wall(base, ing))
        write_json(f"{lt_dir}/{base}_wall.json", loot_simple(f"{base}_wall"))
        write_json(f"{adv_dir}/{base}_wall.json", advancement(base, "wall", ing))

        # ---- connection tags ----
        walls_tag.append(f"blocky13:{base}_wall")
        fences_tag.append(f"blocky13:{base}_fence")

        # ---- mining tags ----
        tool, tier = _mining_tool(base)
        block_ids = [f"blocky13:{base}_{v}" for v in minted_variants]
        if base.endswith("_bricks"):
            block_ids.append(f"blocky13:{base}")  # the colored brick block itself
        if tool == "pickaxe":
            pickaxe.extend(block_ids)
            if tier == "stone":
                needs_stone.extend(block_ids)
            elif tier == "iron":
                needs_iron.extend(block_ids)
            elif tier == "diamond":
                needs_diamond.extend(block_ids)
        elif tool == "shovel":
            shovel.extend(block_ids)

    # The standalone sand layer is shovel-mined too.
    shovel.append("blocky13:sand_layer")

    def tag(values):
        return {"replace": False, "values": sorted(values)}

    write_json(f"{mc_tags}/fences.json", tag(fences_tag))
    write_json(f"{mc_tags}/walls.json", tag(walls_tag))
    write_json(f"{mc_tags}/mineable/pickaxe.json", tag(pickaxe))
    write_json(f"{mc_tags}/mineable/shovel.json", tag(shovel))
    write_json(f"{mc_tags}/needs_stone_tool.json", tag(needs_stone))
    write_json(f"{mc_tags}/needs_iron_tool.json", tag(needs_iron))
    write_json(f"{mc_tags}/needs_diamond_tool.json", tag(needs_diamond))

    # ---- lang entries for every wall ----
    lang_path = os.path.join(ASSETS, "lang/en_us.json")
    with open(lang_path) as f:
        lang = json.load(f)
    for base in bases:
        full_name = f"{title_name(base)} Wall"
        for prefix in ("block", "item"):
            key = f"{prefix}.blocky13.{base}_wall"
            if key not in lang:
                lang[key] = full_name
    with open(lang_path, "w") as f:
        json.dump(lang, f, indent=2, ensure_ascii=False)

    print(f"Generated walls + connection/mining tags for {len(bases)} bases.")


# --------------------------------------------------------------------------- #
# Vertical slabs (issue #18)                                                    #
#                                                                               #
# Like walls, vertical slabs cover *every* registered base, so the base list    #
# and each base's texture/render-type are discovered from the existing          #
# `<base>_slab` block model rather than hard-coded. Each facing has its own      #
# model (no blockstate rotation) so the visible half always matches the         #
# collision shape defined in VerticalSlabBlock.java.                            #
# --------------------------------------------------------------------------- #

# facing -> (from-corner, to-corner, face flush with the cell boundary -> cullface)
_VSLAB_BOXES = {
    "north": ([0, 0, 0], [16, 16, 8],  "north"),
    "south": ([0, 0, 8], [16, 16, 16], "south"),
    "west":  ([0, 0, 0], [8, 16, 16],  "west"),
    "east":  ([8, 0, 0], [16, 16, 16], "east"),
}


def bs_vertical_slab(base):
    variants = {}
    for double in ["false", "true"]:
        for facing in ["north", "south", "east", "west"]:
            for wl in ["false", "true"]:
                key = f"double={double},facing={facing},waterlogged={wl}"
                if double == "true":
                    variants[key] = {"model": f"blocky13:block/{base}_vertical_slab_double"}
                else:
                    variants[key] = {"model": f"blocky13:block/{base}_vertical_slab_{facing}"}
    return {"variants": variants}


def model_vertical_slab_dir(tex, frm, to, cull_face, render_type):
    faces = {}
    for face in ["down", "up", "north", "south", "east", "west"]:
        f = {"texture": "#all"}
        if face == cull_face:
            f["cullface"] = face
        faces[face] = f
    model = {"parent": "minecraft:block/block",
             "textures": {"all": tex, "particle": tex},
             "elements": [{"from": frm, "to": to, "faces": faces}]}
    if render_type:
        model["render_type"] = render_type
    return model


def model_vertical_slab_double(tex, render_type):
    model = {"parent": "minecraft:block/cube_all", "textures": {"all": tex}}
    if render_type:
        model["render_type"] = render_type
    return model


def recipe_vertical_slab(base, ing):
    return {"type": "minecraft:crafting_shaped", "category": "building",
            "key": {"#": ing},
            "pattern": ["#", "#", "#"],
            "result": {"count": 6, "id": f"blocky13:{base}_vertical_slab"}}


def loot_vertical_slab(base):
    bid = f"blocky13:{base}_vertical_slab"
    return {"type": "minecraft:block", "pools": [{"rolls": 1.0, "bonus_rolls": 0.0,
        "entries": [{"type": "minecraft:item", "name": bid,
            "functions": [
                {"function": "minecraft:set_count", "add": False, "count": 2.0,
                 "conditions": [{"condition": "minecraft:block_state_property",
                                 "block": bid,
                                 "properties": {"double": "true"}}]},
                {"function": "minecraft:explosion_decay"}
            ]}]}]}


def generate_vertical_slabs():
    bs_dir  = os.path.join(ASSETS, "blockstates")
    mb_dir  = os.path.join(ASSETS, "models/block")
    mi_dir  = os.path.join(ASSETS, "models/item")
    it_dir  = os.path.join(ASSETS, "items")
    rec_dir = os.path.join(DATA,   "recipe")
    lt_dir  = os.path.join(DATA,   "loot_table/blocks")
    adv_dir = os.path.join(DATA,   "advancement/recipes/blocky13")

    bases = _discover_bases()
    lang_path = os.path.join(ASSETS, "lang/en_us.json")
    with open(lang_path) as f:
        lang = json.load(f)

    for base in bases:
        tex, render_type = _base_side_texture(base)
        ing = _base_ingredient(base)

        write_json(f"{bs_dir}/{base}_vertical_slab.json", bs_vertical_slab(base))
        for facing, (frm, to, cull) in _VSLAB_BOXES.items():
            write_json(f"{mb_dir}/{base}_vertical_slab_{facing}.json",
                       model_vertical_slab_dir(tex, frm, to, cull, render_type))
        write_json(f"{mb_dir}/{base}_vertical_slab_double.json",
                   model_vertical_slab_double(tex, render_type))
        # Item model shows the north-facing half-block (like stairs/trapdoor items).
        write_json(f"{mi_dir}/{base}_vertical_slab.json",
                   {"parent": f"blocky13:block/{base}_vertical_slab_north"})
        write_json(f"{it_dir}/{base}_vertical_slab.json",
                   {"model": {"type": "minecraft:model",
                              "model": f"blocky13:item/{base}_vertical_slab"}})
        write_json(f"{rec_dir}/{base}_vertical_slab.json", recipe_vertical_slab(base, ing))
        write_json(f"{lt_dir}/{base}_vertical_slab.json", loot_vertical_slab(base))
        write_json(f"{adv_dir}/{base}_vertical_slab.json", advancement(base, "vertical_slab", ing))

        full_name = f"{title_name(base)} Vertical Slab"
        for prefix in ("block", "item"):
            key = f"{prefix}.blocky13.{base}_vertical_slab"
            if key not in lang:
                lang[key] = full_name

    with open(lang_path, "w") as f:
        json.dump(lang, f, indent=2, ensure_ascii=False)
    print(f"Generated vertical slabs for {len(bases)} bases.")


# --------------------------------------------------------------------------- #
# Material layers (issue #7)                                                     #
#                                                                               #
# Snow-style stackable layers (1–8) for every registered base. Reuses the       #
# vanilla `minecraft:block/snow_heightN` parents (like the original sand layer)  #
# so the height geometry matches snow exactly; the top layer is a full cube.     #
# --------------------------------------------------------------------------- #

def bs_layer(base):
    variants = {}
    for layers in range(1, 9):
        h = layers * 2
        variants[f"layers={layers}"] = {"model": f"blocky13:block/{base}_layer_height{h}"}
    return {"variants": variants}


def model_layer_height(tex, h, render_type):
    model = {"parent": f"minecraft:block/snow_height{h}",
             "textures": {"particle": tex, "texture": tex}}
    if render_type:
        model["render_type"] = render_type
    return model


def model_layer_full(tex, render_type):
    model = {"parent": "minecraft:block/cube_all", "textures": {"all": tex, "particle": tex}}
    if render_type:
        model["render_type"] = render_type
    return model


def recipe_layer(base, ing):
    return {"type": "minecraft:crafting_shaped", "category": "building",
            "key": {"#": ing},
            "pattern": ["###"],
            "result": {"count": 6, "id": f"blocky13:{base}_layer"}}


def loot_layer(base):
    bid = f"blocky13:{base}_layer"
    functions = []
    for n in range(1, 9):
        functions.append({"function": "minecraft:set_count", "add": False, "count": float(n),
                          "conditions": [{"condition": "minecraft:block_state_property",
                                          "block": bid,
                                          "properties": {"layers": str(n)}}]})
    functions.append({"function": "minecraft:explosion_decay"})
    return {"type": "minecraft:block", "pools": [{"rolls": 1.0, "bonus_rolls": 0.0,
        "entries": [{"type": "minecraft:item", "name": bid, "functions": functions}]}]}


def generate_layers():
    bs_dir  = os.path.join(ASSETS, "blockstates")
    mb_dir  = os.path.join(ASSETS, "models/block")
    mi_dir  = os.path.join(ASSETS, "models/item")
    it_dir  = os.path.join(ASSETS, "items")
    rec_dir = os.path.join(DATA,   "recipe")
    lt_dir  = os.path.join(DATA,   "loot_table/blocks")
    adv_dir = os.path.join(DATA,   "advancement/recipes/blocky13")

    bases = _discover_bases()
    lang_path = os.path.join(ASSETS, "lang/en_us.json")
    with open(lang_path) as f:
        lang = json.load(f)

    for base in bases:
        tex, render_type = _base_side_texture(base)
        ing = _base_ingredient(base)

        write_json(f"{bs_dir}/{base}_layer.json", bs_layer(base))
        for h in (2, 4, 6, 8, 10, 12, 14):
            write_json(f"{mb_dir}/{base}_layer_height{h}.json",
                       model_layer_height(tex, h, render_type))
        write_json(f"{mb_dir}/{base}_layer_height16.json", model_layer_full(tex, render_type))
        # Item model shows the thinnest layer (matches the original sand layer).
        write_json(f"{mi_dir}/{base}_layer.json",
                   {"parent": f"blocky13:block/{base}_layer_height2"})
        write_json(f"{it_dir}/{base}_layer.json",
                   {"model": {"type": "minecraft:model",
                              "model": f"blocky13:item/{base}_layer"}})
        write_json(f"{rec_dir}/{base}_layer.json", recipe_layer(base, ing))
        write_json(f"{lt_dir}/{base}_layer.json", loot_layer(base))
        write_json(f"{adv_dir}/{base}_layer.json", advancement(base, "layer", ing))

        full_name = f"{title_name(base)} Layer"
        for prefix in ("block", "item"):
            key = f"{prefix}.blocky13.{base}_layer"
            if key not in lang:
                lang[key] = full_name

    with open(lang_path, "w") as f:
        json.dump(lang, f, indent=2, ensure_ascii=False)
    print(f"Generated material layers for {len(bases)} bases.")


# --------------------------------------------------------------------------- #
# Colored torches & lamps (issue #9)                                            #
#                                                                               #
# 16 colored torches (standing + wall, sharing one item) and 16 colored lamps.  #
# Torches reuse the vanilla template_torch / template_torch_wall models, so the  #
# torch texture only needs content in the column the templates sample (x 7-8,    #
# y 6-15, flame brightest at the top). Lamps are full glowing cubes.             #
# --------------------------------------------------------------------------- #

REF_TORCH = os.path.join(ASSETS, "textures/block/reference_torch.png")
REF_LAMP  = os.path.join(ASSETS, "textures/block/reference_lamp.png")


def create_torch_reference(path):
    from PIL import Image
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    # Only the column sampled by template_torch (x = 7,8 ; y = 6..15).
    for y in range(6, 16):
        if y <= 7:
            v = 255   # flame core (the up-face shows uv [7,6,9,8])
        elif y <= 9:
            v = 235   # flame
        elif y <= 11:
            v = 200   # hot tip of the handle
        else:
            v = 130   # handle
        for x in (7, 8):
            px[x, y] = (v, v, v, 255)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)


def create_block_plate_icon(path):
    """Item icon for the block plate (issue #12): a small stack of thin plates."""
    from PIL import Image
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()

    def plate(y0, base):
        for x in range(2, 14):
            for y in range(y0, y0 + 3):
                v = base + 25 if y == y0 else (base - 30 if y == y0 + 2 else base)
                px[x, y] = (v, v, v, 255)
        for y in range(y0, y0 + 3):
            px[2, y] = (60, 60, 60, 255)
            px[13, y] = (60, 60, 60, 255)

    plate(4, 150)
    plate(8, 170)
    plate(12, 140)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)


def create_block_crafting_textures(tx_b):
    """Distinct grass-topped-workbench textures for the Block Crafting station (issue #12)."""
    from PIL import Image

    def planks(px):
        brown, brown2, gap = (106, 77, 46), (120, 88, 54), (74, 53, 31)
        for y in range(16):
            for x in range(16):
                c = brown if (y // 4) % 2 == 0 else brown2
                if y % 4 == 3 or x == 7:
                    c = gap if y % 4 == 3 else c
                if x == 7 and y % 4 != 3:
                    c = gap
                px[x, y] = (c[0], c[1], c[2], 255)

    def grass(px, y0, y1):
        a, b = (96, 160, 54), (110, 174, 68)
        for y in range(y0, y1):
            for x in range(16):
                g = a if (x * 3 + y * 5) % 7 < 4 else b
                px[x, y] = (g[0], g[1], g[2], 255)

    side = Image.new("RGBA", (16, 16))
    ps = side.load()
    planks(ps)
    grass(ps, 0, 3)
    for x in range(16):
        ps[x, 3] = (96, 67, 40, 255)
    os.makedirs(tx_b, exist_ok=True)
    side.save(os.path.join(tx_b, "block_crafting_side.png"))

    top = Image.new("RGBA", (16, 16))
    pt = top.load()
    grass(pt, 0, 16)
    for gy in range(3):
        for gx in range(3):
            x0, y0 = 2 + gx * 4, 2 + gy * 4
            for x in range(x0, x0 + 3):
                for y in range(y0, y0 + 3):
                    pt[x, y] = (120, 88, 54, 255)
            for x in range(x0 - 1, x0 + 3):
                if 0 <= x < 16:
                    pt[x, y0 - 1] = (74, 53, 31, 255)
    top.save(os.path.join(tx_b, "block_crafting_top.png"))


def create_lamp_reference(path):
    from PIL import Image
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 255))
    px = img.load()
    for y in range(16):
        for x in range(16):
            if x in (0, 15) or y in (0, 15):
                v = 95    # dark frame
            elif 3 <= x <= 12 and 3 <= y <= 12:
                v = 215   # glow
            else:
                v = 150   # body
            px[x, y] = (v, v, v, 255)
    for y in range(6, 10):       # bright center cluster
        for x in range(6, 10):
            px[x, y] = (245, 245, 245, 255)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)


def bs_torch(color):
    return {"variants": {"": {"model": f"blocky13:block/{color}_torch"}}}


def bs_wall_torch(color):
    m = f"blocky13:block/{color}_wall_torch"
    return {"variants": {
        "facing=east":  {"model": m},
        "facing=south": {"model": m, "y": 90},
        "facing=west":  {"model": m, "y": 180},
        "facing=north": {"model": m, "y": 270},
    }}


def bs_lamp(color):
    return {"variants": {"": {"model": f"blocky13:block/{color}_lamp"}}}


def advancement_recipe(recipe_id, mc_ing):
    return {
        "parent": "minecraft:recipes/root",
        "criteria": {
            "has_ingredient": {"trigger": "minecraft:inventory_changed",
                               "conditions": {"items": [{"items": mc_ing}]}},
            "has_the_recipe": {"trigger": "minecraft:recipe_unlocked",
                               "conditions": {"recipe": recipe_id}},
        },
        "requirements": [["has_the_recipe", "has_ingredient"]],
        "rewards": {"recipes": [recipe_id]},
    }


def loot_named(drop_item_id):
    return {"type": "minecraft:block", "pools": [{"rolls": 1.0, "bonus_rolls": 0.0,
        "entries": [{"type": "minecraft:item", "name": f"blocky13:{drop_item_id}",
            "functions": [{"function": "minecraft:explosion_decay"}]}]}]}


def generate_torches_and_lamps():
    bs_dir  = os.path.join(ASSETS, "blockstates")
    mb_dir  = os.path.join(ASSETS, "models/block")
    mi_dir  = os.path.join(ASSETS, "models/item")
    it_dir  = os.path.join(ASSETS, "items")
    tx_b    = os.path.join(ASSETS, "textures/block")
    rec_dir = os.path.join(DATA,   "recipe")
    lt_dir  = os.path.join(DATA,   "loot_table/blocks")
    adv_dir = os.path.join(DATA,   "advancement/recipes/blocky13")

    create_torch_reference(REF_TORCH)
    create_lamp_reference(REF_LAMP)

    lang_path = os.path.join(ASSETS, "lang/en_us.json")
    with open(lang_path) as f:
        lang = json.load(f)

    # 16 dye colors, in DyeColor order, reusing the brick palette.
    colors = [(name[:-len("_bricks")], rgb) for name, rgb in BRICKS_MATERIALS]

    for color, rgb in colors:
        dye = f"minecraft:{color}_dye"

        # textures
        recolor_texture(REF_TORCH, rgb, f"{tx_b}/{color}_torch.png")
        recolor_texture(REF_LAMP,  rgb, f"{tx_b}/{color}_lamp.png")

        # blockstates
        write_json(f"{bs_dir}/{color}_torch.json",      bs_torch(color))
        write_json(f"{bs_dir}/{color}_wall_torch.json", bs_wall_torch(color))
        write_json(f"{bs_dir}/{color}_lamp.json",       bs_lamp(color))

        # block models
        write_json(f"{mb_dir}/{color}_torch.json",
                   {"parent": "minecraft:block/template_torch",
                    "textures": {"torch": f"blocky13:block/{color}_torch"}})
        write_json(f"{mb_dir}/{color}_wall_torch.json",
                   {"parent": "minecraft:block/template_torch_wall",
                    "textures": {"torch": f"blocky13:block/{color}_torch"}})
        write_json(f"{mb_dir}/{color}_lamp.json",
                   {"parent": "minecraft:block/cube_all",
                    "textures": {"all": f"blocky13:block/{color}_lamp"}})

        # item models
        write_json(f"{mi_dir}/{color}_torch.json",
                   {"parent": "minecraft:item/generated",
                    "textures": {"layer0": f"blocky13:block/{color}_torch"}})
        write_json(f"{mi_dir}/{color}_lamp.json", {"parent": f"blocky13:block/{color}_lamp"})

        # item definitions
        write_json(f"{it_dir}/{color}_torch.json",
                   {"model": {"type": "minecraft:model", "model": f"blocky13:item/{color}_torch"}})
        write_json(f"{it_dir}/{color}_lamp.json",
                   {"model": {"type": "minecraft:model", "model": f"blocky13:item/{color}_lamp"}})

        # recipes (dye an existing torch / glowstone)
        write_json(f"{rec_dir}/{color}_torch.json",
                   {"type": "minecraft:crafting_shapeless", "category": "misc",
                    "ingredients": ["minecraft:torch", dye],
                    "result": {"count": 1, "id": f"blocky13:{color}_torch"}})
        write_json(f"{rec_dir}/{color}_lamp.json",
                   {"type": "minecraft:crafting_shapeless", "category": "building",
                    "ingredients": ["minecraft:glowstone", dye],
                    "result": {"count": 1, "id": f"blocky13:{color}_lamp"}})

        # loot (the wall torch drops the torch item)
        write_json(f"{lt_dir}/{color}_torch.json",      loot_simple(f"{color}_torch"))
        write_json(f"{lt_dir}/{color}_wall_torch.json", loot_named(f"{color}_torch"))
        write_json(f"{lt_dir}/{color}_lamp.json",       loot_simple(f"{color}_lamp"))

        # advancements
        write_json(f"{adv_dir}/{color}_torch.json",
                   advancement_recipe(f"blocky13:{color}_torch", "minecraft:torch"))
        write_json(f"{adv_dir}/{color}_lamp.json",
                   advancement_recipe(f"blocky13:{color}_lamp", "minecraft:glowstone"))

        # lang
        label = title_name(color)
        for key, name in (
            (f"block.blocky13.{color}_torch", f"{label} Torch"),
            (f"item.blocky13.{color}_torch", f"{label} Torch"),
            (f"block.blocky13.{color}_wall_torch", f"{label} Wall Torch"),
            (f"block.blocky13.{color}_lamp", f"{label} Lamp"),
            (f"item.blocky13.{color}_lamp", f"{label} Lamp"),
        ):
            if key not in lang:
                lang[key] = name

    with open(lang_path, "w") as f:
        json.dump(lang, f, indent=2, ensure_ascii=False)
    print(f"Generated colored torches and lamps for {len(colors)} colors.")


# --------------------------------------------------------------------------- #
# Book pile                                                                    #
#                                                                               #
# 1-4 books lying on the floor (placed by using a vanilla book on a block).     #
# Each book is a 10x2x12 box; each color has one 16x16 texture laid out as:     #
#   rows 0-1  spine strip   (uv x 2..14)                                        #
#   rows 2-13 front cover   (uv x 3..13)                                        #
#   rows 14-15 page edges   (uv x 2..14)                                        #
# --------------------------------------------------------------------------- #

# (color name, cover rgb, x0, z0, y rotation) — bottom book first.
BOOK_PILE_BOOKS = [
    ("red",   (150,  38,  36), 3.0, 2.0,   0.0),
    ("blue",  ( 44,  66, 140), 3.0, 2.0,  22.5),
    ("green", ( 52, 110,  48), 4.0, 2.0,   0.0),
    ("brown", (110,  72,  40), 2.5, 2.5, -22.5),
]


def create_book_texture(path, rgb):
    from PIL import Image

    def shade(c, f):
        return tuple(max(0, min(255, int(v * f))) for v in c) + (255,)

    img = Image.new("RGBA", (16, 16))
    px = img.load()
    gold = (222, 177, 45, 255)
    for y in range(16):
        for x in range(16):
            # deterministic speckle so the leather isn't flat
            px[x, y] = shade(rgb, 1.0 + (((x * 7 + y * 13) % 5) - 2) * 0.03)

    # spine: darker lower row, two gold bands
    for x in range(16):
        px[x, 1] = shade(rgb, 0.8)
    for x in (4, 11):
        px[x, 0] = gold
        px[x, 1] = shade(gold[:3], 0.8)

    # cover: dark border around the 10x12 face, parchment title label
    for x in range(3, 13):
        px[x, 2] = shade(rgb, 0.65)
        px[x, 13] = shade(rgb, 0.65)
    for y in range(2, 14):
        px[3, y] = shade(rgb, 0.65)
        px[12, y] = shade(rgb, 0.65)
    for y in range(4, 7):
        for x in range(5, 11):
            px[x, y] = (218, 204, 164, 255)
    for x in range(6, 10):
        px[x, 5] = (90, 70, 50, 255)

    # page edges
    for x in range(16):
        px[x, 14] = (238, 232, 212, 255) if x % 3 else (224, 216, 192, 255)
        px[x, 15] = (212, 203, 178, 255)

    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)


def book_element(color, x0, z0, rot, y0, cull_down):
    tex = f"#{color}"
    cover = {"uv": [3, 2, 13, 14], "texture": tex}
    down = dict(cover)
    if cull_down:
        down["cullface"] = "down"
    element = {
        "from": [x0, y0, z0], "to": [x0 + 10, y0 + 2, z0 + 12],
        "faces": {
            "up":    cover,
            "down":  down,
            "west":  {"uv": [2, 0, 14, 2],   "texture": tex},   # spine
            "east":  {"uv": [2, 14, 14, 16], "texture": tex},   # pages
            "north": {"uv": [3, 14, 13, 16], "texture": tex},
            "south": {"uv": [3, 14, 13, 16], "texture": tex},
        },
    }
    if rot:
        element["rotation"] = {"angle": rot, "axis": "y", "origin": [8, y0 + 1, 8]}
    return element


def model_book_pile(count):
    textures = {"particle": f"blocky13:block/book_{BOOK_PILE_BOOKS[0][0]}"}
    elements = []
    for i, (color, _rgb, x0, z0, rot) in enumerate(BOOK_PILE_BOOKS[:count]):
        textures[color] = f"blocky13:block/book_{color}"
        elements.append(book_element(color, x0, z0, rot, i * 2, cull_down=(i == 0)))
    return {"parent": "minecraft:block/block", "textures": textures, "elements": elements}


def bs_book_pile():
    rotations = {"north": 0, "east": 90, "south": 180, "west": 270}
    variants = {}
    for facing, y in rotations.items():
        for n in range(1, len(BOOK_PILE_BOOKS) + 1):
            v = {"model": f"blocky13:block/book_pile_{n}"}
            if y:
                v["y"] = y
            variants[f"books={n},facing={facing}"] = v
    return {"variants": variants}


def loot_book_pile():
    functions = []
    for n in range(1, len(BOOK_PILE_BOOKS) + 1):
        functions.append({"function": "minecraft:set_count", "add": False, "count": float(n),
                          "conditions": [{"condition": "minecraft:block_state_property",
                                          "block": "blocky13:book_pile",
                                          "properties": {"books": str(n)}}]})
    functions.append({"function": "minecraft:explosion_decay"})
    return {"type": "minecraft:block", "pools": [{"rolls": 1.0, "bonus_rolls": 0.0,
        "entries": [{"type": "minecraft:item", "name": "minecraft:book", "functions": functions}]}]}


def generate_book_pile():
    tx_b = os.path.join(ASSETS, "textures/block")
    for color, rgb, *_ in BOOK_PILE_BOOKS:
        create_book_texture(f"{tx_b}/book_{color}.png", rgb)
    for n in range(1, len(BOOK_PILE_BOOKS) + 1):
        write_json(os.path.join(ASSETS, f"models/block/book_pile_{n}.json"), model_book_pile(n))
    write_json(os.path.join(ASSETS, "blockstates/book_pile.json"), bs_book_pile())
    write_json(os.path.join(DATA, "loot_table/blocks/book_pile.json"), loot_book_pile())

    lang_path = os.path.join(ASSETS, "lang/en_us.json")
    with open(lang_path) as f:
        lang = json.load(f)
    lang.setdefault("block.blocky13.book_pile", "Book Pile")
    with open(lang_path, "w") as f:
        json.dump(lang, f, indent=2, ensure_ascii=False)
    print("Generated book pile.")


if __name__ == "__main__":
    if "--book-pile" in sys.argv:
        generate_book_pile()
        sys.exit(0)

    if "--walls-tags" in sys.argv:
        # No PIL needed: walls reuse existing base textures.
        generate_walls_and_tags()
        sys.exit(0)

    if "--torches-lamps" in sys.argv:
        generate_torches_and_lamps()
        sys.exit(0)

    if "--vertical-slabs" in sys.argv:
        # No PIL needed: vertical slabs reuse existing base textures.
        generate_vertical_slabs()
        generate_walls_and_tags()  # refresh mining tags to include vertical slabs
        sys.exit(0)

    if "--layers" in sys.argv:
        # No PIL needed: layers reuse existing base textures.
        generate_layers()
        generate_walls_and_tags()  # refresh mining tags to include layers
        sys.exit(0)

    for base_id, mc_tex, rgb, is_transparent in MATERIALS:
        print(f"Generating: {base_id}")
        generate_for_material(base_id, mc_tex, rgb, is_transparent)

    create_brick_reference(os.path.join(ASSETS, "textures/block/reference_bricks.png"))
    for base_id, rgb in BRICKS_MATERIALS:
        print(f"Generating bricks: {base_id}")
        generate_for_bricks(base_id, rgb)

    generate_brush_assets()
    create_block_plate_icon(os.path.join(ASSETS, "textures/item/block_plate.png"))
    create_block_crafting_textures(os.path.join(ASSETS, "textures/block"))
    generate_lang_entries()
    generate_vertical_slabs()
    generate_layers()
    generate_torches_and_lamps()
    generate_book_pile()
    generate_walls_and_tags()
    print(f"\nDone! Generated assets for {len(MATERIALS)} materials and {len(BRICKS_MATERIALS)} brick sets.")
