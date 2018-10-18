package de.raidcraft.skillsandeffects.pvp.skills.healing;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.items.CustomItemStack;
import de.raidcraft.api.items.attachments.Consumeable;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.action.HealAction;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Attribute;
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
import de.raidcraft.skillsandeffects.pvp.effects.buffs.generic.AttributeBuff;
import de.raidcraft.skillsandeffects.pvp.effects.buffs.healing.ConsumeEffect;
import lombok.Data;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Consume",
        description = "Ermöglicht es durch Essen und Trinken Leben und Mana zu regenrieren.",
        types = {EffectType.HELPFUL, EffectType.BUFF, EffectType.HEALING}
)
public class Consume extends AbstractSkill implements Triggered {

    private int requiredFoodLevel = 19;

    public Consume(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        requiredFoodLevel = data.getInt("food-level", 19);
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.MONITOR)
    public void onItemConsume(PlayerConsumeTrigger trigger) throws CombatException {

        if (getHolder().getPlayer().getFoodLevel() < requiredFoodLevel) {
            // the player has to have full food level for the regen to kick in
            return;
        }
        if (getHolder().isInCombat()) {
            trigger.getEvent().setCancelled(true);
            throw new CombatException("Du kannst im Kampf kein Essen zu dir nehmen.");
        }
        CustomItemStack customItem = RaidCraft.getCustomItem(trigger.getEvent().getItem());
        if (customItem == null || !(customItem.getItem() instanceof Consumeable)) return;

        new ConsumeableWrapper((Consumeable) customItem.getItem()).consume(customItem);
    }

    @Data
    public class ConsumeableWrapper {

        private final Consumeable consumeable;

        public void consume(ItemStack itemStack) throws CombatException {

            CustomItemStack customItem = RaidCraft.getCustomItem(itemStack);
            if (customItem == null) return;

            if (getConsumeable().getConsumeableType() == Consumeable.Type.RESOURCE && getResource() == null) {
                throw new CombatException("Dir bringt der Verzehr dieses Gegenstands keine Regeneration.");
            }
            if (getConsumeable().getConsumeableType() == Consumeable.Type.ATTRIBUTE && getAttribute() == null) {
                throw new CombatException("Dir bringt der Verzehr dieses Gegenstands keine Attribut Steigerung.");
            }

            if (getConsumeable().isInstant()) {
                applyRessourceGain(getResourceGain());
            } else {
                if (getType() == Consumeable.Type.ATTRIBUTE) {
                    AttributeBuff attributeBuff = Consume.this.addEffect(AttributeBuff.class);
                    attributeBuff.setModifier(getResourceGain());
                    attributeBuff.setAttribute(getAttribute().getName());
                    if (getConsumeable().getDuration() > 0) attributeBuff.setDuration(getConsumeable().getDuration());
                } else {
                    ConsumeEffect consumeEffect = Consume.this.addEffect(ConsumeEffect.class);
                    consumeEffect.setConsumeable(this);
                    if (getConsumeable().getDuration() > 0) consumeEffect.setDuration(getConsumeable().getDuration());
                    if (getConsumeable().getInterval() > 0) consumeEffect.setInterval(getConsumeable().getInterval());
                }
            }

            if (itemStack.getAmount() > 1) {
                itemStack.setAmount(itemStack.getAmount() - 1);
            } else {
                getHolder().getPlayer().getInventory().remove(itemStack);
            }
        }

        public void applyRessourceGain(double ressourceGain) throws CombatException {
            switch (getType()) {
                case HEALTH:
                    new HealAction<>(this, getHolder(), ressourceGain).run();
                    break;
                case RESOURCE:
                    Resource resource = getResource();
                    resource.setCurrent(resource.getCurrent() + ressourceGain);
                    break;
                case ATTRIBUTE:
                    Attribute attribute = getAttribute();
                    attribute.updateBaseValue((int) ressourceGain, true);
                    break;

            }
        }

        public Attribute getAttribute() {
            return getHolder().getAttribute(getConsumeable().getResourceName());
        }

        public Resource getResource() {

            return getHolder().getResource(getConsumeable().getResourceName());
        }

        public double getResourceGain() {

            return getConsumeable().getResourceGain();
        }

        public Consumeable.Type getType() {
            return getConsumeable().getConsumeableType();
        }

        public boolean isPercentage() {
            return getConsumeable().isPercentage();
        }
    }
}
