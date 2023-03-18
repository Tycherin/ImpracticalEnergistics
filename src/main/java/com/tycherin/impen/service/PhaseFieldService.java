package com.tycherin.impen.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.tycherin.impen.blockentity.PhaseFieldControllerBlockEntity;
import com.tycherin.impen.part.PhaseFieldEmitterPart;

import appeng.api.networking.GridServices;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridService;
import appeng.api.networking.IGridServiceProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class PhaseFieldService implements IGridService, IGridServiceProvider {

    // TODO Right now, all of this is global, but it should technically be broken out per level/dimension.

    /** This method should be called during mod initialization */
    public static void init() {
        GridServices.register(PhaseFieldService.class, PhaseFieldService.class);
    }

    private Optional<PhaseFieldControllerBlockEntity> mainController = Optional.empty();
    private final Set<PhaseFieldControllerBlockEntity> controllers = new HashSet<>();
    private final Set<PhaseFieldEmitterPart> emitters = new HashSet<>();

    @Override
    public void addNode(final IGridNode node) {
        if (node.getOwner() instanceof PhaseFieldControllerBlockEntity controller) {
            this.addController(controller);
        }
        else if (node.getOwner() instanceof PhaseFieldEmitterPart emitter) {
            this.addEmitter(emitter);
        }
    }

    private void addController(final PhaseFieldControllerBlockEntity controller) {
        log.info("Adding controller: {}", controller.getBlockPos());
        if (this.mainController.isEmpty()) {
            this.markAsMain(controller);
        }
        this.controllers.add(controller);
    }

    private void addEmitter(final PhaseFieldEmitterPart emitter) {
        log.info("Adding emitter: {}", emitter.getBlockEntity().getBlockPos());
        this.emitters.add(emitter);
        this.mainController.ifPresent(controller -> controller.setEmitters(this.emitters));
    }

    @Override
    public void removeNode(final IGridNode node) {
        if (node.getOwner() instanceof PhaseFieldControllerBlockEntity controller) {
            this.removeController(controller);
        }
        else if (node.getOwner() instanceof PhaseFieldEmitterPart emitter) {
            this.removeEmitter(emitter);
        }
    }

    private void removeController(final PhaseFieldControllerBlockEntity controller) {
        log.info("Removing controller: {}", controller.getBlockPos());
        this.controllers.remove(controller);

        // Special case for removing the current lead controller
        if (this.mainController.isPresent() && this.mainController.get().equals(controller)) {
            this.mainController = Optional.empty();
            // We've removed the current leader. Elect a new leader, and we don't really care which one.
            this.controllers.stream().findFirst().ifPresent(this::markAsMain);
        }
    }

    private void removeEmitter(final PhaseFieldEmitterPart emitter) {
        log.info("Removing emitter: {}", emitter.getBlockEntity().getBlockPos());
        this.emitters.remove(emitter);
        this.mainController.ifPresent(controller -> controller.setEmitters(this.emitters));
    }

    private void markAsMain(final PhaseFieldControllerBlockEntity controller) {
        this.mainController.ifPresent(oldController -> oldController.setEmitters(Collections.emptySet()));
        this.mainController = Optional.of(controller);
        controller.setEmitters(this.emitters);
    }
}
