package de.raidcraft.skillsandeffects.pve.skills;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.combat.EffectType;
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
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.AttackTrigger;
import de.raidcraft.skills.trigger.DamageTrigger;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Warp",
        description = "Teleportiert den Benutzer zu den angegbenen Koordinaten",
        types = {EffectType.MOVEMENT}
)
public class Warp extends AbstractSkill implements Triggered, CommandTriggered {

    private boolean cancelOnDamage;
    private boolean cancelOnAttack;
    private Location destination;

    public Warp(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        int x = data.getInt("x");
        int y = data.getInt("y");
        int z = data.getInt("z");
        float yaw = (float) data.getDouble("yaw");
        float pitch = (float) data.getDouble("pitch");
        destination = new Location(getHolder().getEntity().getWorld(), x, y, z, yaw, pitch);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

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
        if (getHolder().getEffect(CastTime.class).getSource().getSkill().equals(this)) {
            getHolder().getEffect(CastTime.class).remove();
        }
    }
}
