package de.raidcraft.skillsandeffects.effects.buffs.avatar;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.effect.ExpirableEffect;
import de.raidcraft.skills.api.effect.IgnoredEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skillsandeffects.skills.buffs.Avatar;

/**
 * @author Silthus
 */
@IgnoredEffect
public abstract class AbstractAvatar extends ExpirableEffect<Avatar> {

    public AbstractAvatar(Avatar source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {

    }
}
