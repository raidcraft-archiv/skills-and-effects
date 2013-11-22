package de.raidcraft.skillsandeffects.pvp.effects.buffs.generic;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.types.ExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.skill.Skill;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.util.MathUtil;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Health Buff",
        description = "Erhöht die maximale Lebensenergie.",
        types = {EffectType.HELPFUL, EffectType.MAGICAL, EffectType.BUFF, EffectType.PURGEABLE}
)
public class HealthBuff extends ExpirableEffect<Skill> {

    private int increasedHealth;
    private double modifier;

    public HealthBuff(Skill source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @Override
    public void load(ConfigurationSection data) {

        modifier = ConfigUtil.getTotalValue(getSource(), data.getConfigurationSection("health"));
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

        increasedHealth = (int) (target.getMaxHealth() * modifier);
        combatLog("Maximale Leben um " + increasedHealth + "(" + MathUtil.toPercent(modifier) + ") erhöht.");
        target.increaseMaxHealth(increasedHealth);
    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

        combatLog("Maximale Leben um " + increasedHealth + "(" + MathUtil.toPercent(modifier) + ") verringert.");
        target.decreaseMaxHealth(increasedHealth);
    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {


    }
}
