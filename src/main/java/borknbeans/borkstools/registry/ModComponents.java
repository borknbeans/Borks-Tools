package borknbeans.borkstools.registry;

import borknbeans.borkstools.BorksTools;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;

public class ModComponents {

    public static DataComponentType<Identifier> MATERIAL;
    public static DataComponentType<Identifier> HEAD_MATERIAL;
    public static DataComponentType<Identifier> HANDLE_MATERIAL;
    public static DataComponentType<Identifier> BINDING_MATERIAL;

    public static void init() {
        MATERIAL = register("material");
        HEAD_MATERIAL = register("head_material");
        HANDLE_MATERIAL = register("handle_material");
        BINDING_MATERIAL = register("binding_material");
    }

    private static DataComponentType<Identifier> register(String name) {
        return Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath(BorksTools.MOD_ID, name),
            DataComponentType.<Identifier>builder()
                .persistent(Identifier.CODEC)
                .networkSynchronized(ByteBufCodecs.fromCodec(Identifier.CODEC))
                .build()
        );
    }
}
