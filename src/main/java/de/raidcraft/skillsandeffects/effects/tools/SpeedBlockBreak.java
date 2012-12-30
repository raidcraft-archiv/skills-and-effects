package de.raidcraft.skillsandeffects.effects.tools;

import de.raidcraft.RaidCraft;
import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectElement;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.ExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.skill.Skill;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.trigger.PlayerInteractTrigger;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

/**
 * @author Philip
 */
@EffectInformation(
        name = "SpeedBlockBreak",
        description = "Blitzschnelles abbauen von Blöcken",
        types = {EffectType.BUFF},
        elements = {EffectElement.LIGHT}
)
public class SpeedBlockBreak extends ExpirableEffect<Skill> implements Triggered {

    private List<Integer> blocks;
    private String activateMsg;
    private String deactivateMsg;
    private int toolId = -1;
    
    public SpeedBlockBreak(Skill source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @Override
    public void load(ConfigurationSection data) {
        this.activateMsg = data.getString("activate-message", "SuperBreaker aktiviert!");
        this.deactivateMsg = data.getString("deactivate-message", "SuperBreaker deaktiviert!");
        this.toolId = data.getInt("tool-id");
        this.blocks = data.getIntegerList("blocks");
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {
        if(!(target.getEntity() instanceof Player)) {
            return;
        }
        
        ((Player) target.getEntity()).sendMessage(ChatColor.GREEN + activateMsg);
    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {
        if(!(target.getEntity() instanceof Player)) {
            return;
        }

        ((Player) target.getEntity()).sendMessage(ChatColor.GREEN + deactivateMsg);
    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {
    }
    
    @TriggerHandler
    public void onInteract(PlayerInteractTrigger trigger) {

        PlayerInteractEvent event = trigger.getEvent();

        // check if correct tool in use
        if(event.getItem() == null || event.getItem().getTypeId() != toolId) {
            return;
        }
        trigger.getHero().debug("Correct tool!");

        // check if clicked block is in list
        if(event.getClickedBlock() == null || !blocks.contains(event.getClickedBlock().getTypeId())) {
            trigger.getHero().debug("Block not known!");
            return;
        }
        trigger.getHero().debug("Block known!");
        
        BlockBreakEvent fakeBreakEvent = new BlockBreakEvent(event.getClickedBlock(), event.getPlayer());
        RaidCraft.callEvent(fakeBreakEvent);
        if(!fakeBreakEvent.isCancelled()) {
            event.getClickedBlock().setType(Material.AIR);
            trigger.getHero().debug("Delete block");
        }
    }
}
