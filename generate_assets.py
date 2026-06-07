#!/usr/bin/env python3
"""
Generate all JSON and PNG assets for new material bases in blocky13.
Run from the project root.
"""
import os
import json
from PIL import Image

ROOT = os.path.dirname(os.path.abspath(__file__))
ASSETS   = os.path.join(ROOT, "src/main/resources/assets/blocky13")
DATA     = os.path.join(ROOT, "src/main/resources/data/blocky13")

REF_CHAIN_BLOCK = os.path.join(ASSETS, "textures/block/iron_block_chain.png")
REF_BARS_BLOCK  = os.path.join(ASSETS, "textures/block/iron_block_bars.png")
REF_SLAB_ITEM   = os.path.join(ASSETS, "textures/item/iron_block_slab.png")
REF_DOOR_ITEM   = os.path.join(ASSETS, "textures/item/iron_block_door.png")
REF_CHAIN_ITEM  = os.path.join(ASSETS, "textures/item/iron_block_chain.png")

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


def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(data, f, indent=2)


def title_name(base_id):
    return " ".join(w.capitalize() for w in base_id.split("_"))


def recolor_texture(ref_path, rgb, output_path, alpha=255):
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

    with open(lang_path, "w") as f:
        json.dump(lang, f, indent=2, ensure_ascii=False)
    print(f"Updated lang file with {len(MATERIALS)} new materials")


if __name__ == "__main__":
    for base_id, mc_tex, rgb, is_transparent in MATERIALS:
        print(f"Generating: {base_id}")
        generate_for_material(base_id, mc_tex, rgb, is_transparent)

    generate_lang_entries()
    print(f"\nDone! Generated assets for {len(MATERIALS)} materials.")
