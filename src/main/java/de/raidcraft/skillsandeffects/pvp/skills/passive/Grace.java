package de.raidcraft.skillsandeffects.pvp.skills.passive;

import de.raidcraft.skills.api.combat.EffectElement;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.TriggerPriority;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.AttackTrigger;
import de.raidcraft.skills.trigger.PlayerCastSkillTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.skillsandeffects.pvp.effects.buffs.damage.GraceEffect;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Grace",
        description = "Verringert die Zauberzeit des nächsten Heilzaubers nach einer heiligen Attacke",
        types = {EffectType.MAGICAL, EffectType.BUFF, EffectType.HELPFUL},
        elements = {EffectElement.HOLY}
)
public class Grace extends AbstractSkill implements Triggered {

    private double castDecreasePerStack = 0.20;

    public Grace(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        castDecreasePerStack = ConfigUtil.getTotalValue(this, data.getConfigurationSection("casttime-bonus-per-stack"));
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.MONITOR)
    public void onAttack(AttackTrigger trigger) throws CombatException {

        if (trigger.getAttack().isOfAttackElement(EffectElement.HOLY)
                && trigger.getAttack().isOfAttackType(EffectType.DAMAGING)) {
            addEffect(GraceEffect.class);
        }
    }

    @TriggerHandler(ignoreCancelled = true)
    public void onSkillCast(PlayerCastSkillTrigger trigger) throws CombatException {

        if (trigger.getSkill().isOfElement(EffectElement.HOLY)
                && hasEffect(GraceEffect.class)
                && trigger.getSkill().isOfType(EffectType.HEALING)) {
            int stacks = getEffect(GraceEffect.class).getStacks();
            trigger.getAction().setCastTime((int) (trigger.getAction().getCastTime() - castDecreasePerStack * stacks));
            removeEffect(GraceEffect.class);
        }
    }
}
