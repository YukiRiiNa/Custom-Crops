package net.momirealms.customcrops.api.core.block;

import net.momirealms.customcrops.api.action.Action;
import net.momirealms.customcrops.api.core.ExistenceForm;
import net.momirealms.customcrops.api.core.world.CustomCropsBlockState;
import net.momirealms.customcrops.api.requirement.Requirement;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class CropStageConfigImpl implements CropStageConfig {

    private final CropConfig crop;
    private final ExistenceForm existenceForm;
    private final double offset;
    private final String stageID;
    private final int point;
    private final Requirement<Player>[] interactRequirements;
    private final Requirement<Player>[] breakRequirements;
    private final Action<Player>[] interactActions;
    private final Action<Player>[] breakActions;
    private final Action<CustomCropsBlockState>[] growActions;

    public CropStageConfigImpl(
            CropConfig crop,
            ExistenceForm existenceForm,
            double offset,
            String stageID,
            int point,
            Requirement<Player>[] interactRequirements,
            Requirement<Player>[] breakRequirements,
            Action<Player>[] interactActions,
            Action<Player>[] breakActions,
            Action<CustomCropsBlockState>[] growActions
    ) {
        this.crop = crop;
        this.existenceForm = existenceForm;
        this.offset = offset;
        this.stageID = stageID;
        this.point = point;
        this.interactRequirements = interactRequirements;
        this.breakRequirements = breakRequirements;
        this.interactActions = interactActions;
        this.breakActions = breakActions;
        this.growActions = growActions;
    }

    @Override
    public CropConfig crop() {
        return crop;
    }

    @Override
    public double displayInfoOffset() {
        return offset;
    }

    @Nullable
    @Override
    public String stageID() {
        return stageID;
    }

    @Override
    public int point() {
        return point;
    }

    @Override
    public Requirement<Player>[] interactRequirements() {
        return interactRequirements;
    }

    @Override
    public Requirement<Player>[] breakRequirements() {
        return breakRequirements;
    }

    @Override
    public Action<Player>[] interactActions() {
        return interactActions;
    }

    @Override
    public Action<Player>[] breakActions() {
        return breakActions;
    }

    @Override
    public Action<CustomCropsBlockState>[] growActions() {
        return growActions;
    }

    @Override
    public ExistenceForm existenceForm() {
        return existenceForm;
    }

    public static class BuilderImpl implements Builder {

        private CropConfig crop;
        private ExistenceForm existenceForm;
        private double offset;
        private String stageID;
        private int point;
        private Requirement<Player>[] interactRequirements;
        private Requirement<Player>[] breakRequirements;
        private Action<Player>[] interactActions;
        private Action<Player>[] breakActions;
        private Action<CustomCropsBlockState>[] growActions;

        @Override
        public CropStageConfig build() {
            return new CropStageConfigImpl(crop, existenceForm, offset, stageID, point, interactRequirements, breakRequirements, interactActions, breakActions, growActions);
        }

        @Override
        public Builder crop(CropConfig crop) {
            this.crop = crop;
            return this;
        }

        @Override
        public Builder displayInfoOffset(double offset) {
            this.offset = offset;
            return this;
        }

        @Override
        public Builder stageID(String id) {
            this.stageID = id;
            return this;
        }

        @Override
        public Builder point(int i) {
            this.point = i;
            return this;
        }

        @Override
        public Builder interactRequirements(Requirement<Player>[] requirements) {
            this.interactRequirements = requirements;
            return this;
        }

        @Override
        public Builder breakRequirements(Requirement<Player>[] requirements) {
            this.breakRequirements = requirements;
            return this;
        }

        @Override
        public Builder interactActions(Action<Player>[] actions) {
            this.interactActions = actions;
            return this;
        }

        @Override
        public Builder breakActions(Action<Player>[] actions) {
            this.breakActions = actions;
            return this;
        }

        @Override
        public Builder growActions(Action<CustomCropsBlockState>[] actions) {
            this.growActions = actions;
            return this;
        }

        @Override
        public Builder existenceForm(ExistenceForm existenceForm) {
            this.existenceForm = existenceForm;
            return this;
        }
    }
}