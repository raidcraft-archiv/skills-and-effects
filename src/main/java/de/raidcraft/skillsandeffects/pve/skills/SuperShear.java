package de.raidcraft.skillsandeffects.pve.skills;

import de.raidcraft.RaidCraft;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.action.SkillAction;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractLevelableSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.PlayerShearTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Sheep;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Super Shear",
        description = "Das scheeren von Schafen gibt dem Spieler mehr Wolle.",
        types = {EffectType.HELPFUL}
)
public class SuperShear extends AbstractLevelableSkill implements Triggered {

    // maps the amount of wool to drop the their respective chance
    private SortedMap<Integer, ConfigurationSection> chances = new TreeMap<>();

    public SuperShear(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

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
    public void onInteract(PlayerShearTrigger trigger) {

        if (!canUseAbility()) {
            return;
        }
        Entity entity = trigger.getEvent().getEntity();
        if (!(entity instanceof Sheep)) {
            return;
        }
        int amount = 0;
        for (SortedMap.Entry<Integer, ConfigurationSection> entry : chances.entrySet()) {
            if (Math.random() < ConfigUtil.getTotalValue(this, entry.getValue()) && amount < entry.getKey()) {
                amount = entry.getKey();
            }
        }
        if (amount > 0) {
            entity.getLocation().getWorld().dropItemNaturally(
                    entity.getLocation(), new ItemStack(Material.WOOL, amount, ((Sheep) entity).getColor().getWoolData()));
            getAttachedLevel().addExp(getUseExp() * amount);
            substractUsageCost(new SkillAction(this));
        }
    }
}
