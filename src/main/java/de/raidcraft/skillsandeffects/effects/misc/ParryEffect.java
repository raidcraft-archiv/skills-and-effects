package de.raidcraft.skillsandeffects.effects.misc;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.ExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skillsandeffects.skills.armor.Parry;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Parry",
        description = "Es kann für einige Zeit nicht parriert werden.",
        priority = -1.0
)
public class ParryEffect extends ExpirableEffect<Parry> {

    public ParryEffect(Parry source, CharacterTemplate target, EffectData data) {

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
