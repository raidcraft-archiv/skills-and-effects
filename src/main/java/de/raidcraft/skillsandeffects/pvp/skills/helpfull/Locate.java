package de.raidcraft.skillsandeffects.pvp.skills.helpfull;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.util.LocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Locate",
        description = "Der Compass zeigt auf den Spieler der am nächsten ist.",
        types = {EffectType.HELPFUL}
)
public class Locate extends AbstractSkill implements CommandTriggered {

    public Locate(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

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
    }
}
