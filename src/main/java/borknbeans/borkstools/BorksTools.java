package borknbeans.borkstools;

import borknbeans.borkstools.command.MaterialCommand;
import borknbeans.borkstools.listener.MaterialReloader;
import borknbeans.borkstools.registry.ModComponents;
import borknbeans.borkstools.registry.ModItems;
import borknbeans.borkstools.registry.ModRecipes;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BorksTools implements ModInitializer {
	public static final String MOD_ID = "borks-tools";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModComponents.init();
		ModRecipes.init();
		ModItems.init();
		MaterialCommand.register();
		ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(Identifier.parse("materials"), new MaterialReloader());
	}
}