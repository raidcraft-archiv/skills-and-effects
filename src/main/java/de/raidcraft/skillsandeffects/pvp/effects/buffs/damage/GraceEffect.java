package de.raidcraft.skillsandeffects.pvp.effects.buffs.damage;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectElement;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.Stackable;
import de.raidcraft.skills.api.effect.types.ExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skillsandeffects.pvp.skills.passive.FlamingRage;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Grace",
        description = "Verringert die Zauberzeit des nächsten Heilzaubers.",
        types = {EffectType.PURGEABLE, EffectType.MAGICAL, EffectType.HELPFUL},
        elements = {EffectElement.HOLY}
)
public class GraceEffect extends ExpirableEffect<FlamingRage> implements Stackable {

    public GraceEffect(FlamingRage source, CharacterTemplate target, EffectData data) {

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

        setStacks(0);
    }
}
