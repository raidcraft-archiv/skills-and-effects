package de.raidcraft.skillsandeffects.skills.usage;

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
import de.raidcraft.skills.trigger.BrewTrigger;
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
        desc = "Reguliert das Brauen von Tränken",
        types = {EffectType.UNBINDABLE},
        triggerCombat = false
)
public class Brewing extends AbstractLevelableSkill implements Triggered {

    private Map<Material, IngredientSetting> knownIngredients = new HashMap<>();
    private double cleverBrewingChancePerLevel;
    private double maxCleverBrewingChance;

    public Brewing(Hero hero, SkillProperties skillData, Profession profession, THeroSkill database) {

        super(hero, skillData, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        this.cleverBrewingChancePerLevel = data.getDouble("clever-brewing-chance-per-level", 0.1);
        this.maxCleverBrewingChance = data.getDouble("max-clever-brewing-chance", 33);

        ConfigurationSection ingredients = data.getConfigurationSection("ingredients");
        if (ingredients == null) {
            return;
        }

        for(String key : ingredients.getKeys(false)) {
            Material material = ItemUtils.getItem(key);
            if(material == null) {
                RaidCraft.LOGGER.warning("Unknown material '" + key + "' in " + getClass().getSimpleName());
                continue;
            }
            ConfigurationSection blockSettings = ingredients.getConfigurationSection(key);
            int minLevel = blockSettings.getInt("min-level");
            int exp = blockSettings.getInt("exp");

            knownIngredients.put(material, new IngredientSetting(material, exp, minLevel));
        }
    }

    @TriggerHandler(checkUsage = false)
    public void onBrew(BrewTrigger trigger) {

        BrewEvent event = trigger.getEvent();

        ItemStack ingredient = event.getContents().getItem(3);

        if (ingredient == null) {
            return;
        }

        getHero().debug("Brewing ingredient: " + ingredient.getType().name());

        IngredientSetting ingredientSetting = knownIngredients.get(ingredient.getType());

        // unknown ingredient
        if(ingredientSetting == null) {
            return;
        }

        if (ingredientSetting.getMinLevel() > getLevel().getLevel()) {
            event.setCancelled(true);
            if (getHero().getPlayer().isOnline()) {
                getHero().getPlayer().sendMessage(ChatColor.RED + "Du kannst Tränke mit der Zutat " +
                        "'" + ItemUtils.getFriendlyName(ingredient.getType()) + "' " +
                        "erst mit Skill-Level " + ingredientSetting.getMinLevel() + " brauen!");
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), ingredient);
                event.getContents().setItem(3, new ItemStack(Material.AIR, 1));
                return;
            }
        }

        getLevel().addExp(ingredientSetting.getExp());
        getHero().debug("Brewing: Added " + ingredientSetting.getExp() + " EXP");

        // calculate clever brewing
        double chance = getLevel().getLevel() * cleverBrewingChancePerLevel;
        if(chance > maxCleverBrewingChance) {
            chance = maxCleverBrewingChance;
        }
        double random = Math.random() * 100.;
        if (chance > random) {
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
