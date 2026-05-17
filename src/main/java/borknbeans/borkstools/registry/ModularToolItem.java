package borknbeans.borkstools.registry;

import borknbeans.borkstools.listener.MaterialReloader;
import borknbeans.borkstools.material.ToolMaterial;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DamageResistant;
import net.minecraft.world.level.block.state.BlockState;

public class ModularToolItem extends Item {

    public ModularToolItem(Properties properties) {
        super(properties);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (!state.is(BlockTags.MINEABLE_WITH_PICKAXE)) return 1.0f;
        ToolMaterial head    = getMaterial(stack, ModComponents.HEAD_MATERIAL);
        ToolMaterial handle  = getMaterial(stack, ModComponents.HANDLE_MATERIAL);
        ToolMaterial binding = getMaterial(stack, ModComponents.BINDING_MATERIAL);
        if (head == null || handle == null || binding == null) return 1.0f;
        float baseSpeed = head.speed();
        if (Boolean.TRUE.equals(stack.get(ModComponents.DIAMOND_TIPPED)))    baseSpeed += 2.0f;
        if (Boolean.TRUE.equals(stack.get(ModComponents.NETHERITE_PLATING))) baseSpeed += 1.0f;
        return baseSpeed * handle.speedMultiplier() * binding.speedMultiplier();
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        if (!state.is(BlockTags.MINEABLE_WITH_PICKAXE)) return false;
        if (Boolean.TRUE.equals(stack.get(ModComponents.NETHERITE_PLATING)))
            return !state.is(BlockTags.INCORRECT_FOR_NETHERITE_TOOL);
        if (Boolean.TRUE.equals(stack.get(ModComponents.DIAMOND_TIPPED)))
            return !state.is(BlockTags.INCORRECT_FOR_DIAMOND_TOOL);
        ToolMaterial head = getMaterial(stack, ModComponents.HEAD_MATERIAL);
        return head != null && !state.is(head.incorrectBlocksForDrops());
    }

    /** Recalculates MAX_DAMAGE. Call at assembly time (no registry access needed). */
    public static void recalculate(ItemStack stack) {
        ToolMaterial head    = getMaterial(stack, ModComponents.HEAD_MATERIAL);
        ToolMaterial handle  = getMaterial(stack, ModComponents.HANDLE_MATERIAL);
        ToolMaterial binding = getMaterial(stack, ModComponents.BINDING_MATERIAL);
        if (head == null || handle == null || binding == null) return;

        int baseDurability = head.durability();
        if (Boolean.TRUE.equals(stack.get(ModComponents.DIAMOND_TIPPED)))    baseDurability += 750;
        if (Boolean.TRUE.equals(stack.get(ModComponents.NETHERITE_PLATING))) baseDurability += 500;
        stack.set(DataComponents.MAX_DAMAGE, (int)(baseDurability * handle.durabilityMultiplier() * binding.durabilityMultiplier()));
    }

    /** Recalculates MAX_DAMAGE and DAMAGE_RESISTANT. Call when applying/removing upgrades. */
    public static void recalculate(ItemStack stack, RegistryAccess registryAccess) {
        recalculate(stack);

        if (Boolean.TRUE.equals(stack.get(ModComponents.NETHERITE_PLATING))) {
            HolderLookup.RegistryLookup<DamageType> lookup = registryAccess.lookupOrThrow(Registries.DAMAGE_TYPE);
            stack.set(DataComponents.DAMAGE_RESISTANT, new DamageResistant(lookup.getOrThrow(DamageTypeTags.IS_FIRE)));
        } else {
            stack.remove(DataComponents.DAMAGE_RESISTANT);
        }
    }

    private static ToolMaterial getMaterial(ItemStack stack, DataComponentType<Identifier> component) {
        Identifier id = stack.get(component);
        return id != null ? MaterialReloader.MATERIALS.get(id) : null;
    }
}
