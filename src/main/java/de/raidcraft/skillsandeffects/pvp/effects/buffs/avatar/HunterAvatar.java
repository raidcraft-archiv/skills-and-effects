package de.raidcraft.skillsandeffects.pvp.effects.buffs.avatar;

import de.raidcraft.RaidCraft;
import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.action.Attack;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.exceptions.UnknownSkillException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.skill.Skill;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.TriggerPriority;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.trigger.AttackTrigger;
import de.raidcraft.skills.trigger.PlayerCastSkillTrigger;
import de.raidcraft.skills.trigger.ResourceChangeTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.skillsandeffects.pvp.skills.buffs.Avatar;
import de.raidcraft.util.MathUtil;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Hunter Avatar",
        description = "Verwandelt dich in einen Berserker mit erhöhtem Schadensoutput.",
        types = {EffectType.AVATAR, EffectType.BUFF, EffectType.HELPFUL}
)
public class HunterAvatar extends AbstractAvatar implements Triggered {

    private boolean noResourceCost;
    private double attackIncrease = 0.20;
    private double cooldownDecrease = 0.90;
    private Set<Skill> cooldownDecreasedSkills = new HashSet<>();

    public HunterAvatar(Avatar source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @Override
    public void load(ConfigurationSection data) {

        noResourceCost = data.getBoolean("no-resource-cost", true);
        attackIncrease = ConfigUtil.getTotalValue(getSource(), data.getConfigurationSection("attack-increase"));
        cooldownDecrease = ConfigUtil.getTotalValue(getSource(), data.getConfigurationSection("cooldown-decrease"));
        for (String str : data.getStringList("cooldown-decreased-skills")) {
            try {
                Skill skill = getSource().getHolder().getSkill(str);
                cooldownDecreasedSkills.add(skill);
            } catch (UnknownSkillException e) {
                RaidCraft.LOGGER.warning(e.getMessage());
            }
        }
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {


    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {


    }

    @TriggerHandler(ignoreCancelled = true)
    public void onResourceChange(ResourceChangeTrigger trigger) {

        if (!noResourceCost || trigger.getAction() != ResourceChangeTrigger.Action.LOSS) {
            return;
        }
        // dont change the resource
        trigger.setNewValue(trigger.getResource().getCurrent());
    }

    @TriggerHandler(ignoreCancelled = true)
    public void onSkillCastTrigger(PlayerCastSkillTrigger trigger) {

        if (!cooldownDecreasedSkills.contains(trigger.getSkill())) {
            return;
        }
        double cooldown = trigger.getAction().getCooldown();
        double newCooldown = cooldown - cooldown * cooldownDecrease;
        combatLog("Cooldown von " + trigger.getSkill().getFriendlyName() + " um "
                + (cooldown - newCooldown) + "s(" + MathUtil.toPercent(cooldownDecrease) + ") verringert.");
        trigger.getAction().setCooldown(newCooldown);
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.HIGHEST)
    public void onAttack(AttackTrigger trigger) throws CombatException {

        Attack<?,CharacterTemplate> attack = trigger.getAttack();
        if (!attack.isOfAttackType(EffectType.PHYSICAL)
                || !attack.isOfAttackType(EffectType.RANGE)) {
            return;
        }
        int oldDamage = attack.getDamage();
        int newDamage = (int) (oldDamage + oldDamage * attackIncrease);
        combatLog("Schaden um " + (newDamage - oldDamage) + "(" + MathUtil.toPercent(attackIncrease) + ") erhöht.");
        attack.setDamage(newDamage);
    }
}
