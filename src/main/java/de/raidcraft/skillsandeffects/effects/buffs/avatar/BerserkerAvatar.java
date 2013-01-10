package de.raidcraft.skillsandeffects.effects.buffs.avatar;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skillsandeffects.skills.buffs.Avatar;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Berserker Avatar",
        description = "Verwandelt dich in einen Berserker mit erhöhtem Schadensoutput.",
        types = {EffectType.AVATAR, EffectType.BUFF, EffectType.HELPFUL}
)
public class BerserkerAvatar extends AbstractAvatar {

    public BerserkerAvatar(Avatar source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {
        //TODO: implement
    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {
        //TODO: implement
    }
}
