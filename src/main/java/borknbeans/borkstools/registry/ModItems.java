package borknbeans.borkstools.registry;

import borknbeans.borkstools.BorksTools;
import borknbeans.borkstools.part.ToolPartItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import java.util.function.Function;

public class ModItems {
    public static final ModularToolItem MODULAR_PICKAXE = register("modular_pickaxe", ModularToolItem::new, new Item.Properties().durability(250));
    public static final ToolPartItem PICKAXE_HEAD = register("pickaxe_head", ToolPartItem::new, new Item.Properties());
    public static final ToolPartItem HANDLE = register("handle", ToolPartItem::new, new Item.Properties());
    public static final ToolPartItem BINDING = register("binding", ToolPartItem::new, new Item.Properties());

    public static void init() {}

    public static <T extends Item> T register(String name, Function<Item.Properties, T> itemFactory, Item.Properties settings) {
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(BorksTools.MOD_ID, name));
        T item = itemFactory.apply(settings.setId(itemKey));
        Registry.register(BuiltInRegistries.ITEM, itemKey, item);
        return item;
    }
}
