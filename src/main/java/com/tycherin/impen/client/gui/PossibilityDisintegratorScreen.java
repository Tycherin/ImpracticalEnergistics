package com.tycherin.impen.client.gui;

import com.tycherin.impen.blockentity.PossibilityDisintegratorBlockEntity;

import appeng.api.stacks.GenericStack;
import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.me.common.ClientDisplaySlot;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.SlotSemantics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.ItemLike;

public class PossibilityDisintegratorScreen extends UpgradeableScreen<PossibilityDisintegratorMenu> {

    private final ToggleableClientDisplaySlot luckSlot;
    private final ToggleableClientDisplaySlot lootSlot;
    private final ToggleableClientDisplaySlot eggSlot;
    private final ToggleableClientDisplaySlot playerKillSlot;

    public PossibilityDisintegratorScreen(final PossibilityDisintegratorMenu menu, final Inventory playerInventory,
            final Component title, final ScreenStyle style) {
        super(menu, playerInventory, title, style);

        this.luckSlot = new ToggleableClientDisplaySlot(PossibilityDisintegratorBlockEntity.CONSUMABLE_LUCK);
        this.lootSlot = new ToggleableClientDisplaySlot(PossibilityDisintegratorBlockEntity.CONSUMABLE_LOOT);
        this.eggSlot = new ToggleableClientDisplaySlot(PossibilityDisintegratorBlockEntity.CONSUMABLE_EGG);
        this.playerKillSlot = new ToggleableClientDisplaySlot(
                PossibilityDisintegratorBlockEntity.CONSUMABLE_PLAYER_KILL);

        // The SlotSemantic values here don't actually make any sense, I'm just hijacking them to get AE2 to render the
        // screen with the layout that I want, and it isn't worth the trouble to register custom semantics just for this
        this.menu.addClientSideSlot(luckSlot, SlotSemantics.INSCRIBER_PLATE_BOTTOM);
        this.menu.addClientSideSlot(lootSlot, SlotSemantics.INSCRIBER_PLATE_TOP);
        this.menu.addClientSideSlot(eggSlot, SlotSemantics.BIOMETRIC_CARD);
        this.menu.addClientSideSlot(playerKillSlot, SlotSemantics.BLANK_PATTERN);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.luckSlot.setActive(menu.hasLuckConsumable);
        this.lootSlot.setActive(menu.hasLootConsumable);
        this.eggSlot.setActive(menu.hasEggConsumable);
        this.playerKillSlot.setActive(menu.hasPlayerKillConsumable);
    }

    private static class ToggleableClientDisplaySlot extends ClientDisplaySlot {
        private boolean activeFlag = false;

        public ToggleableClientDisplaySlot(final ItemLike displayItem) {
            super(GenericStack.fromItemStack(displayItem.asItem().getDefaultInstance()));
        }

        public void setActive(final boolean activeFlag) {
            this.activeFlag = activeFlag;
        }

        @Override
        public boolean isActive() {
            return activeFlag;
        }
    }
}
