package de.raidcraft.skillsandeffects.effects.buffs;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.effect.ExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skillsandeffects.skills.buffs.Aura;

/**
 * @author Silthus
 */
public abstract class AbstractAura extends ExpirableEffect<Aura> {

    public AbstractAura(Aura source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

        info("Die Aura " + getFriendlyName() + " ist nun aktiv.");
    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

        info("Die Aura " + getFriendlyName() + " ist nicht mehr aktiv.");
    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {

        info("Die Aura " + getFriendlyName() + " wurde erneuert.");
    }
}
