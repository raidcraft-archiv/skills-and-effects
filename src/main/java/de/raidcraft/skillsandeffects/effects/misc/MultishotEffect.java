package de.raidcraft.skillsandeffects.effects.misc;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.ExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skillsandeffects.skills.bow.Multishot;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Multi-Shot",
        description = "Verschiesst mehrere Pfeile auf einmal.",
        types = {EffectType.PHYSICAL, EffectType.HARMFUL, EffectType.DAMAGING}
)
public class MultishotEffect extends ExpirableEffect<Multishot> {

    public MultishotEffect(Multishot source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

        info("Du legst mehrere Pfeile in deinen Bogen ein.");
    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {

    }
}
