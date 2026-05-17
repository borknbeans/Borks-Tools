package borknbeans.borkstools.recipe;

import borknbeans.borkstools.listener.MaterialReloader;
import borknbeans.borkstools.registry.ModComponents;
import borknbeans.borkstools.registry.ModRecipes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class PartCraftingRecipe extends CustomRecipe {

    public static final MapCodec<PartCraftingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Identifier.CODEC.fieldOf("catalyst").forGetter(r -> r.catalystId),
            Identifier.CODEC.fieldOf("result").forGetter(r -> r.resultId)
        ).apply(instance, PartCraftingRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PartCraftingRecipe> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.fromCodec(Identifier.CODEC), r -> r.catalystId,
            ByteBufCodecs.fromCodec(Identifier.CODEC), r -> r.resultId,
            PartCraftingRecipe::new
        );

    private final Identifier catalystId;
    private final Identifier resultId;

    public PartCraftingRecipe(Identifier catalystId, Identifier resultId) {
        this.catalystId = catalystId;
        this.resultId = resultId;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        List<ItemStack> items = nonEmpty(input);
        if (items.size() != 2) return false;

        boolean hasCatalyst = false;
        boolean hasMaterial = false;
        for (ItemStack stack : items) {
            if (isCatalyst(stack)) hasCatalyst = true;
            else if (MaterialReloader.INGREDIENT_TO_MATERIAL.containsKey(itemId(stack))) hasMaterial = true;
        }
        return hasCatalyst && hasMaterial;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        return doAssemble(input);
    }

    private ItemStack doAssemble(CraftingInput input) {
        for (ItemStack stack : nonEmpty(input)) {
            if (!isCatalyst(stack)) {
                Identifier materialId = MaterialReloader.INGREDIENT_TO_MATERIAL.get(itemId(stack));
                if (materialId != null) {
                    ItemStack result = new ItemStack(resultItem());
                    result.set(ModComponents.MATERIAL, materialId);
                    return result;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<PartCraftingRecipe> getSerializer() {
        return ModRecipes.PART_CRAFTING_SERIALIZER;
    }

    private List<ItemStack> nonEmpty(CraftingInput input) {
        List<ItemStack> result = new ArrayList<>();
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) result.add(stack);
        }
        return result;
    }

    private boolean isCatalyst(ItemStack stack) {
        return itemId(stack).equals(catalystId);
    }

    private Item resultItem() {
        return BuiltInRegistries.ITEM.getValue(resultId);
    }

    private static Identifier itemId(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem());
    }
}
