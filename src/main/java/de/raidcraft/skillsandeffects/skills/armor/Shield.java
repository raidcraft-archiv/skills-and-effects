package de.raidcraft.skillsandeffects.skills.armor;

import de.raidcraft.skills.api.combat.AttackType;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.callback.Callback;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractLevelableSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.skills.ConfigurableSkillLevel;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.DamageTrigger;
import de.raidcraft.skills.trigger.ItemHeldTrigger;
import de.raidcraft.skills.util.ItemUtil;
import de.raidcraft.skillsandeffects.effects.armor.Shielded;
import de.raidcraft.util.ItemUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.EnumMap;
import java.util.Map;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Shield",
        desc = "Absorbiert Schaden durch das Tragen eines Schildes.",
        types = {EffectType.HELPFUL}
)
public class Shield extends AbstractLevelableSkill implements Triggered {

    private final Map<Material, Integer> shieldLevels = new EnumMap<>(Material.class);
    private final Map<Material, Double> shieldExp = new EnumMap<>(Material.class);
    private double staminaCostPerBlockedDamage = 0.01;

    public Shield(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        attachLevel(new ConfigurableSkillLevel(this, database, data));

        ConfigurationSection shields = data.getConfigurationSection("shields");
        if (shields != null) {
            for (String key : shields.getKeys(false)) {
                Material item = ItemUtils.getItem(key);
                if (item != null) {
                    shieldLevels.put(item, shields.getInt(key + ".level", 1));
                    shieldExp.put(item, shields.getDouble(key + ".exp", 0.3));
                }
            }
        }
        staminaCostPerBlockedDamage = data.getDouble("stamina-cost-per-damage", 0.01);
    }

    @Override
    public void apply() {

    }

    @Override
    public void remove() {

    }

    @TriggerHandler
    public void onItemHeldChange(ItemHeldTrigger trigger) throws CombatException {

        ItemStack item = getHero().getPlayer().getInventory().getItem(trigger.getEvent().getNewSlot());
        if (item == null || item.getTypeId() == 0) {
            getHero().removeEffect(Shielded.class);
            return;
        }
        final Material type = item.getType();
        if (ItemUtil.ArmorSlot.fromMaterial(type) == ItemUtil.ArmorSlot.SHIELD
                && shieldLevels.containsKey(type)) {
            if (shieldLevels.get(type) > getLevel().getLevel()) {
                getHero().sendMessage(ChatColor.RED + "Du kannst diesen Schild erst ab Level " + shieldLevels.get(type) + " tragen.");
            } else {
                final Shielded effect = addEffect(getHero(), Shielded.class);
                effect.addCallback(new Callback<DamageTrigger>() {
                    @Override
                    public void run(DamageTrigger trigger) throws CombatException {

                        if (!trigger.getAttack().isOfAttackType(AttackType.PHYSICAL)) {
                            // dont block damage that is not physical
                            trigger.getAttack().setCancelled(true);
                            return;
                        }
                        // lets check if he has enough stamina to block the damage
                        int staminaCost = (int) (staminaCostPerBlockedDamage * effect.getBlockedDamage());
                        if (getHero().getStamina() < staminaCost) {
                            int oldBlock = effect.getBlockedDamage();
                            // lets set the blocked damage to the remaining stamina
                            effect.setBlockedDamage((int) (getHero().getStamina() / staminaCostPerBlockedDamage));
                            throw new CombatException(
                                    "Du konntest nur " + effect.getBlockedDamage() + " von " + oldBlock + " Schaden blocken.");
                        } else {
                            getHero().setStamina(getHero().getStamina() - staminaCost);
                        }
                        getLevel().addExp((int) (effect.getBlockedDamage() * shieldExp.get(type)));
                    }
                });
                return;
            }
        }
        getHero().removeEffect(Shielded.class);
    }
}
