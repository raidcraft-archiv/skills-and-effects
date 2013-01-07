package de.raidcraft.skillsandeffects.effects.debuff;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectElement;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.ExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.trigger.DamageTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.skillsandeffects.skills.holy.Purification;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Purification",
        description = "Heilt die Quelle des Effects um einen Prozentsatz des erlittenen Schadens.",
        types = {EffectType.MAGICAL, EffectType.HARMFUL, EffectType.DEBUFF},
        elements = {EffectElement.LIGHT}
)
public class PurificationEffect extends ExpirableEffect<Purification> implements Triggered {

    private ConfigurationSection healPercent;

    public PurificationEffect(Purification source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @Override
    public void load(ConfigurationSection data) {

        healPercent = data.getConfigurationSection("heal-percent");
    }

    private double getHealingPercentage() {

        return ConfigUtil.getTotalValue(getSource(), healPercent);
    }

    @TriggerHandler
    public void onDamage(DamageTrigger trigger) {

        int healAmount = (int) (trigger.getAttack().getDamage() * getHealingPercentage());
        getSource().getHero().getGroup().heal(healAmount);
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {

    }
}
