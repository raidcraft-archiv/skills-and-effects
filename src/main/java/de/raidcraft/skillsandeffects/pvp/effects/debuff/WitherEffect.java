package de.raidcraft.skillsandeffects.pvp.effects.debuff;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectElement;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.action.EffectDamage;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.types.PeriodicExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.skill.Skill;
import de.raidcraft.skills.api.trigger.Triggered;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Wither",
        description = "Der standard Minecraft Wither Effekt.",
        types = {EffectType.MAGICAL, EffectType.DEBUFF, EffectType.HARMFUL, EffectType.DAMAGING, EffectType.PURGEABLE},
        elements = {EffectElement.DARK}
)
public class WitherEffect extends PeriodicExpirableEffect<Skill> implements Triggered {

    private PotionEffect witherEffect;

    public WitherEffect(Skill source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
        this.witherEffect = new PotionEffect(PotionEffectType.WITHER, (int) getDuration(), 0, false);
    }

    @Override
    protected void tick(CharacterTemplate target) throws CombatException {

        new EffectDamage(this, getDamage()).run();
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

        renew(target);
    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

        target.getEntity().removePotionEffect(PotionEffectType.WITHER);
    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {

        target.getEntity().addPotionEffect(witherEffect);
    }
}
