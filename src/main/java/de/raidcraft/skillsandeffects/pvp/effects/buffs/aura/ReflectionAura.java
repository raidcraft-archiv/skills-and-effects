package de.raidcraft.skillsandeffects.pvp.effects.buffs.aura;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.action.MagicalAttack;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.trigger.DamageTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.skillsandeffects.pvp.skills.buffs.Aura;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Reflection Aura",
        description = "Reflektiert Schaden zurück an die Gegner.",
        types = {EffectType.AURA, EffectType.BUFF, EffectType.HARMFUL, EffectType.MAGICAL},
        priority = 1.0
)
public class ReflectionAura extends AbstractAura implements Triggered {

    private ConfigurationSection reflection;

    public ReflectionAura(Aura source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @Override
    public void load(ConfigurationSection data) {

        reflection = data.getConfigurationSection("reflection");
    }

    private double getReflectionAmount() {

        return ConfigUtil.getTotalValue(getSource(), reflection);
    }

    @TriggerHandler
    public void onDamage(DamageTrigger trigger) throws CombatException {

        if (trigger.getAttack().getSource() instanceof CharacterTemplate) {
            int reflectedDamage = (int) (trigger.getAttack().getDamage() * getReflectionAmount());
            new MagicalAttack(getSource().getHolder(), (CharacterTemplate) trigger.getAttack().getSource(), reflectedDamage).run();
            trigger.getAttack().combatLog(this, reflectedDamage + " Schaden zurückgeworfen.");
        }
    }
}
