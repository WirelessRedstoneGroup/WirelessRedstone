package net.licks92.WirelessRedstone.Commands;

import net.licks92.WirelessRedstone.Signs.SignType;
import net.licks92.WirelessRedstone.Signs.WirelessChannel;
import net.licks92.WirelessRedstone.Signs.WirelessReceiver;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandInfo(description = "Create a WirelessPoint", usage = "<channel> <signtype> [sign details]", aliases = {"create", "c"},
        permission = "create", canUseInConsole = false, canUseInCommandBlock = false)
public class Create extends WirelessCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().commandTooFewArguments, sender, true);
            return;
        }

        if (WirelessRedstone.getUtils().getSignType(args[1]) == null) {
            WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().commandIncorrectSignType, sender, true);
            return;
        }

        String cname = args[0];
        SignType type = WirelessRedstone.getUtils().getSignType(args[1]);

        switch (type) {
            case RECEIVER_CLOCK:
            case RECEIVER_DELAYER:
                if (args.length < 3) {
                    WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().commandTooFewArguments, sender, true);
                    return;
                }
                break;
        }

        WirelessChannel channel = WirelessRedstone.getStorage().getWirelessChannel(cname);
        if (channel != null) {
            if (!hasAccessToChannel(sender, cname)) {
                WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().permissionChannelAccess, sender, true);
                return;
            }
        }

        Player player = (Player) sender;
        Location location = player.getLocation();

        if(location.getBlock().getType() != Material.AIR) {
            WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().commandInvalidSignLocation, sender, true);
            return;
        }

        location.getBlock().setType(Material.SIGN_POST);
        Sign sign = (Sign) location.getBlock().getState();
        sign.setLine(1, cname);

        switch (type) {
            case TRANSMITTER:
                sign.setLine(0, WirelessRedstone.getStringManager().tagsTransmitter.get(0));
                sign.update();
                WirelessRedstone.getSignManager().addWirelessTransmitter(cname, location.getBlock(), player);
                break;
            case SCREEN:
                sign.setLine(0, WirelessRedstone.getStringManager().tagsScreen.get(0));
                sign.update();
                WirelessRedstone.getSignManager().addWirelessScreen(cname, location.getBlock(), player);
                break;
            case RECEIVER_NORMAL:
                sign.setLine(0, WirelessRedstone.getStringManager().tagsReceiver.get(0));
                sign.setLine(2, WirelessRedstone.getStringManager().tagsReceiverDefaultType.get(0));
                sign.update();
                WirelessRedstone.getSignManager().addWirelessReceiver(cname, location.getBlock(), player, WirelessReceiver.Type.DEFAULT);
                break;
            case RECEIVER_INVERTER:
                sign.setLine(0, WirelessRedstone.getStringManager().tagsReceiver.get(0));
                sign.setLine(2, WirelessRedstone.getStringManager().tagsReceiverInverterType.get(0));
                sign.update();
                WirelessRedstone.getSignManager().addWirelessReceiver(cname, location.getBlock(), player, WirelessReceiver.Type.INVERTER);
                break;
            case RECEIVER_SWITCH:
                sign.setLine(0, WirelessRedstone.getStringManager().tagsReceiver.get(0));
                sign.setLine(2, WirelessRedstone.getStringManager().tagsReceiverSwitchType.get(0));
                sign.update();
                WirelessRedstone.getSignManager().addWirelessReceiver(cname, location.getBlock(), player, WirelessReceiver.Type.SWITCH);
                break;
            case RECEIVER_DELAYER:
                Integer delay;

                try {
                    delay = Integer.parseInt(args[2]);
                } catch (NumberFormatException ex) {
                    WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().delayNumberOnly, sender, true);
                    location.getBlock().setType(Material.AIR);
                    return;
                }

                sign.setLine(0, WirelessRedstone.getStringManager().tagsReceiver.get(0));
                sign.setLine(2, WirelessRedstone.getStringManager().tagsReceiverDelayerType.get(0));
                sign.setLine(3, delay.toString());
                sign.update();
                WirelessRedstone.getSignManager().addWirelessReceiver(cname, location.getBlock(), player, WirelessReceiver.Type.DELAYER);
                break;
            case RECEIVER_CLOCK:
                Integer pulse;

                try {
                    pulse = Integer.parseInt(args[2]);
                } catch (NumberFormatException ex) {
                    WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().intervalNumberOnly, sender, true);
                    location.getBlock().setType(Material.AIR);
                    return;
                }

                sign.setLine(0, WirelessRedstone.getStringManager().tagsReceiver.get(0));
                sign.setLine(2, WirelessRedstone.getStringManager().tagsReceiverClockType.get(0));
                sign.setLine(3, pulse.toString());
                sign.update();
                WirelessRedstone.getSignManager().addWirelessReceiver(cname, location.getBlock(), player, WirelessReceiver.Type.CLOCK);
                break;
        }
    }
}
