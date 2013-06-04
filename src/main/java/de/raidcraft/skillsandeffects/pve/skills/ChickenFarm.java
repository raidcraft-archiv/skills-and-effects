package de.raidcraft.skillsandeffects.pve.skills;

import de.raidcraft.RaidCraft;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractLevelableSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.PlayerEggThrowTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Chicken Farm",
        description = "Es besteht die Möglichkeit beim schlüpfen von Hühnern extra Babies zu spawnen.",
        types = {EffectType.HELPFUL}
)
public class ChickenFarm extends AbstractLevelableSkill implements Triggered {

    // maps the amount of chickens to spawn the their respective chance
    private SortedMap<Integer, ConfigurationSection> chances = new TreeMap<>();

    public ChickenFarm(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        for (String key : data.getKeys(false)) {
            try {
                int amount = Integer.parseInt(key);
                chances.put(amount, data.getConfigurationSection(key));
            } catch (NumberFormatException e) {
                RaidCraft.LOGGER.warning("Amount must be a number in " + getName());
            }
        }
        chances = Collections.unmodifiableSortedMap(chances);
    }

    @TriggerHandler(ignoreCancelled = true)
    public void onInteract(PlayerEggThrowTrigger trigger) {

        int amount = trigger.getEvent().getNumHatches();
        for (SortedMap.Entry<Integer, ConfigurationSection> entry : chances.entrySet()) {
            if (Math.random() < ConfigUtil.getTotalValue(this, entry.getValue()) && amount < entry.getKey()) {
                amount = entry.getKey();
            }
        }
        if (amount > 0) {
            trigger.getEvent().setHatching(true);
            trigger.getEvent().setNumHatches((byte) amount);
        }
    }
}
