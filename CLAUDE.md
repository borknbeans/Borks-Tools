# Bork's Tools — Project Context for AI Agents

## What This Mod Is
A Fabric Minecraft mod (version 26.1) that implements a Tinkers' Construct-style modular tool system. Players craft individual tool parts (head, binding, handle) from different materials, then assemble them into tools. The final tool's stats are calculated dynamically from the three parts.

## Current State
- **Step 1** ✅ Data-driven material system (JSON files, loaded via resource reload)
- **Step 2** ✅ Part crafting recipes (custom recipe type, one recipe per part type)
- **Step 3** ✅ Assembly recipe (custom recipe type, transfers components to output)
- **Step 4** ✅ Functional tool (mining speed, mining level, durability, attack damage)
- **Models** ✅ Part items and modular pickaxe have models/item definitions
- **Color tinting** ✅ Grayscale textures tinted per-material via `MaterialTintSource` + `ItemTintSources`
- **Upgrades** ✅ `diamond_tipped` and `netherite_plating` components registered; overlay models wired up via `minecraft:composite` in item definition. Textures and upgrade application mechanic not yet implemented.
- **Pending** — Upgrade application mechanic (how the player applies upgrades to a pickaxe)
- **Pending** — Overlay textures for `modular_pickaxe_diamond_tipped` and `modular_pickaxe_netherite_plating`
- **Pending** — More tool types (axe, shovel, sword, hoe)
- **Pending** — Custom crafting block

---

## Project Structure

```
src/main/java/borknbeans/borkstools/
├── BorksTools.java                  — Mod entry point, calls all init() methods
├── command/
│   └── MaterialCommand.java         — /material command: prints stats of held item
├── listener/
│   └── MaterialReloader.java        — Resource reload listener, loads material JSONs
│                                      Populates MATERIALS and INGREDIENT_TO_MATERIAL maps
├── material/
│   └── ToolMaterial.java            — Record: all material stats + fromJson()
├── part/
│   ├── PartType.java                — Enum: PICKAXE_HEAD, BINDING, HANDLE
│   └── ToolPartItem.java            — Item subclass for part items (thin wrapper)
├── recipe/
│   ├── AssemblyRecipe.java          — Custom recipe: head+binding+handle → modular pickaxe
│   │                                  Stamps HEAD/HANDLE/BINDING_MATERIAL components +
│   │                                  MAX_DAMAGE + ATTRIBUTE_MODIFIERS on output
│   └── PartCraftingRecipe.java      — Custom recipe: catalyst+material_item → part with MATERIAL
├── registry/
│   ├── ModComponents.java           — Registers DataComponentType<Identifier> for material slots;
│   │                                  also DataComponentType<Boolean> for DIAMOND_TIPPED, NETHERITE_PLATING
│   ├── ModItems.java                — Registers: MODULAR_PICKAXE, PICKAXE_HEAD, HANDLE, BINDING
│   ├── ModRecipes.java              — Registers recipe types + serializers for both custom recipes
│   └── ModularToolItem.java         — Item subclass for assembled tool
│                                      Overrides getDestroySpeed + isCorrectToolForDrops
│                                      Speed = head.speed * handle.speedMultiplier * binding.speedMultiplier
src/client/java/borknbeans/borkstools/client/
├── BorksToolsClient.java            — Registers MaterialTintSource codec via ItemTintSources.ID_MAPPER
└── MaterialTintSource.java          — ItemTintSource impl: reads MATERIAL/HEAD/HANDLE/BINDING component,
                                       looks up ToolMaterial, returns material.color()
```

---

## Key Systems

### Materials (data-driven)
- JSON files live at `src/main/resources/data/borks-tools/materials/*.json`
- Loaded by `MaterialReloader` on every resource reload (world load, /reload)
- Stored in `MaterialReloader.MATERIALS: Map<Identifier, ToolMaterial>`
- Reverse lookup `MaterialReloader.INGREDIENT_TO_MATERIAL: Map<Identifier, Identifier>` maps crafting item ID → material ID (used by `PartCraftingRecipe`)
- **Available materials**: wood, stone, copper, iron, gold (5 total — diamond and netherite are upgrades, not materials)

**Material JSON fields:**
```json
{
  "mining_level": "iron",          // wood/stone/copper/iron/gold
  "durability": 250,               // head base durability
  "speed": 6.0,                    // head base mining speed
  "attack_damage_bonus": 2.0,
  "enchantment_value": 14,
  "speed_multiplier": 0.95,        // applied when used as handle or binding
  "durability_multiplier": 1.25,   // applied when used as handle or binding
  "repair_items": "#minecraft:iron_tool_materials",
  "crafting_ingredient": "minecraft:iron_ingot",
  "color": "#ffffff"               // hex color for texture tinting
}
```

