package org.voidsink.anewjkuapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;

import org.voidsink.anewjkuapp.base.RecyclerArrayAdapter;
import org.voidsink.anewjkuapp.mensa.Mensa;
import org.voidsink.anewjkuapp.mensa.MensaDay;
import org.voidsink.anewjkuapp.mensa.MensaMenu;
import org.voidsink.anewjkuapp.utils.UIUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MensaMenuAdapter extends RecyclerArrayAdapter<MensaItem, RecyclerView.ViewHolder> implements StickyRecyclerHeadersAdapter<MensaMenuAdapter.MenuHeaderHolder> {

    private static final DateFormat df = SimpleDateFormat.getDateInstance();

    private final Context mContext;
    protected boolean mUseDateHeader;

    public MensaMenuAdapter(Context context, boolean useDateHeader) {
        super();
        this.mContext = context;
        this.mUseDateHeader = useDateHeader;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case MensaItem.TYPE_INFO: {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.mensa_info_item, parent, false);
                return new MensaInfoHolder(v);
            }
            case MensaItem.TYPE_MENU: {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.mensa_menu_item, parent, false);
                return new MenuViewHolder(v);
            }
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case MensaItem.TYPE_INFO: {
                MensaInfoItem mensaInfoItem = (MensaInfoItem) getItem(position);
                ((MensaInfoHolder) holder).mTitle.setText(mensaInfoItem.getTitle());
                UIUtils.setTextAndVisibility(((MensaInfoHolder) holder).mDescr, mensaInfoItem.getDescr());
                break;
            }
            case MensaItem.TYPE_MENU: {
                MenuViewHolder mensaMenuItemHolder = (MenuViewHolder) holder;
                MensaMenu mensaMenuItem = (MensaMenu) getItem(position);

                UIUtils.setTextAndVisibility(mensaMenuItemHolder.mName, mensaMenuItem.getName());
                UIUtils.setTextAndVisibility(mensaMenuItemHolder.mSoup, mensaMenuItem.getSoup());

                mensaMenuItemHolder.mMeal.setText(mensaMenuItem.getMeal());
                if (mensaMenuItem.getPrice() > 0) {
                    mensaMenuItemHolder.mPrice.setText(String.format("%.2f €",
                            mensaMenuItem.getPrice()));
                    mensaMenuItemHolder.mPrice.setVisibility(View.VISIBLE);

                    if (mensaMenuItem.getOehBonus() > 0) {
                        mensaMenuItemHolder.mOehBonus.setText(String.format(
                                "inkl %.2f € ÖH Bonus", mensaMenuItem.getOehBonus()));
                        mensaMenuItemHolder.mOehBonus.setVisibility(View.VISIBLE);
                    } else {
                        mensaMenuItemHolder.mOehBonus.setVisibility(View.GONE);
                    }
                } else {
                    mensaMenuItemHolder.mPrice.setVisibility(View.GONE);
                    mensaMenuItemHolder.mOehBonus.setVisibility(View.GONE);
                }
                break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return this.getItem(position).getType();
    }

    @Override
    public long getHeaderId(int position) {
        MensaItem item = getItem(position);
        if (item != null) {
            if (mUseDateHeader) {
                final MensaDay day = item.getDay();
                if (day != null) {
                    Calendar cal = Calendar.getInstance(); // locale-specific
                    cal.setTimeInMillis(day.getDate().getTime());
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    return cal.getTimeInMillis();
                }
            } else {
                final Mensa mensa = item.getMensa();
                if (mensa != null) {
                    return (long)mensa.getName().hashCode() + (long)Integer.MAX_VALUE; // header id has to be > 0???
                }
            }
        }
        return 0;
    }

    @Override
    public MenuHeaderHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_header, viewGroup, false);
        return new MenuHeaderHolder(v);
    }

    @Override
    public void onBindHeaderViewHolder(MenuHeaderHolder menuHeaderHolder, int position) {
        MensaItem item = getItem(position);
        if (item != null) {
            if (mUseDateHeader) {
                final MensaDay day = item.getDay();
                if (day != null) {
                    menuHeaderHolder.mText.setText(df.format(day.getDate()));
                }
            } else {
                Mensa mensa = item.getMensa();
                if (mensa != null) {
                    menuHeaderHolder.mText.setText(mensa.getName());
                }
            }
        }
    }

    public static class MenuViewHolder extends RecyclerView.ViewHolder {

        public TextView mName;
        public TextView mSoup;
        public TextView mMeal;
        public TextView mPrice;
        public TextView mOehBonus;

        public MenuViewHolder(View itemView) {
            super(itemView);

            mName = (TextView) itemView.findViewById(R.id.mensa_menu_item_name);
            mSoup = (TextView) itemView.findViewById(R.id.mensa_menu_item_soup);
            mMeal = (TextView) itemView.findViewById(R.id.mensa_menu_item_meal);
            mPrice = (TextView) itemView.findViewById(R.id.mensa_menu_item_price);
            mOehBonus = (TextView) itemView.findViewById(R.id.mensa_menu_item_oeh_bonus);
        }
    }

    public static class MensaInfoHolder extends RecyclerView.ViewHolder {
        public TextView mTitle;
        public TextView mDescr;

        public MensaInfoHolder(View itemView) {
            super(itemView);
            mTitle = (TextView) itemView.findViewById(R.id.mensa_info_item_title);
            mDescr = (TextView) itemView.findViewById(R.id.mensa_info_item_descr);
        }
    }


    public class MenuHeaderHolder extends RecyclerView.ViewHolder {
        public TextView mText;

        public MenuHeaderHolder(View itemView) {
            super(itemView);
            mText = (TextView) itemView.findViewById(R.id.list_header_text);
        }

    }
}
