package de.raidcraft.skillsandeffects.pvp.effects.misc;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.types.ExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skillsandeffects.pvp.skills.buffs.HolyShield;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Weaken Soul",
        description = "Verhindert weitere Schild Effekte auf dem Ziel.",
        types = {EffectType.DEBUFF, EffectType.MAGICAL}
)
public class WeakenSoul extends ExpirableEffect<HolyShield> {

    public WeakenSoul(HolyShield source, CharacterTemplate target, EffectData data) {

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
