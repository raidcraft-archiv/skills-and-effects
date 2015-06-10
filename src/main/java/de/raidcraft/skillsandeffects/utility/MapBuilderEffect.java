package de.raidcraft.skillsandeffects.utility;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.effect.AbstractEffect;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.trigger.AttackTrigger;
import de.raidcraft.skills.trigger.DamageTrigger;
import de.raidcraft.skills.trigger.ItemDropTrigger;
import de.raidcraft.skills.trigger.ItemPickupTrigger;
import de.raidcraft.skills.trigger.NPCRightClickTrigger;
import de.raidcraft.skills.trigger.PlayerCastSkillTrigger;
import de.raidcraft.skills.trigger.PlayerInteractTrigger;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;

/**
 * @author mdoering
 */
@EffectInformation(
        name = "Map Builder",
        description = "Toggles the gamemode and prevents the dropping of items and more.",
        global = true
)
public class MapBuilderEffect extends AbstractEffect<MapBuilder> implements Triggered {

    public MapBuilderEffect(MapBuilder source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

        getSource().getHolder().getPlayer().setGameMode(GameMode.CREATIVE);
    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {

    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

        Player player = getSource().getHolder().getPlayer();
        player.setGameMode(GameMode.SURVIVAL);
        for (PotionEffect potionEffect : new ArrayList<>(player.getActivePotionEffects())) {
            player.removePotionEffect(potionEffect.getType());
        }
    }

    @EventHandler
    public void onAttack(AttackTrigger trigger) {

        warn("Du kannst im Map Builder Modus nicht angreifen!");
        trigger.setCancelled(true);
    }

    @EventHandler
    public void onDamage(DamageTrigger trigger) {

        trigger.setCancelled(true);
    }

    @TriggerHandler
    public void onSkillCast(PlayerCastSkillTrigger trigger) {

        warn("Du kannst im Map Builder Modus keine Skills casten!");
        trigger.setCancelled(true);
    }

    @TriggerHandler
    public void onItemPickup(ItemPickupTrigger trigger) {

        trigger.getEvent().setCancelled(true);
    }

    @TriggerHandler
    public void onItemDrop(ItemDropTrigger trigger) {

        warn("Du kannst im Map Builder Modus keine Items droppen!");
        trigger.getEvent().setCancelled(true);
    }

    @TriggerHandler
    public void onInteract(PlayerInteractTrigger trigger) {

        PlayerInteractEvent event = trigger.getEvent();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getClickedBlock() instanceof InventoryHolder) {
                warn("Du kannst im Map Builder Modus keine Kisten o.ä. öffnen.");
                event.setCancelled(true);
            }
        }
    }

    @TriggerHandler
    public void onNPCRightClick(NPCRightClickTrigger trigger) {

        warn("Du kannst im Map Builder Modus nicht mit NPCs interagieren.");
        trigger.getEvent().setCancelled(true);
    }
}
