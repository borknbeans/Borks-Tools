package borknbeans.borkstools.listener;

import borknbeans.borkstools.BorksTools;
import borknbeans.borkstools.material.ToolMaterial;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.resource.v1.reloader.SimpleReloadListener;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MaterialReloader extends SimpleReloadListener<Map<Identifier, ToolMaterial>> {

    public static final Map<Identifier, ToolMaterial> MATERIALS = new HashMap<>();
    // Maps crafting ingredient item ID → material ID for recipe lookup
    public static final Map<Identifier, Identifier> INGREDIENT_TO_MATERIAL = new HashMap<>();

    @Override
    protected Map<Identifier, ToolMaterial> prepare(SharedState state) {
        Map<Identifier, ToolMaterial> loaded = new HashMap<>();

        state.resourceManager().listResources("materials", id -> id.getPath().endsWith(".json"))
            .forEach((id, resource) -> {
                try (var reader = resource.openAsReader()) {
                    var json = JsonParser.parseReader(reader).getAsJsonObject();
                    // Strip .json so the stored key is e.g. borks-tools:materials/wood
                    Identifier cleanId = Identifier.fromNamespaceAndPath(
                        id.getNamespace(),
                        id.getPath().substring(0, id.getPath().length() - 5)
                    );
                    loaded.put(cleanId, ToolMaterial.fromJson(cleanId, json));
                } catch (IOException e) {
                    BorksTools.LOGGER.error("Failed to load material {}: {}", id, e.getMessage());
                }
            });

        return loaded;
    }

    @Override
    protected void apply(Map<Identifier, ToolMaterial> prepared, SharedState state) {
        MATERIALS.clear();
        MATERIALS.putAll(prepared);

        INGREDIENT_TO_MATERIAL.clear();
        prepared.forEach((id, material) -> INGREDIENT_TO_MATERIAL.put(material.craftingIngredient(), id));

        BorksTools.LOGGER.info("Loaded {} tool materials", MATERIALS.size());
    }
}
