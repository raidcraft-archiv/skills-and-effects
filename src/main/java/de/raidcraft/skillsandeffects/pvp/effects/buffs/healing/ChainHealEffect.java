package de.raidcraft.skillsandeffects.pvp.effects.buffs.healing;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.types.ExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skillsandeffects.pvp.skills.magical.ChainLightning;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Chain Heal",
        description = "Verursacht Blitzschaden und verhindert weiteren Schaden von Kettenblitzen.",
        types = {EffectType.DAMAGING, EffectType.MAGICAL}
)
public class ChainHealEffect extends ExpirableEffect<ChainLightning> {

    public ChainHealEffect(ChainLightning source, CharacterTemplate target, EffectData data) {

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
