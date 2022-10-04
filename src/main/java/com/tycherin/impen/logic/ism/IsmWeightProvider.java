package com.tycherin.impen.logic.ism;

import java.util.Collection;

public interface IsmWeightProvider {

    /** @return an ID that is unique across an instance of the game */
    String getId();

    /** @return a collection of {@link IsmWeight}s to be added to any ISMs on this grid */
    Collection<IsmWeight> getWeights();

    /** @return true if this provider's weights have been updated; false otherwise */
    boolean needsUpdate();
    
    /** Called when the update has been successfully processed */
    void markUpdateSuccessful();
}
