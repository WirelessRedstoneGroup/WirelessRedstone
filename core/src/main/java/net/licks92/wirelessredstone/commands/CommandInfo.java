package net.licks92.wirelessredstone.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CommandInfo {

    String description();
    String usage();
    String[] aliases();
    WirelessCommandTabCompletion[] tabCompletion() default {};
    String permission();

    boolean canUseInConsole();
    boolean canUseInCommandBlock();
}