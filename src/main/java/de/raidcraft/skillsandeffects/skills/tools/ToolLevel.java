package de.raidcraft.skillsandeffects.skills.tools;

import de.raidcraft.skills.api.level.AbstractLevel;
import de.raidcraft.skills.api.persistance.LevelData;
import de.raidcraft.skills.api.skill.LevelableSkill;

/**
 * @author Philip
 */
public class ToolLevel extends AbstractLevel<LevelableSkill> {
    
    private final static int EXP_BASE_COST = 100;
    private final static int EXP_INCREASE_PER_LEVEL = 50;

    public ToolLevel(LevelableSkill levelObject, LevelData data) {
            super(levelObject, data);
    }

    @Override
    public int getNeededExpForLevel(int level) {
        return EXP_BASE_COST + level * EXP_INCREASE_PER_LEVEL;
    }
}
