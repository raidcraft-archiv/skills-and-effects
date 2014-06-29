package de.raidcraft.skillsandeffects.pvp.effects.debuff;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.types.ExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skillsandeffects.pvp.skills.healing.Layhands;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Lay Hands",
        description = "Verhindert dass du in der nächsten Zeit von Handauflegen betroffen sein kannst.",
        types = {EffectType.DEBUFF}
)
public class LayhandsEffect extends ExpirableEffect<Layhands> {

    public LayhandsEffect(Layhands source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {

    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

    }
}
