package net.licks92.WirelessRedstone.Commands;

import net.licks92.WirelessRedstone.Main;
import net.licks92.WirelessRedstone.Signs.SignType;
import net.licks92.WirelessRedstone.Signs.WirelessChannel;
import net.licks92.WirelessRedstone.Signs.WirelessReceiver;
import net.licks92.WirelessRedstone.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandInfo(description = "Create a WirelessPoint", usage = "<channel> <signtype> [sign details]", aliases = {"create", "c"},
        permission = "commands.create", canUseInConsole = false, canUseInCommandBlock = false)
public class Create extends WirelessCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            Utils.sendFeedback(Main.getStrings().tooFewArguments, sender, true);
            return;
        }

        if (Utils.getSignType(args[1]) == null) {
            Utils.sendFeedback("Incorrect sign type.", sender, true); //TODO: Add this string to the stringloader
            return;
        }

        String cname = args[0];
        SignType type = Utils.getSignType(args[1]);

        switch (type) {
            case RECEIVER_CLOCK:
            case RECEIVER_DELAYER:
                if (args.length < 3) {
                    Utils.sendFeedback(Main.getStrings().tooFewArguments, sender, true);
                    return;
                }
                break;
        }

        WirelessChannel channel = Main.getStorage().getWirelessChannel(cname);
        if (channel != null) {
            if (!hasAccessToChannel(sender, cname)) {
                Utils.sendFeedback(Main.getStrings().playerDoesntHaveAccessToChannel, sender, true);
                return;
            }
        }

        Player player = (Player) sender;
        Location location = player.getLocation();

        if(location.getBlock().getType() != Material.AIR) {
            Utils.sendFeedback("Can't create sign in your location.", sender, true); //TODO: Add this string to the stringloader
            return;
        }

        location.getBlock().setType(Material.SIGN_POST);
        Sign sign = (Sign) location.getBlock().getState();
        sign.setLine(1, cname);

        switch (type) {
            case TRANSMITTER:
                sign.setLine(0, Main.getStrings().tagsTransmitter.get(0));
                sign.update();
                Main.getSignManager().addWirelessTransmitter(cname, location.getBlock(), player);
                break;
            case SCREEN:
                sign.setLine(0, Main.getStrings().tagsScreen.get(0));
                sign.update();
                Main.getSignManager().addWirelessScreen(cname, location.getBlock(), player);
                break;
            case RECEIVER_NORMAL:
                sign.setLine(0, Main.getStrings().tagsReceiver.get(0));
                sign.setLine(2, Main.getStrings().tagsReceiverDefaultType.get(0));
                sign.update();
                Main.getSignManager().addWirelessReceiver(cname, location.getBlock(), player, WirelessReceiver.Type.DEFAULT);
                break;
            case RECEIVER_INVERTER:
                sign.setLine(0, Main.getStrings().tagsReceiver.get(0));
                sign.setLine(2, Main.getStrings().tagsReceiverInverterType.get(0));
                sign.update();
                Main.getSignManager().addWirelessReceiver(cname, location.getBlock(), player, WirelessReceiver.Type.INVERTER);
                break;
            case RECEIVER_SWITCH:
                sign.setLine(0, Main.getStrings().tagsReceiver.get(0));
                sign.setLine(2, Main.getStrings().tagsReceiverSwitchType.get(0));
                sign.update();
                Main.getSignManager().addWirelessReceiver(cname, location.getBlock(), player, WirelessReceiver.Type.SWITCH);
                break;
            case RECEIVER_DELAYER:
                Integer delay;

                try {
                    delay = Integer.parseInt(args[2]);
                } catch (NumberFormatException ex) {
                    Utils.sendFeedback("That is not a number.", sender, true); //TODO: Add this string to the stringloader
                    location.getBlock().setType(Material.AIR);
                    return;
                }

                sign.setLine(0, Main.getStrings().tagsReceiver.get(0));
                sign.setLine(2, Main.getStrings().tagsReceiverDelayerType.get(0));
                sign.setLine(3, delay.toString());
                sign.update();
                Main.getSignManager().addWirelessReceiver(cname, location.getBlock(), player, WirelessReceiver.Type.DELAYER);
                break;
            case RECEIVER_CLOCK:
                Integer pulse;

                try {
                    pulse = Integer.parseInt(args[2]);
                } catch (NumberFormatException ex) {
                    Utils.sendFeedback("That is not a number.", sender, true); //TODO: Add this string to the stringloader
                    location.getBlock().setType(Material.AIR);
                    return;
                }

                sign.setLine(0, Main.getStrings().tagsReceiver.get(0));
                sign.setLine(2, Main.getStrings().tagsReceiverClockType.get(0));
                sign.setLine(3, pulse.toString());
                sign.update();
                Main.getSignManager().addWirelessReceiver(cname, location.getBlock(), player, WirelessReceiver.Type.CLOCK);
                break;
        }
    }
}
