package de.raidcraft.skillsandeffects.pve.effects;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.block.BlockBreakEvent;

import de.raidcraft.RaidCraft;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractLevelableSkill;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.TriggerPriority;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.PlayerInteractTrigger;
import de.raidcraft.skills.util.ConfigUtil;

/**
 * @author Silthus
 */
@EffectInformation(name = "Axt Profi", description = "Fällt direkt einen Baum.", types = { EffectType.HELPFUL })
public class AxtProfi extends AbstractLevelableSkill {
	private ConfigurationSection maxBlockAmountConfig;
	private double maxBlockAmount;

	public AxtProfi(Hero hero, SkillProperties data, Profession profession,
			THeroSkill database) {
		super(hero, data, profession, database);
	}

	@Override
	public void load(ConfigurationSection data) {
		maxBlockAmountConfig = data.getConfigurationSection("maxBlockAmount");
	}

	private int getMaxBlockAmount() {
		if (maxBlockAmount == 0.0) {
			maxBlockAmount = ConfigUtil.getTotalValue(this,
					maxBlockAmountConfig);
		}
		return (int) maxBlockAmount;
	}

	private boolean isTreeMaterial(Material mat) {
		return mat == Material.WOOD;
	}

	private int walkInDirection(PlayerInteractTrigger trigger,
			BlockFace direction, Block startBlock, int amount, int maxAmount) {
		BlockBreakEvent event;
		while (amount <= maxAmount && isTreeMaterial(startBlock.getType())) {
			if (!RaidCraft.isPlayerPlacedBlock(startBlock)) {
				// check if blocked
				event = new BlockBreakEvent(startBlock, trigger.getEvent()
						.getPlayer());
				RaidCraft.callEvent(event);
				if (event.isCancelled()) {
					startBlock.breakNaturally(trigger.getEvent().getPlayer()
							.getItemInHand());
					amount++;
				}
			}
			startBlock = startBlock.getRelative(BlockFace.DOWN);
		}
		return amount;
	}

	@TriggerHandler(ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractTrigger trigger) {
		Block targetBlock = trigger.getEvent().getClickedBlock();
		if (!isTreeMaterial(targetBlock.getType())) {
			return;
		}
		int amount = 0;
		int maxAmount = getMaxBlockAmount();
		// first walk down
		amount = walkInDirection(trigger, BlockFace.DOWN, targetBlock, amount,
				maxAmount);
		// then walk up
		amount = walkInDirection(trigger, BlockFace.UP, targetBlock, amount,
				maxAmount);
		getHolder().sendMessage(
				ChatColor.YELLOW + "Du hast direkt " + ChatColor.RED + amount
						+ ChatColor.YELLOW + " Holz abgebaut.");
	}
}
