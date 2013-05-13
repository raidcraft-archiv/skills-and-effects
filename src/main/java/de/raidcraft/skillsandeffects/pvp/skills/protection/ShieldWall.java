package de.raidcraft.skillsandeffects.pvp.skills.protection;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.ItemHeldTrigger;
import de.raidcraft.skills.util.ItemUtil;
import de.raidcraft.skillsandeffects.pvp.effects.buffs.protection.ShieldWallEffect;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Shieldwall",
        description = "Du hebst deinen Schild und erhälst weniger Schaden.",
        types = {EffectType.PROTECTION, EffectType.REDUCING, EffectType.HELPFUL}
)
public class ShieldWall extends AbstractSkill implements CommandTriggered, Triggered {

    public ShieldWall(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        if (ItemUtil.isShield(getHolder().getEntity().getEquipment().getItemInHand())) {
            throw new CombatException("Du musst für diesen Skill einen Schild tragen.");
        }
    }

    @TriggerHandler(ignoreCancelled = true)
    public void onItemHeldChange(ItemHeldTrigger trigger) throws CombatException {

        if (!getHolder().hasEffect(ShieldWallEffect.class)) {
            return;
        }
        if (!ItemUtil.isShield(getHolder().getPlayer().getInventory().getItem(trigger.getEvent().getNewSlot()))) {
            getHolder().removeEffect(ShieldWallEffect.class);
        }
    }
}
