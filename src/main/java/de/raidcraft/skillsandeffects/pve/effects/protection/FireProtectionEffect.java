package de.raidcraft.skillsandeffects.pve.effects.protection;

import de.raidcraft.api.items.EquipmentSlot;
import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.types.PeriodicExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.effects.Burn;
import de.raidcraft.skillsandeffects.pve.skills.Fireprotection;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Fire Protection",
        description = "Wirft alle 2s ein Rüstungsteil ab.",
        types = {EffectType.PROTECTION}
)
public class FireProtectionEffect extends PeriodicExpirableEffect<Fireprotection> {

    private int tossedItems = 0;

    public FireProtectionEffect(Fireprotection source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @Override
    protected void tick(CharacterTemplate target) throws CombatException {

        EntityEquipment armor = target.getEntity().getEquipment();
        if (armor.getArmorContents().length == 0) {
            if (tossedItems == 4) {
                target.removeEffect(Burn.class);
                target.getEntity().setFireTicks(0);
            }
            remove();
            return;
        }
        Location location = target.getEntity().getLocation();
        if (armor.getHelmet() != null) {
            location.getWorld().dropItemNaturally(location, armor.getHelmet());
            armor.setHelmet(new ItemStack(Material.AIR));
            target.removeArmor(EquipmentSlot.HEAD);
        } else if (armor.getChestplate() != null) {
            location.getWorld().dropItemNaturally(location, armor.getChestplate());
            armor.setChestplate(new ItemStack(Material.AIR));
            target.removeArmor(EquipmentSlot.CHEST);
        } else if (armor.getLeggings() != null) {
            location.getWorld().dropItemNaturally(location, armor.getLeggings());
            armor.setLeggings(new ItemStack(Material.AIR));
            target.removeArmor(EquipmentSlot.LEGS);
        } else if (armor.getBoots() != null) {
            location.getWorld().dropItemNaturally(location, armor.getBoots());
            armor.setBoots(new ItemStack(Material.AIR));
            target.removeArmor(EquipmentSlot.FEET);
        }
        tossedItems++;
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {


    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {


    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {


    }
}
