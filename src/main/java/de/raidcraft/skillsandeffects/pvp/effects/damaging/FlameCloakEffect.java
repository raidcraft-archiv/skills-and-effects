package de.raidcraft.skillsandeffects.pvp.effects.damaging;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectElement;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.types.PeriodicEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skillsandeffects.pvp.skills.protection.FlameCloak;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Flame Cloak",
        description = "Fügt Gegnern in der Nähe Schaden zu.",
        types = {EffectType.DAMAGING, EffectType.BUFF, EffectType.MAGICAL},
        elements = {EffectElement.FIRE}
)
public class FlameCloakEffect extends PeriodicEffect<FlameCloak> {

    public FlameCloakEffect(FlameCloak source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @Override
    protected void tick(CharacterTemplate t) throws CombatException {

        for (CharacterTemplate target : getSource().getNearbyTargets(false)) {
            getSource().magicalAttack(target, getDamage());
        }
        getSource().substractResourceTick();
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {


    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {


    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {


    }
}
