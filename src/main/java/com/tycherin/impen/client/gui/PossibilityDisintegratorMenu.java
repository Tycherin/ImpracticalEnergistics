package com.tycherin.impen.client.gui;

import com.tycherin.impen.blockentity.PossibilityDisintegratorBlockEntity;

import appeng.api.config.SecurityPermissions;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.implementations.UpgradeableMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class PossibilityDisintegratorMenu extends UpgradeableMenu<PossibilityDisintegratorBlockEntity> {

    public static final MenuType<PossibilityDisintegratorMenu> TYPE = MenuTypeBuilder
            .create(PossibilityDisintegratorMenu::new, PossibilityDisintegratorBlockEntity.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("possibility_disintegrator");

    @GuiSync(10)
    public boolean hasLuckConsumable;
    @GuiSync(11)
    public boolean hasLootConsumable;
    @GuiSync(12)
    public boolean hasEggConsumable;
    @GuiSync(13)
    public boolean hasPlayerKillConsumable;

    private int delay = 40;

    public PossibilityDisintegratorMenu(final int id, final Inventory playerInv,
            final PossibilityDisintegratorBlockEntity be) {
        super(TYPE, id, playerInv, be);
    }

    @Override
    public void broadcastChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);

        if (this.isServerSide()) {
            this.delay++;

            var be = (PossibilityDisintegratorBlockEntity) this.getBlockEntity();
            var snapshot = be.getAvailableConsumables();
            this.hasLuckConsumable = snapshot.hasLuck();
            this.hasLootConsumable = snapshot.hasLoot();
            this.hasEggConsumable = snapshot.hasEgg();
            this.hasPlayerKillConsumable = snapshot.hasPlayerKill();

            if (be.getGridNode().getGrid() != null && this.delay > 15) {
                this.delay = 0;
            }
        }

        super.broadcastChanges();
    }
}
