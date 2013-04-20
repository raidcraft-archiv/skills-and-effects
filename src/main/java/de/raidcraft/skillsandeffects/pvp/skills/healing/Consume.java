package de.raidcraft.skillsandeffects.pvp.skills.healing;

import de.raidcraft.RaidCraft;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.resource.Resource;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.TriggerPriority;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.PlayerConsumeTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.skillsandeffects.pvp.effects.buffs.healing.ConsumeEffect;
import de.raidcraft.util.ItemUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Consume",
        description = "Ermöglicht es durch Essen und Trinken Leben und Mana zu regenrieren.",
        types = {EffectType.HELPFUL, EffectType.BUFF, EffectType.HEALING}
)
public class Consume extends AbstractSkill implements Triggered {

    private final Map<Integer, Consumeable> consumeables = new HashMap<>();

    public Consume(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        loadConsumables(data.getConfigurationSection("food"));
        loadConsumables(data.getConfigurationSection("drinks"));
    }

    private void loadConsumables(ConfigurationSection section) {

        if (section != null) {
            for (String key : section.getKeys(false)) {
                Material item = ItemUtils.getItem(key);
                if (item != null) {
                    Consumeable consumeable = new Consumeable(item.getId(), section.getConfigurationSection(key));
                    consumeables.put(consumeable.itemId, consumeable);
                } else {
                    RaidCraft.LOGGER.warning("Wrong item name " + key + " in " + getName() + ".yml config.");
                }
            }
        }
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.MONITOR)
    public void onItemConsume(PlayerConsumeTrigger trigger) throws CombatException {

        if (getHero().isInCombat()) {
            trigger.getEvent().setItem(null);
            throw new CombatException("Du kannst im Kampf kein Essen zu dir nehmen.");
        }
        ItemStack item = trigger.getEvent().getItem();
        if (item != null && consumeables.containsKey(item.getTypeId())) {
            consumeables.get(item.getTypeId()).consume(item);
        }
    }

    public class Consumeable {

        private final int itemId;
        private final ConsumeableType type;
        private final String resourceName;
        private final ConfigurationSection resourceGain;
        private final boolean percentage;

        public Consumeable(int itemId, ConfigurationSection config) {

            this.itemId = itemId;
            this.resourceName = config.getString("resource", "health");
            this.resourceGain = config.getConfigurationSection("gain");
            this.type = resourceName.equalsIgnoreCase("health") ? ConsumeableType.HEALTH : ConsumeableType.RESOURCE;
            this.percentage = config.getBoolean("percentage", true);
        }

        public void consume(ItemStack itemStack) throws CombatException {

            if (this.itemId != itemStack.getTypeId()) {
                return;
            }
            if (type != ConsumeableType.HEALTH && getResource() == null) {
                throw new CombatException("Dir bringt der Verzehr dieses Essens keine Regeneration.");
            }
            Consume.this.addEffect(getHero(), ConsumeEffect.class).setConsumeable(this);
            if (itemStack.getAmount() > 1) {
                itemStack.setAmount(itemStack.getAmount() - 1);
            } else {
                getHero().getPlayer().getInventory().remove(itemStack);
            }
        }

        public Resource getResource() {

            return getHero().getResource(resourceName);
        }

        public ConsumeableType getType() {

            return type;
        }

        public double getResourceGain() {

            return ConfigUtil.getTotalValue(Consume.this, resourceGain);
        }

        public boolean isPercentage() {

            return percentage;
        }
    }

    public enum ConsumeableType {

        HEALTH,
        RESOURCE
    }
}
