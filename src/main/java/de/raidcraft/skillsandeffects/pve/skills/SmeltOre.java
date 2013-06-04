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
        name = "Smelt Ore",
        description = "Schmilzt Erze in der Hand direkt zu deren Barren Form",
        types = {EffectType.HELPFUL}
)
public class SmeltOre extends AbstractSkill implements CommandTriggered {

    private ConfigurationSection amount;
    private Map<Integer, Integer> itemConversionMap = new HashMap<>();

    public SmeltOre(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        amount = data.getConfigurationSection("amount");
        ConfigurationSection ores = data.getConfigurationSection("ores");
        if (ores == null) {
            RaidCraft.LOGGER.warning("No ores defined in the config of " + getName());
            return;
        }
        for (String key : ores.getKeys(false)) {
            Material item = ItemUtils.getItem(key);
            if (item == null) {
                RaidCraft.LOGGER.warning("Ore key " + key + " is not a valid item in " + getName());
                continue;
            }
            int result = ores.getInt(key, 0);
            if (result == 0) {
                RaidCraft.LOGGER.warning("No result item defined for the ore " + key + " in " + getName());
                continue;
            }
            itemConversionMap.put(item.getId(), result);
        }
    }

    private int getAmount() {

        return (int) ConfigUtil.getTotalValue(this, amount);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        ItemStack itemStack = getHolder().getPlayer().getItemInHand();
        if (itemStack == null || !itemConversionMap.containsKey(itemStack.getTypeId())) {
            throw new CombatException("Du musst für diesen Skill die Items die du konvertieren möchtest in der Hand haben.");
        }
        int amount = getAmount();
        if (itemStack.getAmount() < amount) {
            throw new CombatException("Du musst mindestens " + amount + " Items in deiner Hand halten.");
        }
        if (itemStack.getAmount() == amount) {
            itemStack.setTypeId(itemConversionMap.get(itemStack.getTypeId()));
        } else {
            itemStack.setAmount(itemStack.getAmount() - amount);
            getHolder().getPlayer().getInventory().addItem(new ItemStack(itemConversionMap.get(itemStack.getTypeId()), amount));
        }
        getHolder().sendMessage(ChatColor.GREEN + getFriendlyName() + " wurde vollzogen und deine Items wurden konvertiert.");
        getHolder().getPlayer().updateInventory();
    }
}
