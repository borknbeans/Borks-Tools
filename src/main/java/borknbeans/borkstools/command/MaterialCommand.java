package borknbeans.borkstools.command;

import borknbeans.borkstools.listener.MaterialReloader;
import borknbeans.borkstools.material.ToolMaterial;
import borknbeans.borkstools.registry.ModComponents;
import borknbeans.borkstools.registry.ModItems;
import borknbeans.borkstools.registry.ModularToolItem;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import static net.minecraft.commands.Commands.literal;

public class MaterialCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(literal("material")
                .executes(MaterialCommand::execute)
                .then(literal("upgrade")
                    .then(literal("diamond_tipped")
                        .executes(ctx -> applyUpgrade(ctx, ModComponents.DIAMOND_TIPPED, "diamond_tipped")))
                    .then(literal("netherite_plating")
                        .executes(ctx -> applyUpgrade(ctx, ModComponents.NETHERITE_PLATING, "netherite_plating")))
                    .then(literal("remove")
                        .then(literal("diamond_tipped")
                            .executes(ctx -> removeUpgrade(ctx, ModComponents.DIAMOND_TIPPED, "diamond_tipped")))
                        .then(literal("netherite_plating")
                            .executes(ctx -> removeUpgrade(ctx, ModComponents.NETHERITE_PLATING, "netherite_plating")))
                    )
                )
            )
        );
    }

    private static int execute(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();

        if (source.getEntity() == null) {
            source.sendFailure(Component.literal("Must be run by a player."));
            return 0;
        }

        ItemStack stack = source.getPlayer().getMainHandItem();

        if (stack.is(ModItems.MODULAR_PICKAXE)) {
            printAssembledTool(source, stack);
            return 1;
        }

        Identifier materialId = stack.get(ModComponents.MATERIAL);
        if (materialId == null) {
            source.sendFailure(Component.literal("Held item has no material component."));
            return 0;
        }

        ToolMaterial material = MaterialReloader.MATERIALS.get(materialId);
        if (material == null) {
            source.sendFailure(Component.literal("Material '" + materialId + "' is not loaded."));
            return 0;
        }

        source.sendSuccess(() -> Component.literal(formatMaterial("Material", materialId, material)), false);
        return 1;
    }

    private static void printAssembledTool(CommandSourceStack source, ItemStack stack) {
        Identifier headId    = stack.get(ModComponents.HEAD_MATERIAL);
        Identifier handleId  = stack.get(ModComponents.HANDLE_MATERIAL);
        Identifier bindingId = stack.get(ModComponents.BINDING_MATERIAL);

        StringBuilder sb = new StringBuilder("Modular Pickaxe:\n");

        appendMaterialLine(sb, "Head",    headId);
        appendMaterialLine(sb, "Handle",  handleId);
        appendMaterialLine(sb, "Binding", bindingId);

        if (headId != null && handleId != null && bindingId != null) {
            ToolMaterial head    = MaterialReloader.MATERIALS.get(headId);
            ToolMaterial handle  = MaterialReloader.MATERIALS.get(handleId);
            ToolMaterial binding = MaterialReloader.MATERIALS.get(bindingId);

            if (head != null && handle != null && binding != null) {
                int baseDurability = head.durability();
                if (Boolean.TRUE.equals(stack.get(ModComponents.DIAMOND_TIPPED)))   baseDurability += 750;
                if (Boolean.TRUE.equals(stack.get(ModComponents.NETHERITE_PLATING))) baseDurability += 500;
                int finalDurability = (int)(baseDurability * handle.durabilityMultiplier() * binding.durabilityMultiplier());

                float baseSpeed = head.speed();
                if (Boolean.TRUE.equals(stack.get(ModComponents.DIAMOND_TIPPED)))   baseSpeed += 2.0f;
                if (Boolean.TRUE.equals(stack.get(ModComponents.NETHERITE_PLATING))) baseSpeed += 1.0f;
                float finalSpeed = baseSpeed * handle.speedMultiplier() * binding.speedMultiplier();

                sb.append("\nCalculated stats:\n");
                sb.append("  Durability: ").append(finalDurability).append("\n");
                sb.append(String.format("  Speed: %.2f\n", finalSpeed));
                sb.append("  Attack damage bonus: ").append(head.attackDamageBonus()).append("\n");
                sb.append("  Enchantment value: ").append(head.enchantmentValue());
            }
        }

        String message = sb.toString();
        source.sendSuccess(() -> Component.literal(message), false);
    }

    private static void appendMaterialLine(StringBuilder sb, String slot, Identifier id) {
        if (id == null) {
            sb.append("  ").append(slot).append(": (none)\n");
            return;
        }
        ToolMaterial mat = MaterialReloader.MATERIALS.get(id);
        sb.append("  ").append(slot).append(": ").append(id)
          .append(mat != null ? " (loaded)" : " (NOT LOADED)").append("\n");
    }

    private static int applyUpgrade(CommandContext<CommandSourceStack> ctx, DataComponentType<Boolean> component, String name) {
        ItemStack stack = getHeldPickaxe(ctx);
        if (stack == null) return 0;
        stack.set(component, true);
        ModularToolItem.recalculate(stack, ctx.getSource().registryAccess());
        ctx.getSource().sendSuccess(() -> Component.literal("Applied " + name + " to held pickaxe."), false);
        return 1;
    }

    private static int removeUpgrade(CommandContext<CommandSourceStack> ctx, DataComponentType<Boolean> component, String name) {
        ItemStack stack = getHeldPickaxe(ctx);
        if (stack == null) return 0;
        stack.remove(component);
        ModularToolItem.recalculate(stack, ctx.getSource().registryAccess());
        ctx.getSource().sendSuccess(() -> Component.literal("Removed " + name + " from held pickaxe."), false);
        return 1;
    }

    private static ItemStack getHeldPickaxe(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        if (source.getEntity() == null) {
            source.sendFailure(Component.literal("Must be run by a player."));
            return null;
        }
        ItemStack stack = source.getPlayer().getMainHandItem();
        if (!stack.is(ModItems.MODULAR_PICKAXE)) {
            source.sendFailure(Component.literal("Must be holding a modular pickaxe."));
            return null;
        }
        return stack;
    }

    private static String formatMaterial(String label, Identifier id, ToolMaterial material) {
        return label + ": " + id + "\n" +
            "  Mining level: " + material.incorrectBlocksForDrops().location() + "\n" +
            "  Durability: " + material.durability() + "\n" +
            "  Speed: " + material.speed() + "\n" +
            "  Attack damage bonus: " + material.attackDamageBonus() + "\n" +
            "  Enchantment value: " + material.enchantmentValue() + "\n" +
            "  Speed multiplier: " + material.speedMultiplier() + "\n" +
            "  Durability multiplier: " + material.durabilityMultiplier() + "\n" +
            "  Repair items: " + material.repairItems().location();
    }
}