**speed_multiplier / durability_multiplier tradeoffs by material:**
| Material | speed_multiplier | durability_multiplier | Character |
|---|---|---|---|
| wood | 1.10 | 1.00 | Light, slight speed boost |
| stone | 0.90 | 1.20 | Heavy, durable |
| copper | 1.05 | 0.95 | Light metal, soft |
| iron | 0.95 | 1.25 | Standard heavy metal |
| gold | 1.30 | 0.70 | Very fast, very fragile |

### Data Components
Material slots — `DataComponentType<Identifier>` pointing to a material ID (e.g. `borks-tools:materials/iron`):
- `borks-tools:material` — on part items (head/binding/handle)
- `borks-tools:head_material` — on assembled modular pickaxe
- `borks-tools:handle_material` — on assembled modular pickaxe
- `borks-tools:binding_material` — on assembled modular pickaxe

Upgrade flags — `DataComponentType<Boolean>`, presence = upgrade applied:
- `borks-tools:diamond_tipped` — grants diamond-level mining; renders diamond tip overlay
- `borks-tools:netherite_plating` — grants netherite-level durability/resistance; renders netherite overlay

Apply upgrades with: `stack.set(ModComponents.DIAMOND_TIPPED, true)`

### Stat Calculation
```
final_durability = (int)(head.durability * handle.durabilityMultiplier * binding.durabilityMultiplier)
final_speed      = head.speed * handle.speedMultiplier * binding.speedMultiplier
mining_level     = head.incorrectBlocksForDrops  (BlockTag)
attack_damage    = 1.0 + head.attackDamageBonus
enchant_value    = head.enchantmentValue
```

### Recipes
**Part crafting** (`borks-tools:part_crafting`) — shapeless, 2 ingredients:
- Catalyst item (paper → pickaxe_head, stick → handle, string → binding)
- Any item listed as `crafting_ingredient` in a loaded material
- Output: part item with `MATERIAL` component set

**Assembly** (`borks-tools:assembly`) — shaped, vertical column in crafting table:
```
[ pickaxe_head ]   row 0
[ binding      ]   row 1
[ handle       ]   row 2
```
Output: modular_pickaxe with HEAD/HANDLE/BINDING_MATERIAL components + computed MAX_DAMAGE + ATTRIBUTE_MODIFIERS + TOOL component (empty rules, damagePerBlock=1 — required for durability consumption in mineBlock())

### Recipes (JSON)
`src/main/resources/data/borks-tools/recipe/`
- `pickaxe_head.json`, `handle.json`, `binding.json` — part crafting (one per part type)
- `modular_pickaxe.json` — assembly

### Item Models
Item definitions at `assets/borks-tools/items/`:
- Part items (`pickaxe_head`, `handle`, `binding`): single `minecraft:model` with one `borks-tools:material` tint
- `modular_pickaxe`: `minecraft:composite` with:
  - Base model (3 tinted layers: handle=layer0, head=layer1, binding=layer2)
  - `minecraft:condition` on `borks-tools:diamond_tipped` → `modular_pickaxe_diamond_tipped` overlay
  - `minecraft:condition` on `borks-tools:netherite_plating` → `modular_pickaxe_netherite_plating` overlay

Overlay model files at `assets/borks-tools/models/item/`:
- `modular_pickaxe_diamond_tipped.json` — references texture `borks-tools:item/modular_pickaxe_diamond_tipped` (**not yet created**)
- `modular_pickaxe_netherite_plating.json` — references texture `borks-tools:item/modular_pickaxe_netherite_plating` (**not yet created**)

Overlay textures should be same dimensions as the pickaxe textures, with the upgrade visuals drawn and all other pixels transparent. Colors should be baked in (not grayscale — upgrades are not material-tinted).

---

## Minecraft Version Notes
- **Minecraft**: 26.1 (new versioning scheme, ~2025/2026 era)
- **Fabric Loader**: 0.19.2, **Fabric API**: 0.145.1+26.1
- **Java**: 25
- `ResourceLocation` was renamed to `Identifier` in this version (`net.minecraft.resources.Identifier`)
- `RecipeSerializer` is a **record** in this version — instantiate with `new RecipeSerializer<>(codec, streamCodec)`, not an anonymous class
- Tools are plain `Item` subclasses — for dynamic tools, override `getDestroySpeed` and `isCorrectToolForDrops` directly; add an empty-rules `TOOL` component for durability consumption
- Item models use the new `assets/<namespace>/items/<item>.json` definition file that references a model in `assets/<namespace>/models/item/`; supports `minecraft:composite`, `minecraft:condition`, `minecraft:model`
- `ItemAttributeModifiers` is in `net.minecraft.world.item.component`
- `Enchantable` component class location is currently unknown — `DataComponents.ENCHANTABLE` stores it but the class file wasn't found in decompiled source
- Color tinting uses `ItemTintSource` / `ItemTintSources.ID_MAPPER` (Fabric-widened) — `ColorProviderRegistry` was removed in this version
- `has_component` condition predicate in item definitions checks for component presence on the stack
- `minecraft:condition` requires both `on_true` and `on_false` — use `{"type": "minecraft:empty"}` when one branch should render nothing
