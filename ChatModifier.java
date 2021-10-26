/* Decompiler 22ms, total 202ms, lines 180 */
package me.earth.phobos.features.modules.misc;

import java.text.SimpleDateFormat;
import java.util.Date;
import me.earth.phobos.event.events.PacketEvent.Receive;
import me.earth.phobos.event.events.PacketEvent.Send;
import me.earth.phobos.features.command.Command;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.modules.Module.Category;
import me.earth.phobos.features.modules.client.Managers;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.TextUtil;
import me.earth.phobos.util.Timer;
import me.earth.phobos.util.TextUtil.Color;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ChatModifier extends Module {
   public Setting<ChatModifier.Suffix> suffix;
   public Setting<Boolean> clean;
   public Setting<Boolean> infinite;
   public Setting<Boolean> autoQMain;
   public Setting<Boolean> qNotification;
   public Setting<Integer> qDelay;
   public Setting<Color> timeStamps;
   public Setting<Boolean> rainbowTimeStamps;
   public Setting<Color> bracket;
   public Setting<Boolean> space;
   public Setting<Boolean> all;
   public Setting<Boolean> shrug;
   public Setting<Boolean> disability;
   private final Timer timer;
   private static ChatModifier INSTANCE = new ChatModifier();

   public ChatModifier() {
      super("Chat", "Modifies your chat", Category.MISC, true, false, false);
      this.suffix = this.register(new Setting("Suffix", ChatModifier.Suffix.NONE, "Your Suffix."));
      this.clean = this.register(new Setting("CleanChat", false, "Cleans your chat"));
      this.infinite = this.register(new Setting("Infinite", false, "Makes your chat infinite."));
      this.autoQMain = this.register(new Setting("AutoQMain", false, "Spams AutoQMain"));
      this.qNotification = this.register(new Setting("QNotification", false, (v) -> {
         return (Boolean)this.autoQMain.getValue();
      }));
      this.qDelay = this.register(new Setting("QDelay", 9, 1, 90, (v) -> {
         return (Boolean)this.autoQMain.getValue();
      }));
      this.timeStamps = this.register(new Setting("Time", Color.NONE));
      this.rainbowTimeStamps = this.register(new Setting("RainbowTimeStamps", false, (v) -> {
         return this.timeStamps.getValue() != Color.NONE;
      }));
      this.bracket = this.register(new Setting("Bracket", Color.WHITE, (v) -> {
         return this.timeStamps.getValue() != Color.NONE;
      }));
      this.space = this.register(new Setting("Space", true, (v) -> {
         return this.timeStamps.getValue() != Color.NONE;
      }));
      this.all = this.register(new Setting("All", false, (v) -> {
         return this.timeStamps.getValue() != Color.NONE;
      }));
      this.shrug = this.register(new Setting("Shrug", false));
      this.disability = this.register(new Setting("Disability", false));
      this.timer = new Timer();
      this.setInstance();
   }

   private void setInstance() {
      INSTANCE = this;
   }

   public static ChatModifier getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new ChatModifier();
      }

      return INSTANCE;
   }

   public void onUpdate() {
      if ((Boolean)this.shrug.getValue()) {
         mc.field_71439_g.func_71165_d(TextUtil.shrug);
         this.shrug.setValue(false);
      }

      if ((Boolean)this.autoQMain.getValue()) {
         if (!this.shouldSendMessage(mc.field_71439_g)) {
            return;
         }

         if ((Boolean)this.qNotification.getValue()) {
            Command.sendMessage("<AutoQueueMain> Sending message: /queue main");
         }

         mc.field_71439_g.func_71165_d("/queue main");
         this.timer.reset();
      }

   }

   @SubscribeEvent
   public void onPacketSend(Send event) {
      if (event.getStage() == 0 && event.getPacket() instanceof CPacketChatMessage) {
         CPacketChatMessage packet = (CPacketChatMessage)event.getPacket();
         String s = packet.func_149439_c();
         if (s.startsWith("/")) {
            return;
         }

         switch((ChatModifier.Suffix)this.suffix.getValue()) {
         case EARTH:
            s = s + " ⏐ 3ᴀʀᴛʜʜ4ᴄᴋ";
            break;
         case PHOBOS:
            s = s + " ⏐ ᴘʜᴏʙᴏꜱ";
         }

         if (s.length() >= 256) {
            s = s.substring(0, 256);
         }

         packet.field_149440_a = s;
      }

   }

   @SubscribeEvent
   public void onChatPacketReceive(Receive event) {
      if (event.getStage() == 0 && event.getPacket() instanceof SPacketChat) {
      }

   }

   @SubscribeEvent
   public void onPacketReceive(Receive event) {
      if (event.getStage() == 0 && this.timeStamps.getValue() != Color.NONE && event.getPacket() instanceof SPacketChat) {
         if (!((SPacketChat)event.getPacket()).func_148916_d()) {
            return;
         }

         String originalMessage = ((SPacketChat)event.getPacket()).field_148919_a.func_150254_d();
         String message = this.getTimeString(originalMessage) + originalMessage;
         ((SPacketChat)event.getPacket()).field_148919_a = new TextComponentString(message);
      }

   }

   public String getTimeString(String message) {
      String date = (new SimpleDateFormat("k:mm")).format(new Date());
      if ((Boolean)this.rainbowTimeStamps.getValue()) {
         String timeString = "<" + date + ">" + ((Boolean)this.space.getValue() ? " " : "");
         StringBuilder builder = new StringBuilder(timeString);
         builder.insert(0, "§+");
         if (!message.contains(Managers.getInstance().getRainbowCommandMessage())) {
            builder.append("§r");
         }

         return builder.toString();
      } else {
         return (this.bracket.getValue() == Color.NONE ? "" : TextUtil.coloredString("<", (Color)this.bracket.getValue())) + TextUtil.coloredString(date, (Color)this.timeStamps.getValue()) + (this.bracket.getValue() == Color.NONE ? "" : TextUtil.coloredString(">", (Color)this.bracket.getValue())) + ((Boolean)this.space.getValue() ? " " : "") + "§r";
      }
   }

   private boolean shouldSendMessage(EntityPlayer player) {
      if (player.field_71093_bK != 1) {
         return false;
      } else {
         return !this.timer.passedS((double)(Integer)this.qDelay.getValue()) ? false : player.func_180425_c().equals(new Vec3i(0, 240, 0));
      }
   }

   public static enum Suffix {
      NONE,
      PHOBOS,
      EARTH;
   }
}
