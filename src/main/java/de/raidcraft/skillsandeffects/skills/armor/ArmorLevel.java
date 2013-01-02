package de.raidcraft.skillsandeffects.skills.armor;

import de.raidcraft.skills.api.level.AbstractLevel;
import de.raidcraft.skills.api.persistance.LevelData;
import de.raidcraft.skills.api.skill.LevelableSkill;

/**
 * @author Silthus
 */
public class ArmorLevel extends AbstractLevel<LevelableSkill> {

    private static final int EXP_BASE_COST = 50;
    private static final int EXP_INCREASE_PER_LEVEL = 25;

    public ArmorLevel(Armor levelObject, LevelData data) {

        super(levelObject, data);
    }

    @Override
    public int getNeededExpForLevel(int level) {

        return EXP_BASE_COST + level * EXP_INCREASE_PER_LEVEL;
    }
}
