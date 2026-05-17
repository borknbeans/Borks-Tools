package borknbeans.borkstools.registry;

import borknbeans.borkstools.BorksTools;
import borknbeans.borkstools.recipe.AssemblyRecipe;
import borknbeans.borkstools.recipe.PartCraftingRecipe;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class ModRecipes {

    public static RecipeType<PartCraftingRecipe> PART_CRAFTING_TYPE;
    public static RecipeSerializer<PartCraftingRecipe> PART_CRAFTING_SERIALIZER;

    public static RecipeType<AssemblyRecipe> ASSEMBLY_TYPE;
    public static RecipeSerializer<AssemblyRecipe> ASSEMBLY_SERIALIZER;

    public static void init() {
        PART_CRAFTING_TYPE = Registry.register(
            BuiltInRegistries.RECIPE_TYPE,
            Identifier.fromNamespaceAndPath(BorksTools.MOD_ID, "part_crafting"),
            new RecipeType<PartCraftingRecipe>() {
                @Override public String toString() { return "borks-tools:part_crafting"; }
            }
        );

        PART_CRAFTING_SERIALIZER = Registry.register(
            BuiltInRegistries.RECIPE_SERIALIZER,
            Identifier.fromNamespaceAndPath(BorksTools.MOD_ID, "part_crafting"),
            new RecipeSerializer<>(PartCraftingRecipe.CODEC, PartCraftingRecipe.STREAM_CODEC)
        );

        ASSEMBLY_TYPE = Registry.register(
            BuiltInRegistries.RECIPE_TYPE,
            Identifier.fromNamespaceAndPath(BorksTools.MOD_ID, "assembly"),
            new RecipeType<AssemblyRecipe>() {
                @Override public String toString() { return "borks-tools:assembly"; }
            }
        );

        ASSEMBLY_SERIALIZER = Registry.register(
            BuiltInRegistries.RECIPE_SERIALIZER,
            Identifier.fromNamespaceAndPath(BorksTools.MOD_ID, "assembly"),
            new RecipeSerializer<>(AssemblyRecipe.CODEC, AssemblyRecipe.STREAM_CODEC)
        );
    }
}
