package de.raidcraft.skillsandeffects.pvp.effects.buffs.aura;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.effect.AbstractEffect;
import de.raidcraft.skills.api.effect.IgnoredEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skillsandeffects.pvp.skills.buffs.Aura;

/**
 * @author Silthus
 */
@IgnoredEffect
public abstract class AbstractAura extends AbstractEffect<Aura> {

    public AbstractAura(Aura source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

        info("Die Aura " + getFriendlyName() + " ist nun aktiv.");
    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {

        info("Die Aura " + getFriendlyName() + " wurde erneuert.");
    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

        info("Die Aura " + getFriendlyName() + " ist nicht mehr aktiv.");
    }
}
