package de.raidcraft.skillsandeffects.pvp.skills.passive;

import de.raidcraft.skills.api.combat.EffectElement;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractLevelableSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.PlayerCastSkillTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.skillsandeffects.pvp.effects.buffs.damage.FlamingRageEffect;
import de.raidcraft.skillsandeffects.pvp.skills.magical.Fireball;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Flaming Rage",
        description = "Verringert die Zauberzeit des nächsten Feuerzaubers nach einem Feuerball.",
        types = {EffectType.MAGICAL, EffectType.BUFF, EffectType.HELPFUL},
        elements = {EffectElement.FIRE}
)
public class FlamingRage extends AbstractLevelableSkill implements Triggered {

    private double castDecreasePerStack = 0.20;

    public FlamingRage(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        castDecreasePerStack = ConfigUtil.getTotalValue(this, data.getConfigurationSection("casttime-bonus-per-stack"));
    }

    @TriggerHandler(ignoreCancelled = true)
    public void onSkillCast(PlayerCastSkillTrigger trigger) throws CombatException {

        if (trigger.getSkill().getClass() == Fireball.class) {
            addEffect(getHolder(), FlamingRageEffect.class);
        } else if (trigger.getSkill().isOfElement(EffectElement.FIRE) && getHolder().hasEffect(FlamingRageEffect.class)) {
            int stacks = getHolder().getEffect(FlamingRageEffect.class).getStacks();
            trigger.getAction().setCastTime((int) (trigger.getAction().getCastTime() - trigger.getAction().getCastTime() * castDecreasePerStack * stacks));
            getHolder().removeEffect(FlamingRageEffect.class);
        }
    }
}
