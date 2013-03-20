package de.raidcraft.skillsandeffects.pvp.effects.misc;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.ExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skillsandeffects.pvp.skills.summoning.SummonCreature;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Summoned",
        description = "Markiert beschwörte Kreaturen.",
        types = {EffectType.SUMMON, EffectType.MAGICAL}
)
public class Summoned extends ExpirableEffect<SummonCreature> {

    public Summoned(SummonCreature source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

        int health = getSource().getHealth();
        target.setMaxHealth(health);
        target.setHealth(health);
        target.setDamage(getSource().getDamage());
        target.getEntity().setCustomName(getSource().getHero().getName() + "'s Ergebener");
        target.getEntity().setCustomNameVisible(true);
        // this will make the creature friendly to the party of the summoner
        getSource().getHero().getParty().addMember(target);
    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

        target.getEntity().setCustomNameVisible(false);
        target.kill();
    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {

    }
}
