package de.raidcraft.skillsandeffects.pve.skills.runestone;

import de.raidcraft.api.items.tooltip.FixedMultilineTooltip;
import de.raidcraft.api.items.tooltip.TooltipSlot;
import org.bukkit.ChatColor;

/**
 * @author Silthus
 */
public class RunestoneTooltip extends FixedMultilineTooltip {

    public RunestoneTooltip(int uses, int maxUses, String location) {

        super(TooltipSlot.MISC);
        location = ChatColor.stripColor(location.replace("Ort: ", ""));
        ChatColor color = ChatColor.GREEN;
        if (maxUses > 2 && uses < 3) color = ChatColor.GOLD;
        if (uses < 2) color = ChatColor.RED;
        setTooltip(
                ChatColor.GREEN + "Aufladungen: " + color + uses + ChatColor.GREEN + "/" + ChatColor.AQUA + maxUses,
                ChatColor.GREEN + "Ort: " + ChatColor.GOLD + location
        );
    }
}
