package de.raidcraft.skillsandeffects.pvp.effects.buffs.generic;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.action.Attack;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.ExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.skill.Skill;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.trigger.AttackTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.skills.util.TimeUtil;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Damage Buff",
        description = "Erhöht den Schaden aller Angriffe.",
        types = {EffectType.BUFF, EffectType.PURGEABLE, EffectType.HELPFUL}
)
public class DamageBuff extends ExpirableEffect<Skill> implements Triggered {

    private boolean physical = true;
    private boolean magical = true;
    private boolean oneTime = false;
    private double increase;

    public DamageBuff(Skill source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @Override
    public void load(ConfigurationSection data) {

        physical = data.getBoolean("physical", true);
        magical = data.getBoolean("magical", true);
        oneTime = data.getBoolean("one-time", false);
        increase = ConfigUtil.getTotalValue(getSource(), data.getConfigurationSection("increase"));
    }

    @TriggerHandler(ignoreCancelled = true)
    public void onAttack(AttackTrigger trigger) throws CombatException {

        Attack<?,CharacterTemplate> attack = trigger.getAttack();
        if (!physical && attack.isOfAttackType(EffectType.PHYSICAL)) {
            return;
        }
        if (!magical && attack.isOfAttackType(EffectType.MAGICAL)) {
            return;
        }

        int oldDamage = attack.getDamage();
        int newDamage = (int) (oldDamage + oldDamage * increase);
        attack.setDamage(newDamage);

        attack.combatLog(getSource(), "Schaden um " + newDamage + "(" + (int) (increase * 100) + "%) erhöht.");

        if (oneTime) {
            remove();
        }
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

        if (oneTime) {
            info("Schaden des nächsten Angriffs um " + (int) (increase * 100) + "% erhöht.");
        } else {
            info("Schaden für " + TimeUtil.ticksToSeconds(getDuration()) + "s um " + (int) (increase * 100) + "% erhöht.");
        }
    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {

    }
}
