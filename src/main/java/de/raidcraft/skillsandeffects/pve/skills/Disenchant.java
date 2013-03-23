package de.raidcraft.skillsandeffects.pve.skills;

import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.worldedit.blocks.ItemID;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.util.ConfigUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Disenchant",
        description = "Entfernt alle Verzauberungen von einem Item."
)
public class Disenchant extends AbstractSkill implements CommandTriggered {

    private ConfigurationSection salvageChance;

    public Disenchant(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        salvageChance = data.getConfigurationSection("salvage-chance");
    }

    public double getSalvageChance() {

        return ConfigUtil.getTotalValue(this, salvageChance);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        ItemStack item = getHero().getEntity().getEquipment().getItemInHand();
        if (item == null) {
            throw new CombatException("Bitte nehme das Item das zu entzaubern willst in die Hand.");
        }
        Map<Enchantment,Integer> enchantments = item.getEnchantments();
        if (enchantments.size() < 1 || item.getTypeId() == ItemID.WRITTEN_BOOK) {
            throw new CombatException("Dieses Item hat keine Verzauberungen die du entfernen kannst.");
        }
        for (Enchantment enchantment : enchantments.keySet()) {
            item.removeEnchantment(enchantment);
            if (Math.random() < getSalvageChance()) {
                ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
                book.addEnchantment(enchantment, enchantments.get(enchantment));
                getHero().getPlayer().getInventory().addItem(book);
                // to balance it out we reduce the durability of the item
                item.setDurability((short) (item.getDurability() / 2));
            }
        }
    }
}
