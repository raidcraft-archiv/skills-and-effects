package de.raidcraft.skillsandeffects.pvp.effects.misc;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.types.ExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skillsandeffects.pvp.skills.physical.DeathStrike;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Death Strike",
        description = "When active heals the attacker",
        types = {EffectType.BUFF, EffectType.HELPFUL}
)
public class DeathStrikeEffect extends ExpirableEffect<DeathStrike> {

    public DeathStrikeEffect(DeathStrike source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
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
