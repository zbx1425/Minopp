package cn.zbx1425.minopp.neoforge;

import cn.zbx1425.minopp.Mino;
import cn.zbx1425.minopp.MinoClient;
import cn.zbx1425.minopp.item.ItemHandCards;
import cn.zbx1425.minopp.platform.neoforge.CompatPacketRegistry;
import cn.zbx1425.minopp.platform.neoforge.RegistriesWrapperImpl;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.Optional;
import java.util.UUID;

@Mod(Mino.MOD_ID)
public final class MinoNeoForge {

    private static final RegistriesWrapperImpl registries = new RegistriesWrapperImpl();
    public static final CompatPacketRegistry PACKET_REGISTRY = new CompatPacketRegistry();

    public MinoNeoForge(IEventBus eventBus) {
        Mino.init(registries);

        registries.registerAllDeferred(eventBus);
        eventBus.register(RegistriesWrapperImpl.RegisterCreativeTabs.class);
        eventBus.register(ModEventBusListener.class);
        NeoForge.EVENT_BUS.register(ForgeEventBusListener.class);
        if (FMLEnvironment.dist.isClient()) {
            MinoClient.init();
            eventBus.register(ClientProxy.ModEventBusListener.class);
            NeoForge.EVENT_BUS.register(ClientProxy.ForgeEventBusListener.class);
        }
    }

    public static class ModEventBusListener {

        @SubscribeEvent
        public static void registerPayloadHandlers(final RegisterPayloadHandlersEvent event) {
            PayloadRegistrar registrar = event.registrar("1");
            MinoNeoForge.PACKET_REGISTRY.commit(registrar);
        }
    }

    public static class ForgeEventBusListener {

        @SubscribeEvent
        public static void onRegisterCommands(final RegisterCommandsEvent event) {
            event.getDispatcher().register(Commands.literal("minopp")
                    .then(Commands.literal("give_test_card").requires((commandSourceStack) -> commandSourceStack.hasPermission(2))
                        .executes(context -> {
                            ItemStack stack = new ItemStack(Mino.ITEM_HAND_CARDS.get());
                            UUID uuid = UUID.randomUUID();
                            stack.set(Mino.DATA_COMPONENT_TYPE_CARD_GAME_BINDING.get(),
                                    new ItemHandCards.CardGameBindingComponent(uuid, Optional.empty()));
                            stack.set(DataComponents.CUSTOM_NAME, Component.literal("Test Card " + uuid.toString().substring(0, 8)));
                            context.getSource().getPlayerOrException().getInventory().add(stack);
                            return 1;
                        }))
                    .then(Commands.literal("shout")
                        .executes(context -> {
                            boolean success = Mino.onServerChatMessage("mino", context.getSource().getPlayerOrException());
                            if (!success) throw new SimpleCommandExceptionType(Component.translatable("game.minopp.play.no_game")).create();
                            return 1;
                        }))
            );
        }

        @SubscribeEvent
        public static void onServerChatMessage(final ServerChatEvent event) {
            Mino.onServerChatMessage(event.getRawText(), event.getPlayer());
        }

        @SubscribeEvent
        public static void onLivingIncomingDamage(final EntityInvulnerabilityCheckEvent event) {
            if (event.getEntity().level().isClientSide) return;
            if (event.getEntity() instanceof Player targetPlayer && event.getSource().getEntity() instanceof Player srcPlayer) {
                Mino.onPlayerHurtPlayer(targetPlayer, srcPlayer);
            }
        }
    }
}
