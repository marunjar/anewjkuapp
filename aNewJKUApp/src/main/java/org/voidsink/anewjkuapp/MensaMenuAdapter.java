/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2018 Paul "Marunjar" Pretsch
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package org.voidsink.anewjkuapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.voidsink.anewjkuapp.base.RecyclerArrayAdapter;
import org.voidsink.anewjkuapp.mensa.IDay;
import org.voidsink.anewjkuapp.mensa.IMensa;
import org.voidsink.anewjkuapp.mensa.IMenu;
import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.utils.UIUtils;
import org.voidsink.sectionedrecycleradapter.SectionedAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MensaMenuAdapter extends RecyclerArrayAdapter<MensaItem, RecyclerView.ViewHolder> implements SectionedAdapter<MensaMenuAdapter.MenuHeaderHolder> {

    private static final DateFormat df = SimpleDateFormat.getDateInstance();

    private final boolean mUseDateHeader;

    public MensaMenuAdapter(Context context, boolean useDateHeader) {
        super(context);
        this.mUseDateHeader = useDateHeader;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case MensaItem.TYPE_INFO: {
                MensaInfoItem mensaInfoItem = (MensaInfoItem) getItem(position);
                ((MensaInfoHolder) holder).mTitle.setText(mensaInfoItem.getTitle());
                UIUtils.setTextAndVisibility(((MensaInfoHolder) holder).mDescr, mensaInfoItem.getDescr());
                break;
            }
            case MensaItem.TYPE_MENU: {
                MenuViewHolder mensaMenuItemHolder = (MenuViewHolder) holder;
                IMenu menu = getItem(position).getMenu();

                UIUtils.setTextAndVisibility(mensaMenuItemHolder.mName, menu.getName());
                UIUtils.setTextAndVisibility(mensaMenuItemHolder.mSoup, menu.getSoup());

                mensaMenuItemHolder.mMeal.setText(menu.getMeal());
                if (menu.getPrice() > 0) {
                    mensaMenuItemHolder.mPrice.setText(AppUtils.format(getContext(), "%.2f €",
                            menu.getPrice()));
                    mensaMenuItemHolder.mPrice.setVisibility(View.VISIBLE);

                    if (menu.getPriceBig() > 0 && menu.getPriceBig() != menu.getPrice()) {
                        mensaMenuItemHolder.mPriceBig.setText(AppUtils.format(getContext(), "%.2f €",
                                menu.getPriceBig()));
                        mensaMenuItemHolder.mPriceBig.setVisibility(View.VISIBLE);
                    } else {
                        mensaMenuItemHolder.mPriceBig.setVisibility(View.GONE);
                    }

                    if (menu.getOehBonus() > 0) {
                        mensaMenuItemHolder.mOehBonus.setText(AppUtils.format(getContext(),
                                "%.2f € ÖH Bonus", menu.getOehBonus()));
                        mensaMenuItemHolder.mOehBonus.setVisibility(View.VISIBLE);
                    } else {
                        mensaMenuItemHolder.mOehBonus.setVisibility(View.GONE);
                    }
                } else {
                    mensaMenuItemHolder.mPrice.setVisibility(View.GONE);
                    mensaMenuItemHolder.mPriceBig.setVisibility(View.GONE);
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
                final IDay day = item.getDay();
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
                final IMensa mensa = item.getMensa();
                if (mensa != null) {
                    return (long) mensa.getName().hashCode() + (long) Integer.MAX_VALUE; // header id has to be > 0???
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
                final IDay day = item.getDay();
                if (day != null) {
                    menuHeaderHolder.mText.setText(df.format(day.getDate()));
                }
            } else {
                IMensa mensa = item.getMensa();
                if (mensa != null) {
                    menuHeaderHolder.mText.setText(mensa.getName());
                }
            }
        }
    }

    static class MenuViewHolder extends RecyclerView.ViewHolder {

        final TextView mName;
        final TextView mSoup;
        final TextView mMeal;
        final TextView mPrice;
        final TextView mPriceBig;
        final TextView mOehBonus;

        MenuViewHolder(View itemView) {
            super(itemView);

            mName = itemView.findViewById(R.id.mensa_menu_item_name);
            mSoup = itemView.findViewById(R.id.mensa_menu_item_soup);
            mMeal = itemView.findViewById(R.id.mensa_menu_item_meal);
            mPrice = itemView.findViewById(R.id.mensa_menu_item_price);
            mPriceBig = itemView.findViewById(R.id.mensa_menu_item_price_big);
            mOehBonus = itemView.findViewById(R.id.mensa_menu_item_oeh_bonus);
        }
    }

    static class MensaInfoHolder extends RecyclerView.ViewHolder {
        final TextView mTitle;
        final TextView mDescr;

        MensaInfoHolder(View itemView) {
            super(itemView);
            mTitle = itemView.findViewById(R.id.mensa_info_item_title);
            mDescr = itemView.findViewById(R.id.mensa_info_item_descr);
        }
    }


    static class MenuHeaderHolder extends RecyclerView.ViewHolder {
        final TextView mText;

        MenuHeaderHolder(View itemView) {
            super(itemView);
            mText = itemView.findViewById(R.id.list_header_text);
        }
    }
}
