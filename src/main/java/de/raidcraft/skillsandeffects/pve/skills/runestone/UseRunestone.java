package de.raidcraft.skillsandeffects.pve.skills.runestone;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.RaidCraft;
import de.raidcraft.api.items.CustomItemException;
import de.raidcraft.api.items.CustomItemStack;
import de.raidcraft.api.items.attachments.ItemAttachmentException;
import de.raidcraft.api.items.attachments.UseableItemAttachment;
import de.raidcraft.api.items.tooltip.Tooltip;
import de.raidcraft.api.items.tooltip.TooltipSlot;
import de.raidcraft.skills.api.combat.EffectType;
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
import de.raidcraft.skills.tables.TRunestone;
import de.raidcraft.skills.trigger.AttackTrigger;
import de.raidcraft.skills.trigger.DamageTrigger;
import de.raidcraft.util.LocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Use Runestone",
        description = "Ermöglicht es Runensteine zu benutzen.",
        types = {EffectType.MOVEMENT}
)
public class UseRunestone extends AbstractSkill implements Triggered, CommandTriggered, UseableItemAttachment {

    private boolean cancelOnDamage;
    private boolean cancelOnAttack;
    private CustomItemStack runestone;

    public UseRunestone(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void loadAttachment(ConfigurationSection data) {


    }

    @Override
    public void applyAttachment(Player player) throws CustomItemException {


    }

    @Override
    public void removeAttachment(Player player) throws CustomItemException {


    }

    @Override
    public void load(ConfigurationSection data) {

        cancelOnDamage = data.getBoolean("cancel-on-damage", true);
        cancelOnAttack = data.getBoolean("cancel-on-attack", true);
    }

    @Override
    public void use(CustomItemStack item, Player player, ConfigurationSection args) throws ItemAttachmentException {

        try {
            if (getHolder().hasEffect(CastTime.class)) {
                runestone = null;
                getHolder().removeEffect(CastTime.class);
                removeEffect(Immobilize.class);
            } else {
                runestone = item;
                new SkillAction(this).run();
                // also apply the lock down effect
                addEffect(Immobilize.class);
            }
        } catch (CombatException e) {
            throw new ItemAttachmentException(e.getMessage());
        }
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        CustomItemStack itemInHand = RaidCraft.getCustomItem(getHolder().getPlayer().getItemInHand());
        if (this.runestone == null || !this.runestone.equals(itemInHand)) {
            throw new CombatException("Bitte halte den Runenstein bis zum Ende der Teleportation in deiner Hand.");
        }
        TRunestone runestone = TRunestone.getRunestone(this.runestone);
        this.runestone = null;
        if (runestone == null) {
            getHolder().getPlayer().getInventory().remove(getHolder().getPlayer().getItemInHand());
            throw new CombatException("Der Runenstein in deiner Hand hat bereits seine Wirkung verloren und wurde zerstört.");
        }
        World world = LocationUtil.getCaseInsensitiveWorld(runestone.getWorld());
        if (world == null) {
            throw new CombatException("Die Zielwelt (" + runestone.getWorld() + ") des Runensteins existiert nicht!");
        }
        if (runestone.getRemainingUses() > 1) {
            try {
                TRunestone.updateRunestone(runestone, runestone.getRemainingUses() - 1);
                Tooltip tooltip = itemInHand.getTooltip(TooltipSlot.MISC);
                itemInHand.setTooltip(new RunestoneTooltip(runestone.getRemainingUses(), runestone.getMaxUses(), tooltip.getTooltip()[1]));
                itemInHand.rebuild(getHolder().getPlayer());
                getHolder().getPlayer().getInventory().setItemInHand(itemInHand);
            } catch (CustomItemException ignored) {
            }

        } else if (runestone.getRemainingUses() == 1) {
            TRunestone.deleteRunestone(runestone);
            getHolder().getPlayer().getInventory().remove(getHolder().getPlayer().getItemInHand());
            getHolder().sendMessage(ChatColor.RED + "Die Energie des Runensteins ist erloschen und er wurde zerstört.");
        } else {
            TRunestone.deleteRunestone(runestone);
            getHolder().getPlayer().getInventory().remove(getHolder().getPlayer().getItemInHand());
            throw new CombatException("Es wurden bereits alle Aufladungen des Runensteins aufgebraucht! Der Runenstein ist beim Benutzen zerbrochen...");
        }
        Location location = new Location(world,
                runestone.getX(),
                runestone.getY(),
                runestone.getZ(),
                runestone.getYaw(),
                runestone.getPitch());
        getHolder().getPlayer().teleport(location);
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
            runestone = null;
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
