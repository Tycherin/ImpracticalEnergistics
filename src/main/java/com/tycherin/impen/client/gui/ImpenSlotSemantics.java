package com.tycherin.impen.client.gui;

import appeng.menu.SlotSemantic;
import appeng.menu.SlotSemantics;

public class ImpenSlotSemantics {

    // Note that the semantic IDs that appear here must match 1:1 with what appears in the screen definition files

    /** Phase Field Controller capsule display slots */
    public static final SlotSemantic PFC_CAPSULE = SlotSemantics.register("impen::PFC_CAPSULE",
            false);

    /** Optional block display for SRM recipes */
    public static final SlotSemantic SRM_BLOCK = SlotSemantics.register("impen::SRM_BLOCK",
            false);
}
