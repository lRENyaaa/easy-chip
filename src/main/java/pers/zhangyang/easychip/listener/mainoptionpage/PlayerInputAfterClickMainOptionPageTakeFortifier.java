package pers.zhangyang.easychip.listener.mainoptionpage;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pers.zhangyang.easychip.domain.Fortifier;
import pers.zhangyang.easychip.exception.NotExistFortifierException;
import pers.zhangyang.easychip.exception.NotMoreFortifierException;
import pers.zhangyang.easychip.meta.WorkStationMeta;
import pers.zhangyang.easychip.service.GuiService;
import pers.zhangyang.easychip.service.impl.GuiServiceImpl;
import pers.zhangyang.easychip.yaml.FortifierYaml;
import pers.zhangyang.easychip.yaml.MessageYaml;
import pers.zhangyang.easylibrary.base.FiniteInputListenerBase;
import pers.zhangyang.easylibrary.base.GuiPage;
import pers.zhangyang.easylibrary.util.ItemStackUtil;
import pers.zhangyang.easylibrary.util.MessageUtil;
import pers.zhangyang.easylibrary.util.PlayerUtil;
import pers.zhangyang.easylibrary.util.TransactionInvocationHandler;

import java.util.List;

public class PlayerInputAfterClickMainOptionPageTakeFortifier extends FiniteInputListenerBase {
    public PlayerInputAfterClickMainOptionPageTakeFortifier(Player player, OfflinePlayer owner, GuiPage previousPage) {
        super(player, owner, previousPage, 1);

        List<String> list = MessageYaml.INSTANCE.getStringList("message.chat.howToTakeFortifier");
        MessageUtil.sendMessageTo(player, list);
    }

    @Override
    public void run() {
        int amount;
        try {
            amount = Integer.parseInt(messages[0]);
        } catch (NumberFormatException e) {
            List<String> list = MessageYaml.INSTANCE.getStringList("message.chat.invalidNumber");
            MessageUtil.sendMessageTo(player, list);
            return;
        }
        if (amount < 0) {
            MessageUtil.sendMessageTo(player, MessageYaml.INSTANCE.getStringList("message.chat.invalidNumber"));
            return;
        }


        GuiService guiService = (GuiService) new TransactionInvocationHandler(new GuiServiceImpl()).getProxy();

        WorkStationMeta workStationMeta = guiService.getWorkStation(player.getUniqueId().toString());

        if (workStationMeta.getFortifierItemStack() == null) {
            MessageUtil.sendMessageTo(player, MessageYaml.INSTANCE.getStringList("message.chat.notExistFortifierWhenTakeFortifier"));
            return;
        }

        //检查配置文件是不是有这个强化剂
        Fortifier fortifier = null;
        for (Fortifier c : FortifierYaml.INSTANCE.listFortifier()) {
            if (c.getItemStack().equals(ItemStackUtil.itemStackDeserialize(workStationMeta.getFortifierItemStack()))) {
                fortifier = c;
                break;
            }
        }
        if (fortifier == null) {
            MessageUtil.sendMessageTo(player, MessageYaml.INSTANCE.getStringList("message.chat.notExistFortifierWhenTakeFortifier"));
            return;
        }

        //玩家是不是有空间
        if (PlayerUtil.checkSpace(player, fortifier.getItemStack()) < fortifier.getItemStack().getAmount() * amount) {
            MessageUtil.sendMessageTo(player, MessageYaml.INSTANCE.getStringList("message.chat.notEnoughSpaceWhenTakeFortifier"));
            return;
        }

        try {
            guiService.takeFortifier(owner.getUniqueId().toString(), amount);
        } catch (NotExistFortifierException e) {
            e.printStackTrace();
            return;
        } catch (NotMoreFortifierException e) {
            MessageUtil.sendMessageTo(player, MessageYaml.INSTANCE.getStringList("message.chat.notMoreFortifier"));
            return;
        }
        ItemStack chipItemStack = fortifier.getItemStack();
        PlayerUtil.addItem(player, chipItemStack, fortifier.getItemStack().getAmount() * amount);
        MessageUtil.sendMessageTo(player, MessageYaml.INSTANCE.getStringList("message.chat.takeFortifier"));


    }
}
