package de.raidcraft.skillsandeffects.pvp.effects.debuff;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.types.ExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.trigger.AttackTrigger;
import de.raidcraft.skills.trigger.PlayerCastSkillTrigger;
import de.raidcraft.skills.util.TimeUtil;
import de.raidcraft.skillsandeffects.pvp.skills.debuff.Curse;
import de.raidcraft.util.MathUtil;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Curse",
        description = "Verflucht das Ziel und verursacht einen Debuff",
        types = {EffectType.DEBUFF, EffectType.MAGICAL, EffectType.HARMFUL, EffectType.PURGEABLE}
)
public class CurseEffect extends ExpirableEffect<Curse> implements Triggered {

    private final PotionEffect blind;

    public CurseEffect(Curse source, CharacterTemplate target, EffectData data) {

        super(source, target, data);

        this.blind = new PotionEffect(PotionEffectType.BLINDNESS, (int) getDuration(), 1, false);
    }

    @TriggerHandler(ignoreCancelled = true)
    public void onAttack(AttackTrigger trigger) {

        if (getSource().getType() != Curse.Type.WEAKNESS) {
            return;
        }
        int damage = trigger.getAttack().getDamage();
        double reduction = getSource().getWeakness();
        int reducedDamage = (int) (damage * reduction);
        combatLog("Angriffs Schaden um " + reducedDamage + "(" + MathUtil.toPercent(reduction) + "%) verringert.");
        trigger.getAttack().setDamage(damage - reducedDamage);
    }

    @TriggerHandler(ignoreCancelled = true)
    public void onSpellCast(PlayerCastSkillTrigger trigger) {

        if (getSource().getType() != Curse.Type.CASTTIME) {
            return;
        }
        int castTime = trigger.getAction().getCastTime();
        double modifier = getSource().getCastTime();
        int newCastTime = (int) (castTime * modifier);
        combatLog("Zauberzeit um " + TimeUtil.ticksToSeconds(newCastTime - castTime)
                + "s (" + MathUtil.toPercent(modifier) + "%) erhöht.");
        trigger.getAction().setCastTime(newCastTime);
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

        renew(target);
    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

        target.getEntity().removePotionEffect(PotionEffectType.BLINDNESS);
    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {

        switch (getSource().getType()) {

            case BLINDNESS:
                target.getEntity().addPotionEffect(blind);
                break;
        }
    }
}
