package de.raidcraft.skillsandeffects.pvp.effects.buffs.generic;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.effect.ExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.skillsandeffects.pvp.skills.buffs.GenericBuff;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
public class HealthBuff extends ExpirableEffect<GenericBuff> {

    private int oldMaxHealth;
    private double modifier;

    public HealthBuff(GenericBuff source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @Override
    public void load(ConfigurationSection data) {

        modifier = ConfigUtil.getTotalValue(getSource(), data.getConfigurationSection("health"));
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

        oldMaxHealth = target.getMaxHealth();
        target.setMaxHealth((int) (oldMaxHealth + oldMaxHealth * modifier));
    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

        target.setMaxHealth(oldMaxHealth);
    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {


    }
}
