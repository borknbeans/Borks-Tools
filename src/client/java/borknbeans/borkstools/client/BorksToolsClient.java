package borknbeans.borkstools.client;

import borknbeans.borkstools.BorksTools;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.resources.Identifier;

public class BorksToolsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ItemTintSources.ID_MAPPER.put(
            Identifier.fromNamespaceAndPath(BorksTools.MOD_ID, "material"),
            MaterialTintSource.MAP_CODEC
        );
    }
}
