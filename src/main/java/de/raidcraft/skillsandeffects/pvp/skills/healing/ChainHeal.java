package de.raidcraft.skillsandeffects.pvp.skills.healing;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.RaidCraft;
import de.raidcraft.skills.SkillsPlugin;
import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectElement;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.skillsandeffects.pvp.effects.buffs.healing.ChainHealEffect;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Chain Heal",
        description = "Schickt eine Welle von Heilung auf den Weg.",
        types = {EffectType.DAMAGING, EffectType.MAGICAL, EffectType.SILENCABLE},
        elements = {EffectElement.LIGHTNING}
)
public class ChainHeal extends AbstractSkill implements CommandTriggered {

    private ConfigurationSection jumps;
    private ConfigurationSection reductionPerJump;
    private int jumpRange;
    private int jumpCount = 0;
    private double intialHeal;

    public ChainHeal(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

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

        jumpCount = 0;
        intialHeal = getTotalDamage();
        // damage the intial target
        CharacterTemplate target = getTarget(args, true, true);
        if (!target.isFriendly(getHolder())) {
            throw new CombatException("Du kannst nur befreundete Ziele heilen.");
        }
        castChainHeal(target, intialHeal);
    }

    private void castChainHeal(CharacterTemplate target, final double healAmount) throws CombatException {

        heal(target, healAmount).run();

        ++jumpCount;
        addEffect(target, ChainHealEffect.class);
        if (jumpCount < getJumpCount()) {
            try {
                for (final CharacterTemplate nextTarget : target.getNearbyTargets(jumpRange, true)) {
                    if (target.hasEffect(ChainHealEffect.class) || target.equals(getHolder()) || !target.isFriendly(getHolder())) {
                        continue;
                    }
                    final int newHealAmount = (int) (healAmount - (intialHeal * getReductionPerJump()));
                    if (newHealAmount > 0) {
                        Bukkit.getScheduler().runTaskLater(RaidCraft.getComponent(SkillsPlugin.class), () -> {
                            try {
                                castChainHeal(nextTarget, newHealAmount);
                            } catch (CombatException e) {
                                getHolder().sendMessage(ChatColor.RED + e.getMessage());
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
}
