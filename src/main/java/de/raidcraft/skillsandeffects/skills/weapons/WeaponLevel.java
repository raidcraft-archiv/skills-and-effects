package de.raidcraft.skillsandeffects.skills.weapons;

import de.raidcraft.skills.api.level.AbstractLevel;
import de.raidcraft.skills.api.persistance.LevelData;
import de.raidcraft.skills.api.skill.LevelableSkill;

/**
 * @author Silthus
 */
public class WeaponLevel extends AbstractLevel<LevelableSkill> {

    private static final int EXP_BASE_COST = 100;
    private static final int EXP_INCREASE_PER_LEVEL = 50;

    public WeaponLevel(LevelableSkill levelObject, LevelData data) {

        super(levelObject, data);
    }

    @Override
    public int getNeededExpForLevel(int level) {

        return EXP_BASE_COST + level * EXP_INCREASE_PER_LEVEL;
    }
}
