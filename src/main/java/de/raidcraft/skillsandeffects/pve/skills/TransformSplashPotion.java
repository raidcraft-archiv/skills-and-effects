package de.raidcraft.skillsandeffects.pve.skills;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Transform Splash Potion",
        description = "Verwandelt einen normalen Trank in einen Wurftrank."
)
public class TransformSplashPotion extends AbstractSkill implements CommandTriggered {

    public TransformSplashPotion(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        if (getHero().getItemTypeInHand() != Material.POTION) {
            throw new CombatException("Du musst für diesen Skill den Trank den du anritzen willst in der Hand haben.");
        }
        ItemStack itemInHand = getHero().getEntity().getEquipment().getItemInHand();
        if (itemInHand == null) {
            throw new CombatException("Bitte nehme den Trank den du anritzten willst in die Hand.");
        }
        byte data = (byte) itemInHand.getDurability();
        // lets check if the splash potion bit is already set
        // here we shift the 14 position (the splash potion bit) so
        // that it is a written byte and check if the bit is set
        if ((data & (1L << 14)) != 0) {
            throw new CombatException("Dieser Trank ist bereits ein Wurftrank.");
        }
        // so now lets set the bit
        // remember that the 14 bit is the bit for splash potions
        // http://www.minecraftwiki.net/wiki/Potion#Data_value_table
        data |= 1 << 14;
        itemInHand.setDurability(data);
        ItemMeta itemMeta = itemInHand.getItemMeta();
        itemMeta.setDisplayName("Angeritzter Wurftrank");
        itemInHand.setItemMeta(itemMeta);
    }
}
