package de.raidcraft.skillsandeffects.pvp.effects.buffs.healing;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectElement;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.action.HealAction;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.types.PeriodicExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skillsandeffects.pvp.skills.healing.HealOverTime;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Heal over Time",
        description = "Heilt das Ziel über die Zeit verteilt.",
        types = {EffectType.MAGICAL, EffectType.BUFF, EffectType.PURGEABLE, EffectType.MAGICAL, EffectType.HEALING, EffectType.HELPFUL},
        elements = {EffectElement.HOLY}
)
public class HealOverTimeEffect extends PeriodicExpirableEffect<HealOverTime> {

    public HealOverTimeEffect(HealOverTime source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @Override
    protected void tick(CharacterTemplate target) throws CombatException {

        new HealAction<>(this, target, getDamage()).run();
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

        combatLog(getFriendlyName() + " erhalten.");
    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {

        combatLog(getFriendlyName() + " wurde erneuert.");
    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

        combatLog(getFriendlyName() + " wurde entfernt.");
    }
}
