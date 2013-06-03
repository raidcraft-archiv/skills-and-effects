package de.raidcraft.skillsandeffects.pvp.skills.helpfull;

import com.sk89q.worldedit.blocks.ItemID;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.action.SkillAction;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.PlayerInteractTrigger;
import de.raidcraft.util.LocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Locate",
        description = "Der Compass zeigt auf den Spieler der am nächsten ist.",
        types = {EffectType.HELPFUL}
)
public class Locate extends AbstractSkill implements Triggered {

    public Locate(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @TriggerHandler(ignoreCancelled = true)
    public void interact(PlayerInteractTrigger trigger) throws CombatException {

        PlayerInteractEvent event = trigger.getEvent();
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        // check if he has a compas in his hand
        ItemStack item = event.getPlayer().getItemInHand();
        if (item == null || item.getTypeId() != ItemID.COMPASS || !canUseSkill()) {
            return;
        }
        int distance = getTotalRange();
        Player locatedPlayer = null;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("raidcraft.ignore")) {
                continue;
            }
            // calculate the distance
            int blockDistance = LocationUtil.getBlockDistance(player.getLocation(), getHolder().getEntity().getLocation());
            if (locatedPlayer == null
                    || blockDistance < distance) {
                locatedPlayer = player;
                distance = blockDistance;
            }
        }
        if (locatedPlayer == null) {
            throw new CombatException("Es wurde kein Spieler im Umkreis von " + distance + "m gefunden.");
        }
        getHolder().getPlayer().setCompassTarget(locatedPlayer.getLocation());
        getHolder().sendMessage(ChatColor.GREEN + "Die letzte Position des Spielers " + locatedPlayer.getName()
                + " wurde in " + distance + "m Entfernung auf deinem Kompass markiert.");
        substractUsageCost(new SkillAction(this));
    }
}
