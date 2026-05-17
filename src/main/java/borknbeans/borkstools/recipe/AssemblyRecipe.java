package borknbeans.borkstools.recipe;

import borknbeans.borkstools.listener.MaterialReloader;
import borknbeans.borkstools.material.ToolMaterial;
import borknbeans.borkstools.registry.ModComponents;
import borknbeans.borkstools.registry.ModItems;
import borknbeans.borkstools.registry.ModRecipes;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class AssemblyRecipe extends CustomRecipe {

    public static final MapCodec<AssemblyRecipe> CODEC = MapCodec.unit(new AssemblyRecipe());
    public static final StreamCodec<RegistryFriendlyByteBuf, AssemblyRecipe> STREAM_CODEC =
        StreamCodec.unit(new AssemblyRecipe());

    @Override
    public boolean matches(CraftingInput input, Level level) {
        for (int col = 0; col < input.width(); col++) {
            if (matchesColumn(input, col)) return true;
        }
        return false;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        for (int col = 0; col < input.width(); col++) {
            if (matchesColumn(input, col)) {
                ItemStack headStack    = input.getItem(col);
                ItemStack bindingStack = input.getItem(input.width() + col);
                ItemStack handleStack  = input.getItem(2 * input.width() + col);

                Identifier headId    = headStack.get(ModComponents.MATERIAL);
                Identifier handleId  = handleStack.get(ModComponents.MATERIAL);
                Identifier bindingId = bindingStack.get(ModComponents.MATERIAL);

                ItemStack result = new ItemStack(ModItems.MODULAR_PICKAXE);
                result.set(ModComponents.HEAD_MATERIAL,    headId);
                result.set(ModComponents.HANDLE_MATERIAL,  handleId);
                result.set(ModComponents.BINDING_MATERIAL, bindingId);

                ToolMaterial head    = MaterialReloader.MATERIALS.get(headId);
                ToolMaterial handle  = MaterialReloader.MATERIALS.get(handleId);
                ToolMaterial binding = MaterialReloader.MATERIALS.get(bindingId);

                if (head != null && handle != null && binding != null) {
                    int durability = (int) ((head.durability() + handle.durability()) * binding.durabilityMultiplier());
                    result.set(DataComponents.MAX_DAMAGE, durability);

                    float attackDamage = 1.0f + head.attackDamageBonus();
                    result.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
                        .add(Attributes.ATTACK_DAMAGE,
                             new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, attackDamage, AttributeModifier.Operation.ADD_VALUE),
                             EquipmentSlotGroup.MAINHAND)
                        .add(Attributes.ATTACK_SPEED,
                             new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -2.8f, AttributeModifier.Operation.ADD_VALUE),
                             EquipmentSlotGroup.MAINHAND)
                        .build());
                }

                return result;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<AssemblyRecipe> getSerializer() {
        return ModRecipes.ASSEMBLY_SERIALIZER;
    }

    // Checks that exactly one column has head/binding/handle in rows 0-2, all other slots empty.
    private boolean matchesColumn(CraftingInput input, int col) {
        if (input.height() < 3) return false;

        for (int r = 0; r < input.height(); r++) {
            for (int c = 0; c < input.width(); c++) {
                boolean filled = !input.getItem(r * input.width() + c).isEmpty();
                boolean expected = (c == col && r < 3);
                if (filled != expected) return false;
            }
        }

        ItemStack head    = input.getItem(col);
        ItemStack binding = input.getItem(input.width() + col);
        ItemStack handle  = input.getItem(2 * input.width() + col);

        return head.is(ModItems.PICKAXE_HEAD)   && head.has(ModComponents.MATERIAL)
            && binding.is(ModItems.BINDING)      && binding.has(ModComponents.MATERIAL)
            && handle.is(ModItems.HANDLE)        && handle.has(ModComponents.MATERIAL);
    }
}
