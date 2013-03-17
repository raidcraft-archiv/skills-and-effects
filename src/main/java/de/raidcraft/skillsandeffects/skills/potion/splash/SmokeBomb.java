package de.raidcraft.skillsandeffects.skills.potion.splash;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.ProjectileType;
import de.raidcraft.skills.api.combat.action.RangedAttack;
import de.raidcraft.skills.api.combat.callback.LocationCallback;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractLevelableSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.PlayerInteractTrigger;
import de.raidcraft.util.BukkitUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Smoke Bomb",
        desc = "Hüllt alle am Aufschlagsort in Rauch ein und blendet diese.",
        types = {EffectType.HARMFUL, EffectType.DEBUFF}
)
public class SmokeBomb extends AbstractLevelableSkill implements CommandTriggered, Triggered {

    private static final short MUNDANE_SPLASH_DATA = 16384;
    private static final String INVISIBLE_IDENTIFIER = "§1§1§1";
    private static final String DISPLAY_NAME = "Rauchbombe";

    public SmokeBomb(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        // 16384 is the data bit of the splash potion
        // this will create a mundane splash potion
        ItemStack item = new ItemStack(Material.POTION, 1, MUNDANE_SPLASH_DATA);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        meta.setMainEffect(PotionEffectType.BLINDNESS);
        meta.setDisplayName(DISPLAY_NAME);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(INVISIBLE_IDENTIFIER);
        meta.setLore(lore);
        item.setItemMeta(meta);

        getHero().getPlayer().getInventory().addItem(item);
    }

    @TriggerHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractTrigger trigger) throws CombatException {

        PlayerInteractEvent event = trigger.getEvent();
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.POTION || item.getData().getData() != MUNDANE_SPLASH_DATA) {
            return;
        }

        // the held item is a mundane splash potion
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        if (!meta.getDisplayName().equals(DISPLAY_NAME)) {
            return;
        }
        try {
            if (!meta.getLore().get(0).equals(INVISIBLE_IDENTIFIER)) {
                return;
            }
        } catch (IndexOutOfBoundsException ignored) {
            return;
        }
        // at this point we asume the potion is the one we created
        // now lets cancel the interact event and spawn our own splash potion
        event.setCancelled(true);
        final RangedAttack<LocationCallback> attack = rangedAttack(ProjectileType.SPLASH_POTION);
        attack.addCallback(new LocationCallback() {
            @Override
            public void run(Location location) throws CombatException {

                BukkitUtil.getNearbyEntities(attack.getProjectile(), getTotalRange());
            }
        });
        attack.run();
    }
}
