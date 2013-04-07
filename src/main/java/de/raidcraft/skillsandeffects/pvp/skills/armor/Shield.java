package de.raidcraft.skillsandeffects.pvp.skills.armor;

import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.callback.Callback;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.resource.Resource;
import de.raidcraft.skills.api.skill.AbstractLevelableSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.DamageTrigger;
import de.raidcraft.skills.trigger.ItemHeldTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.skills.util.ItemUtil;
import de.raidcraft.skillsandeffects.pvp.effects.armor.Shielded;
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
        description = "Absorbiert Schaden durch das Tragen eines Schildes.",
        types = {EffectType.HELPFUL, EffectType.ABSORBING, EffectType.PHYSICAL}
)
public class Shield extends AbstractLevelableSkill implements Triggered {

    private final Map<Material, Integer> shieldLevels = new EnumMap<>(Material.class);
    private final Map<Material, Double> shieldExp = new EnumMap<>(Material.class);
    private final Map<Material, Double> shieldReduction = new EnumMap<>(Material.class);
    private double resourceCostPerBlockedDamage = 0.01;
    private String resourceName = "stamina";

    public Shield(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        ConfigurationSection shields = data.getConfigurationSection("shields");
        if (shields != null) {
            for (String key : shields.getKeys(false)) {
                Material item = ItemUtils.getItem(key);
                if (item != null) {
                    shieldLevels.put(item, shields.getInt(key + ".level", 1));
                    shieldExp.put(item, shields.getDouble(key + ".exp", 0.3));
                    shieldReduction.put(item,
                            ConfigUtil.getTotalValue(this, data.getConfigurationSection("shields." + key + ".reduction")));
                }
            }
        }
        resourceName = data.getString("resource.name", "stamina");
        resourceCostPerBlockedDamage = data.getDouble("resource.cost-per-damage", 0.01);
    }

    @TriggerHandler
    public void onItemHeldChange(ItemHeldTrigger trigger) throws CombatException {

        ItemStack item = getHero().getPlayer().getInventory().getItem(trigger.getEvent().getNewSlot());
        if (item == null || item.getTypeId() == 0 || !getHero().isInCombat()) {
            getHero().removeEffect(Shielded.class);
            return;
        }

        if (getHero().getResource(resourceName) == null) {
            return;
        }

        final Material type = item.getType();
        if (ItemUtil.isShield(type) && shieldLevels.containsKey(type)) {
            if (shieldLevels.get(type) > getAttachedLevel().getLevel()) {
                getHero().sendMessage(ChatColor.RED + "Du kannst diesen Schild erst ab Level " + shieldLevels.get(type) + " tragen.");
            } else {
                final Shielded effect = addEffect(getHero(), Shielded.class);
                effect.setDamageReduction(shieldReduction.get(type));
                effect.addCallback(new Callback<DamageTrigger>() {
                    @Override
                    public void run(DamageTrigger trigger) throws CombatException {

                        if (!trigger.getAttack().isOfAttackType(EffectType.PHYSICAL)
                                || trigger.getAttack().isOfAttackType(EffectType.IGNORE_ARMOR)) {
                            // dont block damage that is not physical
                            trigger.getAttack().setCancelled(true);
                            return;
                        }
                        Resource resource = getHero().getResource(resourceName);
                        // lets check if he has enough stamina to block the damage
                        int resourceCost = (int) (resourceCostPerBlockedDamage * effect.getBlockedDamage());
                        if (resource.getCurrent() < resourceCost) {
                            int oldBlock = effect.getBlockedDamage();
                            // lets set the blocked damage to the remaining stamina
                            effect.setBlockedDamage((int) (resource.getCurrent() / resourceCostPerBlockedDamage));
                            throw new CombatException(
                                    "Du konntest nur " + effect.getBlockedDamage() + " von " + oldBlock + " Schaden blocken.");
                        } else {
                            resource.setCurrent(resource.getCurrent() - resourceCost);
                        }
                        getAttachedLevel().addExp((int) (effect.getBlockedDamage() * shieldExp.get(type)));
                    }
                });
                return;
            }
        }
        getHero().removeEffect(Shielded.class);
    }
}
