package de.raidcraft.skillsandeffects.pvp.skills.magical;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.RaidCraft;
import de.raidcraft.skills.SkillsPlugin;
import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectElement;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.action.EntityAttack;
import de.raidcraft.skills.api.combat.callback.EntityAttackCallback;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.skillsandeffects.pvp.effects.damaging.ChainLightningEffect;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Chain Lightning",
        description = "Lässt einen Blitz auf Nachbarn überspringen.",
        types = {EffectType.DAMAGING, EffectType.MAGICAL, EffectType.SILENCABLE},
        elements = {EffectElement.LIGHTNING}
)
public class ChainLightning extends AbstractSkill implements CommandTriggered {

    private ConfigurationSection jumps;
    private ConfigurationSection reductionPerJump;
    private int jumpRange;
    private int jumpCount = 0;
    private double initialDamage;

    public ChainLightning(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        jumps = data.getConfigurationSection("jumps");
        reductionPerJump = data.getConfigurationSection("reduction-per-jump");
        jumpRange = data.getInt("jump-range", 10);
    }

    private int getJumpCount() {

        return (int) ConfigUtil.getTotalValue(this, jumps);
    }

    private double getReductionPerJump() {

        return ConfigUtil.getTotalValue(this, reductionPerJump);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        initialDamage = getTotalDamage();
        // damage the intial target
        strikeChainLightning(getTarget(), initialDamage);
        jumpCount = 0;
    }

    private void strikeChainLightning(CharacterTemplate target, final double damage) throws CombatException {

        magicalAttack(target, damage, new EntityAttackCallback() {
            @Override
            public void run(EntityAttack attack) throws CombatException {

                ++jumpCount;
                ChainLightning.this.addEffect(attack.getTarget(), ChainLightningEffect.class);
                if (jumpCount < getJumpCount()) {
                    try {
                        for (final CharacterTemplate target : attack.getTarget().getNearbyTargets(jumpRange)) {
                            if (target.hasEffect(ChainLightningEffect.class) || target.equals(getHolder()) || target.isFriendly(getHolder())) {
                                continue;
                            }
                            final int newDamage = (int) (damage - (initialDamage * getReductionPerJump()));
                            if (newDamage > 0) {
                                Bukkit.getScheduler().runTaskLater(RaidCraft.getComponent(SkillsPlugin.class), new Runnable() {
                                    @Override
                                    public void run() {

                                        try {
                                            strikeChainLightning(target, newDamage);
                                        } catch (CombatException e) {
                                            getHolder().sendMessage(ChatColor.RED + e.getMessage());
                                        }
                                    }
                                }, 4L);
                            }
                            break;
                        }
                    } catch (CombatException | IllegalArgumentException e) {
                        getHolder().sendMessage(ChatColor.RED + e.getMessage());
                    }
                }
            }
        }).run();
    }
}
