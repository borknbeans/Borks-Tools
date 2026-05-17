package borknbeans.borkstools.registry;

import borknbeans.borkstools.listener.MaterialReloader;
import borknbeans.borkstools.material.ToolMaterial;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class ModularToolItem extends Item {

    public ModularToolItem(Properties properties) {
        super(properties);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (!state.is(BlockTags.MINEABLE_WITH_PICKAXE)) return 1.0f;
        ToolMaterial handle = getMaterial(stack, ModComponents.HANDLE_MATERIAL);
        return handle != null ? handle.speed() : 1.0f;
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        ToolMaterial head = getMaterial(stack, ModComponents.HEAD_MATERIAL);
        if (head == null) return false;
        return state.is(BlockTags.MINEABLE_WITH_PICKAXE) && !state.is(head.incorrectBlocksForDrops());
    }

    private static ToolMaterial getMaterial(ItemStack stack, DataComponentType<Identifier> component) {
        Identifier id = stack.get(component);
        return id != null ? MaterialReloader.MATERIALS.get(id) : null;
    }
}
