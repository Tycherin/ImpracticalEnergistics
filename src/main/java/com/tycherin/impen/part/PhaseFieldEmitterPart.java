package com.tycherin.impen.part;

import java.util.List;

import appeng.api.networking.GridFlags;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.util.AECableType;
import appeng.items.parts.PartModels;
import appeng.parts.AEBasePart;
import appeng.parts.automation.PlaneConnectionHelper;
import appeng.parts.automation.PlaneModelData;
import appeng.parts.automation.PlaneModels;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraftforge.client.model.data.IModelData;

public class PhaseFieldEmitterPart extends AEBasePart {

    // TODO Update these
    private static final PlaneModels MODELS = new PlaneModels("part/capture_plane",
            "part/capture_plane_on");

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    private final PlaneConnectionHelper connectionHelper = new PlaneConnectionHelper(this);

    public PhaseFieldEmitterPart(final IPartItem<?> partItem) {
        super(partItem);
        this.getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL);
    }

    @Override
    public void getBoxes(final IPartCollisionHelper collisionHelper) {
        // Same behavior here as in AnnihilationPlanePart
        if (collisionHelper.isBBCollision()) {
            collisionHelper.addBox(0, 0, 14, 16, 16, 15.5);
            return;
        }
        connectionHelper.getBoxes(collisionHelper);
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

    @Override
    public IModelData getModelData() {
        return new PlaneModelData(connectionHelper.getConnections());
    }

    @Override
    public void onNeighborChanged(final BlockGetter level, final BlockPos pos, final BlockPos neighbor) {
        connectionHelper.updateConnections();
    }

    @Override
    public float getCableConnectionLength(final AECableType cable) {
        return 1;
    }
}
