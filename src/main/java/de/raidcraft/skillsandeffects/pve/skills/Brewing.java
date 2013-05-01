package de.raidcraft.skillsandeffects.pve.skills;

import de.raidcraft.RaidCraft;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractLevelableSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.BrewTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.util.ItemUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Philip
 */
@SkillInformation(
        name = "Brewing",
        description = "Reguliert das Brauen von Tränken",
        triggerCombat = false
)
public class Brewing extends AbstractLevelableSkill implements Triggered {

    private Map<Material, IngredientSetting> knownIngredients = new HashMap<>();
    private ConfigurationSection cleverBrewingChance;

    public Brewing(Hero hero, SkillProperties skillData, Profession profession, THeroSkill database) {

        super(hero, skillData, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        this.cleverBrewingChance = data.getConfigurationSection("clever-brewing-chance");

        ConfigurationSection ingredients = data.getConfigurationSection("ingredients");
        if (ingredients == null) {
            return;
        }

        for (String key : ingredients.getKeys(false)) {
            Material material = ItemUtils.getItem(key);
            if (material == null) {
                RaidCraft.LOGGER.warning("Unknown material '" + key + "' in " + getClass().getSimpleName());
                continue;
            }
            ConfigurationSection blockSettings = ingredients.getConfigurationSection(key);
            int level = blockSettings.getInt("level");
            int exp = blockSettings.getInt("exp");

            knownIngredients.put(material, new IngredientSetting(material, exp, level));
        }
    }

    public double getCleverBrewingChance() {

        return ConfigUtil.getTotalValue(this, cleverBrewingChance);
    }

    @TriggerHandler(ignoreCancelled = true)
    public void onBrew(BrewTrigger trigger) {

        BrewEvent event = trigger.getEvent();

        ItemStack ingredient = event.getContents().getItem(3);

        if (ingredient == null) {
            return;
        }

        getHolder().debug("Brewing ingredient: " + ingredient.getType().name());

        IngredientSetting ingredientSetting = knownIngredients.get(ingredient.getType());

        // unknown ingredient
        if(ingredientSetting == null) {
            return;
        }

        if (ingredientSetting.getMinLevel() > getAttachedLevel().getLevel()) {
            event.setCancelled(true);
            if (getHolder().getPlayer().isOnline()) {
                getHolder().getPlayer().sendMessage(ChatColor.RED + "Du kannst Tränke mit der Zutat " +
                        "'" + ItemUtils.getFriendlyName(ingredient.getType()) + "' " +
                        "erst mit Skill-Level " + ingredientSetting.getMinLevel() + " brauen!");
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), ingredient);
                event.getContents().setItem(3, new ItemStack(Material.AIR, 1));
                return;
            }
        }

        getAttachedLevel().addExp(ingredientSetting.getExp());
        getHolder().debug("Brewing: Added " + ingredientSetting.getExp() + " EXP");

        if (Math.random() < getCleverBrewingChance()) {
            ingredient.setAmount(ingredient.getAmount() + 1);
        }
    }

    public class IngredientSetting {

        private Material ingredient;
        private int exp;
        private int minLevel;

        public IngredientSetting(Material ingredient, int exp, int minLevel) {
            this.ingredient = ingredient;
            this.exp = exp;
            this.minLevel = minLevel;
        }

        public Material getIngredient() {
            return ingredient;
        }

        public int getExp() {
            return exp;
        }

        public int getMinLevel() {
            return minLevel;
        }
    }
}
