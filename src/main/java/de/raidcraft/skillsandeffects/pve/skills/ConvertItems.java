package de.raidcraft.skillsandeffects.pve.skills;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.RaidCraft;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.util.ItemUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Convert Items",
        description = "Schmilzt Erze in der Hand direkt zu deren Barren Form",
        types = {EffectType.HELPFUL}
)
public class ConvertItems extends AbstractSkill implements CommandTriggered {

    private ConfigurationSection amount;
    private Map<Material, Material> itemConversionMap = new HashMap<>();

    public ConvertItems(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        amount = data.getConfigurationSection("amount");
        ConfigurationSection ores = data.getConfigurationSection("items");
        if (ores == null) {
            RaidCraft.LOGGER.warning("No items defined in the config of " + getName());
            return;
        }
        for (String key : ores.getKeys(false)) {
            Material item = ItemUtils.getItem(key);
            if (item == null) {
                RaidCraft.LOGGER.warning("Item key " + key + " is not a valid item in " + getName());
                continue;
            }
            Material result = ItemUtils.getItem(ores.getString(key, "AIR"));
            if (result == Material.AIR) {
                RaidCraft.LOGGER.warning("No result item defined for the item " + key + " in " + getName());
                continue;
            }
            itemConversionMap.put(item, result);
        }
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        ItemStack itemStack = getHolder().getPlayer().getItemInHand();
        if (itemStack == null || !itemConversionMap.containsKey(itemStack.getType())) {
            throw new CombatException("Du musst für diesen Skill die Items die du konvertieren möchtest in der Hand haben.");
        }
        int amount = getAmount();
        if (itemStack.getAmount() < amount) {
            throw new CombatException("Du musst mindestens " + amount + " Items in deiner Hand halten.");
        }
        if (itemStack.getAmount() == amount) {
            itemStack.setType(itemConversionMap.get(itemStack.getType()));
        } else {
            itemStack.setAmount(itemStack.getAmount() - amount);
            getHolder().getPlayer().getInventory().addItem(new ItemStack(itemConversionMap.get(itemStack.getType()), amount));
        }
        getHolder().sendMessage(ChatColor.GREEN + getFriendlyName() + " wurde vollzogen und deine Items wurden konvertiert.");
        getHolder().getPlayer().updateInventory();
    }

    private int getAmount() {

        return (int) ConfigUtil.getTotalValue(this, amount);
    }
}
