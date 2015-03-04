package de.raidcraft.skillsandeffects.pve.skills;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.api.items.CustomItemException;
import de.raidcraft.skills.api.combat.action.SkillAction;
import de.raidcraft.skills.api.effect.common.CastTime;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.TriggerPriority;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.effects.disabling.Immobilize;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.AttackTrigger;
import de.raidcraft.skills.trigger.DamageTrigger;
import de.raidcraft.skills.trigger.PlayerInteractTrigger;
import de.raidcraft.util.CustomItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.regex.Matcher;

/**
 * @author mdoering
 */
@SkillInformation(
        name = "Runestone User",
        description = "Ermöglicht es Runensteine zu benutzen."
)
public class RunestoneUser extends AbstractSkill implements Triggered, CommandTriggered {

    private boolean cancelOnDamage;
    private boolean cancelOnAttack;
    private ItemStack runestone;

    public RunestoneUser(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        cancelOnDamage = data.getBoolean("cancel-on-damage", true);
        cancelOnAttack = data.getBoolean("cancel-on-attack", true);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        try {
            if (runestone == null) {
                checkExecution(getHolder().getPlayer());
                return;
            }
            List<String> lore = runestone.getItemMeta().getLore();
            int uses = CustomItemUtil.decodeItemId(lore.get(0));
            if (uses < 1) {
                getHolder().getPlayer().getInventory().remove(runestone);
                throw new CombatException("Es wurden alle Aufladungen aufgebraucht!");
            }
            Matcher matcher = Runestone.LOCATION_PATTERN.matcher(lore.get(1));
            if (!matcher.matches()) throw new CombatException("Invalid location pattern!");
            World world = Bukkit.getWorld(matcher.group(1));
            if (world == null) {
                throw new CombatException("World of runestone is not loaded!");
            }
            Location location = new Location(world, Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(3)), Integer.parseInt(matcher.group(4)));
            getHolder().getPlayer().teleport(location);
            if (uses - 1 < 1) {
                getHolder().getPlayer().getInventory().remove(runestone);
                getHolder().sendMessage(ChatColor.RED + "Es wurden alle Runenstein Aufladungen verbraucht! Der Runenstein wurde zerstört.");
                return;
            }
            Runestone.updateLore(runestone, uses - 1);
        } catch (CustomItemException ignored) {
        }
    }

    @TriggerHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractTrigger trigger) {

        checkExecution(trigger.getEvent().getPlayer());
    }

    private void checkExecution(Player player) {

        ItemStack itemInHand = player.getItemInHand();
        if (itemInHand.hasItemMeta() && itemInHand.getItemMeta().hasLore() && itemInHand.getItemMeta().getLore().size() > 1) {
            try {
                if (CustomItemUtil.decodeItemId(itemInHand.getItemMeta()) == Runestone.RUNESTONE_ID) {
                    if (getHolder().hasEffect(CastTime.class)) {
                        getHolder().removeEffect(CastTime.class);
                        removeEffect(Immobilize.class);
                    } else {
                        runestone = itemInHand;
                        new SkillAction(this).run();
                        // also apply the lock down effect
                        addEffect(Immobilize.class);
                    }
                }
            } catch (CustomItemException | CombatException ignored) {
            }
        }
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.MONITOR)
    public void onDamage(DamageTrigger trigger) throws CombatException {

        if (!cancelOnDamage) {
            return;
        }
        checkForSkillCast();
    }

    private void checkForSkillCast() throws CombatException {

        if (!getHolder().hasEffect(CastTime.class)) {
            return;
        }
        if (getEffect(CastTime.class).getSource().getSkill().equals(this)) {
            removeEffect(CastTime.class);
            removeEffect(Immobilize.class);
        }
    }

    public void onAttack(AttackTrigger trigger) throws CombatException {

        if (!cancelOnAttack) {
            return;
        }
        checkForSkillCast();
    }
}
