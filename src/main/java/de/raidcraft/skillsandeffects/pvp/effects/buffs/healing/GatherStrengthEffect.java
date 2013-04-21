package de.raidcraft.skillsandeffects.pvp.effects.buffs.healing;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.types.PeriodicExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.skillsandeffects.pvp.skills.protection.GatherStrength;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Gather Strength",
        description = "Stellt Leben über Zeit wieder her.",
        types = {EffectType.HEALING, EffectType.HELPFUL, EffectType.PROTECTION},
        priority = -1.0
)
public class GatherStrengthEffect extends PeriodicExpirableEffect<GatherStrength> {

    private double restorePerTick = 0.03;
    private double initialHeal = 0.0;

    public GatherStrengthEffect(GatherStrength source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @Override
    public void load(ConfigurationSection data) {

        restorePerTick = ConfigUtil.getTotalValue(getSource(), data.getConfigurationSection("heal-percent-per-tick"));
        initialHeal = ConfigUtil.getTotalValue(getSource(), data.getConfigurationSection("initial-heal-percent"));
    }

    @Override
    protected void tick(CharacterTemplate target) throws CombatException {

        target.heal((int) (target.getMaxHealth() * restorePerTick));
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

        if (initialHeal > 0.0) {
            target.heal((int) (target.getMaxHealth() * initialHeal));
        }
    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {

    }
}
