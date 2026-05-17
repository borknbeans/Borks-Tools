package borknbeans.borkstools.client;

import borknbeans.borkstools.listener.MaterialReloader;
import borknbeans.borkstools.material.ToolMaterial;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record MaterialTintSource(Identifier componentId) implements ItemTintSource {

    public static final MapCodec<MaterialTintSource> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Identifier.CODEC.fieldOf("component").forGetter(MaterialTintSource::componentId)
        ).apply(instance, MaterialTintSource::new)
    );

    @Override
    @SuppressWarnings("unchecked")
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        DataComponentType<Identifier> componentType =
            (DataComponentType<Identifier>) BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(componentId);
        if (componentType == null) return -1;

        Identifier materialId = stack.get(componentType);
        if (materialId == null) return -1;

        ToolMaterial material = MaterialReloader.MATERIALS.get(materialId);
        return material != null ? material.color() : -1;
    }

    @Override
    public MapCodec<? extends ItemTintSource> type() {
        return MAP_CODEC;
    }
}
