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
import de.raidcraft.skills.skills.ConfigurableSkillLevel;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.BrewTrigger;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;

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

    private Map<String, Object> potionExp;
    private Map<String, Object> potionLevel;

    public Brewing(Hero hero, SkillProperties skillData, Profession profession, THeroSkill database) {

        super(hero, skillData, profession, database);
        attachLevel(new ConfigurableSkillLevel(this, database, skillData.getData()));
    }

    @Override
    public void load(ConfigurationSection data) {
        this.potionExp = data.getConfigurationSection("exp").getValues(false);
        this.potionLevel = data.getConfigurationSection("level").getValues(false);
    }

    @TriggerHandler(checkUsage = false)
    public void onBrew(BrewTrigger trigger) {

        BrewEvent event = trigger.getEvent();

        for(int i = 0; i < 3; i++) {
            ItemStack itemStack = event.getContents().getItem(i);

            if(itemStack == null) {
                continue;
            }
            if(itemStack.getType() == Material.POTION) {
                Potion potion = Potion.fromItemStack(itemStack);

                try {
                    // check if player can brew potion with current level
                    int level = (Integer) potionLevel.get(String.valueOf(potion.getNameId()));
                    if(level > getLevel().getLevel()) {
                        event.setCancelled(true);
                        if(getHero().getPlayer().isOnline()) {
                            getHero().getPlayer().sendMessage(ChatColor.RED + "Du kannst " + potion.getType().name() + " Tränke erst mit " +
                                    "Level " + level + " brauen!");
                        }
                        return;
                    }

                    // give exp
                    int exp = (Integer) potionExp.get(String.valueOf(potion.getNameId()));
                    getLevel().addExp(exp);

                } catch (Exception ignored) {
                }
            }
        }
    }

    @Override
    public void apply() {
        RaidCraft.getPermissions().playerAdd(getHero().getPlayer(), "antiguest.preventions.brew");
    }

    @Override
    public void remove() {
        // nothing to do
    }
}
