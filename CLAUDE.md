# Bork's Tools — Project Context for AI Agents

## What This Mod Is
A Fabric Minecraft mod (version 26.1) that implements a Tinkers' Construct-style modular tool system. Players craft individual tool parts (head, binding, handle) from different materials, then assemble them into tools. The final tool's stats are calculated dynamically from the three parts.

## Current State
Steps 1–4 of the initial plan are implemented and tested:
- **Step 1** ✅ Data-driven material system (JSON files, loaded via resource reload)
- **Step 2** ✅ Part crafting recipes (custom recipe type, one recipe per part type)
- **Step 3** ✅ Assembly recipe (custom recipe type, transfers components to output)
- **Step 4** ✅ Functional tool (mining speed, mining level, durability, attack damage)
- **Models** ✅ Part items and modular pickaxe have models/item definitions
- **Pending** — Color tinting system (grayscale textures + per-material color in JSON)

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
│   ├── ModComponents.java           — Registers DataComponentType<Identifier> for:
│   │                                  MATERIAL, HEAD_MATERIAL, HANDLE_MATERIAL, BINDING_MATERIAL
│   ├── ModItems.java                — Registers: MODULAR_PICKAXE, PICKAXE_HEAD, HANDLE, BINDING
│   ├── ModRecipes.java              — Registers recipe types + serializers for both custom recipes
│   └── ModularToolItem.java         — Item subclass for assembled tool
│                                      Overrides getDestroySpeed + isCorrectToolForDrops
│                                      using HEAD_MATERIAL (mining level) and HANDLE_MATERIAL (speed)
```

---

## Key Systems

### Materials (data-driven)
- JSON files live at `src/main/resources/data/borks-tools/materials/*.json`
- Loaded by `MaterialReloader` on every resource reload (world load, /reload)
- Stored in `MaterialReloader.MATERIALS: Map<Identifier, ToolMaterial>`
- Reverse lookup `MaterialReloader.INGREDIENT_TO_MATERIAL: Map<Identifier, Identifier>` maps crafting item ID → material ID (used by `PartCraftingRecipe`)

**Material JSON fields:**
```json
{
  "mining_level": "iron",          // wood/stone/copper/iron/diamond/gold/netherite
  "durability": 250,
  "speed": 6.0,
  "attack_damage_bonus": 2.0,
  "enchantment_value": 14,
  "durability_multiplier": 1.2,    // used by binding slot only
  "repair_items": "#minecraft:iron_tool_materials",
  "crafting_ingredient": "minecraft:iron_ingot"
}
```

### Data Components
All stored as `DataComponentType<Identifier>` pointing to a material ID (e.g. `borks-tools:materials/iron`):
- `borks-tools:material` — on part items (head/binding/handle)
- `borks-tools:head_material` — on assembled modular pickaxe
- `borks-tools:handle_material` — on assembled modular pickaxe
- `borks-tools:binding_material` — on assembled modular pickaxe

### Stat Calculation
```
final_durability = (head.durability + handle.durability) * binding.durabilityMultiplier
speed            = handle.speed
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
Output: modular_pickaxe with HEAD/HANDLE/BINDING_MATERIAL components + computed MAX_DAMAGE + ATTRIBUTE_MODIFIERS

### Recipes (JSON)
`src/main/resources/data/borks-tools/recipe/`
- `pickaxe_head.json`, `handle.json`, `binding.json` — part crafting (one per part type)
- `modular_pickaxe.json` — assembly

---

## Planned Next Features
- **Color tinting**: Each material will have a single hex color in its JSON (`"color": "#RRGGBB"`). Part item textures will be grayscale. A client-side color provider reads the `MATERIAL` component and tints the texture at render time. The modular pickaxe uses separate overlay textures per part (positioned for compositing) each tinted by the corresponding material component.
- **More tool types**: Axe, shovel, sword, hoe — same architecture, just new part types and assembly recipes
- **Custom crafting block**: Replace crafting table recipes with a dedicated station (already planned but deferred)

---

## Minecraft Version Notes
- **Minecraft**: 26.1 (new versioning scheme, ~2025/2026 era)
- **Fabric Loader**: 0.19.2, **Fabric API**: 0.145.1+26.1
- **Java**: 25
- `ResourceLocation` was renamed to `Identifier` in this version (`net.minecraft.resources.Identifier`)
- `RecipeSerializer` is a **record** in this version — instantiate with `new RecipeSerializer<>(codec, streamCodec)`, not an anonymous class
- Tools are plain `Item` subclasses in this version — behavior set via `Item.Properties.pickaxe()`/`.axe()` etc. which sets a `TOOL` component. For dynamic tools, override `getDestroySpeed` and `isCorrectToolForDrops` directly instead.
- Item models use the new `assets/<namespace>/items/<item>.json` definition file (introduced ~1.21.2) that references a model in `assets/<namespace>/models/item/`
- `ItemAttributeModifiers` is in `net.minecraft.world.item.component`
- `Enchantable` component class location is currently unknown — `DataComponents.ENCHANTABLE` stores it but the class file wasn't found in decompiled source
