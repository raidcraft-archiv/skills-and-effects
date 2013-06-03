package de.raidcraft.skillsandeffects.pvp.skills.passive;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectElement;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.action.HealAction;
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
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.util.LocationUtil;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Impact Heal",
        description = "Heilt das freundliche Ziel welches am nächsten der Schadensquelle steht.",
        types = {EffectType.MAGICAL, EffectType.BUFF, EffectType.HEALING}
)
public class ImpactHeal extends AbstractSkill implements Triggered {

    private EffectElement element;
    private ConfigurationSection healPercentage;
    private ConfigurationSection selfHealPercentage;

    public ImpactHeal(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        element = EffectElement.fromString(data.getString("element"));
        healPercentage = data.getConfigurationSection("heal-percentage");
        selfHealPercentage = data.getConfigurationSection("self-heal-percentage");
    }

    public double getHealPercentage() {

        return ConfigUtil.getTotalValue(this, healPercentage);
    }

    public double getSelfHealPercentage() {

        return ConfigUtil.getTotalValue(this, selfHealPercentage);
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.MONITOR)
    public void onAttack(AttackTrigger trigger) {

        if (!trigger.getAttack().isOfAttackElement(element)) {
            return;
        }
        try {
            List<CharacterTemplate> targets = trigger.getAttack().getTarget().getNearbyTargets(getTotalRange(), true);
            if (targets.isEmpty()) {
                return;
            }
            CharacterTemplate characterTemplate = null;
            if (targets.size() == 1) {
                characterTemplate = targets.get(0);
            } else {
                Location location = trigger.getAttack().getTarget().getEntity().getLocation();
                int range = getTotalRange();
                for (CharacterTemplate character : targets) {
                    if (LocationUtil.getBlockDistance(location, character.getEntity().getLocation()) < range) {
                        characterTemplate = character;
                    }
                }
            }
            if (characterTemplate == null) {
                return;
            }

            int healAmount;
            if (characterTemplate.equals(getHolder())) {
                healAmount = (int) (getSelfHealPercentage() * trigger.getAttack().getDamage());
            } else {
                healAmount = (int) (getHealPercentage() * trigger.getAttack().getDamage());
            }
            new HealAction<>(this, characterTemplate, healAmount).run();
        } catch (CombatException ignored) {

        }
    }
}
