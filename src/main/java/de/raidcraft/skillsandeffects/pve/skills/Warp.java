package de.raidcraft.skillsandeffects.pve.skills;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.api.items.CustomItemException;
import de.raidcraft.api.items.CustomItemStack;
import de.raidcraft.api.items.attachments.ItemAttachmentException;
import de.raidcraft.api.items.attachments.UseableItemAttachment;
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
import de.raidcraft.skills.trigger.AttackTrigger;
import de.raidcraft.skills.trigger.DamageTrigger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Warp",
        description = "Teleportiert den Benutzer zu den angegbenen Koordinaten",
        types = {EffectType.MOVEMENT}
)
public class Warp extends AbstractSkill implements Triggered, CommandTriggered, UseableItemAttachment {

    private boolean cancelOnDamage;
    private boolean cancelOnAttack;
    private boolean bedSpawn;
    private Location destination;

    public Warp(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void loadAttachment(ConfigurationSection data) {

        
    }

    @Override
    public void load(ConfigurationSection data) {

        cancelOnDamage = data.getBoolean("cancel-on-damage", true);
        cancelOnAttack = data.getBoolean("cancel-on-attack", true);
        bedSpawn = data.getBoolean("bed-spawn", false);
        int x = data.getInt("x");
        int y = data.getInt("y");
        int z = data.getInt("z");
        float yaw = (float) data.getDouble("yaw");
        float pitch = (float) data.getDouble("pitch");
        destination = new Location(Bukkit.getWorld(data.getString("world")), x, y, z, yaw, pitch);
    }

    @Override
    public void use(CustomItemStack item, Player player, ConfigurationSection args) throws ItemAttachmentException {

        try {
            if (getHolder().hasEffect(CastTime.class)) {
                getHolder().removeEffect(CastTime.class);
                removeEffect(Immobilize.class);
            } else {
                new SkillAction(this).run();
                // also apply the lock down effect
                addEffect(Immobilize.class);
            }
        } catch (CombatException e) {
            throw new ItemAttachmentException(e.getMessage());
        }
    }

    @Override
    public void applyAttachment(Player player) throws CustomItemException {


    }

    @Override
    public void removeAttachment(Player player) throws CustomItemException {


    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        if (bedSpawn) {
            Location spawnLocation = getHolder().getPlayer().getBedSpawnLocation();
            if (spawnLocation != null) {
                getHolder().getEntity().teleport(spawnLocation);
                return;
            }
        }
        getHolder().getEntity().teleport(destination);
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.MONITOR)
    public void onDamage(DamageTrigger trigger) throws CombatException {

        if (!cancelOnDamage) {
            return;
        }
        checkForSkillCast();
    }

    public void onAttack(AttackTrigger trigger) throws CombatException {

        if (!cancelOnAttack) {
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
}
