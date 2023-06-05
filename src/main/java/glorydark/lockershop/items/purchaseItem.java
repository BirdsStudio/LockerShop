package glorydark.lockershop.items;

import cn.nukkit.Player;

public interface purchaseItem {

    void equip(Player player);

    void unequip(Player player);

    boolean claim(String player, String claimer, boolean purchase, Object... params);

    String getName();

    long getDuration();

    String getDisplayName();

    String getDescription();

    double getNeedMoney();

}
