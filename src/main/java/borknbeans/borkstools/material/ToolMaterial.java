package borknbeans.borkstools.material;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.Map;

public record ToolMaterial(
        Identifier id,
        TagKey<Block> incorrectBlocksForDrops,
        int durability,
        float speed,
        float attackDamageBonus,
        int enchantmentValue,
        float durabilityMultiplier,
        TagKey<Item> repairItems,
        Identifier craftingIngredient
) {
    private static final Map<String, TagKey<Block>> MINING_LEVELS = Map.of(
        "wood",      BlockTags.INCORRECT_FOR_WOODEN_TOOL,
        "stone",     BlockTags.INCORRECT_FOR_STONE_TOOL,
        "copper",    BlockTags.INCORRECT_FOR_COPPER_TOOL,
        "iron",      BlockTags.INCORRECT_FOR_IRON_TOOL,
        "diamond",   BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
        "gold",      BlockTags.INCORRECT_FOR_GOLD_TOOL,
        "netherite", BlockTags.INCORRECT_FOR_NETHERITE_TOOL
    );

    public static ToolMaterial fromJson(Identifier id, JsonObject json) {
        String miningLevel = json.get("mining_level").getAsString();
        TagKey<Block> incorrectBlocks = MINING_LEVELS.get(miningLevel);
        if (incorrectBlocks == null) {
            throw new IllegalArgumentException("Unknown mining_level '" + miningLevel + "' in material " + id);
        }

        int durability = json.get("durability").getAsInt();
        float speed = json.get("speed").getAsFloat();
        float attackDamageBonus = json.get("attack_damage_bonus").getAsFloat();
        int enchantmentValue = json.get("enchantment_value").getAsInt();
        float durabilityMultiplier = json.get("durability_multiplier").getAsFloat();

        String tagStr = json.get("repair_items").getAsString();
        Identifier tagId = Identifier.parse(tagStr.startsWith("#") ? tagStr.substring(1) : tagStr);
        TagKey<Item> repairItems = TagKey.create(Registries.ITEM, tagId);

        Identifier craftingIngredient = Identifier.parse(json.get("crafting_ingredient").getAsString());

        return new ToolMaterial(id, incorrectBlocks, durability, speed, attackDamageBonus, enchantmentValue, durabilityMultiplier, repairItems, craftingIngredient);
    }
}
